#!/usr/bin/env python3
"""Build platform launcher icons from KirinMaL's original kirin artwork.

Run: python tools/generate_brand_assets.py
Requires: Pillow.
"""
from __future__ import annotations

import json
from pathlib import Path
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "assets/branding/kirinmal-icon.webp"
ICON = Image.open(SOURCE).convert("RGBA")
BGCOLOR = (13, 15, 48, 255)


def output(path: str, image: Image.Image, **kwargs: object) -> None:
    target = ROOT / path
    target.parent.mkdir(parents=True, exist_ok=True)
    image.save(target, **kwargs)


def render(size: int, *, round_icon: bool = False) -> Image.Image:
    result = Image.new("RGBA", (size, size), BGCOLOR)
    symbol_size = round(size * 0.90)
    symbol = ICON.resize((symbol_size, symbol_size), Image.Resampling.LANCZOS)
    result.alpha_composite(symbol, ((size - symbol_size) // 2, (size - symbol_size) // 2))
    if round_icon:
        mask = Image.new("L", (size, size), 0)
        ImageDraw.Draw(mask).ellipse((0, 0, size - 1, size - 1), fill=255)
        result.putalpha(mask)
    return result


def transparent(size: int, ratio: float) -> Image.Image:
    background = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    art_size = round(size * ratio)
    art = ICON.resize((art_size, art_size), Image.Resampling.LANCZOS)
    background.alpha_composite(art, ((size-art_size)//2, (size-art_size)//2))
    return background


def main() -> None:
    densities = {
        "mdpi": 48, "hdpi": 72, "xhdpi": 96,
        "xxhdpi": 144, "xxxhdpi": 192,
    }
    prefix = "app/androidApp/src/main/res/"
    for density, size in densities.items():
        output(f"{prefix}mipmap-{density}/ic_launcher.png", render(size))
        output(f"{prefix}mipmap-{density}/ic_launcher_round.png", render(size, round_icon=True))
    output("app/androidApp/src/main/ic_launcher-playstore.png", render(512))
    output(f"{prefix}drawable-nodpi/kirinmal_foreground.png", transparent(432, .70))
    output(f"{prefix}drawable-nodpi/kirinmal_splash.png", transparent(256, .68))
    output("core/resources/src/commonMain/composeResources/drawable/kirinmal_logo.png", transparent(256, .94))
    output("app/desktopApp/icons/icon.png", render(512))
    output("app/desktopApp/icons/icon.ico", render(256).convert("RGBA"),
           format="ICO", sizes=[(16,16), (24,24), (32,32), (48,48), (64,64), (128,128), (256,256)])
    output("app/desktopApp/icons/icon.icns", render(1024).convert("RGB"), format="ICNS")
    ios = "app/iosApp/iosApp/Assets.xcassets/AppIcon.appiconset"
    output(f"{ios}/AppIcon.png", render(1024))
    (ROOT / ios / "Contents.json").write_text(
        json.dumps({
            "images": [{"filename": "AppIcon.png", "idiom": "universal",
                        "platform": "ios", "size": "1024x1024"}],
            "info": {"author": "xcode", "version": 1},
        }, indent=2) + "\n",
        encoding="utf-8",
    )
    print("KirinMaL icon assets generated for Android, iOS and Desktop.")


if __name__ == "__main__":
    main()
