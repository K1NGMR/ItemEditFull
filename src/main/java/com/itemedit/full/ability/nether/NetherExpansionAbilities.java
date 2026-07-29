package com.itemedit.full.ability.nether;

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
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Burning Wastes expansion: 20 additional ghast/piglin/blackstone-themed abilities
 * layered on top of the original 5 nether abilities.
 */
public class NetherExpansionAbilities implements Listener {

    // ---- Bastion Shield: blocks a set number of hits entirely ----
    private static final Map<UUID, Long> shieldActive = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> shieldHitsRemaining = new ConcurrentHashMap<>();

    private static ItemEditFull pluginInstance;

    public static void register(ItemEditFull plugin) {
        pluginInstance = plugin;

        plugin.getAbilityManager().registerAbility(new NetherrackEruption(plugin));
        plugin.getAbilityManager().registerAbility(new SoulFireBrand(plugin));
        plugin.getAbilityManager().registerAbility(new GhastTearHeal(plugin));
        plugin.getAbilityManager().registerAbility(new BlazePowderBurst(plugin));
        plugin.getAbilityManager().registerAbility(new WitherRoseCurse(plugin));
        plugin.getAbilityManager().registerAbility(new NetherWartPoison(plugin));
        plugin.getAbilityManager().registerAbility(new CrimsonSporeCloud(plugin));
        plugin.getAbilityManager().registerAbility(new WarpedTeleport(plugin));
        plugin.getAbilityManager().registerAbility(new PiglinCharge(plugin));
        plugin.getAbilityManager().registerAbility(new BastionShield(plugin));
        plugin.getAbilityManager().registerAbility(new HoglinRam(plugin));
        plugin.getAbilityManager().registerAbility(new NetherStarPulse(plugin));
        plugin.getAbilityManager().registerAbility(new QuartzShardVolley(plugin));
        plugin.getAbilityManager().registerAbility(new BasaltPillar(plugin));
        plugin.getAbilityManager().registerAbility(new SoulSandTrap(plugin));
        plugin.getAbilityManager().registerAbility(new MagmaCubeBounce(plugin));
        plugin.getAbilityManager().registerAbility(new WitherSkullBarrage(plugin));
        plugin.getAbilityManager().registerAbility(new NetherPortalStep(plugin));
        plugin.getAbilityManager().registerAbility(new GlowstoneFlashbang(plugin));
        plugin.getAbilityManager().registerAbility(new CrimsonVineSnare(plugin));

        plugin.getServer().getPluginManager().registerEvents(new NetherExpansionAbilities(), plugin);
    }

    static void activateShield(UUID uuid, long expireAt, int hits) {
        shieldActive.put(uuid, expireAt);
        shieldHitsRemaining.put(uuid, hits);
    }

    @EventHandler
    public void onShieldDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity();
        Long expire = shieldActive.get(player.getUniqueId());
        if (expire == null) {
            return;
        }
        if (System.currentTimeMillis() >= expire || shieldHitsRemaining.getOrDefault(player.getUniqueId(), 0) <= 0) {
            clearShield(player.getUniqueId());
            return;
        }
        event.setCancelled(true);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BASALT_BREAK, 1.0f, 0.7f);
        player.getWorld().spawnParticle(Particle.BLOCK_CRACK, player.getLocation().add(0, 1, 0), 15, 0.3, 0.4, 0.3, 0, Material.BLACKSTONE.createBlockData());
        int remaining = shieldHitsRemaining.getOrDefault(player.getUniqueId(), 1) - 1;
        if (remaining <= 0) {
            clearShield(player.getUniqueId());
        } else {
            shieldHitsRemaining.put(player.getUniqueId(), remaining);
        }
    }

    private static void clearShield(UUID uuid) {
        shieldActive.remove(uuid);
        shieldHitsRemaining.remove(uuid);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        clearShield(event.getPlayer().getUniqueId());
    }
}

// ==================== 20 NEW BURNING WASTES ABILITIES ====================

class NetherrackEruption extends Ability {
    private final ItemEditFull plugin;
    public NetherrackEruption(ItemEditFull plugin) {
        super("netherrack_eruption", "Netherrack Eruption", "Erupts spikes of netherrack from the ground in a line ahead of you.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double range = getDoubleParam(plugin, item, "range", 6.0);
        double damage = getDoubleParam(plugin, item, "damage", 6.0);
        Location start = player.getLocation();
        Vector dir = VectorUtils.safeNormalize(start.getDirection().setY(0));
        if (dir.lengthSquared() < 0.01) {
            dir = new Vector(0, 0, 1);
        }
        player.getWorld().playSound(start, Sound.BLOCK_NETHERRACK_BREAK, 1.2f, 0.7f);
        for (int i = 1; i <= (int) range; i++) {
            Location step = start.clone().add(dir.clone().multiply(i));
            step.getWorld().spawnParticle(Particle.BLOCK_CRACK, step.clone().add(0, 0.5, 0), 8, 0.2, 0.3, 0.2, 0, Material.NETHERRACK.createBlockData());
            for (Entity entity : step.getWorld().getNearbyEntities(step, 1.0, 1.5, 1.0)) {
                if (entity instanceof LivingEntity && !entity.equals(player)) {
                    ((LivingEntity) entity).damage(damage, player);
                }
            }
        }
        return true;
    }
}

class SoulFireBrand extends Ability {
    private final ItemEditFull plugin;
    public SoulFireBrand(ItemEditFull plugin) {
        super("soul_fire_brand", "Soul Fire Brand", "Brands the target with soul fire, dealing damage over time that resists extinguishing.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double dotDuration = getDoubleParam(plugin, item, "dot_duration", 6.0);
        double dotDamage = getDoubleParam(plugin, item, "dot_damage", 2.0);
        Entity target = player.getTargetEntity(6);
        if (!(target instanceof LivingEntity)) {
            player.sendMessage("§cNo target in range.");
            return false;
        }
        LivingEntity living = (LivingEntity) target;
        living.getWorld().playSound(living.getLocation(), Sound.ITEM_FIRECHARGE_USE, 1.0f, 0.5f);
        new CompatRunnable() {
            int ticksElapsed = 0;
            @Override
            public void run() {
                if (!living.isValid() || living.isDead() || ticksElapsed >= (dotDuration * 20)) {
                    cancel();
                    return;
                }
                living.damage(dotDamage, player);
                living.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, living.getLocation().add(0, 1, 0), 3, 0.2, 0.3, 0.2, 0.01);
                ticksElapsed += 20;
            }
        }.runTaskTimer(plugin, living, 20L, 20L);
        return true;
    }
}

class GhastTearHeal extends Ability {
    private final ItemEditFull plugin;
    public GhastTearHeal(ItemEditFull plugin) {
        super("ghast_tear_heal", "Ghast Tear Heal", "Consumes a ghast tear's essence to heal and cure negative effects.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double heal = getDoubleParam(plugin, item, "heal", 4.0);
        double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        player.setHealth(Math.min(maxHealth, player.getHealth() + heal));
        for (PotionEffect effect : new ArrayList<>(player.getActivePotionEffects())) {
            if (isNegative(effect.getType())) {
                player.removePotionEffect(effect.getType());
            }
        }
        Location healLoc = player.getLocation().add(0, 1.5, 0);
        EffectUtils.fanfare(healLoc,
                new EffectUtils.SoundLayer(Sound.ENTITY_GHAST_AMBIENT, 0.8f, 1.4f),
                new EffectUtils.SoundLayer(Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.6f));
        EffectUtils.burst(healLoc,
                new EffectUtils.Layer(Particle.HEART, 6, 0.2, 0.2, 0.2, 0.01),
                new EffectUtils.Layer(Particle.CRIT_MAGIC, 10, 0.25, 0.3, 0.25, 0.03));
        return true;
    }
    private boolean isNegative(PotionEffectType type) {
        return type == PotionEffectType.POISON || type == PotionEffectType.WITHER || type == PotionEffectType.SLOW
                || type == PotionEffectType.WEAKNESS || type == PotionEffectType.BLINDNESS || type == PotionEffectType.CONFUSION
                || type == PotionEffectType.HUNGER;
    }
}

class BlazePowderBurst extends Ability {
    private final ItemEditFull plugin;
    public BlazePowderBurst(ItemEditFull plugin) {
        super("blaze_powder_burst", "Blaze Powder Burst", "Ignites a cloud of blaze powder around you, burning nearby enemies.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double radius = getDoubleParam(plugin, item, "radius", 4.0);
        int fireTicks = getIntParam(plugin, item, "fire_ticks", 60);
        Location loc = player.getLocation();
        Location burstLoc = loc.clone().add(0, 1, 0);
        EffectUtils.fanfare(burstLoc,
                new EffectUtils.SoundLayer(Sound.ITEM_FIRECHARGE_USE, 1.0f, 1.2f),
                new EffectUtils.SoundLayer(Sound.ENTITY_BLAZE_BURN, 0.7f, 1.1f));
        EffectUtils.burst(burstLoc,
                new EffectUtils.Layer(Particle.FLAME, 30, radius * 0.4, 0.5, radius * 0.4, 0.05),
                new EffectUtils.Layer(Particle.LAVA, 4, radius * 0.3, 0.2, radius * 0.3, 0));
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
            if (entity instanceof LivingEntity && !entity.equals(player)) {
                entity.setFireTicks(fireTicks);
            }
        }
        return true;
    }
}

class WitherRoseCurse extends Ability {
    private final ItemEditFull plugin;
    public WitherRoseCurse(ItemEditFull plugin) {
        super("wither_rose_curse", "Wither Rose Curse", "Plants a curse that withers the target's health regeneration.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 8.0);
        int amplifier = getIntParam(plugin, item, "amplifier", 0);
        Entity target = player.getTargetEntity(8);
        if (!(target instanceof LivingEntity)) {
            player.sendMessage("§cNo target in sight.");
            return false;
        }
        LivingEntity living = (LivingEntity) target;
        living.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, (int) (duration * 20), amplifier));
        EffectUtils.fanfare(living.getLocation(),
                new EffectUtils.SoundLayer(Sound.ENTITY_WITHER_HURT, 1.0f, 0.8f),
                new EffectUtils.SoundLayer(Sound.BLOCK_SOUL_SAND_BREAK, 0.6f, 0.6f));
        EffectUtils.burst(living.getLocation().add(0, 1, 0),
                new EffectUtils.Layer(Particle.SMOKE_NORMAL, 10, 0.2, 0.3, 0.2, 0.02),
                new EffectUtils.Layer(Particle.SOUL, 8, 0.2, 0.3, 0.2, 0.02));
        return true;
    }
}

class NetherWartPoison extends Ability {
    private final ItemEditFull plugin;
    public NetherWartPoison(ItemEditFull plugin) {
        super("nether_wart_poison", "Nether Wart Poison", "Spits a brewed venom that poisons the target.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 5.0);
        int amplifier = getIntParam(plugin, item, "amplifier", 1);
        Entity target = player.getTargetEntity(10);
        if (!(target instanceof LivingEntity)) {
            player.sendMessage("§cNo target in sight.");
            return false;
        }
        LivingEntity living = (LivingEntity) target;
        living.addPotionEffect(new PotionEffect(PotionEffectType.POISON, (int) (duration * 20), amplifier));
        EffectUtils.fanfare(living.getLocation(),
                new EffectUtils.SoundLayer(Sound.ENTITY_SPIDER_HURT, 1.0f, 0.7f),
                new EffectUtils.SoundLayer(Sound.BLOCK_FUNGUS_BREAK, 0.5f, 0.8f));
        EffectUtils.burst(living.getLocation().add(0, 1, 0),
                new EffectUtils.Layer(Particle.VILLAGER_HAPPY, 8, 0.2, 0.3, 0.2, 0),
                new EffectUtils.Layer(Particle.SMOKE_NORMAL, 5, 0.2, 0.2, 0.2, 0.01));
        return true;
    }
}

class CrimsonSporeCloud extends Ability {
    private final ItemEditFull plugin;
    public CrimsonSporeCloud(ItemEditFull plugin) {
        super("crimson_spore_cloud", "Crimson Spore Cloud", "Releases a cloud of crimson spores that damage and nauseate enemies.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double radius = getDoubleParam(plugin, item, "radius", 4.0);
        double duration = getDoubleParam(plugin, item, "duration", 6.0);
        Location loc = player.getLocation();
        loc.getWorld().playSound(loc, Sound.BLOCK_FUNGUS_BREAK, 1.0f, 0.8f);
        loc.getWorld().spawnParticle(Particle.SPORE_BLOSSOM_AIR, loc.add(0, 1, 0), 25, radius * 0.4, 0.5, radius * 0.4, 0);
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, radius, 2.0, radius)) {
            if (entity instanceof LivingEntity && !entity.equals(player)) {
                LivingEntity living = (LivingEntity) entity;
                living.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, (int) (duration * 20), 0));
                living.damage(2.0, player);
            }
        }
        return true;
    }
}

class WarpedTeleport extends Ability {
    private final ItemEditFull plugin;
    public WarpedTeleport(ItemEditFull plugin) {
        super("warped_teleport", "Warped Teleport", "Teleport to a nearby warped fungus, or a short distance forward.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double range = getDoubleParam(plugin, item, "range", 8.0);
        Location origin = player.getLocation();
        Vector dir = VectorUtils.safeNormalize(player.getLocation().getDirection());
        Location dest = origin.clone();
        for (int i = (int) range; i >= 1; i--) {
            Location candidate = origin.clone().add(dir.clone().multiply(i));
            if (candidate.getBlock().getType().isAir() && candidate.clone().add(0, 1, 0).getBlock().getType().isAir()) {
                dest = candidate;
                break;
            }
        }
        EffectUtils.burst(origin.add(0, 1, 0),
                new EffectUtils.Layer(Particle.SOUL, 20, 0.3, 0.5, 0.3, 0.1),
                new EffectUtils.Layer(Particle.SOUL_FIRE_FLAME, 8, 0.2, 0.4, 0.2, 0.03));
        player.teleport(dest.setDirection(player.getLocation().getDirection()));
        EffectUtils.burst(dest.clone().add(0, 1, 0),
                new EffectUtils.Layer(Particle.SOUL, 20, 0.3, 0.5, 0.3, 0.1),
                new EffectUtils.Layer(Particle.SOUL_FIRE_FLAME, 8, 0.2, 0.4, 0.2, 0.03));
        dest.getWorld().playSound(dest, Sound.BLOCK_RESPAWN_ANCHOR_AMBIENT, 1.0f, 1.3f);
        return true;
    }
}

class PiglinCharge extends Ability {
    private final ItemEditFull plugin;
    public PiglinCharge(ItemEditFull plugin) {
        super("piglin_charge", "Piglin Charge", "A brutish charge that knocks enemies back and deals bonus damage to armoured foes.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double range = getDoubleParam(plugin, item, "range", 6.0);
        double damage = getDoubleParam(plugin, item, "damage", 6.0);
        double armoredBonus = getDoubleParam(plugin, item, "armored_bonus", 3.0);
        Location start = player.getLocation();
        Vector dir = VectorUtils.safeNormalize(start.getDirection().setY(0));
        if (dir.lengthSquared() < 0.01) {
            dir = new Vector(0, 0, 1);
        }
        final Vector chargeDir = dir;
        player.getWorld().playSound(start, Sound.ENTITY_PIGLIN_ANGRY, 1.3f, 0.8f);
        for (int i = 1; i <= (int) range; i++) {
            Location step = start.clone().add(chargeDir.clone().multiply(i)).add(0, 1, 0);
            step.getWorld().spawnParticle(Particle.CRIT, step, 3, 0.2, 0.2, 0.2, 0);
            for (Entity entity : step.getWorld().getNearbyEntities(step, 1.2, 1.2, 1.2)) {
                if (entity instanceof LivingEntity && !entity.equals(player)) {
                    LivingEntity living = (LivingEntity) entity;
                    double totalDamage = damage + (isArmored(living) ? armoredBonus : 0);
                    living.damage(totalDamage, player);
                    living.setVelocity(chargeDir.clone().multiply(1.3).setY(0.3));
                }
            }
        }
        player.setVelocity(chargeDir.clone().multiply(1.1).setY(Math.max(player.getVelocity().getY(), 0.1)));
        return true;
    }
    private boolean isArmored(LivingEntity living) {
        EntityEquipment eq = living.getEquipment();
        if (eq == null) {
            return false;
        }
        return !isEmpty(eq.getHelmet()) || !isEmpty(eq.getChestplate()) || !isEmpty(eq.getLeggings()) || !isEmpty(eq.getBoots());
    }
    private boolean isEmpty(ItemStack stack) {
        return stack == null || stack.getType().isAir();
    }
}

class BastionShield extends Ability {
    private final ItemEditFull plugin;
    public BastionShield(ItemEditFull plugin) {
        super("bastion_shield", "Bastion Shield", "Raises a shield of blackstone that blocks the next few hits.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 8.0);
        int absorbHits = getIntParam(plugin, item, "absorb_hits", 3);
        NetherExpansionAbilities.activateShield(player.getUniqueId(), System.currentTimeMillis() + (long) (duration * 1000), absorbHits);
        EffectUtils.fanfare(player.getLocation(),
                new EffectUtils.SoundLayer(Sound.BLOCK_BASALT_PLACE, 1.0f, 0.7f),
                new EffectUtils.SoundLayer(Sound.BLOCK_BASALT_BREAK, 0.5f, 0.5f));
        EffectUtils.burst(player.getLocation().add(0, 1, 0),
                new EffectUtils.Layer(Particle.BLOCK_CRACK, 20, 0.4, 0.6, 0.4, 0, Material.BLACKSTONE.createBlockData()));
        EffectUtils.ring(player.getLocation(), 1.2, 12,
                new EffectUtils.Layer(Particle.BLOCK_CRACK, 3, 0.05, 0.3, 0.05, 0, Material.BLACKSTONE.createBlockData()));
        player.sendMessage("§8A bastion shield surrounds you!");
        return true;
    }
}

class HoglinRam extends Ability {
    private final ItemEditFull plugin;
    public HoglinRam(ItemEditFull plugin) {
        super("hoglin_ram", "Hoglin Ram", "A ferocious ram attack that knocks the target far back.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double range = getDoubleParam(plugin, item, "range", 5.0);
        double damage = getDoubleParam(plugin, item, "damage", 5.0);
        double knockback = getDoubleParam(plugin, item, "knockback", 2.0);
        Entity target = player.getTargetEntity((int) range);
        if (!(target instanceof LivingEntity)) {
            player.sendMessage("§cNo target in range.");
            return false;
        }
        LivingEntity living = (LivingEntity) target;
        living.damage(damage, player);
        Vector away = VectorUtils.safeNormalize(living.getLocation().toVector().subtract(player.getLocation().toVector())).multiply(knockback).setY(0.4);
        living.setVelocity(away);
        living.getWorld().playSound(living.getLocation(), Sound.ENTITY_HOGLIN_ATTACK, 1.2f, 0.8f);
        return true;
    }
}

class NetherStarPulse extends Ability {
    private final ItemEditFull plugin;
    public NetherStarPulse(ItemEditFull plugin) {
        super("nether_star_pulse", "Nether Star Pulse", "Releases a pulse of nether star energy, healing you and damaging enemies.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double radius = getDoubleParam(plugin, item, "radius", 4.0);
        double heal = getDoubleParam(plugin, item, "heal", 3.0);
        double damage = getDoubleParam(plugin, item, "damage", 5.0);
        Location loc = player.getLocation();
        loc.getWorld().playSound(loc, Sound.ENTITY_WITHER_SPAWN, 1.0f, 1.5f);
        loc.getWorld().spawnParticle(Particle.END_ROD, loc.add(0, 1, 0), 30, radius * 0.3, 0.5, radius * 0.3, 0.05);
        double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        player.setHealth(Math.min(maxHealth, player.getHealth() + heal));
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
            if (entity instanceof LivingEntity && !entity.equals(player)) {
                ((LivingEntity) entity).damage(damage, player);
            }
        }
        return true;
    }
}

class QuartzShardVolley extends Ability {
    private final ItemEditFull plugin;
    public QuartzShardVolley(ItemEditFull plugin) {
        super("quartz_shard_volley", "Quartz Shard Volley", "Fires a volley of sharpened quartz shards.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        int count = getIntParam(plugin, item, "count", 5);
        double damageEach = getDoubleParam(plugin, item, "damage_each", 2.0);
        Location origin = player.getEyeLocation();
        Vector facing = origin.getDirection().normalize();
        player.getWorld().playSound(origin, Sound.BLOCK_STONE_HIT, 1.0f, 1.2f);
        for (int i = 0; i < count; i++) {
            double spreadAngle = Math.toRadians((Math.random() - 0.5) * 25.0);
            Vector spread = new Vector(
                    facing.getX() * Math.cos(spreadAngle) - facing.getZ() * Math.sin(spreadAngle),
                    facing.getY(),
                    facing.getX() * Math.sin(spreadAngle) + facing.getZ() * Math.cos(spreadAngle)
            );
            for (int d = 1; d <= 8; d++) {
                Location point = origin.clone().add(spread.clone().multiply(d));
                point.getWorld().spawnParticle(Particle.CRIT_MAGIC, point, 1, 0, 0, 0, 0);
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

class BasaltPillar extends Ability {
    private final ItemEditFull plugin;
    public BasaltPillar(ItemEditFull plugin) {
        super("basalt_pillar", "Basalt Pillar", "Raises columns of basalt to trap an enemy in place.");
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
        Location base = living.getLocation().getBlock().getLocation();
        living.getWorld().playSound(base, Sound.BLOCK_BASALT_PLACE, 1.2f, 0.7f);

        List<Block> cage = new ArrayList<>();
        int[][] offsets = {{1, 0, 0}, {-1, 0, 0}, {0, 0, 1}, {0, 0, -1}};
        for (int[] off : offsets) {
            Block b = base.clone().add(off[0], 0, off[1]).getBlock();
            if (b.getType() == Material.AIR) {
                b.setType(Material.BASALT);
                cage.add(b);
            }
        }
        living.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) (rootDuration * 20) + 20, 6));
        new CompatRunnable() {
            @Override
            public void run() {
                for (Block b : cage) {
                    if (b.getType() == Material.BASALT) {
                        b.setType(Material.AIR);
                    }
                }
            }
        }.runTaskLater(plugin, base, (long) (rootDuration * 20));
        return true;
    }
}

class SoulSandTrap extends Ability {
    private final ItemEditFull plugin;
    public SoulSandTrap(ItemEditFull plugin) {
        super("soul_sand_trap", "Soul Sand Trap", "Turns the ground beneath a target to soul sand, slowing them heavily.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 5.0);
        Entity target = player.getTargetEntity(8);
        if (!(target instanceof LivingEntity)) {
            player.sendMessage("§cNo target in sight.");
            return false;
        }
        LivingEntity living = (LivingEntity) target;
        living.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) (duration * 20), 3));
        EffectUtils.fanfare(living.getLocation(),
                new EffectUtils.SoundLayer(Sound.BLOCK_SOUL_SAND_BREAK, 1.0f, 0.7f),
                new EffectUtils.SoundLayer(Sound.ENTITY_WITHER_SKELETON_AMBIENT, 0.5f, 0.8f));
        EffectUtils.burst(living.getLocation(),
                new EffectUtils.Layer(Particle.SOUL, 15, 0.3, 0.1, 0.3, 0.02),
                new EffectUtils.Layer(Particle.SMOKE_NORMAL, 6, 0.3, 0.1, 0.3, 0.01));
        return true;
    }
}

class MagmaCubeBounce extends Ability {
    private final ItemEditFull plugin;
    public MagmaCubeBounce(ItemEditFull plugin) {
        super("magma_cube_bounce", "Magma Cube Bounce", "Bounce like a magma cube, gaining fall immunity and a damaging landing.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double radius = getDoubleParam(plugin, item, "radius", 3.0);
        double damage = getDoubleParam(plugin, item, "damage", 4.0);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 60, 0));
        Vector v = player.getVelocity();
        player.setVelocity(new Vector(v.getX(), 1.0, v.getZ()));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_MAGMA_CUBE_JUMP, 1.2f, 0.8f);
        new CompatRunnable() {
            int ticksElapsed = 0;
            boolean wasAirborne = false;
            @Override
            public void run() {
                ticksElapsed += 1;
                if (!player.isOnline()) {
                    cancel();
                    return;
                }
                if (!player.isOnGround()) {
                    wasAirborne = true;
                }
                if ((wasAirborne && player.isOnGround()) || ticksElapsed >= 50) {
                    cancel();
                    Location loc = player.getLocation();
                    loc.getWorld().playSound(loc, Sound.ENTITY_MAGMA_CUBE_SQUISH, 1.2f, 0.7f);
                    loc.getWorld().spawnParticle(Particle.LAVA, loc, 15, radius * 0.3, 0.2, radius * 0.3, 0);
                    for (Entity entity : loc.getWorld().getNearbyEntities(loc, radius, 2.0, radius)) {
                        if (entity instanceof LivingEntity && !entity.equals(player)) {
                            ((LivingEntity) entity).damage(damage, player);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, player, 1L, 1L);
        return true;
    }
}

class WitherSkullBarrage extends Ability {
    private final ItemEditFull plugin;
    public WitherSkullBarrage(ItemEditFull plugin) {
        super("wither_skull_barrage", "Wither Skull Barrage", "Fires three wither skulls in a spread.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        int count = getIntParam(plugin, item, "count", 3);
        Location origin = player.getEyeLocation();
        Vector facing = origin.getDirection().normalize();
        EffectUtils.fanfare(origin,
                new EffectUtils.SoundLayer(Sound.ENTITY_WITHER_SHOOT, 1.0f, 1.0f),
                new EffectUtils.SoundLayer(Sound.ENTITY_WITHER_AMBIENT, 0.5f, 1.3f));
        EffectUtils.burst(origin,
                new EffectUtils.Layer(Particle.SMOKE_LARGE, 14, 0.15, 0.15, 0.15, 0.02),
                new EffectUtils.Layer(Particle.SOUL_FIRE_FLAME, 6, 0.1, 0.1, 0.1, 0.02));
        for (int i = 0; i < count; i++) {
            double spreadAngle = Math.toRadians((i - (count - 1) / 2.0) * 15.0);
            Vector spread = new Vector(
                    facing.getX() * Math.cos(spreadAngle) - facing.getZ() * Math.sin(spreadAngle),
                    facing.getY(),
                    facing.getX() * Math.sin(spreadAngle) + facing.getZ() * Math.cos(spreadAngle)
            );
            WitherSkull skull = player.getWorld().spawn(origin, WitherSkull.class);
            skull.setShooter(player);
            skull.setDirection(spread);
            skull.setVelocity(spread.multiply(1.2));
        }
        return true;
    }
}

class NetherPortalStep extends Ability {
    private final ItemEditFull plugin;
    public NetherPortalStep(ItemEditFull plugin) {
        super("nether_portal_step", "Nether Portal Step", "Opens a brief unstable portal, teleporting you a short distance.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double range = getDoubleParam(plugin, item, "range", 10.0);
        Location origin = player.getLocation();
        Vector dir = VectorUtils.safeNormalize(player.getLocation().getDirection());
        Location dest = origin.clone();
        for (int i = (int) range; i >= 1; i--) {
            Location candidate = origin.clone().add(dir.clone().multiply(i));
            if (candidate.getBlock().getType().isAir() && candidate.clone().add(0, 1, 0).getBlock().getType().isAir()) {
                dest = candidate;
                break;
            }
        }
        origin.getWorld().spawnParticle(Particle.PORTAL, origin.add(0, 1, 0), 30, 0.3, 0.5, 0.3, 0.3);
        origin.getWorld().playSound(origin, Sound.BLOCK_PORTAL_TRIGGER, 1.0f, 0.8f);
        player.teleport(dest.setDirection(player.getLocation().getDirection()));
        dest.getWorld().spawnParticle(Particle.PORTAL, dest.clone().add(0, 1, 0), 30, 0.3, 0.5, 0.3, 0.3);
        dest.getWorld().playSound(dest, Sound.BLOCK_PORTAL_AMBIENT, 1.0f, 1.0f);
        return true;
    }
}

class GlowstoneFlashbang extends Ability {
    private final ItemEditFull plugin;
    public GlowstoneFlashbang(ItemEditFull plugin) {
        super("glowstone_flashbang", "Glowstone Flashbang", "Shatters a glowstone cluster in a blinding flash.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double radius = getDoubleParam(plugin, item, "radius", 5.0);
        double blindDuration = getDoubleParam(plugin, item, "blind_duration", 4.0);
        Location loc = player.getLocation();
        loc.getWorld().playSound(loc, Sound.BLOCK_GLASS_BREAK, 1.3f, 1.5f);
        loc.getWorld().spawnParticle(Particle.FLASH, loc.add(0, 1, 0), 5, 0.2, 0.2, 0.2, 0);
        loc.getWorld().spawnParticle(Particle.BLOCK_CRACK, loc, 30, radius * 0.4, 0.5, radius * 0.4, 0, Material.GLOWSTONE.createBlockData());
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
            if (entity instanceof LivingEntity && !entity.equals(player)) {
                ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, (int) (blindDuration * 20), 0));
            }
        }
        return true;
    }
}

class CrimsonVineSnare extends Ability {
    private final ItemEditFull plugin;
    public CrimsonVineSnare(ItemEditFull plugin) {
        super("crimson_vine_snare", "Crimson Vine Snare", "Entangling vines burst from the ground, rooting nearby enemies.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double radius = getDoubleParam(plugin, item, "radius", 4.0);
        double rootDuration = getDoubleParam(plugin, item, "root_duration", 3.0);
        Location loc = player.getLocation();
        EffectUtils.fanfare(loc,
                new EffectUtils.SoundLayer(Sound.BLOCK_WEEPING_VINES_STEP, 1.0f, 0.8f),
                new EffectUtils.SoundLayer(Sound.BLOCK_NETHERRACK_BREAK, 0.6f, 0.6f));
        EffectUtils.ring(loc, radius, 16,
                new EffectUtils.Layer(Particle.BLOCK_CRACK, 4, 0.15, 0.4, 0.15, 0, Material.CRIMSON_ROOTS.createBlockData()));
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, radius, 2.0, radius)) {
            if (entity instanceof LivingEntity && !entity.equals(player)) {
                ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) (rootDuration * 20), 5));
            }
        }
        return true;
    }
}
