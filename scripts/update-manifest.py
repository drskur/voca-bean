#!/usr/bin/env python3
"""Print GitHub release update.json; redirect stdout to the desired new file."""
import argparse
import hashlib
import json
from pathlib import Path

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument("apk", type=Path)
parser.add_argument("--version-code", type=int, required=True)
parser.add_argument("--version-name", required=True)
parser.add_argument("--notes", default="")
args = parser.parse_args()
if not 1 <= args.version_code <= 2_100_000_000:
    parser.error("version-code must be between 1 and 2100000000")
if not args.version_name.strip():
    parser.error("version-name must not be blank")
if args.apk.suffix != ".apk" or not args.apk.is_file():
    parser.error("apk must be an existing .apk file")
digest = hashlib.sha256()
with args.apk.open("rb") as apk:
    for chunk in iter(lambda: apk.read(1024 * 1024), b""):
        digest.update(chunk)
print(json.dumps({
    "versionCode": args.version_code,
    "versionName": args.version_name,
    "apkFile": args.apk.name,
    "sha256": digest.hexdigest(),
    "notes": args.notes,
}, ensure_ascii=False, indent=2))
