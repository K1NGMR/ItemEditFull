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
import com.itemedit.full.utils.EffectUtils;
import com.itemedit.full.utils.EffectUtils.Layer;
import com.itemedit.full.utils.EffectUtils.SoundLayer;
import org.bukkit.util.Vector;

import java.util.*;

public class OverworldAbilities implements Listener {
    static final Map<UUID, Long> activeCreeperCharges = new java.util.concurrent.ConcurrentHashMap<>();
    static final Map<UUID, Long> activeSpiderClimbs = new java.util.concurrent.ConcurrentHashMap<>();
    static final Map<UUID, Long> activeBlizzardShields = new java.util.concurrent.ConcurrentHashMap<>();
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

    @EventHandler
    public void onQuit(org.bukkit.event.player.PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        activeCreeperCharges.remove(id);
        activeSpiderClimbs.remove(id);
        activeBlizzardShields.remove(id);
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
                ItemStack hand = player.getInventory().getItemInMainHand();
                Ability ab = pluginInstance.getAbilityManager().getAbility("creeper_charge");
                double bonusDamage = ab != null ? ab.getDoubleParam(hand, "bonus_damage", 6.0) : 6.0;
                double delay = ab != null ? ab.getDoubleParam(hand, "explosion_delay", 0.5) : 0.5;
                new CompatRunnable() {
                    @Override
                    public void run() {
                        EffectUtils.fanfare(loc,
                                new SoundLayer(Sound.ENTITY_GENERIC_EXPLODE, 1.2f, 1.0f),
                                new SoundLayer(Sound.ENTITY_CREEPER_DEATH, 0.8f, 0.9f));
                        EffectUtils.burst(loc,
                                new Layer(Particle.EXPLOSION_LARGE, 1, 0, 0, 0, 0),
                                new Layer(Particle.EXPLOSION_HUGE, 1, 0, 0, 0, 0),
                                new Layer(Particle.SMOKE_LARGE, 12, 0.4, 0.3, 0.4, 0.05));
                        event.setDamage(event.getDamage() + bonusDamage);
                    }
                }.runTaskLater(pluginInstance, loc, (long)(delay * 20));
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
                ItemStack hand = player.getInventory().getItemInMainHand();
                Ability ab = pluginInstance.getAbilityManager().getAbility("blizzard_shield");
                double slowDur = ab != null ? ab.getDoubleParam(hand, "slow_duration", 3.0) : 3.0;
                int slowAmp = ab != null ? ab.getIntParam(hand, "slow_amplifier", 2) : 2;
                attacker.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) (slowDur * 20), slowAmp));
                EffectUtils.burst(attacker.getLocation().add(0, 1, 0),
                        new Layer(Particle.SNOWFLAKE, 10, 0.3, 0.3, 0.3, 0.05),
                        new Layer(Particle.CLOUD, 6, 0.3, 0.3, 0.3, 0.02));
                attacker.getWorld().playSound(attacker.getLocation(), Sound.BLOCK_GLASS_BREAK, 0.8f, 1.5f);
            }
        }
    }

    @EventHandler
    public void onPhantomHit(ProjectileHitEvent event) {
        if (event.getEntity().hasMetadata("phantom_strike")) {
            Location loc = event.getHitBlock() != null ? event.getHitBlock().getLocation() : event.getEntity().getLocation();
            EffectUtils.burst(loc,
                    new Layer(Particle.SCULK_SOUL, 15, 0.2, 0.2, 0.2, 0.02),
                    new Layer(Particle.SOUL, 8, 0.25, 0.25, 0.25, 0.01),
                    new Layer(Particle.PORTAL, 10, 0.3, 0.3, 0.3, 0.4));
            EffectUtils.fanfare(loc,
                    new SoundLayer(Sound.ENTITY_PHANTOM_BITE, 1.0f, 1.0f),
                    new SoundLayer(Sound.ENTITY_PHANTOM_HURT, 0.6f, 0.7f));

            if (event.getHitEntity() instanceof LivingEntity) {
                LivingEntity hit = (LivingEntity) event.getHitEntity();
                double blindDur = 5.0;
                int blindAmp = 0;
                double damage = 5.0;
                if (event.getEntity().hasMetadata("blindness_duration")) {
                    blindDur = event.getEntity().getMetadata("blindness_duration").get(0).asDouble();
                }
                if (event.getEntity().hasMetadata("blindness_amplifier")) {
                    blindAmp = event.getEntity().getMetadata("blindness_amplifier").get(0).asInt();
                }
                if (event.getEntity().hasMetadata("damage")) {
                    damage = event.getEntity().getMetadata("damage").get(0).asDouble();
                }
                hit.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, (int) (blindDur * 20), blindAmp));
                hit.damage(damage, (Player) event.getEntity().getShooter());
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
        double velocity = getDoubleParam(plugin, item, "velocity", 1.5);
        double range = getDoubleParam(plugin, item, "range", 5.0);

        Entity target = player.getTargetEntity((int) range);
        if (!(target instanceof LivingEntity)) { player.sendMessage("§cNo target entity."); return false; }
        LivingEntity living = (LivingEntity) target;
        Location impact = living.getLocation();
        living.damage(damage, player);
        living.setVelocity(new Vector(0, velocity, 0));
        EffectUtils.burst(impact,
                new Layer(Particle.BLOCK_CRACK, 20, 0.4, 0.2, 0.4, 0.1, Material.STONE.createBlockData()),
                new Layer(Particle.CRIT, 12, 0.3, 0.3, 0.3, 0.2),
                new Layer(Particle.DAMAGE_INDICATOR, 6, 0.3, 0.3, 0.3, 0.1));
        EffectUtils.fanfare(impact,
                new SoundLayer(Sound.ENTITY_IRON_GOLEM_ATTACK, 1.2f, 1.0f),
                new SoundLayer(Sound.BLOCK_STONE_BREAK, 0.8f, 0.8f));
        return true;
    }
}

class CreeperCharge extends Ability {
    private final ItemEditFull plugin;
    public CreeperCharge(ItemEditFull plugin) { super("creeper_charge", "Creeper Charge", "Your next melee hit causes an explosion."); this.plugin = plugin; }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 10.0);
        Location loc = player.getLocation();
        EffectUtils.fanfare(loc,
                new SoundLayer(Sound.ENTITY_CREEPER_PRIMED, 1.0f, 1.0f),
                new SoundLayer(Sound.BLOCK_FIRE_AMBIENT, 0.6f, 1.4f));
        EffectUtils.burst(loc.clone().add(0, 1, 0),
                new Layer(Particle.SMOKE_NORMAL, 10, 0.3, 0.4, 0.3, 0.02),
                new Layer(Particle.FLAME, 6, 0.2, 0.3, 0.2, 0.02));
        OverworldAbilities.registerCreeperCharge(player.getUniqueId(), System.currentTimeMillis() + (long)(duration*1000));
        return true;
    }
}

class PhantomStrike extends Ability {
    private final ItemEditFull plugin;
    public PhantomStrike(ItemEditFull plugin) { super("phantom_strike", "Phantom Strike", "Fires a phantom projectile that blinds targets."); this.plugin = plugin; }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double blindDur = getDoubleParam(plugin, item, "blindness_duration", 5.0);
        int blindAmp = getIntParam(plugin, item, "blindness_amplifier", 0);
        double damage = getDoubleParam(plugin, item, "damage", 5.0);

        Snowball ball = player.launchProjectile(Snowball.class);
        ball.setMetadata("phantom_strike", new FixedMetadataValue(plugin, true));
        ball.setMetadata("blindness_duration", new FixedMetadataValue(plugin, blindDur));
        ball.setMetadata("blindness_amplifier", new FixedMetadataValue(plugin, blindAmp));
        ball.setMetadata("damage", new FixedMetadataValue(plugin, damage));
        EffectUtils.burst(player.getEyeLocation(),
                new Layer(Particle.SOUL, 8, 0.15, 0.15, 0.15, 0.01),
                new Layer(Particle.SCULK_SOUL, 6, 0.15, 0.15, 0.15, 0.01));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PHANTOM_SWOOP, 1.0f, 1.2f);
        return true;
    }
}

class BeeSwarm extends Ability {
    private final ItemEditFull plugin;
    public BeeSwarm(ItemEditFull plugin) { super("bee_swarm", "Bee Swarm", "Summons 3 angry bees targeting targeted entity."); this.plugin = plugin; }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        int count = getIntParam(plugin, item, "bees", 3);
        int anger = getIntParam(plugin, item, "anger", 200);
        double duration = getDoubleParam(plugin, item, "duration", 10.0);
        double range = getDoubleParam(plugin, item, "range", 15.0);

        Entity target = player.getTargetEntity((int) range);
        if (!(target instanceof LivingEntity)) { player.sendMessage("§cNo target."); return false; }
        LivingEntity living = (LivingEntity) target;
        EffectUtils.fanfare(player.getLocation(),
                new SoundLayer(Sound.ENTITY_BEE_LOOP, 1.0f, 1.0f),
                new SoundLayer(Sound.BLOCK_BEEHIVE_WORK, 0.8f, 1.2f));
        EffectUtils.burst(player.getLocation().add(0, 1, 0),
                new Layer(Particle.CRIT, 10, 0.5, 0.5, 0.5, 0.1),
                new Layer(Particle.COMPOSTER, 12, 0.5, 0.5, 0.5, 0.0));
        for (int i = 0; i < count; i++) {
            Bee bee = (Bee) player.getWorld().spawnEntity(player.getLocation().add((Math.random()-0.5)*2, 1, (Math.random()-0.5)*2), EntityType.BEE);
            bee.setHasStung(false);
            bee.setTarget(living);
            bee.setAnger(anger);
            bee.setMetadata("helper", new FixedMetadataValue(plugin, "true"));
            new CompatRunnable() {
                @Override public void run() { if (bee.isValid()) bee.remove(); }
            }.runTaskLater(plugin, bee, (long) (duration * 20));
        }
        return true;
    }
}

class WolfPack extends Ability {
    private final ItemEditFull plugin;
    public WolfPack(ItemEditFull plugin) { super("wolf_pack", "Wolf Pack", "Summons 3 helper wolves."); this.plugin = plugin; }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        int count = getIntParam(plugin, item, "wolves", 3);
        double duration = getDoubleParam(plugin, item, "duration", 15.0);
        double range = getDoubleParam(plugin, item, "range", 12.0);

        EffectUtils.fanfare(player.getLocation(),
                new SoundLayer(Sound.ENTITY_WOLF_GROWL, 1.0f, 1.0f),
                new SoundLayer(Sound.ENTITY_WOLF_HOWL, 0.7f, 1.1f));
        EffectUtils.burst(player.getLocation().add(0, 0.5, 0),
                new Layer(Particle.SWEEP_ATTACK, 3, 0.6, 0.2, 0.6, 0.0),
                new Layer(Particle.CRIT, 12, 0.6, 0.3, 0.6, 0.1));
        List<Wolf> spawned = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Wolf wolf = (Wolf) player.getWorld().spawnEntity(player.getLocation().add((Math.random()-0.5)*2, 0, (Math.random()-0.5)*2), EntityType.WOLF);
            wolf.setAngry(true);
            wolf.setMetadata("helper", new FixedMetadataValue(plugin, "true"));
            Entity target = player.getTargetEntity((int) range);
            if (target instanceof LivingEntity) wolf.setTarget((LivingEntity) target);
            spawned.add(wolf);
        }
        new CompatRunnable() {
            @Override public void run() { for (Wolf w : spawned) { if (w.isValid()) w.remove(); } }
        }.runTaskLater(plugin, player, (long) (duration * 20));
        return true;
    }
}

class PoisonIvy extends Ability {
    private final ItemEditFull plugin;
    public PoisonIvy(ItemEditFull plugin) { super("poison_ivy", "Poison Ivy", "Creates poison leaf trap."); this.plugin = plugin; }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 5.0);
        double poisonDur = getDoubleParam(plugin, item, "poison_duration", 3.0);
        int poisonAmp = getIntParam(plugin, item, "poison_amplifier", 1);
        double range = getDoubleParam(plugin, item, "range", 15.0);

        Block target = player.getTargetBlockExact((int) range);
        if (target == null) return false;
        Location loc = target.getLocation().add(0.5, 1.0, 0.5);
        EffectUtils.fanfare(loc,
                new SoundLayer(Sound.BLOCK_AZALEA_LEAVES_PLACE, 1.0f, 0.8f),
                new SoundLayer(Sound.ENTITY_SPIDER_AMBIENT, 0.5f, 0.6f));
        new CompatRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks > (duration * 20)) { cancel(); return; }
                EffectUtils.burst(loc,
                        new Layer(Particle.SPELL_WITCH, 5, 0.4, 0.1, 0.4, 0),
                        new Layer(Particle.COMPOSTER, 3, 0.35, 0.15, 0.35, 0));
                for (Entity vic : loc.getWorld().getNearbyEntities(loc, 1.5, 1.5, 1.5)) {
                    if (vic instanceof LivingEntity && !vic.equals(player)) {
                        ((LivingEntity) vic).addPotionEffect(new PotionEffect(PotionEffectType.POISON, (int) (poisonDur * 20), poisonAmp));
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
        double upwardVelocity = getDoubleParam(plugin, item, "velocity", 0.25);
        EffectUtils.fanfare(player.getLocation(),
                new SoundLayer(Sound.ENTITY_SPIDER_STEP, 1.0f, 1.5f),
                new SoundLayer(Sound.ENTITY_SPIDER_AMBIENT, 0.6f, 0.8f));
        EffectUtils.burst(player.getLocation(),
                new Layer(Particle.CRIT_MAGIC, 10, 0.3, 0.5, 0.3, 0.1));
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
                    player.setVelocity(player.getVelocity().setY(upwardVelocity));
                    if (ticks % 6 == 0) {
                        EffectUtils.burst(player.getLocation(),
                                new Layer(Particle.CRIT_MAGIC, 3, 0.2, 0.2, 0.2, 0.02));
                    }
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
        int slowFallAmp = getIntParam(plugin, item, "slow_falling_amplifier", 0);
        int invisAmp = getIntParam(plugin, item, "invisibility_amplifier", 0);

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BAT_LOOP, 1.0f, 1.2f);
        new CompatRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks >= (duration * 20) || !player.isOnline()) { cancel(); return; }
                if (player.isSneaking()) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 10, slowFallAmp));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 10, invisAmp));
                    EffectUtils.burst(player.getLocation().add(0, 1, 0),
                            new Layer(Particle.SMOKE_NORMAL, 4, 0.3, 0.2, 0.3, 0.01),
                            new Layer(Particle.SOUL, 2, 0.2, 0.2, 0.2, 0.01));
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
        double bounceVelocity = getDoubleParam(plugin, item, "velocity", 1.5);
        double bounceHeight = getDoubleParam(plugin, item, "height", 0.4);
        double jumpDuration = getDoubleParam(plugin, item, "jump_duration", 3.0);
        int jumpAmp = getIntParam(plugin, item, "jump_amplifier", 2);
        double resistanceDuration = getDoubleParam(plugin, item, "resistance_duration", 5.0);
        int resistanceAmp = getIntParam(plugin, item, "resistance_amplifier", 4);

        EffectUtils.fanfare(player.getLocation(),
                new SoundLayer(Sound.ENTITY_SLIME_JUMP, 1.2f, 1.0f),
                new SoundLayer(Sound.ENTITY_SLIME_SQUISH, 0.8f, 1.3f));
        EffectUtils.burst(player.getLocation(),
                new Layer(Particle.CRIT, 10, 0.3, 0.1, 0.3, 0.15),
                new Layer(Particle.CLOUD, 8, 0.3, 0.1, 0.3, 0.02));
        Vector dir = player.getLocation().getDirection().setY(bounceHeight).normalize().multiply(bounceVelocity);
        player.setVelocity(dir);
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, (int) (jumpDuration * 20), jumpAmp));
        new CompatRunnable() {
            @Override
            public void run() {
                if (player.isOnline()) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, (int) (resistanceDuration * 20), resistanceAmp));
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
        int count = getIntParam(plugin, item, "rocks", 5);
        double spawnHeight = getDoubleParam(plugin, item, "height", 12.0);
        double range = getDoubleParam(plugin, item, "range", 20.0);

        Block target = player.getTargetBlockExact((int) range);
        if (target == null) return false;
        Location loc = target.getLocation().add(0.5, spawnHeight, 0.5);
        EffectUtils.fanfare(loc,
                new SoundLayer(Sound.BLOCK_BASALT_PLACE, 1.0f, 0.8f),
                new SoundLayer(Sound.BLOCK_STONE_BREAK, 0.8f, 0.6f));
        EffectUtils.burst(loc,
                new Layer(Particle.BLOCK_CRACK, 25, 0.6, 0.2, 0.6, 0.1, Material.COBBLESTONE.createBlockData()));
        for (int i = 0; i < count; i++) {
            new CompatRunnable() {
                @Override
                public void run() {
                    Location spawnLoc = loc.clone().add((Math.random()-0.5)*3.0, 0, (Math.random()-0.5)*3.0);
                    EffectUtils.burst(spawnLoc,
                            new Layer(Particle.FALLING_DUST, 8, 0.2, 0.2, 0.2, 0, Material.COBBLESTONE.createBlockData()));
                    org.bukkit.entity.FallingBlock rock = loc.getWorld().spawnFallingBlock(spawnLoc, Material.COBBLESTONE.createBlockData());
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
        double damage = getDoubleParam(plugin, item, "damage", 4.0);
        double velocity = getDoubleParam(plugin, item, "velocity", 1.3);
        double range = getDoubleParam(plugin, item, "range", 10.0);

        Entity target = player.getTargetEntity((int) range);
        if (!(target instanceof LivingEntity)) return false;
        LivingEntity living = (LivingEntity) target;
        Location loc = living.getLocation();
        EffectUtils.fanfare(loc,
                new SoundLayer(Sound.BLOCK_WATER_AMBIENT, 1.2f, 1.5f),
                new SoundLayer(Sound.ENTITY_DOLPHIN_SPLASH, 0.8f, 1.2f));
        EffectUtils.burst(loc,
                new Layer(Particle.WATER_SPLASH, 30, 0.3, 2.0, 0.3, 0.1),
                new Layer(Particle.DRIP_WATER, 15, 0.3, 1.0, 0.3, 0.0),
                new Layer(Particle.REVERSE_PORTAL, 10, 0.2, 1.5, 0.2, 0.05));
        living.damage(damage, player);
        living.setVelocity(new Vector(0, velocity, 0));
        return true;
    }
}

class EarthShield extends Ability {
    private final ItemEditFull plugin;
    public EarthShield(ItemEditFull plugin) { super("earth_shield", "Earth Shield", "Absorbs damage."); this.plugin = plugin; }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 8.0);
        int amp = getIntParam(plugin, item, "amplifier", 1);

        EffectUtils.fanfare(player.getLocation(),
                new SoundLayer(Sound.BLOCK_GRASS_PLACE, 1.0f, 0.8f),
                new SoundLayer(Sound.BLOCK_STONE_PLACE, 0.7f, 0.6f));
        EffectUtils.ring(player.getLocation(), 1.0, 12,
                new Layer(Particle.BLOCK_CRACK, 2, 0.05, 0.3, 0.05, 0, Material.DIRT.createBlockData()));
        EffectUtils.burst(player.getLocation().add(0, 1, 0),
                new Layer(Particle.CRIT, 8, 0.4, 0.4, 0.4, 0.1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, (int) (duration * 20), amp));
        return true;
    }
}

class Photosynthesis extends Ability {
    private final ItemEditFull plugin;
    public Photosynthesis(ItemEditFull plugin) { super("photosynthesis", "Photosynthesis", "Heals standing in sunlight."); this.plugin = plugin; }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double healAmount = getDoubleParam(plugin, item, "heal", 4.0);
        Location loc = player.getLocation();
        if (loc.getBlock().getLightFromSky() > 10 && loc.getWorld().getTime() < 12000) {
            EffectUtils.fanfare(player.getLocation(),
                    new SoundLayer(Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 1.5f),
                    new SoundLayer(Sound.ENTITY_PLAYER_LEVELUP, 0.4f, 1.8f));
            player.setHealth(Math.min(player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue(), player.getHealth() + healAmount));
            EffectUtils.burst(player.getLocation().add(0, 1.0, 0),
                    new Layer(Particle.VILLAGER_HAPPY, 10, 0.3, 0.5, 0.3, 0.01),
                    new Layer(Particle.END_ROD, 8, 0.3, 0.6, 0.3, 0.02));
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
        EffectUtils.fanfare(player.getLocation(),
                new SoundLayer(Sound.BLOCK_GLASS_PLACE, 1.0f, 1.5f),
                new SoundLayer(Sound.BLOCK_SNOW_PLACE, 0.7f, 1.3f));
        EffectUtils.ring(player.getLocation().add(0, 1, 0), 1.2, 14,
                new Layer(Particle.SNOWFLAKE, 2, 0.05, 0.2, 0.05, 0.01));
        EffectUtils.burst(player.getLocation().add(0, 1, 0),
                new Layer(Particle.CLOUD, 10, 0.4, 0.4, 0.4, 0.02));
        OverworldAbilities.registerBlizzardShield(player.getUniqueId(), System.currentTimeMillis() + (long)(duration*1000));
        return true;
    }
}

class Avalanche extends Ability {
    private final ItemEditFull plugin;
    public Avalanche(ItemEditFull plugin) { super("avalanche", "Avalanche", "Volley of snowballs."); this.plugin = plugin; }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double velocity = getDoubleParam(plugin, item, "velocity", 1.4);
        double angleSpread = getDoubleParam(plugin, item, "angle_spread", 6.0);

        Location loc = player.getEyeLocation();
        Vector dir = loc.getDirection().normalize();
        EffectUtils.fanfare(loc,
                new SoundLayer(Sound.BLOCK_SNOW_PLACE, 1.2f, 1.2f),
                new SoundLayer(Sound.ENTITY_SNOWBALL_THROW, 0.8f, 0.8f));
        EffectUtils.burst(loc,
                new Layer(Particle.SNOWFLAKE, 20, 0.3, 0.3, 0.3, 0.1));
        for (int i = -3; i <= 3; i++) {
            double angleRad = Math.toRadians(i * angleSpread);
            double cos = Math.cos(angleRad);
            double sin = Math.sin(angleRad);
            double x = dir.getX() * cos - dir.getZ() * sin;
            double z = dir.getX() * sin + dir.getZ() * cos;
            Vector spreadDir = new Vector(x, dir.getY(), z);
            Snowball s = player.launchProjectile(Snowball.class);
            s.setVelocity(spreadDir.multiply(velocity));
        }
        return true;
    }
}

class WindWalk extends Ability {
    private final ItemEditFull plugin;
    public WindWalk(ItemEditFull plugin) { super("wind_walk", "Wind Walk", "Speed and Jump boost."); this.plugin = plugin; }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 15.0);
        int speedAmp = getIntParam(plugin, item, "speed_amplifier", 2);
        int jumpAmp = getIntParam(plugin, item, "jump_amplifier", 1);

        EffectUtils.fanfare(player.getLocation(),
                new SoundLayer(Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.5f),
                new SoundLayer(Sound.ITEM_ELYTRA_FLYING, 0.5f, 1.6f));
        EffectUtils.ring(player.getLocation().add(0, 1, 0), 0.8, 10,
                new Layer(Particle.CLOUD, 2, 0.05, 0.1, 0.05, 0.02));
        EffectUtils.burst(player.getLocation().add(0, 1, 0),
                new Layer(Particle.SWEEP_ATTACK, 2, 0.3, 0.2, 0.3, 0.0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, (int) (duration * 20), speedAmp));
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, (int) (duration * 20), jumpAmp));
        return true;
    }
}

class SporeBlast extends Ability {
    private final ItemEditFull plugin;
    public SporeBlast(ItemEditFull plugin) { super("spore_blast", "Spore Blast", "Poison spores."); this.plugin = plugin; }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double radius = getDoubleParam(plugin, item, "radius", 4.0);
        double damage = getDoubleParam(plugin, item, "damage", 3.0);
        double duration = getDoubleParam(plugin, item, "poison_duration", 5.0);
        int poisonAmp = getIntParam(plugin, item, "poison_amplifier", 0);
        int slowAmp = getIntParam(plugin, item, "slow_amplifier", 1);

        Location loc = player.getLocation();
        EffectUtils.fanfare(loc,
                new SoundLayer(Sound.BLOCK_MUD_BREAK, 1.2f, 0.8f),
                new SoundLayer(Sound.ENTITY_SPIDER_HURT, 0.5f, 0.6f));
        EffectUtils.ring(loc.add(0, 0.2, 0), radius, 16,
                new Layer(Particle.SPORE_BLOSSOM_AIR, 2, 0.1, 0.3, 0.1, 0.0));
        EffectUtils.burst(loc,
                new Layer(Particle.SPELL_WITCH, 20, radius * 0.4, 0.8, radius * 0.4, 0.0));
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, radius, 2.0, radius)) {
            if (entity instanceof LivingEntity && !entity.equals(player)) {
                LivingEntity living = (LivingEntity) entity;
                living.damage(damage, player);
                living.addPotionEffect(new PotionEffect(PotionEffectType.POISON, (int) (duration * 20), poisonAmp));
                living.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) (duration * 20), slowAmp));
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
        double radius = getDoubleParam(plugin, item, "radius", 5.0);
        double damage = getDoubleParam(plugin, item, "damage", 2.0);
        double blindDur = getDoubleParam(plugin, item, "blindness_duration", 5.0);
        int blindAmp = getIntParam(plugin, item, "blindness_amplifier", 0);

        Location loc = player.getLocation();
        EffectUtils.fanfare(loc,
                new SoundLayer(Sound.ENTITY_SQUID_SQUIRT, 1.2f, 1.0f),
                new SoundLayer(Sound.ENTITY_SQUID_AMBIENT, 0.7f, 0.8f));
        EffectUtils.burst(loc,
                new Layer(Particle.SQUID_INK, 20, radius * 0.3, 1.0, radius * 0.3, 0.05),
                new Layer(Particle.PORTAL, 15, radius * 0.3, 1.0, radius * 0.3, 0.05));
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, radius, 2.0, radius)) {
            if (entity instanceof LivingEntity && !entity.equals(player)) {
                ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, (int) (blindDur * 20), blindAmp));
                ((LivingEntity) entity).damage(damage, player);
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
        double duration = getDoubleParam(plugin, item, "duration", 30.0);
        int graceAmp = getIntParam(plugin, item, "dolphin_grace_amplifier", 0);
        int breathAmp = getIntParam(plugin, item, "water_breathing_amplifier", 0);

        EffectUtils.fanfare(player.getLocation(),
                new SoundLayer(Sound.ENTITY_DOLPHIN_PLAY, 1.0f, 1.2f),
                new SoundLayer(Sound.ENTITY_DOLPHIN_SPLASH, 0.7f, 1.1f));
        EffectUtils.burst(player.getLocation(),
                new Layer(Particle.WATER_SPLASH, 20, 0.4, 0.4, 0.4, 0.1),
                new Layer(Particle.REVERSE_PORTAL, 12, 0.3, 0.5, 0.3, 0.05));
        player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, (int) (duration * 20), graceAmp));
        player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, (int) (duration * 20), breathAmp));
        return true;
    }
}

class MinerSense extends Ability {
    private final ItemEditFull plugin;
    public MinerSense(ItemEditFull plugin) { super("miner_sense", "Miner Sense", "Haste II support."); this.plugin = plugin; }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 15.0);
        int hasteAmp = getIntParam(plugin, item, "haste_amplifier", 1);
        int visionAmp = getIntParam(plugin, item, "night_vision_amplifier", 0);

        EffectUtils.fanfare(player.getLocation(),
                new SoundLayer(Sound.BLOCK_AMETHYST_CLUSTER_STEP, 1.0f, 1.2f),
                new SoundLayer(Sound.BLOCK_STONE_HIT, 0.6f, 1.4f));
        EffectUtils.burst(player.getLocation().add(0, 1, 0),
                new Layer(Particle.BLOCK_CRACK, 15, 0.3, 0.3, 0.3, 0.1, Material.STONE.createBlockData()),
                new Layer(Particle.ELECTRIC_SPARK, 8, 0.3, 0.3, 0.3, 0.02));
        player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, (int) (duration * 20), hasteAmp));
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, (int) (duration * 20), visionAmp));
        return true;
    }
}

class PufferfishPoison extends Ability {
    private final ItemEditFull plugin;
    public PufferfishPoison(ItemEditFull plugin) { super("pufferfish_poison", "Pufferfish Poison", "Poisons targets around you."); this.plugin = plugin; }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double radius = getDoubleParam(plugin, item, "radius", 2.5);
        double poisonDur = getDoubleParam(plugin, item, "poison_duration", 5.0);
        int poisonAmp = getIntParam(plugin, item, "poison_amplifier", 1);
        double damage = getDoubleParam(plugin, item, "damage", 2.0);

        EffectUtils.fanfare(player.getLocation(),
                new SoundLayer(Sound.ENTITY_PUFFER_FISH_BLOW_OUT, 1.0f, 1.0f),
                new SoundLayer(Sound.ENTITY_PUFFER_FISH_STING, 0.8f, 1.2f));
        EffectUtils.burst(player.getLocation().add(0, 1, 0),
                new Layer(Particle.WATER_SPLASH, 15, radius, 0.5, radius, 0.02),
                new Layer(Particle.SPELL_WITCH, 12, radius * 0.6, 0.5, radius * 0.6, 0.0));
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof LivingEntity && !entity.equals(player)) {
                LivingEntity living = (LivingEntity) entity;
                living.addPotionEffect(new PotionEffect(PotionEffectType.POISON, (int) (poisonDur * 20), poisonAmp));
                living.damage(damage, player);
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
        double duration = getDoubleParam(plugin, item, "duration", 10.0);
        int resistAmp = getIntParam(plugin, item, "resistance_amplifier", 2);
        int slowAmp = getIntParam(plugin, item, "slow_amplifier", 0);

        EffectUtils.fanfare(player.getLocation(),
                new SoundLayer(Sound.BLOCK_WOOD_PLACE, 1.0f, 0.8f),
                new SoundLayer(Sound.ITEM_ARMOR_EQUIP_GENERIC, 0.7f, 0.6f));
        EffectUtils.ring(player.getLocation(), 0.9, 10,
                new Layer(Particle.BLOCK_CRACK, 2, 0.05, 0.4, 0.05, 0, Material.OAK_LOG.createBlockData()));
        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, (int) (duration * 20), resistAmp));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) (duration * 20), slowAmp));
        return true;
    }
}

class WindShockwave extends Ability {
    private final ItemEditFull plugin;
    public WindShockwave(ItemEditFull plugin) { super("wind_shockwave", "Wind Shockwave", "Deflects arrows."); this.plugin = plugin; }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double radius = getDoubleParam(plugin, item, "radius", 6.0);
        double velocity = getDoubleParam(plugin, item, "deflect_velocity", 1.5);

        Location loc = player.getLocation();
        EffectUtils.fanfare(loc,
                new SoundLayer(Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1.2f, 1.2f),
                new SoundLayer(Sound.ITEM_ELYTRA_FLYING, 0.6f, 1.5f));
        EffectUtils.ring(loc.clone().add(0, 1, 0), radius * 0.5, 16,
                new Layer(Particle.SWEEP_ATTACK, 1, 0.0, 0.0, 0.0, 0.0));
        EffectUtils.burst(loc.add(0, 1, 0),
                new Layer(Particle.CLOUD, 20, radius*0.5, 0.5, radius*0.5, 0.05));
        for (Entity ent : player.getNearbyEntities(radius, radius, radius)) {
            if (ent instanceof Projectile) {
                Vector deflection = com.itemedit.full.utils.VectorUtils.safeNormalize(ent.getLocation().toVector().subtract(player.getLocation().toVector())).multiply(velocity);
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
        double damage = getDoubleParam(plugin, item, "damage", 6.0);
        double velocity = getDoubleParam(plugin, item, "velocity", 1.0);

        Arrow arrow = player.launchProjectile(Arrow.class);
        arrow.setMetadata("custom_ability", new FixedMetadataValue(plugin, true));
        arrow.setDamage(damage);
        arrow.setVelocity(arrow.getVelocity().multiply(velocity));
        EffectUtils.fanfare(player.getLocation(),
                new SoundLayer(Sound.ENTITY_ARROW_SHOOT, 1.0f, 1.5f),
                new SoundLayer(Sound.BLOCK_BAMBOO_PLACE, 0.6f, 1.3f));
        EffectUtils.burst(player.getEyeLocation(),
                new Layer(Particle.COMPOSTER, 6, 0.1, 0.1, 0.1, 0.0),
                new Layer(Particle.CRIT, 6, 0.1, 0.1, 0.1, 0.1));
        return true;
    }
}

class Tempest extends Ability {
    private final ItemEditFull plugin;
    public Tempest(ItemEditFull plugin) { super("tempest", "Tempest", "Strikes lightning in targeted area."); this.plugin = plugin; }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double damage = getDoubleParam(plugin, item, "damage", 5.0);
        int fireTicks = getIntParam(plugin, item, "fire_ticks", 40);
        double range = getDoubleParam(plugin, item, "range", 25.0);

        Block target = player.getTargetBlockExact((int) range);
        if (target == null) return false;
        Location loc = target.getLocation();
        EffectUtils.fanfare(loc,
                new SoundLayer(Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.5f, 0.8f),
                new SoundLayer(Sound.ITEM_ELYTRA_FLYING, 0.6f, 0.5f));
        EffectUtils.burst(loc.clone().add(0, 1, 0),
                new Layer(Particle.ELECTRIC_SPARK, 25, 2.5, 1.0, 2.5, 0.1),
                new Layer(Particle.SMOKE_LARGE, 10, 2.0, 0.5, 2.0, 0.05));
        for (int i = 0; i < 3; i++) {
            new CompatRunnable() {
                @Override
                public void run() {
                    Location strikeLoc = loc.clone().add((Math.random()-0.5)*5.0, 0, (Math.random()-0.5)*5.0);
                    strikeLoc.getWorld().strikeLightningEffect(strikeLoc);
                    EffectUtils.burst(strikeLoc,
                            new Layer(Particle.FLAME, 12, 0.5, 0.3, 0.5, 0.05),
                            new Layer(Particle.ELECTRIC_SPARK, 15, 0.5, 0.5, 0.5, 0.1));
                    for (Entity entity : strikeLoc.getWorld().getNearbyEntities(strikeLoc, 3.0, 3.0, 3.0)) {
                        if (entity instanceof LivingEntity && !entity.equals(player)) {
                            ((LivingEntity) entity).damage(damage, player);
                            entity.setFireTicks(fireTicks);
                        }
                    }
                }
            }.runTaskLater(plugin, loc, i * 15L);
        }
        return true;
    }
}
