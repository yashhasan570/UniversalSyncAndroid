#!/usr/bin/env python3
"""Re-apply the Universal Sync Android rebrand after each upstream sync (idempotent)."""
from pathlib import Path
import os, glob
from PIL import Image

ROOT = Path(__file__).resolve().parent.parent

def main():
    PROTECT = ['libsyncthingnative.so', 'build-syncthing.py']
    REPL = [
        ('com.nutomic.syncthingandroid','com.universalsync.android'),
        ('com.github.catfriend1.syncthingfork','com.universalsync.android'),
        ('github.com/Catfriend1/syncthing-android','github.com/yashhasan570/UniversalSyncAndroid'),
        ('github.com/Catfriend1/syncthingfork','github.com/yashhasan570/UniversalSyncAndroid'),
        ('Catfriend1','yashhasan570'),
        ('catfriend1','yashhasan570'),
        ('syncthing-android','UniversalSyncAndroid'),
        ('syncthing-fork','UniversalSync'),
        ('Syncthing','UniversalSync'),
        ('syncthing','universalsync'),
        ('Universal SyncService','UniversalSyncService'),
        ('Universal SyncRunnable','UniversalSyncRunnable'),
        ('Universal SyncApp','UniversalSyncApp'),
        ('Universal SyncActivity','UniversalSyncActivity'),
        ('Universal SyncModule','UniversalSyncModule'),
        ('Universal SyncServiceBinder','UniversalSyncServiceBinder'),
        ('Universal SyncTrustManager','UniversalSyncTrustManager'),
    ]
    TEXT_EXTS = {'.java','.kt','.kts','.gradle','.xml','.md','.txt','.py','.sh','.cmd','.pro','.properties','.yml','.yaml','.mk','.json','.html'}
    SKIP = {'.git','node_modules','scripts'}
    changed = 0
    for dirpath, dirnames, filenames in os.walk(ROOT):
        dirnames[:] = [d for d in dirnames if d not in SKIP]
        for fn in filenames:
            p = Path(dirpath)/fn
            if p.name == 'libSyncthingNative.mk': continue
            if p.suffix.lower() not in TEXT_EXTS: continue
            try: s = p.read_text(encoding='utf-8')
            except Exception: continue
            o = s
            for a in PROTECT: s = s.replace(a, '__PT__'+a)
            for a,b in REPL: s = s.replace(a,b)
            import re as _re
            s = _re.sub(r'Universal Sync([A-Za-z])', r'UniversalSync\1', s)
            s = s.replace('__PT__','')
            if s != o:
                p.write_text(s, encoding='utf-8'); changed += 1
    badge = ROOT/'scripts/icons/badge.png'
    n = 0
    if badge.exists():
        img = Image.open(badge).convert('RGBA')
        targets = glob.glob(str(ROOT/'app/src/main/res/drawable*ic_universalsync_logo*.png'))
        for t in targets:
            p = Path(t)
            try:
                ref = Image.open(p); w,h = ref.size
                canvas = Image.new('RGBA',(w,h),(0,0,0,0))
                inner = img.resize((w,h), Image.LANCZOS)
                canvas.paste(inner,(0,0),inner)
                canvas.save(p)
                n += 1
            except Exception:
                pass
    print(f'migrate: {changed} files swept, {n} icon files updated')

if __name__ == '__main__':
    main()
