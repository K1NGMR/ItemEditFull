package com.itemedit.full.ability.undead;

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
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Bone Guard expansion: 20 additional arrow/marrow-themed abilities layered
 * on top of the original 5 skeleton abilities.
 */
public class SkeletonExpansionAbilities implements Listener {

    // ---- Calcified Armor / Phalanx Stance: flat damage reduction while active ----
    private static final Map<UUID, Long> flatReductionActive = new ConcurrentHashMap<>();
    private static final Map<UUID, Double> flatReductionAmount = new ConcurrentHashMap<>();

    // ---- Brittle Curse: multiplies damage taken while active ----
    private static final Map<UUID, Long> brittleActive = new ConcurrentHashMap<>();
    private static final Map<UUID, Double> brittleMultiplier = new ConcurrentHashMap<>();

    // ---- Undying Bones: survives fatal damage once, then gains a combat burst ----
    private static final Map<UUID, Long> undyingReady = new ConcurrentHashMap<>();

    private static ItemEditFull pluginInstance;

    public static void register(ItemEditFull plugin) {
        pluginInstance = plugin;

        plugin.getAbilityManager().registerAbility(new BoneSpearThrow(plugin));
        plugin.getAbilityManager().registerAbility(new MarrowDrain(plugin));
        plugin.getAbilityManager().registerAbility(new RattlingCharge(plugin));
        plugin.getAbilityManager().registerAbility(new CalcifiedArmor(plugin));
        plugin.getAbilityManager().registerAbility(new ArrowRain(plugin));
        plugin.getAbilityManager().registerAbility(new PiercingShot(plugin));
        plugin.getAbilityManager().registerAbility(new BoneWall(plugin));
        plugin.getAbilityManager().registerAbility(new SkeletalSummon(plugin));
        plugin.getAbilityManager().registerAbility(new QuiverRefill(plugin));
        plugin.getAbilityManager().registerAbility(new BrittleCurse(plugin));
        plugin.getAbilityManager().registerAbility(new DeathRattle(plugin));
        plugin.getAbilityManager().registerAbility(new BoneShrapnel(plugin));
        plugin.getAbilityManager().registerAbility(new LongbowSnipe(plugin));
        plugin.getAbilityManager().registerAbility(new UndyingBones(plugin));
        plugin.getAbilityManager().registerAbility(new SkullToss(plugin));
        plugin.getAbilityManager().registerAbility(new ArrowStorm(plugin));
        plugin.getAbilityManager().registerAbility(new BoneDustCloud(plugin));
        plugin.getAbilityManager().registerAbility(new RicochetShot(plugin));
        plugin.getAbilityManager().registerAbility(new PhalanxStance(plugin));
        plugin.getAbilityManager().registerAbility(new LastStandVolley(plugin));

        plugin.getServer().getPluginManager().registerEvents(new SkeletonExpansionAbilities(), plugin);
    }

    static void activateFlatReduction(UUID uuid, long expireAt, double reduction) {
        flatReductionActive.put(uuid, expireAt);
        flatReductionAmount.put(uuid, reduction);
    }

    static void activateBrittle(UUID uuid, long expireAt, double multiplier) {
        brittleActive.put(uuid, expireAt);
        brittleMultiplier.put(uuid, multiplier);
    }

    static void armUndying(UUID uuid, long expireAt) {
        undyingReady.put(uuid, expireAt);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onFlatReductionDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity();
        Long expire = flatReductionActive.get(player.getUniqueId());
        if (expire == null) {
            return;
        }
        if (System.currentTimeMillis() >= expire) {
            flatReductionActive.remove(player.getUniqueId());
            flatReductionAmount.remove(player.getUniqueId());
            return;
        }
        double reduction = flatReductionAmount.getOrDefault(player.getUniqueId(), 2.0);
        event.setDamage(Math.max(0.0, event.getDamage() - reduction));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBrittleDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity();
        Long expire = brittleActive.get(player.getUniqueId());
        if (expire == null) {
            return;
        }
        if (System.currentTimeMillis() >= expire) {
            brittleActive.remove(player.getUniqueId());
            brittleMultiplier.remove(player.getUniqueId());
            return;
        }
        double multiplier = brittleMultiplier.getOrDefault(player.getUniqueId(), 1.2);
        event.setDamage(event.getDamage() * multiplier);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFatalDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity();
        Long expire = undyingReady.get(player.getUniqueId());
        if (expire == null || System.currentTimeMillis() >= expire) {
            return;
        }
        if (player.getHealth() - event.getFinalDamage() > 0) {
            return;
        }
        undyingReady.remove(player.getUniqueId());
        event.setCancelled(true);
        player.setHealth(2.0);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 100, 1));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_SKELETON_AMBIENT, 1.2f, 0.6f);
        player.getWorld().spawnParticle(Particle.CRIT, player.getLocation().add(0, 1, 0), 30, 0.4, 0.6, 0.4, 0.1);
        player.sendMessage("§fYour bones refuse to fall apart!");
    }

    @EventHandler
    public void onSkullTossHit(ProjectileHitEvent event) {
        if (!event.getEntity().hasMetadata("skeleton_skull_toss")) {
            return;
        }
        Location loc = event.getHitBlock() != null ? event.getHitBlock().getLocation().add(0, 1, 0) : event.getEntity().getLocation();
        loc.getWorld().spawnParticle(Particle.CLOUD, loc, 20, 0.4, 0.3, 0.4, 0.02);
        loc.getWorld().playSound(loc, Sound.BLOCK_BONE_BLOCK_BREAK, 1.0f, 0.7f);
        double blindDuration = event.getEntity().hasMetadata("blind_duration") ? event.getEntity().getMetadata("blind_duration").get(0).asDouble() : 3.0;
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, 3.0, 2.0, 3.0)) {
            if (entity instanceof LivingEntity) {
                ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, (int) (blindDuration * 20), 0));
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        flatReductionActive.remove(id);
        flatReductionAmount.remove(id);
        brittleActive.remove(id);
        brittleMultiplier.remove(id);
        undyingReady.remove(id);
    }
}

// ==================== 20 NEW BONE GUARD ABILITIES ====================

class BoneSpearThrow extends Ability {
    private final ItemEditFull plugin;
    public BoneSpearThrow(ItemEditFull plugin) {
        super("bone_spear_throw", "Bone Spear Throw", "Hurls a sharpened bone spear that pierces through enemies in a line.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double range = getDoubleParam(plugin, item, "range", 10.0);
        double damage = getDoubleParam(plugin, item, "damage", 7.0);
        Location origin = player.getEyeLocation();
        Vector dir = origin.getDirection().normalize();
        EffectUtils.fanfare(origin, new EffectUtils.SoundLayer(Sound.ENTITY_SKELETON_SHOOT, 1.0f, 0.7f),
                new EffectUtils.SoundLayer(Sound.BLOCK_BONE_BLOCK_HIT, 0.8f, 0.6f));
        for (int i = 1; i <= (int) range; i++) {
            Location point = origin.clone().add(dir.clone().multiply(i));
            point.getWorld().spawnParticle(Particle.CRIT, point, 2, 0.1, 0.1, 0.1, 0);
            point.getWorld().spawnParticle(Particle.DUST, point, 1, 0.05, 0.05, 0.05, 0,
                    new Particle.DustOptions(Color.fromRGB(225, 222, 205), 1.0f));
            for (Entity entity : point.getWorld().getNearbyEntities(point, 1.0, 1.0, 1.0)) {
                if (entity instanceof LivingEntity && !entity.equals(player)) {
                    ((LivingEntity) entity).damage(damage, player);
                }
            }
        }
        return true;
    }
}

class MarrowDrain extends Ability {
    private final ItemEditFull plugin;
    public MarrowDrain(ItemEditFull plugin) {
        super("marrow_drain", "Marrow Drain", "Drains marrow from a struck enemy, weakening their strength and boosting yours.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 6.0);
        Entity target = player.getTargetEntity(5);
        if (!(target instanceof LivingEntity)) {
            player.sendMessage("§cNo target in range.");
            return false;
        }
        LivingEntity living = (LivingEntity) target;
        living.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, (int) (duration * 20), 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, (int) (duration * 20), 0));
        Location drainLoc = living.getLocation().add(0, 1, 0);
        EffectUtils.fanfare(drainLoc, new EffectUtils.SoundLayer(Sound.BLOCK_BONE_BLOCK_BREAK, 1.0f, 0.9f));
        EffectUtils.burst(drainLoc,
                new EffectUtils.Layer(Particle.CRIT, 10, 0.2, 0.3, 0.2, 0),
                new EffectUtils.Layer(Particle.SOUL, 10, 0.2, 0.3, 0.2, 0.02));
        EffectUtils.spiral(plugin, living, living.getLocation(), 0.6, 8, Math.PI / 4, 0.15, 1L,
                new EffectUtils.Layer(Particle.SOUL, 1, 0, 0, 0, 0));
        return true;
    }
}

class RattlingCharge extends Ability {
    private final ItemEditFull plugin;
    public RattlingCharge(ItemEditFull plugin) {
        super("rattling_charge", "Rattling Charge", "Charge forward with clattering bones, damaging anything you collide with.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double range = getDoubleParam(plugin, item, "range", 6.0);
        double damage = getDoubleParam(plugin, item, "damage", 5.0);
        Location start = player.getLocation();
        Vector dir = VectorUtils.safeNormalize(start.getDirection().setY(0));
        if (dir.lengthSquared() < 0.01) {
            dir = new Vector(0, 0, 1);
        }
        final Vector chargeDir = dir;
        EffectUtils.fanfare(start, new EffectUtils.SoundLayer(Sound.ENTITY_SKELETON_STEP, 1.5f, 0.7f),
                new EffectUtils.SoundLayer(Sound.BLOCK_BONE_BLOCK_STEP, 1.2f, 0.8f));
        for (int i = 1; i <= (int) range; i++) {
            Location step = start.clone().add(chargeDir.clone().multiply(i)).add(0, 1, 0);
            step.getWorld().spawnParticle(Particle.CRIT, step, 4, 0.2, 0.2, 0.2, 0);
            step.getWorld().spawnParticle(Particle.CLOUD, step, 3, 0.3, 0.15, 0.3, 0.01);
            for (Entity entity : step.getWorld().getNearbyEntities(step, 1.2, 1.2, 1.2)) {
                if (entity instanceof LivingEntity && !entity.equals(player)) {
                    LivingEntity living = (LivingEntity) entity;
                    living.damage(damage, player);
                    living.setVelocity(chargeDir.clone().multiply(1.2).setY(0.25));
                }
            }
        }
        player.setVelocity(chargeDir.clone().multiply(1.1).setY(Math.max(player.getVelocity().getY(), 0.1)));
        return true;
    }
}

class CalcifiedArmor extends Ability {
    private final ItemEditFull plugin;
    public CalcifiedArmor(ItemEditFull plugin) {
        super("calcified_armor", "Calcified Armor", "Hardens your bones into armor plating, reducing damage taken.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 10.0);
        double reduction = getDoubleParam(plugin, item, "reduction", 2.0);
        SkeletonExpansionAbilities.activateFlatReduction(player.getUniqueId(), System.currentTimeMillis() + (long) (duration * 1000), reduction);
        Location armorLoc = player.getLocation().add(0, 1, 0);
        EffectUtils.fanfare(armorLoc, new EffectUtils.SoundLayer(Sound.BLOCK_BONE_BLOCK_PLACE, 1.0f, 0.6f));
        EffectUtils.burst(armorLoc,
                new EffectUtils.Layer(Particle.CLOUD, 15, 0.4, 0.6, 0.4, 0),
                new EffectUtils.Layer(Particle.DUST, 16, 0.4, 0.6, 0.4, 0,
                        new Particle.DustOptions(Color.fromRGB(210, 208, 195), 1.4f)));
        EffectUtils.ring(player.getLocation().add(0, 0.1, 0), 1.0, 10, new EffectUtils.Layer(Particle.CRIT, 1, 0, 0.1, 0, 0));
        player.sendMessage("§fYour bones calcify into plating!");
        return true;
    }
}

class ArrowRain extends Ability {
    private final ItemEditFull plugin;
    public ArrowRain(ItemEditFull plugin) {
        super("arrow_rain", "Arrow Rain", "Fires a volley of arrows arcing down onto a target area.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double radius = getDoubleParam(plugin, item, "radius", 4.0);
        int arrows = getIntParam(plugin, item, "arrows", 8);
        double damageEach = getDoubleParam(plugin, item, "damage_each", 2.0);
        Block targetBlock = player.getTargetBlockExact(30);
        Location center = targetBlock != null ? targetBlock.getLocation().add(0.5, 1.0, 0.5) : player.getLocation();
        EffectUtils.fanfare(center, new EffectUtils.SoundLayer(Sound.ENTITY_ARROW_SHOOT, 1.0f, 0.8f));
        EffectUtils.ring(center, radius, 10, new EffectUtils.Layer(Particle.DUST, 1, 0, 0.05, 0, 0,
                new Particle.DustOptions(Color.fromRGB(220, 218, 200), 1.0f)));
        for (int i = 0; i < arrows; i++) {
            double angle = Math.random() * 2 * Math.PI;
            double r = Math.random() * radius;
            Location pLoc = center.clone().add(Math.cos(angle) * r, 0, Math.sin(angle) * r);
            pLoc.getWorld().spawnParticle(Particle.CRIT, pLoc.clone().add(0, 4, 0), 1, 0, 0, 0, 0);
            pLoc.getWorld().spawnParticle(Particle.CRIT, pLoc, 3, 0.2, 0.1, 0.2, 0.02);
            for (Entity entity : pLoc.getWorld().getNearbyEntities(pLoc, 1.0, 1.5, 1.0)) {
                if (entity instanceof LivingEntity && !entity.equals(player)) {
                    ((LivingEntity) entity).damage(damageEach, player);
                }
            }
        }
        return true;
    }
}

class PiercingShot extends Ability {
    private final ItemEditFull plugin;
    public PiercingShot(ItemEditFull plugin) {
        super("piercing_shot", "Piercing Shot", "A single precise shot that ignores a portion of the target's armor.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double damage = getDoubleParam(plugin, item, "damage", 8.0);
        double armorPierce = getDoubleParam(plugin, item, "armor_pierce", 0.4);
        Entity target = player.getTargetEntity(20);
        if (!(target instanceof LivingEntity)) {
            player.sendMessage("§cNo target in sight.");
            return false;
        }
        LivingEntity living = (LivingEntity) target;
        double healthBefore = living.getHealth();
        living.damage(damage * (1 - armorPierce), player);
        double trueDamage = damage * armorPierce;
        double healthAfter = Math.max(0.0, living.getHealth() - trueDamage);
        living.setHealth(healthAfter);
        Location hitLoc = living.getLocation().add(0, 1, 0);
        EffectUtils.fanfare(hitLoc, new EffectUtils.SoundLayer(Sound.ENTITY_ARROW_HIT, 1.0f, 1.0f),
                new EffectUtils.SoundLayer(Sound.BLOCK_BONE_BLOCK_HIT, 1.0f, 1.2f));
        EffectUtils.burst(hitLoc,
                new EffectUtils.Layer(Particle.CRIT, 15, 0.2, 0.3, 0.2, 0.1),
                new EffectUtils.Layer(Particle.DUST, 10, 0.2, 0.3, 0.2, 0,
                        new Particle.DustOptions(Color.fromRGB(230, 226, 210), 1.2f)));
        return true;
    }
}

class BoneWall extends Ability {
    private final ItemEditFull plugin;
    public BoneWall(ItemEditFull plugin) {
        super("bone_wall", "Bone Wall", "Raises a temporary wall of bone that blocks movement and projectiles.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 8.0);
        int width = getIntParam(plugin, item, "width", 3);
        Vector dir = VectorUtils.safeNormalize(player.getLocation().getDirection().setY(0));
        if (dir.lengthSquared() < 0.01) {
            dir = new Vector(0, 0, 1);
        }
        Vector right = new Vector(-dir.getZ(), 0, dir.getX());
        Location base = player.getLocation().add(dir.clone().multiply(2));
        List<Block> wallBlocks = new ArrayList<>();
        int half = width / 2;
        for (int i = -half; i <= half; i++) {
            for (int y = 0; y <= 1; y++) {
                Block block = base.clone().add(right.clone().multiply(i)).add(0, y, 0).getBlock();
                if (block.getType() == Material.AIR) {
                    block.setType(Material.BONE_BLOCK);
                    wallBlocks.add(block);
                }
            }
        }
        EffectUtils.fanfare(base, new EffectUtils.SoundLayer(Sound.BLOCK_BONE_BLOCK_PLACE, 1.0f, 0.7f));
        for (Block b : wallBlocks) {
            EffectUtils.burst(b.getLocation().add(0.5, 0.5, 0.5),
                    new EffectUtils.Layer(Particle.CLOUD, 6, 0.2, 0.3, 0.2, 0),
                    new EffectUtils.Layer(Particle.CRIT, 4, 0.2, 0.3, 0.2, 0.02));
        }
        new CompatRunnable() {
            @Override
            public void run() {
                for (Block b : wallBlocks) {
                    if (b.getType() == Material.BONE_BLOCK) {
                        b.getWorld().spawnParticle(Particle.CLOUD, b.getLocation().add(0.5, 0.5, 0.5), 8, 0.2, 0.3, 0.2, 0);
                        b.setType(Material.AIR);
                    }
                }
            }
        }.runTaskLater(plugin, base, (long) (duration * 20));
        return true;
    }
}

class SkeletalSummon extends Ability {
    private final ItemEditFull plugin;
    public SkeletalSummon(ItemEditFull plugin) {
        super("skeletal_summon", "Skeletal Summon", "Summons a skeletal archer ally to fight at range beside you.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double lifetime = getDoubleParam(plugin, item, "lifetime", 25.0);
        Location loc = player.getLocation();
        Skeleton skeleton = (Skeleton) loc.getWorld().spawnEntity(loc, EntityType.SKELETON);
        skeleton.setCustomName("§f" + player.getName() + "'s Bone Archer");
        skeleton.setCustomNameVisible(true);
        skeleton.setMetadata("helper", new FixedMetadataValue(plugin, player.getUniqueId().toString()));
        EffectUtils.fanfare(loc, new EffectUtils.SoundLayer(Sound.ENTITY_SKELETON_AMBIENT, 1.0f, 0.9f));
        EffectUtils.burst(loc.add(0, 1, 0),
                new EffectUtils.Layer(Particle.CLOUD, 20, 0.4, 0.6, 0.4, 0),
                new EffectUtils.Layer(Particle.CRIT, 12, 0.4, 0.6, 0.4, 0.02));
        for (Entity entity : skeleton.getNearbyEntities(12, 6, 12)) {
            if (entity instanceof LivingEntity && !(entity instanceof Player) && !entity.hasMetadata("helper")) {
                skeleton.setTarget((LivingEntity) entity);
                break;
            }
        }
        new CompatRunnable() {
            @Override
            public void run() {
                if (skeleton.isValid()) {
                    EffectUtils.burst(skeleton.getLocation().add(0, 1, 0),
                            new EffectUtils.Layer(Particle.SMOKE_NORMAL, 10, 0.2, 0.3, 0.2, 0.01),
                            new EffectUtils.Layer(Particle.CRIT, 6, 0.2, 0.3, 0.2, 0.02));
                    skeleton.remove();
                }
            }
        }.runTaskLater(plugin, player, (long) (lifetime * 20));
        return true;
    }
}

class QuiverRefill extends Ability {
    private final ItemEditFull plugin;
    public QuiverRefill(ItemEditFull plugin) {
        super("quiver_refill", "Quiver Refill", "Instantly restocks your quiver with a handful of enchanted arrows.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        int arrows = getIntParam(plugin, item, "arrows", 8);
        Map<Integer, ItemStack> leftover = player.getInventory().addItem(new ItemStack(Material.ARROW, arrows));
        if (!leftover.isEmpty()) {
            for (ItemStack extra : leftover.values()) {
                player.getWorld().dropItem(player.getLocation(), extra);
            }
        }
        EffectUtils.fanfare(player.getLocation(), new EffectUtils.SoundLayer(Sound.ITEM_BUNDLE_INSERT, 1.0f, 1.2f));
        EffectUtils.burst(player.getLocation().add(0, 1, 0),
                new EffectUtils.Layer(Particle.CRIT, 8, 0.2, 0.2, 0.2, 0.02));
        player.sendMessage("§fYour quiver is restocked!");
        return true;
    }
}

class BrittleCurse extends Ability {
    private final ItemEditFull plugin;
    public BrittleCurse(ItemEditFull plugin) {
        super("brittle_curse", "Brittle Curse", "Curses a target's armor, making them take extra damage from all sources.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 6.0);
        double bonus = getDoubleParam(plugin, item, "damage_bonus", 0.2);
        Entity target = player.getTargetEntity(8);
        if (!(target instanceof Player)) {
            player.sendMessage("§cNo player target in sight.");
            return false;
        }
        Player targetPlayer = (Player) target;
        SkeletonExpansionAbilities.activateBrittle(targetPlayer.getUniqueId(), System.currentTimeMillis() + (long) (duration * 1000), 1.0 + bonus);
        Location curseLoc = targetPlayer.getLocation().add(0, 1, 0);
        EffectUtils.fanfare(curseLoc, new EffectUtils.SoundLayer(Sound.BLOCK_BONE_BLOCK_BREAK, 1.0f, 0.6f));
        EffectUtils.burst(curseLoc,
                new EffectUtils.Layer(Particle.CRIT, 10, 0.3, 0.3, 0.3, 0),
                new EffectUtils.Layer(Particle.DUST, 12, 0.3, 0.3, 0.3, 0,
                        new Particle.DustOptions(Color.fromRGB(200, 195, 180), 1.3f)));
        targetPlayer.sendMessage("§fA brittle curse cracks your defenses!");
        return true;
    }
}

class DeathRattle extends Ability {
    private final ItemEditFull plugin;
    public DeathRattle(ItemEditFull plugin) {
        super("death_rattle", "Death Rattle", "A bone-chilling scream that fears nearby enemies.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double radius = getDoubleParam(plugin, item, "radius", 5.0);
        double fearDuration = getDoubleParam(plugin, item, "fear_duration", 3.0);
        Location loc = player.getLocation();
        EffectUtils.fanfare(loc.clone().add(0, 1, 0), new EffectUtils.SoundLayer(Sound.ENTITY_SKELETON_HURT, 1.5f, 0.4f),
                new EffectUtils.SoundLayer(Sound.ENTITY_WITHER_SKELETON_AMBIENT, 0.8f, 0.5f));
        EffectUtils.ring(loc.clone().add(0, 0.2, 0), radius, 16,
                new EffectUtils.Layer(Particle.SOUL, 1, 0, 0.3, 0, 0));
        EffectUtils.burst(loc.clone().add(0, 1, 0), new EffectUtils.Layer(Particle.SMOKE_NORMAL, 12, 0.3, 0.3, 0.3, 0.02));
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
            if (entity instanceof LivingEntity && !entity.equals(player)) {
                LivingEntity living = (LivingEntity) entity;
                Vector away = VectorUtils.safeNormalize(living.getLocation().toVector().subtract(loc.toVector())).multiply(1.2).setY(0.2);
                living.setVelocity(away);
                living.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) (fearDuration * 20), 0));
            }
        }
        return true;
    }
}

class BoneShrapnel extends Ability {
    private final ItemEditFull plugin;
    public BoneShrapnel(ItemEditFull plugin) {
        super("bone_shrapnel", "Bone Shrapnel", "Explodes a cluster of bone shards outward, damaging all nearby enemies.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double radius = getDoubleParam(plugin, item, "radius", 4.0);
        double damage = getDoubleParam(plugin, item, "damage", 6.0);
        Location loc = player.getLocation();
        EffectUtils.fanfare(loc, new EffectUtils.SoundLayer(Sound.BLOCK_BONE_BLOCK_BREAK, 1.3f, 0.7f),
                new EffectUtils.SoundLayer(Sound.ENTITY_GENERIC_EXPLODE, 0.6f, 1.4f));
        Location center = loc.add(0, 1, 0);
        EffectUtils.burst(center,
                new EffectUtils.Layer(Particle.CRIT, 40, radius * 0.4, 0.5, radius * 0.4, 0.15),
                new EffectUtils.Layer(Particle.DUST, 24, radius * 0.4, 0.5, radius * 0.4, 0,
                        new Particle.DustOptions(Color.fromRGB(225, 222, 205), 1.3f)));
        EffectUtils.ring(center, radius, 14, new EffectUtils.Layer(Particle.CRIT, 1, 0, 0.2, 0, 0));
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
            if (entity instanceof LivingEntity && !entity.equals(player)) {
                ((LivingEntity) entity).damage(damage, player);
            }
        }
        return true;
    }
}

class LongbowSnipe extends Ability {
    private final ItemEditFull plugin;
    public LongbowSnipe(ItemEditFull plugin) {
        super("longbow_snipe", "Longbow Snipe", "A charged long-range shot dealing heavy damage to a distant target.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double range = getDoubleParam(plugin, item, "range", 25.0);
        double damage = getDoubleParam(plugin, item, "damage", 10.0);
        double chargeSeconds = getDoubleParam(plugin, item, "charge", 1.5);
        Entity target = player.getTargetEntity((int) range);
        if (!(target instanceof LivingEntity)) {
            player.sendMessage("§cNo target in range.");
            return false;
        }
        LivingEntity living = (LivingEntity) target;
        UUID targetId = living.getUniqueId();
        EffectUtils.fanfare(player.getLocation(), new EffectUtils.SoundLayer(Sound.ITEM_CROSSBOW_LOADING_START, 1.0f, 0.7f));
        EffectUtils.spiral(plugin, player, player.getEyeLocation(), 0.4, (int) (chargeSeconds * 20), Math.PI / 3, 0.01, 1L,
                new EffectUtils.Layer(Particle.CRIT, 1, 0, 0, 0, 0));
        new CompatRunnable() {
            @Override
            public void run() {
                if (!living.isValid() || living.isDead() || !player.isOnline()) {
                    return;
                }
                if (!targetId.equals(living.getUniqueId()) || living.getLocation().distanceSquared(player.getLocation()) > range * range * 1.5) {
                    return;
                }
                living.damage(damage, player);
                Location snipeLoc = living.getLocation().add(0, 1, 0);
                EffectUtils.fanfare(snipeLoc, new EffectUtils.SoundLayer(Sound.ITEM_CROSSBOW_SHOOT, 1.2f, 0.9f));
                EffectUtils.burst(snipeLoc,
                        new EffectUtils.Layer(Particle.CRIT, 20, 0.3, 0.4, 0.3, 0.1),
                        new EffectUtils.Layer(Particle.DUST, 14, 0.3, 0.4, 0.3, 0,
                                new Particle.DustOptions(Color.fromRGB(225, 222, 205), 1.2f)));
            }
        }.runTaskLater(plugin, player, (long) (chargeSeconds * 20));
        return true;
    }
}

class UndyingBones extends Ability {
    private final ItemEditFull plugin;
    public UndyingBones(ItemEditFull plugin) {
        super("undying_bones", "Undying Bones", "Braces your skeleton against death; the next fatal blow leaves you standing and empowered.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double window = getDoubleParam(plugin, item, "window", 30.0);
        SkeletonExpansionAbilities.armUndying(player.getUniqueId(), System.currentTimeMillis() + (long) (window * 1000));
        Location braceLoc = player.getLocation().add(0, 1, 0);
        EffectUtils.fanfare(braceLoc, new EffectUtils.SoundLayer(Sound.BLOCK_BONE_BLOCK_PLACE, 1.0f, 0.5f));
        EffectUtils.burst(braceLoc,
                new EffectUtils.Layer(Particle.CLOUD, 20, 0.3, 0.5, 0.3, 0),
                new EffectUtils.Layer(Particle.SOUL, 16, 0.3, 0.5, 0.3, 0.02));
        player.sendMessage("§fYour bones brace against death itself.");
        return true;
    }
}

class SkullToss extends Ability {
    private final ItemEditFull plugin;
    public SkullToss(ItemEditFull plugin) {
        super("skull_toss", "Skull Toss", "Throws a cracked skull that explodes into a cloud of bone dust on impact.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double blindDuration = getDoubleParam(plugin, item, "blind_duration", 3.0);
        Snowball skull = player.launchProjectile(Snowball.class);
        skull.setMetadata("skeleton_skull_toss", new FixedMetadataValue(plugin, true));
        skull.setMetadata("blind_duration", new FixedMetadataValue(plugin, blindDuration));
        EffectUtils.fanfare(player.getEyeLocation(), new EffectUtils.SoundLayer(Sound.ENTITY_SKELETON_SHOOT, 1.0f, 0.6f));
        EffectUtils.burst(player.getEyeLocation(), new EffectUtils.Layer(Particle.CRIT, 6, 0.1, 0.1, 0.1, 0.02));
        return true;
    }
}

class ArrowStorm extends Ability {
    private final ItemEditFull plugin;
    public ArrowStorm(ItemEditFull plugin) {
        super("arrow_storm", "Arrow Storm", "Fires a rapid burst of arrows in a spread in front of you.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        int arrows = getIntParam(plugin, item, "arrows", 5);
        double damageEach = getDoubleParam(plugin, item, "damage_each", 3.0);
        Location origin = player.getEyeLocation();
        Vector facing = origin.getDirection().normalize();
        EffectUtils.fanfare(origin, new EffectUtils.SoundLayer(Sound.ENTITY_SKELETON_SHOOT, 1.2f, 1.0f),
                new EffectUtils.SoundLayer(Sound.ENTITY_ARROW_SHOOT, 0.8f, 1.3f));
        for (int i = 0; i < arrows; i++) {
            double spreadAngle = Math.toRadians((Math.random() - 0.5) * 30.0);
            Vector spread = facing.clone();
            spread = new Vector(
                    spread.getX() * Math.cos(spreadAngle) - spread.getZ() * Math.sin(spreadAngle),
                    spread.getY(),
                    spread.getX() * Math.sin(spreadAngle) + spread.getZ() * Math.cos(spreadAngle)
            );
            for (int d = 1; d <= 8; d++) {
                Location point = origin.clone().add(spread.clone().multiply(d));
                point.getWorld().spawnParticle(Particle.CRIT, point, 1, 0, 0, 0, 0);
                point.getWorld().spawnParticle(Particle.CLOUD, point, 1, 0.02, 0.02, 0.02, 0);
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

class BoneDustCloud extends Ability {
    private final ItemEditFull plugin;
    public BoneDustCloud(ItemEditFull plugin) {
        super("bone_dust_cloud", "Bone Dust Cloud", "Releases a cloud of choking bone dust that blinds nearby enemies.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double radius = getDoubleParam(plugin, item, "radius", 4.0);
        double blindDuration = getDoubleParam(plugin, item, "blind_duration", 4.0);
        Location loc = player.getLocation();
        EffectUtils.fanfare(loc, new EffectUtils.SoundLayer(Sound.BLOCK_BONE_BLOCK_BREAK, 1.0f, 1.2f));
        EffectUtils.spiral(plugin, player, loc.clone().add(0, 0.2, 0), radius * 0.6, 16, Math.PI / 3, 0.08, 1L,
                new EffectUtils.Layer(Particle.CLOUD, 2, 0.1, 0.1, 0.1, 0.01));
        EffectUtils.burst(loc.clone().add(0, 1, 0), new EffectUtils.Layer(Particle.CLOUD, 20, radius * 0.4, 0.5, radius * 0.4, 0.02));
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
            if (entity instanceof LivingEntity && !entity.equals(player)) {
                ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, (int) (blindDuration * 20), 0));
            }
        }
        return true;
    }
}

class RicochetShot extends Ability {
    private final ItemEditFull plugin;
    public RicochetShot(ItemEditFull plugin) {
        super("ricochet_shot", "Ricochet Shot", "Fires an arrow that bounces to a second nearby target on impact.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double damage = getDoubleParam(plugin, item, "damage", 5.0);
        double bounceRange = getDoubleParam(plugin, item, "bounce_range", 6.0);
        Entity primary = player.getTargetEntity(20);
        if (!(primary instanceof LivingEntity)) {
            player.sendMessage("§cNo target in sight.");
            return false;
        }
        LivingEntity firstTarget = (LivingEntity) primary;
        firstTarget.damage(damage, player);
        Location firstLoc = firstTarget.getLocation().add(0, 1, 0);
        EffectUtils.fanfare(firstLoc, new EffectUtils.SoundLayer(Sound.ENTITY_ARROW_HIT, 1.0f, 1.0f));
        EffectUtils.burst(firstLoc, new EffectUtils.Layer(Particle.CRIT, 8, 0.2, 0.3, 0.2, 0.05));
        for (Entity entity : firstTarget.getNearbyEntities(bounceRange, bounceRange, bounceRange)) {
            if (entity instanceof LivingEntity && !entity.equals(player) && !entity.equals(firstTarget)) {
                LivingEntity second = (LivingEntity) entity;
                second.damage(damage, player);
                Location secondLoc = second.getLocation().add(0, 1, 0);
                EffectUtils.fanfare(secondLoc, new EffectUtils.SoundLayer(Sound.ENTITY_ARROW_HIT, 0.8f, 1.3f));
                EffectUtils.burst(secondLoc,
                        new EffectUtils.Layer(Particle.CRIT, 10, 0.2, 0.3, 0.2, 0.05),
                        new EffectUtils.Layer(Particle.CLOUD, 6, 0.2, 0.3, 0.2, 0.01));
                break;
            }
        }
        return true;
    }
}

class PhalanxStance extends Ability {
    private final ItemEditFull plugin;
    public PhalanxStance(ItemEditFull plugin) {
        super("phalanx_stance", "Phalanx Stance", "Plant your feet, gaining knockback resistance and damage reduction while stationary.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 8.0);
        double reduction = getDoubleParam(plugin, item, "reduction", 2.0);
        Location startLoc = player.getLocation();
        double originalKnockback = 0.0;
        org.bukkit.attribute.AttributeInstance kbAttr = player.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE);
        if (kbAttr != null) {
            originalKnockback = kbAttr.getBaseValue();
            kbAttr.setBaseValue(1.0);
        }
        SkeletonExpansionAbilities.activateFlatReduction(player.getUniqueId(), System.currentTimeMillis() + (long) (duration * 1000), reduction);
        EffectUtils.fanfare(player.getLocation(), new EffectUtils.SoundLayer(Sound.BLOCK_BONE_BLOCK_PLACE, 1.0f, 0.8f));
        EffectUtils.ring(player.getLocation().add(0, 0.1, 0), 0.9, 8, new EffectUtils.Layer(Particle.DUST, 1, 0, 0.2, 0, 0,
                new Particle.DustOptions(Color.fromRGB(225, 222, 205), 1.1f)));
        double finalOriginalKnockback = originalKnockback;
        new CompatRunnable() {
            int ticksElapsed = 0;
            @Override
            public void run() {
                if (!player.isOnline() || ticksElapsed >= (duration * 20)) {
                    org.bukkit.attribute.AttributeInstance kb = player.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE);
                    if (kb != null) {
                        kb.setBaseValue(finalOriginalKnockback);
                    }
                    cancel();
                    return;
                }
                if (player.getLocation().distanceSquared(startLoc) > 0.25) {
                    org.bukkit.attribute.AttributeInstance kb = player.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE);
                    if (kb != null) {
                        kb.setBaseValue(finalOriginalKnockback);
                    }
                    player.sendMessage("§7Your stance breaks as you move.");
                    cancel();
                    return;
                }
                ticksElapsed += 10;
            }
        }.runTaskTimer(plugin, player, 10L, 10L);
        return true;
    }
}

class LastStandVolley extends Ability {
    private final ItemEditFull plugin;
    public LastStandVolley(ItemEditFull plugin) {
        super("last_stand_volley", "Last Stand Volley", "Fires every remaining arrow at once in a devastating spread when below half health.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double thresholdPercent = getDoubleParam(plugin, item, "threshold_hp", 0.5);
        int arrows = getIntParam(plugin, item, "arrows", 10);
        double damageEach = getDoubleParam(plugin, item, "damage_each", 3.0);
        double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        if (player.getHealth() > maxHealth * thresholdPercent) {
            player.sendMessage("§cYou must be below " + (int) (thresholdPercent * 100) + "% health to make your last stand.");
            return false;
        }
        Location loc = player.getLocation();
        EffectUtils.fanfare(loc, new EffectUtils.SoundLayer(Sound.ENTITY_SKELETON_SHOOT, 1.5f, 1.2f),
                new EffectUtils.SoundLayer(Sound.BLOCK_BONE_BLOCK_BREAK, 1.0f, 0.6f));
        EffectUtils.burst(loc.clone().add(0, 1, 0), new EffectUtils.Layer(Particle.DUST, 20, 0.3, 0.4, 0.3, 0,
                new Particle.DustOptions(Color.fromRGB(230, 226, 210), 1.4f)));
        for (int i = 0; i < arrows; i++) {
            double angle = i * 2 * Math.PI / arrows;
            Vector dir = new Vector(Math.cos(angle), 0.1, Math.sin(angle));
            for (int d = 1; d <= 8; d++) {
                Location point = loc.clone().add(0, 1, 0).add(dir.clone().multiply(d));
                point.getWorld().spawnParticle(Particle.CRIT, point, 1, 0, 0, 0, 0);
                point.getWorld().spawnParticle(Particle.CLOUD, point, 1, 0.02, 0.02, 0.02, 0);
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
