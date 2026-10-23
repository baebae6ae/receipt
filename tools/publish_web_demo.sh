#!/bin/sh
# docs/ 를 gh-pages 브랜치로 올려 GitHub Pages(https://baebae6ae.github.io/receipt/)를 갱신한다.
# 문구·문자열을 고쳤다면 먼저: python3 tools/sync_web_demo.py && 커밋
set -e
cd "$(dirname "$0")/.."
git subtree split --prefix docs -b gh-pages
git push origin gh-pages
