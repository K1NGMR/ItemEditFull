package com.itemedit.full.command;

import com.itemedit.full.ItemEditFull;
import com.itemedit.full.ability.Ability;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Registry;

import java.util.*;
import java.util.stream.Collectors;

public class ItemEditCommand implements CommandExecutor, TabCompleter {
    private final ItemEditFull plugin;

    public ItemEditCommand(ItemEditFull plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("itemedit.use")) {
            player.sendMessage("§cYou do not have permission to use this command.");
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            player.sendMessage("§cYou must hold an item in your main hand.");
            return true;
        }

        if (args.length == 0) {
            plugin.getGuiManager().openMainMenu(player);
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "rename":
                handleRename(player, item, args);
                break;
            case "lore":
                handleLore(player, item, args);
                break;
            case "enchant":
                handleEnchant(player, item, args);
                break;
            case "unbreakable":
                handleUnbreakable(player, item, args);
                break;
            case "flag":
                handleFlag(player, item, args);
                break;
            case "attribute":
                handleAttribute(player, item, args);
                break;
            case "ability":
                handleAbility(player, item, args);
                break;
            case "custom":
                handleCustom(player, item, args);
                break;
            case "gui":
                plugin.getGuiManager().openMainMenu(player);
                break;
            case "hidetooltips":
                handleHideTooltips(player, item, args);
                break;
            default:
                sendHelp(player);
                break;
        }

        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage("§6§lItemEdit Full Commands:");
        player.sendMessage("§e/ie gui §7- Opens the visual editing GUI.");
        player.sendMessage("§e/ie rename <name> §7- Renames the held item.");
        player.sendMessage("§e/ie lore add <text> §7- Adds a line of lore.");
        player.sendMessage("§e/ie lore set <line> <text> §7- Sets a specific line of lore.");
        player.sendMessage("§e/ie lore remove <line> §7- Removes a line of lore.");
        player.sendMessage("§e/ie lore clear §7- Clears all lore.");
        player.sendMessage("§e/ie enchant <enchantment> <level> §7- Enchants the item.");
        player.sendMessage("§e/ie unbreakable <true/false> §7- Sets unbreakable state.");
        player.sendMessage("§e/ie flag <add/remove/clear> <flag> §7- Manages item flags.");
        player.sendMessage("§e/ie attribute <add/remove/clear> <attr> [val] §7- Manages attributes.");
        player.sendMessage("§e/ie ability <add/remove/list> [ability] §7- Manages item abilities.");
        player.sendMessage("§e/ie custom <ability> <param> <value> §7- Overrides ability values.");
        player.sendMessage("§e/ie hidetooltips [true/false] §7- Hides or shows item tooltips.");
    }

    private Component parseText(String text) {
        String translated = ChatColor.translateAlternateColorCodes('&', text);
        if (text.contains("<") && text.contains(">")) {
            return MiniMessage.miniMessage().deserialize(text);
        }
        return LegacyComponentSerializer.legacySection().deserialize(translated);
    }

    private void saveItem(Player player, ItemStack item, ItemMeta meta) {
        item.setItemMeta(meta);
        player.getInventory().setItemInMainHand(item);
        player.updateInventory();
    }

    private void handleRename(Player player, ItemStack item, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /ie rename <name>");
            return;
        }
        String name = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        ItemMeta meta = item.getItemMeta();
        meta.displayName(parseText(name));
        saveItem(player, item, meta);
        player.sendMessage("§aItem renamed successfully!");
    }

    private void handleLore(Player player, ItemStack item, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /ie lore <add/set/remove/clear> [args]");
            return;
        }

        ItemMeta meta = item.getItemMeta();
        List<Component> lore = meta.hasLore() ? meta.lore() : new ArrayList<>();
        if (lore == null) {
            lore = new ArrayList<>();
        }

        String operation = args[1].toLowerCase();
        switch (operation) {
            case "add":
                if (args.length < 3) {
                    player.sendMessage("§cUsage: /ie lore add <text>");
                    return;
                }
                String addedText = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                lore.add(parseText(addedText));
                meta.lore(lore);
                saveItem(player, item, meta);
                player.sendMessage("§aAdded lore line.");
                break;
            case "set":
                if (args.length < 4) {
                    player.sendMessage("§cUsage: /ie lore set <line> <text>");
                    return;
                }
                try {
                    int line = Integer.parseInt(args[2]) - 1;
                    if (line < 0 || line > lore.size()) {
                        player.sendMessage("§cInvalid line number. Current size is " + lore.size() + ".");
                        return;
                    }
                    String text = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
                    Component parsed = parseText(text);
                    if (line == lore.size()) {
                        lore.add(parsed);
                    } else {
                        lore.set(line, parsed);
                    }
                    meta.lore(lore);
                    saveItem(player, item, meta);
                    player.sendMessage("§aSet lore line " + (line + 1) + ".");
                } catch (NumberFormatException e) {
                    player.sendMessage("§cLine must be a number.");
                }
                break;
            case "remove":
                if (args.length < 3) {
                    player.sendMessage("§cUsage: /ie lore remove <line>");
                    return;
                }
                try {
                    int line = Integer.parseInt(args[2]) - 1;
                    if (line < 0 || line >= lore.size()) {
                        player.sendMessage("§cLine number out of bounds.");
                        return;
                    }
                    lore.remove(line);
                    meta.lore(lore);
                    saveItem(player, item, meta);
                    player.sendMessage("§aRemoved lore line " + (line + 1) + ".");
                } catch (NumberFormatException e) {
                    player.sendMessage("§cLine must be a number.");
                }
                break;
            case "clear":
                meta.lore(null);
                saveItem(player, item, meta);
                player.sendMessage("§aLore cleared.");
                break;
            default:
                player.sendMessage("§cUnknown operation. Use add, set, remove, or clear.");
                break;
        }
    }

    private void handleEnchant(Player player, ItemStack item, String[] args) {
        if (args.length < 3) {
            player.sendMessage("§cUsage: /ie enchant <enchantment> <level>");
            return;
        }

        String enchantName = args[1].toLowerCase();
        int level;
        try {
            level = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            player.sendMessage("§cLevel must be a number.");
            return;
        }

        Enchantment enchantment = null;
        for (Enchantment e : Enchantment.values()) {
            if (e.getName().equalsIgnoreCase(enchantName) || e.getKey().getKey().equalsIgnoreCase(enchantName)) {
                enchantment = e;
                break;
            }
        }

        if (enchantment == null) {
            player.sendMessage("§cEnchantment not found.");
            return;
        }

        if (level <= 0) {
            item.removeEnchantment(enchantment);
            player.getInventory().setItemInMainHand(item);
            player.updateInventory();
            player.sendMessage("§aRemoved enchantment " + enchantment.getKey().getKey() + ".");
        } else {
            ItemMeta meta = item.getItemMeta();
            meta.addEnchant(enchantment, level, true);
            saveItem(player, item, meta);
            player.sendMessage("§aAdded enchantment " + enchantment.getKey().getKey() + " Level " + level + ".");
        }
    }

    private void handleUnbreakable(Player player, ItemStack item, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /ie unbreakable <true/false>");
            return;
        }
        boolean state = Boolean.parseBoolean(args[1]);
        ItemMeta meta = item.getItemMeta();
        meta.setUnbreakable(state);
        saveItem(player, item, meta);
        player.sendMessage("§aSet unbreakable to " + state + ".");
    }

    private void handleFlag(Player player, ItemStack item, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /ie flag <add/remove/clear> [flag]");
            return;
        }

        String operation = args[1].toLowerCase();
        ItemMeta meta = item.getItemMeta();

        if (operation.equalsIgnoreCase("clear")) {
            for (ItemFlag flag : ItemFlag.values()) {
                meta.removeItemFlags(flag);
            }
            saveItem(player, item, meta);
            player.sendMessage("§aCleared all item flags.");
            return;
        }

        if (args.length < 3) {
            player.sendMessage("§cUsage: /ie flag <add/remove> <flag>");
            return;
        }

        String flagName = args[2].toUpperCase();
        ItemFlag flag = null;
        try {
            flag = ItemFlag.valueOf(flagName);
        } catch (IllegalArgumentException e) {
            for (ItemFlag f : ItemFlag.values()) {
                if (f.name().equalsIgnoreCase(flagName)) {
                    flag = f;
                    break;
                }
            }
        }

        if (flag == null) {
            player.sendMessage("§cInvalid item flag.");
            return;
        }

        if (operation.equalsIgnoreCase("add")) {
            meta.addItemFlags(flag);
            player.sendMessage("§aAdded flag " + flag.name());
        } else if (operation.equalsIgnoreCase("remove")) {
            meta.removeItemFlags(flag);
            player.sendMessage("§aRemoved flag " + flag.name());
        } else {
            player.sendMessage("§cUnknown operation. Use add, remove, or clear.");
            return;
        }
        saveItem(player, item, meta);
    }

    private List<Attribute> getAllAttributes() {
        List<Attribute> list = new ArrayList<>();
        try {
            Class<?> attributeClass = Class.forName("org.bukkit.attribute.Attribute");
            if (attributeClass.isEnum()) {
                for (Object val : (Object[]) attributeClass.getMethod("values").invoke(null)) {
                    list.add((Attribute) val);
                }
            } else {
                java.lang.reflect.Field field = Registry.class.getField("ATTRIBUTE");
                Registry<?> registry = (Registry<?>) field.get(null);
                for (Object obj : registry) {
                    if (obj instanceof Attribute) {
                        list.add((Attribute) obj);
                    }
                }
            }
        } catch (Exception e) {
            // Fallback
        }
        return list;
    }

    private Attribute getAttributeByName(String name) {
        String upper = name.toUpperCase();
        for (Attribute a : getAllAttributes()) {
            if (a instanceof org.bukkit.Keyed) {
                String key = ((org.bukkit.Keyed) a).getKey().getKey().toUpperCase();
                if (key.equalsIgnoreCase(upper) || key.replace("GENERIC_", "").equalsIgnoreCase(upper)) {
                    return a;
                }
            }
            if (a.name().equalsIgnoreCase(upper) || a.name().replace("GENERIC_", "").equalsIgnoreCase(upper)) {
                return a;
            }
        }
        return null;
    }

    private void handleAttribute(Player player, ItemStack item, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /ie attribute <add/remove/clear> [attribute] [value]");
            return;
        }

        String operation = args[1].toLowerCase();
        ItemMeta meta = item.getItemMeta();

        if (operation.equalsIgnoreCase("clear")) {
            for (Attribute attr : getAllAttributes()) {
                meta.removeAttributeModifier(attr);
            }
            saveItem(player, item, meta);
            player.sendMessage("§aCleared all attributes.");
            return;
        }

        if (args.length < 3) {
            player.sendMessage("§cUsage: /ie attribute <add/remove> <attribute> [value]");
            return;
        }

        String attrName = args[2].toUpperCase();
        Attribute attribute = getAttributeByName(attrName);

        if (attribute == null) {
            player.sendMessage("§cInvalid attribute.");
            return;
        }

        if (operation.equalsIgnoreCase("add")) {
            if (args.length < 4) {
                player.sendMessage("§cUsage: /ie attribute add <attribute> <value>");
                return;
            }
            double val;
            try {
                val = Double.parseDouble(args[3]);
            } catch (NumberFormatException e) {
                player.sendMessage("§cValue must be a number.");
                return;
            }
            AttributeModifier modifier = new AttributeModifier(
                    UUID.randomUUID(),
                    "ItemEditAttribute",
                    val,
                    AttributeModifier.Operation.ADD_NUMBER
            );
            meta.addAttributeModifier(attribute, modifier);
            player.sendMessage("§aAdded attribute " + attribute.name() + " with value " + val + ".");
        } else if (operation.equalsIgnoreCase("remove")) {
            meta.removeAttributeModifier(attribute);
            player.sendMessage("§aRemoved attribute " + attribute.name() + ".");
        } else {
            player.sendMessage("§cUnknown operation. Use add, remove, or clear.");
            return;
        }
        saveItem(player, item, meta);
    }

    private void handleHideTooltips(Player player, ItemStack item, String[] args) {
        ItemMeta meta = item.getItemMeta();
        List<ItemFlag> hideFlags = new ArrayList<>();
        boolean allHidden = true;
        for (ItemFlag flag : ItemFlag.values()) {
            if (flag.name().startsWith("HIDE_")) {
                hideFlags.add(flag);
                if (!meta.hasItemFlag(flag)) {
                    allHidden = false;
                }
            }
        }

        boolean hide;
        if (args.length >= 2) {
            hide = Boolean.parseBoolean(args[1]);
        } else {
            hide = !allHidden;
        }

        for (ItemFlag flag : hideFlags) {
            if (hide) {
                meta.addItemFlags(flag);
            } else {
                meta.removeItemFlags(flag);
            }
        }
        saveItem(player, item, meta);
        player.sendMessage("§aSet hide tooltips to " + hide + ".");
    }

    private void handleAbility(Player player, ItemStack item, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /ie ability <add/remove/list> [ability]");
            return;
        }

        String operation = args[1].toLowerCase();
        if (operation.equalsIgnoreCase("list")) {
            player.sendMessage("§6§lAvailable Abilities:");
            for (Ability ability : plugin.getAbilityManager().getRegisteredAbilities()) {
                player.sendMessage("§e- " + ability.getId() + " §7(" + ability.getName() + "): " + ability.getDescription());
            }
            return;
        }

        if (args.length < 3) {
            player.sendMessage("§cUsage: /ie ability <add/remove> <ability_id>");
            return;
        }

        String abilityId = args[2].toLowerCase();
        List<String> current = new ArrayList<>(plugin.getAbilityManager().getItemAbilities(item));

        if (operation.equalsIgnoreCase("add")) {
            Ability ability = plugin.getAbilityManager().getAbility(abilityId);
            if (ability == null) {
                player.sendMessage("§cAbility '" + abilityId + "' does not exist.");
                return;
            }
            if (current.contains(abilityId)) {
                player.sendMessage("§cThis item already has this ability.");
                return;
            }
            current.add(abilityId);
            plugin.getAbilityManager().setItemAbilities(item, current);
            player.getInventory().setItemInMainHand(item);
            player.updateInventory();
            player.sendMessage("§aAdded ability '" + ability.getName() + "' to your item.");
        } else if (operation.equalsIgnoreCase("remove")) {
            if (!current.contains(abilityId)) {
                player.sendMessage("§cThis item does not have this ability.");
                return;
            }
            current.remove(abilityId);
            plugin.getAbilityManager().setItemAbilities(item, current);
            player.getInventory().setItemInMainHand(item);
            player.updateInventory();
            player.sendMessage("§aRemoved ability '" + abilityId + "' from your item.");
        } else {
            player.sendMessage("§cUnknown operation. Use add, remove, or list.");
        }
    }

    private void handleCustom(Player player, ItemStack item, String[] args) {
        if (args.length < 4) {
            player.sendMessage("§cUsage: /ie custom <ability_id> <parameter> <value>");
            player.sendMessage("§7Example: /ie custom lava_spit cooldown 2.5");
            return;
        }

        String abilityId = args[1].toLowerCase();
        String param = args[2].toLowerCase();
        String valStr = args[3].toLowerCase();

        Ability ability = plugin.getAbilityManager().getAbility(abilityId);
        if (ability == null) {
            player.sendMessage("§cAbility '" + abilityId + "' does not exist.");
            return;
        }

        List<String> itemAbilities = plugin.getAbilityManager().getItemAbilities(item);
        if (!itemAbilities.contains(abilityId)) {
            player.sendMessage("§cWarning: The item does not currently have this ability bound, but parameter will be set.");
        }

        try {
            if (valStr.contains(".") || param.equalsIgnoreCase("cooldown") || param.equalsIgnoreCase("damage") || param.equalsIgnoreCase("health_heal")) {
                double val = Double.parseDouble(valStr);
                ability.setCustomParam(plugin, item, param, val);
                player.sendMessage("§aSet custom parameter '" + param + "' to " + val + " for ability " + ability.getName() + " on this item.");
            } else {
                int val = Integer.parseInt(valStr);
                ability.setCustomParam(plugin, item, param, val);
                player.sendMessage("§aSet custom parameter '" + param + "' to " + val + " for ability " + ability.getName() + " on this item.");
            }
            // Explicitly sync item slot to client after custom param edit
            player.getInventory().setItemInMainHand(item);
            player.updateInventory();
        } catch (NumberFormatException e) {
            player.sendMessage("§cValue must be a valid number.");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filter(Arrays.asList("rename", "lore", "enchant", "unbreakable", "flag", "attribute", "ability", "custom", "gui", "hidetooltips"), args[0]);
        }

        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            switch (sub) {
                case "lore":
                    return filter(Arrays.asList("add", "set", "remove", "clear"), args[1]);
                case "enchant":
                    return filter(Arrays.stream(Enchantment.values()).map(e -> e.getKey().getKey()).collect(Collectors.toList()), args[1]);
                case "unbreakable":
                    return filter(Arrays.asList("true", "false"), args[1]);
                case "hidetooltips":
                    return filter(Arrays.asList("true", "false"), args[1]);
                case "flag":
                case "attribute":
                case "ability":
                    return filter(Arrays.asList("add", "remove", "clear", "list"), args[1]);
                case "custom":
                    return filter(plugin.getAbilityManager().getRegisteredAbilities().stream().map(Ability::getId).collect(Collectors.toList()), args[1]);
            }
        }

        if (args.length == 3) {
            String sub = args[0].toLowerCase();
            String op = args[1].toLowerCase();
            if (sub.equalsIgnoreCase("flag")) {
                if (op.equalsIgnoreCase("add") || op.equalsIgnoreCase("remove")) {
                    return filter(Arrays.stream(ItemFlag.values()).map(Enum::name).collect(Collectors.toList()), args[2]);
                }
            } else if (sub.equalsIgnoreCase("attribute")) {
                if (op.equalsIgnoreCase("add") || op.equalsIgnoreCase("remove")) {
                    List<String> names = getAllAttributes().stream()
                            .map(a -> {
                                if (a instanceof org.bukkit.Keyed) {
                                    return ((org.bukkit.Keyed) a).getKey().getKey().toUpperCase();
                                }
                                return a.name();
                            })
                            .collect(Collectors.toList());
                    return filter(names, args[2]);
                }
            } else if (sub.equalsIgnoreCase("ability")) {
                if (op.equalsIgnoreCase("add") || op.equalsIgnoreCase("remove")) {
                    return filter(plugin.getAbilityManager().getRegisteredAbilities().stream().map(Ability::getId).collect(Collectors.toList()), args[2]);
                }
            } else if (sub.equalsIgnoreCase("custom")) {
                return filter(Arrays.asList("cooldown", "damage", "radius", "duration", "fire_ticks", "health_heal", "hunger_heal"), args[2]);
            }
        }

        return Collections.emptyList();
    }

    private List<String> filter(List<String> list, String prefix) {
        String lower = prefix.toLowerCase();
        return list.stream().filter(s -> s.toLowerCase().startsWith(lower)).collect(Collectors.toList());
    }
}
