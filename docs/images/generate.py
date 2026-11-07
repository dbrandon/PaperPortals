#!/usr/bin/env python3
"""Generate the README illustrations as Minecraft-style pixel-art SVGs.

All sprites and textures here are original pixel art drawn in the style of
Minecraft; no game assets are used. Run from anywhere:

    python3 docs/images/generate.py

Outputs land next to this script (and in ./items/).
"""
import random
from pathlib import Path

OUT = Path(__file__).resolve().parent
FONT = "'Minecraftia','Monocraft','DejaVu Sans Mono','Menlo','Consolas',monospace"

# Minecraft chat colours used by the plugin's item names and lore
MC_GREEN, MC_DARK_PURPLE, MC_GOLD = "#55FF55", "#AA00AA", "#FFAA00"
MC_RED, MC_GRAY, MC_WHITE = "#FF5555", "#AAAAAA", "#FFFFFF"


# --------------------------------------------------------------------------
# Pixel helpers
# --------------------------------------------------------------------------

def runs(grid, palette, overlap=0.0):
    """Turn a grid of palette keys (or hex colours) into row-run <rect>s."""
    out = []
    for y, row in enumerate(grid):
        x = 0
        while x < len(row):
            key = row[x]
            colour = palette.get(key, key) if palette else key
            start = x
            while x < len(row) and row[x] == key:
                x += 1
            if colour in (".", None):
                continue
            out.append(
                f'<rect x="{start}" y="{y}" width="{x - start + overlap:g}" '
                f'height="{1 + overlap:g}" fill="{colour}"/>'
            )
    return "".join(out)


def sprite_def(sid, rows, palette, glint=False):
    """A 16x16 flat item sprite, optionally with an animated enchant glint."""
    assert len(rows) == 16 and all(len(r) == 16 for r in rows), sid
    body = runs(rows, palette)
    if glint:
        mask = [["#c27bff" if c != "." else "." for c in r] for r in rows]
        body += (
            f'<g opacity="0.25">{runs(mask, None)}'
            '<animate attributeName="opacity" values="0.1;0.45;0.1" dur="3s" '
            'repeatCount="indefinite"/></g>'
        )
    return f'<g id="{sid}">{body}</g>'


def texture(seed, fn):
    rnd = random.Random(seed)
    return [[fn(u, v, rnd) for u in range(16)] for v in range(16)]


def pick(rnd, pal, weights=None):
    return rnd.choices(pal, weights=weights)[0]


# --------------------------------------------------------------------------
# Item sprites (16x16, original art)
# --------------------------------------------------------------------------

EYE = ([
    "................",
    "......kkkk......",
    "....kkGGGGkk....",
    "...kGGggggGGk...",
    "..kGWggTTgggGk..",
    "..kGggTttTggGk..",
    ".kGggTtPPtTggGk.",
    ".kGgTtPPPPtTgGk.",
    ".kGgTtPPPPtTgGk.",
    ".kGggTtPPtTggGk.",
    "..kGggTttTggGk..",
    "..kGgggTTgggGk..",
    "...kGGggggGGk...",
    "....kkGGGGkk....",
    "......kkkk......",
    "................",
], {"k": "#0b2a22", "G": "#1e6b52", "g": "#2f9e78", "T": "#5fd6b0",
    "t": "#a8f0d8", "P": "#0d1f1a", "W": "#d8fff0"})

FLINT_AND_STEEL = ([
    "................",
    "..........sss...",
    ".........sWWWs..",
    "........sWsssWs.",
    "........sWs.sWs.",
    ".........ss.sWs.",
    "...........sWs..",
    "......kk..sWs...",
    ".....kFFk..s....",
    "....kFDFFk......",
    "...kFDFFFk......",
    "...kFFFFk.......",
    "..kFFFFk........",
    "..kFFk..........",
    "...kk...........",
    "................",
], {"s": "#3a3a3a", "W": "#d0d0d0", "k": "#1a1a1a", "F": "#4b4b4b",
    "D": "#7a7a7a"})

DIAMOND = ([
    "................",
    "................",
    "................",
    ".....kkkkkk.....",
    "....kWWCCCCk....",
    "...kWCCCCCCck...",
    "..kCWCCCCCCcck..",
    "..kCCCCCCCCcck..",
    "...kCCCCCCcck...",
    "....kCCCCcck....",
    ".....kCCcck.....",
    "......kcck......",
    ".......kk.......",
    "................",
    "................",
    "................",
], {"k": "#0e4b46", "C": "#4ee6da", "W": "#e8fffc", "c": "#22a99d"})

NETHERITE = ([
    "................",
    "................",
    "................",
    "................",
    "....kkkkkkkkk...",
    "...kLLLLLLLLk...",
    "..kLHLLLLLLkNk..",
    ".kLLLLLLLLkNNk..",
    ".kkkkkkkkkkNk...",
    ".kNNNNNNNNkk....",
    ".kNNNNNNNNk.....",
    ".kkkkkkkkkk.....",
    "................",
    "................",
    "................",
    "................",
], {"L": "#5d5458", "H": "#8a7f84", "N": "#3a3236", "k": "#1a1517"})

DYE = ([
    "................",
    "................",
    ".......kk.......",
    "......kPPk......",
    ".....kPHPPk.....",
    ".....kPPPPk.....",
    "....kPPPPPPk....",
    "...kPPPPPPPPk...",
    "...kPPPPPPPDk...",
    "...kPPPPPPDDk...",
    "....kPPPPDDk....",
    ".....kkkkkk.....",
    "................",
    "................",
    "................",
    "................",
], {"P": "#8a3fd1", "H": "#c792f5", "D": "#5b2391", "k": "#2a0f45"})

ANVIL = ([
    "................",
    "................",
    "................",
    "..kkkkkkkkkkkk..",
    "..kHHHHHHHHHHk..",
    "..kAAAAAAAAAAk..",
    "...kkkAAAAkkk...",
    ".....kAAAAk.....",
    ".....kaAAak.....",
    "....kkaaaakk....",
    "...kHHHHHHHHk...",
    "...kAAAAAAAAk...",
    "...kkkkkkkkkk...",
    "................",
    "................",
    "................",
], {"H": "#a8a8a8", "A": "#6e6e6e", "a": "#4a4a4a", "k": "#1e1e1e"})

SPRITES = {
    "eye": EYE, "flint_and_steel": FLINT_AND_STEEL, "diamond": DIAMOND,
    "netherite_ingot": NETHERITE, "dye": DYE, "anvil": ANVIL,
}
GLINT = {"eye_glint": "eye", "igniter": "flint_and_steel"}


# --------------------------------------------------------------------------
# Block textures (16x16, procedurally generated)
# --------------------------------------------------------------------------

def tex_grass_top(u, v, r):
    return pick(r, ["#5b8c32", "#6a9e3a", "#7cb342", "#4f7d2b"])


DIRT = ["#79553a", "#8b6243", "#6b4a31", "#5d3f29"]


def make_grass_side():
    r = random.Random(7)
    drip = [r.randint(2, 5) for _ in range(16)]
    return [[pick(r, ["#5b8c32", "#6a9e3a", "#7cb342"]) if v < drip[u]
             else pick(r, DIRT) for u in range(16)] for v in range(16)]


def tex_netherrack(u, v, r):
    return pick(r, ["#6f2f2f", "#7f3535", "#8f3b3b", "#5e2626", "#9c4a4a"],
                [3, 3, 2, 2, 1])


def tex_end_stone(u, v, r):
    return pick(r, ["#dbdea0", "#e6e8b0", "#cfd290", "#c4c784"], [4, 3, 2, 1])


def tex_redstone_block(u, v, r):
    if u in (0, 15) or v in (0, 15):
        return "#7c0e08"
    return pick(r, ["#b01a10", "#c4241a", "#9a140c", "#e0402c"], [4, 3, 2, 1])


def tex_stone_bricks(u, v, r):
    if v % 4 == 3:
        return "#585858"
    offset = 0 if (v // 4) % 2 == 0 else 4
    if (u + offset) % 8 == 7:
        return "#585858"
    if v % 4 == 0:
        return pick(r, ["#9a9a9a", "#919191"])
    return pick(r, ["#7d7d7d", "#868686", "#757575", "#8f8f8f"])


def tex_carto_top(u, v, r):
    if u in (0, 15) or v in (0, 15):
        return "#4a3220"
    if (u, v) in {(4, 5), (5, 5), (6, 6), (7, 6), (8, 7), (9, 7), (10, 8),
                  (11, 9), (11, 10)}:
        return "#8b5a2b"
    if 3 <= u <= 6 and 9 <= v <= 12:
        return "#7aa65a"
    return pick(r, ["#e8dcb8", "#dfd2aa", "#efe5c6"])


def tex_carto_side(u, v, r):
    if v in (0, 15) or u in (0, 15):
        return "#2e1f12"
    if v == 7:
        return "#3a2817"
    if v in (1, 2) and 2 <= u <= 13:
        return pick(r, ["#e8dcb8", "#dfd2aa"])
    return pick(r, ["#5a3d24", "#4d331e", "#664629"])


def make_portal(phase):
    import math
    pal = ["#3b0a7a", "#5a14b0", "#7a22d6", "#9b45f0", "#c48aff"]
    r = random.Random(11 + phase)
    grid = []
    for v in range(16):
        row = []
        for u in range(16):
            val = (math.sin(u * 0.55 + math.sin(v * 0.45 + phase) * 2.2 + phase)
                   + math.cos(v * 0.5 - u * 0.2) * 0.6 + r.uniform(-0.35, 0.35))
            idx = max(0, min(4, int((val + 1.8) / 3.6 * 5)))
            row.append(pal[idx])
        grid.append(row)
    return grid


TEXTURES = {
    "grass_top": texture(3, tex_grass_top),
    "grass_side": make_grass_side(),
    "netherrack": texture(5, tex_netherrack),
    "end_stone": texture(9, tex_end_stone),
    "redstone_block": texture(13, tex_redstone_block),
    "stone_bricks": texture(17, tex_stone_bricks),
    "carto_top": texture(19, tex_carto_top),
    "carto_side": texture(23, tex_carto_side),
}

BLOCKS = {  # name: (top, left, right)
    "grass_block": ("grass_top", "grass_side", "grass_side"),
    "netherrack": ("netherrack",) * 3,
    "end_stone": ("end_stone",) * 3,
    "redstone_block": ("redstone_block",) * 3,
    "stone_bricks": ("stone_bricks",) * 3,
    "cartography_table": ("carto_top", "carto_side", "carto_side"),
    "portal": ("portal",) * 3,
}


def texture_defs(names):
    defs = []
    for name in sorted(names):
        if name == "portal":
            a, b = make_portal(0), make_portal(2)
            defs.append(
                f'<g id="tex-portal">{runs(a, None, 0.03)}<g>{runs(b, None, 0.03)}'
                '<animate attributeName="opacity" values="0;1;0" dur="2.4s" '
                'repeatCount="indefinite"/></g></g>')
        else:
            defs.append(f'<g id="tex-{name}">{runs(TEXTURES[name], None, 0.03)}</g>')
    return defs


# --------------------------------------------------------------------------
# Isometric projection.  Block (x, y, z) -> screen; 1 block = 16 texels.
# Visible faces: top (y+1), "left" (z+1), "right" (x+1).
# --------------------------------------------------------------------------

def iso(x, y, z):
    return 16 * (x - z), 8 * (x + z) - 16 * y


def face(tex, origin, du, dv, corners, shade):
    ox, oy = iso(*origin)
    pts = " ".join(f"{px:g},{py:g}" for px, py in (iso(*c) for c in corners))
    s = (f'<use href="#tex-{tex}" transform="matrix({du[0]:g},{du[1]:g},'
         f'{dv[0]:g},{dv[1]:g},{ox:g},{oy:g})"/>')
    if shade:
        s += f'<polygon points="{pts}" fill="#000" opacity="{shade}"/>'
    return s


def cube(block, x, y, z, faces=("top", "left", "right")):
    top, left, right = BLOCKS[block]
    out = []
    if "top" in faces:
        out.append(face(top, (x, y + 1, z), (1, .5), (-1, .5), [], 0))
    if "left" in faces:
        out.append(face(left, (x, y + 1, z + 1), (1, .5), (0, 1),
                        [(x, y + 1, z + 1), (x + 1, y + 1, z + 1),
                         (x + 1, y, z + 1), (x, y, z + 1)], 0.18))
    if "right" in faces:
        out.append(face(right, (x + 1, y + 1, z + 1), (1, -.5), (0, 1),
                        [(x + 1, y + 1, z + 1), (x + 1, y + 1, z),
                         (x + 1, y, z), (x + 1, y, z + 1)], 0.38))
    return "".join(out)


def portal_pane(x, y, z):
    """A vertical portal sheet in the middle of block (x, y, z), facing +z."""
    return face("portal", (x, y + 1, z + .5), (1, .5), (0, 1), [], 0)


def scene(items):
    """items: (sort_key, svg).  Returns (svg, bbox)."""
    items.sort(key=lambda t: t[0])
    return "".join(s for _, s in items)


def bbox(blocks):
    xs, ys = [], []
    for (x, y, z) in blocks:
        for dx in (0, 1):
            for dy in (0, 1):
                for dz in (0, 1):
                    px, py = iso(x + dx, y + dy, z + dz)
                    xs.append(px)
                    ys.append(py)
    return min(xs), min(ys), max(xs), max(ys)


def standing_portal(w=4, h=5):
    items, cells = [], []
    for gx in range(w):
        for gy in range(h):
            cells.append((gx, gy, 0))
            edge = gx in (0, w - 1) or gy in (0, h - 1)
            svg = cube("stone_bricks", gx, gy, 0) if edge else portal_pane(gx, gy, 0)
            items.append(((gx, gy), svg))
    return scene(items), bbox(cells)


def floor_portal(n=4):
    items, cells = [], []
    for gx in range(n):
        for gz in range(n):
            cells.append((gx, 0, gz))
            edge = gx in (0, n - 1) or gz in (0, n - 1)
            if edge:
                svg = cube("stone_bricks", gx, 0, gz)
            else:
                svg = cube("portal", gx, 0, gz, faces=("top",))
            items.append(((gx + gz, gx), svg))
    return scene(items), bbox(cells)


def fit(svg, box, x, y, size):
    """Place an iso scene so its bbox fits in a size x size square at (x, y)."""
    x0, y0, x1, y1 = box
    s = size / max(x1 - x0, y1 - y0)
    cx = x + (size - (x1 - x0) * s) / 2 - x0 * s
    cy = y + (size - (y1 - y0) * s) / 2 - y0 * s
    return f'<g transform="translate({cx:g},{cy:g}) scale({s:g})">{svg}</g>'


def block_icon(block, x, y, size=16):
    return fit(cube(block, 0, 0, 0), (-16, -16, 16, 16), x + size * .06,
               y + size * .06, size * .88)


def item_icon(sid, x, y, size=16):
    return f'<use href="#{sid}" transform="translate({x:g},{y:g}) scale({size / 16:g})"/>'


# --------------------------------------------------------------------------
# Minecraft-style GUI pieces (units are GUI pixels)
# --------------------------------------------------------------------------

def panel(x, y, w, h):
    return (f'<rect x="{x + 1}" y="{y}" width="{w - 2}" height="{h}" fill="#000"/>'
            f'<rect x="{x}" y="{y + 1}" width="{w}" height="{h - 2}" fill="#000"/>'
            f'<rect x="{x + 1}" y="{y + 1}" width="{w - 3}" height="{h - 3}" fill="#fff"/>'
            f'<rect x="{x + 3}" y="{y + 3}" width="{w - 4}" height="{h - 4}" fill="#555"/>'
            f'<rect x="{x + 3}" y="{y + 3}" width="{w - 6}" height="{h - 6}" fill="#c6c6c6"/>')


def slot(x, y, size=18):
    return (f'<rect x="{x}" y="{y}" width="{size - 1}" height="{size - 1}" fill="#373737"/>'
            f'<rect x="{x + 1}" y="{y + 1}" width="{size - 1}" height="{size - 1}" fill="#fff"/>'
            f'<rect x="{x + 1}" y="{y + 1}" width="{size - 2}" height="{size - 2}" fill="#8b8b8b"/>')


ARROW = [
    "..............#.......",
    "..............##......",
    "..............###.....",
    "..............####....",
    "..............#####...",
    "######################",
    "#######################",
    "########################",
    "#######################",
    "######################",
    "..............#####...",
    "..............####....",
    "..............###.....",
    "..............##......",
    "..............#.......",
]


def arrow(x, y, colour="#8b8b8b", scale=1.0):
    rows = [r.ljust(24, ".") for r in ARROW]
    return (f'<g transform="translate({x:g},{y:g}) scale({scale:g})">'
            f'{runs(rows, {"#": colour})}</g>')


def plus(x, y, colour="#8b8b8b"):
    return (f'<rect x="{x + 5}" y="{y}" width="3" height="13" fill="{colour}"/>'
            f'<rect x="{x}" y="{y + 5}" width="13" height="3" fill="{colour}"/>')


def esc(s):
    return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")


def text(x, y, s, colour="#404040", size=8, shadow=False, anchor="start",
         italic=False):
    style = ' font-style="italic"' if italic else ""
    common = (f'font-family="{FONT}" font-size="{size:g}" text-anchor="{anchor}"'
              f'{style}')
    out = ""
    if shadow:
        out += (f'<text x="{x + size / 8:g}" y="{y + size / 8:g}" {common} '
                f'fill="#3f3f3f">{esc(s)}</text>')
    return out + f'<text x="{x:g}" y="{y:g}" {common} fill="{colour}">{esc(s)}</text>'


def text_width(s, size):
    return len(s) * size * 0.6


def tooltip(x, y, lines, size=8):
    """lines: [(text, colour, italic)]"""
    w = max(text_width(t, size) for t, _, _ in lines) + 8
    h = len(lines) * (size + 3) + 6
    out = (f'<rect x="{x + 1}" y="{y}" width="{w - 2:g}" height="{h}" fill="#100010" opacity="0.94"/>'
           f'<rect x="{x}" y="{y + 1}" width="{w:g}" height="{h - 2}" fill="#100010" opacity="0.94"/>'
           f'<rect x="{x + 1.5}" y="{y + 1.5}" width="{w - 3:g}" height="{h - 3}" fill="none" '
           f'stroke="#5000ff" stroke-opacity="0.45" stroke-width="1"/>')
    for i, (t, colour, italic) in enumerate(lines):
        out += text(x + 4, y + 3 + size + i * (size + 3) - 1, t, colour, size,
                    shadow=True, italic=italic)
    return out, w, h


def cycle(groups, period=6.0):
    """Show each group in turn (SMIL, works in GitHub's <img> rendering)."""
    n = len(groups)
    key_times = ";".join(f"{i / n:.4f}" for i in range(n))
    out = []
    for i, g in enumerate(groups):
        values = ";".join("1" if j == i else "0" for j in range(n))
        out.append(f'<g opacity="{1 if i == 0 else 0}">{g}<animate attributeName="opacity" '
                   f'values="{values}" keyTimes="{key_times}" calcMode="discrete" '
                   f'dur="{period:g}s" repeatCount="indefinite"/></g>')
    return "".join(out)


# --------------------------------------------------------------------------
# Output
# --------------------------------------------------------------------------

def defs_for(sprites=(), textures=()):
    d = []
    for sid in sprites:
        if sid in GLINT:
            rows, pal = SPRITES[GLINT[sid]]
            d.append(sprite_def(sid, rows, pal, glint=True))
        else:
            rows, pal = SPRITES[sid]
            d.append(sprite_def(sid, rows, pal))
    d += texture_defs(textures)
    return "<defs>" + "".join(d) + "</defs>"


def write_svg(path, w, h, scale, title, defs, body):
    svg = (f'<svg xmlns="http://www.w3.org/2000/svg" '
           f'xmlns:xlink="http://www.w3.org/1999/xlink" '
           f'viewBox="0 0 {w * scale:g} {h * scale:g}" width="{w * scale:g}" '
           f'height="{h * scale:g}"><title>{esc(title)}</title>{defs}'
           f'<g transform="scale({scale:g})" shape-rendering="crispEdges">{body}</g></svg>\n')
    # GitHub renders SVGs via <img>; href works in modern browsers, xlink:href in older ones
    svg = svg.replace('<use href=', '<use xlink:href=')
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(svg)
    print(f"wrote {path.relative_to(OUT.parent.parent)} ({len(svg) // 1024} KB)")


ALL_TEX = set(TEXTURES) | {"portal"}


def make_items():
    for sid in ("eye", "eye_glint", "flint_and_steel", "igniter", "diamond",
                "netherite_ingot", "dye", "anvil"):
        write_svg(OUT / "items" / f"{sid}.svg", 16, 16, 2, sid.replace("_", " "),
                  defs_for([sid]), item_icon(sid, 0, 0))
    for block in ("grass_block", "netherrack", "end_stone", "redstone_block",
                  "cartography_table", "stone_bricks"):
        top, left, right = BLOCKS[block]
        write_svg(OUT / "items" / f"{block}.svg", 16, 16, 2, block.replace("_", " "),
                  defs_for(textures={top, left, right}), block_icon(block, 0, 0))


def make_journey():
    tile_w, tile_h, gap = 76, 92, 22
    steps = [
        ("Craft an Eye", ["8 Eyes of Ender +", "a dimension block"]),
        ("Make Igniter", ["Anvil: Flint and", "Steel + the Eye"]),
        ("Set the Spot", ["Right-click a", "cartography table"]),
        ("Light it!", ["Right-click inside", "a closed frame"]),
    ]
    portal_svg, portal_box = standing_portal(4, 5)
    icons = [
        item_icon("eye_glint", 0, 0, 32),
        item_icon("anvil", 0, 0, 32),
        block_icon("cartography_table", 0, 0, 32),
        fit(portal_svg, portal_box, 0, 0, 32),
    ]
    body = ""
    for i, ((title, sub), icon) in enumerate(zip(steps, icons)):
        x = i * (tile_w + gap)
        body += panel(x, 0, tile_w, tile_h)
        body += slot(x + (tile_w - 36) // 2, 8, 36)
        body += f'<g transform="translate({x + (tile_w - 36) // 2 + 2},10)">{icon}</g>'
        body += text(x + tile_w / 2, 60, title, size=8, anchor="middle")
        for j, line in enumerate(sub):
            body += text(x + tile_w / 2, 72 + j * 8, line, "#555", 5.5, anchor="middle")
        if i < len(steps) - 1:
            body += arrow(x + tile_w + 3, 21, scale=0.68)
    w = 4 * tile_w + 3 * gap
    write_svg(OUT / "journey.svg", w, tile_h, 3,
              "Craft an Eye of Rhumb, make an igniter, set the destination, light the portal",
              defs_for(["eye_glint", "anvil"], ALL_TEX), body)


def make_eye_recipe():
    W, H = 176, 74
    body = panel(0, 0, W, H) + text(28, 13, "Crafting")
    gx, gy = 29, 17
    for r in range(3):
        for c in range(3):
            body += slot(gx + c * 18, gy + r * 18)
            if (r, c) != (1, 1):
                body += item_icon("eye", gx + c * 18 + 1, gy + r * 18 + 1)
    body += arrow(90, 35)
    body += slot(119, 31, 26)
    body += item_icon("eye_glint", 124, 36)

    variants = [
        ("grass_block", "Overworld Eye of Rhumb", MC_GREEN),
        ("netherrack", "Nether Eye of Rhumb", MC_DARK_PURPLE),
        ("end_stone", "Eye of Rhumb's End", MC_GOLD),
    ]
    frames = []
    max_w = 0
    for block, name, colour in variants:
        tip, tw, _ = tooltip(150, 22, [(name, colour, False)])
        max_w = max(max_w, tw)
        frames.append(block_icon(block, gx + 18 + 1, gy + 18 + 1) + tip)
    body += cycle(frames)
    write_svg(OUT / "eye-recipe.svg", 150 + max_w + 2, H, 3,
              "Eight Eyes of Ender around a Grass Block, Netherrack or End Stone make an Eye of Rhumb",
              defs_for(["eye", "eye_glint"], {"grass_top", "grass_side", "netherrack", "end_stone"}),
              body)


def make_anvil():
    W, H = 176, 78
    body = panel(0, 0, W, H) + text(60, 13, "Repair & Name")
    body += item_icon("anvil", 30, 12, 20)
    body += ('<rect x="59" y="19" width="110" height="16" fill="#000"/>'
             '<rect x="60" y="20" width="108" height="14" fill="#1a1a1a"/>')
    body += text(63, 30, "Gunter's Igniter", "#e0e0e0", 7.5)
    body += slot(26, 46) + item_icon("flint_and_steel", 27, 47)
    body += plus(54, 48)
    body += slot(75, 46) + item_icon("eye_glint", 76, 47)
    body += arrow(100, 47)
    body += slot(133, 46) + item_icon("igniter", 134, 47)
    body += text(W - 8, 72, "Enchantment Cost: 2", "#80ff20", 6.5, shadow=True,
                 anchor="end")
    tip, tw, _ = tooltip(W + 4, 30, [
        ("Gunter's Igniter", MC_GREEN, False),
        ("Unbound", MC_RED, False),
        ("1 ignite remaining", MC_DARK_PURPLE, True),
    ])
    body += tip
    write_svg(OUT / "anvil.svg", W + 4 + tw + 2, H, 3,
              "Anvil: Flint and Steel plus an Eye of Rhumb makes a Gunter's Igniter",
              defs_for(["anvil", "flint_and_steel", "eye_glint", "igniter"]), body)


def make_portal_frames():
    s_svg, s_box = standing_portal(4, 5)
    f_svg, f_box = floor_portal(4)
    sw, sh = s_box[2] - s_box[0], s_box[3] - s_box[1]
    fw, fh = f_box[2] - f_box[0], f_box[3] - f_box[1]
    gap = 48
    h = max(sh, fh)
    body = (f'<g transform="translate({-s_box[0]:g},{(h - sh) / 2 - s_box[1]:g})">{s_svg}</g>'
            f'<g transform="translate({sw + gap - f_box[0]:g},{(h - fh) / 2 - f_box[1]:g})">{f_svg}</g>')
    write_svg(OUT / "portal-frames.svg", sw + gap + fw, h, 2.5,
              "A standing stone-brick portal and a floor portal, both filled with a purple portal",
              defs_for(textures={"stone_bricks", "portal"}), body)


if __name__ == "__main__":
    make_items()
    make_journey()
    make_eye_recipe()
    make_anvil()
    make_portal_frames()
