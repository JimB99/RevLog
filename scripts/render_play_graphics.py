#!/usr/bin/env python3
"""Render Google Play icon and feature graphic from ic_launcher_foreground.png."""
from __future__ import annotations

import math
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[1]
FOREGROUND = ROOT / "app/src/main/res/drawable-nodpi/ic_launcher_foreground.png"
OUT_DIR = ROOT / "docs/play-store"

GRADIENT_START = (0x0D, 0x4F, 0x5C)
GRADIENT_END = (0x1A, 0x6B, 0x7A)


def linear_gradient(size: tuple[int, int], angle_deg: float = 135) -> Image.Image:
    w, h = size
    img = Image.new("RGB", (w, h))
    px = img.load()
    rad = math.radians(angle_deg)
    cos_a, sin_a = math.cos(rad), math.sin(rad)
    for y in range(h):
        for x in range(w):
            t = (x / max(w - 1, 1) * cos_a + y / max(h - 1, 1) * sin_a + 1) / 2
            t = max(0.0, min(1.0, t))
            r = int(GRADIENT_START[0] + (GRADIENT_END[0] - GRADIENT_START[0]) * t)
            g = int(GRADIENT_START[1] + (GRADIENT_END[1] - GRADIENT_START[1]) * t)
            b = int(GRADIENT_START[2] + (GRADIENT_END[2] - GRADIENT_START[2]) * t)
            px[x, y] = (r, g, b)
    return img


def main() -> None:
    OUT_DIR.mkdir(parents=True, exist_ok=True)
    icon_src = Image.open(FOREGROUND).convert("RGBA")

    icon_512 = icon_src.resize((512, 512), Image.Resampling.LANCZOS)
    icon_path = OUT_DIR / "play-store-icon-512.png"
    icon_512.save(icon_path, format="PNG", optimize=True)
    print(f"Wrote {icon_path} ({icon_path.stat().st_size // 1024} KiB)")

    fg_w, fg_h = 1024, 500
    margin_r = 40
    banner = linear_gradient((fg_w, fg_h))

    icon_side = 280
    icon_on_banner = icon_src.resize((icon_side, icon_side), Image.Resampling.LANCZOS)
    icon_x = 56
    banner.paste(icon_on_banner, (icon_x, (fg_h - icon_side) // 2), icon_on_banner)

    draw = ImageDraw.Draw(banner)
    title = "RevLog"
    subtitle_lines = ("Vehicle logbook for", "cars & motorcycles")
    tagline = "Service history · Backups · Reminders · Offline"

    text_x = icon_x + icon_side + 48
    max_text_w = fg_w - text_x - margin_r

    font_title = _fit_font(draw, title, max_text_w, start_size=68, bold=True)
    font_sub = _fit_font(draw, subtitle_lines[0], max_text_w, start_size=34, bold=False)
    font_tag = _fit_font(draw, tagline, max_text_w, start_size=24, bold=False)

    sub_h = _line_height(font_sub)
    block_top = (fg_h - (68 + sub_h * 2 + 36 + 8)) // 2
    y = max(48, block_top)
    draw.text((text_x, y), title, fill=(255, 255, 255), font=font_title)
    y += 78
    for line in subtitle_lines:
        draw.text((text_x, y), line, fill=(220, 235, 238), font=font_sub)
        y += sub_h + 4
    y += 8
    draw.text((text_x, y), tagline, fill=(180, 210, 216), font=font_tag)

    fg_path = OUT_DIR / "play-store-feature-graphic-1024x500.png"
    banner.save(fg_path, format="PNG", optimize=True)
    print(f"Wrote {fg_path} ({fg_path.stat().st_size // 1024} KiB)")


def _text_width(draw: ImageDraw.ImageDraw, text: str, font: ImageFont.ImageFont) -> int:
    bbox = draw.textbbox((0, 0), text, font=font)
    return bbox[2] - bbox[0]


def _line_height(font: ImageFont.ImageFont) -> int:
    bbox = font.getbbox("Ag")
    return bbox[3] - bbox[1]


def _fit_font(
    draw: ImageDraw.ImageDraw,
    text: str,
    max_width: int,
    *,
    start_size: int,
    bold: bool,
    min_size: int = 18,
) -> ImageFont.FreeTypeFont | ImageFont.ImageFont:
    size = start_size
    while size >= min_size:
        font = _load_font(size, bold=bold)
        if _text_width(draw, text, font) <= max_width:
            return font
        size -= 2
    return _load_font(min_size, bold=bold)


def _load_font(size: int, bold: bool) -> ImageFont.FreeTypeFont | ImageFont.ImageFont:
    candidates = []
    if bold:
        candidates.extend(
            [
                "C:/Windows/Fonts/segoeuib.ttf",
                "C:/Windows/Fonts/arialbd.ttf",
            ]
        )
    else:
        candidates.extend(
            [
                "C:/Windows/Fonts/segoeui.ttf",
                "C:/Windows/Fonts/arial.ttf",
            ]
        )
    for path in candidates:
        p = Path(path)
        if p.is_file():
            return ImageFont.truetype(str(p), size=size)
    return ImageFont.load_default()


if __name__ == "__main__":
    main()