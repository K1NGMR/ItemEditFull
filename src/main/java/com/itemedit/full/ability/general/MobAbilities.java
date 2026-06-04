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
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class MobAbilities implements Listener {
    private static final Map<UUID, Long> activeVenomousStrikes = new HashMap<>();
    private static final Map<UUID, Long> activeWitherShields = new HashMap<>();
    private static ItemEditFull pluginInstance;

    public static void register(ItemEditFull plugin) {
        pluginInstance = plugin;
        
        // Spiders (5)
        plugin.getAbilityManager().registerAbility(new SpiderWeb(plugin));
        plugin.getAbilityManager().registerAbility(new ArachnidJump(plugin));
        plugin.getAbilityManager().registerAbility(new VenomousStrike(plugin));
        plugin.getAbilityManager().registerAbility(new SpiderClimbTrigger(plugin));
        plugin.getAbilityManager().registerAbility(new SpiderNest(plugin));

        // Cave Spiders (5)
        plugin.getAbilityManager().registerAbility(new NeurotoxinBite(plugin));
        plugin.getAbilityManager().registerAbility(new CaveSpiderLeap(plugin));
        plugin.getAbilityManager().registerAbility(new VenomSpit(plugin));
        plugin.getAbilityManager().registerAbility(new ScurryingSpeed(plugin));
        plugin.getAbilityManager().registerAbility(new CaveSpiderNest(plugin));

        // Ender Dragon (5)
        plugin.getAbilityManager().registerAbility(new DragonBreathVolley(plugin));
        plugin.getAbilityManager().registerAbility(new DragonWingBuffet(plugin));
        plugin.getAbilityManager().registerAbility(new DragonRoar(plugin));
        plugin.getAbilityManager().registerAbility(new DragonDash(plugin));
        plugin.getAbilityManager().registerAbility(new DragonEggBomb(plugin));

        // Wither (5)
        plugin.getAbilityManager().registerAbility(new WitherSkullVolley(plugin));
        plugin.getAbilityManager().registerAbility(new WitherExplosion(plugin));
        plugin.getAbilityManager().registerAbility(new WitherShield(plugin));
        plugin.getAbilityManager().registerAbility(new DecayingPresence(plugin));
        plugin.getAbilityManager().registerAbility(new WitherAbsorption(plugin));

        plugin.getServer().getPluginManager().registerEvents(new MobAbilities(), plugin);
    }

    public static void registerVenomousStrike(UUID uuid, long expire) {
        activeVenomousStrikes.put(uuid, expire);
    }

    public static void registerWitherShield(UUID uuid, long expire) {
        activeWitherShields.put(uuid, expire);
    }

    @EventHandler
    public void onVenomousHit(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof LivingEntity) {
            Player player = (Player) event.getDamager();
            Long expire = activeVenomousStrikes.get(player.getUniqueId());
            if (expire != null && System.currentTimeMillis() < expire) {
                activeVenomousStrikes.remove(player.getUniqueId());
                LivingEntity target = (LivingEntity) event.getEntity();
                target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 1));
                target.getWorld().playSound(target.getLocation(), Sound.ENTITY_SPIDER_DEATH, 0.8f, 1.2f);
            }
        }
    }

    @EventHandler
    public void onWitherShieldDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            Long expire = activeWitherShields.get(player.getUniqueId());
            if (expire != null && System.currentTimeMillis() < expire) {
                if (event.getDamager() instanceof Projectile) {
                    event.setCancelled(true);
                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_BREAK_BLOCK, 0.8f, 1.5f);
                } else {
                    event.setDamage(event.getDamage() * 0.5); // 50% damage reduction
                }
            }
        }
    }

    @EventHandler
    public void onMobProjectileHit(ProjectileHitEvent event) {
        Projectile entity = event.getEntity();
        if (entity.hasMetadata("spider_web")) {
            Location loc = event.getHitBlock() != null ? event.getHitBlock().getLocation().add(0, 1, 0) : event.getHitEntity().getLocation();
            Block block = loc.getBlock();
            if (block.getType() == Material.AIR) {
                block.setType(Material.COBWEB);
                new BukkitRunnable() {
                    @Override public void run() { if (block.getType() == Material.COBWEB) block.setType(Material.AIR); }
                }.runTaskLater(pluginInstance, 60L); // 3 seconds
            }
        } else if (entity.hasMetadata("venom_spit")) {
            Location loc = event.getHitBlock() != null ? event.getHitBlock().getLocation() : event.getEntity().getLocation();
            loc.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, loc, 10, 0.2, 0.2, 0.2, 0.05);
            loc.getWorld().playSound(loc, Sound.BLOCK_SLIME_BLOCK_BREAK, 1.0f, 1.2f);
            for (Entity nearby : loc.getWorld().getNearbyEntities(loc, 3.0, 2.0, 3.0)) {
                if (nearby instanceof LivingEntity && !nearby.equals(entity.getShooter())) {
                    ((LivingEntity) nearby).addPotionEffect(new PotionEffect(PotionEffectType.POISON, 80, 1));
                }
            }
        } else if (entity.hasMetadata("dragon_egg")) {
            Location loc = event.getHitBlock() != null ? event.getHitBlock().getLocation() : event.getEntity().getLocation();
            loc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, loc, 3, 0.3, 0.3, 0.3, 0.1);
            loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.8f, 0.8f);
            loc.getWorld().playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.5f, 0.8f);
            for (Entity vic : loc.getWorld().getNearbyEntities(loc, 5.0, 3.0, 5.0)) {
                if (vic instanceof LivingEntity && !vic.equals(entity.getShooter())) {
                    ((LivingEntity) vic).damage(10.0, (Player) entity.getShooter());
                    vic.setVelocity(vic.getLocation().toVector().subtract(loc.toVector()).normalize().multiply(1.5).setY(0.4));
                }
            }
        }
    }
}

// ==================== SPIDER ABILITIES ====================

class SpiderWeb extends Ability {
    private final ItemEditFull plugin;
    public SpiderWeb(ItemEditFull plugin) { super("spider_web", "Spider Web", "Fires a web projectile trapping entities in cobwebs."); this.plugin = plugin; }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        Snowball ball = player.launchProjectile(Snowball.class);
        ball.setMetadata("spider_web", new FixedMetadataValue(plugin, true));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_SPIDER_STEP, 1.0f, 1.0f);
        return true;
    }
}

class ArachnidJump extends Ability {
    private final ItemEditFull plugin;
    public ArachnidJump(ItemEditFull plugin) { super("arachnid_jump", "Arachnid Jump", "Pounce forward, poisoning entities near landing."); this.plugin = plugin; }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_SPIDER_AMBIENT, 1.0f, 1.2f);
        Vector dir = player.getLocation().getDirection().setY(0.35).normalize().multiply(1.4);
        player.setVelocity(dir);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline()) {
                    Location loc = player.getLocation();
                    loc.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, loc, 10, 1.5, 0.2, 1.5, 0.01);
                    for (Entity ent : loc.getWorld().getNearbyEntities(loc, 2.5, 2.0, 2.5)) {
                        if (ent instanceof LivingEntity && !ent.equals(player)) {
                            ((LivingEntity) ent).addPotionEffect(new PotionEffect(PotionEffectType.POISON, 60, 0));
                        }
                    }
                }
            }
        }.runTaskLater(plugin, 10L);
        return true;
    }
}

class VenomousStrike extends Ability {
    private final ItemEditFull plugin;
    public VenomousStrike(ItemEditFull plugin) { super("venomous_strike", "Venomous Strike", "Your next melee hit inflicts poison."); this.plugin = plugin; }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_SPIDER_AMBIENT, 1.0f, 0.9f);
        MobAbilities.registerVenomousStrike(player.getUniqueId(), System.currentTimeMillis() + 10000);
        return true;
    }
}

class SpiderClimbTrigger extends Ability {
    private final ItemEditFull plugin;
    public SpiderClimbTrigger(ItemEditFull plugin) { super("spider_climb_passive", "Spider Wallclimb", "Climb walls easily."); this.plugin = plugin; }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_SPIDER_STEP, 1.0f, 1.5f);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 1));
        return true;
    }
}

class SpiderNest extends Ability {
    private final ItemEditFull plugin;
    public SpiderNest(ItemEditFull plugin) { super("spider_nest", "Spider Nest", "Summons 2 spiders."); this.plugin = plugin; }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_SPIDER_AMBIENT, 1.0f, 1.0f);
        for (int i = 0; i < 2; i++) {
            Spider s = (Spider) player.getWorld().spawnEntity(player.getLocation().add((Math.random()-0.5)*2, 0, (Math.random()-0.5)*2), EntityType.SPIDER);
            s.setMetadata("helper", new FixedMetadataValue(plugin, "true"));
            new BukkitRunnable() {
                @Override public void run() { if (s.isValid()) s.remove(); }
            }.runTaskLater(plugin, 300L);
        }
        return true;
    }
}

// ==================== CAVE SPIDER ABILITIES ====================

class NeurotoxinBite extends Ability {
    private final ItemEditFull plugin;
    public NeurotoxinBite(ItemEditFull plugin) { super("neurotoxin_bite", "Neurotoxin Bite", "Inflicts Nausea, Slowness, and Poison."); this.plugin = plugin; }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        Entity target = player.getTargetEntity(5);
        if (!(target instanceof LivingEntity)) return false;
        LivingEntity living = (LivingEntity) target;
        living.damage(3.0, player);
        living.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 1));
        living.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 160, 0));
        living.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 1));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_SPIDER_AMBIENT, 1.0f, 1.0f);
        return true;
    }
}

class CaveSpiderLeap extends Ability {
    private final ItemEditFull plugin;
    public CaveSpiderLeap(ItemEditFull plugin) { super("cave_spider_leap", "Cave Spider Leap", "Forward pounce, poisoning target."); this.plugin = plugin; }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_SPIDER_AMBIENT, 1.0f, 1.2f);
        player.setVelocity(player.getLocation().getDirection().setY(0.3).normalize().multiply(1.3));
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline()) {
                    Entity target = player.getTargetEntity(3);
                    if (target instanceof LivingEntity) {
                        ((LivingEntity) target).addPotionEffect(new PotionEffect(PotionEffectType.POISON, 80, 1));
                        ((LivingEntity) target).damage(2.0, player);
                    }
                }
            }
        }.runTaskLater(plugin, 8L);
        return true;
    }
}

class VenomSpit extends Ability {
    private final ItemEditFull plugin;
    public VenomSpit(ItemEditFull plugin) { super("venom_spit", "Venom Spit", "Fires a spit splash of poison."); this.plugin = plugin; }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        Snowball spit = player.launchProjectile(Snowball.class);
        spit.setMetadata("venom_spit", new FixedMetadataValue(plugin, true));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_LLAMA_SPIT, 1.0f, 1.5f);
        return true;
    }
}

class ScurryingSpeed extends Ability {
    private final ItemEditFull plugin;
    public ScurryingSpeed(ItemEditFull plugin) { super("scurrying_speed", "Scurrying Speed", "Speed III boost."); this.plugin = plugin; }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_SPIDER_STEP, 1.0f, 1.8f);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 2));
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 100, 1));
        return true;
    }
}

class CaveSpiderNest extends Ability {
    private final ItemEditFull plugin;
    public CaveSpiderNest(ItemEditFull plugin) { super("cave_spider_nest", "Cave Spider Nest", "Summons 2 cave spiders."); this.plugin = plugin; }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_SPIDER_AMBIENT, 1.0f, 1.0f);
        for (int i = 0; i < 2; i++) {
            CaveSpider s = (CaveSpider) player.getWorld().spawnEntity(player.getLocation().add((Math.random()-0.5)*2, 0, (Math.random()-0.5)*2), EntityType.CAVE_SPIDER);
            s.setMetadata("helper", new FixedMetadataValue(plugin, "true"));
            new BukkitRunnable() {
                @Override public void run() { if (s.isValid()) s.remove(); }
            }.runTaskLater(plugin, 300L);
        }
        return true;
    }
}

// ==================== ENDER DRAGON ABILITIES ====================

class DragonBreathVolley extends Ability {
    private final ItemEditFull plugin;
    public DragonBreathVolley(ItemEditFull plugin) { super("dragon_breath_volley", "Dragon Volley", "Launches 3 dragon fireballs."); this.plugin = plugin; }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        Vector dir = player.getEyeLocation().getDirection().normalize();
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_SHOOT, 1.2f, 0.8f);
        for (int i = -1; i <= 1; i++) {
            double angleRad = Math.toRadians(i * 10);
            double cos = Math.cos(angleRad);
            double sin = Math.sin(angleRad);
            double x = dir.getX() * cos - dir.getZ() * sin;
            double z = dir.getX() * sin + dir.getZ() * cos;
            Vector spreadDir = new Vector(x, dir.getY(), z);
            DragonFireball df = player.launchProjectile(DragonFireball.class);
            df.setVelocity(spreadDir.multiply(1.5));
        }
        return true;
    }
}

class DragonWingBuffet extends Ability {
    private final ItemEditFull plugin;
    public DragonWingBuffet(ItemEditFull plugin) { super("dragon_wing_buffet", "Wing Buffet", "Pushes back all entities in a front cone."); this.plugin = plugin; }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        Location loc = player.getEyeLocation();
        Vector dir = loc.getDirection().normalize();
        player.getWorld().playSound(loc, Sound.ENTITY_ENDER_DRAGON_FLAP, 1.5f, 0.9f);
        for (Entity ent : player.getWorld().getNearbyEntities(player.getLocation(), 8.0, 4.0, 8.0)) {
            if (ent instanceof LivingEntity && !ent.equals(player)) {
                Vector toEntity = ent.getLocation().toVector().subtract(player.getLocation().toVector());
                if (toEntity.normalize().dot(dir) > 0.6) {
                    ((LivingEntity) ent).damage(4.0, player);
                    ent.setVelocity(dir.clone().multiply(2.0).setY(0.5));
                }
            }
        }
        return true;
    }
}

class DragonRoar extends Ability {
    private final ItemEditFull plugin;
    public DragonRoar(ItemEditFull plugin) { super("dragon_roar", "Dragon Roar", "Roars, dealing magic damage and wither."); this.plugin = plugin; }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        Location loc = player.getLocation();
        player.getWorld().playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.5f, 0.7f);
        loc.getWorld().spawnParticle(Particle.DRAGON_BREATH, loc, 30, 3.0, 1.0, 3.0, 0.05);
        for (Entity ent : player.getNearbyEntities(6.0, 3.0, 6.0)) {
            if (ent instanceof LivingEntity && !ent.equals(player)) {
                LivingEntity living = (LivingEntity) ent;
                living.damage(6.0, player);
                living.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 100, 1));
            }
        }
        return true;
    }
}

class DragonDash extends Ability {
    private final ItemEditFull plugin;
    public DragonDash(ItemEditFull plugin) { super("dragon_dash", "Dragon Dash", "Dashes forward at hypersonic speed."); this.plugin = plugin; }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        Location loc = player.getLocation();
        Vector dir = loc.getDirection().setY(0.1).normalize().multiply(2.0);
        player.setVelocity(dir);
        player.getWorld().playSound(loc, Sound.ENTITY_ENDER_DRAGON_FLAP, 1.5f, 1.2f);
        new BukkitRunnable() {
            int step = 0;
            @Override
            public void run() {
                if (step > 6 || !player.isOnline()) { cancel(); return; }
                player.getWorld().spawnParticle(Particle.DRAGON_BREATH, player.getLocation(), 5, 0.2, 0.2, 0.2, 0);
                for (Entity ent : player.getNearbyEntities(2.0, 2.0, 2.0)) {
                    if (ent instanceof LivingEntity && !ent.equals(player)) {
                        ((LivingEntity) ent).damage(6.0, player);
                        ent.setVelocity(dir.clone().multiply(0.8).setY(0.4));
                    }
                }
                step++;
            }
        }.runTaskTimer(plugin, 0L, 2L);
        return true;
    }
}

class DragonEggBomb extends Ability {
    private final ItemEditFull plugin;
    public DragonEggBomb(ItemEditFull plugin) { super("dragon_egg_bomb", "Dragon Egg Bomb", "Launches a dragon egg that explodes."); this.plugin = plugin; }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        Snowball egg = player.launchProjectile(Snowball.class);
        egg.setMetadata("dragon_egg", new FixedMetadataValue(plugin, true));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_SHOOT, 1.0f, 1.2f);
        return true;
    }
}

// ==================== WITHER ABILITIES ====================

class WitherSkullVolley extends Ability {
    private final ItemEditFull plugin;
    public WitherSkullVolley(ItemEditFull plugin) { super("wither_skull_volley", "Wither Volley", "Launches 3 wither skulls."); this.plugin = plugin; }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        Vector dir = player.getEyeLocation().getDirection().normalize();
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_SHOOT, 1.0f, 1.0f);
        for (int i = -1; i <= 1; i++) {
            double angleRad = Math.toRadians(i * 10);
            double cos = Math.cos(angleRad);
            double sin = Math.sin(angleRad);
            double x = dir.getX() * cos - dir.getZ() * sin;
            double z = dir.getX() * sin + dir.getZ() * cos;
            Vector spreadDir = new Vector(x, dir.getY(), z);
            WitherSkull ws = player.launchProjectile(WitherSkull.class);
            ws.setVelocity(spreadDir.multiply(1.5));
            ws.setMetadata("custom_ability", new FixedMetadataValue(plugin, true));
        }
        return true;
    }
}

class WitherExplosion extends Ability {
    private final ItemEditFull plugin;
    public WitherExplosion(ItemEditFull plugin) { super("wither_explosion", "Wither Explosion", "Simulates wither birth explosion."); this.plugin = plugin; }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        Location loc = player.getLocation();
        loc.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, loc, 5, 0.2, 0.2, 0.2, 0.1);
        loc.getWorld().playSound(loc, Sound.ENTITY_WITHER_SPAWN, 1.5f, 1.0f);
        for (Entity ent : player.getNearbyEntities(6.0, 3.0, 6.0)) {
            if (ent instanceof LivingEntity && !ent.equals(player)) {
                ((LivingEntity) ent).damage(10.0, player);
                ent.setVelocity(ent.getLocation().toVector().subtract(loc.toVector()).normalize().multiply(1.8).setY(0.5));
                ((LivingEntity) ent).addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 160, 1));
            }
        }
        return true;
    }
}

class WitherShield extends Ability {
    private final ItemEditFull plugin;
    public WitherShield(ItemEditFull plugin) { super("wither_shield", "Wither Shield", "Projectile immunity and 50% defense."); this.plugin = plugin; }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 8.0);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_AMBIENT, 1.2f, 1.0f);
        MobAbilities.registerWitherShield(player.getUniqueId(), System.currentTimeMillis() + (long)(duration*1000));
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks >= (duration*20) || !player.isOnline()) { cancel(); return; }
                player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation().add(0, 1, 0), 4, 0.4, 0.5, 0.4, 0.01);
                ticks += 5;
            }
        }.runTaskTimer(plugin, 0L, 5L);
        return true;
    }
}

class DecayingPresence extends Ability {
    private final ItemEditFull plugin;
    public DecayingPresence(ItemEditFull plugin) { super("decaying_presence", "Decaying Presence", "Wither aura."); this.plugin = plugin; }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double radius = getDoubleParam(plugin, item, "radius", 5.0);
        double duration = getDoubleParam(plugin, item, "duration", 10.0);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_HURT, 1.0f, 0.5f);
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks >= (duration*20) || !player.isOnline()) { cancel(); return; }
                Location loc = player.getLocation();
                loc.getWorld().spawnParticle(Particle.SMOKE_NORMAL, loc, 15, radius*0.5, 0.5, radius*0.5, 0.02);
                for (Entity ent : player.getNearbyEntities(radius, 3.0, radius)) {
                    if (ent instanceof LivingEntity && !ent.equals(player)) {
                        ((LivingEntity) ent).addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 60, 1));
                    }
                }
                ticks += 20;
            }
        }.runTaskTimer(plugin, 0L, 20L);
        return true;
    }
}

class WitherAbsorption extends Ability {
    private final ItemEditFull plugin;
    public WitherAbsorption(ItemEditFull plugin) { super("wither_absorption", "Wither Absorption", "Drains health from nearby entities to heal you."); this.plugin = plugin; }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double radius = getDoubleParam(plugin, item, "radius", 5.0);
        double damage = getDoubleParam(plugin, item, "drain_damage", 4.0);
        Location loc = player.getLocation();
        player.getWorld().playSound(loc, Sound.ENTITY_WITHER_SPAWN, 0.8f, 1.8f);
        int count = 0;
        for (Entity ent : player.getNearbyEntities(radius, 3.0, radius)) {
            if (ent instanceof LivingEntity && !ent.equals(player)) {
                LivingEntity living = (LivingEntity) ent;
                living.damage(damage, player);
                living.getWorld().spawnParticle(Particle.PORTAL, living.getLocation().add(0, 1, 0), 10, 0.2, 0.2, 0.2, 0.05);
                count++;
            }
        }
        if (count > 0) {
            double totalHeal = count * (damage * 0.5);
            player.setHealth(Math.min(player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue(), player.getHealth() + totalHeal));
            player.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0, 1.5, 0), 5, 0.2, 0.2, 0.2, 0.01);
        }
        return true;
    }
}
