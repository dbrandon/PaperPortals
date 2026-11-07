# Paper Portals

**Build your own portals in survival, and send them anywhere you like.**

Paper Portals is a plugin for [Paper](https://papermc.io/) Minecraft servers. Players craft a special
igniter, attune it to a spot in the world, then light any frame they build. Walk through and you
arrive right where the igniter was attuned. No coordinate math and no surprise links to a vanilla
nether portal.

<p align="center">
  <img src="docs/images/journey.svg" alt="Craft an Eye of Rhumb, make an igniter at an anvil, set the destination at a cartography table, then light a portal frame" width="100%">
</p>

---

## Contents

- [For players](#for-players)
  - [1. Craft an Eye of Rhumb](#1-craft-an-eye-of-rhumb)
  - [2. Make a Gunter's Igniter](#2-make-a-gunters-igniter)
  - [3. Upgrade it (optional)](#3-upgrade-it-optional)
  - [4. Set the destination](#4-set-the-destination)
  - [5. Build a frame and light it](#5-build-a-frame-and-light-it)
  - [Tips and gotchas](#tips-and-gotchas)
- [For server admins](#for-server-admins)
- [For contributors](#for-contributors)

---

## For players

### 1. Craft an Eye of Rhumb

The Eye of Rhumb tells your portal which dimension it leads to. Surround a block from that dimension
with eight Eyes of Ender on a crafting table:

<p align="center">
  <img src="docs/images/eye-recipe.svg" alt="Crafting grid: eight Eyes of Ender around a center block. Grass Block makes an Overworld eye, Netherrack a Nether eye, End Stone an End eye" width="640">
</p>

| Center block | You get                    | Your portal can lead to… |
| ------------ | -------------------------- | ------------------------ |
| Grass Block  | Eye of Rhumb (Overworld)   | somewhere in the Overworld |
| Netherrack   | Eye of Rhumb (Nether)      | somewhere in the Nether  |
| End Stone    | Eye of Rhumb's End         | somewhere in the End     |

### 2. Make a Gunter's Igniter

At an **anvil**, put a **Flint and Steel** in the left slot and your **Eye of Rhumb** in the right
slot. The result is a glowing *Gunter's Igniter* attuned to that dimension. You can rename it in the
anvil while you're there.

> 💡 Costs **2 levels** by default.

### 3. Upgrade it (optional)

Before you set a destination, you can upgrade the igniter at the anvil again. Put the igniter on the
left and one of these on the right:

| Add…               | What it does                                             | Default cost |
| ------------------ | -------------------------------------------------------- | ------------ |
| 💎 Diamond          | Igniter lights **10** portals instead of 1               | 5 levels     |
| ⬛ Netherite Ingot  | Igniter lights **50** portals (can follow a diamond)     | 15 levels    |
| 🎨 Any dye          | Tints the portal (add several for a multi-hued portal)   | 2 levels     |
| 🟥 Block of Redstone | Makes the portal redstone-controlled *(experimental)*   | 10 levels    |

> ⚠️ **Upgrade first!** Once you set a destination in the next step, the igniter is locked and
> can't be upgraded any more.

### 4. Set the destination

Go to where you want the portal to **lead**. It has to be in the dimension the igniter is attuned to.
Place a **Cartography Table** there and **right-click it with the igniter**.

Lightning strikes ⚡, the table is used up, and the igniter now remembers that spot. Its tooltip
shows the coordinates.

### 5. Build a frame and light it

Back home, build a closed frame out of **any solid blocks**. **Right-click the inside edge of the
frame** with the igniter and the opening fills with a portal.

<p align="center">
  <img src="docs/images/portal-frames.svg" alt="A standing portal frame you walk through after a short wait, and a flat floor portal that teleports instantly" width="760">
</p>

- **Standing portals** (vertical) work like a nether portal: stand in one for about 2.5 seconds.
- **Floor portals** (flat) teleport you **instantly** when you step on them. Great for trapdoor drops!
- Creative and spectator players always teleport instantly.

The plugin looks for a safe place to land, so you won't arrive inside a wall.

### Tips and gotchas

- A basic igniter has **one use** and breaks after lighting a portal. Add a diamond or netherite to
  get more uses. Creative players don't use up charges.
- Each portal leads **one way**. To get back, make a second igniter attuned to your starting point.
- **Breaking the frame** removes the portal.
- Right-clicking a cartography table with an igniter that already has a destination does nothing.
  The destination is permanent.

---

## For server admins

### Installing

1. Download the latest `PaperPortals-*.jar` from the [Releases](../../releases) page, or
   [build it yourself](#building).
2. Drop it in your server's `plugins/` folder.
3. Restart the server. Paper downloads the protobuf library the plugin needs automatically.

**Requirements:** Java 25 and a Paper server that matches the release you download. Each release
is named for its Paper version (for example *PaperPortals for 26.3*).

### Commands

All commands need the `portpower.command.use` permission (ops have it by default).

| Command                                 | What it does                                               |
| --------------------------------------- | ---------------------------------------------------------- |
| `/portpower gettool <world> <player>`   | Gives a player a ready-made igniter, skipping the crafting steps. `<world>` is `overworld`, `the_nether` or `the_end`. |
| `/portpower debugdump`                  | Writes all loaded portal blocks to the server log.         |

### Configuration

`plugins/PaperPortals/config.yml`:

```yaml
# Which block fills a portal. The default, "light", is invisible.
# You can also use "minecraft:nether_portal" for the classic purple swirl.
portal-block-type-list:
  - "minecraft:light"
portal-vertical-block-type: "minecraft:light"    # standing portals
portal-horizontal-block-type: "minecraft:light"  # floor portals

# Anvil XP-level cost for each step
igniter-operation-costs:
  bind-to-eye: 2        # Flint and Steel + Eye of Rhumb
  bind-to-dye: 2
  bind-to-diamond: 5
  bind-to-netherite: 15
  bind-to-redstone: 10
```

---

## For contributors

Contributions are welcome! Here's how to get going.

### Building

You need **JDK 25**. The Gradle wrapper handles everything else.

```bash
git clone https://github.com/dbrandon/PaperPortals.git
cd PaperPortals
./gradlew build
```

The plugin jar ends up in `build/libs/`. Copy it into a local Paper test server's `plugins/` folder
to try it out.

### How the code is organized

Everything lives under `src/main/java/com/tubba/mc/paper/portals/`:

| File / package               | Responsibility                                                       |
| ---------------------------- | -------------------------------------------------------------------- |
| `PortalPowerPlugin`          | Plugin entry point, wiring, and the `/portpower` command             |
| `RhumbEyeManager`            | Eye of Rhumb items and crafting recipes                              |
| `IgniterManager`             | Igniter items: anvil upgrades, lore, uses, binding to a destination  |
| `PortalListener`             | Lighting portals, walking into them, the teleport countdown, anvils |
| `PortalRegistry`             | Stores which blocks are portals and where they lead                  |
| `PortalPhysicsListener`      | Keeps portal blocks from breaking, removes them when the frame breaks |
| `NetherPortalBlockListener`  | Stops vanilla nether portals from linking into custom portals        |
| `PortalSpawnFinder`          | Finds a safe spot to land                                            |
| `scan/`                      | Flood fill that finds the open area inside a frame                   |
| `src/main/proto/`            | Protobuf schema for data stored on items and blocks                  |

Item and block state is stored as protobuf messages in Bukkit's `PersistentDataContainer`. If you
change `portal_data.proto`, add new fields instead of renumbering existing ones, so portals and
igniters already in players' worlds keep working.

### Sending a change

1. Fork the repo and create a branch for your change.
2. Test it on a local Paper server.
3. Open a pull request describing what you changed and how you tested it.

### Security

Supported versions are listed in [SECURITY.md](SECURITY.md). If you find a security problem,
please contact the maintainer privately instead of opening a public issue.
