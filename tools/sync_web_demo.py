#!/usr/bin/env python3
"""웹 체험판(docs/)을 앱 리소스와 맞춘다.

- app/src/main/assets/phrases_*.json → docs/demo/phrases_*.json (그대로 복사)
- app/src/main/res/values*/strings.xml → docs/demo/strings.js (window.RES)

문구나 strings.xml 을 고친 뒤 실행: python3 tools/sync_web_demo.py
"""
import json
import shutil
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
RES = ROOT / "app/src/main/res"
ASSETS = ROOT / "app/src/main/assets"
OUT = ROOT / "docs/demo"


def unescape(text: str) -> str:
    out, i = [], 0
    while i < len(text):
        c = text[i]
        if c == "\\" and i + 1 < len(text):
            n = text[i + 1]
            out.append({"n": "\n", "t": "\t"}.get(n, n))
            i += 2
            continue
        out.append(c)
        i += 1
    return "".join(out)


def load(path: Path) -> dict:
    strings, arrays = {}, {}
    for el in ET.parse(path).getroot():
        name = el.get("name")
        if el.tag == "string":
            strings[name] = unescape("".join(el.itertext()))
        elif el.tag == "string-array":
            arrays[name] = [unescape("".join(item.itertext())) for item in el]
    return {"strings": strings, "arrays": arrays}


def main() -> None:
    OUT.mkdir(parents=True, exist_ok=True)
    for lang in ("ko", "en"):
        shutil.copyfile(ASSETS / f"phrases_{lang}.json", OUT / f"phrases_{lang}.json")
    res = {"ko": load(RES / "values/strings.xml"), "en": load(RES / "values-en/strings.xml")}
    body = json.dumps(res, ensure_ascii=False, indent=1)
    (OUT / "strings.js").write_text(
        "// 자동 생성: tools/sync_web_demo.py (app/src/main/res/values*/strings.xml). 직접 고치지 마세요.\n"
        f"window.RES = {body};\n",
        encoding="utf-8",
    )
    print("synced", OUT)


if __name__ == "__main__":
    main()
