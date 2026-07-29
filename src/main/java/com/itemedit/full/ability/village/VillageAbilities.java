package com.itemedit.full.ability.village;

import com.itemedit.full.ItemEditFull;
import com.itemedit.full.ability.Ability;
import com.itemedit.full.utils.CompatRunnable;
import com.itemedit.full.utils.EffectUtils;
import com.itemedit.full.utils.VectorUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Zombie;
import org.bukkit.entity.ZombieVillager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Trade Compact: 10 village/profession-themed abilities (bells, professions,
 * and the quiet economy of villagers) - a brand-new school with no prior abilities.
 */
public class VillageAbilities implements Listener {

    // ---- Armorer's Temper: flat damage reduction while active ----
    private static final Map<UUID, Long> temperActive = new ConcurrentHashMap<>();
    private static final Map<UUID, Double> temperReduction = new ConcurrentHashMap<>();

    // ---- Librarian's Insight: bonus XP from kills while active ----
    private static final Map<UUID, Long> insightActive = new ConcurrentHashMap<>();
    private static final Map<UUID, Double> insightBonus = new ConcurrentHashMap<>();

    private static ItemEditFull pluginInstance;

    public static void register(ItemEditFull plugin) {
        pluginInstance = plugin;

        plugin.getAbilityManager().registerAbility(new SummonIronGuardian(plugin));
        plugin.getAbilityManager().registerAbility(new BellsAlarm(plugin));
        plugin.getAbilityManager().registerAbility(new LibrariansInsight(plugin));
        plugin.getAbilityManager().registerAbility(new FarmersBounty(plugin));
        plugin.getAbilityManager().registerAbility(new ClericsCure(plugin));
        plugin.getAbilityManager().registerAbility(new ArmorersTemper(plugin));
        plugin.getAbilityManager().registerAbility(new CurePulse(plugin));
        plugin.getAbilityManager().registerAbility(new CartographersReveal(plugin));
        plugin.getAbilityManager().registerAbility(new MasonsWall(plugin));
        plugin.getAbilityManager().registerAbility(new FletchersBarrage(plugin));

        plugin.getServer().getPluginManager().registerEvents(new VillageAbilities(), plugin);
    }

    static void activateTemper(UUID uuid, long expireAt, double reduction) {
        temperActive.put(uuid, expireAt);
        temperReduction.put(uuid, reduction);
    }

    static void activateInsight(UUID uuid, long expireAt, double bonus) {
        insightActive.put(uuid, expireAt);
        insightBonus.put(uuid, bonus);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onTemperDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity();
        Long expire = temperActive.get(player.getUniqueId());
        if (expire == null) {
            return;
        }
        if (System.currentTimeMillis() >= expire) {
            temperActive.remove(player.getUniqueId());
            temperReduction.remove(player.getUniqueId());
            return;
        }
        double reduction = temperReduction.getOrDefault(player.getUniqueId(), 3.0);
        event.setDamage(Math.max(0.0, event.getDamage() - reduction));
    }

    @EventHandler
    public void onInsightKill(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) {
            return;
        }
        Long expire = insightActive.get(killer.getUniqueId());
        if (expire == null || System.currentTimeMillis() >= expire) {
            return;
        }
        double bonus = insightBonus.getOrDefault(killer.getUniqueId(), 0.5);
        int extraXp = (int) Math.ceil(event.getDroppedExp() * bonus);
        if (extraXp > 0) {
            event.setDroppedExp(event.getDroppedExp() + extraXp);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        temperActive.remove(id);
        temperReduction.remove(id);
        insightActive.remove(id);
        insightBonus.remove(id);
    }
}

// ==================== 10 TRADE COMPACT ABILITIES ====================

class SummonIronGuardian extends Ability {
    private final ItemEditFull plugin;
    public SummonIronGuardian(ItemEditFull plugin) {
        super("summon_iron_guardian", "Summon Iron Guardian", "Calls a temporary iron golem ally to protect you.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double lifetime = getDoubleParam(plugin, item, "lifetime", 25.0);
        Location loc = player.getLocation();
        IronGolem golem;
        try {
            golem = loc.getWorld().spawn(loc, IronGolem.class);
        } catch (Exception e) {
            player.sendMessage("§cThe guardian could not be summoned here.");
            return false;
        }
        golem.setCustomName("§a" + player.getName() + "'s Guardian");
        golem.setCustomNameVisible(true);
        golem.setPlayerCreated(true);
        EffectUtils.fanfare(loc,
                new EffectUtils.SoundLayer(Sound.ENTITY_IRON_GOLEM_REPAIR, 1.0f, 0.9f),
                new EffectUtils.SoundLayer(Sound.BLOCK_ANVIL_LAND, 0.6f, 1.3f));
        EffectUtils.burst(loc.add(0, 1, 0),
                new EffectUtils.Layer(Particle.VILLAGER_HAPPY, 15, 0.4, 0.6, 0.4, 0),
                new EffectUtils.Layer(Particle.CRIT, 12, 0.4, 0.5, 0.4, 0.1),
                new EffectUtils.Layer(Particle.COMPOSTER, 8, 0.3, 0.4, 0.3, 0));
        for (Entity entity : golem.getNearbyEntities(10, 5, 10)) {
            if (entity instanceof Monster) {
                golem.setTarget((LivingEntity) entity);
                break;
            }
        }
        new CompatRunnable() {
            @Override
            public void run() {
                if (golem.isValid()) {
                    EffectUtils.burst(golem.getLocation().add(0, 1, 0),
                            new EffectUtils.Layer(Particle.SMOKE_NORMAL, 15, 0.3, 0.4, 0.3, 0.02),
                            new EffectUtils.Layer(Particle.VILLAGER_HAPPY, 6, 0.3, 0.4, 0.3, 0));
                    golem.getWorld().playSound(golem.getLocation(), Sound.ENTITY_IRON_GOLEM_HURT, 0.6f, 0.7f);
                    golem.remove();
                }
            }
        }.runTaskLater(plugin, player, (long) (lifetime * 20));
        return true;
    }
}

class BellsAlarm extends Ability {
    private final ItemEditFull plugin;
    public BellsAlarm(ItemEditFull plugin) {
        super("bells_alarm", "Bell's Alarm", "Rings an alarm that alerts and briefly buffs nearby allies' defence.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double radius = getDoubleParam(plugin, item, "radius", 8.0);
        double duration = getDoubleParam(plugin, item, "duration", 6.0);
        Location loc = player.getLocation();
        EffectUtils.fanfare(loc,
                new EffectUtils.SoundLayer(Sound.BLOCK_BELL_USE, 1.2f, 1.0f),
                new EffectUtils.SoundLayer(Sound.BLOCK_BELL_RESONATE, 1.0f, 0.8f));
        EffectUtils.ring(loc.clone().add(0, 1.2, 0), radius * 0.5, 16,
                new EffectUtils.Layer(Particle.NOTE, 1, 0, 0.1, 0, 1.0));
        EffectUtils.burst(loc.add(0, 2, 0),
                new EffectUtils.Layer(Particle.NOTE, 10, 0.3, 0.3, 0.3, 1.0),
                new EffectUtils.Layer(Particle.VILLAGER_HAPPY, 10, 0.4, 0.4, 0.4, 0));
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
            if (entity instanceof Player) {
                ((Player) entity).addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, (int) (duration * 20), 0));
            }
        }
        return true;
    }
}

class LibrariansInsight extends Ability {
    private final ItemEditFull plugin;
    public LibrariansInsight(ItemEditFull plugin) {
        super("librarians_insight", "Librarian's Insight", "A surge of borrowed knowledge grants bonus experience from your next kills.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 15.0);
        double xpBonus = getDoubleParam(plugin, item, "xp_bonus", 0.5);
        VillageAbilities.activateInsight(player.getUniqueId(), System.currentTimeMillis() + (long) (duration * 1000), xpBonus);
        EffectUtils.fanfare(player.getLocation(),
                new EffectUtils.SoundLayer(Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.2f),
                new EffectUtils.SoundLayer(Sound.ITEM_BOOK_PAGE_TURN, 0.8f, 1.0f));
        EffectUtils.burst(player.getLocation().add(0, 1, 0),
                new EffectUtils.Layer(Particle.ENCHANTMENT_TABLE, 30, 0.4, 0.6, 0.4, 1.0),
                new EffectUtils.Layer(Particle.VILLAGER_HAPPY, 10, 0.3, 0.4, 0.3, 0));
        return true;
    }
}

class FarmersBounty extends Ability {
    private final ItemEditFull plugin;
    public FarmersBounty(ItemEditFull plugin) {
        super("farmers_bounty", "Farmer's Bounty", "Harvests nearby crops instantly, healing you for each one collected.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double radius = getDoubleParam(plugin, item, "radius", 5.0);
        double healPerCrop = getDoubleParam(plugin, item, "heal_per_crop", 1.0);
        Location loc = player.getLocation();
        int harvested = 0;
        int r = (int) radius;
        for (int x = -r; x <= r; x++) {
            for (int z = -r; z <= r; z++) {
                for (int y = -2; y <= 2; y++) {
                    Block b = loc.clone().add(x, y, z).getBlock();
                    if (isMatureCrop(b)) {
                        b.getWorld().dropItemNaturally(b.getLocation(), new ItemStack(cropSeed(b.getType())));
                        Ageable age = (Ageable) b.getBlockData();
                        age.setAge(0);
                        b.setBlockData(age);
                        harvested++;
                    }
                }
            }
        }
        if (harvested == 0) {
            player.sendMessage("§cNo mature crops nearby to harvest.");
            return false;
        }
        double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        player.setHealth(Math.min(maxHealth, player.getHealth() + healPerCrop * harvested));
        EffectUtils.fanfare(loc,
                new EffectUtils.SoundLayer(Sound.BLOCK_CROP_BREAK, 1.0f, 1.0f),
                new EffectUtils.SoundLayer(Sound.ENTITY_PLAYER_LEVELUP, 0.4f, 1.6f));
        EffectUtils.burst(loc.clone().add(0, 1, 0),
                new EffectUtils.Layer(Particle.COMPOSTER, 12 + harvested, 0.4, 0.3, 0.4, 0),
                new EffectUtils.Layer(Particle.VILLAGER_HAPPY, 10, 0.4, 0.4, 0.4, 0));
        player.sendMessage("§eHarvested " + harvested + " crops!");
        return true;
    }
    private boolean isMatureCrop(Block b) {
        if (!(b.getBlockData() instanceof Ageable)) {
            return false;
        }
        Material type = b.getType();
        if (type != Material.WHEAT && type != Material.CARROTS && type != Material.POTATOES && type != Material.BEETROOTS) {
            return false;
        }
        Ageable age = (Ageable) b.getBlockData();
        return age.getAge() >= age.getMaximumAge();
    }
    private Material cropSeed(Material crop) {
        switch (crop) {
            case WHEAT: return Material.WHEAT;
            case CARROTS: return Material.CARROT;
            case POTATOES: return Material.POTATO;
            case BEETROOTS: return Material.BEETROOT;
            default: return Material.WHEAT_SEEDS;
        }
    }
}

class ClericsCure extends Ability {
    private final ItemEditFull plugin;
    public ClericsCure(ItemEditFull plugin) {
        super("clerics_cure", "Cleric's Cure", "A blessing that cures poison, wither, and negative effects from you and nearby allies.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double radius = getDoubleParam(plugin, item, "radius", 5.0);
        Location loc = player.getLocation();
        EffectUtils.fanfare(loc,
                new EffectUtils.SoundLayer(Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.4f),
                new EffectUtils.SoundLayer(Sound.BLOCK_BELL_USE, 0.8f, 1.5f));
        EffectUtils.burst(loc.add(0, 1, 0),
                new EffectUtils.Layer(Particle.VILLAGER_HAPPY, 25, radius * 0.3, 0.4, radius * 0.3, 0),
                new EffectUtils.Layer(Particle.END_ROD, 10, radius * 0.2, 0.4, radius * 0.2, 0.02));
        cleanse(player);
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
            if (entity instanceof Player && !entity.equals(player)) {
                cleanse((Player) entity);
            }
        }
        return true;
    }
    private void cleanse(Player p) {
        for (PotionEffect effect : new ArrayList<>(p.getActivePotionEffects())) {
            PotionEffectType type = effect.getType();
            if (type == PotionEffectType.POISON || type == PotionEffectType.WITHER || type == PotionEffectType.SLOW
                    || type == PotionEffectType.WEAKNESS || type == PotionEffectType.BLINDNESS || type == PotionEffectType.CONFUSION) {
                p.removePotionEffect(type);
            }
        }
    }
}

class ArmorersTemper extends Ability {
    private final ItemEditFull plugin;
    public ArmorersTemper(ItemEditFull plugin) {
        super("armorers_temper", "Armorer's Temper", "Reinforces your armour at the forge, granting temporary damage reduction.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 12.0);
        double reduction = getDoubleParam(plugin, item, "reduction", 3.0);
        VillageAbilities.activateTemper(player.getUniqueId(), System.currentTimeMillis() + (long) (duration * 1000), reduction);
        EffectUtils.fanfare(player.getLocation(),
                new EffectUtils.SoundLayer(Sound.BLOCK_ANVIL_USE, 1.0f, 1.0f),
                new EffectUtils.SoundLayer(Sound.ITEM_ARMOR_EQUIP_IRON, 1.0f, 0.9f));
        EffectUtils.burst(player.getLocation().add(0, 1, 0),
                new EffectUtils.Layer(Particle.CRIT, 15, 0.3, 0.4, 0.3, 0),
                new EffectUtils.Layer(Particle.FIREWORK_SPARK, 10, 0.3, 0.4, 0.3, 0.02));
        return true;
    }
}

class CurePulse extends Ability {
    private final ItemEditFull plugin;
    public CurePulse(ItemEditFull plugin) {
        super("cure_pulse", "Cure Pulse", "Releases a pulse that debuffs nearby zombies or cures an afflicted villager.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double radius = getDoubleParam(plugin, item, "radius", 5.0);
        Location loc = player.getLocation();
        ZombieVillager afflicted = null;
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
            if (entity instanceof ZombieVillager) {
                afflicted = (ZombieVillager) entity;
                break;
            }
        }
        if (afflicted != null) {
            Location villagerLoc = afflicted.getLocation();
            afflicted.remove();
            Villager villager = (Villager) villagerLoc.getWorld().spawnEntity(villagerLoc, EntityType.VILLAGER);
            EffectUtils.fanfare(villagerLoc,
                    new EffectUtils.SoundLayer(Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 1.0f, 1.0f),
                    new EffectUtils.SoundLayer(Sound.ENTITY_VILLAGER_CELEBRATE, 0.8f, 1.1f));
            EffectUtils.burst(villagerLoc.add(0, 1, 0),
                    new EffectUtils.Layer(Particle.VILLAGER_HAPPY, 20, 0.4, 0.6, 0.4, 0),
                    new EffectUtils.Layer(Particle.END_ROD, 6, 0.3, 0.5, 0.3, 0.02));
            player.sendMessage("§aYou cured an afflicted villager!");
            return true;
        }
        boolean debuffed = false;
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
            if (entity instanceof Zombie) {
                ((Zombie) entity).addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 1));
                ((Zombie) entity).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 1));
                debuffed = true;
            }
        }
        if (!debuffed) {
            player.sendMessage("§cNo zombies or afflicted villagers nearby.");
            return false;
        }
        EffectUtils.fanfare(loc, new EffectUtils.SoundLayer(Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED, 1.0f, 0.8f));
        EffectUtils.burst(loc.clone().add(0, 1, 0),
                new EffectUtils.Layer(Particle.VILLAGER_HAPPY, 15, radius * 0.3, 0.4, radius * 0.3, 0),
                new EffectUtils.Layer(Particle.SPELL_MOB, 10, radius * 0.2, 0.4, radius * 0.2, 0));
        return true;
    }
}

class CartographersReveal extends Ability {
    private final ItemEditFull plugin;
    public CartographersReveal(ItemEditFull plugin) {
        super("cartographers_reveal", "Cartographer's Reveal", "Marks all nearby points of interest on your compass briefly.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        int radius = getIntParam(plugin, item, "radius", 40);
        Location loc = player.getLocation();
        Block nearest = null;
        double nearestDist = Double.MAX_VALUE;
        int step = 3;
        for (int x = -radius; x <= radius; x += step) {
            for (int z = -radius; z <= radius; z += step) {
                for (int y = -6; y <= 6; y += step) {
                    Block b = loc.clone().add(x, y, z).getBlock();
                    Material type = b.getType();
                    if (type == Material.CHEST || type == Material.SPAWNER || type == Material.BELL) {
                        double dist = b.getLocation().distanceSquared(loc);
                        if (dist < nearestDist) {
                            nearestDist = dist;
                            nearest = b;
                        }
                    }
                }
            }
        }
        if (nearest == null) {
            player.sendMessage("§7No points of interest found nearby.");
            return true;
        }
        player.setCompassTarget(nearest.getLocation());
        player.sendMessage("§bCompass now points to a nearby point of interest, " + Math.round(Math.sqrt(nearestDist)) + " blocks away.");
        EffectUtils.fanfare(player.getLocation(),
                new EffectUtils.SoundLayer(Sound.ITEM_LODESTONE_COMPASS_LOCK, 1.0f, 1.0f),
                new EffectUtils.SoundLayer(Sound.BLOCK_NOTE_BLOCK_BELL, 0.7f, 1.4f));
        EffectUtils.burst(player.getLocation().add(0, 1, 0),
                new EffectUtils.Layer(Particle.END_ROD, 8, 0.2, 0.3, 0.2, 0.02),
                new EffectUtils.Layer(Particle.VILLAGER_HAPPY, 6, 0.2, 0.3, 0.2, 0));
        return true;
    }
}

class MasonsWall extends Ability {
    private final ItemEditFull plugin;
    public MasonsWall(ItemEditFull plugin) {
        super("masons_wall", "Mason's Wall", "Quickly raises a defensive stone wall in front of you.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 12.0);
        int width = getIntParam(plugin, item, "width", 3);
        Vector dir = VectorUtils.safeNormalize(player.getLocation().getDirection().setY(0));
        if (dir.lengthSquared() < 0.01) {
            dir = new Vector(0, 0, 1);
        }
        Vector right = new Vector(-dir.getZ(), 0, dir.getX());
        Location base = player.getLocation().add(dir.clone().multiply(2));
        List<Block> wallBlocks = new ArrayList<>();
        int half = width / 2;
        for (int i = -half; i <= half; i++) {
            for (int y = 0; y <= 2; y++) {
                Block block = base.clone().add(right.clone().multiply(i)).add(0, y, 0).getBlock();
                if (block.getType() == Material.AIR) {
                    block.setType(Material.STONE_BRICKS);
                    wallBlocks.add(block);
                }
            }
        }
        EffectUtils.fanfare(player.getLocation(),
                new EffectUtils.SoundLayer(Sound.BLOCK_STONE_PLACE, 1.0f, 1.0f),
                new EffectUtils.SoundLayer(Sound.BLOCK_STONE_BREAK, 0.6f, 0.7f));
        for (Block b : wallBlocks) {
            EffectUtils.burst(b.getLocation().add(0.5, 0.5, 0.5),
                    new EffectUtils.Layer(Particle.BLOCK_CRACK, 6, 0.3, 0.3, 0.3, 0, Material.STONE_BRICKS.createBlockData()));
        }
        new CompatRunnable() {
            @Override
            public void run() {
                for (Block b : wallBlocks) {
                    if (b.getType() == Material.STONE_BRICKS) {
                        b.setType(Material.AIR);
                    }
                }
            }
        }.runTaskLater(plugin, base, (long) (duration * 20));
        return true;
    }
}

class FletchersBarrage extends Ability {
    private final ItemEditFull plugin;
    public FletchersBarrage(ItemEditFull plugin) {
        super("fletchers_barrage", "Fletcher's Barrage", "Fires a rapid barrage of arrows crafted on the spot.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        int count = getIntParam(plugin, item, "count", 6);
        double damageEach = getDoubleParam(plugin, item, "damage_each", 2.0);
        Location origin = player.getEyeLocation();
        Vector facing = origin.getDirection().normalize();
        EffectUtils.fanfare(origin,
                new EffectUtils.SoundLayer(Sound.ITEM_CROSSBOW_SHOOT, 1.2f, 1.2f),
                new EffectUtils.SoundLayer(Sound.BLOCK_WOOD_BREAK, 0.5f, 1.4f));
        for (int i = 0; i < count; i++) {
            double spreadAngle = Math.toRadians((i - (count - 1) / 2.0) * 8.0);
            Vector spread = new Vector(
                    facing.getX() * Math.cos(spreadAngle) - facing.getZ() * Math.sin(spreadAngle),
                    facing.getY(),
                    facing.getX() * Math.sin(spreadAngle) + facing.getZ() * Math.cos(spreadAngle)
            );
            for (int d = 1; d <= 10; d++) {
                Location point = origin.clone().add(spread.clone().multiply(d));
                point.getWorld().spawnParticle(Particle.CRIT, point, 1, 0, 0, 0, 0);
                for (Entity entity : point.getWorld().getNearbyEntities(point, 0.8, 0.8, 0.8)) {
                    if (entity instanceof LivingEntity && !entity.equals(player)) {
                        ((LivingEntity) entity).damage(damageEach, player);
                    }
                }
            }
        }
        return true;
    }
}
