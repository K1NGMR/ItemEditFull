package com.itemedit.full.ability.chrono;

import com.itemedit.full.ItemEditFull;
import com.itemedit.full.ability.Ability;
import com.itemedit.full.utils.CompatRunnable;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Temporal Study: 10 time-manipulation abilities (rewinds, echoes, and time
 * bent to the wielder's will) - a brand-new school with no prior abilities.
 */
public class ChronoAbilities implements Listener {

    // ---- Foresight Dodge: guaranteed evasion window ----
    private static final Map<UUID, Long> dodgeActive = new ConcurrentHashMap<>();

    // ---- Chrono Anchor: a marked return point ----
    private static final Map<UUID, Location> anchorPoint = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> anchorExpire = new ConcurrentHashMap<>();

    // ---- Echo Repeat: remembers the last damage a player dealt, and to whom ----
    private static final Map<UUID, Double> lastDamageDealt = new ConcurrentHashMap<>();
    private static final Map<UUID, UUID> lastDamageTarget = new ConcurrentHashMap<>();

    private static ItemEditFull pluginInstance;

    public static void register(ItemEditFull plugin) {
        pluginInstance = plugin;

        plugin.getAbilityManager().registerAbility(new TemporalRewind(plugin));
        plugin.getAbilityManager().registerAbility(new HasteField(plugin));
        plugin.getAbilityManager().registerAbility(new StasisField(plugin));
        plugin.getAbilityManager().registerAbility(new EchoRepeat(plugin));
        plugin.getAbilityManager().registerAbility(new ForesightDodge(plugin));
        plugin.getAbilityManager().registerAbility(new ChronoAnchor(plugin));
        plugin.getAbilityManager().registerAbility(new SlowField(plugin));
        plugin.getAbilityManager().registerAbility(new TimeDilation(plugin));
        plugin.getAbilityManager().registerAbility(new TemporalDuplicate(plugin));
        plugin.getAbilityManager().registerAbility(new AgeReversal(plugin));

        plugin.getServer().getPluginManager().registerEvents(new ChronoAbilities(), plugin);
    }

    static void activateDodge(UUID uuid, long expireAt) {
        dodgeActive.put(uuid, expireAt);
    }

    static Location getAnchor(UUID uuid) {
        Long expire = anchorExpire.get(uuid);
        if (expire == null || System.currentTimeMillis() >= expire) {
            return null;
        }
        return anchorPoint.get(uuid);
    }

    static void setAnchor(UUID uuid, Location loc, long expireAt) {
        anchorPoint.put(uuid, loc);
        anchorExpire.put(uuid, expireAt);
    }

    static void clearAnchor(UUID uuid) {
        anchorPoint.remove(uuid);
        anchorExpire.remove(uuid);
    }

    static double getLastDamage(UUID uuid) {
        return lastDamageDealt.getOrDefault(uuid, 0.0);
    }

    static UUID getLastDamageTarget(UUID uuid) {
        return lastDamageTarget.get(uuid);
    }

    @EventHandler
    public void onPlayerDealDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof LivingEntity) {
            Player damager = (Player) event.getDamager();
            lastDamageDealt.put(damager.getUniqueId(), event.getDamage());
            lastDamageTarget.put(damager.getUniqueId(), event.getEntity().getUniqueId());
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
        if (System.currentTimeMillis() < expire) {
            event.setCancelled(true);
            player.getWorld().playSound(player.getLocation(), Sound.ITEM_CHORUS_FRUIT_TELEPORT, 0.6f, 1.8f);
        }
        dodgeActive.remove(player.getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        dodgeActive.remove(id);
        clearAnchor(id);
        lastDamageDealt.remove(id);
        lastDamageTarget.remove(id);
    }
}

// ==================== 10 TEMPORAL STUDY ABILITIES ====================

class TemporalRewind extends Ability {
    private final ItemEditFull plugin;
    public TemporalRewind(ItemEditFull plugin) {
        super("temporal_rewind", "Temporal Rewind", "Marks your current vitality; a few seconds later you are pulled back to it.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double window = getDoubleParam(plugin, item, "window", 3.0);
        double snapshotHealth = player.getHealth();
        Location snapshotLoc = player.getLocation();
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 1.5f);
        player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation().add(0, 1, 0), 15, 0.3, 0.4, 0.3, 0.02);
        new CompatRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    return;
                }
                if (player.getHealth() < snapshotHealth) {
                    player.setHealth(Math.min(snapshotHealth, player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));
                    player.getWorld().playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_RESONATE, 1.0f, 1.2f);
                    player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation().add(0, 1, 0), 25, 0.3, 0.5, 0.3, 0.05);
                    player.sendMessage("§bTime rewinds your wounds away!");
                }
            }
        }.runTaskLater(plugin, player, (long) (window * 20));
        return true;
    }
}

class HasteField extends Ability {
    private final ItemEditFull plugin;
    public HasteField(ItemEditFull plugin) {
        super("haste_field", "Haste Field", "Creates a field that speeds up everyone standing inside it.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double radius = getDoubleParam(plugin, item, "radius", 5.0);
        double duration = getDoubleParam(plugin, item, "duration", 8.0);
        Location center = player.getLocation();
        center.getWorld().playSound(center, Sound.BLOCK_AMETHYST_BLOCK_RESONATE, 1.0f, 1.5f);
        new CompatRunnable() {
            int ticksElapsed = 0;
            @Override
            public void run() {
                if (ticksElapsed >= (duration * 20)) {
                    cancel();
                    return;
                }
                center.getWorld().spawnParticle(Particle.END_ROD, center.clone().add(0, 0.5, 0), 6, radius * 0.3, 0.2, radius * 0.3, 0.01);
                for (Entity entity : center.getWorld().getNearbyEntities(center, radius, 2.0, radius)) {
                    if (entity instanceof LivingEntity) {
                        ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 1));
                    }
                }
                ticksElapsed += 20;
            }
        }.runTaskTimer(plugin, center, 0L, 20L);
        return true;
    }
}

class StasisField extends Ability {
    private final ItemEditFull plugin;
    public StasisField(ItemEditFull plugin) {
        super("stasis_field", "Stasis Field", "Freezes all enemies in an area in place for a moment.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double radius = getDoubleParam(plugin, item, "radius", 5.0);
        double freezeDuration = getDoubleParam(plugin, item, "freeze", 2.0);
        Location loc = player.getLocation();
        loc.getWorld().playSound(loc, Sound.BLOCK_GLASS_PLACE, 1.0f, 0.6f);
        loc.getWorld().spawnParticle(Particle.SNOWFLAKE, loc.add(0, 1, 0), 30, radius * 0.3, 0.5, radius * 0.3, 0);
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
            if (entity instanceof LivingEntity && !entity.equals(player)) {
                LivingEntity living = (LivingEntity) entity;
                living.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) (freezeDuration * 20), 9));
                living.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, (int) (freezeDuration * 20), -10, false, false));
            }
        }
        return true;
    }
}

class EchoRepeat extends Ability {
    private final ItemEditFull plugin;
    public EchoRepeat(ItemEditFull plugin) {
        super("echo_repeat", "Echo Repeat", "Repeats your last dealt hit instantly at half effectiveness.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double effectiveness = getDoubleParam(plugin, item, "effectiveness", 0.5);
        double lastDamage = ChronoAbilities.getLastDamage(player.getUniqueId());
        UUID targetId = ChronoAbilities.getLastDamageTarget(player.getUniqueId());
        if (targetId == null || lastDamage <= 0) {
            player.sendMessage("§cNo recent strike to echo.");
            return false;
        }
        Entity target = plugin.getServer().getEntity(targetId);
        if (!(target instanceof LivingEntity) || !target.isValid()) {
            player.sendMessage("§cYour last target is no longer within reach of the echo.");
            return false;
        }
        LivingEntity living = (LivingEntity) target;
        living.damage(lastDamage * effectiveness, player);
        living.getWorld().playSound(living.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 1.4f);
        living.getWorld().spawnParticle(Particle.CRIT_MAGIC, living.getLocation().add(0, 1, 0), 15, 0.3, 0.4, 0.3, 0.1);
        return true;
    }
}

class ForesightDodge extends Ability {
    private final ItemEditFull plugin;
    public ForesightDodge(ItemEditFull plugin) {
        super("foresight_dodge", "Foresight Dodge", "Grants a window of guaranteed evasion against the next attack.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double window = getDoubleParam(plugin, item, "window", 2.0);
        ChronoAbilities.activateDodge(player.getUniqueId(), System.currentTimeMillis() + (long) (window * 1000));
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_AMETHYST_CLUSTER_STEP, 1.0f, 1.5f);
        player.sendMessage("§bYou glimpse the moment before it happens.");
        return true;
    }
}

class ChronoAnchor extends Ability {
    private final ItemEditFull plugin;
    public ChronoAnchor(ItemEditFull plugin) {
        super("chrono_anchor", "Chrono Anchor", "Marks your current location; use again to instantly return to it.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double anchorLife = getDoubleParam(plugin, item, "anchor_life", 15.0);
        Location existing = ChronoAbilities.getAnchor(player.getUniqueId());
        if (existing != null) {
            player.teleport(existing);
            player.getWorld().playSound(player.getLocation(), Sound.ITEM_CHORUS_FRUIT_TELEPORT, 1.0f, 1.0f);
            player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation().add(0, 1, 0), 20, 0.3, 0.5, 0.3, 0.2);
            ChronoAbilities.clearAnchor(player.getUniqueId());
            player.sendMessage("§bYou snap back to your anchor point!");
        } else {
            ChronoAbilities.setAnchor(player.getUniqueId(), player.getLocation(), System.currentTimeMillis() + (long) (anchorLife * 1000));
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.3f);
            player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation().add(0, 1, 0), 15, 0.2, 0.4, 0.2, 0.02);
            player.sendMessage("§bAnchor set. Use this ability again to return here.");
        }
        return true;
    }
}

class SlowField extends Ability {
    private final ItemEditFull plugin;
    public SlowField(ItemEditFull plugin) {
        super("slow_field", "Slow Field", "Creates a field that slows all enemies standing inside it.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double radius = getDoubleParam(plugin, item, "radius", 5.0);
        double duration = getDoubleParam(plugin, item, "duration", 8.0);
        Location center = player.getLocation();
        center.getWorld().playSound(center, Sound.BLOCK_AMETHYST_BLOCK_HIT, 1.0f, 0.6f);
        new CompatRunnable() {
            int ticksElapsed = 0;
            @Override
            public void run() {
                if (ticksElapsed >= (duration * 20)) {
                    cancel();
                    return;
                }
                center.getWorld().spawnParticle(Particle.SMOKE_NORMAL, center.clone().add(0, 0.5, 0), 4, radius * 0.3, 0.2, radius * 0.3, 0.01);
                for (Entity entity : center.getWorld().getNearbyEntities(center, radius, 2.0, radius)) {
                    if (entity instanceof LivingEntity && !entity.equals(player)) {
                        ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 1));
                    }
                }
                ticksElapsed += 20;
            }
        }.runTaskTimer(plugin, center, 0L, 20L);
        return true;
    }
}

class TimeDilation extends Ability {
    private final ItemEditFull plugin;
    public TimeDilation(ItemEditFull plugin) {
        super("time_dilation", "Time Dilation", "Hastens you while slowing everyone else nearby.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double radius = getDoubleParam(plugin, item, "radius", 5.0);
        double duration = getDoubleParam(plugin, item, "duration", 6.0);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, (int) (duration * 20), 2));
        player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, (int) (duration * 20), 1));
        Location loc = player.getLocation();
        loc.getWorld().playSound(loc, Sound.BLOCK_AMETHYST_BLOCK_RESONATE, 1.0f, 1.2f);
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
            if (entity instanceof LivingEntity && !entity.equals(player)) {
                ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) (duration * 20), 1));
            }
        }
        return true;
    }
}

class TemporalDuplicate extends Ability {
    private final ItemEditFull plugin;
    public TemporalDuplicate(ItemEditFull plugin) {
        super("temporal_duplicate", "Temporal Duplicate", "Splits off a brief echo of yourself that mimics your last move to confuse enemies.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 5.0);
        Location loc = player.getLocation();
        ArmorStand echo = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
        echo.setCustomName("§b" + player.getName() + "'s Echo");
        echo.setCustomNameVisible(true);
        echo.setGravity(false);
        echo.setBasePlate(true);
        loc.getWorld().playSound(loc, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.8f, 1.6f);
        loc.getWorld().spawnParticle(Particle.END_ROD, loc.add(0, 1, 0), 20, 0.3, 0.5, 0.3, 0.1);
        for (Entity entity : echo.getNearbyEntities(10, 5, 10)) {
            if (entity instanceof Monster && entity instanceof Mob) {
                ((Mob) entity).setTarget(echo);
            }
        }
        new CompatRunnable() {
            @Override
            public void run() {
                if (echo.isValid()) {
                    echo.getWorld().spawnParticle(Particle.END_ROD, echo.getLocation().add(0, 1, 0), 15, 0.3, 0.5, 0.3, 0.05);
                    echo.remove();
                }
            }
        }.runTaskLater(plugin, player, (long) (duration * 20));
        return true;
    }
}

class AgeReversal extends Ability {
    private final ItemEditFull plugin;
    public AgeReversal(ItemEditFull plugin) {
        super("age_reversal", "Age Reversal", "Slowly reverses recent damage, healing you over time.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 8.0);
        double healPerTick = getDoubleParam(plugin, item, "heal_tick", 1.0);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 1.0f);
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
                player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation().add(0, 1.5, 0), 2, 0.2, 0.2, 0.2, 0.01);
                ticksElapsed += 20;
            }
        }.runTaskTimer(plugin, player, 20L, 20L);
        return true;
    }
}
