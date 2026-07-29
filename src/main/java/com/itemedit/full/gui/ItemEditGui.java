package com.itemedit.full.gui;

import com.itemedit.full.ItemEditFull;
import com.itemedit.full.ability.Ability;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import com.itemedit.full.utils.SchedulerUtils;

import java.util.*;

public class ItemEditGui implements Listener {
    private final ItemEditFull plugin;
    private final Map<UUID, ChatPromptType> chatPrompts = new HashMap<>();
    private final Map<UUID, Integer> activeLoreLines = new HashMap<>();
    private final Map<UUID, ParamEditData> activeParamEdits = new HashMap<>();
    private final Map<UUID, Integer> abilityMenuPage = new HashMap<>();

    private static final int ABILITIES_PER_PAGE = 45;
    private static final int SLOT_PREV = 45;
    private static final int SLOT_BACK = 49;
    private static final int SLOT_NEXT = 53;

    public enum ChatPromptType {
        RENAME,
        ADD_LORE,
        SET_LORE
    }

    private static class ParamEditData {
        final String abilityId;
        final String paramName;

        ParamEditData(String abilityId, String paramName) {
            this.abilityId = abilityId;
            this.paramName = paramName;
        }
    }

    public ItemEditGui(ItemEditFull plugin) {
        this.plugin = plugin;
    }

    private void saveItem(Player player, ItemStack item, ItemMeta meta) {
        item.setItemMeta(meta);
        player.getInventory().setItemInMainHand(item);
        player.updateInventory();
    }

    private ItemStack createGuiItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name));
        List<Component> loreList = new ArrayList<>();
        for (String line : lore) {
            loreList.add(Component.text(line));
        }
        meta.lore(loreList);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE);
        item.setItemMeta(meta);
        return item;
    }

    public void openMainMenu(Player player) {
        ItemStack held = player.getInventory().getItemInMainHand();
        if (held == null || held.getType().isAir()) {
            player.sendMessage("§cYou must hold an item to edit it.");
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 27, "§0Item Edit - Main Menu");

        // Styling panes
        ItemStack pane = createGuiItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 27; i++) {
            inv.setItem(i, pane);
        }

        inv.setItem(10, createGuiItem(Material.NAME_TAG, "§e§lRename Item", "§7Click to change the", "§7display name of the item."));
        inv.setItem(11, createGuiItem(Material.WRITABLE_BOOK, "§e§lEdit Lore", "§7Click to manage lines", "§7of item description/lore."));
        inv.setItem(12, createGuiItem(Material.ENCHANTED_BOOK, "§e§lEnchantments", "§7Click to add or remove", "§7enchantments on this item."));
        inv.setItem(13, createGuiItem(Material.PAPER, "§e§lToggle Flags", "§7Click to hide/show", "§7enchantment, attribute flags."));
        inv.setItem(14, createGuiItem(Material.ANVIL, "§e§lUnbreakable State", "§7Click to toggle whether", "§7this item loses durability."));
        inv.setItem(15, createGuiItem(Material.BLAZE_POWDER, "§e§lAdd/Remove Abilities", "§7Click to bind lava-related", "§7right-click abilities."));
        inv.setItem(16, createGuiItem(Material.COMPARATOR, "§e§lCustomize Parameters", "§7Click to override cooldown,", "§7damage, and ability values."));

        player.openInventory(inv);
    }

    public void openEnchantMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§0Item Edit - Enchantments");
        ItemStack pane = createGuiItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 27; i++) {
            inv.setItem(i, pane);
        }

        inv.setItem(10, createGuiItem(Material.BOOK, "§aSharpness"));
        inv.setItem(11, createGuiItem(Material.BOOK, "§aProtection"));
        inv.setItem(12, createGuiItem(Material.BOOK, "§aUnbreaking"));
        inv.setItem(13, createGuiItem(Material.BOOK, "§aEfficiency"));
        inv.setItem(14, createGuiItem(Material.BOOK, "§aMending"));
        inv.setItem(15, createGuiItem(Material.BOOK, "§aFire Aspect"));
        inv.setItem(16, createGuiItem(Material.BOOK, "§aLooting"));

        inv.setItem(22, createGuiItem(Material.BARRIER, "§cBack to Menu"));
        player.openInventory(inv);
    }

    public void openFlagsMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§0Item Edit - Flags");
        ItemStack pane = createGuiItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 27; i++) {
            inv.setItem(i, pane);
        }

        inv.setItem(11, createGuiItem(Material.PAPER, "§bHide Enchants"));
        inv.setItem(13, createGuiItem(Material.PAPER, "§bHide Attributes"));
        inv.setItem(15, createGuiItem(Material.PAPER, "§bHide Unbreakable"));

        inv.setItem(22, createGuiItem(Material.BARRIER, "§cBack to Menu"));
        player.openInventory(inv);
    }

    public void openAbilitiesMenu(Player player) {
        openAbilitiesMenu(player, abilityMenuPage.getOrDefault(player.getUniqueId(), 0));
    }

    public void openAbilitiesMenu(Player player, int page) {
        List<Ability> abilities = new ArrayList<>(plugin.getAbilityManager().getRegisteredAbilities());
        int maxPage = Math.max(0, (abilities.size() - 1) / ABILITIES_PER_PAGE);
        page = Math.max(0, Math.min(page, maxPage));
        abilityMenuPage.put(player.getUniqueId(), page);

        Inventory inv = Bukkit.createInventory(null, 54, "§0Item Edit - Abilities");
        ItemStack pane = createGuiItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = ABILITIES_PER_PAGE; i < 54; i++) {
            inv.setItem(i, pane);
        }

        List<String> current = plugin.getAbilityManager().getItemAbilities(player.getInventory().getItemInMainHand());
        int start = page * ABILITIES_PER_PAGE;
        for (int i = 0; i < ABILITIES_PER_PAGE; i++) {
            int idx = start + i;
            if (idx >= abilities.size()) {
                break;
            }
            Ability ability = abilities.get(idx);
            boolean has = current.contains(ability.getId().toLowerCase());
            String prefix = has ? "§a✔ " : "§6";
            inv.setItem(i, createGuiItem(
                    has ? Material.LIME_DYE : Material.FIREWORK_STAR,
                    prefix + ability.getName(),
                    "§7ID: " + ability.getId(),
                    has ? "§eClick to remove" : "§eClick to add"));
        }

        if (page > 0) {
            inv.setItem(SLOT_PREV, createGuiItem(Material.ARROW, "§ePrevious Page"));
        }
        inv.setItem(SLOT_BACK, createGuiItem(Material.BARRIER, "§cBack to Menu"));
        if (page < maxPage) {
            inv.setItem(SLOT_NEXT, createGuiItem(Material.ARROW, "§eNext Page", "§7Page " + (page + 1) + "/" + (maxPage + 1)));
        }
        player.openInventory(inv);
    }

    public void openParamsMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§0Item Edit - Customize Parameters");
        ItemStack pane = createGuiItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 27; i++) {
            inv.setItem(i, pane);
        }

        ItemStack held = player.getInventory().getItemInMainHand();
        List<String> currentAbilities = plugin.getAbilityManager().getItemAbilities(held);

        if (currentAbilities.isEmpty()) {
            inv.setItem(13, createGuiItem(Material.BARRIER, "§cNo abilities on item to customize!"));
        } else {
            int slot = 10;
            for (String abilityId : currentAbilities) {
                if (slot >= 17) {
                    break; // keep the back button slot (22) and inventory bounds safe
                }
                Ability ability = plugin.getAbilityManager().getAbility(abilityId);
                if (ability != null) {
                    inv.setItem(slot++, createGuiItem(Material.COMPASS, "§e§l" + ability.getName(), "§7Click to customize parameter", "§7cooldown or duration/damage."));
                }
            }
        }

        inv.setItem(22, createGuiItem(Material.BARRIER, "§cBack to Menu"));
        player.openInventory(inv);
    }

    public void openParamEditor(Player player, String abilityId) {
        Ability ability = plugin.getAbilityManager().getAbility(abilityId);
        if (ability == null) return;

        Inventory inv = Bukkit.createInventory(null, 27, "§0Edit: " + ability.getName());
        ItemStack pane = createGuiItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 27; i++) {
            inv.setItem(i, pane);
        }

        ItemStack held = player.getInventory().getItemInMainHand();
        double currentCooldown = ability.getDoubleParam(plugin, held, "cooldown", 5.0);

        inv.setItem(11, createGuiItem(Material.RED_WOOL, "§c-1s Cooldown"));
        inv.setItem(13, createGuiItem(Material.CLOCK, "§eCooldown: §a" + currentCooldown + "s"));
        inv.setItem(15, createGuiItem(Material.GREEN_WOOL, "§a+1s Cooldown"));

        inv.setItem(22, createGuiItem(Material.BARRIER, "§cBack to Abilities"));
        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (!title.startsWith("§0Item Edit")) {
            // Check if it is a specific ability parameter editor
            if (title.startsWith("§0Edit: ")) {
                event.setCancelled(true);
                Player player = (Player) event.getWhoClicked();
                int slot = event.getRawSlot();
                ItemStack held = player.getInventory().getItemInMainHand();
                String abilityName = title.replace("§0Edit: ", "");
                Ability ability = null;
                for (Ability a : plugin.getAbilityManager().getRegisteredAbilities()) {
                    if (a.getName().equalsIgnoreCase(abilityName)) {
                        ability = a;
                        break;
                    }
                }
                if (ability == null) return;

                double currentCooldown = ability.getDoubleParam(plugin, held, "cooldown", 5.0);
                if (slot == 11) {
                    double newVal = Math.max(0.5, currentCooldown - 1.0);
                    ability.setCustomParam(plugin, held, "cooldown", newVal);
                    player.getInventory().setItemInMainHand(held);
                    player.updateInventory();
                    player.sendMessage("§aCooldown set to " + newVal + "s");
                    openParamEditor(player, ability.getId());
                } else if (slot == 15) {
                    double newVal = currentCooldown + 1.0;
                    ability.setCustomParam(plugin, held, "cooldown", newVal);
                    player.getInventory().setItemInMainHand(held);
                    player.updateInventory();
                    player.sendMessage("§aCooldown set to " + newVal + "s");
                    openParamEditor(player, ability.getId());
                } else if (slot == 22) {
                    openParamsMenu(player);
                }
            }
            return;
        }

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack held = player.getInventory().getItemInMainHand();
        if (held == null || held.getType().isAir()) {
            player.closeInventory();
            player.sendMessage("§cYou must hold an item to edit.");
            return;
        }

        int slot = event.getRawSlot();
        if (title.equals("§0Item Edit - Main Menu")) {
            switch (slot) {
                case 10: // Rename
                    chatPrompts.put(player.getUniqueId(), ChatPromptType.RENAME);
                    player.closeInventory();
                    player.sendMessage("§eType the new item name in chat (supports hex/MiniMessage tags like <red>Name</red>). Type 'cancel' to exit.");
                    break;
                case 11: // Lore
                    chatPrompts.put(player.getUniqueId(), ChatPromptType.ADD_LORE);
                    player.closeInventory();
                    player.sendMessage("§eType a line of lore to add. Type 'cancel' to exit.");
                    break;
                case 12: // Enchantments
                    openEnchantMenu(player);
                    break;
                case 13: // Flags
                    openFlagsMenu(player);
                    break;
                case 14: // Unbreakable
                    ItemMeta meta = held.getItemMeta();
                    meta.setUnbreakable(!meta.isUnbreakable());
                    saveItem(player, held, meta);
                    player.sendMessage("§aUnbreakable toggled to " + meta.isUnbreakable() + "!");
                    openMainMenu(player);
                    break;
                case 15: // Abilities
                    openAbilitiesMenu(player);
                    break;
                case 16: // Params
                    openParamsMenu(player);
                    break;
            }
        } else if (title.equals("§0Item Edit - Enchantments")) {
            if (slot == 22) {
                openMainMenu(player);
                return;
            }
            Enchantment enchantment = null;
            switch (slot) {
                case 10: enchantment = Enchantment.DAMAGE_ALL; break;
                case 11: enchantment = Enchantment.PROTECTION_ENVIRONMENTAL; break;
                case 12: enchantment = Enchantment.DURABILITY; break;
                case 13: enchantment = Enchantment.DIG_SPEED; break;
                case 14: enchantment = Enchantment.MENDING; break;
                case 15: enchantment = Enchantment.FIRE_ASPECT; break;
                case 16: enchantment = Enchantment.LOOT_BONUS_MOBS; break;
            }
                if (enchantment != null) {
                    ItemMeta meta = held.getItemMeta();
                    if (meta.hasEnchant(enchantment)) {
                        meta.removeEnchant(enchantment);
                        player.sendMessage("§cRemoved enchantment.");
                    } else {
                        meta.addEnchant(enchantment, 5, true);
                        player.sendMessage("§aAdded enchantment.");
                    }
                    saveItem(player, held, meta);
                }
        } else if (title.equals("§0Item Edit - Flags")) {
            if (slot == 22) {
                openMainMenu(player);
                return;
            }
            ItemFlag flag = null;
            switch (slot) {
                case 11: flag = ItemFlag.HIDE_ENCHANTS; break;
                case 13: flag = ItemFlag.HIDE_ATTRIBUTES; break;
                case 15: flag = ItemFlag.HIDE_UNBREAKABLE; break;
            }
                if (flag != null) {
                    ItemMeta meta = held.getItemMeta();
                    if (meta.hasItemFlag(flag)) {
                        meta.removeItemFlags(flag);
                        player.sendMessage("§aRevealed flag info.");
                    } else {
                        meta.addItemFlags(flag);
                        player.sendMessage("§aHid flag info.");
                    }
                    saveItem(player, held, meta);
                }
        } else if (title.equals("§0Item Edit - Abilities")) {
            int page = abilityMenuPage.getOrDefault(player.getUniqueId(), 0);
            if (slot == SLOT_BACK) {
                openMainMenu(player);
                return;
            }
            if (slot == SLOT_PREV) {
                openAbilitiesMenu(player, page - 1);
                return;
            }
            if (slot == SLOT_NEXT) {
                openAbilitiesMenu(player, page + 1);
                return;
            }
            if (slot < 0 || slot >= ABILITIES_PER_PAGE) {
                return;
            }
            ItemStack clicked = event.getCurrentItem();
            if (clicked != null && clicked.hasItemMeta()) {
                List<Component> lore = clicked.getItemMeta().lore();
                if (lore != null && !lore.isEmpty()) {
                    // Strip color codes so the "§7ID: <id>" prefix doesn't corrupt the parsed id.
                    String line = ChatColor.stripColor(LegacyComponentSerializer.legacySection().serialize(lore.get(0)));
                    String id = line.replace("ID: ", "").trim().toLowerCase();
                    Ability ability = plugin.getAbilityManager().getAbility(id);
                    if (ability == null) {
                        return;
                    }
                    String canonicalId = ability.getId().toLowerCase();
                    List<String> current = new ArrayList<>(plugin.getAbilityManager().getItemAbilities(held));
                    if (current.contains(canonicalId)) {
                        current.remove(canonicalId);
                        player.sendMessage("§cRemoved ability " + ability.getName());
                    } else {
                        current.add(canonicalId);
                        player.sendMessage("§aAdded ability " + ability.getName());
                    }
                    plugin.getAbilityManager().setItemAbilities(held, current);
                    player.getInventory().setItemInMainHand(held);
                    player.updateInventory();
                    openAbilitiesMenu(player, page);
                }
            }
        } else if (title.equals("§0Item Edit - Customize Parameters")) {
            if (slot == 22) {
                openMainMenu(player);
                return;
            }
            ItemStack clicked = event.getCurrentItem();
            if (clicked != null && clicked.hasItemMeta()) {
                String name = clicked.getItemMeta().displayName() != null ?
                        LegacyComponentSerializer.legacySection().serialize(clicked.getItemMeta().displayName()) : "";
                name = ChatColor.stripColor(name);
                Ability ability = null;
                for (Ability a : plugin.getAbilityManager().getRegisteredAbilities()) {
                    if (a.getName().equalsIgnoreCase(name)) {
                        ability = a;
                        break;
                    }
                }
                if (ability != null) {
                    openParamEditor(player, ability.getId());
                }
            }
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        ChatPromptType prompt = chatPrompts.get(player.getUniqueId());
        if (prompt == null) {
            return;
        }

        event.setCancelled(true);
        chatPrompts.remove(player.getUniqueId());

        String message = event.getMessage().trim();
        if (message.equalsIgnoreCase("cancel")) {
            player.sendMessage("§cCancelled.");
            SchedulerUtils.runTask(plugin, player, () -> openMainMenu(player));
            return;
        }

        SchedulerUtils.runTask(plugin, player, () -> {
            ItemStack held = player.getInventory().getItemInMainHand();
            if (held == null || held.getType().isAir()) {
                player.sendMessage("§cYou must hold the item to edit it.");
                return;
            }

            ItemMeta meta = held.getItemMeta();
            if (prompt == ChatPromptType.RENAME) {
                meta.displayName(parseText(message));
                saveItem(player, held, meta);
                player.sendMessage("§aItem renamed!");
            } else if (prompt == ChatPromptType.ADD_LORE) {
                List<Component> lore = meta.hasLore() ? meta.lore() : new ArrayList<>();
                if (lore == null) {
                    lore = new ArrayList<>();
                }
                lore.add(parseText(message));
                meta.lore(lore);
                saveItem(player, held, meta);
                player.sendMessage("§aLore added!");
            }
            openMainMenu(player);
        });
    }

    // Detects a MiniMessage-style tag such as <red>, </bold>, <#ffAA00> without matching
    // stray comparison text like "a < b > c" or "<3".
    private static final java.util.regex.Pattern MINI_TAG =
            java.util.regex.Pattern.compile("</?[a-zA-Z#][^<>]*>");

    private Component parseText(String text) {
        if (MINI_TAG.matcher(text).find()) {
            try {
                return MiniMessage.miniMessage().deserialize(text);
            } catch (Exception ignored) {
                // Fall through to legacy parsing on malformed MiniMessage input.
            }
        }
        return LegacyComponentSerializer.legacySection()
                .deserialize(ChatColor.translateAlternateColorCodes('&', text));
    }

    @EventHandler
    public void onQuit(org.bukkit.event.player.PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        chatPrompts.remove(id);
        activeLoreLines.remove(id);
        activeParamEdits.remove(id);
        abilityMenuPage.remove(id);
    }
}
