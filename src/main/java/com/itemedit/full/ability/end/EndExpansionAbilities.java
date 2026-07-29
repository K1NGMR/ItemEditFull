package com.itemedit.full.ability.end;

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
import org.bukkit.entity.Endermite;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ShulkerBullet;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
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
 * The Void Chorus expansion: 20 additional teleport/void-themed abilities layered
 * on top of the original 7 end abilities.
 */
public class EndExpansionAbilities implements Listener {

    // ---- Void Tether: damage the player takes is partially mirrored onto a tethered target ----
    private static final Map<UUID, Long> tetherActive = new ConcurrentHashMap<>();
    private static final Map<UUID, UUID> tetherTarget = new ConcurrentHashMap<>();
    private static final Map<UUID, Double> tetherShare = new ConcurrentHashMap<>();

    // ---- Dragon Scale Ward: absorbs a pool of damage ----
    private static final Map<UUID, Long> scaleWardActive = new ConcurrentHashMap<>();
    private static final Map<UUID, Double> scaleWardPool = new ConcurrentHashMap<>();

    // ---- Purpur Bulwark: blocks incoming projectile damage entirely while active ----
    private static final Map<UUID, Long> bulwarkActive = new ConcurrentHashMap<>();

    private static ItemEditFull pluginInstance;

    public static void register(ItemEditFull plugin) {
        pluginInstance = plugin;

        plugin.getAbilityManager().registerAbility(new ChorusHop(plugin));
        plugin.getAbilityManager().registerAbility(new VoidStep(plugin));
        plugin.getAbilityManager().registerAbility(new EnderBarrage(plugin));
        plugin.getAbilityManager().registerAbility(new EndermiteSwarm(plugin));
        plugin.getAbilityManager().registerAbility(new VoidTether(plugin));
        plugin.getAbilityManager().registerAbility(new EndCrystalBurst(plugin));
        plugin.getAbilityManager().registerAbility(new ObsidianPillar(plugin));
        plugin.getAbilityManager().registerAbility(new GatewayPull(plugin));
        plugin.getAbilityManager().registerAbility(new EnderCamouflage(plugin));
        plugin.getAbilityManager().registerAbility(new VoidGaze(plugin));
        plugin.getAbilityManager().registerAbility(new ChorusRegen(plugin));
        plugin.getAbilityManager().registerAbility(new DragonScaleWard(plugin));
        plugin.getAbilityManager().registerAbility(new EndStoneWall(plugin));
        plugin.getAbilityManager().registerAbility(new ShulkerBarrage(plugin));
        plugin.getAbilityManager().registerAbility(new VoidMaw(plugin));
        plugin.getAbilityManager().registerAbility(new StarShardThrow(plugin));
        plugin.getAbilityManager().registerAbility(new ElytraBurst(plugin));
        plugin.getAbilityManager().registerAbility(new SkySunder(plugin));
        plugin.getAbilityManager().registerAbility(new PurpurBulwark(plugin));
        plugin.getAbilityManager().registerAbility(new EndShardRain(plugin));

        plugin.getServer().getPluginManager().registerEvents(new EndExpansionAbilities(), plugin);
    }

    static void activateTether(UUID playerUuid, UUID targetUuid, long expireAt, double share) {
        tetherActive.put(playerUuid, expireAt);
        tetherTarget.put(playerUuid, targetUuid);
        tetherShare.put(playerUuid, share);
    }

    static void activateScaleWard(UUID uuid, long expireAt, double pool) {
        scaleWardActive.put(uuid, expireAt);
        scaleWardPool.put(uuid, pool);
    }

    static void activateBulwark(UUID uuid, long expireAt) {
        bulwarkActive.put(uuid, expireAt);
    }

    @EventHandler
    public void onTetherDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity();
        Long expire = tetherActive.get(player.getUniqueId());
        if (expire == null) {
            return;
        }
        if (System.currentTimeMillis() >= expire) {
            clearTether(player.getUniqueId());
            return;
        }
        UUID targetId = tetherTarget.get(player.getUniqueId());
        if (targetId == null) {
            return;
        }
        Entity targetEntity = pluginInstance.getServer().getEntity(targetId);
        if (!(targetEntity instanceof LivingEntity) || !targetEntity.isValid()) {
            return;
        }
        double share = tetherShare.getOrDefault(player.getUniqueId(), 0.3);
        ((LivingEntity) targetEntity).damage(event.getDamage() * share, player);
    }

    @EventHandler
    public void onScaleWardDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity();
        Long expire = scaleWardActive.get(player.getUniqueId());
        if (expire == null) {
            return;
        }
        if (System.currentTimeMillis() >= expire) {
            scaleWardActive.remove(player.getUniqueId());
            scaleWardPool.remove(player.getUniqueId());
            return;
        }
        double pool = scaleWardPool.getOrDefault(player.getUniqueId(), 0.0);
        if (pool <= 0) {
            scaleWardActive.remove(player.getUniqueId());
            scaleWardPool.remove(player.getUniqueId());
            return;
        }
        double absorbed = Math.min(pool, event.getDamage());
        event.setDamage(event.getDamage() - absorbed);
        double remaining = pool - absorbed;
        if (remaining <= 0) {
            scaleWardActive.remove(player.getUniqueId());
            scaleWardPool.remove(player.getUniqueId());
        } else {
            scaleWardPool.put(player.getUniqueId(), remaining);
        }
    }

    @EventHandler
    public void onBulwarkDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity();
        Long expire = bulwarkActive.get(player.getUniqueId());
        if (expire == null) {
            return;
        }
        if (System.currentTimeMillis() >= expire) {
            bulwarkActive.remove(player.getUniqueId());
            return;
        }
        if (event.getDamager() instanceof Projectile) {
            event.setCancelled(true);
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_SHULKER_BOX_CLOSE, 1.0f, 1.2f);
        }
    }

    @EventHandler
    public void onBarrageHit(ProjectileHitEvent event) {
        if (!event.getEntity().hasMetadata("ender_barrage") || !(event.getHitEntity() instanceof LivingEntity)) {
            return;
        }
        LivingEntity target = (LivingEntity) event.getHitEntity();
        double damage = event.getEntity().hasMetadata("barrage_damage") ? event.getEntity().getMetadata("barrage_damage").get(0).asDouble() : 2.0;
        Object shooter = event.getEntity().getShooter();
        if (shooter instanceof Player) {
            target.damage(damage, (Player) shooter);
        } else {
            target.damage(damage);
        }
        target.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 40, 0));
        EffectUtils.fanfare(target.getLocation(), new EffectUtils.SoundLayer(Sound.ENTITY_ENDERMAN_TELEPORT, 0.7f, 1.5f));
        EffectUtils.burst(target.getLocation().add(0, 1, 0),
                new EffectUtils.Layer(Particle.PORTAL, 10, 0.2, 0.3, 0.2, 0.05),
                new EffectUtils.Layer(Particle.END_ROD, 4, 0.1, 0.2, 0.1, 0.02));
    }

    @EventHandler
    public void onStarShardHit(ProjectileHitEvent event) {
        if (!event.getEntity().hasMetadata("star_shard")) {
            return;
        }
        Location loc = event.getHitBlock() != null ? event.getHitBlock().getLocation().add(0.5, 1, 0.5) : event.getEntity().getLocation();
        double radius = event.getEntity().hasMetadata("shard_radius") ? event.getEntity().getMetadata("shard_radius").get(0).asDouble() : 3.0;
        double damage = event.getEntity().hasMetadata("shard_damage") ? event.getEntity().getMetadata("shard_damage").get(0).asDouble() : 6.0;
        loc.getWorld().playSound(loc, Sound.ENTITY_ENDER_DRAGON_HURT, 0.8f, 1.4f);
        loc.getWorld().spawnParticle(Particle.END_ROD, loc, 25, 0.4, 0.4, 0.4, 0.05);
        Object shooter = event.getEntity().getShooter();
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
            if (entity instanceof LivingEntity) {
                if (shooter instanceof Player && entity.equals(shooter)) {
                    continue;
                }
                if (shooter instanceof Player) {
                    ((LivingEntity) entity).damage(damage, (Player) shooter);
                } else {
                    ((LivingEntity) entity).damage(damage);
                }
            }
        }
    }

    private static void clearTether(UUID uuid) {
        tetherActive.remove(uuid);
        tetherTarget.remove(uuid);
        tetherShare.remove(uuid);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        clearTether(id);
        scaleWardActive.remove(id);
        scaleWardPool.remove(id);
        bulwarkActive.remove(id);
    }
}

// ==================== 20 NEW VOID CHORUS ABILITIES ====================

class ChorusHop extends Ability {
    private final ItemEditFull plugin;
    public ChorusHop(ItemEditFull plugin) {
        super("chorus_hop", "Chorus Hop", "Short randomised teleport, like chorus fruit, that confuses pursuers.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double range = getDoubleParam(plugin, item, "range", 6.0);
        Location origin = player.getLocation();
        for (int attempt = 0; attempt < 6; attempt++) {
            double dx = (Math.random() - 0.5) * 2 * range;
            double dz = (Math.random() - 0.5) * 2 * range;
            Location candidate = origin.clone().add(dx, 0, dz);
            if (candidate.getBlock().getType().isAir() && candidate.clone().add(0, 1, 0).getBlock().getType().isAir()) {
                origin.getWorld().spawnParticle(Particle.PORTAL, origin.add(0, 1, 0), 20, 0.3, 0.5, 0.3, 0.3);
                player.teleport(candidate.setDirection(player.getLocation().getDirection()));
                candidate.getWorld().spawnParticle(Particle.PORTAL, candidate.clone().add(0, 1, 0), 20, 0.3, 0.5, 0.3, 0.3);
                candidate.getWorld().playSound(candidate, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.2f);
                return true;
            }
        }
        player.sendMessage("§dThe chorus fruit found nowhere safe to send you.");
        return false;
    }
}

class VoidStep extends Ability {
    private final ItemEditFull plugin;
    public VoidStep(ItemEditFull plugin) {
        super("void_step", "Void Step", "Step briefly into the void, becoming untargetable and immune to damage.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 1.5);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.7f);
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, (int) (duration * 20) + 5, 0));
        player.setInvulnerable(true);
        player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation().add(0, 1, 0), 30, 0.4, 0.6, 0.4, 0.4);
        new CompatRunnable() {
            @Override
            public void run() {
                player.setInvulnerable(false);
                if (player.isOnline()) {
                    player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation().add(0, 1, 0), 20, 0.4, 0.6, 0.4, 0.3);
                }
            }
        }.runTaskLater(plugin, player, (long) (duration * 20));
        return true;
    }
}

class EnderBarrage extends Ability {
    private final ItemEditFull plugin;
    public EnderBarrage(ItemEditFull plugin) {
        super("ender_barrage", "Ender Barrage", "Hurls a rapid volley of ender pearls that damage and briefly disorient.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        int count = getIntParam(plugin, item, "count", 4);
        double damageEach = getDoubleParam(plugin, item, "damage_each", 2.0);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_SCREAM, 1.0f, 1.3f);
        for (int i = 0; i < count; i++) {
            new CompatRunnable() {
                @Override
                public void run() {
                    if (!player.isOnline()) {
                        return;
                    }
                    Snowball pearl = player.launchProjectile(Snowball.class);
                    pearl.setMetadata("ender_barrage", new FixedMetadataValue(plugin, true));
                    pearl.setMetadata("barrage_damage", new FixedMetadataValue(plugin, damageEach));
                }
            }.runTaskLater(plugin, player, i * 3L);
        }
        return true;
    }
}

class EndermiteSwarm extends Ability {
    private final ItemEditFull plugin;
    public EndermiteSwarm(ItemEditFull plugin) {
        super("endermite_swarm", "Endermite Swarm", "Summons a small swarm of endermites to harass your enemies.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        int count = getIntParam(plugin, item, "count", 3);
        double lifetime = getDoubleParam(plugin, item, "lifetime", 15.0);
        Location loc = player.getLocation();
        loc.getWorld().playSound(loc, Sound.ENTITY_ENDERMITE_AMBIENT, 1.0f, 1.0f);
        for (int i = 0; i < count; i++) {
            double angle = i * 2 * Math.PI / count;
            Location spawnLoc = loc.clone().add(Math.cos(angle), 0, Math.sin(angle));
            Endermite mite = (Endermite) spawnLoc.getWorld().spawnEntity(spawnLoc, EntityType.ENDERMITE);
            mite.setMetadata("helper", new FixedMetadataValue(plugin, player.getUniqueId().toString()));
            for (Entity entity : mite.getNearbyEntities(10, 5, 10)) {
                if (entity instanceof LivingEntity && !(entity instanceof Player) && !entity.hasMetadata("helper")) {
                    mite.setTarget((LivingEntity) entity);
                    break;
                }
            }
            new CompatRunnable() {
                @Override
                public void run() {
                    if (mite.isValid()) {
                        mite.getWorld().spawnParticle(Particle.PORTAL, mite.getLocation().add(0, 0.5, 0), 10, 0.2, 0.2, 0.2, 0.1);
                        mite.remove();
                    }
                }
            }.runTaskLater(plugin, player, (long) (lifetime * 20));
        }
        return true;
    }
}

class VoidTether extends Ability {
    private final ItemEditFull plugin;
    public VoidTether(ItemEditFull plugin) {
        super("void_tether", "Void Tether", "Tethers you to a target; damage you take is partially shared with them.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 6.0);
        double share = getDoubleParam(plugin, item, "share", 0.3);
        double range = getDoubleParam(plugin, item, "range", 10.0);
        Entity target = player.getTargetEntity((int) range);
        if (!(target instanceof LivingEntity)) {
            player.sendMessage("§cNo target in range.");
            return false;
        }
        EndExpansionAbilities.activateTether(player.getUniqueId(), target.getUniqueId(), System.currentTimeMillis() + (long) (duration * 1000), share);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.6f, 1.3f);
        player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation().add(0, 1, 0), 15, 0.3, 0.5, 0.3, 0.1);
        player.sendMessage("§5You are tethered to your target's fate!");
        return true;
    }
}

class EndCrystalBurst extends Ability {
    private final ItemEditFull plugin;
    public EndCrystalBurst(ItemEditFull plugin) {
        super("end_crystal_burst", "End Crystal Burst", "Summons and detonates an end crystal at a target location.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double radius = getDoubleParam(plugin, item, "radius", 4.0);
        double damage = getDoubleParam(plugin, item, "damage", 9.0);
        Block target = player.getTargetBlockExact(12);
        Location loc = target != null ? target.getLocation().add(0.5, 1.0, 0.5) : player.getLocation();
        loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.3f, 1.0f);
        loc.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, loc, 3, 0.3, 0.3, 0.3, 0);
        loc.getWorld().spawnParticle(Particle.END_ROD, loc, 30, radius * 0.4, 0.5, radius * 0.4, 0.05);
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
            if (entity instanceof LivingEntity && !entity.equals(player)) {
                ((LivingEntity) entity).damage(damage, player);
            }
        }
        return true;
    }
}

class ObsidianPillar extends Ability {
    private final ItemEditFull plugin;
    public ObsidianPillar(ItemEditFull plugin) {
        super("obsidian_pillar", "Obsidian Pillar", "Raises a pillar of obsidian beneath a target, launching them upward.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double damage = getDoubleParam(plugin, item, "damage", 3.0);
        Entity target = player.getTargetEntity(10);
        if (!(target instanceof LivingEntity)) {
            player.sendMessage("§cNo target in sight.");
            return false;
        }
        LivingEntity living = (LivingEntity) target;
        living.damage(damage, player);
        Vector v = living.getVelocity();
        living.setVelocity(new Vector(v.getX(), 1.3, v.getZ()));
        living.getWorld().playSound(living.getLocation(), Sound.BLOCK_STONE_BREAK, 1.0f, 0.6f);
        living.getWorld().spawnParticle(Particle.BLOCK_CRACK, living.getLocation(), 20, 0.3, 0.5, 0.3, 0, Material.OBSIDIAN.createBlockData());
        return true;
    }
}

class GatewayPull extends Ability {
    private final ItemEditFull plugin;
    public GatewayPull(ItemEditFull plugin) {
        super("gateway_pull", "Gateway Pull", "Opens a brief rift that pulls nearby enemies toward its centre.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double radius = getDoubleParam(plugin, item, "radius", 6.0);
        Location loc = player.getLocation();
        loc.getWorld().playSound(loc, Sound.BLOCK_END_GATEWAY_SPAWN, 1.0f, 1.0f);
        loc.getWorld().spawnParticle(Particle.PORTAL, loc.add(0, 1, 0), 50, radius * 0.3, 0.5, radius * 0.3, 0.5);
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
            if (entity instanceof LivingEntity && !entity.equals(player)) {
                LivingEntity living = (LivingEntity) entity;
                Vector pull = VectorUtils.safeNormalize(loc.toVector().subtract(living.getLocation().toVector())).multiply(1.1).setY(0.2);
                living.setVelocity(pull);
            }
        }
        return true;
    }
}

class EnderCamouflage extends Ability {
    private final ItemEditFull plugin;
    public EnderCamouflage(ItemEditFull plugin) {
        super("ender_camouflage", "Ender Camouflage", "Grants brief invisibility when standing still near end-stone or purpur blocks.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 6.0);
        boolean nearEndBlocks = false;
        Location loc = player.getLocation();
        for (int x = -2; x <= 2 && !nearEndBlocks; x++) {
            for (int y = -1; y <= 1 && !nearEndBlocks; y++) {
                for (int z = -2; z <= 2 && !nearEndBlocks; z++) {
                    Material type = loc.clone().add(x, y, z).getBlock().getType();
                    if (type == Material.END_STONE || type == Material.PURPUR_BLOCK || type == Material.PURPUR_PILLAR) {
                        nearEndBlocks = true;
                    }
                }
            }
        }
        if (!nearEndBlocks) {
            player.sendMessage("§cYou must be near end-stone or purpur to blend in.");
            return false;
        }
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, (int) (duration * 20), 0));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.6f, 1.5f);
        return true;
    }
}

class VoidGaze extends Ability {
    private final ItemEditFull plugin;
    public VoidGaze(ItemEditFull plugin) {
        super("void_gaze", "Void Gaze", "Lock eyes with a target, blinding and slowing them.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double range = getDoubleParam(plugin, item, "range", 12.0);
        double blindDuration = getDoubleParam(plugin, item, "blind_duration", 3.0);
        Entity target = player.getTargetEntity((int) range);
        if (!(target instanceof LivingEntity)) {
            player.sendMessage("§cNo target in sight.");
            return false;
        }
        LivingEntity living = (LivingEntity) target;
        living.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, (int) (blindDuration * 20), 0));
        living.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) (blindDuration * 20), 1));
        living.getWorld().playSound(living.getLocation(), Sound.ENTITY_ENDERMAN_STARE, 1.0f, 1.0f);
        return true;
    }
}

class ChorusRegen extends Ability {
    private final ItemEditFull plugin;
    public ChorusRegen(ItemEditFull plugin) {
        super("chorus_regen", "Chorus Regeneration", "Consumes chorus energy to heal over a short duration.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 6.0);
        double healPerTick = getDoubleParam(plugin, item, "heal_tick", 1.0);
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_CHORUS_FRUIT_TELEPORT, 0.7f, 1.4f);
        new CompatRunnable() {
            int ticksElapsed = 0;
            @Override
            public void run() {
                if (!player.isOnline() || ticksElapsed >= (duration * 20)) {
                    cancel();
                    return;
                }
                double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                player.setHealth(Math.min(maxHealth, player.getHealth() + healPerTick));
                player.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0, 1.5, 0), 1, 0.2, 0.2, 0.2, 0);
                ticksElapsed += 20;
            }
        }.runTaskTimer(plugin, player, 20L, 20L);
        return true;
    }
}

class DragonScaleWard extends Ability {
    private final ItemEditFull plugin;
    public DragonScaleWard(ItemEditFull plugin) {
        super("dragon_scale_ward", "Dragon Scale Ward", "Grants a shield of dragon scale that absorbs a burst of damage.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double absorb = getDoubleParam(plugin, item, "absorb", 8.0);
        double duration = getDoubleParam(plugin, item, "duration", 8.0);
        EndExpansionAbilities.activateScaleWard(player.getUniqueId(), System.currentTimeMillis() + (long) (duration * 1000), absorb);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_HURT, 0.8f, 0.7f);
        player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation().add(0, 1, 0), 25, 0.4, 0.6, 0.4, 0.02);
        player.sendMessage("§dDragon scales shimmer around you!");
        return true;
    }
}

class EndStoneWall extends Ability {
    private final ItemEditFull plugin;
    public EndStoneWall(ItemEditFull plugin) {
        super("end_stone_wall", "End Stone Wall", "Raises a temporary wall of end stone to block a path or projectile.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 10.0);
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
                    block.setType(Material.END_STONE);
                    wallBlocks.add(block);
                }
            }
        }
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_END_PORTAL_FRAME_FILL, 1.0f, 1.0f);
        new CompatRunnable() {
            @Override
            public void run() {
                for (Block b : wallBlocks) {
                    if (b.getType() == Material.END_STONE) {
                        b.setType(Material.AIR);
                    }
                }
            }
        }.runTaskLater(plugin, base, (long) (duration * 20));
        return true;
    }
}

class ShulkerBarrage extends Ability {
    private final ItemEditFull plugin;
    public ShulkerBarrage(ItemEditFull plugin) {
        super("shulker_barrage", "Shulker Barrage", "Fires a volley of homing shulker bullets at nearby enemies.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        int count = getIntParam(plugin, item, "count", 3);
        double range = getDoubleParam(plugin, item, "range", 20.0);
        List<LivingEntity> targets = new ArrayList<>();
        for (Entity entity : player.getNearbyEntities(range, range, range)) {
            if (entity instanceof LivingEntity && !entity.equals(player)) {
                targets.add((LivingEntity) entity);
                if (targets.size() >= count) {
                    break;
                }
            }
        }
        if (targets.isEmpty()) {
            player.sendMessage("§cNo targets nearby to lock onto.");
            return false;
        }
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_SHULKER_SHOOT, 1.0f, 1.0f);
        for (LivingEntity target : targets) {
            ShulkerBullet bullet = player.getWorld().spawn(player.getEyeLocation(), ShulkerBullet.class);
            bullet.setShooter(player);
            bullet.setTarget(target);
        }
        return true;
    }
}

class VoidMaw extends Ability {
    private final ItemEditFull plugin;
    public VoidMaw(ItemEditFull plugin) {
        super("void_maw", "Void Maw", "Opens a maw of void beneath a target, pulling them down and dealing damage.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double radius = getDoubleParam(plugin, item, "radius", 3.0);
        double damage = getDoubleParam(plugin, item, "damage", 6.0);
        Block target = player.getTargetBlockExact(10);
        Location loc = target != null ? target.getLocation().add(0.5, 1.0, 0.5) : player.getLocation();
        loc.getWorld().playSound(loc, Sound.ENTITY_ENDERMAN_SCREAM, 1.0f, 0.6f);
        loc.getWorld().spawnParticle(Particle.SQUID_INK, loc, 30, radius * 0.3, 0.3, radius * 0.3, 0.05);
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
            if (entity instanceof LivingEntity && !entity.equals(player)) {
                LivingEntity living = (LivingEntity) entity;
                living.damage(damage, player);
                living.setVelocity(new Vector(living.getVelocity().getX(), -0.8, living.getVelocity().getZ()));
            }
        }
        return true;
    }
}

class StarShardThrow extends Ability {
    private final ItemEditFull plugin;
    public StarShardThrow(ItemEditFull plugin) {
        super("star_shard_throw", "Star Shard Throw", "Throws a shard of end-crystal light that explodes on impact.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double radius = getDoubleParam(plugin, item, "radius", 3.0);
        double damage = getDoubleParam(plugin, item, "damage", 6.0);
        Snowball shard = player.launchProjectile(Snowball.class);
        shard.setMetadata("star_shard", new FixedMetadataValue(plugin, true));
        shard.setMetadata("shard_radius", new FixedMetadataValue(plugin, radius));
        shard.setMetadata("shard_damage", new FixedMetadataValue(plugin, damage));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 0.6f, 1.6f);
        return true;
    }
}

class ElytraBurst extends Ability {
    private final ItemEditFull plugin;
    public ElytraBurst(ItemEditFull plugin) {
        super("elytra_burst", "Elytra Burst", "Grants a short burst of glide-assisted speed and fall negation.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 4.0);
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_ELYTRA_FLYING, 1.0f, 1.2f);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, (int) (duration * 20) + 40, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, (int) (duration * 20), 2));
        Vector forward = player.getLocation().getDirection().normalize().multiply(1.0).setY(0.4);
        player.setVelocity(forward);
        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 15, 0.3, 0.2, 0.3, 0.02);
        return true;
    }
}

class SkySunder extends Ability {
    private final ItemEditFull plugin;
    public SkySunder(ItemEditFull plugin) {
        super("sky_sunder", "Sky Sunder", "A thunderous roar that knocks back and damages all nearby enemies.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double radius = getDoubleParam(plugin, item, "radius", 6.0);
        double damage = getDoubleParam(plugin, item, "damage", 5.0);
        Location loc = player.getLocation();
        loc.getWorld().playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.5f, 0.8f);
        loc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, loc.add(0, 1, 0), 3, 0.3, 0.3, 0.3, 0);
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
            if (entity instanceof LivingEntity && !entity.equals(player)) {
                LivingEntity living = (LivingEntity) entity;
                living.damage(damage, player);
                Vector away = VectorUtils.safeNormalize(living.getLocation().toVector().subtract(loc.toVector())).multiply(1.5).setY(0.4);
                living.setVelocity(away);
            }
        }
        return true;
    }
}

class PurpurBulwark extends Ability {
    private final ItemEditFull plugin;
    public PurpurBulwark(ItemEditFull plugin) {
        super("purpur_bulwark", "Purpur Bulwark", "Erects a rotating barrier of purpur fragments that blocks incoming projectiles.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 8.0);
        EndExpansionAbilities.activateBulwark(player.getUniqueId(), System.currentTimeMillis() + (long) (duration * 1000));
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_SHULKER_BOX_OPEN, 1.0f, 0.9f);
        new CompatRunnable() {
            int ticksElapsed = 0;
            @Override
            public void run() {
                if (!player.isOnline() || ticksElapsed >= (duration * 20)) {
                    cancel();
                    return;
                }
                double angle = ticksElapsed * 0.3;
                Location pLoc = player.getLocation().add(0, 1, 0);
                for (int i = 0; i < 3; i++) {
                    double a = angle + (i * 2 * Math.PI / 3);
                    Location particleLoc = pLoc.clone().add(Math.cos(a) * 1.0, 0, Math.sin(a) * 1.0);
                    particleLoc.getWorld().spawnParticle(Particle.PORTAL, particleLoc, 1, 0, 0, 0, 0);
                }
                ticksElapsed += 2;
            }
        }.runTaskTimer(plugin, player, 0L, 2L);
        return true;
    }
}

class EndShardRain extends Ability {
    private final ItemEditFull plugin;
    public EndShardRain(ItemEditFull plugin) {
        super("end_shard_rain", "End Shard Rain", "Calls down a rain of crystalline shards over an area.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double radius = getDoubleParam(plugin, item, "radius", 5.0);
        double duration = getDoubleParam(plugin, item, "duration", 4.0);
        Location center = player.getLocation();
        center.getWorld().playSound(center, Sound.ENTITY_ENDER_DRAGON_SHOOT, 1.0f, 1.2f);
        new CompatRunnable() {
            int ticksElapsed = 0;
            @Override
            public void run() {
                if (ticksElapsed >= (duration * 20) || !player.isOnline()) {
                    cancel();
                    return;
                }
                for (int i = 0; i < 8; i++) {
                    double angle = Math.random() * 2 * Math.PI;
                    double r = Math.random() * radius;
                    Location pLoc = center.clone().add(Math.cos(angle) * r, 2.0 + Math.random() * 2.0, Math.sin(angle) * r);
                    pLoc.getWorld().spawnParticle(Particle.END_ROD, pLoc, 1, 0, 0, 0, 0);
                }
                if (ticksElapsed % 20 == 0) {
                    for (Entity entity : center.getWorld().getNearbyEntities(center, radius, 2.0, radius)) {
                        if (entity instanceof LivingEntity && !entity.equals(player)) {
                            ((LivingEntity) entity).damage(2.0, player);
                        }
                    }
                }
                ticksElapsed += 2;
            }
        }.runTaskTimer(plugin, player, 0L, 2L);
        return true;
    }
}
