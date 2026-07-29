package com.itemedit.full.ability.undead;

import com.itemedit.full.ItemEditFull;
import com.itemedit.full.ability.Ability;
import com.itemedit.full.utils.CompatRunnable;
import com.itemedit.full.utils.VectorUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
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
 * The Rotting Legion expansion: 20 additional decay/undead-themed abilities layered
 * on top of the original 5 zombie abilities.
 */
public class ZombieExpansionAbilities implements Listener {

    // ---- Decayed Armor: multiplies damage taken while active ----
    private static final Map<UUID, Long> decayActive = new ConcurrentHashMap<>();
    private static final Map<UUID, Double> decayMultiplier = new ConcurrentHashMap<>();

    // ---- Corpse Explosion: remembers each player's most recent kill ----
    private static final Map<UUID, Location> lastKillLocation = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> lastKillTime = new ConcurrentHashMap<>();

    // ---- Undying Shamble: survives fatal damage once ----
    private static final Map<UUID, Long> shambleReady = new ConcurrentHashMap<>();

    private static ItemEditFull pluginInstance;

    public static void register(ItemEditFull plugin) {
        pluginInstance = plugin;

        plugin.getAbilityManager().registerAbility(new RottingGrasp(plugin));
        plugin.getAbilityManager().registerAbility(new PlagueBreath(plugin));
        plugin.getAbilityManager().registerAbility(new GraveRise(plugin));
        plugin.getAbilityManager().registerAbility(new DecayedArmor(plugin));
        plugin.getAbilityManager().registerAbility(new FesteringWound(plugin));
        plugin.getAbilityManager().registerAbility(new ShamblingHorde(plugin));
        plugin.getAbilityManager().registerAbility(new CorpseExplosion(plugin));
        plugin.getAbilityManager().registerAbility(new HungerOfTheGrave(plugin));
        plugin.getAbilityManager().registerAbility(new MoanOfDread(plugin));
        plugin.getAbilityManager().registerAbility(new GraveDirtThrow(plugin));
        plugin.getAbilityManager().registerAbility(new PutridCloud(plugin));
        plugin.getAbilityManager().registerAbility(new FleshRegeneration(plugin));
        plugin.getAbilityManager().registerAbility(new DeadweightPull(plugin));
        plugin.getAbilityManager().registerAbility(new RottenFeast(plugin));
        plugin.getAbilityManager().registerAbility(new GraveyardShift(plugin));
        plugin.getAbilityManager().registerAbility(new HuskTransformation(plugin));
        plugin.getAbilityManager().registerAbility(new NightStalker(plugin));
        plugin.getAbilityManager().registerAbility(new UndyingShamble(plugin));
        plugin.getAbilityManager().registerAbility(new InfectiousSwarm(plugin));
        plugin.getAbilityManager().registerAbility(new BoneDeepChill(plugin));

        plugin.getServer().getPluginManager().registerEvents(new ZombieExpansionAbilities(), plugin);
    }

    static void activateDecay(UUID uuid, long expireAt, double multiplier) {
        decayActive.put(uuid, expireAt);
        decayMultiplier.put(uuid, multiplier);
    }

    static Location getRecentKillLocation(UUID uuid) {
        Long time = lastKillTime.get(uuid);
        if (time == null || System.currentTimeMillis() - time > 15000) {
            return null;
        }
        return lastKillLocation.get(uuid);
    }

    static void armShamble(UUID uuid, long expireAt) {
        shambleReady.put(uuid, expireAt);
    }

    @EventHandler
    public void onKill(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer != null) {
            lastKillLocation.put(killer.getUniqueId(), event.getEntity().getLocation());
            lastKillTime.put(killer.getUniqueId(), System.currentTimeMillis());
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDecayDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity();
        Long expire = decayActive.get(player.getUniqueId());
        if (expire == null) {
            return;
        }
        if (System.currentTimeMillis() >= expire) {
            decayActive.remove(player.getUniqueId());
            decayMultiplier.remove(player.getUniqueId());
            return;
        }
        double multiplier = decayMultiplier.getOrDefault(player.getUniqueId(), 1.3);
        event.setDamage(event.getDamage() * multiplier);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFatalDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity();
        Long expire = shambleReady.get(player.getUniqueId());
        if (expire == null || System.currentTimeMillis() >= expire) {
            return;
        }
        double finalDamage = event.getFinalDamage();
        if (player.getHealth() - finalDamage > 0) {
            return;
        }
        shambleReady.remove(player.getUniqueId());
        event.setCancelled(true);
        player.setHealth(1.0);
        player.setFireTicks(0);
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60, 1));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED, 1.0f, 0.6f);
        player.getWorld().spawnParticle(Particle.SOUL, player.getLocation().add(0, 1, 0), 30, 0.4, 0.6, 0.4, 0.05);
        player.sendMessage("§2Your shambling will refuses to let you die!");
    }

    @EventHandler
    public void onGraveDirtHit(ProjectileHitEvent event) {
        if (!event.getEntity().hasMetadata("grave_dirt")) {
            return;
        }
        Location loc = event.getHitBlock() != null ? event.getHitBlock().getLocation().add(0, 1, 0) : event.getEntity().getLocation();
        loc.getWorld().spawnParticle(Particle.FALLING_DUST, loc, 20, 0.3, 0.3, 0.3, 0, Material.COARSE_DIRT.createBlockData());
        loc.getWorld().playSound(loc, Sound.BLOCK_GRAVEL_BREAK, 1.0f, 0.7f);
        double blindDuration = event.getEntity().hasMetadata("blind_duration") ? event.getEntity().getMetadata("blind_duration").get(0).asDouble() : 3.0;
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, 2.5, 2.0, 2.5)) {
            if (entity instanceof LivingEntity) {
                ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, (int) (blindDuration * 20), 0));
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        decayActive.remove(id);
        decayMultiplier.remove(id);
        lastKillLocation.remove(id);
        lastKillTime.remove(id);
        shambleReady.remove(id);
    }
}

// ==================== 20 NEW ROTTING LEGION ABILITIES ====================

class RottingGrasp extends Ability {
    private final ItemEditFull plugin;
    public RottingGrasp(ItemEditFull plugin) {
        super("rotting_grasp", "Rotting Grasp", "Your grip infects the target, dealing poison damage over time.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 6.0);
        int amplifier = getIntParam(plugin, item, "amplifier", 1);
        Entity target = player.getTargetEntity(4);
        if (!(target instanceof LivingEntity)) {
            player.sendMessage("§cNo target in range.");
            return false;
        }
        LivingEntity living = (LivingEntity) target;
        living.addPotionEffect(new PotionEffect(PotionEffectType.POISON, (int) (duration * 20), amplifier));
        living.getWorld().playSound(living.getLocation(), Sound.ENTITY_ZOMBIE_INFECT, 1.0f, 0.8f);
        living.getWorld().spawnParticle(Particle.VILLAGER_ANGRY, living.getLocation().add(0, 1, 0), 6, 0.2, 0.3, 0.2, 0);
        return true;
    }
}

class PlagueBreath extends Ability {
    private final ItemEditFull plugin;
    public PlagueBreath(ItemEditFull plugin) {
        super("plague_breath", "Plague Breath", "Exhale a cloud of rot that nauseates and poisons nearby enemies.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double radius = getDoubleParam(plugin, item, "radius", 4.0);
        double duration = getDoubleParam(plugin, item, "duration", 5.0);
        Location loc = player.getLocation();
        loc.getWorld().playSound(loc, Sound.ENTITY_ZOMBIE_AMBIENT, 1.2f, 0.6f);
        loc.getWorld().spawnParticle(Particle.VILLAGER_ANGRY, loc.add(0, 1, 0), 20, radius * 0.3, 0.5, radius * 0.3, 0);
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, radius, 2.0, radius)) {
            if (entity instanceof LivingEntity && !entity.equals(player)) {
                LivingEntity living = (LivingEntity) entity;
                living.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, (int) (duration * 20), 0));
                living.addPotionEffect(new PotionEffect(PotionEffectType.POISON, (int) (duration * 20), 0));
            }
        }
        return true;
    }
}

class GraveRise extends Ability {
    private final ItemEditFull plugin;
    public GraveRise(ItemEditFull plugin) {
        super("grave_rise", "Grave Rise", "Summons a temporary zombie ally to fight beside you.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double lifetime = getDoubleParam(plugin, item, "lifetime", 25.0);
        double health = getDoubleParam(plugin, item, "health", 20.0);
        Location loc = player.getLocation();
        Zombie zombie = (Zombie) loc.getWorld().spawnEntity(loc, EntityType.ZOMBIE);
        zombie.setCustomName("§2" + player.getName() + "'s Risen Ally");
        zombie.setCustomNameVisible(true);
        zombie.setMetadata("helper", new FixedMetadataValue(plugin, player.getUniqueId().toString()));
        if (zombie.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            zombie.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health);
        }
        zombie.setHealth(Math.min(health, zombie.getHealth()));
        loc.getWorld().playSound(loc, Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 1.0f, 0.7f);
        loc.getWorld().spawnParticle(Particle.SOUL, loc.add(0, 1, 0), 25, 0.4, 0.6, 0.4, 0.05);
        for (Entity entity : zombie.getNearbyEntities(12, 6, 12)) {
            if (entity instanceof LivingEntity && !(entity instanceof Player) && !entity.hasMetadata("helper")) {
                zombie.setTarget((LivingEntity) entity);
                break;
            }
        }
        new CompatRunnable() {
            @Override
            public void run() {
                if (zombie.isValid()) {
                    zombie.getWorld().spawnParticle(Particle.SMOKE_NORMAL, zombie.getLocation().add(0, 1, 0), 10, 0.2, 0.3, 0.2, 0.01);
                    zombie.remove();
                }
            }
        }.runTaskLater(plugin, player, (long) (lifetime * 20));
        return true;
    }
}

class DecayedArmor extends Ability {
    private final ItemEditFull plugin;
    public DecayedArmor(ItemEditFull plugin) {
        super("decayed_armor", "Decayed Armor", "Curses a target's equipment, making them take extra damage from all sources.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 8.0);
        double multiplier = getDoubleParam(plugin, item, "damage_multiplier", 1.3);
        Entity target = player.getTargetEntity(8);
        if (!(target instanceof Player)) {
            player.sendMessage("§cNo player target in sight.");
            return false;
        }
        Player targetPlayer = (Player) target;
        ZombieExpansionAbilities.activateDecay(targetPlayer.getUniqueId(), System.currentTimeMillis() + (long) (duration * 1000), multiplier);
        targetPlayer.getWorld().playSound(targetPlayer.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 1.0f, 0.7f);
        targetPlayer.getWorld().spawnParticle(Particle.VILLAGER_ANGRY, targetPlayer.getLocation().add(0, 1.5, 0), 8, 0.3, 0.3, 0.3, 0);
        targetPlayer.sendMessage("§2Your armor decays under a rotting curse!");
        return true;
    }
}

class FesteringWound extends Ability {
    private final ItemEditFull plugin;
    public FesteringWound(ItemEditFull plugin) {
        super("festering_wound", "Festering Wound", "Strikes leave a wound that worsens the longer it goes untreated.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double dotDuration = getDoubleParam(plugin, item, "dot_duration", 8.0);
        double baseDamage = getDoubleParam(plugin, item, "dot_damage", 1.0);
        Entity target = player.getTargetEntity(4);
        if (!(target instanceof LivingEntity)) {
            player.sendMessage("§cNo target in range.");
            return false;
        }
        LivingEntity living = (LivingEntity) target;
        living.getWorld().playSound(living.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 0.8f, 0.6f);
        new CompatRunnable() {
            int ticksElapsed = 0;
            int stack = 1;
            @Override
            public void run() {
                if (!living.isValid() || living.isDead() || ticksElapsed >= (dotDuration * 20)) {
                    cancel();
                    return;
                }
                living.damage(baseDamage * stack, player);
                living.getWorld().spawnParticle(Particle.VILLAGER_ANGRY, living.getLocation().add(0, 1, 0), 2, 0.2, 0.2, 0.2, 0);
                stack = Math.min(stack + 1, 4);
                ticksElapsed += 40;
            }
        }.runTaskTimer(plugin, living, 40L, 40L);
        return true;
    }
}

class ShamblingHorde extends Ability {
    private final ItemEditFull plugin;
    public ShamblingHorde(ItemEditFull plugin) {
        super("shambling_horde", "Shambling Horde", "Summons three weak zombie minions that swarm your target.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        int count = getIntParam(plugin, item, "count", 3);
        double lifetime = getDoubleParam(plugin, item, "lifetime", 15.0);
        Location loc = player.getLocation();
        loc.getWorld().playSound(loc, Sound.ENTITY_ZOMBIE_AMBIENT, 1.2f, 1.3f);
        for (int i = 0; i < count; i++) {
            double angle = i * 2 * Math.PI / count;
            Location spawnLoc = loc.clone().add(Math.cos(angle) * 1.5, 0, Math.sin(angle) * 1.5);
            Zombie zombie = (Zombie) spawnLoc.getWorld().spawnEntity(spawnLoc, EntityType.ZOMBIE);
            zombie.setBaby(true);
            zombie.setMetadata("helper", new FixedMetadataValue(plugin, player.getUniqueId().toString()));
            if (zombie.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
                zombie.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(8.0);
            }
            zombie.setHealth(8.0);
            for (Entity entity : zombie.getNearbyEntities(10, 5, 10)) {
                if (entity instanceof LivingEntity && !(entity instanceof Player) && !entity.hasMetadata("helper")) {
                    zombie.setTarget((LivingEntity) entity);
                    break;
                }
            }
            new CompatRunnable() {
                @Override
                public void run() {
                    if (zombie.isValid()) {
                        zombie.getWorld().spawnParticle(Particle.SMOKE_NORMAL, zombie.getLocation().add(0, 1, 0), 6, 0.2, 0.2, 0.2, 0.01);
                        zombie.remove();
                    }
                }
            }.runTaskLater(plugin, player, (long) (lifetime * 20));
        }
        return true;
    }
}

class CorpseExplosion extends Ability {
    private final ItemEditFull plugin;
    public CorpseExplosion(ItemEditFull plugin) {
        super("corpse_explosion", "Corpse Explosion", "Detonates a nearby dead mob's corpse, damaging enemies around it.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double radius = getDoubleParam(plugin, item, "radius", 4.0);
        double damage = getDoubleParam(plugin, item, "damage", 6.0);
        Location recent = ZombieExpansionAbilities.getRecentKillLocation(player.getUniqueId());
        Location loc = (recent != null && recent.distanceSquared(player.getLocation()) <= 400) ? recent : player.getLocation();
        loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.2f, 0.8f);
        loc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, loc, 2, 0.2, 0.2, 0.2, 0);
        loc.getWorld().spawnParticle(Particle.VILLAGER_ANGRY, loc, 15, radius * 0.3, 0.4, radius * 0.3, 0);
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
            if (entity instanceof LivingEntity && !entity.equals(player)) {
                LivingEntity living = (LivingEntity) entity;
                living.damage(damage, player);
                living.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 60, 0));
            }
        }
        return true;
    }
}

class HungerOfTheGrave extends Ability {
    private final ItemEditFull plugin;
    public HungerOfTheGrave(ItemEditFull plugin) {
        super("hunger_of_the_grave", "Hunger of the Grave", "Drains hunger from a struck target and restores your own.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        int hungerDrain = getIntParam(plugin, item, "hunger_drain", 6);
        Entity target = player.getTargetEntity(4);
        if (!(target instanceof Player)) {
            player.sendMessage("§cNo player target in sight.");
            return false;
        }
        Player targetPlayer = (Player) target;
        targetPlayer.setFoodLevel(Math.max(0, targetPlayer.getFoodLevel() - hungerDrain));
        player.setFoodLevel(Math.min(20, player.getFoodLevel() + hungerDrain));
        player.setSaturation(Math.min(20.0f, player.getSaturation() + hungerDrain));
        targetPlayer.getWorld().playSound(targetPlayer.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_HURT, 1.0f, 0.8f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_BURP, 1.0f, 1.0f);
        return true;
    }
}

class MoanOfDread extends Ability {
    private final ItemEditFull plugin;
    public MoanOfDread(ItemEditFull plugin) {
        super("moan_of_dread", "Moan of Dread", "A groaning wail that weakens nearby enemies' resolve.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double radius = getDoubleParam(plugin, item, "radius", 6.0);
        double duration = getDoubleParam(plugin, item, "duration", 6.0);
        Location loc = player.getLocation();
        loc.getWorld().playSound(loc, Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED, 1.0f, 0.4f);
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
            if (entity instanceof LivingEntity && !entity.equals(player)) {
                ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, (int) (duration * 20), 1));
            }
        }
        return true;
    }
}

class GraveDirtThrow extends Ability {
    private final ItemEditFull plugin;
    public GraveDirtThrow(ItemEditFull plugin) {
        super("grave_dirt_throw", "Grave Dirt Throw", "Throws a clump of grave dirt that blinds on impact.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double blindDuration = getDoubleParam(plugin, item, "blind_duration", 3.0);
        Snowball dirt = player.launchProjectile(Snowball.class);
        dirt.setMetadata("grave_dirt", new FixedMetadataValue(plugin, true));
        dirt.setMetadata("blind_duration", new FixedMetadataValue(plugin, blindDuration));
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_GRAVEL_HIT, 1.0f, 0.8f);
        return true;
    }
}

class PutridCloud extends Ability {
    private final ItemEditFull plugin;
    public PutridCloud(ItemEditFull plugin) {
        super("putrid_cloud", "Putrid Cloud", "Releases a lingering cloud of nausea around you.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double radius = getDoubleParam(plugin, item, "radius", 5.0);
        double duration = getDoubleParam(plugin, item, "duration", 8.0);
        Location center = player.getLocation();
        center.getWorld().playSound(center, Sound.ENTITY_ZOMBIE_AMBIENT, 1.0f, 0.5f);
        new CompatRunnable() {
            int ticksElapsed = 0;
            @Override
            public void run() {
                if (!player.isOnline() || ticksElapsed >= (duration * 20)) {
                    cancel();
                    return;
                }
                center.getWorld().spawnParticle(Particle.VILLAGER_ANGRY, center.clone().add(0, 1, 0), 6, radius * 0.3, 0.4, radius * 0.3, 0);
                if (ticksElapsed % 20 == 0) {
                    for (Entity entity : center.getWorld().getNearbyEntities(center, radius, 2.0, radius)) {
                        if (entity instanceof LivingEntity && !entity.equals(player)) {
                            ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 60, 0));
                        }
                    }
                }
                ticksElapsed += 5;
            }
        }.runTaskTimer(plugin, center, 0L, 5L);
        return true;
    }
}

class FleshRegeneration extends Ability {
    private final ItemEditFull plugin;
    public FleshRegeneration(ItemEditFull plugin) {
        super("flesh_regeneration", "Flesh Regeneration", "Slowly knits your wounds back together over time.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 10.0);
        double healPerTick = getDoubleParam(plugin, item, "heal_tick", 1.0);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 1.0f, 1.2f);
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

class DeadweightPull extends Ability {
    private final ItemEditFull plugin;
    public DeadweightPull(ItemEditFull plugin) {
        super("deadweight_pull", "Deadweight Pull", "Grips a nearby enemy and drags them toward you.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double range = getDoubleParam(plugin, item, "range", 6.0);
        Entity target = player.getTargetEntity((int) range);
        if (!(target instanceof LivingEntity)) {
            player.sendMessage("§cNo target in range.");
            return false;
        }
        LivingEntity living = (LivingEntity) target;
        Vector pull = VectorUtils.safeNormalize(player.getLocation().toVector().subtract(living.getLocation().toVector())).multiply(1.1).setY(0.15);
        living.setVelocity(pull);
        living.getWorld().playSound(living.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR, 1.0f, 0.8f);
        return true;
    }
}

class RottenFeast extends Ability {
    private final ItemEditFull plugin;
    public RottenFeast(ItemEditFull plugin) {
        super("rotten_feast", "Rotten Feast", "Consuming a kill's remains heals you and grants brief resistance.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double heal = getDoubleParam(plugin, item, "heal", 3.0);
        double resistDuration = getDoubleParam(plugin, item, "resist_duration", 4.0);
        double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        player.setHealth(Math.min(maxHealth, player.getHealth() + heal));
        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, (int) (resistDuration * 20), 0));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EAT, 1.0f, 0.7f);
        player.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0, 1.5, 0), 5, 0.2, 0.2, 0.2, 0.01);
        return true;
    }
}

class GraveyardShift extends Ability {
    private final ItemEditFull plugin;
    public GraveyardShift(ItemEditFull plugin) {
        super("graveyard_shift", "Graveyard Shift", "Grants bonus speed and strength during the night.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 10.0);
        long time = player.getWorld().getTime();
        boolean isNight = time >= 13000 && time <= 23000;
        int amplifier = isNight ? 1 : 0;
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, (int) (duration * 20), amplifier));
        player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, (int) (duration * 20), amplifier));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_AMBIENT, 1.0f, isNight ? 0.6f : 1.2f);
        player.sendMessage(isNight ? "§2The graveyard hours empower you!" : "§7You feel the night's pull, though it is not yet dark.");
        return true;
    }
}

class HuskTransformation extends Ability {
    private final ItemEditFull plugin;
    public HuskTransformation(ItemEditFull plugin) {
        super("husk_transformation", "Husk Transformation", "Temporarily grants sun immunity and hunger resistance.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 30.0);
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, (int) (duration * 20), 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, (int) (duration * 20), 0));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_HUSK_AMBIENT, 1.0f, 0.9f);
        player.getWorld().spawnParticle(Particle.FALLING_DUST, player.getLocation().add(0, 1, 0), 20, 0.3, 0.6, 0.3, 0, Material.SAND.createBlockData());
        player.sendMessage("§eYour flesh hardens like sun-baked husk!");
        return true;
    }
}

class NightStalker extends Ability {
    private final ItemEditFull plugin;
    public NightStalker(ItemEditFull plugin) {
        super("night_stalker", "Night Stalker", "Grants brief invisibility when standing in darkness.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 5.0);
        int lightLevel = player.getLocation().getBlock().getLightLevel();
        if (lightLevel > 7) {
            player.sendMessage("§cToo much light here to stalk unseen.");
            return false;
        }
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, (int) (duration * 20), 0));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_AMBIENT, 0.6f, 1.4f);
        return true;
    }
}

class UndyingShamble extends Ability {
    private final ItemEditFull plugin;
    public UndyingShamble(ItemEditFull plugin) {
        super("undying_shamble", "Undying Shamble", "If you'd take fatal damage, survive with one heart instead, once per use.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double window = getDoubleParam(plugin, item, "window", 30.0);
        ZombieExpansionAbilities.armShamble(player.getUniqueId(), System.currentTimeMillis() + (long) (window * 1000));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 1.0f, 0.8f);
        player.getWorld().spawnParticle(Particle.SOUL, player.getLocation().add(0, 1, 0), 20, 0.3, 0.5, 0.3, 0.02);
        player.sendMessage("§2Your grave-bound will guards you against death!");
        return true;
    }
}

class InfectiousSwarm extends Ability {
    private final ItemEditFull plugin;
    public InfectiousSwarm(ItemEditFull plugin) {
        super("infectious_swarm", "Infectious Swarm", "Spreads a slow-acting infection to all enemies within range.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double radius = getDoubleParam(plugin, item, "radius", 5.0);
        double duration = getDoubleParam(plugin, item, "duration", 5.0);
        Location loc = player.getLocation();
        loc.getWorld().playSound(loc, Sound.ENTITY_ZOMBIE_INFECT, 1.0f, 1.0f);
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
            if (entity instanceof LivingEntity && !entity.equals(player)) {
                ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.POISON, (int) (duration * 20), 0));
            }
        }
        return true;
    }
}

class BoneDeepChill extends Ability {
    private final ItemEditFull plugin;
    public BoneDeepChill(ItemEditFull plugin) {
        super("bone_deep_chill", "Bone-Deep Chill", "A cold, dead touch that slows and weakens on contact.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 5.0);
        Entity target = player.getTargetEntity(4);
        if (!(target instanceof LivingEntity)) {
            player.sendMessage("§cNo target in range.");
            return false;
        }
        LivingEntity living = (LivingEntity) target;
        living.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) (duration * 20), 1));
        living.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, (int) (duration * 20), 0));
        living.getWorld().playSound(living.getLocation(), Sound.BLOCK_SNOW_BREAK, 1.0f, 0.6f);
        living.getWorld().spawnParticle(Particle.SNOWFLAKE, living.getLocation().add(0, 1, 0), 10, 0.2, 0.3, 0.2, 0.01);
        return true;
    }
}
