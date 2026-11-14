#!/usr/bin/env python3
"""Re-apply the Universal Sync Android rebrand after each upstream sync (idempotent)."""
from pathlib import Path
import os

ROOT = Path(__file__).resolve().parent.parent

# Functional anchors that must never change:
#  - *.universal sync.net relay/discovery hosts (public infrastructure the app needs)
#  -  /  (core build + ABI artifact names)
#  - lowercase 'universal sync' REST-API + protocol strings kept for core compatibility
ANCHORS = [
    '', '', '',
    '', '', '',
    '', '', '',
]
REPL = [
    ('UniversalSyncNative', 'UniversalSyncNative'),
    ('UNIVERSAL SYNC', 'UNIVERSAL SYNC'),
    ('UniversalSync', 'UniversalSync'),
    ('universal sync', 'universal sync'),
]

def main():
    TEXT_EXTS = {'.java', '.kt', '.kts', '.xml', '.md', '.txt', '.sh', '.py', '.cmd',
                 '.gradle', '.toml', '.properties', '.yml', '.yaml', '.json', '.mk', '.mk.bak'}
    SKIP = {'.git', 'scripts', 'node_modules'}
    changed = 0
    for dirpath, dirnames, filenames in os.walk(ROOT):
        rel = os.path.relpath(dirpath, ROOT).replace(os.sep, '/')
        dirnames[:] = [d for d in dirnames if rel.split('/')[0] not in SKIP]
        for fn in filenames:
            p = Path(dirpath) / fn
            if p.suffix.lower() not in TEXT_EXTS: continue
            try: s = p.read_text(encoding='utf-8')
            except Exception: continue
            o = s
            for a in ANCHORS: s = s.replace(a, chr(2) + chr(1))
            for a, b in REPL: s = s.replace(a, b)
            s = s.replace(chr(2) + chr(1), '')
            # collapse display mangles: 'universal sync[A-Za-z]' -> camel
            import re as _re
            s = _re.sub(r'universal sync([A-Za-z])', r'universalSync\1', s)
            if s != o:
                p.write_text(s, encoding='utf-8'); changed += 1
    print(f'migrate: {changed} files swept')

if __name__ == '__main__':
    main()
