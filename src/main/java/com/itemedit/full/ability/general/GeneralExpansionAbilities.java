package com.itemedit.full.ability.general;

import com.itemedit.full.ItemEditFull;
import com.itemedit.full.ability.Ability;
import com.itemedit.full.utils.CompatRunnable;
import com.itemedit.full.utils.EffectUtils;
import com.itemedit.full.utils.VectorUtils;
import org.bukkit.Color;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Elemental Core expansion: 20 additional foundational-force abilities layered
 * on top of the original 39 general abilities.
 */
public class GeneralExpansionAbilities implements Listener {

    // ---- Echo Strike: repeats the caster's next hit a moment later ----
    private static final Map<UUID, Long> echoActive = new ConcurrentHashMap<>();
    private static final Map<UUID, Double> echoDelay = new ConcurrentHashMap<>();

    // ---- Light Barrier: absorbs a set number of hits ----
    private static final Map<UUID, Long> barrierActive = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> barrierHitsRemaining = new ConcurrentHashMap<>();

    // ---- Thornmail Aura: reflects a percentage of melee damage taken ----
    private static final Map<UUID, Long> thornmailActive = new ConcurrentHashMap<>();
    private static final Map<UUID, Double> thornmailPercent = new ConcurrentHashMap<>();

    // ---- Curse Mark: amplifies the next damage anyone deals to the marked target ----
    private static final Map<UUID, Long> curseMarkActive = new ConcurrentHashMap<>();
    private static final Map<UUID, Double> curseMarkAmplify = new ConcurrentHashMap<>();

    private static ItemEditFull pluginInstance;

    public static void register(ItemEditFull plugin) {
        pluginInstance = plugin;

        plugin.getAbilityManager().registerAbility(new MomentumBurst(plugin));
        plugin.getAbilityManager().registerAbility(new EchoStrike(plugin));
        plugin.getAbilityManager().registerAbility(new LightBarrier(plugin));
        plugin.getAbilityManager().registerAbility(new StaticField(plugin));
        plugin.getAbilityManager().registerAbility(new ChainBind(plugin));
        plugin.getAbilityManager().registerAbility(new MirrorImage(plugin));
        plugin.getAbilityManager().registerAbility(new NullField(plugin));
        plugin.getAbilityManager().registerAbility(new GaleStep(plugin));
        plugin.getAbilityManager().registerAbility(new RootSnare(plugin));
        plugin.getAbilityManager().registerAbility(new PrismBeam(plugin));
        plugin.getAbilityManager().registerAbility(new SecondWind(plugin));
        plugin.getAbilityManager().registerAbility(new BattleFocus(plugin));
        plugin.getAbilityManager().registerAbility(new TidalPull(plugin));
        plugin.getAbilityManager().registerAbility(new ThornmailAura(plugin));
        plugin.getAbilityManager().registerAbility(new SolarRay(plugin));
        plugin.getAbilityManager().registerAbility(new LunarVeil(plugin));
        plugin.getAbilityManager().registerAbility(new PhaseShift(plugin));
        plugin.getAbilityManager().registerAbility(new GravityFlip(plugin));
        plugin.getAbilityManager().registerAbility(new CurseMark(plugin));
        plugin.getAbilityManager().registerAbility(new BlessingTotem(plugin));

        plugin.getServer().getPluginManager().registerEvents(new GeneralExpansionAbilities(), plugin);
    }

    static void activateEcho(UUID uuid, long expireAt, double delaySeconds) {
        echoActive.put(uuid, expireAt);
        echoDelay.put(uuid, delaySeconds);
    }

    static void activateBarrier(UUID uuid, long expireAt, int hits) {
        barrierActive.put(uuid, expireAt);
        barrierHitsRemaining.put(uuid, hits);
    }

    static void activateThornmail(UUID uuid, long expireAt, double percent) {
        thornmailActive.put(uuid, expireAt);
        thornmailPercent.put(uuid, percent);
    }

    static void markCurse(UUID targetUuid, long expireAt, double amplify) {
        curseMarkActive.put(targetUuid, expireAt);
        curseMarkAmplify.put(targetUuid, amplify);
    }

    @EventHandler
    public void onEchoHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getDamager();
        Long expire = echoActive.get(player.getUniqueId());
        if (expire == null) {
            return;
        }
        if (System.currentTimeMillis() >= expire) {
            echoActive.remove(player.getUniqueId());
            echoDelay.remove(player.getUniqueId());
            return;
        }
        if (!(event.getEntity() instanceof LivingEntity)) {
            return;
        }
        echoActive.remove(player.getUniqueId());
        double delay = echoDelay.getOrDefault(player.getUniqueId(), 1.0);
        double repeatDamage = event.getDamage();
        LivingEntity target = (LivingEntity) event.getEntity();
        UUID targetId = target.getUniqueId();
        new CompatRunnable() {
            @Override
            public void run() {
                Entity refreshed = pluginInstance.getServer().getEntity(targetId);
                if (refreshed instanceof LivingEntity && refreshed.isValid()) {
                    ((LivingEntity) refreshed).damage(repeatDamage, player);
                    refreshed.getWorld().spawnParticle(Particle.CRIT_MAGIC, refreshed.getLocation().add(0, 1, 0), 15, 0.3, 0.4, 0.3, 0.1);
                    refreshed.getWorld().playSound(refreshed.getLocation(), Sound.ENTITY_ILLUSIONER_MIRROR_MOVE, 1.0f, 1.2f);
                }
            }
        }.runTaskLater(pluginInstance, target, (long) (delay * 20));
    }

    @EventHandler
    public void onBarrierDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity();
        Long expire = barrierActive.get(player.getUniqueId());
        if (expire == null) {
            return;
        }
        if (System.currentTimeMillis() >= expire || barrierHitsRemaining.getOrDefault(player.getUniqueId(), 0) <= 0) {
            clearBarrier(player.getUniqueId());
            return;
        }
        event.setCancelled(true);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1.0f, 1.3f);
        player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation().add(0, 1, 0), 12, 0.3, 0.4, 0.3, 0.02);
        int remaining = barrierHitsRemaining.getOrDefault(player.getUniqueId(), 1) - 1;
        if (remaining <= 0) {
            clearBarrier(player.getUniqueId());
        } else {
            barrierHitsRemaining.put(player.getUniqueId(), remaining);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onThornmailDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity();
        Long expire = thornmailActive.get(player.getUniqueId());
        if (expire == null) {
            return;
        }
        if (System.currentTimeMillis() >= expire) {
            thornmailActive.remove(player.getUniqueId());
            thornmailPercent.remove(player.getUniqueId());
            return;
        }
        if (!(event.getDamager() instanceof LivingEntity)) {
            return;
        }
        double percent = thornmailPercent.getOrDefault(player.getUniqueId(), 0.25);
        double reflected = event.getDamage() * percent;
        ((LivingEntity) event.getDamager()).damage(reflected, player);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onCurseMarkDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) {
            return;
        }
        UUID id = event.getEntity().getUniqueId();
        Long expire = curseMarkActive.get(id);
        if (expire == null) {
            return;
        }
        if (System.currentTimeMillis() >= expire) {
            curseMarkActive.remove(id);
            curseMarkAmplify.remove(id);
            return;
        }
        double amplify = curseMarkAmplify.getOrDefault(id, 0.25);
        event.setDamage(event.getDamage() * (1.0 + amplify));
        curseMarkActive.remove(id);
        curseMarkAmplify.remove(id);
        event.getEntity().getWorld().spawnParticle(Particle.SPELL_WITCH, event.getEntity().getLocation().add(0, 1, 0), 10, 0.3, 0.3, 0.3, 0);
    }

    private static void clearBarrier(UUID uuid) {
        barrierActive.remove(uuid);
        barrierHitsRemaining.remove(uuid);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        echoActive.remove(id);
        echoDelay.remove(id);
        clearBarrier(id);
        thornmailActive.remove(id);
        thornmailPercent.remove(id);
    }
}

// ==================== 20 NEW ELEMENTAL CORE ABILITIES ====================

class MomentumBurst extends Ability {
    private final ItemEditFull plugin;
    public MomentumBurst(ItemEditFull plugin) {
        super("momentum_burst", "Momentum Burst", "Converts your fall speed into an outward shockwave on landing.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double radius = getDoubleParam(plugin, item, "radius", 5.0);
        player.sendMessage("§bMomentum gathers beneath your feet - land hard to release it.");
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.8f, 1.3f);
        new CompatRunnable() {
            int ticksElapsed = 0;
            boolean wasAirborne = false;
            @Override
            public void run() {
                ticksElapsed += 1;
                if (!player.isOnline() || ticksElapsed >= 200) {
                    cancel();
                    return;
                }
                if (!player.isOnGround()) {
                    wasAirborne = true;
                }
                if (wasAirborne && player.isOnGround()) {
                    double fallDistance = player.getFallDistance();
                    player.setFallDistance(0f);
                    double damage = Math.min(20.0, fallDistance * 1.2);
                    if (damage > 1.0) {
                        Location loc = player.getLocation();
                        loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_BIG_FALL, 1.2f, 0.9f);
                        loc.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, loc, 15, radius * 0.3, 0.1, radius * 0.3, 0);
                        for (Entity entity : loc.getWorld().getNearbyEntities(loc, radius, 2.0, radius)) {
                            if (entity instanceof LivingEntity && !entity.equals(player)) {
                                ((LivingEntity) entity).damage(damage, player);
                            }
                        }
                    }
                    cancel();
                }
            }
        }.runTaskTimer(plugin, player, 1L, 1L);
        return true;
    }
}

class EchoStrike extends Ability {
    private final ItemEditFull plugin;
    public EchoStrike(ItemEditFull plugin) {
        super("echo_strike", "Echo Strike", "Your next hit lands twice: once now, once again a moment later.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double window = getDoubleParam(plugin, item, "window", 6.0);
        double delay = getDoubleParam(plugin, item, "delay", 1.0);
        GeneralExpansionAbilities.activateEcho(player.getUniqueId(), System.currentTimeMillis() + (long) (window * 1000), delay);
        Location echoLoc = player.getLocation().add(0, 1, 0);
        EffectUtils.fanfare(echoLoc,
                new EffectUtils.SoundLayer(Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 1.2f),
                new EffectUtils.SoundLayer(Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.6f, 0.9f));
        EffectUtils.burst(echoLoc,
                new EffectUtils.Layer(Particle.CRIT_MAGIC, 12, 0.3, 0.4, 0.3, 0.05),
                new EffectUtils.Layer(Particle.END_ROD, 6, 0.2, 0.3, 0.2, 0.01));
        EffectUtils.staggered(plugin, player, 2, 3L, i ->
                player.getWorld().spawnParticle(Particle.CRIT_MAGIC, player.getLocation().add(0, 1, 0), 8, 0.25, 0.35, 0.25, 0.03));
        player.sendMessage("§bYour next hit will echo!");
        return true;
    }
}

class LightBarrier extends Ability {
    private final ItemEditFull plugin;
    public LightBarrier(ItemEditFull plugin) {
        super("light_barrier", "Light Barrier", "Erects a barrier of solid light that blocks a set number of hits.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 8.0);
        int absorbHits = getIntParam(plugin, item, "absorb_hits", 3);
        GeneralExpansionAbilities.activateBarrier(player.getUniqueId(), System.currentTimeMillis() + (long) (duration * 1000), absorbHits);
        Location barrierLoc = player.getLocation().add(0, 1, 0);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.2f);
        EffectUtils.burst(barrierLoc,
                new EffectUtils.Layer(Particle.END_ROD, 14, 0.3, 0.5, 0.3, 0.02),
                new EffectUtils.Layer(Particle.FLASH, 1, 0, 0, 0, 0));
        EffectUtils.ring(barrierLoc, 1.2, 16, new EffectUtils.Layer(Particle.END_ROD, 1, 0.05, 0.1, 0.05, 0.01));
        return true;
    }
}

class StaticField extends Ability {
    private final ItemEditFull plugin;
    public StaticField(ItemEditFull plugin) {
        super("static_field", "Static Field", "Charges the air around you, dealing small periodic damage to nearby enemies.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double radius = getDoubleParam(plugin, item, "radius", 4.0);
        double duration = getDoubleParam(plugin, item, "duration", 6.0);
        double tickDamage = getDoubleParam(plugin, item, "tick_damage", 1.0);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_RESONATE, 1.0f, 1.4f);
        new CompatRunnable() {
            int ticksElapsed = 0;
            @Override
            public void run() {
                if (!player.isOnline() || ticksElapsed >= (duration * 20)) {
                    cancel();
                    return;
                }
                Location loc = player.getLocation();
                loc.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, loc.clone().add(0, 1, 0), 6, radius * 0.3, 0.4, radius * 0.3, 0.02);
                if (ticksElapsed % 20 == 0) {
                    for (Entity entity : loc.getWorld().getNearbyEntities(loc, radius, 2.0, radius)) {
                        if (entity instanceof LivingEntity && !entity.equals(player)) {
                            ((LivingEntity) entity).damage(tickDamage, player);
                        }
                    }
                }
                ticksElapsed += 5;
            }
        }.runTaskTimer(plugin, player, 0L, 5L);
        return true;
    }
}

class ChainBind extends Ability {
    private final ItemEditFull plugin;
    public ChainBind(ItemEditFull plugin) {
        super("chain_bind", "Chain Bind", "Binds a target in place with spectral chains.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double range = getDoubleParam(plugin, item, "range", 8.0);
        double rootDuration = getDoubleParam(plugin, item, "root_duration", 3.0);
        Entity target = player.getTargetEntity((int) range);
        if (!(target instanceof LivingEntity)) {
            player.sendMessage("§cNo target in range.");
            return false;
        }
        LivingEntity living = (LivingEntity) target;
        living.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) (rootDuration * 20), 6));
        living.getWorld().playSound(living.getLocation(), Sound.BLOCK_CHAIN_PLACE, 1.0f, 0.8f);
        Location chainLoc = living.getLocation();
        EffectUtils.ring(chainLoc.clone().add(0, 0.1, 0), 0.8, 10, new EffectUtils.Layer(Particle.SOUL, 1, 0.05, 0.1, 0.05, 0.01));
        EffectUtils.burst(chainLoc.clone().add(0, 1, 0),
                new EffectUtils.Layer(Particle.SOUL_FIRE_FLAME, 10, 0.25, 0.4, 0.25, 0.01),
                new EffectUtils.Layer(Particle.SMOKE_LARGE, 4, 0.2, 0.3, 0.2, 0.01));
        return true;
    }
}

class MirrorImage extends Ability {
    private final ItemEditFull plugin;
    public MirrorImage(ItemEditFull plugin) {
        super("mirror_image", "Mirror Image", "Creates a decoy copy of yourself that briefly draws enemy attention.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 6.0);
        Location loc = player.getLocation();
        ArmorStand decoy = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
        decoy.setCustomName("§b" + player.getName() + "'s Image");
        decoy.setCustomNameVisible(true);
        decoy.setInvisible(false);
        decoy.setGravity(false);
        decoy.setBasePlate(true);
        loc.getWorld().playSound(loc, Sound.ITEM_TOTEM_USE, 0.6f, 1.5f);
        loc.getWorld().spawnParticle(Particle.PORTAL, loc.add(0, 1, 0), 20, 0.3, 0.5, 0.3, 0.2);
        for (Entity entity : decoy.getNearbyEntities(10, 5, 10)) {
            if (entity instanceof Monster && entity instanceof Mob) {
                ((Mob) entity).setTarget(decoy);
            }
        }
        new CompatRunnable() {
            @Override
            public void run() {
                if (decoy.isValid()) {
                    decoy.getWorld().spawnParticle(Particle.PORTAL, decoy.getLocation().add(0, 1, 0), 15, 0.3, 0.5, 0.3, 0.1);
                    decoy.remove();
                }
            }
        }.runTaskLater(plugin, player, (long) (duration * 20));
        return true;
    }
}

class NullField extends Ability {
    private final ItemEditFull plugin;
    public NullField(ItemEditFull plugin) {
        super("null_field", "Null Field", "Emits a pulse that strips harmful potion effects from you and nearby allies.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double radius = getDoubleParam(plugin, item, "radius", 5.0);
        Location loc = player.getLocation();
        loc.getWorld().playSound(loc, Sound.BLOCK_BEACON_DEACTIVATE, 1.0f, 1.5f);
        loc.getWorld().spawnParticle(Particle.CLOUD, loc.add(0, 1, 0), 20, radius * 0.3, 0.4, radius * 0.3, 0);
        cleanse(player);
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
            if (entity instanceof Player && !entity.equals(player)) {
                cleanse((Player) entity);
            }
        }
        return true;
    }
    private void cleanse(Player p) {
        for (PotionEffect effect : new ArrayList<>(p.getActivePotionEffects())) {
            PotionEffectType type = effect.getType();
            if (type == PotionEffectType.POISON || type == PotionEffectType.WITHER || type == PotionEffectType.SLOW
                    || type == PotionEffectType.WEAKNESS || type == PotionEffectType.BLINDNESS
                    || type == PotionEffectType.CONFUSION || type == PotionEffectType.HUNGER) {
                p.removePotionEffect(type);
            }
        }
    }
}

class GaleStep extends Ability {
    private final ItemEditFull plugin;
    public GaleStep(ItemEditFull plugin) {
        super("gale_step", "Gale Step", "A gust-propelled dash that passes through enemies without collision.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double range = getDoubleParam(plugin, item, "range", 7.0);
        Vector dir = VectorUtils.safeNormalize(player.getLocation().getDirection().setY(0));
        if (dir.lengthSquared() < 0.01) {
            dir = new Vector(0, 0, 1);
        }
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_ELYTRA_FLYING, 0.8f, 1.5f);
        player.setVelocity(dir.multiply(1.8).setY(0.1));
        Location loc = player.getLocation();
        for (int i = 1; i <= (int) range; i++) {
            Location step = loc.clone().add(dir.clone().multiply(i)).add(0, 1, 0);
            step.getWorld().spawnParticle(Particle.CLOUD, step, 3, 0.2, 0.2, 0.2, 0.01);
        }
        return true;
    }
}

class RootSnare extends Ability {
    private final ItemEditFull plugin;
    public RootSnare(ItemEditFull plugin) {
        super("root_snare", "Root Snare", "Roots burst from the ground, entangling a targeted enemy.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double range = getDoubleParam(plugin, item, "range", 6.0);
        double rootDuration = getDoubleParam(plugin, item, "root_duration", 3.0);
        Entity target = player.getTargetEntity((int) range);
        if (!(target instanceof LivingEntity)) {
            player.sendMessage("§cNo target in range.");
            return false;
        }
        LivingEntity living = (LivingEntity) target;
        living.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) (rootDuration * 20), 6));
        living.getWorld().playSound(living.getLocation(), Sound.BLOCK_ROOTS_BREAK, 1.0f, 0.8f);
        Location rootLoc = living.getLocation();
        EffectUtils.burst(rootLoc,
                new EffectUtils.Layer(Particle.COMPOSTER, 10, 0.3, 0.1, 0.3, 0),
                new EffectUtils.Layer(Particle.DUST, 14, 0.35, 0.15, 0.35, 0,
                        new Particle.DustOptions(Color.fromRGB(101, 67, 33), 1.1f)));
        EffectUtils.ring(rootLoc.clone().add(0, 0.05, 0), 0.9, 8, new EffectUtils.Layer(Particle.DUST, 1, 0.05, 0.05, 0.05, 0,
                new Particle.DustOptions(Color.fromRGB(76, 153, 0), 1.0f)));
        return true;
    }
}

class PrismBeam extends Ability {
    private final ItemEditFull plugin;
    public PrismBeam(ItemEditFull plugin) {
        super("prism_beam", "Prism Beam", "Fires a refracting beam of light that damages all enemies in a line.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double range = getDoubleParam(plugin, item, "range", 10.0);
        double damage = getDoubleParam(plugin, item, "damage", 6.0);
        Location origin = player.getEyeLocation();
        Vector dir = origin.getDirection().normalize();
        player.getWorld().playSound(origin, Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 1.3f);
        EffectUtils.burst(origin, new EffectUtils.Layer(Particle.FLASH, 1, 0, 0, 0, 0));
        int[][] prismColors = {{255, 0, 0}, {255, 140, 0}, {255, 255, 0}, {0, 200, 0}, {0, 120, 255}, {140, 0, 255}};
        for (int i = 0; i < range; i++) {
            Location point = origin.clone().add(dir.clone().multiply(i));
            point.getWorld().spawnParticle(Particle.END_ROD, point, 1, 0, 0, 0, 0);
            int[] c = prismColors[i % prismColors.length];
            point.getWorld().spawnParticle(Particle.DUST, point, 2, 0.05, 0.05, 0.05, 0,
                    new Particle.DustOptions(Color.fromRGB(c[0], c[1], c[2]), 0.8f));
            for (Entity entity : point.getWorld().getNearbyEntities(point, 1.0, 1.0, 1.0)) {
                if (entity instanceof LivingEntity && !entity.equals(player)) {
                    ((LivingEntity) entity).damage(damage, player);
                }
            }
        }
        return true;
    }
}

class SecondWind extends Ability {
    private final ItemEditFull plugin;
    public SecondWind(ItemEditFull plugin) {
        super("second_wind", "Second Wind", "Grants a burst of regeneration and speed when your health drops low.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double thresholdPercent = getDoubleParam(plugin, item, "threshold_hp", 0.3);
        double duration = getDoubleParam(plugin, item, "duration", 5.0);
        double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        if (player.getHealth() > maxHealth * thresholdPercent) {
            player.sendMessage("§cYou aren't hurt badly enough to catch a second wind.");
            return false;
        }
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, (int) (duration * 20), 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, (int) (duration * 20), 1));
        Location windLoc = player.getLocation().add(0, 1.2, 0);
        EffectUtils.fanfare(windLoc,
                new EffectUtils.SoundLayer(Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.5f),
                new EffectUtils.SoundLayer(Sound.ITEM_TOTEM_USE, 0.4f, 1.8f));
        EffectUtils.burst(windLoc,
                new EffectUtils.Layer(Particle.HEART, 8, 0.2, 0.2, 0.2, 0.01),
                new EffectUtils.Layer(Particle.VILLAGER_HAPPY, 10, 0.3, 0.3, 0.3, 0.02));
        EffectUtils.ring(player.getLocation().add(0, 0.1, 0), 1.0, 10, new EffectUtils.Layer(Particle.VILLAGER_HAPPY, 1, 0.05, 0.1, 0.05, 0.01));
        return true;
    }
}

class BattleFocus extends Ability {
    private final ItemEditFull plugin;
    public BattleFocus(ItemEditFull plugin) {
        super("battle_focus", "Battle Focus", "Sharpens your focus, boosting attack damage for a short time.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 8.0);
        int amplifier = getIntParam(plugin, item, "amplifier", 1);
        player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, (int) (duration * 20), amplifier));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 0.8f);
        Location focusLoc = player.getLocation().add(0, 1.5, 0);
        EffectUtils.burst(focusLoc,
                new EffectUtils.Layer(Particle.CRIT, 10, 0.3, 0.3, 0.3, 0.1),
                new EffectUtils.Layer(Particle.FIREWORK_SPARK, 12, 0.25, 0.35, 0.25, 0.05));
        EffectUtils.staggered(plugin, player, 3, 4L, i ->
                player.getWorld().spawnParticle(Particle.CRIT, player.getLocation().add(0, 1.2, 0), 5, 0.2, 0.2, 0.2, 0.05));
        return true;
    }
}

class TidalPull extends Ability {
    private final ItemEditFull plugin;
    public TidalPull(ItemEditFull plugin) {
        super("tidal_pull", "Tidal Pull", "Pulls all nearby enemies toward you in a wave of force.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double radius = getDoubleParam(plugin, item, "radius", 6.0);
        Location loc = player.getLocation();
        loc.getWorld().playSound(loc, Sound.ENTITY_PLAYER_SPLASH_HIGH_SPEED, 1.0f, 0.9f);
        loc.getWorld().spawnParticle(Particle.WATER_SPLASH, loc.add(0, 1, 0), 30, radius * 0.3, 0.4, radius * 0.3, 0.1);
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
            if (entity instanceof LivingEntity && !entity.equals(player)) {
                LivingEntity living = (LivingEntity) entity;
                Vector pull = VectorUtils.safeNormalize(loc.toVector().subtract(living.getLocation().toVector())).multiply(1.2).setY(0.2);
                living.setVelocity(pull);
            }
        }
        return true;
    }
}

class ThornmailAura extends Ability {
    private final ItemEditFull plugin;
    public ThornmailAura(ItemEditFull plugin) {
        super("thornmail_aura", "Thornmail Aura", "Reflects a portion of melee damage back to attackers.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 8.0);
        double reflect = getDoubleParam(plugin, item, "reflect", 0.25);
        GeneralExpansionAbilities.activateThornmail(player.getUniqueId(), System.currentTimeMillis() + (long) (duration * 1000), reflect);
        player.getWorld().playSound(player.getLocation(), Sound.ENCHANT_THORNS_HIT, 1.0f, 0.8f);
        Location thornLoc = player.getLocation().add(0, 1, 0);
        EffectUtils.burst(thornLoc,
                new EffectUtils.Layer(Particle.DUST, 16, 0.3, 0.4, 0.3, 0,
                        new Particle.DustOptions(Color.fromRGB(60, 130, 40), 1.2f)),
                new EffectUtils.Layer(Particle.CRIT, 6, 0.25, 0.3, 0.25, 0.05));
        EffectUtils.ring(player.getLocation().add(0, 0.1, 0), 1.1, 12,
                new EffectUtils.Layer(Particle.DUST, 1, 0.05, 0.1, 0.05, 0,
                        new Particle.DustOptions(Color.fromRGB(60, 130, 40), 1.0f)));
        return true;
    }
}

class SolarRay extends Ability {
    private final ItemEditFull plugin;
    public SolarRay(ItemEditFull plugin) {
        super("solar_ray", "Solar Ray", "Channels a beam of concentrated sunlight, dealing bonus damage during the day.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double range = getDoubleParam(plugin, item, "range", 10.0);
        double damage = getDoubleParam(plugin, item, "damage", 6.0);
        double dayBonus = getDoubleParam(plugin, item, "day_bonus", 0.5);
        long time = player.getWorld().getTime();
        boolean isDay = time < 12000;
        double totalDamage = isDay ? damage * (1 + dayBonus) : damage;
        Location origin = player.getEyeLocation();
        Vector dir = origin.getDirection().normalize();
        player.getWorld().playSound(origin, Sound.BLOCK_BEACON_AMBIENT, 1.0f, 1.5f);
        EffectUtils.burst(origin,
                new EffectUtils.Layer(Particle.FLAME, 10, 0.15, 0.15, 0.15, 0.01),
                new EffectUtils.Layer(Particle.FIREWORK_SPARK, isDay ? 14 : 6, 0.2, 0.2, 0.2, 0.02));
        for (int i = 0; i < range; i++) {
            Location point = origin.clone().add(dir.clone().multiply(i));
            point.getWorld().spawnParticle(Particle.END_ROD, point, 1, 0, 0, 0, 0);
            point.getWorld().spawnParticle(Particle.FLAME, point, 1, 0.03, 0.03, 0.03, 0.001);
            for (Entity entity : point.getWorld().getNearbyEntities(point, 1.0, 1.0, 1.0)) {
                if (entity instanceof LivingEntity && !entity.equals(player)) {
                    ((LivingEntity) entity).damage(totalDamage, player);
                }
            }
        }
        return true;
    }
}

class LunarVeil extends Ability {
    private final ItemEditFull plugin;
    public LunarVeil(ItemEditFull plugin) {
        super("lunar_veil", "Lunar Veil", "Grants night vision and stealth while under open sky at night.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 10.0);
        long time = player.getWorld().getTime();
        boolean isNight = time >= 13000 && time <= 23000;
        boolean underSky = player.getLocation().getBlock().getLightFromSky() > 0;
        if (!isNight || !underSky) {
            player.sendMessage("§cYou must stand beneath the open night sky.");
            return false;
        }
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, (int) (duration * 20), 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, (int) (duration * 20), 0));
        Location veilLoc = player.getLocation().add(0, 1, 0);
        player.getWorld().playSound(player.getLocation(), Sound.AMBIENT_CAVE, 0.5f, 1.6f);
        EffectUtils.burst(veilLoc,
                new EffectUtils.Layer(Particle.DUST, 16, 0.3, 0.5, 0.3, 0,
                        new Particle.DustOptions(Color.fromRGB(150, 160, 220), 1.3f)),
                new EffectUtils.Layer(Particle.PORTAL, 8, 0.25, 0.4, 0.25, 0.02));
        EffectUtils.ring(player.getLocation().add(0, 0.05, 0), 1.0, 10,
                new EffectUtils.Layer(Particle.DUST, 1, 0.05, 0.05, 0.05, 0,
                        new Particle.DustOptions(Color.fromRGB(150, 160, 220), 1.0f)));
        return true;
    }
}

class PhaseShift extends Ability {
    private final ItemEditFull plugin;
    public PhaseShift(ItemEditFull plugin) {
        super("phase_shift", "Phase Shift", "Briefly become intangible, passing through enemies and projectiles.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 1.0);
        player.setInvulnerable(true);
        player.playEffect(EntityEffect.TOTEM_RESURRECT);
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_CHORUS_FRUIT_TELEPORT, 0.7f, 1.6f);
        new CompatRunnable() {
            @Override
            public void run() {
                player.setInvulnerable(false);
            }
        }.runTaskLater(plugin, player, (long) (duration * 20));
        return true;
    }
}

class GravityFlip extends Ability {
    private final ItemEditFull plugin;
    public GravityFlip(ItemEditFull plugin) {
        super("gravity_flip", "Gravity Flip", "Inverts gravity beneath a target, sending them tumbling upward.");
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
        living.setVelocity(new Vector(0, 1.6, 0));
        living.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 60, 0));
        living.getWorld().playSound(living.getLocation(), Sound.ENTITY_SHULKER_TELEPORT, 1.0f, 0.8f);
        living.getWorld().spawnParticle(Particle.REVERSE_PORTAL, living.getLocation(), 20, 0.3, 0.5, 0.3, 0.05);
        return true;
    }
}

class CurseMark extends Ability {
    private final ItemEditFull plugin;
    public CurseMark(ItemEditFull plugin) {
        super("curse_mark", "Curse Mark", "Marks an enemy; the next damage dealt to them by anyone is amplified.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 6.0);
        double amplify = getDoubleParam(plugin, item, "amplify", 0.25);
        Entity target = player.getTargetEntity(8);
        if (!(target instanceof LivingEntity)) {
            player.sendMessage("§cNo target in sight.");
            return false;
        }
        LivingEntity living = (LivingEntity) target;
        GeneralExpansionAbilities.markCurse(living.getUniqueId(), System.currentTimeMillis() + (long) (duration * 1000), amplify);
        living.getWorld().playSound(living.getLocation(), Sound.ENTITY_WITCH_CELEBRATE, 1.0f, 0.7f);
        EffectUtils.burst(living.getLocation().add(0, 1.5, 0),
                new EffectUtils.Layer(Particle.SPELL_WITCH, 10, 0.3, 0.3, 0.3, 0),
                new EffectUtils.Layer(Particle.SOUL_FIRE_FLAME, 6, 0.2, 0.3, 0.2, 0.01));
        EffectUtils.ring(living.getLocation().add(0, 0.1, 0), 0.9, 8, new EffectUtils.Layer(Particle.SOUL_FIRE_FLAME, 1, 0.05, 0.1, 0.05, 0.01));
        return true;
    }
}

class BlessingTotem extends Ability {
    private final ItemEditFull plugin;
    public BlessingTotem(ItemEditFull plugin) {
        super("blessing_totem", "Blessing Totem", "Plants a totem that heals and buffs allies who stand near it.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double radius = getDoubleParam(plugin, item, "radius", 5.0);
        double duration = getDoubleParam(plugin, item, "duration", 10.0);
        Location totemLoc = player.getLocation();
        totemLoc.getWorld().playSound(totemLoc, Sound.ITEM_TOTEM_USE, 1.0f, 1.2f);
        new CompatRunnable() {
            int ticksElapsed = 0;
            @Override
            public void run() {
                if (ticksElapsed >= (duration * 20)) {
                    cancel();
                    return;
                }
                totemLoc.getWorld().spawnParticle(Particle.TOTEM, totemLoc.clone().add(0, 1, 0), 6, radius * 0.2, 0.4, radius * 0.2, 0.02);
                if (ticksElapsed % 20 == 0) {
                    for (Entity entity : totemLoc.getWorld().getNearbyEntities(totemLoc, radius, 2.0, radius)) {
                        if (entity instanceof Player) {
                            Player ally = (Player) entity;
                            double maxHealth = ally.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                            ally.setHealth(Math.min(maxHealth, ally.getHealth() + 1.0));
                            ally.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 30, 0));
                        }
                    }
                }
                ticksElapsed += 5;
            }
        }.runTaskTimer(plugin, totemLoc, 0L, 5L);
        return true;
    }
}
