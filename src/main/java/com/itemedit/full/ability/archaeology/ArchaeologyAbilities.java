package com.itemedit.full.ability.archaeology;

import com.itemedit.full.ItemEditFull;
import com.itemedit.full.ability.Ability;
import com.itemedit.full.utils.CompatRunnable;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sniffer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Buried Record: 10 archaeology-themed abilities (brushes, suspicious sand,
 * sniffers, and ancient relics) - a brand-new school with no prior abilities.
 */
public class ArchaeologyAbilities implements Listener {

    // ---- Fossil Plating: flat damage reduction while active ----
    private static final Map<UUID, Long> fossilActive = new ConcurrentHashMap<>();
    private static final Map<UUID, Double> fossilReduction = new ConcurrentHashMap<>();

    private static ItemEditFull pluginInstance;

    public static void register(ItemEditFull plugin) {
        pluginInstance = plugin;

        plugin.getAbilityManager().registerAbility(new SniffersUnearth(plugin));
        plugin.getAbilityManager().registerAbility(new SuspiciousReveal(plugin));
        plugin.getAbilityManager().registerAbility(new PotteryShardThrow(plugin));
        plugin.getAbilityManager().registerAbility(new BrushDustCloud(plugin));
        plugin.getAbilityManager().registerAbility(new FossilPlating(plugin));
        plugin.getAbilityManager().registerAbility(new RelicResonance(plugin));
        plugin.getAbilityManager().registerAbility(new ArchaeologistsLuck(plugin));
        plugin.getAbilityManager().registerAbility(new CallTheSniffer(plugin));
        plugin.getAbilityManager().registerAbility(new BuriedTreasureSense(plugin));
        plugin.getAbilityManager().registerAbility(new TorchflowerBloom(plugin));

        plugin.getServer().getPluginManager().registerEvents(new ArchaeologyAbilities(), plugin);
    }

    static void activateFossilPlating(UUID uuid, long expireAt, double reduction) {
        fossilActive.put(uuid, expireAt);
        fossilReduction.put(uuid, reduction);
    }

    @EventHandler
    public void onFossilDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity();
        Long expire = fossilActive.get(player.getUniqueId());
        if (expire == null) {
            return;
        }
        if (System.currentTimeMillis() >= expire) {
            fossilActive.remove(player.getUniqueId());
            fossilReduction.remove(player.getUniqueId());
            return;
        }
        double reduction = fossilReduction.getOrDefault(player.getUniqueId(), 3.0);
        event.setDamage(Math.max(0.0, event.getDamage() - reduction));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        fossilActive.remove(id);
        fossilReduction.remove(id);
    }
}

// ==================== 10 BURIED RECORD ABILITIES ====================

class SniffersUnearth extends Ability {
    private final ItemEditFull plugin;
    public SniffersUnearth(ItemEditFull plugin) {
        super("sniffers_unearth", "Sniffer's Unearth", "Calls a sniffer to dig up a random buff seed at your feet.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double digTime = getDoubleParam(plugin, item, "dig_time", 2.0);
        Location loc = player.getLocation();
        loc.getWorld().playSound(loc, Sound.ENTITY_SNIFFER_DIGGING, 1.0f, 1.0f);
        loc.getWorld().spawnParticle(Particle.FALLING_DUST, loc, 15, 0.3, 0.1, 0.3, 0, Material.DIRT.createBlockData());
        new CompatRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    return;
                }
                PotionEffectType[] buffs = {PotionEffectType.SPEED, PotionEffectType.INCREASE_DAMAGE, PotionEffectType.REGENERATION, PotionEffectType.DAMAGE_RESISTANCE};
                PotionEffectType chosen = buffs[(int) (Math.random() * buffs.length)];
                player.addPotionEffect(new PotionEffect(chosen, 200, 0));
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_SNIFFER_HAPPY, 1.0f, 1.2f);
                player.getWorld().spawnParticle(Particle.COMPOSTER, player.getLocation().add(0, 1, 0), 15, 0.3, 0.4, 0.3, 0);
                player.sendMessage("§6You unearthed a seed of " + chosen.getName().toLowerCase().replace("_", " ") + "!");
            }
        }.runTaskLater(plugin, player, (long) (digTime * 20));
        return true;
    }
}

class SuspiciousReveal extends Ability {
    private final ItemEditFull plugin;
    public SuspiciousReveal(ItemEditFull plugin) {
        super("suspicious_reveal", "Suspicious Reveal", "Sends out a pulse that reveals hidden traps, loot, and enemies nearby.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double radius = getDoubleParam(plugin, item, "radius", 8.0);
        double duration = getDoubleParam(plugin, item, "duration", 5.0);
        Location loc = player.getLocation();
        loc.getWorld().playSound(loc, Sound.ITEM_BRUSH_BRUSHING_SAND, 1.0f, 1.2f);
        loc.getWorld().spawnParticle(Particle.FALLING_DUST, loc.add(0, 1, 0), 30, radius * 0.3, 0.4, radius * 0.3, 0, Material.SAND.createBlockData());
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
            if (entity instanceof LivingEntity && !entity.equals(player)) {
                ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, (int) (duration * 20), 0));
            }
        }
        return true;
    }
}

class PotteryShardThrow extends Ability {
    private final ItemEditFull plugin;
    public PotteryShardThrow(ItemEditFull plugin) {
        super("pottery_shard_throw", "Pottery Shard Throw", "Hurls a sharp ancient pottery shard at a target.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double range = getDoubleParam(plugin, item, "range", 8.0);
        double damage = getDoubleParam(plugin, item, "damage", 5.0);
        Entity target = player.getTargetEntity((int) range);
        if (!(target instanceof LivingEntity)) {
            player.sendMessage("§cNo target in range.");
            return false;
        }
        LivingEntity living = (LivingEntity) target;
        living.damage(damage, player);
        living.getWorld().playSound(living.getLocation(), Sound.BLOCK_DECORATED_POT_SHATTER, 1.0f, 1.0f);
        living.getWorld().spawnParticle(Particle.BLOCK_CRACK, living.getLocation().add(0, 1, 0), 15, 0.3, 0.3, 0.3, 0, Material.TERRACOTTA.createBlockData());
        return true;
    }
}

class BrushDustCloud extends Ability {
    private final ItemEditFull plugin;
    public BrushDustCloud(ItemEditFull plugin) {
        super("brush_dust_cloud", "Brush Dust Cloud", "Sweeps a cloud of ancient dust that blinds nearby enemies.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double radius = getDoubleParam(plugin, item, "radius", 4.0);
        double blindDuration = getDoubleParam(plugin, item, "blind_duration", 3.0);
        Location loc = player.getLocation();
        loc.getWorld().playSound(loc, Sound.ITEM_BRUSH_BRUSHING_GRAVEL, 1.0f, 1.0f);
        loc.getWorld().spawnParticle(Particle.FALLING_DUST, loc.add(0, 1, 0), 25, radius * 0.3, 0.4, radius * 0.3, 0, Material.GRAVEL.createBlockData());
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
            if (entity instanceof LivingEntity && !entity.equals(player)) {
                ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, (int) (blindDuration * 20), 0));
            }
        }
        return true;
    }
}

class FossilPlating extends Ability {
    private final ItemEditFull plugin;
    public FossilPlating(ItemEditFull plugin) {
        super("fossil_plating", "Fossil Plating", "Encases you in fossilised bone plating, granting temporary armour.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 10.0);
        double reduction = getDoubleParam(plugin, item, "reduction", 3.0);
        ArchaeologyAbilities.activateFossilPlating(player.getUniqueId(), System.currentTimeMillis() + (long) (duration * 1000), reduction);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BONE_BLOCK_PLACE, 1.0f, 0.6f);
        player.getWorld().spawnParticle(Particle.BLOCK_CRACK, player.getLocation().add(0, 1, 0), 20, 0.4, 0.6, 0.4, 0, Material.BONE_BLOCK.createBlockData());
        return true;
    }
}

class RelicResonance extends Ability {
    private final ItemEditFull plugin;
    public RelicResonance(ItemEditFull plugin) {
        super("relic_resonance", "Relic Resonance", "Channels an ancient relic for a random beneficial buff.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 8.0);
        PotionEffectType[] buffs = {PotionEffectType.SPEED, PotionEffectType.JUMP, PotionEffectType.FIRE_RESISTANCE, PotionEffectType.WATER_BREATHING, PotionEffectType.NIGHT_VISION};
        PotionEffectType chosen = buffs[(int) (Math.random() * buffs.length)];
        player.addPotionEffect(new PotionEffect(chosen, (int) (duration * 20), 1));
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 0.8f);
        player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation().add(0, 1, 0), 15, 0.3, 0.4, 0.3, 0.02);
        player.sendMessage("§6The relic resonates with " + chosen.getName().toLowerCase().replace("_", " ") + "!");
        return true;
    }
}

class ArchaeologistsLuck extends Ability {
    private final ItemEditFull plugin;
    public ArchaeologistsLuck(ItemEditFull plugin) {
        super("archaeologists_luck", "Archaeologist's Luck", "Grants a temporary boost to loot fortune from kills and blocks.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 20.0);
        player.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, (int) (duration * 20), 0));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.4f);
        return true;
    }
}

class CallTheSniffer extends Ability {
    private final ItemEditFull plugin;
    public CallTheSniffer(ItemEditFull plugin) {
        super("call_the_sniffer", "Call the Sniffer", "Summons a friendly sniffer that sniffs out nearby danger and marks it.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double radius = getDoubleParam(plugin, item, "radius", 12.0);
        double duration = getDoubleParam(plugin, item, "duration", 10.0);
        Location loc = player.getLocation();
        Sniffer sniffer;
        try {
            sniffer = (Sniffer) loc.getWorld().spawnEntity(loc, EntityType.SNIFFER);
        } catch (Exception e) {
            player.sendMessage("§cThe sniffer could not be summoned here.");
            return false;
        }
        sniffer.setMetadata("helper", new FixedMetadataValue(plugin, player.getUniqueId().toString()));
        sniffer.getWorld().playSound(loc, Sound.ENTITY_SNIFFER_IDLE, 1.0f, 1.0f);
        for (Entity entity : sniffer.getNearbyEntities(radius, 5, radius)) {
            if (entity instanceof LivingEntity && !(entity instanceof Player) && !entity.hasMetadata("helper")) {
                ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, (int) (duration * 20), 0));
            }
        }
        new CompatRunnable() {
            @Override
            public void run() {
                if (sniffer.isValid()) {
                    sniffer.getWorld().spawnParticle(Particle.SMOKE_NORMAL, sniffer.getLocation().add(0, 1, 0), 15, 0.3, 0.4, 0.3, 0.02);
                    sniffer.remove();
                }
            }
        }.runTaskLater(plugin, player, (long) (duration * 20));
        return true;
    }
}

class BuriedTreasureSense extends Ability {
    private final ItemEditFull plugin;
    public BuriedTreasureSense(ItemEditFull plugin) {
        super("buried_treasure_sense", "Buried Treasure Sense", "Reveals the direction of the nearest valuable block or chest.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        int range = getIntParam(plugin, item, "range", 30);
        Location loc = player.getLocation();
        Block nearest = null;
        double nearestDist = Double.MAX_VALUE;
        int step = 2;
        for (int x = -range; x <= range; x += step) {
            for (int y = -8; y <= 8; y += step) {
                for (int z = -range; z <= range; z += step) {
                    Block b = loc.clone().add(x, y, z).getBlock();
                    Material type = b.getType();
                    if (type == Material.CHEST || type == Material.TRAPPED_CHEST || type == Material.ENDER_CHEST || type == Material.BARREL) {
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
            player.sendMessage("§6No treasure sensed within range.");
            return true;
        }
        Location target = nearest.getLocation().add(0.5, 0.5, 0.5);
        double dx = target.getX() - loc.getX();
        double dz = target.getZ() - loc.getZ();
        String direction = describeDirection(dx, dz);
        double distance = Math.sqrt(nearestDist);
        player.sendMessage("§6Treasure sensed " + direction + ", about " + Math.round(distance) + " blocks away.");
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_AMETHYST_CLUSTER_STEP, 1.0f, 1.4f);
        return true;
    }
    private String describeDirection(double dx, double dz) {
        double angle = Math.toDegrees(Math.atan2(dx, -dz));
        if (angle < 0) angle += 360;
        String[] dirs = {"north", "north-east", "east", "south-east", "south", "south-west", "west", "north-west"};
        int index = (int) Math.round(angle / 45.0) % 8;
        return "to the " + dirs[index];
    }
}

class TorchflowerBloom extends Ability {
    private final ItemEditFull plugin;
    public TorchflowerBloom(ItemEditFull plugin) {
        super("torchflower_bloom", "Torchflower Bloom", "A blooming torchflower releases healing pollen around you.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double radius = getDoubleParam(plugin, item, "radius", 4.0);
        double heal = getDoubleParam(plugin, item, "heal", 4.0);
        Location loc = player.getLocation();
        loc.getWorld().playSound(loc, Sound.BLOCK_SWEET_BERRY_BUSH_PLACE, 1.0f, 1.3f);
        loc.getWorld().spawnParticle(Particle.COMPOSTER, loc.add(0, 1, 0), 25, radius * 0.3, 0.4, radius * 0.3, 0);
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, radius, 2.0, radius)) {
            if (entity instanceof Player) {
                Player ally = (Player) entity;
                double maxHealth = ally.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                ally.setHealth(Math.min(maxHealth, ally.getHealth() + heal));
            }
        }
        return true;
    }
}
