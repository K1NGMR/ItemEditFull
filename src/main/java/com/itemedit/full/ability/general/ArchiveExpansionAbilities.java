package com.itemedit.full.ability.general;

import com.itemedit.full.ItemEditFull;
import com.itemedit.full.ability.Ability;
import com.itemedit.full.utils.CompatRunnable;
import com.itemedit.full.utils.EffectUtils;
import com.itemedit.full.utils.VectorUtils;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Wandering Archive expansion: 20 additional archetype-themed abilities layered
 * on top of the plugin's largest, most eclectic school.
 */
public class ArchiveExpansionAbilities implements Listener {

    // ---- Duelist's Riposte: parry window that counters the next melee hit ----
    private static final Map<UUID, Long> riposteActive = new ConcurrentHashMap<>();
    private static final Map<UUID, Double> riposteCounterDamage = new ConcurrentHashMap<>();

    // ---- Occultist's Soul Link: mirrors a percentage of damage taken onto a linked target ----
    private static final Map<UUID, Long> soulLinkActive = new ConcurrentHashMap<>();
    private static final Map<UUID, UUID> soulLinkTarget = new ConcurrentHashMap<>();
    private static final Map<UUID, Double> soulLinkPercent = new ConcurrentHashMap<>();

    private static ItemEditFull pluginInstance;

    public static void register(ItemEditFull plugin) {
        pluginInstance = plugin;

        plugin.getAbilityManager().registerAbility(new MonksKiPalm(plugin));
        plugin.getAbilityManager().registerAbility(new MonksMeditation(plugin));
        plugin.getAbilityManager().registerAbility(new MonksFlurry(plugin));
        plugin.getAbilityManager().registerAbility(new RangersBearTrap(plugin));
        plugin.getAbilityManager().registerAbility(new RangersSnareNet(plugin));
        plugin.getAbilityManager().registerAbility(new RangersMultishot(plugin));
        plugin.getAbilityManager().registerAbility(new RangersCamouflage(plugin));
        plugin.getAbilityManager().registerAbility(new AlchemistsVolatileFlask(plugin));
        plugin.getAbilityManager().registerAbility(new AlchemistsTransmute(plugin));
        plugin.getAbilityManager().registerAbility(new AlchemistsCorrosiveBomb(plugin));
        plugin.getAbilityManager().registerAbility(new EngineersTurret(plugin));
        plugin.getAbilityManager().registerAbility(new EngineersLandmine(plugin));
        plugin.getAbilityManager().registerAbility(new EngineersConstruct(plugin));
        plugin.getAbilityManager().registerAbility(new DruidsWildshapeWolf(plugin));
        plugin.getAbilityManager().registerAbility(new DruidsHealingRain(plugin));
        plugin.getAbilityManager().registerAbility(new DruidsThornLash(plugin));
        plugin.getAbilityManager().registerAbility(new DuelistsRiposte(plugin));
        plugin.getAbilityManager().registerAbility(new DuelistsLunge(plugin));
        plugin.getAbilityManager().registerAbility(new OccultistsSoulLink(plugin));
        plugin.getAbilityManager().registerAbility(new GladiatorsExecute(plugin));

        plugin.getServer().getPluginManager().registerEvents(new ArchiveExpansionAbilities(), plugin);
    }

    static void activateRiposte(UUID uuid, long expireAt, double counterDamage) {
        riposteActive.put(uuid, expireAt);
        riposteCounterDamage.put(uuid, counterDamage);
    }

    static void activateSoulLink(UUID playerUuid, UUID targetUuid, long expireAt, double percent) {
        soulLinkActive.put(playerUuid, expireAt);
        soulLinkTarget.put(playerUuid, targetUuid);
        soulLinkPercent.put(playerUuid, percent);
    }

    @EventHandler
    public void onRiposteDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity();
        Long expire = riposteActive.get(player.getUniqueId());
        if (expire == null) {
            return;
        }
        riposteActive.remove(player.getUniqueId());
        if (System.currentTimeMillis() >= expire) {
            riposteCounterDamage.remove(player.getUniqueId());
            return;
        }
        if (!(event.getDamager() instanceof LivingEntity)) {
            return;
        }
        event.setCancelled(true);
        double counter = riposteCounterDamage.getOrDefault(player.getUniqueId(), 8.0);
        riposteCounterDamage.remove(player.getUniqueId());
        ((LivingEntity) event.getDamager()).damage(counter, player);
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1.2f, 1.2f);
        player.getWorld().spawnParticle(Particle.CRIT, player.getLocation().add(0, 1, 0), 20, 0.3, 0.4, 0.3, 0.1);
    }

    @EventHandler
    public void onSoulLinkDamage(org.bukkit.event.entity.EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity();
        Long expire = soulLinkActive.get(player.getUniqueId());
        if (expire == null) {
            return;
        }
        if (System.currentTimeMillis() >= expire) {
            clearSoulLink(player.getUniqueId());
            return;
        }
        UUID targetId = soulLinkTarget.get(player.getUniqueId());
        if (targetId == null) {
            return;
        }
        Entity linked = pluginInstance.getServer().getEntity(targetId);
        if (!(linked instanceof LivingEntity) || !linked.isValid()) {
            return;
        }
        double percent = soulLinkPercent.getOrDefault(player.getUniqueId(), 0.3);
        ((LivingEntity) linked).damage(event.getDamage() * percent, player);
    }

    private static void clearSoulLink(UUID uuid) {
        soulLinkActive.remove(uuid);
        soulLinkTarget.remove(uuid);
        soulLinkPercent.remove(uuid);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        riposteActive.remove(id);
        riposteCounterDamage.remove(id);
        clearSoulLink(id);
    }
}

// ==================== 20 NEW WANDERING ARCHIVE ABILITIES ====================

class MonksKiPalm extends Ability {
    private final ItemEditFull plugin;
    public MonksKiPalm(ItemEditFull plugin) {
        super("monks_ki_palm", "Monk's Ki Palm", "A focused palm strike that deals bonus damage and briefly staggers.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double damage = getDoubleParam(plugin, item, "damage", 6.0);
        double staggerDuration = getDoubleParam(plugin, item, "stagger", 1.0);
        Entity target = player.getTargetEntity(4);
        if (!(target instanceof LivingEntity)) {
            player.sendMessage("§cNo target in range.");
            return false;
        }
        LivingEntity living = (LivingEntity) target;
        living.damage(damage, player);
        living.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) (staggerDuration * 20), 5));
        living.getWorld().playSound(living.getLocation(), Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK, 1.0f, 1.2f);
        return true;
    }
}

class MonksMeditation extends Ability {
    private final ItemEditFull plugin;
    public MonksMeditation(ItemEditFull plugin) {
        super("monks_meditation", "Monk's Meditation", "Meditate briefly to restore health and clear negative effects.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double heal = getDoubleParam(plugin, item, "heal", 5.0);
        double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        player.setHealth(Math.min(maxHealth, player.getHealth() + heal));
        for (PotionEffect effect : new java.util.ArrayList<>(player.getActivePotionEffects())) {
            PotionEffectType type = effect.getType();
            if (type == PotionEffectType.POISON || type == PotionEffectType.WITHER || type == PotionEffectType.SLOW
                    || type == PotionEffectType.WEAKNESS || type == PotionEffectType.CONFUSION) {
                player.removePotionEffect(type);
            }
        }
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 1.0f);
        player.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0, 1.5, 0), 6, 0.2, 0.2, 0.2, 0.01);
        return true;
    }
}

class MonksFlurry extends Ability {
    private final ItemEditFull plugin;
    public MonksFlurry(ItemEditFull plugin) {
        super("monks_flurry", "Monk's Flurry", "Unleash a rapid flurry of five quick strikes on nearby enemies.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        int hits = getIntParam(plugin, item, "hits", 5);
        double damageEach = getDoubleParam(plugin, item, "damage_each", 2.0);
        double radius = getDoubleParam(plugin, item, "radius", 3.0);
        Location loc = player.getLocation();
        player.getWorld().playSound(loc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.2f, 1.4f);
        LivingEntity primary = null;
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
            if (entity instanceof LivingEntity && !entity.equals(player)) {
                primary = (LivingEntity) entity;
                break;
            }
        }
        if (primary == null) {
            player.sendMessage("§cNo target nearby to flurry.");
            return false;
        }
        LivingEntity target = primary;
        new CompatRunnable() {
            int hitsLanded = 0;
            @Override
            public void run() {
                if (hitsLanded >= hits || !target.isValid() || target.isDead()) {
                    cancel();
                    return;
                }
                target.damage(damageEach, player);
                target.getWorld().spawnParticle(Particle.CRIT, target.getLocation().add(0, 1, 0), 4, 0.2, 0.2, 0.2, 0);
                hitsLanded++;
            }
        }.runTaskTimer(plugin, target, 0L, 3L);
        return true;
    }
}

class RangersBearTrap extends Ability {
    private final ItemEditFull plugin;
    public RangersBearTrap(ItemEditFull plugin) {
        super("rangers_bear_trap", "Ranger's Bear Trap", "Place a hidden trap that roots the first enemy to step on it.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double rootDuration = getDoubleParam(plugin, item, "root_duration", 4.0);
        double damage = getDoubleParam(plugin, item, "damage", 3.0);
        Block targetBlock = player.getTargetBlockExact(5);
        Location trapLoc = (targetBlock != null ? targetBlock.getLocation() : player.getLocation()).add(0.5, 1, 0.5);
        trapLoc.getWorld().playSound(trapLoc, Sound.BLOCK_ANVIL_LAND, 0.6f, 1.5f);
        new CompatRunnable() {
            int ticksElapsed = 0;
            @Override
            public void run() {
                if (ticksElapsed >= 400) {
                    cancel();
                    return;
                }
                for (Entity entity : trapLoc.getWorld().getNearbyEntities(trapLoc, 1.2, 1.0, 1.2)) {
                    if (entity instanceof LivingEntity && !entity.equals(player)) {
                        LivingEntity living = (LivingEntity) entity;
                        living.damage(damage, player);
                        living.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) (rootDuration * 20), 6));
                        trapLoc.getWorld().playSound(trapLoc, Sound.ENTITY_IRON_GOLEM_STEP, 1.0f, 1.4f);
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

class RangersSnareNet extends Ability {
    private final ItemEditFull plugin;
    public RangersSnareNet(ItemEditFull plugin) {
        super("rangers_snare_net", "Ranger's Snare Net", "Throws a net that entangles all enemies caught in its radius.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double radius = getDoubleParam(plugin, item, "radius", 4.0);
        double rootDuration = getDoubleParam(plugin, item, "root_duration", 3.0);
        Block target = player.getTargetBlockExact(10);
        Location loc = target != null ? target.getLocation().add(0.5, 1, 0.5) : player.getLocation();
        loc.getWorld().playSound(loc, Sound.BLOCK_BAMBOO_BREAK, 1.0f, 0.8f);
        loc.getWorld().spawnParticle(Particle.CRIT, loc, 20, radius * 0.3, 0.3, radius * 0.3, 0);
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, radius, 2.0, radius)) {
            if (entity instanceof LivingEntity && !entity.equals(player)) {
                ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) (rootDuration * 20), 5));
            }
        }
        return true;
    }
}

class RangersMultishot extends Ability {
    private final ItemEditFull plugin;
    public RangersMultishot(ItemEditFull plugin) {
        super("rangers_multishot", "Ranger's Multishot", "Fires three arrows in a tight spread simultaneously.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        int count = getIntParam(plugin, item, "count", 3);
        double damageEach = getDoubleParam(plugin, item, "damage_each", 3.0);
        Location origin = player.getEyeLocation();
        Vector facing = origin.getDirection().normalize();
        player.getWorld().playSound(origin, Sound.ITEM_CROSSBOW_SHOOT, 1.0f, 1.0f);
        for (int i = 0; i < count; i++) {
            double spreadAngle = Math.toRadians((i - (count - 1) / 2.0) * 10.0);
            Vector spread = new Vector(
                    facing.getX() * Math.cos(spreadAngle) - facing.getZ() * Math.sin(spreadAngle),
                    facing.getY(),
                    facing.getX() * Math.sin(spreadAngle) + facing.getZ() * Math.cos(spreadAngle)
            );
            for (int d = 1; d <= 12; d++) {
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

class RangersCamouflage extends Ability {
    private final ItemEditFull plugin;
    public RangersCamouflage(ItemEditFull plugin) {
        super("rangers_camouflage", "Ranger's Camouflage", "Blend into your surroundings, gaining invisibility while stationary.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 8.0);
        Location startLoc = player.getLocation();
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, (int) (duration * 20), 0));
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_GRASS_STEP, 0.8f, 0.8f);
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

class AlchemistsVolatileFlask extends Ability {
    private final ItemEditFull plugin;
    public AlchemistsVolatileFlask(ItemEditFull plugin) {
        super("alchemists_volatile_flask", "Alchemist's Volatile Flask", "Throws a flask that explodes into a random elemental effect.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double radius = getDoubleParam(plugin, item, "radius", 4.0);
        Block target = player.getTargetBlockExact(12);
        Location loc = target != null ? target.getLocation().add(0.5, 1, 0.5) : player.getLocation();
        PotionEffectType[] effects = {PotionEffectType.POISON, PotionEffectType.WITHER, PotionEffectType.SLOW, PotionEffectType.WEAKNESS};
        PotionEffectType chosen = effects[(int) (Math.random() * effects.length)];
        loc.getWorld().playSound(loc, Sound.ENTITY_SPLASH_POTION_BREAK, 1.0f, 1.0f);
        loc.getWorld().spawnParticle(Particle.SPELL_WITCH, loc, 30, radius * 0.3, 0.4, radius * 0.3, 0);
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
            if (entity instanceof LivingEntity && !entity.equals(player)) {
                ((LivingEntity) entity).addPotionEffect(new PotionEffect(chosen, 100, 1));
            }
        }
        return true;
    }
}

class AlchemistsTransmute extends Ability {
    private final ItemEditFull plugin;
    public AlchemistsTransmute(ItemEditFull plugin) {
        super("alchemists_transmute", "Alchemist's Transmute", "Converts a portion of your hunger into instant healing.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        int hungerCost = getIntParam(plugin, item, "hunger_cost", 4);
        double heal = getDoubleParam(plugin, item, "heal", 5.0);
        if (player.getFoodLevel() < hungerCost) {
            player.sendMessage("§cYou're too hungry to transmute.");
            return false;
        }
        player.setFoodLevel(player.getFoodLevel() - hungerCost);
        double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        player.setHealth(Math.min(maxHealth, player.getHealth() + heal));
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_BOTTLE_FILL, 1.0f, 1.2f);
        player.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0, 1.5, 0), 6, 0.2, 0.2, 0.2, 0.01);
        return true;
    }
}

class AlchemistsCorrosiveBomb extends Ability {
    private final ItemEditFull plugin;
    public AlchemistsCorrosiveBomb(ItemEditFull plugin) {
        super("alchemists_corrosive_bomb", "Alchemist's Corrosive Bomb", "A bomb that corrodes armour, reducing its effectiveness on impact.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double radius = getDoubleParam(plugin, item, "radius", 4.0);
        double duration = getDoubleParam(plugin, item, "duration", 6.0);
        Block target = player.getTargetBlockExact(12);
        Location loc = target != null ? target.getLocation().add(0.5, 1, 0.5) : player.getLocation();
        loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1.0f, 0.7f);
        loc.getWorld().spawnParticle(Particle.SPELL_WITCH, loc, 25, radius * 0.3, 0.4, radius * 0.3, 0);
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
            if (entity instanceof LivingEntity && !entity.equals(player)) {
                ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, (int) (duration * 20), 1));
            }
        }
        return true;
    }
}

class EngineersTurret extends Ability {
    private final ItemEditFull plugin;
    public EngineersTurret(ItemEditFull plugin) {
        super("engineers_turret", "Engineer's Turret", "Deploys a temporary turret that fires arrows at nearby enemies.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double lifetime = getDoubleParam(plugin, item, "lifetime", 15.0);
        double damageEach = getDoubleParam(plugin, item, "damage_each", 2.0);
        double range = getDoubleParam(plugin, item, "range", 12.0);
        Location turretLoc = player.getLocation();
        turretLoc.getWorld().playSound(turretLoc, Sound.BLOCK_ANVIL_PLACE, 0.8f, 1.2f);
        new CompatRunnable() {
            int ticksElapsed = 0;
            @Override
            public void run() {
                if (ticksElapsed >= (lifetime * 20)) {
                    cancel();
                    return;
                }
                if (ticksElapsed % 20 == 0) {
                    for (Entity entity : turretLoc.getWorld().getNearbyEntities(turretLoc, range, range, range)) {
                        if (entity instanceof LivingEntity && !entity.equals(player) && !(entity instanceof Player)) {
                            ((LivingEntity) entity).damage(damageEach, player);
                            turretLoc.getWorld().spawnParticle(Particle.CRIT, entity.getLocation().add(0, 1, 0), 6, 0.2, 0.2, 0.2, 0);
                            turretLoc.getWorld().playSound(turretLoc, Sound.ENTITY_ARROW_SHOOT, 0.6f, 1.4f);
                            break;
                        }
                    }
                }
                ticksElapsed += 5;
            }
        }.runTaskTimer(plugin, turretLoc, 0L, 5L);
        return true;
    }
}

class EngineersLandmine extends Ability {
    private final ItemEditFull plugin;
    public EngineersLandmine(ItemEditFull plugin) {
        super("engineers_landmine", "Engineer's Landmine", "Plants a hidden mine that detonates when an enemy steps near.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double radius = getDoubleParam(plugin, item, "radius", 2.0);
        double damage = getDoubleParam(plugin, item, "damage", 8.0);
        Location mineLoc = player.getLocation();
        mineLoc.getWorld().playSound(mineLoc, Sound.BLOCK_TRIPWIRE_CLICK_ON, 1.0f, 1.0f);
        new CompatRunnable() {
            int ticksElapsed = 0;
            @Override
            public void run() {
                if (ticksElapsed >= 400) {
                    cancel();
                    return;
                }
                for (Entity entity : mineLoc.getWorld().getNearbyEntities(mineLoc, radius, radius, radius)) {
                    if (entity instanceof LivingEntity && !entity.equals(player)) {
                        LivingEntity living = (LivingEntity) entity;
                        living.damage(damage, player);
                        mineLoc.getWorld().playSound(mineLoc, Sound.ENTITY_GENERIC_EXPLODE, 1.3f, 1.0f);
                        mineLoc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, mineLoc, 2, 0.2, 0.2, 0.2, 0);
                        cancel();
                        return;
                    }
                }
                ticksElapsed += 5;
            }
        }.runTaskTimer(plugin, mineLoc, 0L, 5L);
        return true;
    }
}

class EngineersConstruct extends Ability {
    private final ItemEditFull plugin;
    public EngineersConstruct(ItemEditFull plugin) {
        super("engineers_construct", "Engineer's Iron Construct", "Assembles a temporary iron construct ally that blocks and punches.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double lifetime = getDoubleParam(plugin, item, "lifetime", 20.0);
        Location loc = player.getLocation();
        IronGolem golem;
        try {
            golem = loc.getWorld().spawn(loc, IronGolem.class);
        } catch (Exception e) {
            player.sendMessage("§cThe construct could not be assembled here.");
            return false;
        }
        golem.setCustomName("§7" + player.getName() + "'s Construct");
        golem.setCustomNameVisible(true);
        golem.setPlayerCreated(true);
        loc.getWorld().playSound(loc, Sound.BLOCK_ANVIL_USE, 1.0f, 0.8f);
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
                    golem.getWorld().spawnParticle(Particle.SMOKE_NORMAL, golem.getLocation().add(0, 1, 0), 15, 0.3, 0.4, 0.3, 0.02);
                    golem.remove();
                }
            }
        }.runTaskLater(plugin, player, (long) (lifetime * 20));
        return true;
    }
}

class DruidsWildshapeWolf extends Ability {
    private final ItemEditFull plugin;
    public DruidsWildshapeWolf(ItemEditFull plugin) {
        super("druids_wildshape_wolf", "Druid's Wildshape: Wolf", "Briefly transform, gaining wolf speed and bite damage.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 10.0);
        double biteDamage = getDoubleParam(plugin, item, "bite_damage", 5.0);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, (int) (duration * 20), 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, (int) (duration * 20), 0));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WOLF_GROWL, 1.0f, 0.9f);
        player.sendMessage("§2You feel the wolf's speed and bite (" + biteDamage + " base) course through you!");
        return true;
    }
}

class DruidsHealingRain extends Ability {
    private final ItemEditFull plugin;
    public DruidsHealingRain(ItemEditFull plugin) {
        super("druids_healing_rain", "Druid's Healing Rain", "Calls a gentle rain that heals allies standing beneath it.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double radius = getDoubleParam(plugin, item, "radius", 5.0);
        double duration = getDoubleParam(plugin, item, "duration", 6.0);
        Location center = player.getLocation();
        center.getWorld().playSound(center, Sound.WEATHER_RAIN, 1.0f, 1.2f);
        new CompatRunnable() {
            int ticksElapsed = 0;
            @Override
            public void run() {
                if (ticksElapsed >= (duration * 20)) {
                    cancel();
                    return;
                }
                center.getWorld().spawnParticle(Particle.DRIP_WATER, center.clone().add(0, 2, 0), 8, radius * 0.3, 0.5, radius * 0.3, 0);
                if (ticksElapsed % 20 == 0) {
                    for (Entity entity : center.getWorld().getNearbyEntities(center, radius, 2.0, radius)) {
                        if (entity instanceof Player) {
                            Player ally = (Player) entity;
                            double maxHealth = ally.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                            ally.setHealth(Math.min(maxHealth, ally.getHealth() + 1.0));
                        }
                    }
                }
                ticksElapsed += 5;
            }
        }.runTaskTimer(plugin, center, 0L, 5L);
        return true;
    }
}

class DruidsThornLash extends Ability {
    private final ItemEditFull plugin;
    public DruidsThornLash(ItemEditFull plugin) {
        super("druids_thorn_lash", "Druid's Thorn Lash", "Whips out a thorned vine, damaging and pulling a target.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double range = getDoubleParam(plugin, item, "range", 7.0);
        double damage = getDoubleParam(plugin, item, "damage", 5.0);
        Entity target = player.getTargetEntity((int) range);
        if (!(target instanceof LivingEntity)) {
            player.sendMessage("§cNo target in range.");
            return false;
        }
        LivingEntity living = (LivingEntity) target;
        living.damage(damage, player);
        Vector pull = VectorUtils.safeNormalize(player.getLocation().toVector().subtract(living.getLocation().toVector())).multiply(1.0).setY(0.15);
        living.setVelocity(pull);
        living.getWorld().playSound(living.getLocation(), Sound.BLOCK_VINE_STEP, 1.0f, 0.8f);
        return true;
    }
}

class DuelistsRiposte extends Ability {
    private final ItemEditFull plugin;
    public DuelistsRiposte(ItemEditFull plugin) {
        super("duelists_riposte", "Duelist's Riposte", "Parry the next melee hit and counter with a guaranteed critical strike.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double window = getDoubleParam(plugin, item, "window", 1.5);
        double counterDamage = getDoubleParam(plugin, item, "counter_damage", 8.0);
        ArchiveExpansionAbilities.activateRiposte(player.getUniqueId(), System.currentTimeMillis() + (long) (window * 1000), counterDamage);
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_SHIELD_BLOCK, 0.8f, 1.4f);
        player.sendMessage("§fYou ready a parry stance!");
        return true;
    }
}

class DuelistsLunge extends Ability {
    private final ItemEditFull plugin;
    public DuelistsLunge(ItemEditFull plugin) {
        super("duelists_lunge", "Duelist's Lunge", "A precise forward lunge that deals bonus damage to a single target.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double range = getDoubleParam(plugin, item, "range", 5.0);
        double damage = getDoubleParam(plugin, item, "damage", 7.0);
        Entity target = player.getTargetEntity((int) range);
        if (!(target instanceof LivingEntity)) {
            player.sendMessage("§cNo target in range.");
            return false;
        }
        LivingEntity living = (LivingEntity) target;
        Vector toTarget = VectorUtils.safeNormalize(living.getLocation().toVector().subtract(player.getLocation().toVector()));
        player.setVelocity(toTarget.multiply(0.9).setY(0.1));
        living.damage(damage, player);
        living.getWorld().playSound(living.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 1.2f);
        return true;
    }
}

class OccultistsSoulLink extends Ability {
    private final ItemEditFull plugin;
    public OccultistsSoulLink(ItemEditFull plugin) {
        super("occultists_soul_link", "Occultist's Soul Link", "Links your fate to a target; a portion of damage you take is mirrored onto them.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 8.0);
        double mirror = getDoubleParam(plugin, item, "mirror", 0.3);
        double range = getDoubleParam(plugin, item, "range", 10.0);
        Entity target = player.getTargetEntity((int) range);
        if (!(target instanceof LivingEntity)) {
            player.sendMessage("§cNo target in range.");
            return false;
        }
        ArchiveExpansionAbilities.activateSoulLink(player.getUniqueId(), target.getUniqueId(), System.currentTimeMillis() + (long) (duration * 1000), mirror);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITCH_AMBIENT, 1.0f, 0.7f);
        player.getWorld().spawnParticle(Particle.SPELL_WITCH, player.getLocation().add(0, 1, 0), 15, 0.3, 0.4, 0.3, 0);
        player.sendMessage("§5Your soul is linked to your target's fate!");
        return true;
    }
}

class GladiatorsExecute extends Ability {
    private final ItemEditFull plugin;
    public GladiatorsExecute(ItemEditFull plugin) {
        super("gladiators_execute", "Gladiator's Execute", "Deals massive bonus damage to enemies below a health threshold.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double thresholdPercent = getDoubleParam(plugin, item, "threshold_hp", 0.25);
        double bonusDamage = getDoubleParam(plugin, item, "bonus_damage", 10.0);
        Entity target = player.getTargetEntity(5);
        if (!(target instanceof LivingEntity)) {
            player.sendMessage("§cNo target in range.");
            return false;
        }
        LivingEntity living = (LivingEntity) target;
        double maxHealth = living.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null ? living.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() : living.getHealth();
        if (living.getHealth() > maxHealth * thresholdPercent) {
            player.sendMessage("§cYour target is too healthy to execute.");
            return false;
        }
        living.damage(bonusDamage, player);
        living.getWorld().playSound(living.getLocation(), Sound.ENTITY_PLAYER_ATTACK_STRONG, 1.2f, 0.8f);
        living.getWorld().spawnParticle(Particle.CRIT, living.getLocation().add(0, 1, 0), 25, 0.3, 0.4, 0.3, 0.1);
        return true;
    }
}
