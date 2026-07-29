package com.itemedit.full.ability.general;

import com.itemedit.full.ItemEditFull;
import com.itemedit.full.ability.Ability;
import com.itemedit.full.utils.CompatRunnable;
import com.itemedit.full.utils.EffectUtils;
import com.itemedit.full.utils.VectorUtils;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Biome;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Silverfish;
import org.bukkit.entity.SkeletonHorse;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Feral Mimicry expansion: 20 additional borrowed-instinct abilities layered
 * on top of the original 20 mob abilities.
 */
public class MobExpansionAbilities implements Listener {

    // ---- tracks the last time each player took damage, for Zombified Piglin Wrath ----
    private static final Map<UUID, Long> lastDamagedTime = new ConcurrentHashMap<>();

    // ---- tracks each player's most recently launched projectile, for Allay's Return ----
    private static final Map<UUID, Entity> lastProjectile = new ConcurrentHashMap<>();

    // ---- shared dodge-chance state for Magma Cube Split / Vex Flicker ----
    private static final Map<UUID, Long> dodgeActive = new ConcurrentHashMap<>();
    private static final Map<UUID, Double> dodgeChance = new ConcurrentHashMap<>();

    private static ItemEditFull pluginInstance;

    public static void register(ItemEditFull plugin) {
        pluginInstance = plugin;

        plugin.getAbilityManager().registerAbility(new EndermanTeleportStrike(plugin));
        plugin.getAbilityManager().registerAbility(new EndermanBlockSnatch(plugin));
        plugin.getAbilityManager().registerAbility(new EndermanStare(plugin));
        plugin.getAbilityManager().registerAbility(new GuardianSpikeBarrage(plugin));
        plugin.getAbilityManager().registerAbility(new GuardianDeathstare(plugin));
        plugin.getAbilityManager().registerAbility(new SilverfishBurrow(plugin));
        plugin.getAbilityManager().registerAbility(new SilverfishInfest(plugin));
        plugin.getAbilityManager().registerAbility(new MagmaCubeSplit(plugin));
        plugin.getAbilityManager().registerAbility(new HuskAridFury(plugin));
        plugin.getAbilityManager().registerAbility(new ZombifiedPiglinWrath(plugin));
        plugin.getAbilityManager().registerAbility(new BlazeWildfireRush(plugin));
        plugin.getAbilityManager().registerAbility(new CaveSpiderWebSnare(plugin));
        plugin.getAbilityManager().registerAbility(new SkeletonHorseSummon(plugin));
        plugin.getAbilityManager().registerAbility(new ZoglinRampage(plugin));
        plugin.getAbilityManager().registerAbility(new StriderLavaDash(plugin));
        plugin.getAbilityManager().registerAbility(new AllaysReturn(plugin));
        plugin.getAbilityManager().registerAbility(new PhantomDreadDive(plugin));
        plugin.getAbilityManager().registerAbility(new IronGolemSlam(plugin));
        plugin.getAbilityManager().registerAbility(new DrownedUndertowSnare(plugin));
        plugin.getAbilityManager().registerAbility(new VexFlicker(plugin));

        plugin.getServer().getPluginManager().registerEvents(new MobExpansionAbilities(), plugin);
    }

    static boolean wasRecentlyDamaged(UUID uuid, double withinSeconds) {
        Long time = lastDamagedTime.get(uuid);
        return time != null && (System.currentTimeMillis() - time) <= withinSeconds * 1000;
    }

    static Entity getLastProjectile(UUID uuid) {
        return lastProjectile.get(uuid);
    }

    static void activateDodge(UUID uuid, long expireAt, double chance) {
        dodgeActive.put(uuid, expireAt);
        dodgeChance.put(uuid, chance);
    }

    @EventHandler
    public void onPlayerDamaged(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            lastDamagedTime.put(event.getEntity().getUniqueId(), System.currentTimeMillis());
        }
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (event.getEntity().getShooter() instanceof Player) {
            Player shooter = (Player) event.getEntity().getShooter();
            lastProjectile.put(shooter.getUniqueId(), event.getEntity());
        }
    }

    @EventHandler
    public void onDodgeDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity();
        Long expire = dodgeActive.get(player.getUniqueId());
        if (expire == null) {
            return;
        }
        if (System.currentTimeMillis() >= expire) {
            dodgeActive.remove(player.getUniqueId());
            dodgeChance.remove(player.getUniqueId());
            return;
        }
        double chance = dodgeChance.getOrDefault(player.getUniqueId(), 0.4);
        if (Math.random() < chance) {
            event.setCancelled(true);
            EffectUtils.burst(player.getLocation().add(0, 1, 0),
                    new EffectUtils.Layer(Particle.REVERSE_PORTAL, 12, 0.3, 0.4, 0.3, 0.3),
                    new EffectUtils.Layer(Particle.SMOKE_NORMAL, 5, 0.2, 0.3, 0.2, 0.02));
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.7f, 1.6f);
        }
    }

    @EventHandler
    public void onWebSnareHit(ProjectileHitEvent event) {
        if (!event.getEntity().hasMetadata("cave_spider_web") || !(event.getHitEntity() instanceof LivingEntity)) {
            return;
        }
        LivingEntity target = (LivingEntity) event.getHitEntity();
        target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 60, 1));
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, 2));
        EffectUtils.burst(target.getLocation().add(0, 1, 0),
                new EffectUtils.Layer(Particle.SPELL_MOB, 10, 0.3, 0.3, 0.3, 0.02),
                new EffectUtils.Layer(Particle.CRIT, 6, 0.3, 0.3, 0.3, 0.02));
        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_SPIDER_HURT, 1.0f, 1.2f);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        lastDamagedTime.remove(id);
        lastProjectile.remove(id);
        dodgeActive.remove(id);
        dodgeChance.remove(id);
    }
}

// ==================== 20 NEW FERAL MIMICRY ABILITIES ====================

class EndermanTeleportStrike extends Ability {
    private final ItemEditFull plugin;
    public EndermanTeleportStrike(ItemEditFull plugin) {
        super("enderman_teleport_strike", "Enderman Teleport Strike", "Teleport behind your target and strike immediately.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double range = getDoubleParam(plugin, item, "range", 8.0);
        double damage = getDoubleParam(plugin, item, "damage", 6.0);
        Entity target = player.getTargetEntity((int) range);
        if (!(target instanceof LivingEntity)) {
            player.sendMessage("§cNo target in range.");
            return false;
        }
        LivingEntity living = (LivingEntity) target;
        Vector behind = VectorUtils.safeNormalize(living.getLocation().toVector().subtract(player.getLocation().toVector())).multiply(-1.5);
        Location dest = living.getLocation().clone().add(behind).setDirection(living.getLocation().toVector().subtract(player.getLocation().toVector()).multiply(-1));
        EffectUtils.burst(player.getLocation().add(0, 1, 0),
                new EffectUtils.Layer(Particle.PORTAL, 15, 0.3, 0.5, 0.3, 0.2),
                new EffectUtils.Layer(Particle.REVERSE_PORTAL, 8, 0.3, 0.5, 0.3, 0.1));
        player.teleport(dest);
        living.damage(damage, player);
        dest.getWorld().playSound(dest, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        EffectUtils.burst(dest.clone().add(0, 1, 0),
                new EffectUtils.Layer(Particle.PORTAL, 20, 0.3, 0.5, 0.3, 0.25),
                new EffectUtils.Layer(Particle.CRIT, 6, 0.3, 0.3, 0.3, 0.05));
        return true;
    }
}

class EndermanBlockSnatch extends Ability {
    private final ItemEditFull plugin;
    public EndermanBlockSnatch(ItemEditFull plugin) {
        super("enderman_block_snatch", "Enderman Block Snatch", "Rip a chunk of terrain from the ground and hurl it at an enemy.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double range = getDoubleParam(plugin, item, "range", 10.0);
        double damage = getDoubleParam(plugin, item, "damage", 5.0);
        Entity target = player.getTargetEntity((int) range);
        if (!(target instanceof LivingEntity)) {
            player.sendMessage("§cNo target in range.");
            return false;
        }
        LivingEntity living = (LivingEntity) target;
        living.damage(damage, player);
        living.getWorld().spawnParticle(org.bukkit.Particle.BLOCK_CRACK, living.getLocation().add(0, 1, 0), 25, 0.3, 0.4, 0.3, 0,
                player.getWorld().getBlockAt(player.getLocation().add(0, -1, 0)).getBlockData());
        EffectUtils.burst(living.getLocation().add(0, 1, 0),
                new EffectUtils.Layer(Particle.BLOCK_DUST, 15, 0.3, 0.4, 0.3, 0.02),
                new EffectUtils.Layer(Particle.CRIT, 5, 0.3, 0.3, 0.3, 0.03));
        living.getWorld().playSound(living.getLocation(), Sound.ENTITY_ENDERMAN_STARE, 1.0f, 0.8f);
        return true;
    }
}

class EndermanStare extends Ability {
    private final ItemEditFull plugin;
    public EndermanStare(ItemEditFull plugin) {
        super("enderman_stare", "Enderman Stare", "A fixed stare that provokes and slows anyone who looks back.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double range = getDoubleParam(plugin, item, "range", 10.0);
        double duration = getDoubleParam(plugin, item, "duration", 4.0);
        Entity target = player.getTargetEntity((int) range);
        if (!(target instanceof LivingEntity)) {
            player.sendMessage("§cNo target in sight.");
            return false;
        }
        LivingEntity living = (LivingEntity) target;
        living.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) (duration * 20), 1));
        EffectUtils.burst(living.getEyeLocation(),
                new EffectUtils.Layer(Particle.PORTAL, 6, 0.15, 0.1, 0.15, 0.05),
                new EffectUtils.Layer(Particle.REVERSE_PORTAL, 4, 0.15, 0.1, 0.15, 0.05));
        living.getWorld().playSound(living.getLocation(), Sound.ENTITY_ENDERMAN_STARE, 1.2f, 0.9f);
        return true;
    }
}

class GuardianSpikeBarrage extends Ability {
    private final ItemEditFull plugin;
    public GuardianSpikeBarrage(ItemEditFull plugin) {
        super("guardian_spike_barrage", "Guardian Spike Barrage", "Fires a volley of guardian spines outward.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double radius = getDoubleParam(plugin, item, "radius", 5.0);
        double damage = getDoubleParam(plugin, item, "damage", 5.0);
        Location loc = player.getLocation();
        loc.getWorld().playSound(loc, Sound.ENTITY_GUARDIAN_ATTACK, 1.2f, 1.0f);
        Location center = loc.add(0, 1, 0);
        EffectUtils.burst(center,
                new EffectUtils.Layer(Particle.CRIT_MAGIC, 30, radius * 0.4, 0.4, radius * 0.4, 0.1),
                new EffectUtils.Layer(Particle.WATER_BUBBLE, 20, radius * 0.4, 0.4, radius * 0.4, 0.05));
        EffectUtils.ring(center, radius, 16, new EffectUtils.Layer(Particle.DRIP_WATER, 2, 0.05, 0.1, 0.05, 0.01));
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
            if (entity instanceof LivingEntity && !entity.equals(player)) {
                ((LivingEntity) entity).damage(damage, player);
            }
        }
        return true;
    }
}

class GuardianDeathstare extends Ability {
    private final ItemEditFull plugin;
    public GuardianDeathstare(ItemEditFull plugin) {
        super("guardian_deathstare", "Guardian Deathstare", "Charges then fires a piercing laser beam at a locked target.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double range = getDoubleParam(plugin, item, "range", 15.0);
        double damage = getDoubleParam(plugin, item, "damage", 9.0);
        double chargeSeconds = getDoubleParam(plugin, item, "charge", 1.5);
        Entity target = player.getTargetEntity((int) range);
        if (!(target instanceof LivingEntity)) {
            player.sendMessage("§cNo target in range.");
            return false;
        }
        LivingEntity living = (LivingEntity) target;
        UUID targetId = living.getUniqueId();
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GUARDIAN_ATTACK, 0.8f, 0.6f);
        EffectUtils.spiral(plugin, player, player.getEyeLocation(), 0.4, (int) (chargeSeconds * 10), Math.PI / 3, 0.03, 2L,
                new EffectUtils.Layer(Particle.WATER_BUBBLE, 3, 0.05, 0.05, 0.05, 0.01));
        new CompatRunnable() {
            @Override
            public void run() {
                Entity refreshed = plugin.getServer().getEntity(targetId);
                if (refreshed instanceof LivingEntity && refreshed.isValid()) {
                    ((LivingEntity) refreshed).damage(damage, player);
                    refreshed.getWorld().playSound(refreshed.getLocation(), Sound.ENTITY_GUARDIAN_ATTACK, 1.4f, 1.2f);
                    EffectUtils.burst(refreshed.getLocation().add(0, 1, 0),
                            new EffectUtils.Layer(Particle.CRIT_MAGIC, 20, 0.3, 0.4, 0.3, 0.1),
                            new EffectUtils.Layer(Particle.WATER_BUBBLE, 15, 0.3, 0.4, 0.3, 0.05));
                }
            }
        }.runTaskLater(plugin, player, (long) (chargeSeconds * 20));
        return true;
    }
}

class SilverfishBurrow extends Ability {
    private final ItemEditFull plugin;
    public SilverfishBurrow(ItemEditFull plugin) {
        super("silverfish_burrow", "Silverfish Burrow", "Burrow briefly into a nearby block, becoming untargetable.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 2.0);
        player.setInvulnerable(true);
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, (int) (duration * 20) + 5, 0));
        EffectUtils.burst(player.getLocation(),
                new EffectUtils.Layer(Particle.BLOCK_DUST, 20, 0.3, 0.2, 0.3, 0.02,
                        player.getWorld().getBlockAt(player.getLocation().add(0, -1, 0)).getBlockData()),
                new EffectUtils.Layer(Particle.SMOKE_NORMAL, 8, 0.3, 0.2, 0.3, 0.01));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_SILVERFISH_AMBIENT, 1.0f, 1.4f);
        new CompatRunnable() {
            @Override
            public void run() {
                player.setInvulnerable(false);
                EffectUtils.burst(player.getLocation(),
                        new EffectUtils.Layer(Particle.BLOCK_DUST, 15, 0.3, 0.2, 0.3, 0.02,
                                player.getWorld().getBlockAt(player.getLocation().add(0, -1, 0)).getBlockData()));
            }
        }.runTaskLater(plugin, player, (long) (duration * 20));
        return true;
    }
}

class SilverfishInfest extends Ability {
    private final ItemEditFull plugin;
    public SilverfishInfest(ItemEditFull plugin) {
        super("silverfish_infest", "Silverfish Infestation", "Infests the target, spawning a silverfish that harasses them.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double range = getDoubleParam(plugin, item, "range", 8.0);
        double lifetime = getDoubleParam(plugin, item, "lifetime", 12.0);
        Entity target = player.getTargetEntity((int) range);
        if (!(target instanceof LivingEntity)) {
            player.sendMessage("§cNo target in range.");
            return false;
        }
        LivingEntity livingTarget = (LivingEntity) target;
        Silverfish fish = (Silverfish) livingTarget.getWorld().spawnEntity(livingTarget.getLocation(), EntityType.SILVERFISH);
        fish.setMetadata("helper", new FixedMetadataValue(plugin, player.getUniqueId().toString()));
        fish.setTarget(livingTarget);
        EffectUtils.ring(livingTarget.getLocation().add(0, 0.1, 0), 0.6, 8,
                new EffectUtils.Layer(Particle.BLOCK_DUST, 2, 0.05, 0.1, 0.05, 0.01,
                        livingTarget.getWorld().getBlockAt(livingTarget.getLocation().add(0, -1, 0)).getBlockData()));
        fish.getWorld().playSound(fish.getLocation(), Sound.ENTITY_SILVERFISH_AMBIENT, 1.0f, 1.0f);
        new CompatRunnable() {
            @Override
            public void run() {
                if (fish.isValid()) {
                    fish.remove();
                }
            }
        }.runTaskLater(plugin, player, (long) (lifetime * 20));
        return true;
    }
}

class MagmaCubeSplit extends Ability {
    private final ItemEditFull plugin;
    public MagmaCubeSplit(ItemEditFull plugin) {
        super("magma_cube_split", "Magma Cube Split", "Fragments you into flickering afterimages, granting a chance to avoid hits.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 5.0);
        double dodgeChance = getDoubleParam(plugin, item, "dodge_chance", 0.35);
        MobExpansionAbilities.activateDodge(player.getUniqueId(), System.currentTimeMillis() + (long) (duration * 1000), dodgeChance);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_MAGMA_CUBE_SQUISH, 1.0f, 1.3f);
        EffectUtils.burst(player.getLocation().add(0, 1, 0),
                new EffectUtils.Layer(Particle.LAVA, 10, 0.3, 0.4, 0.3, 0),
                new EffectUtils.Layer(Particle.SLIME, 12, 0.3, 0.4, 0.3, 0.02));
        EffectUtils.ring(player.getLocation(), 0.7, 6, new EffectUtils.Layer(Particle.SLIME, 3, 0.05, 0.1, 0.05, 0.01));
        return true;
    }
}

class HuskAridFury extends Ability {
    private final ItemEditFull plugin;
    public HuskAridFury(ItemEditFull plugin) {
        super("husk_arid_fury", "Husk's Arid Fury", "Grants bonus damage and hunger-drain immunity while in hot biomes.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 10.0);
        double damageBonus = getDoubleParam(plugin, item, "damage_bonus", 0.15);
        Biome biome = player.getLocation().getBlock().getBiome();
        boolean hot = biome.name().contains("DESERT") || biome.name().contains("BADLANDS") || biome.name().contains("SAVANNA") || biome.name().contains("NETHER");
        int amplifier = hot ? 1 : 0;
        player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, (int) (duration * 20), amplifier));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, (int) (duration * 20), 0));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_HUSK_AMBIENT, 1.0f, 0.9f);
        player.sendMessage(hot ? "§eThe arid heat fuels your fury!" : "§7You feel only a faint echo of husk-fury here.");
        return true;
    }
}

class ZombifiedPiglinWrath extends Ability {
    private final ItemEditFull plugin;
    public ZombifiedPiglinWrath(ItemEditFull plugin) {
        super("zombified_piglin_wrath", "Zombified Piglin Wrath", "Enrages you, boosting attack speed if you've recently been struck.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 5.0);
        double recentWindow = getDoubleParam(plugin, item, "recent_window", 6.0);
        boolean recentlyHit = MobExpansionAbilities.wasRecentlyDamaged(player.getUniqueId(), recentWindow);
        int amplifier = recentlyHit ? 2 : 0;
        player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, (int) (duration * 20), amplifier));
        player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, (int) (duration * 20), recentlyHit ? 1 : 0));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ZOMBIFIED_PIGLIN_ANGRY, 1.2f, 0.8f);
        return true;
    }
}

class BlazeWildfireRush extends Ability {
    private final ItemEditFull plugin;
    public BlazeWildfireRush(ItemEditFull plugin) {
        super("blaze_wildfire_rush", "Blaze Wildfire Rush", "Dash forward trailing fire, igniting enemies in your path.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double range = getDoubleParam(plugin, item, "range", 6.0);
        int fireTicks = getIntParam(plugin, item, "fire_ticks", 60);
        Location start = player.getLocation();
        Vector dir = VectorUtils.safeNormalize(start.getDirection().setY(0));
        if (dir.lengthSquared() < 0.01) {
            dir = new Vector(0, 0, 1);
        }
        player.getWorld().playSound(start, Sound.ENTITY_BLAZE_BURN, 1.0f, 1.0f);
        for (int i = 1; i <= (int) range; i++) {
            Location step = start.clone().add(dir.clone().multiply(i)).add(0, 1, 0);
            step.getWorld().spawnParticle(org.bukkit.Particle.FLAME, step, 5, 0.2, 0.2, 0.2, 0.02);
            for (Entity entity : step.getWorld().getNearbyEntities(step, 1.2, 1.2, 1.2)) {
                if (entity instanceof LivingEntity && !entity.equals(player)) {
                    entity.setFireTicks(fireTicks);
                }
            }
        }
        player.setVelocity(dir.multiply(1.2).setY(Math.max(player.getVelocity().getY(), 0.1)));
        return true;
    }
}

class CaveSpiderWebSnare extends Ability {
    private final ItemEditFull plugin;
    public CaveSpiderWebSnare(ItemEditFull plugin) {
        super("cave_spider_web_snare", "Cave Spider Web Snare", "Fires a sticky web that slows and poisons on contact.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        Snowball web = player.launchProjectile(Snowball.class);
        web.setMetadata("cave_spider_web", new FixedMetadataValue(plugin, true));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_SPIDER_AMBIENT, 1.0f, 1.3f);
        return true;
    }
}

class SkeletonHorseSummon extends Ability {
    private final ItemEditFull plugin;
    public SkeletonHorseSummon(ItemEditFull plugin) {
        super("skeleton_horse_summon", "Skeleton Horse Summon", "Summons a spectral skeleton horse to ride for a short time.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 20.0);
        Location loc = player.getLocation();
        SkeletonHorse horse = (SkeletonHorse) loc.getWorld().spawnEntity(loc, EntityType.SKELETON_HORSE);
        horse.setTamed(true);
        horse.setOwner(player);
        horse.setAdult();
        org.bukkit.attribute.AttributeInstance speedAttr = horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (speedAttr != null) {
            speedAttr.setBaseValue(speedAttr.getBaseValue() * 1.5);
        }
        horse.addPassenger(player);
        horse.getWorld().playSound(loc, Sound.ENTITY_SKELETON_HORSE_AMBIENT, 1.0f, 0.9f);
        new CompatRunnable() {
            @Override
            public void run() {
                if (horse.isValid()) {
                    horse.eject();
                    horse.getWorld().spawnParticle(org.bukkit.Particle.SMOKE_NORMAL, horse.getLocation().add(0, 1, 0), 15, 0.3, 0.4, 0.3, 0.02);
                    horse.remove();
                }
            }
        }.runTaskLater(plugin, player, (long) (duration * 20));
        return true;
    }
}

class ZoglinRampage extends Ability {
    private final ItemEditFull plugin;
    public ZoglinRampage(ItemEditFull plugin) {
        super("zoglin_rampage", "Zoglin Rampage", "A wild rampage charge that damages and knocks back everything nearby.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double radius = getDoubleParam(plugin, item, "radius", 3.0);
        double damage = getDoubleParam(plugin, item, "damage", 6.0);
        Location loc = player.getLocation();
        loc.getWorld().playSound(loc, Sound.ENTITY_ZOGLIN_ANGRY, 1.3f, 0.8f);
        loc.getWorld().spawnParticle(org.bukkit.Particle.CRIT, loc.add(0, 1, 0), 25, radius * 0.3, 0.4, radius * 0.3, 0.1);
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
            if (entity instanceof LivingEntity && !entity.equals(player)) {
                LivingEntity living = (LivingEntity) entity;
                living.damage(damage, player);
                Vector away = VectorUtils.safeNormalize(living.getLocation().toVector().subtract(loc.toVector())).multiply(1.4).setY(0.3);
                living.setVelocity(away);
            }
        }
        return true;
    }
}

class StriderLavaDash extends Ability {
    private final ItemEditFull plugin;
    public StriderLavaDash(ItemEditFull plugin) {
        super("strider_lava_dash", "Strider Lava Dash", "Dash swiftly across any surface, immune to fire, briefly.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 4.0);
        int amplifier = getIntParam(plugin, item, "speed_amplifier", 1);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, (int) (duration * 20), amplifier));
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, (int) (duration * 20), 0));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_STRIDER_HAPPY, 1.0f, 1.2f);
        return true;
    }
}

class AllaysReturn extends Ability {
    private final ItemEditFull plugin;
    public AllaysReturn(ItemEditFull plugin) {
        super("allays_return", "Allay's Return", "Recall your last thrown projectile instantly back to hand.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double speedBonusDuration = getDoubleParam(plugin, item, "speed_duration", 3.0);
        Entity projectile = MobExpansionAbilities.getLastProjectile(player.getUniqueId());
        if (projectile instanceof Projectile && projectile.isValid()) {
            projectile.getWorld().spawnParticle(org.bukkit.Particle.END_ROD, projectile.getLocation(), 10, 0.2, 0.2, 0.2, 0.02);
            projectile.remove();
            player.sendMessage("§dYour projectile snaps back to you!");
        } else {
            player.sendMessage("§dNo projectile in flight - your allay returns empty-handed.");
        }
        player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, (int) (speedBonusDuration * 20), 0));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM, 1.0f, 1.4f);
        return true;
    }
}

class PhantomDreadDive extends Ability {
    private final ItemEditFull plugin;
    public PhantomDreadDive(ItemEditFull plugin) {
        super("phantom_dread_dive", "Phantom Dread Dive", "Swoop in from above, dealing bonus damage to targets caught in darkness.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double range = getDoubleParam(plugin, item, "range", 8.0);
        double damage = getDoubleParam(plugin, item, "damage", 6.0);
        double nightBonus = getDoubleParam(plugin, item, "night_bonus", 0.5);
        Entity target = player.getTargetEntity((int) range);
        if (!(target instanceof LivingEntity)) {
            player.sendMessage("§cNo target in range.");
            return false;
        }
        LivingEntity living = (LivingEntity) target;
        int lightLevel = living.getLocation().getBlock().getLightLevel();
        double totalDamage = lightLevel <= 4 ? damage * (1 + nightBonus) : damage;
        living.damage(totalDamage, player);
        Vector v = player.getVelocity();
        player.setVelocity(new Vector(v.getX(), 0.5, v.getZ()));
        living.getWorld().playSound(living.getLocation(), Sound.ENTITY_PHANTOM_SWOOP, 1.2f, 1.0f);
        return true;
    }
}

class IronGolemSlam extends Ability {
    private final ItemEditFull plugin;
    public IronGolemSlam(ItemEditFull plugin) {
        super("iron_golem_slam", "Iron Golem Slam", "A ground slam that knocks up and damages all nearby enemies.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double radius = getDoubleParam(plugin, item, "radius", 5.0);
        double damage = getDoubleParam(plugin, item, "damage", 8.0);
        Location loc = player.getLocation();
        loc.getWorld().playSound(loc, Sound.ENTITY_IRON_GOLEM_ATTACK, 1.3f, 0.7f);
        loc.getWorld().spawnParticle(org.bukkit.Particle.EXPLOSION_NORMAL, loc, 15, radius * 0.3, 0.1, radius * 0.3, 0);
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, radius, 2.0, radius)) {
            if (entity instanceof LivingEntity && !entity.equals(player)) {
                LivingEntity living = (LivingEntity) entity;
                living.damage(damage, player);
                living.setVelocity(new Vector(living.getVelocity().getX(), 1.0, living.getVelocity().getZ()));
            }
        }
        return true;
    }
}

class DrownedUndertowSnare extends Ability {
    private final ItemEditFull plugin;
    public DrownedUndertowSnare(ItemEditFull plugin) {
        super("drowned_undertow_snare", "Drowned Undertow Snare", "A trident-assisted undertow that roots and reels in a distant target.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double range = getDoubleParam(plugin, item, "range", 10.0);
        double rootDuration = getDoubleParam(plugin, item, "root_duration", 2.0);
        Entity target = player.getTargetEntity((int) range);
        if (!(target instanceof LivingEntity)) {
            player.sendMessage("§cNo target in range.");
            return false;
        }
        LivingEntity living = (LivingEntity) target;
        Vector pull = VectorUtils.safeNormalize(player.getLocation().toVector().subtract(living.getLocation().toVector())).multiply(1.5).setY(0.15);
        living.setVelocity(pull);
        living.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) (rootDuration * 20), 4));
        living.getWorld().playSound(living.getLocation(), Sound.ENTITY_DROWNED_SWIM, 1.0f, 0.9f);
        living.getWorld().spawnParticle(org.bukkit.Particle.WATER_SPLASH, living.getLocation(), 20, 0.3, 0.3, 0.3, 0.05);
        return true;
    }
}

class VexFlicker extends Ability {
    private final ItemEditFull plugin;
    public VexFlicker(ItemEditFull plugin) {
        super("vex_flicker", "Vex Flicker", "Flicker erratically for a moment, becoming very hard to hit.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 3.0);
        double evasion = getDoubleParam(plugin, item, "evasion_bonus", 0.6);
        MobExpansionAbilities.activateDodge(player.getUniqueId(), System.currentTimeMillis() + (long) (duration * 1000), evasion);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_VEX_CHARGE, 1.0f, 1.3f);
        player.getWorld().spawnParticle(org.bukkit.Particle.SMOKE_NORMAL, player.getLocation(), 15, 0.3, 0.5, 0.3, 0.02);
        return true;
    }
}
