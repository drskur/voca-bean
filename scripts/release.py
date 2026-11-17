#!/usr/bin/env python3
"""Validate the committed release version, then push branch and tag atomically."""
import argparse
from pathlib import Path
import re
import subprocess
import sys


def git(*args):
    return subprocess.check_output(["git", *args], text=True).strip()


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--check", action="store_true", help="검사만 수행")
    args = parser.parse_args()
    root = Path(git("rev-parse", "--show-toplevel"))
    props = dict(
        line.strip().split("=", 1)
        for line in (root / "version.properties").read_text().splitlines()
        if line.strip() and not line.lstrip().startswith("#")
    )
    code = int(props["VERSION_CODE"])
    name = props["VERSION_NAME"].strip()
    if not 1 <= code <= 2_100_000_000:
        raise ValueError("VERSION_CODE 범위가 올바르지 않습니다.")
    if not re.fullmatch(r"\d+\.\d+\.\d+", name):
        raise ValueError("VERSION_NAME은 1.0.0 형태여야 합니다.")
    tag = f"v{name}"
    if git("status", "--porcelain"):
        raise ValueError("변경 사항을 먼저 커밋하세요. 작업 트리가 깨끗해야 합니다.")
    branch = git("branch", "--show-current")
    if not branch:
        raise ValueError("Detached HEAD에서는 배포할 수 없습니다.")
    git("remote", "get-url", "origin")
    if git("tag", "--list", tag):
        raise ValueError(f"로컬 태그 {tag}가 이미 있습니다. 새 버전으로 배포하세요.")
    if git("ls-remote", "--tags", "origin", f"refs/tags/{tag}"):
        raise ValueError(f"원격 태그 {tag}가 이미 있습니다. 새 버전으로 배포하세요.")
    for path in (".github/workflows/release.yml", "scripts/update-manifest.py", "version.properties"):
        git("ls-files", "--error-unmatch", path)
    print(f"배포 검사 통과: {tag}, versionCode={code}, branch={branch}", flush=True)
    print("이전 배포보다 VERSION_CODE가 증가했는지는 별도로 확인하세요.", flush=True)
    if args.check:
        return
    subprocess.run(["git", "tag", "-a", tag, "-m", f"VocaBean {name} 배포"], check=True)
    # No force push: remote branch conflicts fail without publishing either ref.
    try:
        subprocess.run([
            "git", "push", "--atomic", "origin",
            f"HEAD:refs/heads/{branch}", f"refs/tags/{tag}",
        ], check=True)
    except subprocess.CalledProcessError:
        print(
            f"push 실패. 로컬 태그 {tag}는 보존했습니다. 원인을 해결한 뒤 "
            f"git push --atomic origin HEAD:refs/heads/{branch} refs/tags/{tag} 로 재시도하세요.",
            file=sys.stderr,
        )
        raise
    print(f"{tag} push 완료. GitHub Actions에서 배포 상태를 확인하세요.")


if __name__ == "__main__":
    try:
        main()
    except (ValueError, KeyError, subprocess.CalledProcessError) as error:
        print(f"배포 중단: {error}", file=sys.stderr)
        sys.exit(1)
