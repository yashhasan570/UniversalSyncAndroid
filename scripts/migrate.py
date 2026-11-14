#!/usr/bin/env python3
"""Re-apply the Universal Sync Android rebrand after each upstream sync (idempotent)."""
from pathlib import Path
import os, shutil
from PIL import Image

ROOT = Path(__file__).resolve().parent.parent

def main():
    REPL = [
        ('com.nutomic.syncthingandroid','com.universalsync.android'),
        ('com.github.catfriend1.syncthingfork','com.universalsync.android'),
        ('github.com/Catfriend1/syncthing-android','github.com/yashhasan570/UniversalSyncAndroid'),
        ('github.com/Catfriend1/syncthingfork','github.com/yashhasan570/UniversalSyncAndroid'),
        ('Catfriend1','UniversalSync'),
        ('catfriend1','universalsync'),
        ('researchxxl','universalsync'),
        ('nutomic','universalsync'),
        ('Syncthing','Universal Sync'),
    ]
    TEXT_EXTS = {'.kt','.java','.xml','.kts','.gradle','.md','.json','.yml','.yaml','.txt','.properties','.pro','.html','.js','.sh','.cmd','.bash','.py'}
    SKIP = {'.git','node_modules','scripts'}
    changed = 0
    for dirpath, dirnames, filenames in os.walk(ROOT):
        dirnames[:] = [d for d in dirnames if d not in SKIP]
        for fn in filenames:
            p = Path(dirpath)/fn
            if p.suffix.lower() not in TEXT_EXTS and p.suffix != '': continue
            try: s = p.read_text(encoding='utf-8')
            except Exception: continue
            o = s
            for a,b in REPL: s = s.replace(a,b)
            if s != o:
                p.write_text(s, encoding='utf-8'); changed += 1
    moved = 0
    for src in list(ROOT.glob('**/com/nutomic/syncthingandroid')):
        if '.git' in src.parts: continue
        target = src.parents[2] / 'com' / 'universalsync' / 'android'
        target.parent.mkdir(parents=True, exist_ok=True)
        if target.exists():
            for item in src.iterdir():
                shutil.move(str(item), str(target/item.name))
        else:
            src.rename(target)
        for pdir in [src.parent, src.parent.parent, src.parent.parent.parent]:
            try: pdir.rmdir()
            except OSError: pass
        moved += 1
    badge = ROOT/'scripts/icons/badge.png'
    n = 0
    if badge.exists():
        img = Image.open(badge).convert('RGBA')
        for p in (ROOT/'app/src/main/res').glob('mipmap-*/ic_launcher*.png'):
            ref = Image.open(p); w,h = ref.size
            if 'background' in p.name and 'foreground' not in p.name:
                Image.new('RGBA',(w,h),(13,27,42,255)).save(p)
            elif 'foreground' in p.name:
                canvas = Image.new('RGBA',(w,h),(0,0,0,0))
                inner = img.resize((int(w*0.66),int(w*0.66)), Image.LANCZOS)
                canvas.paste(inner, ((w-inner.size[0])//2,(h-inner.size[1])//2), inner)
                canvas.save(p)
            else:
                canvas = Image.new('RGBA',(w,h),(0,0,0,0))
                inner = img.resize((w,h), Image.LANCZOS)
                canvas.paste(inner,(0,0),inner)
                canvas.save(p)
            n+=1
    print(f'migrate: {changed} files swept, {moved} package dirs moved, {n} icon files updated')

if __name__ == '__main__':
    main()
