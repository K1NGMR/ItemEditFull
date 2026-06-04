# ItemEdit Full

![ItemEdit Banner](banner.png)

**ItemEdit Full** is the premium, feature-complete item customization plugin for Paper/Spigot Minecraft servers (1.20.4 - 1.21.x+). It allows server administrators and players to customize their weapons and items with custom stats, lore, flags, enchantments, and custom special abilities using a visual editor GUI.

---

## 🎨 Branding / Icon
<img src="logo.png" width="128" height="128" alt="ItemEdit Logo" />

---

## 🚀 Key Features

* **🖥️ Interactive GUI Editor**: Run `/ie gui` to customize items visually inside a chest menu (manage flags, lore, enchants, attributes, and abilities).
* **121 Premium Abilities**: Includes all Light abilities, plus 71 premium-exclusive abilities:
  - **Spiders & Cave Spiders**: Web shooters, arachnid pounce, toxic bites, scurrying speed.
  - **Ender Dragon**: Dragon roar, wing buffet, breath volley, dragon egg bomb.
  - **Wither**: Wither skull volley, wither shield, decaying presence aura, health absorption.
  - **Lava premium**: Premium lava abilities like `lava_absorption`.
  - **Overworld & General premium**: Custom extra elemental strikes, and more.
* **🔧 Custom Param Overrides (`/ie custom`)**: Override ability-specific stats per weapon (e.g. modify cooldowns, radius, fire ticks, damage, or healing rates on individual items).
* **🛡️ Self-Damage Protection**: Built-in mechanism to prevent players from taking damage from their own abilities (e.g., custom lightning, projectiles, or explosion effects).
* **Command Suite**:
  - `/ie rename <name>`: Rename items with MiniMessage (RGB) or legacy color codes.
  - `/ie lore <add/set/remove/clear>`: Manage multi-line lore.
  - `/ie enchant <enchantment> <level>`: Enchant items with bypass support.
  - `/ie unbreakable <true/false>`: Set the item to be unbreakable.
  - `/ie flag <add/remove/clear> <flag>`: Manage specific item flags.
  - `/ie attribute <add/remove/clear> <attribute> [value]`: Add attributes like health, attack speed, movement speed, etc.
  - `/ie ability <add/remove/list> [ability]`: Bind custom abilities to items.
  - `/ie custom <ability> <param> <value>`: Override parameter values for custom items.
  - `/ie hidetooltips [true/false]`: Hide or show item tooltips (flags).
* **Full Tab Completion**: Auto-completes all subcommands, arguments, enchantments, attributes, flags, abilities, and custom parameters.

---

## 🛠️ Commands & Permissions

- **Command**: `/ie` or `/itemedit`
- **Permission**: `itemedit.use` (default: op)

---

## ⚙️ Compilation & Installation

ItemEdit is built on Java 17.

1. Clone the repository.
2. Build with Maven:
   ```bash
   mvn clean package
   ```
3. Copy the compiled jar from `target/ItemEditFull.jar` to your server's `plugins` directory.
