package com.itemedit.full.ability.general;

import com.itemedit.full.ItemEditFull;
import com.itemedit.full.ability.Ability;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import com.itemedit.full.utils.CompatRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class OverworldAbilities implements Listener {
    static final Map<UUID, Long> activeCreeperCharges = new HashMap<>();
    static final Map<UUID, Long> activeSpiderClimbs = new HashMap<>();
    static final Map<UUID, Long> activeBlizzardShields = new HashMap<>();
    private static ItemEditFull pluginInstance;

    public static void register(ItemEditFull plugin) {
        pluginInstance = plugin;
        plugin.getAbilityManager().registerAbility(new GolemSlam(plugin));
        plugin.getAbilityManager().registerAbility(new CreeperCharge(plugin));
        plugin.getAbilityManager().registerAbility(new PhantomStrike(plugin));
        plugin.getAbilityManager().registerAbility(new BeeSwarm(plugin));
        plugin.getAbilityManager().registerAbility(new WolfPack(plugin));
        plugin.getAbilityManager().registerAbility(new PoisonIvy(plugin));
        plugin.getAbilityManager().registerAbility(new SpiderClimb(plugin));
        plugin.getAbilityManager().registerAbility(new BatGlide(plugin));
        plugin.getAbilityManager().registerAbility(new SlimeBounce(plugin));
        plugin.getAbilityManager().registerAbility(new RockSlide(plugin));
        plugin.getAbilityManager().registerAbility(new Geyser(plugin));
        plugin.getAbilityManager().registerAbility(new EarthShield(plugin));
        plugin.getAbilityManager().registerAbility(new Photosynthesis(plugin));
        plugin.getAbilityManager().registerAbility(new BlizzardShield(plugin));
        plugin.getAbilityManager().registerAbility(new Avalanche(plugin));
        plugin.getAbilityManager().registerAbility(new WindWalk(plugin));
        plugin.getAbilityManager().registerAbility(new SporeBlast(plugin));
        plugin.getAbilityManager().registerAbility(new SquidInk(plugin));
        plugin.getAbilityManager().registerAbility(new DolphinGrace(plugin));
        plugin.getAbilityManager().registerAbility(new MinerSense(plugin));
        plugin.getAbilityManager().registerAbility(new PufferfishPoison(plugin));
        plugin.getAbilityManager().registerAbility(new OakSkin(plugin));
        plugin.getAbilityManager().registerAbility(new WindShockwave(plugin));
        plugin.getAbilityManager().registerAbility(new BambooSpear(plugin));
        plugin.getAbilityManager().registerAbility(new Tempest(plugin));

        plugin.getServer().getPluginManager().registerEvents(new OverworldAbilities(), plugin);
    }

    public static void registerCreeperCharge(UUID uuid, long expire) {
        activeCreeperCharges.put(uuid, expire);
    }

    public static void registerSpiderClimb(UUID uuid, long expire) {
        activeSpiderClimbs.put(uuid, expire);
    }

    public static void registerBlizzardShield(UUID uuid, long expire) {
        activeBlizzardShields.put(uuid, expire);
    }

    @EventHandler
    public void onCreeperHit(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            Long expire = activeCreeperCharges.get(player.getUniqueId());
            if (expire != null && System.currentTimeMillis() < expire) {
                activeCreeperCharges.remove(player.getUniqueId());
                Location loc = event.getEntity().getLocation();
                loc.getWorld().playSound(loc, Sound.ENTITY_CREEPER_PRIMED, 1.0f, 0.5f);
                new CompatRunnable() {
                    @Override
                    public void run() {
                        loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.2f, 1.0f);
                        loc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, loc, 1, 0, 0, 0, 0);
                        event.setDamage(event.getDamage() + 6.0);
                    }
                }.runTaskLater(pluginInstance, loc, 10L);
            }
        }
    }

    @EventHandler
    public void onBlizzardShieldHit(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof LivingEntity) {
            Player player = (Player) event.getEntity();
            LivingEntity attacker = (LivingEntity) event.getDamager();
            Long expire = activeBlizzardShields.get(player.getUniqueId());
            if (expire != null && System.currentTimeMillis() < expire) {
                attacker.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, 2));
                attacker.getWorld().playSound(attacker.getLocation(), Sound.BLOCK_GLASS_BREAK, 0.8f, 1.5f);
            }
        }
    }

    @EventHandler
    public void onPhantomHit(ProjectileHitEvent event) {
        if (event.getEntity().hasMetadata("phantom_strike")) {
            Location loc = event.getHitBlock() != null ? event.getHitBlock().getLocation() : event.getEntity().getLocation();
            loc.getWorld().spawnParticle(Particle.SCULK_SOUL, loc, 15, 0.2, 0.2, 0.2, 0.02);
            loc.getWorld().playSound(loc, Sound.ENTITY_PHANTOM_BITE, 1.0f, 1.0f);

            if (event.getHitEntity() instanceof LivingEntity) {
                LivingEntity hit = (LivingEntity) event.getHitEntity();
                hit.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 0));
                hit.damage(5.0, (Player) event.getEntity().getShooter());
            }
        }
    }
}

// ==================== OVERWORLD ABILITIES ====================

class GolemSlam extends Ability {
    private final ItemEditFull plugin;
    public GolemSlam(ItemEditFull plugin) { super("golem_slam", "Golem Slam", "Throws target high into the air, dealing damage."); this.plugin = plugin; }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double damage = getDoubleParam(plugin, item, "damage", 6.0);
        Entity target = player.getTargetEntity(5);
        if (!(target instanceof LivingEntity)) { player.sendMessage("§cNo target entity."); return false; }
        LivingEntity living = (LivingEntity) target;
        living.damage(damage, player);
        living.setVelocity(new Vector(0, 1.5, 0));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_IRON_GOLEM_ATTACK, 1.2f, 1.0f);
        return true;
    }
}

class CreeperCharge extends Ability {
    private final ItemEditFull plugin;
    public CreeperCharge(ItemEditFull plugin) { super("creeper_charge", "Creeper Charge", "Your next melee hit causes an explosion."); this.plugin = plugin; }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 10.0);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_CREEPER_PRIMED, 1.0f, 1.0f);
        OverworldAbilities.registerCreeperCharge(player.getUniqueId(), System.currentTimeMillis() + (long)(duration*1000));
        return true;
    }
}

class PhantomStrike extends Ability {
    private final ItemEditFull plugin;
    public PhantomStrike(ItemEditFull plugin) { super("phantom_strike", "Phantom Strike", "Fires a phantom projectile that blinds targets."); this.plugin = plugin; }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        Snowball ball = player.launchProjectile(Snowball.class);
        ball.setMetadata("phantom_strike", new FixedMetadataValue(plugin, true));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PHANTOM_SWOOP, 1.0f, 1.2f);
        return true;
    }
}

class BeeSwarm extends Ability {
    private final ItemEditFull plugin;
    public BeeSwarm(ItemEditFull plugin) { super("bee_swarm", "Bee Swarm", "Summons 3 angry bees targeting targeted entity."); this.plugin = plugin; }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        Entity target = player.getTargetEntity(15);
        if (!(target instanceof LivingEntity)) { player.sendMessage("§cNo target."); return false; }
        LivingEntity living = (LivingEntity) target;
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BEE_LOOP, 1.0f, 1.0f);
        for (int i = 0; i < 3; i++) {
            Bee bee = (Bee) player.getWorld().spawnEntity(player.getLocation().add((Math.random()-0.5)*2, 1, (Math.random()-0.5)*2), EntityType.BEE);
            bee.setHasStung(false);
            bee.setTarget(living);
            bee.setAnger(200);
            bee.setMetadata("helper", new FixedMetadataValue(plugin, "true"));
            new CompatRunnable() {
                @Override public void run() { if (bee.isValid()) bee.remove(); }
            }.runTaskLater(plugin, bee, 200L); // 10s
        }
        return true;
    }
}

class WolfPack extends Ability {
    private final ItemEditFull plugin;
    public WolfPack(ItemEditFull plugin) { super("wolf_pack", "Wolf Pack", "Summons 3 helper wolves."); this.plugin = plugin; }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WOLF_GROWL, 1.0f, 1.0f);
        List<Wolf> spawned = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Wolf wolf = (Wolf) player.getWorld().spawnEntity(player.getLocation().add((Math.random()-0.5)*2, 0, (Math.random()-0.5)*2), EntityType.WOLF);
            wolf.setAngry(true);
            wolf.setMetadata("helper", new FixedMetadataValue(plugin, "true"));
            Entity target = player.getTargetEntity(12);
            if (target instanceof LivingEntity) wolf.setTarget((LivingEntity) target);
            spawned.add(wolf);
        }
        new CompatRunnable() {
            @Override public void run() { for (Wolf w : spawned) { if (w.isValid()) w.remove(); } }
        }.runTaskLater(plugin, player, 300L);
        return true;
    }
}

class PoisonIvy extends Ability {
    private final ItemEditFull plugin;
    public PoisonIvy(ItemEditFull plugin) { super("poison_ivy", "Poison Ivy", "Creates poison leaf trap."); this.plugin = plugin; }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        Block target = player.getTargetBlockExact(15);
        if (target == null) return false;
        Location loc = target.getLocation().add(0.5, 1.0, 0.5);
        loc.getWorld().playSound(loc, Sound.BLOCK_AZALEA_LEAVES_PLACE, 1.0f, 0.8f);
        new CompatRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks > 100) { cancel(); return; }
                loc.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, loc, 5, 0.4, 0.1, 0.4, 0);
                for (Entity vic : loc.getWorld().getNearbyEntities(loc, 1.5, 1.5, 1.5)) {
                    if (vic instanceof LivingEntity && !vic.equals(player)) {
                        ((LivingEntity) vic).addPotionEffect(new PotionEffect(PotionEffectType.POISON, 60, 1));
                    }
                }
                ticks += 10;
            }
        }.runTaskTimer(plugin, loc, 0L, 10L);
        return true;
    }
}

class SpiderClimb extends Ability {
    private final ItemEditFull plugin;
    public SpiderClimb(ItemEditFull plugin) { super("spider_climb", "Spider Climb", "Allows wall climbing for 15 seconds."); this.plugin = plugin; }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 15.0);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_SPIDER_STEP, 1.0f, 1.5f);
        OverworldAbilities.registerSpiderClimb(player.getUniqueId(), System.currentTimeMillis() + (long)(duration*1000));
        new CompatRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                Long exp = OverworldAbilities.activeSpiderClimbs.get(player.getUniqueId());
                if (exp == null || System.currentTimeMillis() > exp || !player.isOnline()) { cancel(); return; }
                // wall climbing check
                boolean wall = false;
                Location pLoc = player.getLocation();
                for (org.bukkit.block.BlockFace face : new org.bukkit.block.BlockFace[]{org.bukkit.block.BlockFace.NORTH, org.bukkit.block.BlockFace.SOUTH, org.bukkit.block.BlockFace.EAST, org.bukkit.block.BlockFace.WEST}) {
                    if (pLoc.getBlock().getRelative(face).getType().isSolid()) {
                        wall = true; break;
                    }
                }
                if (wall && player.isSneaking()) {
                    player.setVelocity(player.getVelocity().setY(0.25));
                }
                ticks += 2;
            }
        }.runTaskTimer(plugin, player, 0L, 2L);
        return true;
    }
}

class BatGlide extends Ability {
    private final ItemEditFull plugin;
    public BatGlide(ItemEditFull plugin) { super("bat_glide", "Bat Glide", "Slow falling and invisibility while sneaking."); this.plugin = plugin; }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 15.0);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BAT_LOOP, 1.0f, 1.2f);
        new CompatRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks >= (duration * 20) || !player.isOnline()) { cancel(); return; }
                if (player.isSneaking()) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 10, 0));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 10, 0));
                }
                ticks += 5;
            }
        }.runTaskTimer(plugin, player, 0L, 5L);
        return true;
    }
}

class SlimeBounce extends Ability {
    private final ItemEditFull plugin;
    public SlimeBounce(ItemEditFull plugin) { super("slime_bounce", "Slime Bounce", "Bounce forward, negate fall damage."); this.plugin = plugin; }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_SLIME_JUMP, 1.2f, 1.0f);
        Vector dir = player.getLocation().getDirection().setY(0.4).normalize().multiply(1.5);
        player.setVelocity(dir);
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 60, 2));
        new CompatRunnable() {
            @Override
            public void run() {
                if (player.isOnline()) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 100, 4)); // absolute fall negation
                }
            }
        }.runTaskLater(plugin, player, 10L);
        return true;
    }
}

class RockSlide extends Ability {
    private final ItemEditFull plugin;
    public RockSlide(ItemEditFull plugin) { super("rock_slide", "Rock Slide", "Drops stones from the sky."); this.plugin = plugin; }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        Block target = player.getTargetBlockExact(20);
        if (target == null) return false;
        Location loc = target.getLocation().add(0.5, 12.0, 0.5);
        loc.getWorld().playSound(loc, Sound.BLOCK_BASALT_PLACE, 1.0f, 0.8f);
        for (int i = 0; i < 5; i++) {
            new CompatRunnable() {
                @Override
                public void run() {
                    org.bukkit.entity.FallingBlock rock = loc.getWorld().spawnFallingBlock(loc.clone().add((Math.random()-0.5)*3.0, 0, (Math.random()-0.5)*3.0), Material.COBBLESTONE.createBlockData());
                    rock.setDropItem(false);
                    rock.setHurtEntities(true);
                }
            }.runTaskLater(plugin, loc, i * 3L);
        }
        return true;
    }
}

class Geyser extends Ability {
    private final ItemEditFull plugin;
    public Geyser(ItemEditFull plugin) { super("geyser", "Geyser", "Spawns a water jet that launches target."); this.plugin = plugin; }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        Entity target = player.getTargetEntity(10);
        if (!(target instanceof LivingEntity)) return false;
        LivingEntity living = (LivingEntity) target;
        Location loc = living.getLocation();
        loc.getWorld().playSound(loc, Sound.BLOCK_WATER_AMBIENT, 1.2f, 1.5f);
        loc.getWorld().spawnParticle(Particle.WATER_SPLASH, loc, 30, 0.3, 2.0, 0.3, 0.1);
        living.damage(4.0, player);
        living.setVelocity(new Vector(0, 1.3, 0));
        return true;
    }
}

class EarthShield extends Ability {
    private final ItemEditFull plugin;
    public EarthShield(ItemEditFull plugin) { super("earth_shield", "Earth Shield", "Absorbs damage."); this.plugin = plugin; }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_GRASS_PLACE, 1.0f, 0.8f);
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 160, 1));
        return true;
    }
}

class Photosynthesis extends Ability {
    private final ItemEditFull plugin;
    public Photosynthesis(ItemEditFull plugin) { super("photosynthesis", "Photosynthesis", "Heals standing in sunlight."); this.plugin = plugin; }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        Location loc = player.getLocation();
        if (loc.getBlock().getLightFromSky() > 10 && loc.getWorld().getTime() < 12000) {
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 1.5f);
            player.setHealth(Math.min(player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue(), player.getHealth() + 4.0));
            player.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, player.getLocation().add(0, 1.0, 0), 10, 0.3, 0.5, 0.3, 0.01);
            return true;
        } else {
            player.sendMessage("§cYou must stand in direct sunlight during the day!");
            return false;
        }
    }
}

class BlizzardShield extends Ability {
    private final ItemEditFull plugin;
    public BlizzardShield(ItemEditFull plugin) { super("blizzard_shield", "Blizzard Shield", "Slows attackers."); this.plugin = plugin; }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 8.0);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1.0f, 1.5f);
        OverworldAbilities.registerBlizzardShield(player.getUniqueId(), System.currentTimeMillis() + (long)(duration*1000));
        return true;
    }
}

class Avalanche extends Ability {
    private final ItemEditFull plugin;
    public Avalanche(ItemEditFull plugin) { super("avalanche", "Avalanche", "Volley of snowballs."); this.plugin = plugin; }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        Location loc = player.getEyeLocation();
        Vector dir = loc.getDirection().normalize();
        player.getWorld().playSound(loc, Sound.BLOCK_SNOW_PLACE, 1.2f, 1.2f);
        for (int i = -3; i <= 3; i++) {
            double angleRad = Math.toRadians(i * 6);
            double cos = Math.cos(angleRad);
            double sin = Math.sin(angleRad);
            double x = dir.getX() * cos - dir.getZ() * sin;
            double z = dir.getX() * sin + dir.getZ() * cos;
            Vector spreadDir = new Vector(x, dir.getY(), z);
            Snowball s = player.launchProjectile(Snowball.class);
            s.setVelocity(spreadDir.multiply(1.4));
        }
        return true;
    }
}

class WindWalk extends Ability {
    private final ItemEditFull plugin;
    public WindWalk(ItemEditFull plugin) { super("wind_walk", "Wind Walk", "Speed and Jump boost."); this.plugin = plugin; }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.5f);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 300, 2));
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 300, 1));
        return true;
    }
}

class SporeBlast extends Ability {
    private final ItemEditFull plugin;
    public SporeBlast(ItemEditFull plugin) { super("spore_blast", "Spore Blast", "Poison spores."); this.plugin = plugin; }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        Location loc = player.getLocation();
        loc.getWorld().playSound(loc, Sound.BLOCK_MUD_BREAK, 1.2f, 0.8f);
        loc.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, loc, 25, 3.0, 1.0, 3.0, 0.02);
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, 4.0, 2.0, 4.0)) {
            if (entity instanceof LivingEntity && !entity.equals(player)) {
                LivingEntity living = (LivingEntity) entity;
                living.damage(3.0, player);
                living.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 0));
                living.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 1));
            }
        }
        return true;
    }
}

class SquidInk extends Ability {
    private final ItemEditFull plugin;
    public SquidInk(ItemEditFull plugin) { super("squid_ink", "Squid Ink", "Blinds nearby enemies."); this.plugin = plugin; }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        Location loc = player.getLocation();
        loc.getWorld().playSound(loc, Sound.ENTITY_SQUID_SQUIRT, 1.2f, 1.0f);
        loc.getWorld().spawnParticle(Particle.PORTAL, loc, 30, 2.0, 1.0, 2.0, 0.05);
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, 5.0, 2.0, 5.0)) {
            if (entity instanceof LivingEntity && !entity.equals(player)) {
                ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 0));
                ((LivingEntity) entity).damage(2.0, player);
            }
        }
        return true;
    }
}

class DolphinGrace extends Ability {
    private final ItemEditFull plugin;
    public DolphinGrace(ItemEditFull plugin) { super("dolphin_grace", "Dolphins Grace", "Swimming boosts."); this.plugin = plugin; }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_DOLPHIN_PLAY, 1.0f, 1.2f);
        player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 600, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 600, 0));
        return true;
    }
}

class MinerSense extends Ability {
    private final ItemEditFull plugin;
    public MinerSense(ItemEditFull plugin) { super("miner_sense", "Miner Sense", "Haste II support."); this.plugin = plugin; }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_AMETHYST_CLUSTER_STEP, 1.0f, 1.2f);
        player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 300, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 300, 0));
        return true;
    }
}

class PufferfishPoison extends Ability {
    private final ItemEditFull plugin;
    public PufferfishPoison(ItemEditFull plugin) { super("pufferfish_poison", "Pufferfish Poison", "Poisons targets around you."); this.plugin = plugin; }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PUFFER_FISH_BLOW_OUT, 1.0f, 1.0f);
        player.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, player.getLocation().add(0, 1, 0), 15, 2.0, 0.5, 2.0, 0.02);
        for (Entity entity : player.getNearbyEntities(2.5, 2.5, 2.5)) {
            if (entity instanceof LivingEntity && !entity.equals(player)) {
                LivingEntity living = (LivingEntity) entity;
                living.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 1));
                living.damage(2.0, player);
            }
        }
        return true;
    }
}

class OakSkin extends Ability {
    private final ItemEditFull plugin;
    public OakSkin(ItemEditFull plugin) { super("oak_skin", "Oak Skin", "Resistance III and Slowness I."); this.plugin = plugin; }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_WOOD_PLACE, 1.0f, 0.8f);
        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 200, 2));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 200, 0));
        return true;
    }
}

class WindShockwave extends Ability {
    private final ItemEditFull plugin;
    public WindShockwave(ItemEditFull plugin) { super("wind_shockwave", "Wind Shockwave", "Deflects arrows."); this.plugin = plugin; }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        Location loc = player.getLocation();
        player.getWorld().playSound(loc, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1.2f, 1.2f);
        loc.getWorld().spawnParticle(Particle.CLOUD, loc.add(0, 1, 0), 20, 3.0, 0.5, 3.0, 0.05);
        for (Entity ent : player.getNearbyEntities(6.0, 6.0, 6.0)) {
            if (ent instanceof Projectile) {
                Vector deflection = ent.getLocation().toVector().subtract(player.getLocation().toVector()).normalize().multiply(1.5);
                ent.setVelocity(deflection);
            }
        }
        return true;
    }
}

class BambooSpear extends Ability {
    private final ItemEditFull plugin;
    public BambooSpear(ItemEditFull plugin) { super("bamboo_spear", "Bamboo Spear", "Bamboo projectile dealing damage."); this.plugin = plugin; }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        Arrow arrow = player.launchProjectile(Arrow.class);
        arrow.setMetadata("custom_ability", new FixedMetadataValue(plugin, true));
        arrow.setDamage(6.0);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1.0f, 1.5f);
        return true;
    }
}

class Tempest extends Ability {
    private final ItemEditFull plugin;
    public Tempest(ItemEditFull plugin) { super("tempest", "Tempest", "Strikes lightning in targeted area."); this.plugin = plugin; }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        Block target = player.getTargetBlockExact(25);
        if (target == null) return false;
        Location loc = target.getLocation();
        loc.getWorld().playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.5f, 0.8f);
        for (int i = 0; i < 3; i++) {
            new CompatRunnable() {
                @Override
                public void run() {
                    Location strikeLoc = loc.clone().add((Math.random()-0.5)*5.0, 0, (Math.random()-0.5)*5.0);
                    strikeLoc.getWorld().strikeLightningEffect(strikeLoc);
                    for (Entity entity : strikeLoc.getWorld().getNearbyEntities(strikeLoc, 3.0, 3.0, 3.0)) {
                        if (entity instanceof LivingEntity && !entity.equals(player)) {
                            ((LivingEntity) entity).damage(5.0, player);
                            entity.setFireTicks(40);
                        }
                    }
                }
            }.runTaskLater(plugin, loc, i * 15L);
        }
        return true;
    }
}
