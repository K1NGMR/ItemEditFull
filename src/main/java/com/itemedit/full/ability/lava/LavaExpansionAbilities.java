package com.itemedit.full.ability.lava;

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
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
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
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Molten School expansion: 20 additional fire/obsidian-themed abilities layered
 * on top of the original 6 lava abilities (LavaSpit, LavaWalker, HeatWave, LavaPour,
 * LavaAbsorption, FireAura).
 */
public class LavaExpansionAbilities implements Listener {

    // ---- Obsidian Carapace: flat percentage damage reduction while active ----
    private static final Map<UUID, Long> carapaceActive = new ConcurrentHashMap<>();
    private static final Map<UUID, Double> carapaceReduction = new ConcurrentHashMap<>();

    // ---- Heat Mirage: chance for melee attackers against this player to miss ----
    private static final Map<UUID, Long> mirageActive = new ConcurrentHashMap<>();
    private static final Map<UUID, Double> mirageMissChance = new ConcurrentHashMap<>();

    // ---- Molten Core: next melee hit lands with bonus fire damage ----
    private static final Map<UUID, Long> moltenCoreCharge = new ConcurrentHashMap<>();
    private static final Map<UUID, Double> moltenCoreBonusDamage = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> moltenCoreFireTicks = new ConcurrentHashMap<>();

    // ---- Searing Brand: next N hits by the caster against a marked target deal bonus fire damage ----
    private static final Map<UUID, BrandData> searingBrand = new ConcurrentHashMap<>();

    private static ItemEditFull pluginInstance;

    public static void register(ItemEditFull plugin) {
        pluginInstance = plugin;

        plugin.getAbilityManager().registerAbility(new MagmaArmor(plugin));
        plugin.getAbilityManager().registerAbility(new ObsidianCarapace(plugin));
        plugin.getAbilityManager().registerAbility(new MoltenCore(plugin));
        plugin.getAbilityManager().registerAbility(new CinderTrail(plugin));
        plugin.getAbilityManager().registerAbility(new LavaGeyser(plugin));
        plugin.getAbilityManager().registerAbility(new MoltenBulwark(plugin));
        plugin.getAbilityManager().registerAbility(new SearingGrip(plugin));
        plugin.getAbilityManager().registerAbility(new PyroclasticBurst(plugin));
        plugin.getAbilityManager().registerAbility(new EmberWings(plugin));
        plugin.getAbilityManager().registerAbility(new VolcanicStomp(plugin));
        plugin.getAbilityManager().registerAbility(new AshenFootsteps(plugin));
        plugin.getAbilityManager().registerAbility(new ThermalVent(plugin));
        plugin.getAbilityManager().registerAbility(new MagmaTendrils(plugin));
        plugin.getAbilityManager().registerAbility(new InfernoDash(plugin));
        plugin.getAbilityManager().registerAbility(new MoltenRain(plugin));
        plugin.getAbilityManager().registerAbility(new HeatMirage(plugin));
        plugin.getAbilityManager().registerAbility(new CoreMeltdown(plugin));
        plugin.getAbilityManager().registerAbility(new MagmaGolem(plugin));
        plugin.getAbilityManager().registerAbility(new SearingBrand(plugin));
        plugin.getAbilityManager().registerAbility(new EruptionLeap(plugin));

        plugin.getServer().getPluginManager().registerEvents(new LavaExpansionAbilities(), plugin);
    }

    static void activateCarapace(UUID uuid, long expireAt, double reduction) {
        carapaceActive.put(uuid, expireAt);
        carapaceReduction.put(uuid, reduction);
    }

    static void activateMirage(UUID uuid, long expireAt, double missChance) {
        mirageActive.put(uuid, expireAt);
        mirageMissChance.put(uuid, missChance);
    }

    static void chargeMoltenCore(UUID uuid, long expireAt, double bonusDamage, int fireTicks) {
        moltenCoreCharge.put(uuid, expireAt);
        moltenCoreBonusDamage.put(uuid, bonusDamage);
        moltenCoreFireTicks.put(uuid, fireTicks);
    }

    static void brandTarget(UUID casterUuid, UUID targetUuid, int hits, double bonusDamage, long expireAt) {
        searingBrand.put(casterUuid, new BrandData(targetUuid, hits, bonusDamage, expireAt));
    }

    private static final class BrandData {
        final UUID targetUuid;
        int hitsRemaining;
        final double bonusDamage;
        final long expireAt;

        BrandData(UUID targetUuid, int hitsRemaining, double bonusDamage, long expireAt) {
            this.targetUuid = targetUuid;
            this.hitsRemaining = hitsRemaining;
            this.bonusDamage = bonusDamage;
            this.expireAt = expireAt;
        }
    }

    @EventHandler
    public void onCarapaceDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity();
        Long expire = carapaceActive.get(player.getUniqueId());
        if (expire == null) {
            return;
        }
        if (System.currentTimeMillis() >= expire) {
            carapaceActive.remove(player.getUniqueId());
            carapaceReduction.remove(player.getUniqueId());
            return;
        }
        double reduction = carapaceReduction.getOrDefault(player.getUniqueId(), 0.4);
        event.setDamage(event.getDamage() * (1.0 - reduction));
    }

    @EventHandler
    public void onMirageDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity();
        Long expire = mirageActive.get(player.getUniqueId());
        if (expire == null) {
            return;
        }
        if (System.currentTimeMillis() >= expire) {
            mirageActive.remove(player.getUniqueId());
            mirageMissChance.remove(player.getUniqueId());
            return;
        }
        if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
            return;
        }
        if (!(event.getDamager() instanceof LivingEntity)) {
            return;
        }
        double missChance = mirageMissChance.getOrDefault(player.getUniqueId(), 0.3);
        if (Math.random() < missChance) {
            event.setCancelled(true);
            player.getWorld().playSound(player.getLocation(), Sound.ITEM_FIRECHARGE_USE, 0.6f, 1.8f);
            player.getWorld().spawnParticle(Particle.FLAME, player.getLocation().add(0, 1, 0), 6, 0.3, 0.3, 0.3, 0.01);
        }
    }

    @EventHandler
    public void onMoltenCoreHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getDamager();
        Long expire = moltenCoreCharge.get(player.getUniqueId());
        if (expire == null) {
            return;
        }
        if (System.currentTimeMillis() >= expire) {
            moltenCoreCharge.remove(player.getUniqueId());
            moltenCoreBonusDamage.remove(player.getUniqueId());
            moltenCoreFireTicks.remove(player.getUniqueId());
            return;
        }
        if (!(event.getEntity() instanceof LivingEntity)) {
            return;
        }
        moltenCoreCharge.remove(player.getUniqueId());
        double bonus = moltenCoreBonusDamage.getOrDefault(player.getUniqueId(), 6.0);
        int fireTicks = moltenCoreFireTicks.getOrDefault(player.getUniqueId(), 100);
        moltenCoreBonusDamage.remove(player.getUniqueId());
        moltenCoreFireTicks.remove(player.getUniqueId());

        event.setDamage(event.getDamage() + bonus);
        LivingEntity target = (LivingEntity) event.getEntity();
        target.setFireTicks(fireTicks);
        target.getWorld().spawnParticle(Particle.LAVA, target.getLocation().add(0, 1, 0), 14, 0.3, 0.5, 0.3, 0);
        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_GENERIC_BURN, 1.0f, 1.0f);
    }

    @EventHandler
    public void onSearingBrandHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getDamager();
        BrandData data = searingBrand.get(player.getUniqueId());
        if (data == null) {
            return;
        }
        if (System.currentTimeMillis() >= data.expireAt) {
            searingBrand.remove(player.getUniqueId());
            return;
        }
        if (!event.getEntity().getUniqueId().equals(data.targetUuid)) {
            return;
        }
        event.setDamage(event.getDamage() + data.bonusDamage);
        if (event.getEntity() instanceof LivingEntity) {
            ((LivingEntity) event.getEntity()).setFireTicks(40);
        }
        event.getEntity().getWorld().spawnParticle(Particle.FLAME, event.getEntity().getLocation().add(0, 1, 0), 6, 0.2, 0.3, 0.2, 0.01);
        data.hitsRemaining--;
        if (data.hitsRemaining <= 0) {
            searingBrand.remove(player.getUniqueId());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        carapaceActive.remove(id);
        carapaceReduction.remove(id);
        mirageActive.remove(id);
        mirageMissChance.remove(id);
        moltenCoreCharge.remove(id);
        moltenCoreBonusDamage.remove(id);
        moltenCoreFireTicks.remove(id);
        searingBrand.remove(id);
    }
}

// ==================== 20 NEW MOLTEN SCHOOL ABILITIES ====================

class MagmaArmor extends Ability {
    private final ItemEditFull plugin;
    public MagmaArmor(ItemEditFull plugin) {
        super("magma_armor", "Magma Armor", "Coats you in cooling magma plating, granting resistance and immunity to fire.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 12.0);
        int amplifier = getIntParam(plugin, item, "resistance_amplifier", 1);
        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, (int) (duration * 20), amplifier));
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, (int) (duration * 20), 0));
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_LAVA_EXTINGUISH, 1.0f, 0.7f);
        player.getWorld().spawnParticle(Particle.LAVA, player.getLocation().add(0, 1, 0), 15, 0.4, 0.6, 0.4, 0);
        player.sendMessage("§6Magma Armor hardens around you!");
        return true;
    }
}

class ObsidianCarapace extends Ability {
    private final ItemEditFull plugin;
    public ObsidianCarapace(ItemEditFull plugin) {
        super("obsidian_carapace", "Obsidian Carapace", "Petrifies your skin into obsidian, cutting incoming damage but slowing you.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 8.0);
        double reduction = getDoubleParam(plugin, item, "dmg_reduction", 0.4);
        int slowAmplifier = getIntParam(plugin, item, "slow_amplifier", 1);
        LavaExpansionAbilities.activateCarapace(player.getUniqueId(), System.currentTimeMillis() + (long) (duration * 1000), reduction);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) (duration * 20), slowAmplifier));
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_STONE_PLACE, 1.0f, 0.6f);
        player.getWorld().spawnParticle(Particle.BLOCK_CRACK, player.getLocation().add(0, 1, 0), 25, 0.4, 0.8, 0.4, 0, Material.OBSIDIAN.createBlockData());
        player.sendMessage("§8Your skin hardens into obsidian!");
        return true;
    }
}

class MoltenCore extends Ability {
    private final ItemEditFull plugin;
    public MoltenCore(ItemEditFull plugin) {
        super("molten_core", "Molten Core", "Charges a molten core in your chest; your next melee hit erupts for bonus fire damage.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double bonusDamage = getDoubleParam(plugin, item, "bonus_damage", 6.0);
        double window = getDoubleParam(plugin, item, "window", 6.0);
        int fireTicks = getIntParam(plugin, item, "fire_ticks", 100);
        LavaExpansionAbilities.chargeMoltenCore(player.getUniqueId(), System.currentTimeMillis() + (long) (window * 1000), bonusDamage, fireTicks);
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_FIRECHARGE_USE, 1.0f, 0.6f);
        player.getWorld().spawnParticle(Particle.FLAME, player.getLocation().add(0, 1, 0), 20, 0.3, 0.5, 0.3, 0.02);
        player.sendMessage("§cYour core ignites - your next strike will erupt!");
        return true;
    }
}

class CinderTrail extends Ability {
    private final ItemEditFull plugin;
    public CinderTrail(ItemEditFull plugin) {
        super("cinder_trail", "Cinder Trail", "Leaves a line of burning cinders behind you while sprinting.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 6.0);
        double tickDamage = getDoubleParam(plugin, item, "tick_damage", 1.0);
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_FLINTANDSTEEL_USE, 1.0f, 1.2f);
        new CompatRunnable() {
            int ticksElapsed = 0;
            @Override
            public void run() {
                if (!player.isOnline() || ticksElapsed >= (duration * 20)) {
                    cancel();
                    return;
                }
                Location loc = player.getLocation();
                loc.getWorld().spawnParticle(Particle.FLAME, loc, 4, 0.2, 0.05, 0.2, 0.01);
                if (player.isSprinting()) {
                    for (Entity entity : loc.getWorld().getNearbyEntities(loc, 1.3, 1.0, 1.3)) {
                        if (entity instanceof LivingEntity && !entity.equals(player)) {
                            ((LivingEntity) entity).damage(tickDamage, player);
                            entity.setFireTicks(20);
                        }
                    }
                }
                ticksElapsed += 4;
            }
        }.runTaskTimer(plugin, player, 0L, 4L);
        return true;
    }
}

class LavaGeyser extends Ability {
    private final ItemEditFull plugin;
    public LavaGeyser(ItemEditFull plugin) {
        super("lava_geyser", "Lava Geyser", "Erupts a geyser beneath a target point, launching and burning nearby enemies.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double radius = getDoubleParam(plugin, item, "radius", 3.0);
        double damage = getDoubleParam(plugin, item, "damage", 5.0);
        double launch = getDoubleParam(plugin, item, "launch", 1.2);
        Block target = player.getTargetBlockExact(10);
        Location origin = target != null ? target.getLocation().add(0.5, 1.0, 0.5) : player.getLocation();
        origin.getWorld().playSound(origin, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.4f);
        origin.getWorld().spawnParticle(Particle.LAVA, origin, 30, radius * 0.4, 0.5, radius * 0.4, 0.1);
        origin.getWorld().spawnParticle(Particle.FLAME, origin, 40, radius * 0.4, 0.8, radius * 0.4, 0.05);
        for (Entity entity : origin.getWorld().getNearbyEntities(origin, radius, 3.0, radius)) {
            if (entity instanceof LivingEntity && !entity.equals(player)) {
                LivingEntity living = (LivingEntity) entity;
                living.damage(damage, player);
                living.setFireTicks(60);
                Vector v = living.getVelocity();
                living.setVelocity(new Vector(v.getX(), launch, v.getZ()));
            }
        }
        return true;
    }
}

class MoltenBulwark extends Ability {
    private final ItemEditFull plugin;
    public MoltenBulwark(ItemEditFull plugin) {
        super("molten_bulwark", "Molten Bulwark", "Raises a wall of hardened magma ahead of you that blocks incoming projectiles.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 10.0);
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
            for (int y = 0; y <= 2; y++) {
                Location loc = base.clone().add(right.clone().multiply(i)).add(0, y, 0);
                Block block = loc.getBlock();
                if (block.getType() == Material.AIR) {
                    block.setType(Material.MAGMA_BLOCK);
                    wallBlocks.add(block);
                    // Molten rock lurching up out of the ground into place, block by block.
                    EffectUtils.burst(loc.clone().add(0.5, 0.5, 0.5),
                            new EffectUtils.Layer(Particle.LAVA, 6, 0.25, 0.25, 0.25, 0.02),
                            new EffectUtils.Layer(Particle.SMOKE_NORMAL, 4, 0.2, 0.2, 0.2, 0.01));
                }
            }
        }
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_STONE_PLACE, 1.0f, 0.5f);

        new CompatRunnable() {
            @Override
            public void run() {
                for (Block b : wallBlocks) {
                    if (b.getType() == Material.MAGMA_BLOCK) {
                        b.setType(Material.AIR);
                        // Crumbles back into ash and smoke as the bulwark cools and collapses.
                        EffectUtils.burst(b.getLocation().add(0.5, 0.5, 0.5),
                                new EffectUtils.Layer(Particle.ASH, 8, 0.25, 0.25, 0.25, 0.02),
                                new EffectUtils.Layer(Particle.SMOKE_NORMAL, 6, 0.2, 0.2, 0.2, 0.01));
                    }
                }
                base.getWorld().playSound(base, Sound.BLOCK_LAVA_EXTINGUISH, 0.8f, 1.1f);
            }
        }.runTaskLater(plugin, base, (long) (duration * 20));
        return true;
    }
}

class SearingGrip extends Ability {
    private final ItemEditFull plugin;
    public SearingGrip(ItemEditFull plugin) {
        super("searing_grip", "Searing Grip", "Superheats a nearby enemy's armor, dealing damage over time.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double dotDamage = getDoubleParam(plugin, item, "dot_damage", 2.0);
        double dotDuration = getDoubleParam(plugin, item, "dot_duration", 5.0);
        Entity target = player.getTargetEntity(4);
        if (!(target instanceof LivingEntity)) {
            player.sendMessage("§cNo target in range to sear.");
            return false;
        }
        LivingEntity living = (LivingEntity) target;
        living.getWorld().playSound(living.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1.0f, 0.6f);
        living.getWorld().spawnParticle(Particle.LAVA, living.getLocation().add(0, 1, 0), 10, 0.3, 0.4, 0.3, 0);
        new CompatRunnable() {
            int ticksElapsed = 0;
            @Override
            public void run() {
                if (!living.isValid() || living.isDead() || ticksElapsed >= (dotDuration * 20)) {
                    cancel();
                    return;
                }
                living.damage(dotDamage, player);
                living.getWorld().spawnParticle(Particle.SMOKE_NORMAL, living.getLocation().add(0, 1, 0), 3, 0.2, 0.2, 0.2, 0.01);
                ticksElapsed += 20;
            }
        }.runTaskTimer(plugin, living, 20L, 20L);
        return true;
    }
}

class PyroclasticBurst extends Ability {
    private final ItemEditFull plugin;
    public PyroclasticBurst(ItemEditFull plugin) {
        super("pyroclastic_burst", "Pyroclastic Burst", "Releases a cone of ash and embers, blinding and burning enemies in front of you.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double radius = getDoubleParam(plugin, item, "radius", 5.0);
        double blindDuration = getDoubleParam(plugin, item, "blind_duration", 3.0);
        Location origin = player.getEyeLocation();
        Vector facing = origin.getDirection().normalize();
        player.getWorld().playSound(origin, Sound.ENTITY_BLAZE_SHOOT, 1.2f, 0.7f);
        player.getWorld().spawnParticle(Particle.ASH, origin.clone().add(facing.clone().multiply(2)), 40, 1.0, 0.6, 1.0, 0.05);
        for (Entity entity : origin.getWorld().getNearbyEntities(origin, radius, radius, radius)) {
            if (!(entity instanceof LivingEntity) || entity.equals(player)) {
                continue;
            }
            Vector toEntity = VectorUtils.safeNormalize(entity.getLocation().toVector().subtract(origin.toVector()));
            if (toEntity.lengthSquared() < 0.0001) {
                continue;
            }
            double angle = Math.toDegrees(Math.acos(Math.max(-1.0, Math.min(1.0, facing.dot(toEntity)))));
            if (angle <= 45.0) {
                LivingEntity living = (LivingEntity) entity;
                living.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, (int) (blindDuration * 20), 0));
                living.setFireTicks(60);
                living.getWorld().playSound(living.getLocation(), Sound.ENTITY_GENERIC_BURN, 0.8f, 1.2f);
            }
        }
        return true;
    }
}

class EmberWings extends Ability {
    private final ItemEditFull plugin;
    public EmberWings(ItemEditFull plugin) {
        super("ember_wings", "Ember Wings", "Grants a short burst of fire-fuelled flight, trailing embers behind you.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 5.0);
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_FIRECHARGE_USE, 1.0f, 1.4f);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, (int) (duration * 20) + 40, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, (int) (duration * 20), 8));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, (int) (duration * 20), 1));
        Vector v = player.getVelocity();
        player.setVelocity(new Vector(v.getX(), 0.9, v.getZ()));
        new CompatRunnable() {
            int ticksElapsed = 0;
            @Override
            public void run() {
                if (!player.isOnline() || ticksElapsed >= (duration * 20)) {
                    cancel();
                    return;
                }
                player.getWorld().spawnParticle(Particle.FLAME, player.getLocation(), 3, 0.2, 0.2, 0.2, 0.01);
                ticksElapsed += 2;
            }
        }.runTaskTimer(plugin, player, 0L, 2L);
        return true;
    }
}

class VolcanicStomp extends Ability {
    private final ItemEditFull plugin;
    public VolcanicStomp(ItemEditFull plugin) {
        super("volcanic_stomp", "Volcanic Stomp", "Cracks the ground open, spewing lava fissures toward nearby enemies.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double radius = getDoubleParam(plugin, item, "radius", 6.0);
        double damage = getDoubleParam(plugin, item, "damage", 7.0);
        Location loc = player.getLocation();
        loc.getWorld().playSound(loc, Sound.ENTITY_RAVAGER_STUNNED, 1.3f, 0.6f);
        for (int x = -(int) radius; x <= radius; x++) {
            for (int z = -(int) radius; z <= radius; z++) {
                if (x * x + z * z <= radius * radius) {
                    Location pLoc = loc.clone().add(x, 0, z);
                    pLoc.getWorld().spawnParticle(Particle.LAVA, pLoc, 1, 0, 0.1, 0, 0);
                    pLoc.getWorld().spawnParticle(Particle.SMOKE_NORMAL, pLoc, 1, 0, 0.2, 0, 0.01);
                }
            }
        }
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, radius, 2.0, radius)) {
            if (entity instanceof LivingEntity && !entity.equals(player)) {
                LivingEntity living = (LivingEntity) entity;
                living.damage(damage, player);
                living.setFireTicks(40);
            }
        }
        return true;
    }
}

class AshenFootsteps extends Ability {
    private final ItemEditFull plugin;
    public AshenFootsteps(ItemEditFull plugin) {
        super("ashen_footsteps", "Ashen Footsteps", "Turns the ground beneath you to ash, extinguishing your own fire and muffling your footsteps.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 10.0);
        player.setFireTicks(0);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_SAND_BREAK, 0.8f, 0.6f);
        new CompatRunnable() {
            int ticksElapsed = 0;
            @Override
            public void run() {
                if (!player.isOnline() || ticksElapsed >= (duration * 20)) {
                    cancel();
                    return;
                }
                if (player.getFireTicks() > 0) {
                    player.setFireTicks(0);
                }
                Location loc = player.getLocation();
                loc.getWorld().spawnParticle(Particle.ASH, loc, 2, 0.2, 0.02, 0.2, 0);
                ticksElapsed += 5;
            }
        }.runTaskTimer(plugin, player, 0L, 5L);
        return true;
    }
}

class ThermalVent extends Ability {
    private final ItemEditFull plugin;
    public ThermalVent(ItemEditFull plugin) {
        super("thermal_vent", "Thermal Vent", "Plants a vent that periodically releases scalding steam at anyone nearby.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 15.0);
        double tickDamage = getDoubleParam(plugin, item, "tick_damage", 2.0);
        double radius = getDoubleParam(plugin, item, "radius", 3.0);
        Location ventLoc = player.getLocation();
        ventLoc.getWorld().playSound(ventLoc, Sound.BLOCK_LAVA_AMBIENT, 1.0f, 0.8f);
        new CompatRunnable() {
            int ticksElapsed = 0;
            @Override
            public void run() {
                if (ticksElapsed >= (duration * 20)) {
                    cancel();
                    return;
                }
                ventLoc.getWorld().spawnParticle(Particle.CLOUD, ventLoc.clone().add(0, 0.2, 0), 8, radius * 0.3, 0.3, radius * 0.3, 0.02);
                if (ticksElapsed % 20 == 0) {
                    for (Entity entity : ventLoc.getWorld().getNearbyEntities(ventLoc, radius, 2.0, radius)) {
                        if (entity instanceof LivingEntity) {
                            ((LivingEntity) entity).damage(tickDamage, player);
                        }
                    }
                    ventLoc.getWorld().playSound(ventLoc, Sound.BLOCK_LAVA_POP, 1.0f, 1.2f);
                }
                ticksElapsed += 2;
            }
        }.runTaskTimer(plugin, ventLoc, 0L, 2L);
        return true;
    }
}

class MagmaTendrils extends Ability {
    private final ItemEditFull plugin;
    public MagmaTendrils(ItemEditFull plugin) {
        super("magma_tendrils", "Magma Tendrils", "Molten tendrils erupt from the ground to grab and root a nearby enemy.");
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
        living.getWorld().playSound(base, Sound.BLOCK_LAVA_POP, 1.2f, 0.7f);
        living.getWorld().spawnParticle(Particle.LAVA, base.clone().add(0.5, 0.5, 0.5), 15, 0.3, 0.3, 0.3, 0);

        List<Block> cage = new ArrayList<>();
        int[][] offsets = {{1, 0, 0}, {-1, 0, 0}, {0, 0, 1}, {0, 0, -1}};
        for (int[] off : offsets) {
            Block b = base.clone().add(off[0], 0, off[1]).getBlock();
            if (b.getType() == Material.AIR) {
                b.setType(Material.MAGMA_BLOCK);
                cage.add(b);
            }
        }
        Block below = base.clone().add(0, -1, 0).getBlock();
        Material originalBelow = below.getType();
        boolean convertedFloor = originalBelow != Material.MAGMA_BLOCK;
        if (convertedFloor) {
            below.setType(Material.MAGMA_BLOCK);
        }

        living.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) (rootDuration * 20) + 20, 6));

        new CompatRunnable() {
            @Override
            public void run() {
                for (Block b : cage) {
                    if (b.getType() == Material.MAGMA_BLOCK) {
                        b.setType(Material.AIR);
                    }
                }
                if (convertedFloor && below.getType() == Material.MAGMA_BLOCK) {
                    below.setType(originalBelow);
                }
            }
        }.runTaskLater(plugin, base, (long) (rootDuration * 20));
        return true;
    }
}

class InfernoDash extends Ability {
    private final ItemEditFull plugin;
    public InfernoDash(ItemEditFull plugin) {
        super("inferno_dash", "Inferno Dash", "Dash forward wreathed in flame, damaging and igniting everything in your path.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double range = getDoubleParam(plugin, item, "range", 6.0);
        double damage = getDoubleParam(plugin, item, "damage", 5.0);
        Location start = player.getLocation();
        Vector direction = VectorUtils.safeNormalize(start.getDirection().setY(0));
        if (direction.lengthSquared() < 0.01) {
            direction = new Vector(0, 0, 1);
        }
        final Vector dashDir = direction;
        player.getWorld().playSound(start, Sound.ITEM_FIRECHARGE_USE, 1.0f, 0.8f);

        Set<UUID> hit = new HashSet<>();
        int steps = (int) range;
        for (int i = 1; i <= steps; i++) {
            Location step = start.clone().add(dashDir.clone().multiply(i)).add(0, 1, 0);
            step.getWorld().spawnParticle(Particle.FLAME, step, 6, 0.3, 0.3, 0.3, 0.02);
            for (Entity entity : step.getWorld().getNearbyEntities(step, 1.2, 1.2, 1.2)) {
                if (entity instanceof LivingEntity && !entity.equals(player) && hit.add(entity.getUniqueId())) {
                    LivingEntity living = (LivingEntity) entity;
                    living.damage(damage, player);
                    living.setFireTicks(60);
                }
            }
        }
        Vector v = player.getVelocity();
        player.setVelocity(dashDir.clone().multiply(1.4).setY(Math.max(v.getY(), 0.15)));
        return true;
    }
}

class MoltenRain extends Ability {
    private final ItemEditFull plugin;
    public MoltenRain(ItemEditFull plugin) {
        super("molten_rain", "Molten Rain", "Calls a brief shower of molten droplets over an area, igniting the ground.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double radius = getDoubleParam(plugin, item, "radius", 5.0);
        double duration = getDoubleParam(plugin, item, "duration", 4.0);
        double tickDamage = getDoubleParam(plugin, item, "tick_damage", 1.5);
        Location center = player.getLocation();
        player.getWorld().playSound(center, Sound.ITEM_FIRECHARGE_USE, 1.0f, 0.5f);
        new CompatRunnable() {
            int ticksElapsed = 0;
            @Override
            public void run() {
                if (ticksElapsed >= (duration * 20) || !player.isOnline()) {
                    cancel();
                    return;
                }
                for (int i = 0; i < 12; i++) {
                    double angle = Math.random() * 2 * Math.PI;
                    double r = Math.random() * radius;
                    Location pLoc = center.clone().add(Math.cos(angle) * r, 2.0 + Math.random() * 2.0, Math.sin(angle) * r);
                    pLoc.getWorld().spawnParticle(Particle.LAVA, pLoc, 1, 0, 0, 0, 0);
                    pLoc.getWorld().spawnParticle(Particle.FLAME, pLoc, 1, 0, 0, 0, 0.01);
                }
                if (ticksElapsed % 20 == 0) {
                    for (Entity entity : center.getWorld().getNearbyEntities(center, radius, 2.0, radius)) {
                        if (entity instanceof LivingEntity && !entity.equals(player)) {
                            LivingEntity living = (LivingEntity) entity;
                            living.damage(tickDamage, player);
                            living.setFireTicks(40);
                        }
                    }
                }
                ticksElapsed += 2;
            }
        }.runTaskTimer(plugin, player, 0L, 2L);
        return true;
    }
}

class HeatMirage extends Ability {
    private final ItemEditFull plugin;
    public HeatMirage(ItemEditFull plugin) {
        super("heat_mirage", "Heat Mirage", "Superheated air bends light around you, giving melee attackers a chance to miss.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 8.0);
        double missChance = getDoubleParam(plugin, item, "miss_chance", 0.3);
        LavaExpansionAbilities.activateMirage(player.getUniqueId(), System.currentTimeMillis() + (long) (duration * 1000), missChance);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_FIRE_AMBIENT, 1.0f, 1.4f);
        player.getWorld().spawnParticle(Particle.FLAME, player.getLocation().add(0, 1, 0), 15, 0.4, 0.6, 0.4, 0.01);
        player.sendMessage("§6The air around you shimmers with heat!");
        return true;
    }
}

class CoreMeltdown extends Ability {
    private final ItemEditFull plugin;
    public CoreMeltdown(ItemEditFull plugin) {
        super("core_meltdown", "Core Meltdown", "After a brief charge, unleash a devastating eruption around you at the cost of self-damage.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double radius = getDoubleParam(plugin, item, "radius", 7.0);
        double damage = getDoubleParam(plugin, item, "damage", 12.0);
        double selfDamage = getDoubleParam(plugin, item, "self_damage", 3.0);
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_FIRECHARGE_USE, 1.0f, 0.3f);
        player.getWorld().spawnParticle(Particle.LAVA, player.getLocation().add(0, 1, 0), 10, 0.3, 0.5, 0.3, 0);
        new CompatRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    return;
                }
                Location loc = player.getLocation();
                loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 0.6f);
                loc.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, loc.clone().add(0, 0.5, 0), 3, 0.3, 0.3, 0.3, 0);
                loc.getWorld().spawnParticle(Particle.LAVA, loc, 60, radius * 0.4, 0.6, radius * 0.4, 0.15);
                for (Entity entity : loc.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
                    if (entity instanceof LivingEntity && !entity.equals(player)) {
                        LivingEntity living = (LivingEntity) entity;
                        living.damage(damage, player);
                        living.setFireTicks(100);
                    }
                }
                player.damage(selfDamage);
            }
        }.runTaskLater(plugin, player, 30L);
        return true;
    }
}

class MagmaGolem extends Ability {
    private final ItemEditFull plugin;
    public MagmaGolem(ItemEditFull plugin) {
        super("magma_golem", "Magma Golem", "Summons a temporary molten golem that punches and burns nearby enemies.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        int lifetimeSeconds = getIntParam(plugin, item, "lifetime", 20);
        double golemDamage = getDoubleParam(plugin, item, "golem_damage", 4.0);

        Location loc = player.getLocation();
        IronGolem golem;
        try {
            golem = loc.getWorld().spawn(loc, IronGolem.class);
        } catch (Exception e) {
            player.sendMessage("§cThe molten golem could not take shape here.");
            return false;
        }
        golem.setCustomName("§6" + player.getName() + "'s Magma Golem");
        golem.setCustomNameVisible(true);
        golem.setPlayerCreated(true);
        AttributeInstance atk = golem.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
        if (atk != null) {
            atk.setBaseValue(golemDamage);
        }
        golem.getWorld().playSound(loc, Sound.ENTITY_IRON_GOLEM_ATTACK, 1.2f, 0.6f);
        golem.getWorld().spawnParticle(Particle.LAVA, loc.clone().add(0, 1, 0), 25, 0.5, 0.8, 0.5, 0);

        new CompatRunnable() {
            int ticksElapsed = 0;
            final int lifetimeTicks = lifetimeSeconds * 20;
            @Override
            public void run() {
                if (!golem.isValid() || ticksElapsed >= lifetimeTicks) {
                    if (golem.isValid()) {
                        golem.getWorld().spawnParticle(Particle.LAVA, golem.getLocation().add(0, 1, 0), 20, 0.4, 0.6, 0.4, 0);
                        golem.getWorld().playSound(golem.getLocation(), Sound.BLOCK_LAVA_EXTINGUISH, 1.0f, 0.8f);
                        golem.remove();
                    }
                    cancel();
                    return;
                }
                if (ticksElapsed % 10 == 0) {
                    golem.getWorld().spawnParticle(Particle.FLAME, golem.getLocation().add(0, 1, 0), 4, 0.3, 0.5, 0.3, 0.01);
                    LivingEntity target = golem.getTarget();
                    if (target != null && target.isValid()) {
                        if (target.getLocation().distanceSquared(golem.getLocation()) <= 6.25) {
                            target.setFireTicks(40);
                        }
                    } else {
                        for (Entity nearby : golem.getNearbyEntities(10, 5, 10)) {
                            if (nearby instanceof Monster) {
                                golem.setTarget((LivingEntity) nearby);
                                break;
                            }
                        }
                    }
                }
                ticksElapsed += 5;
            }
        }.runTaskTimer(plugin, golem, 0L, 5L);
        return true;
    }
}

class SearingBrand extends Ability {
    private final ItemEditFull plugin;
    public SearingBrand(ItemEditFull plugin) {
        super("searing_brand", "Searing Brand", "Brands a target; your next several hits against them deal bonus fire damage.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double bonusDamage = getDoubleParam(plugin, item, "bonus_damage", 2.0);
        int hits = getIntParam(plugin, item, "hits", 3);
        double markDuration = getDoubleParam(plugin, item, "mark_duration", 10.0);
        Entity target = player.getTargetEntity(10);
        if (!(target instanceof LivingEntity)) {
            player.sendMessage("§cNo target in sight to brand.");
            return false;
        }
        LivingEntity living = (LivingEntity) target;
        LavaExpansionAbilities.brandTarget(player.getUniqueId(), living.getUniqueId(), hits, bonusDamage,
                System.currentTimeMillis() + (long) (markDuration * 1000));
        living.getWorld().playSound(living.getLocation(), Sound.ITEM_FIRECHARGE_USE, 1.0f, 1.4f);
        living.getWorld().spawnParticle(Particle.FLAME, living.getLocation().add(0, 1, 0), 12, 0.3, 0.5, 0.3, 0.01);
        String targetName = living instanceof Player ? ((Player) living).getName() : living.getName();
        player.sendMessage("§6You brand " + targetName + " with searing fire!");
        return true;
    }
}

class EruptionLeap extends Ability {
    private final ItemEditFull plugin;
    public EruptionLeap(ItemEditFull plugin) {
        super("eruption_leap", "Eruption Leap", "Leap skyward and slam down, cracking the earth and igniting the surrounding area.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double radius = getDoubleParam(plugin, item, "radius", 4.0);
        double damage = getDoubleParam(plugin, item, "damage", 6.0);
        Vector v = player.getVelocity();
        player.setVelocity(new Vector(v.getX(), 1.4, v.getZ()));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GHAST_SHOOT, 1.0f, 0.6f);

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
                if ((wasAirborne && player.isOnGround()) || ticksElapsed >= 60) {
                    cancel();
                    Location loc = player.getLocation();
                    loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.2f, 0.8f);
                    loc.getWorld().spawnParticle(Particle.LAVA, loc, 30, radius * 0.4, 0.3, radius * 0.4, 0.1);
                    loc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, loc, 2, 0.3, 0.1, 0.3, 0);
                    for (Entity entity : loc.getWorld().getNearbyEntities(loc, radius, 2.0, radius)) {
                        if (entity instanceof LivingEntity && !entity.equals(player)) {
                            LivingEntity living = (LivingEntity) entity;
                            living.damage(damage, player);
                            living.setFireTicks(60);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, player, 1L, 1L);
        return true;
    }
}
