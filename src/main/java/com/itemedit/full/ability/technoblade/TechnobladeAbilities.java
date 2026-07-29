package com.itemedit.full.ability.technoblade;

import com.itemedit.full.ItemEditFull;
import com.itemedit.full.ability.Ability;
import com.itemedit.full.utils.CompatRunnable;
import com.itemedit.full.utils.VectorUtils;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wither;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
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
 * The Blood God's Legacy: 10 abilities in memory of Technoblade, drawn from his
 * documented catchphrases, persona, and his final charity stream for the Sarcoma
 * Foundation of America. A brand-new school with no prior abilities.
 */
public class TechnobladeAbilities implements Listener {

    // ---- Technoblade Never Dies: survives fatal damage once, then rises empowered ----
    private static final Map<UUID, Long> reviveReady = new ConcurrentHashMap<>();

    // ---- Blood for the Blood God: heals the caster for a portion of the next few hits dealt ----
    private static final Map<UUID, Long> bloodGodActive = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> bloodGodHitsRemaining = new ConcurrentHashMap<>();
    private static final Map<UUID, Double> bloodGodLifesteal = new ConcurrentHashMap<>();

    // ---- Another Hundred Years: a stacking curse that grows every time the caster hits the target ----
    private static final Map<UUID, Integer> tauntStacks = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> tauntExpire = new ConcurrentHashMap<>();
    private static final Map<UUID, UUID> tauntCaster = new ConcurrentHashMap<>();
    private static final Map<UUID, Double> tauntBonusPerStack = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> tauntMaxStacks = new ConcurrentHashMap<>();

    // ---- Circle Strafe: a guaranteed-dodge window ----
    private static final Map<UUID, Long> dodgeActive = new ConcurrentHashMap<>();

    // ---- So Long, Nerds: a farewell shield left for nearby allies ----
    private static final Map<UUID, Long> farewellShieldActive = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> farewellShieldHits = new ConcurrentHashMap<>();

    private static ItemEditFull pluginInstance;

    public static void register(ItemEditFull plugin) {
        pluginInstance = plugin;

        plugin.getAbilityManager().registerAbility(new TechnobladeNeverDies(plugin));
        plugin.getAbilityManager().registerAbility(new BloodForTheBloodGod(plugin));
        plugin.getAbilityManager().registerAbility(new CrownedPiglinRoyalty(plugin));
        plugin.getAbilityManager().registerAbility(new HundredYearsTaunt(plugin));
        plugin.getAbilityManager().registerAbility(new GreatPotatoFarmer(plugin));
        plugin.getAbilityManager().registerAbility(new CircleStrafeDash(plugin));
        plugin.getAbilityManager().registerAbility(new WolfPackCharity(plugin));
        plugin.getAbilityManager().registerAbility(new TechnothonWither(plugin));
        plugin.getAbilityManager().registerAbility(new OneOfUsRally(plugin));
        plugin.getAbilityManager().registerAbility(new SoLongNerds(plugin));

        plugin.getServer().getPluginManager().registerEvents(new TechnobladeAbilities(), plugin);
    }

    static void armRevive(UUID uuid, long expireAt) {
        reviveReady.put(uuid, expireAt);
    }

    static void activateBloodGod(UUID uuid, long expireAt, int hits, double lifesteal) {
        bloodGodActive.put(uuid, expireAt);
        bloodGodHitsRemaining.put(uuid, hits);
        bloodGodLifesteal.put(uuid, lifesteal);
    }

    static void markTaunt(UUID targetUuid, UUID casterUuid, long expireAt, double bonusPerStack, int maxStacks) {
        // Refresh the mark; only reset the stack count if a different caster re-marks the target.
        if (!casterUuid.equals(tauntCaster.get(targetUuid))) {
            tauntStacks.put(targetUuid, 0);
        }
        tauntCaster.put(targetUuid, casterUuid);
        tauntExpire.put(targetUuid, expireAt);
        tauntBonusPerStack.put(targetUuid, bonusPerStack);
        tauntMaxStacks.put(targetUuid, maxStacks);
    }

    static void activateDodge(UUID uuid, long expireAt) {
        dodgeActive.put(uuid, expireAt);
    }

    static void addFarewellShield(UUID uuid, long expireAt, int hits) {
        farewellShieldActive.put(uuid, expireAt);
        farewellShieldHits.put(uuid, hits);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFatalDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity();
        Long expire = reviveReady.get(player.getUniqueId());
        if (expire == null || System.currentTimeMillis() >= expire) {
            return;
        }
        if (player.getHealth() - event.getFinalDamage() > 0) {
            return;
        }
        reviveReady.remove(player.getUniqueId());
        event.setCancelled(true);
        player.setHealth(Math.min(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue(), 6.0));
        player.setFireTicks(0);
        player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 200, 2));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 1));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 0.6f);
        player.getWorld().spawnParticle(Particle.CRIT, player.getLocation().add(0, 1, 0), 40, 0.4, 0.6, 0.4, 0.15);
        player.sendMessage("§cTechnoblade never dies.");
    }

    @EventHandler
    public void onBloodGodHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getDamager();
        Long expire = bloodGodActive.get(player.getUniqueId());
        if (expire == null) {
            return;
        }
        if (System.currentTimeMillis() >= expire || bloodGodHitsRemaining.getOrDefault(player.getUniqueId(), 0) <= 0) {
            clearBloodGod(player.getUniqueId());
            return;
        }
        double percent = bloodGodLifesteal.getOrDefault(player.getUniqueId(), 0.3);
        double healAmount = event.getFinalDamage() * percent;
        double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        player.setHealth(Math.min(maxHealth, player.getHealth() + healAmount));
        player.getWorld().spawnParticle(Particle.CRIT_MAGIC, event.getEntity().getLocation().add(0, 1, 0), 10, 0.2, 0.3, 0.2, 0.05);
        int remaining = bloodGodHitsRemaining.getOrDefault(player.getUniqueId(), 1) - 1;
        if (remaining <= 0) {
            clearBloodGod(player.getUniqueId());
        } else {
            bloodGodHitsRemaining.put(player.getUniqueId(), remaining);
        }
    }

    @EventHandler
    public void onTauntHit(EntityDamageByEntityEvent event) {
        UUID targetId = event.getEntity().getUniqueId();
        Long expire = tauntExpire.get(targetId);
        if (expire == null) {
            return;
        }
        if (System.currentTimeMillis() >= expire) {
            clearTaunt(targetId);
            return;
        }
        UUID casterId = tauntCaster.get(targetId);
        if (casterId == null || !(event.getDamager() instanceof Player) || !((Player) event.getDamager()).getUniqueId().equals(casterId)) {
            return;
        }
        int stacks = tauntStacks.getOrDefault(targetId, 0);
        if (stacks > 0) {
            double bonusPerStack = tauntBonusPerStack.getOrDefault(targetId, 1.0);
            event.setDamage(event.getDamage() + (stacks * bonusPerStack));
        }
        int maxStacks = tauntMaxStacks.getOrDefault(targetId, 5);
        tauntStacks.put(targetId, Math.min(maxStacks, stacks + 1));
        event.getEntity().getWorld().spawnParticle(Particle.CRIT, event.getEntity().getLocation().add(0, 1, 0), 6 + stacks * 2, 0.2, 0.3, 0.2, 0.05);
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
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_HORSE_JUMP, 0.6f, 1.6f);
        }
        dodgeActive.remove(player.getUniqueId());
    }

    @EventHandler
    public void onFarewellShieldDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity();
        Long expire = farewellShieldActive.get(player.getUniqueId());
        if (expire == null) {
            return;
        }
        if (System.currentTimeMillis() >= expire || farewellShieldHits.getOrDefault(player.getUniqueId(), 0) <= 0) {
            clearFarewellShield(player.getUniqueId());
            return;
        }
        event.setCancelled(true);
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 0.6f, 1.4f);
        int remaining = farewellShieldHits.getOrDefault(player.getUniqueId(), 1) - 1;
        if (remaining <= 0) {
            clearFarewellShield(player.getUniqueId());
        } else {
            farewellShieldHits.put(player.getUniqueId(), remaining);
        }
    }

    // This school's summoned allies (wolves, the charity Wither) are tagged with the plugin-wide
    // "helper" metadata convention, which ZombieAbilities.onHelperTarget/onHelperDamage already
    // protects unconditionally against attacking any player - no separate handler needed here.

    private static void clearBloodGod(UUID uuid) {
        bloodGodActive.remove(uuid);
        bloodGodHitsRemaining.remove(uuid);
        bloodGodLifesteal.remove(uuid);
    }

    private static void clearTaunt(UUID uuid) {
        tauntStacks.remove(uuid);
        tauntExpire.remove(uuid);
        tauntCaster.remove(uuid);
        tauntBonusPerStack.remove(uuid);
        tauntMaxStacks.remove(uuid);
    }

    private static void clearFarewellShield(UUID uuid) {
        farewellShieldActive.remove(uuid);
        farewellShieldHits.remove(uuid);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        reviveReady.remove(id);
        clearBloodGod(id);
        dodgeActive.remove(id);
        clearFarewellShield(id);
    }
}

// ==================== 10 ABILITIES IN MEMORY OF TECHNOBLADE ====================

class TechnobladeNeverDies extends Ability {
    private final ItemEditFull plugin;
    public TechnobladeNeverDies(ItemEditFull plugin) {
        super("technoblade_never_dies", "Technoblade Never Dies", "If you'd take fatal damage, cheat death once and rise with a burst of strength.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double window = getDoubleParam(plugin, item, "window", 60.0);
        TechnobladeAbilities.armRevive(player.getUniqueId(), System.currentTimeMillis() + (long) (window * 1000));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.6f, 1.2f);
        player.getWorld().spawnParticle(Particle.SOUL, player.getLocation().add(0, 1, 0), 20, 0.3, 0.5, 0.3, 0.02);
        player.sendMessage("§cSome things never die.");
        return true;
    }
}

class BloodForTheBloodGod extends Ability {
    private final ItemEditFull plugin;
    public BloodForTheBloodGod(ItemEditFull plugin) {
        super("blood_for_the_blood_god", "Blood for the Blood God", "Rally the battle cry; your next several hits heal you for a portion of the damage dealt.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 6.0);
        int hits = getIntParam(plugin, item, "hits", 4);
        double lifesteal = getDoubleParam(plugin, item, "lifesteal_percent", 0.3);
        TechnobladeAbilities.activateBloodGod(player.getUniqueId(), System.currentTimeMillis() + (long) (duration * 1000), hits, lifesteal);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_RAVAGER_ROAR, 1.0f, 0.7f);
        player.getWorld().spawnParticle(Particle.CRIT, player.getLocation().add(0, 1, 0), 20, 0.3, 0.4, 0.3, 0.1);
        player.sendMessage("§4Blood for the Blood God!");
        return true;
    }
}

class CrownedPiglinRoyalty extends Ability {
    private final ItemEditFull plugin;
    public CrownedPiglinRoyalty(ItemEditFull plugin) {
        super("crowned_piglin_royalty", "Crowned Piglin Royalty", "Don the crown and the royal red cape, granting resistance and strength.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 12.0);
        int resistanceAmp = getIntParam(plugin, item, "resistance_amplifier", 1);
        int strengthAmp = getIntParam(plugin, item, "strength_amplifier", 1);
        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, (int) (duration * 20), resistanceAmp));
        player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, (int) (duration * 20), strengthAmp));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PIGLIN_ADMIRING_ITEM, 1.0f, 0.7f);
        player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation().add(0, 1.5, 0), 15, 0.3, 0.4, 0.3, 0.02);
        return true;
    }
}

class HundredYearsTaunt extends Ability {
    private final ItemEditFull plugin;
    public HundredYearsTaunt(ItemEditFull plugin) {
        super("hundred_years_taunt", "Another Hundred Years", "Taunt a target with an escalating curse - each hit you land on them grows stronger than the last.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "mark_duration", 15.0);
        double bonusPerStack = getDoubleParam(plugin, item, "bonus_damage_per_stack", 1.0);
        int maxStacks = getIntParam(plugin, item, "max_stacks", 5);
        Entity target = player.getTargetEntity(8);
        if (!(target instanceof LivingEntity)) {
            player.sendMessage("§cNo target in sight to taunt.");
            return false;
        }
        LivingEntity living = (LivingEntity) target;
        TechnobladeAbilities.markTaunt(living.getUniqueId(), player.getUniqueId(), System.currentTimeMillis() + (long) (duration * 1000), bonusPerStack, maxStacks);
        living.getWorld().playSound(living.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 0.6f);
        String targetName = living instanceof Player ? ((Player) living).getName() : living.getName();
        player.sendMessage("§eIf you wish to defeat me, " + targetName + ", train for another hundred years.");
        return true;
    }
}

class GreatPotatoFarmer extends Ability {
    private final ItemEditFull plugin;
    public GreatPotatoFarmer(ItemEditFull plugin) {
        super("great_potato_farmer", "Great Potato War", "Channel hundreds of hours of dedication, slowly restoring your health the longer you hold steady.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 15.0);
        double healPerTick = getDoubleParam(plugin, item, "heal_tick", 1.0);
        Location startLoc = player.getLocation();
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_CROP_BREAK, 1.0f, 0.8f);
        new CompatRunnable() {
            int ticksElapsed = 0;
            @Override
            public void run() {
                if (!player.isOnline() || ticksElapsed >= (duration * 20)) {
                    cancel();
                    return;
                }
                if (player.getLocation().distanceSquared(startLoc) > 1.0) {
                    player.sendMessage("§6You stopped farming - the harvest ends early.");
                    cancel();
                    return;
                }
                double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                player.setHealth(Math.min(maxHealth, player.getHealth() + healPerTick));
                player.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, player.getLocation().add(0, 1, 0), 3, 0.2, 0.2, 0.2, 0.01);
                ticksElapsed += 20;
            }
        }.runTaskTimer(plugin, player, 20L, 20L);
        return true;
    }
}

class CircleStrafeDash extends Ability {
    private final ItemEditFull plugin;
    public CircleStrafeDash(ItemEditFull plugin) {
        super("circle_strafe_dash", "Circle Strafe", "Dash in a tight arc around your target, dodging their next attack and repositioning for a counter.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double radius = getDoubleParam(plugin, item, "radius", 3.0);
        double dodgeWindow = getDoubleParam(plugin, item, "dodge_window", 1.0);
        Entity target = player.getTargetEntity((int) (radius * 3));
        if (!(target instanceof LivingEntity)) {
            player.sendMessage("§cNo target nearby to circle.");
            return false;
        }
        LivingEntity living = (LivingEntity) target;
        Vector toPlayer = VectorUtils.safeNormalize(player.getLocation().toVector().subtract(living.getLocation().toVector()));
        Vector tangent = new Vector(-toPlayer.getZ(), 0, toPlayer.getX());
        if (Math.random() < 0.5) {
            tangent.multiply(-1);
        }
        Location arcPoint = living.getLocation().clone().add(toPlayer.multiply(radius)).add(tangent.multiply(radius * 0.7));
        arcPoint.setY(player.getLocation().getY());
        arcPoint.setDirection(living.getLocation().toVector().subtract(arcPoint.toVector()));
        player.teleport(arcPoint);
        TechnobladeAbilities.activateDodge(player.getUniqueId(), System.currentTimeMillis() + (long) (dodgeWindow * 1000));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.8f, 1.4f);
        player.getWorld().spawnParticle(Particle.SWEEP_ATTACK, player.getLocation().add(0, 1, 0), 3, 0.2, 0.2, 0.2, 0);
        return true;
    }
}

class WolfPackCharity extends Ability {
    private final ItemEditFull plugin;
    public WolfPackCharity(ItemEditFull plugin) {
        super("wolf_pack_charity", "Call the Pack", "In the spirit of his charity stream, summon a loyal pack of wolves to fight at your side.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        int wolfCount = getIntParam(plugin, item, "wolf_count", 3);
        double lifetime = getDoubleParam(plugin, item, "lifetime", 20.0);
        Location loc = player.getLocation();
        loc.getWorld().playSound(loc, Sound.ENTITY_WOLF_HOWL, 1.0f, 1.0f);
        for (int i = 0; i < wolfCount; i++) {
            double angle = i * 2 * Math.PI / wolfCount;
            Location spawnLoc = loc.clone().add(Math.cos(angle) * 1.5, 0, Math.sin(angle) * 1.5);
            Wolf wolf = (Wolf) spawnLoc.getWorld().spawnEntity(spawnLoc, EntityType.WOLF);
            wolf.setTamed(true);
            wolf.setOwner(player);
            wolf.setMetadata("helper", new FixedMetadataValue(plugin, player.getUniqueId().toString()));
            for (Entity entity : wolf.getNearbyEntities(10, 5, 10)) {
                if (entity instanceof Monster) {
                    wolf.setTarget((LivingEntity) entity);
                    break;
                }
            }
            new CompatRunnable() {
                @Override
                public void run() {
                    if (wolf.isValid()) {
                        wolf.getWorld().spawnParticle(Particle.HEART, wolf.getLocation().add(0, 1, 0), 5, 0.2, 0.2, 0.2, 0.01);
                        wolf.remove();
                    }
                }
            }.runTaskLater(plugin, player, (long) (lifetime * 20));
        }
        return true;
    }
}

class TechnothonWither extends Ability {
    private final ItemEditFull plugin;
    public TechnothonWither(ItemEditFull plugin) {
        super("technothon_wither", "For the Cause", "Echoing the charity stream that raised hundreds of thousands for cancer research, summon a Wither to fight briefly on your behalf.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double lifetime = getDoubleParam(plugin, item, "wither_lifetime", 15.0);
        Location loc = player.getLocation();
        Wither wither;
        try {
            wither = (Wither) loc.getWorld().spawnEntity(loc, EntityType.WITHER);
        } catch (Exception e) {
            player.sendMessage("§cThe cause could not be summoned here.");
            return false;
        }
        wither.setCustomName("§5" + player.getName() + "'s Cause");
        wither.setCustomNameVisible(true);
        wither.setMetadata("helper", new FixedMetadataValue(plugin, player.getUniqueId().toString()));
        loc.getWorld().playSound(loc, Sound.ENTITY_WITHER_SPAWN, 1.0f, 1.0f);
        for (Entity entity : wither.getNearbyEntities(15, 8, 15)) {
            if (entity instanceof Monster) {
                wither.setTarget((LivingEntity) entity);
                break;
            }
        }
        player.sendMessage("§5For the cause.");
        new CompatRunnable() {
            @Override
            public void run() {
                if (wither.isValid()) {
                    wither.getWorld().spawnParticle(Particle.SMOKE_LARGE, wither.getLocation().add(0, 2, 0), 25, 0.5, 0.8, 0.5, 0.05);
                    wither.remove();
                }
            }
        }.runTaskLater(plugin, player, (long) (lifetime * 20));
        return true;
    }
}

class OneOfUsRally extends Ability {
    private final ItemEditFull plugin;
    public OneOfUsRally(ItemEditFull plugin) {
        super("one_of_us_rally", "One of Us!", "Rally everyone nearby with the old chat chant, granting allies a shared burst of courage and speed.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double radius = getDoubleParam(plugin, item, "radius", 8.0);
        double duration = getDoubleParam(plugin, item, "duration", 8.0);
        Location loc = player.getLocation();
        loc.getWorld().playSound(loc, Sound.ENTITY_VILLAGER_CELEBRATE, 1.0f, 0.9f);
        loc.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, loc.add(0, 1.5, 0), 20, radius * 0.3, 0.4, radius * 0.3, 0);
        int rallied = 0;
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
            if (entity instanceof Player) {
                Player ally = (Player) entity;
                ally.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, (int) (duration * 20), 1));
                ally.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, (int) (duration * 20), 0));
                ally.sendMessage("§aOne of us! One of us!");
                rallied++;
            }
        }
        if (rallied == 0) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, (int) (duration * 20), 1));
            player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, (int) (duration * 20), 0));
        }
        return true;
    }
}

class SoLongNerds extends Ability {
    private final ItemEditFull plugin;
    public SoLongNerds(ItemEditFull plugin) {
        super("so_long_nerds", "So Long, Nerds", "A final farewell - vanish in a burst of light, leaving behind a blessing that heals and shields nearby allies.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double radius = getDoubleParam(plugin, item, "radius", 6.0);
        double heal = getDoubleParam(plugin, item, "heal", 4.0);
        double shieldDuration = getDoubleParam(plugin, item, "shield_duration", 6.0);
        int absorbHits = getIntParam(plugin, item, "absorb_hits", 1);
        double vanishDuration = getDoubleParam(plugin, item, "vanish_duration", 3.0);
        Location loc = player.getLocation();
        loc.getWorld().playSound(loc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        loc.getWorld().spawnParticle(Particle.END_ROD, loc.add(0, 1, 0), 40, radius * 0.3, 0.6, radius * 0.3, 0.05);
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, (int) (vanishDuration * 20), 0));
        long expireAt = System.currentTimeMillis() + (long) (shieldDuration * 1000);
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
            if (entity instanceof Player) {
                Player ally = (Player) entity;
                double maxHealth = ally.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                ally.setHealth(Math.min(maxHealth, ally.getHealth() + heal));
                TechnobladeAbilities.addFarewellShield(ally.getUniqueId(), expireAt, absorbHits);
                ally.sendMessage("§7So long, nerds.");
            }
        }
        return true;
    }
}
