package com.itemedit.full.ability.warden;

import com.itemedit.full.ItemEditFull;
import com.itemedit.full.ability.Ability;
import com.itemedit.full.utils.CompatRunnable;
import com.itemedit.full.utils.VectorUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Deep Dark expansion: 20 additional sculk/vibration-themed abilities layered
 * on top of the original 5 warden abilities.
 */
public class WardenExpansionAbilities implements Listener {

    // ---- Resonant Ward: absorbs a set number of hits and reflects part of the damage ----
    private static final Map<UUID, Long> wardActive = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> wardHitsRemaining = new ConcurrentHashMap<>();
    private static final Map<UUID, Double> wardReflectPercent = new ConcurrentHashMap<>();

    // ---- Warden Hide: flat damage reduction while active ----
    private static final Map<UUID, Long> hideActive = new ConcurrentHashMap<>();
    private static final Map<UUID, Double> hideFlatReduction = new ConcurrentHashMap<>();

    private static ItemEditFull pluginInstance;

    public static void register(ItemEditFull plugin) {
        pluginInstance = plugin;

        plugin.getAbilityManager().registerAbility(new GloomStep(plugin));
        plugin.getAbilityManager().registerAbility(new EchoShard(plugin));
        plugin.getAbilityManager().registerAbility(new DarknessVeil(plugin));
        plugin.getAbilityManager().registerAbility(new SculkBloom(plugin));
        plugin.getAbilityManager().registerAbility(new ResonantWard(plugin));
        plugin.getAbilityManager().registerAbility(new TendrilLash(plugin));
        plugin.getAbilityManager().registerAbility(new HeartbeatDread(plugin));
        plugin.getAbilityManager().registerAbility(new WardensCharge(plugin));
        plugin.getAbilityManager().registerAbility(new SculkSpikes(plugin));
        plugin.getAbilityManager().registerAbility(new DeepResonance(plugin));
        plugin.getAbilityManager().registerAbility(new GloomCamouflage(plugin));
        plugin.getAbilityManager().registerAbility(new ShriekingPulse(plugin));
        plugin.getAbilityManager().registerAbility(new SculkHarvest(plugin));
        plugin.getAbilityManager().registerAbility(new VibrationTrap(plugin));
        plugin.getAbilityManager().registerAbility(new AbyssalRoar(plugin));
        plugin.getAbilityManager().registerAbility(new SculkRegeneration(plugin));
        plugin.getAbilityManager().registerAbility(new NightPursuit(plugin));
        plugin.getAbilityManager().registerAbility(new SonicLance(plugin));
        plugin.getAbilityManager().registerAbility(new WardenHide(plugin));
        plugin.getAbilityManager().registerAbility(new SilenceWard(plugin));

        plugin.getServer().getPluginManager().registerEvents(new WardenExpansionAbilities(), plugin);
    }

    static void activateWard(UUID uuid, long expireAt, int hits, double reflectPercent) {
        wardActive.put(uuid, expireAt);
        wardHitsRemaining.put(uuid, hits);
        wardReflectPercent.put(uuid, reflectPercent);
    }

    static void activateHide(UUID uuid, long expireAt, double flatReduction) {
        hideActive.put(uuid, expireAt);
        hideFlatReduction.put(uuid, flatReduction);
    }

    private static boolean isSculkLike(Material type) {
        return type == Material.SCULK || type == Material.SCULK_VEIN || type == Material.SCULK_CATALYST
                || type == Material.SCULK_SENSOR || type == Material.SCULK_SHRIEKER;
    }

    @EventHandler
    public void onWardDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity();
        Long expire = wardActive.get(player.getUniqueId());
        if (expire == null) {
            return;
        }
        if (System.currentTimeMillis() >= expire || wardHitsRemaining.getOrDefault(player.getUniqueId(), 0) <= 0) {
            clearWard(player.getUniqueId());
            return;
        }
        double reflect = wardReflectPercent.getOrDefault(player.getUniqueId(), 0.5);
        double reflectedAmount = event.getDamage() * reflect;
        event.setCancelled(true);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_SCULK_SENSOR_CLICKING_STOP, 1.0f, 1.0f);
        player.getWorld().spawnParticle(Particle.SCULK_CHARGE_POP, player.getLocation().add(0, 1, 0), 10, 0.3, 0.4, 0.3, 0);
        if (event.getDamager() instanceof LivingEntity) {
            ((LivingEntity) event.getDamager()).damage(reflectedAmount, player);
        }
        int remaining = wardHitsRemaining.getOrDefault(player.getUniqueId(), 1) - 1;
        if (remaining <= 0) {
            clearWard(player.getUniqueId());
        } else {
            wardHitsRemaining.put(player.getUniqueId(), remaining);
        }
    }

    @EventHandler
    public void onHideDamage(org.bukkit.event.entity.EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity();
        Long expire = hideActive.get(player.getUniqueId());
        if (expire == null) {
            return;
        }
        if (System.currentTimeMillis() >= expire) {
            hideActive.remove(player.getUniqueId());
            hideFlatReduction.remove(player.getUniqueId());
            return;
        }
        double reduction = hideFlatReduction.getOrDefault(player.getUniqueId(), 3.0);
        event.setDamage(Math.max(0.0, event.getDamage() - reduction));
    }

    @EventHandler
    public void onEchoShardHit(ProjectileHitEvent event) {
        if (!event.getEntity().hasMetadata("warden_echo_shard")) {
            return;
        }
        if (!(event.getHitEntity() instanceof LivingEntity)) {
            return;
        }
        LivingEntity target = (LivingEntity) event.getHitEntity();
        double damage = event.getEntity().hasMetadata("echo_damage") ? event.getEntity().getMetadata("echo_damage").get(0).asDouble() : 5.0;
        Object shooter = event.getEntity().getShooter();
        if (shooter instanceof Player) {
            target.damage(damage, (Player) shooter);
        } else {
            target.damage(damage);
        }
        target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 120, 0));
        target.getWorld().playSound(target.getLocation(), Sound.BLOCK_SCULK_SENSOR_CLICKING, 1.0f, 0.8f);
        target.getWorld().spawnParticle(Particle.SCULK_SOUL, target.getLocation().add(0, 1, 0), 10, 0.2, 0.3, 0.2, 0.02);
    }

    private static void clearWard(UUID uuid) {
        wardActive.remove(uuid);
        wardHitsRemaining.remove(uuid);
        wardReflectPercent.remove(uuid);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        clearWard(id);
        hideActive.remove(id);
        hideFlatReduction.remove(id);
    }
}

// ==================== 20 NEW DEEP DARK ABILITIES ====================

class GloomStep extends Ability {
    private final ItemEditFull plugin;
    public GloomStep(ItemEditFull plugin) {
        super("gloom_step", "Gloom Step", "Teleport a short distance through sculk shadow, leaving a burst of dark particles.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double range = getDoubleParam(plugin, item, "range", 8.0);
        Location origin = player.getLocation();
        Vector dir = VectorUtils.safeNormalize(player.getLocation().getDirection());
        Location dest = origin.clone();
        for (int i = (int) range; i >= 1; i--) {
            Location candidate = origin.clone().add(dir.clone().multiply(i));
            if (candidate.getBlock().getType().isAir() && candidate.clone().add(0, 1, 0).getBlock().getType().isAir()) {
                dest = candidate;
                break;
            }
        }
        origin.getWorld().spawnParticle(Particle.SCULK_SOUL, origin.add(0, 1, 0), 20, 0.3, 0.5, 0.3, 0.05);
        player.teleport(dest.setDirection(player.getLocation().getDirection()));
        dest.getWorld().spawnParticle(Particle.SCULK_SOUL, dest.clone().add(0, 1, 0), 20, 0.3, 0.5, 0.3, 0.05);
        dest.getWorld().playSound(dest, Sound.ENTITY_WARDEN_TENDRIL_CLICKS, 1.0f, 1.2f);
        return true;
    }
}

class EchoShard extends Ability {
    private final ItemEditFull plugin;
    public EchoShard(ItemEditFull plugin) {
        super("echo_shard", "Echo Shard", "Fires a shard of resonant sculk that damages and marks the target for vibration tracking.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double damage = getDoubleParam(plugin, item, "damage", 5.0);
        Snowball shard = player.launchProjectile(Snowball.class);
        shard.setMetadata("warden_echo_shard", new FixedMetadataValue(plugin, true));
        shard.setMetadata("echo_damage", new FixedMetadataValue(plugin, damage));
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_SCULK_SENSOR_CLICKING, 1.0f, 0.7f);
        return true;
    }
}

class DarknessVeil extends Ability {
    private final ItemEditFull plugin;
    public DarknessVeil(ItemEditFull plugin) {
        super("darkness_veil", "Darkness Veil", "Blinds nearby enemies with a wave of living darkness.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double radius = getDoubleParam(plugin, item, "radius", 6.0);
        double blindDuration = getDoubleParam(plugin, item, "blind_duration", 4.0);
        Location loc = player.getLocation();
        loc.getWorld().playSound(loc, Sound.ENTITY_WARDEN_ROAR, 0.8f, 0.5f);
        loc.getWorld().spawnParticle(Particle.SCULK_SOUL, loc.add(0, 1, 0), 40, radius * 0.4, 0.6, radius * 0.4, 0.02);
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
            if (entity instanceof LivingEntity && !entity.equals(player)) {
                LivingEntity living = (LivingEntity) entity;
                living.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, (int) (blindDuration * 20), 0));
                living.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, (int) (blindDuration * 20), 0));
            }
        }
        return true;
    }
}

class SculkBloom extends Ability {
    private final ItemEditFull plugin;
    public SculkBloom(ItemEditFull plugin) {
        super("sculk_bloom", "Sculk Bloom", "Spreads a patch of sculk beneath you that slows enemies who cross it.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double radius = getDoubleParam(plugin, item, "radius", 4.0);
        double duration = getDoubleParam(plugin, item, "duration", 12.0);
        Location center = player.getLocation();
        List<Block> placed = new ArrayList<>();
        int r = (int) radius;
        for (int x = -r; x <= r; x++) {
            for (int z = -r; z <= r; z++) {
                if (x * x + z * z <= radius * radius) {
                    Block below = center.clone().add(x, -1, z).getBlock();
                    if (below.getType().isSolid() && below.getType() != Material.SCULK && Math.random() < 0.5) {
                        placed.add(below);
                    }
                }
            }
        }
        Map<Block, Material> originals = new ConcurrentHashMap<>();
        for (Block b : placed) {
            originals.put(b, b.getType());
            b.setType(Material.SCULK);
        }
        player.getWorld().playSound(center, Sound.BLOCK_SCULK_SPREAD, 1.0f, 0.8f);
        new CompatRunnable() {
            int ticksElapsed = 0;
            @Override
            public void run() {
                if (ticksElapsed >= (duration * 20)) {
                    for (Map.Entry<Block, Material> entry : originals.entrySet()) {
                        if (entry.getKey().getType() == Material.SCULK) {
                            entry.getKey().setType(entry.getValue());
                        }
                    }
                    cancel();
                    return;
                }
                for (Block b : placed) {
                    for (Entity entity : b.getWorld().getNearbyEntities(b.getLocation().add(0.5, 1, 0.5), 0.6, 1.0, 0.6)) {
                        if (entity instanceof LivingEntity && !entity.equals(player)) {
                            ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 30, 2));
                        }
                    }
                }
                ticksElapsed += 10;
            }
        }.runTaskTimer(plugin, center, 0L, 10L);
        return true;
    }
}

class ResonantWard extends Ability {
    private final ItemEditFull plugin;
    public ResonantWard(ItemEditFull plugin) {
        super("resonant_ward", "Resonant Ward", "Raises a barrier that absorbs the next hit and reflects part of it back.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 8.0);
        int absorbHits = getIntParam(plugin, item, "absorb_hits", 1);
        double reflect = getDoubleParam(plugin, item, "reflect", 0.5);
        WardenExpansionAbilities.activateWard(player.getUniqueId(), System.currentTimeMillis() + (long) (duration * 1000), absorbHits, reflect);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_SCULK_SENSOR_PLACE, 1.0f, 1.2f);
        player.getWorld().spawnParticle(Particle.SCULK_CHARGE, player.getLocation().add(0, 1, 0), 20, 0.4, 0.6, 0.4, 0.1);
        player.sendMessage("§5A resonant ward shields you!");
        return true;
    }
}

class TendrilLash extends Ability {
    private final ItemEditFull plugin;
    public TendrilLash(ItemEditFull plugin) {
        super("tendril_lash", "Tendril Lash", "A sculk tendril lashes out, pulling one enemy toward you.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double range = getDoubleParam(plugin, item, "range", 7.0);
        Entity target = player.getTargetEntity((int) range);
        if (!(target instanceof LivingEntity)) {
            player.sendMessage("§cNo target in range.");
            return false;
        }
        LivingEntity living = (LivingEntity) target;
        Vector pull = VectorUtils.safeNormalize(player.getLocation().toVector().subtract(living.getLocation().toVector())).multiply(1.2).setY(0.2);
        living.setVelocity(pull);
        living.getWorld().playSound(living.getLocation(), Sound.ENTITY_WARDEN_TENDRIL_CLICKS, 1.2f, 0.7f);
        living.getWorld().spawnParticle(Particle.SCULK_SOUL, living.getLocation(), 15, 0.3, 0.3, 0.3, 0.05);
        return true;
    }
}

class HeartbeatDread extends Ability {
    private final ItemEditFull plugin;
    public HeartbeatDread(ItemEditFull plugin) {
        super("heartbeat_dread", "Heartbeat Dread", "A pulsing dread aura slows and weakens enemies who linger nearby.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double radius = getDoubleParam(plugin, item, "radius", 5.0);
        double duration = getDoubleParam(plugin, item, "duration", 6.0);
        Location loc = player.getLocation();
        loc.getWorld().playSound(loc, Sound.ENTITY_WARDEN_HEARTBEAT, 1.5f, 0.9f);
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, radius, 2.0, radius)) {
            if (entity instanceof LivingEntity && !entity.equals(player)) {
                LivingEntity living = (LivingEntity) entity;
                living.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) (duration * 20), 1));
                living.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, (int) (duration * 20), 1));
            }
        }
        return true;
    }
}

class WardensCharge extends Ability {
    private final ItemEditFull plugin;
    public WardensCharge(ItemEditFull plugin) {
        super("wardens_charge", "Warden's Charge", "Bull-rush forward, knocking aside and damaging anything in your path.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double range = getDoubleParam(plugin, item, "range", 6.0);
        double damage = getDoubleParam(plugin, item, "damage", 6.0);
        Location start = player.getLocation();
        Vector dir = VectorUtils.safeNormalize(start.getDirection().setY(0));
        if (dir.lengthSquared() < 0.01) {
            dir = new Vector(0, 0, 1);
        }
        final Vector chargeDir = dir;
        player.getWorld().playSound(start, Sound.ENTITY_WARDEN_STEP, 1.5f, 0.6f);
        for (int i = 1; i <= (int) range; i++) {
            Location step = start.clone().add(chargeDir.clone().multiply(i)).add(0, 1, 0);
            step.getWorld().spawnParticle(Particle.SCULK_CHARGE_POP, step, 3, 0.2, 0.2, 0.2, 0);
            for (Entity entity : step.getWorld().getNearbyEntities(step, 1.2, 1.2, 1.2)) {
                if (entity instanceof LivingEntity && !entity.equals(player)) {
                    LivingEntity living = (LivingEntity) entity;
                    living.damage(damage, player);
                    living.setVelocity(chargeDir.clone().multiply(1.3).setY(0.3));
                }
            }
        }
        player.setVelocity(chargeDir.clone().multiply(1.1).setY(Math.max(player.getVelocity().getY(), 0.1)));
        return true;
    }
}

class SculkSpikes extends Ability {
    private final ItemEditFull plugin;
    public SculkSpikes(ItemEditFull plugin) {
        super("sculk_spikes", "Sculk Spikes", "Erupts a line of sculk spikes from the ground ahead of you.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double range = getDoubleParam(plugin, item, "range", 5.0);
        double damage = getDoubleParam(plugin, item, "damage", 6.0);
        Location start = player.getLocation();
        Vector dir = VectorUtils.safeNormalize(start.getDirection().setY(0));
        if (dir.lengthSquared() < 0.01) {
            dir = new Vector(0, 0, 1);
        }
        player.getWorld().playSound(start, Sound.BLOCK_SCULK_CATALYST_BLOOM, 1.0f, 0.8f);
        for (int i = 1; i <= (int) range; i++) {
            Location step = start.clone().add(dir.clone().multiply(i));
            step.getWorld().spawnParticle(Particle.SCULK_CHARGE, step, 6, 0.15, 0.4, 0.15, 0.05);
            for (Entity entity : step.getWorld().getNearbyEntities(step, 1.0, 1.5, 1.0)) {
                if (entity instanceof LivingEntity && !entity.equals(player)) {
                    ((LivingEntity) entity).damage(damage, player);
                }
            }
        }
        return true;
    }
}

class DeepResonance extends Ability {
    private final ItemEditFull plugin;
    public DeepResonance(ItemEditFull plugin) {
        super("deep_resonance", "Deep Resonance", "Emits a low vibration that reveals nearby hidden or sneaking enemies to you.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double radius = getDoubleParam(plugin, item, "radius", 10.0);
        double duration = getDoubleParam(plugin, item, "duration", 8.0);
        Location loc = player.getLocation();
        loc.getWorld().playSound(loc, Sound.BLOCK_SCULK_SENSOR_CLICKING, 1.2f, 1.0f);
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
            if (entity instanceof LivingEntity && !entity.equals(player)) {
                ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, (int) (duration * 20), 0));
            }
        }
        player.sendMessage("§5You feel the vibrations of everything nearby.");
        return true;
    }
}

class GloomCamouflage extends Ability {
    private final ItemEditFull plugin;
    public GloomCamouflage(ItemEditFull plugin) {
        super("gloom_camouflage", "Gloom Camouflage", "Merges with nearby sculk or dark blocks, granting brief near-invisibility while still.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 6.0);
        Location startLoc = player.getLocation();
        player.getWorld().playSound(startLoc, Sound.BLOCK_SCULK_PLACE, 1.0f, 0.6f);
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, (int) (duration * 20), 0, false, false));
        new CompatRunnable() {
            int ticksElapsed = 0;
            @Override
            public void run() {
                if (!player.isOnline() || ticksElapsed >= (duration * 20)) {
                    cancel();
                    return;
                }
                if (player.getLocation().distanceSquared(startLoc) > 1.0) {
                    player.removePotionEffect(PotionEffectType.INVISIBILITY);
                    cancel();
                    return;
                }
                ticksElapsed += 5;
            }
        }.runTaskTimer(plugin, player, 5L, 5L);
        return true;
    }
}

class ShriekingPulse extends Ability {
    private final ItemEditFull plugin;
    public ShriekingPulse(ItemEditFull plugin) {
        super("shrieking_pulse", "Shrieking Pulse", "A short-range shriek that deafens and disorients nearby enemies.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double radius = getDoubleParam(plugin, item, "radius", 4.0);
        double duration = getDoubleParam(plugin, item, "duration", 3.0);
        Location loc = player.getLocation();
        loc.getWorld().playSound(loc, Sound.ENTITY_WARDEN_AGITATED, 1.3f, 1.0f);
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
            if (entity instanceof LivingEntity && !entity.equals(player)) {
                LivingEntity living = (LivingEntity) entity;
                living.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, (int) (duration * 20), 1));
                living.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) (duration * 20), 1));
            }
        }
        return true;
    }
}

class SculkHarvest extends Ability {
    private final ItemEditFull plugin;
    public SculkHarvest(ItemEditFull plugin) {
        super("sculk_harvest", "Sculk Harvest", "Drains a small amount of health from a struck enemy into you.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double damage = getDoubleParam(plugin, item, "damage", 4.0);
        double heal = getDoubleParam(plugin, item, "heal", 2.0);
        Entity target = player.getTargetEntity(4);
        if (!(target instanceof LivingEntity)) {
            player.sendMessage("§cNo target in range.");
            return false;
        }
        LivingEntity living = (LivingEntity) target;
        living.damage(damage, player);
        double maxHealth = player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
        player.setHealth(Math.min(maxHealth, player.getHealth() + heal));
        living.getWorld().playSound(living.getLocation(), Sound.BLOCK_SCULK_CATALYST_BLOOM, 1.0f, 1.0f);
        player.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0, 1.5, 0), 3, 0.2, 0.2, 0.2, 0.01);
        return true;
    }
}

class VibrationTrap extends Ability {
    private final ItemEditFull plugin;
    public VibrationTrap(ItemEditFull plugin) {
        super("vibration_trap", "Vibration Trap", "Plants an invisible trigger that roots and damages the first enemy to step near it.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double triggerRadius = getDoubleParam(plugin, item, "radius", 2.0);
        double rootDuration = getDoubleParam(plugin, item, "root_duration", 2.0);
        double damage = getDoubleParam(plugin, item, "damage", 4.0);
        Block targetBlock = player.getTargetBlockExact(5);
        Location trapLoc = (targetBlock != null ? targetBlock.getLocation() : player.getLocation()).add(0.5, 1, 0.5);
        trapLoc.getWorld().playSound(trapLoc, Sound.BLOCK_SCULK_SENSOR_PLACE, 1.0f, 1.0f);
        new CompatRunnable() {
            int ticksElapsed = 0;
            @Override
            public void run() {
                if (ticksElapsed >= 300) {
                    cancel();
                    return;
                }
                trapLoc.getWorld().spawnParticle(Particle.SCULK_CHARGE_POP, trapLoc, 1, 0.1, 0.1, 0.1, 0);
                for (Entity entity : trapLoc.getWorld().getNearbyEntities(trapLoc, triggerRadius, triggerRadius, triggerRadius)) {
                    if (entity instanceof LivingEntity && !entity.equals(player)) {
                        LivingEntity living = (LivingEntity) entity;
                        living.damage(damage, player);
                        living.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) (rootDuration * 20), 6));
                        trapLoc.getWorld().playSound(trapLoc, Sound.BLOCK_SCULK_SHRIEKER_SHRIEK, 1.0f, 1.2f);
                        cancel();
                        return;
                    }
                }
                ticksElapsed += 5;
            }
        }.runTaskTimer(plugin, trapLoc, 0L, 5L);
        return true;
    }
}

class AbyssalRoar extends Ability {
    private final ItemEditFull plugin;
    public AbyssalRoar(ItemEditFull plugin) {
        super("abyssal_roar", "Abyssal Roar", "Unleashes a roar that fears nearby enemies, causing them to flee briefly.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double radius = getDoubleParam(plugin, item, "radius", 6.0);
        Location loc = player.getLocation();
        loc.getWorld().playSound(loc, Sound.ENTITY_WARDEN_ROAR, 2.0f, 0.7f);
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
            if (entity instanceof LivingEntity && !entity.equals(player)) {
                LivingEntity living = (LivingEntity) entity;
                Vector away = VectorUtils.safeNormalize(living.getLocation().toVector().subtract(loc.toVector())).multiply(1.5).setY(0.3);
                living.setVelocity(away);
                living.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0));
            }
        }
        return true;
    }
}

class SculkRegeneration extends Ability {
    private final ItemEditFull plugin;
    public SculkRegeneration(ItemEditFull plugin) {
        super("sculk_regeneration", "Sculk Regeneration", "Converts nearby sculk energy into healing over time.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 8.0);
        double healPerTick = getDoubleParam(plugin, item, "heal_tick", 1.0);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_SCULK_CATALYST_BLOOM, 1.0f, 1.0f);
        new CompatRunnable() {
            int ticksElapsed = 0;
            @Override
            public void run() {
                if (!player.isOnline() || ticksElapsed >= (duration * 20)) {
                    cancel();
                    return;
                }
                Location loc = player.getLocation();
                boolean nearSculk = false;
                for (int x = -3; x <= 3 && !nearSculk; x++) {
                    for (int y = -2; y <= 2 && !nearSculk; y++) {
                        for (int z = -3; z <= 3 && !nearSculk; z++) {
                            if (isSculkNearby(loc.clone().add(x, y, z).getBlock().getType())) {
                                nearSculk = true;
                            }
                        }
                    }
                }
                if (nearSculk) {
                    double maxHealth = player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
                    player.setHealth(Math.min(maxHealth, player.getHealth() + healPerTick));
                    loc.getWorld().spawnParticle(Particle.SCULK_SOUL, loc.add(0, 1, 0), 3, 0.2, 0.3, 0.2, 0.01);
                }
                ticksElapsed += 20;
            }
            private boolean isSculkNearby(Material type) {
                return type == Material.SCULK || type == Material.SCULK_VEIN || type == Material.SCULK_CATALYST
                        || type == Material.SCULK_SENSOR || type == Material.SCULK_SHRIEKER;
            }
        }.runTaskTimer(plugin, player, 20L, 20L);
        return true;
    }
}

class NightPursuit extends Ability {
    private final ItemEditFull plugin;
    public NightPursuit(ItemEditFull plugin) {
        super("night_pursuit", "Night Pursuit", "Grants bonus speed and attack damage while in darkness or at night.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 10.0);
        long time = player.getWorld().getTime();
        boolean isNight = time >= 13000 && time <= 23000;
        int lightLevel = player.getLocation().getBlock().getLightLevel();
        boolean isDark = lightLevel <= 4;
        int amplifier = (isNight || isDark) ? 2 : 1;
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, (int) (duration * 20), amplifier));
        player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, (int) (duration * 20), (isNight || isDark) ? 1 : 0));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WARDEN_STEP, 0.8f, 1.3f);
        player.sendMessage((isNight || isDark) ? "§5The dark empowers your hunt!" : "§7You feel a faint pull of the night.");
        return true;
    }
}

class SonicLance extends Ability {
    private final ItemEditFull plugin;
    public SonicLance(ItemEditFull plugin) {
        super("sonic_lance", "Sonic Lance", "Fires a piercing line of sound that damages every enemy struck.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double range = getDoubleParam(plugin, item, "range", 8.0);
        double damage = getDoubleParam(plugin, item, "damage", 6.0);
        Location origin = player.getEyeLocation();
        Vector dir = origin.getDirection().normalize();
        player.getWorld().playSound(origin, Sound.ENTITY_WARDEN_SONIC_CHARGE, 1.0f, 1.3f);
        for (int i = 0; i < range; i++) {
            Location point = origin.clone().add(dir.clone().multiply(i));
            point.getWorld().spawnParticle(Particle.SCULK_CHARGE_POP, point, 1, 0, 0, 0, 0);
            for (Entity entity : point.getWorld().getNearbyEntities(point, 1.0, 1.0, 1.0)) {
                if (entity instanceof LivingEntity && !entity.equals(player)) {
                    ((LivingEntity) entity).damage(damage, player);
                }
            }
        }
        return true;
    }
}

class WardenHide extends Ability {
    private final ItemEditFull plugin;
    public WardenHide(ItemEditFull plugin) {
        super("warden_hide", "Warden Hide", "Temporarily thickens your skin with sculk plating, granting flat damage reduction.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 10.0);
        double reduction = getDoubleParam(plugin, item, "reduction", 3.0);
        WardenExpansionAbilities.activateHide(player.getUniqueId(), System.currentTimeMillis() + (long) (duration * 1000), reduction);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_SCULK_PLACE, 1.0f, 0.5f);
        player.getWorld().spawnParticle(Particle.SCULK_CHARGE, player.getLocation().add(0, 1, 0), 20, 0.4, 0.6, 0.4, 0.05);
        player.sendMessage("§5Your skin thickens with sculk plating!");
        return true;
    }
}

class SilenceWard extends Ability {
    private final ItemEditFull plugin;
    public SilenceWard(ItemEditFull plugin) {
        super("silence_ward", "Silence Ward", "Suppresses a nearby enemy's item abilities for a short time.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 5.0);
        Entity target = player.getTargetEntity(8);
        if (!(target instanceof Player)) {
            player.sendMessage("§cNo player target in sight to silence.");
            return false;
        }
        Player targetPlayer = (Player) target;
        plugin.getAbilityManager().silencePlayer(targetPlayer, (long) (duration * 1000));
        targetPlayer.getWorld().playSound(targetPlayer.getLocation(), Sound.BLOCK_SCULK_SHRIEKER_SHRIEK, 0.8f, 1.5f);
        targetPlayer.getWorld().spawnParticle(Particle.SCULK_SOUL, targetPlayer.getLocation().add(0, 1.5, 0), 15, 0.3, 0.4, 0.3, 0.02);
        targetPlayer.sendMessage("§5Your abilities have been silenced!");
        player.sendMessage("§6You silence " + targetPlayer.getName() + "!");
        return true;
    }
}
