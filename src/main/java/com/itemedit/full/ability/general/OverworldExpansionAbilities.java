package com.itemedit.full.ability.general;

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
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
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
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Wild Biomes expansion: 20 additional beast/flora-themed abilities layered
 * on top of the original 25 overworld abilities.
 */
public class OverworldExpansionAbilities implements Listener {

    // ---- Woolen Ward: absorbs exactly one hit ----
    private static final Map<UUID, Long> woolenWardActive = new ConcurrentHashMap<>();

    // ---- Rabbit's Hop / Cherry Bloom Veil: chance/window to dodge the next hit(s) ----
    private static final Map<UUID, Long> dodgeActive = new ConcurrentHashMap<>();
    private static final Map<UUID, Double> dodgeChance = new ConcurrentHashMap<>();

    // ---- Sunflower Turn: stacking outdoor speed ----
    private static final Map<UUID, Integer> sunflowerStacks = new ConcurrentHashMap<>();

    private static ItemEditFull pluginInstance;

    public static void register(ItemEditFull plugin) {
        pluginInstance = plugin;

        plugin.getAbilityManager().registerAbility(new FoxPounce(plugin));
        plugin.getAbilityManager().registerAbility(new PandaRoll(plugin));
        plugin.getAbilityManager().registerAbility(new LlamaSpitAbility(plugin));
        plugin.getAbilityManager().registerAbility(new WildGallop(plugin));
        plugin.getAbilityManager().registerAbility(new MangroveSnare(plugin));
        plugin.getAbilityManager().registerAbility(new AzaleaBloom(plugin));
        plugin.getAbilityManager().registerAbility(new WoolenWard(plugin));
        plugin.getAbilityManager().registerAbility(new BovineCure(plugin));
        plugin.getAbilityManager().registerAbility(new BoarCharge(plugin));
        plugin.getAbilityManager().registerAbility(new RabbitsHop(plugin));
        plugin.getAbilityManager().registerAbility(new FrogsTongue(plugin));
        plugin.getAbilityManager().registerAbility(new ParrotScreech(plugin));
        plugin.getAbilityManager().registerAbility(new AllaysSwiftness(plugin));
        plugin.getAbilityManager().registerAbility(new CherryBloomVeil(plugin));
        plugin.getAbilityManager().registerAbility(new ThicketAmbush(plugin));
        plugin.getAbilityManager().registerAbility(new GlowBerryLight(plugin));
        plugin.getAbilityManager().registerAbility(new HoneycombSeal(plugin));
        plugin.getAbilityManager().registerAbility(new MountainGoatCharge(plugin));
        plugin.getAbilityManager().registerAbility(new MyceliumSpread(plugin));
        plugin.getAbilityManager().registerAbility(new SunflowerTurn(plugin));

        plugin.getServer().getPluginManager().registerEvents(new OverworldExpansionAbilities(), plugin);
    }

    static void activateWoolenWard(UUID uuid, long expireAt) {
        woolenWardActive.put(uuid, expireAt);
    }

    static void activateDodge(UUID uuid, long expireAt, double chance) {
        dodgeActive.put(uuid, expireAt);
        dodgeChance.put(uuid, chance);
    }

    static int addSunflowerStack(UUID uuid, int max) {
        int current = sunflowerStacks.getOrDefault(uuid, 0);
        int next = Math.min(max, current + 1);
        sunflowerStacks.put(uuid, next);
        return next;
    }

    @EventHandler
    public void onWoolenWardDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity();
        Long expire = woolenWardActive.get(player.getUniqueId());
        if (expire == null) {
            return;
        }
        woolenWardActive.remove(player.getUniqueId());
        if (System.currentTimeMillis() >= expire) {
            return;
        }
        event.setCancelled(true);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_SHEEP_SHEAR, 1.0f, 1.0f);
        player.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, player.getLocation().add(0, 1, 0), 8, 0.2, 0.3, 0.2, 0);
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
        if (System.currentTimeMillis() >= expire) {
            dodgeActive.remove(player.getUniqueId());
            dodgeChance.remove(player.getUniqueId());
            return;
        }
        double chance = dodgeChance.getOrDefault(player.getUniqueId(), 0.5);
        if (Math.random() < chance) {
            event.setCancelled(true);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_RABBIT_JUMP, 1.0f, 1.4f);
        }
    }

    @EventHandler
    public void onLlamaSpitHit(ProjectileHitEvent event) {
        if (!event.getEntity().hasMetadata("llama_spit") || !(event.getHitEntity() instanceof LivingEntity)) {
            return;
        }
        LivingEntity target = (LivingEntity) event.getHitEntity();
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 80, 1));
        target.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 80, 0));
        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_LLAMA_SPIT, 1.0f, 1.0f);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        woolenWardActive.remove(id);
        dodgeActive.remove(id);
        dodgeChance.remove(id);
        sunflowerStacks.remove(id);
    }
}

// ==================== 20 NEW WILD BIOMES ABILITIES ====================

class FoxPounce extends Ability {
    private final ItemEditFull plugin;
    public FoxPounce(ItemEditFull plugin) {
        super("fox_pounce", "Fox Pounce", "A quick pounce that deals bonus damage to unaware targets.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double range = getDoubleParam(plugin, item, "range", 5.0);
        double damage = getDoubleParam(plugin, item, "damage", 5.0);
        double backstabBonus = getDoubleParam(plugin, item, "backstab_bonus", 3.0);
        Entity target = player.getTargetEntity((int) range);
        if (!(target instanceof LivingEntity)) {
            player.sendMessage("§cNo target in range.");
            return false;
        }
        LivingEntity living = (LivingEntity) target;
        Vector targetFacing = living.getLocation().getDirection();
        Vector towardPlayer = VectorUtils.safeNormalize(player.getLocation().toVector().subtract(living.getLocation().toVector()));
        boolean unaware = targetFacing.dot(towardPlayer) < -0.3;
        living.damage(damage + (unaware ? backstabBonus : 0), player);
        living.getWorld().playSound(living.getLocation(), Sound.ENTITY_FOX_AMBIENT, 1.0f, 1.3f);
        Vector pounce = player.getLocation().getDirection().normalize().multiply(0.8).setY(0.3);
        player.setVelocity(pounce);
        return true;
    }
}

class PandaRoll extends Ability {
    private final ItemEditFull plugin;
    public PandaRoll(ItemEditFull plugin) {
        super("panda_roll", "Panda Roll", "Roll forward, becoming briefly immune to knockback and dealing collision damage.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double range = getDoubleParam(plugin, item, "range", 5.0);
        double damage = getDoubleParam(plugin, item, "damage", 4.0);
        Location start = player.getLocation();
        Vector dir = VectorUtils.safeNormalize(start.getDirection().setY(0));
        if (dir.lengthSquared() < 0.01) {
            dir = new Vector(0, 0, 1);
        }
        player.getWorld().playSound(start, Sound.ENTITY_PANDA_AGGRESSIVE_AMBIENT, 1.0f, 1.2f);
        for (int i = 1; i <= (int) range; i++) {
            Location step = start.clone().add(dir.clone().multiply(i)).add(0, 0.5, 0);
            step.getWorld().spawnParticle(Particle.CLOUD, step, 3, 0.2, 0.1, 0.2, 0);
            for (Entity entity : step.getWorld().getNearbyEntities(step, 1.0, 1.0, 1.0)) {
                if (entity instanceof LivingEntity && !entity.equals(player)) {
                    ((LivingEntity) entity).damage(damage, player);
                }
            }
        }
        player.setVelocity(dir.clone().multiply(1.0));
        return true;
    }
}

class LlamaSpitAbility extends Ability {
    private final ItemEditFull plugin;
    public LlamaSpitAbility(ItemEditFull plugin) {
        super("llama_spit", "Llama Spit", "Spit a glob of caustic llama slobber that slows and nauseates.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        Snowball spit = player.launchProjectile(Snowball.class);
        spit.setMetadata("llama_spit", new FixedMetadataValue(plugin, true));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_LLAMA_ANGRY, 1.0f, 1.0f);
        return true;
    }
}

class WildGallop extends Ability {
    private final ItemEditFull plugin;
    public WildGallop(ItemEditFull plugin) {
        super("wild_gallop", "Wild Gallop", "Grants a burst of speed as if riding a galloping horse.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 6.0);
        int amplifier = getIntParam(plugin, item, "speed_amplifier", 1);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, (int) (duration * 20), amplifier));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_HORSE_GALLOP, 1.0f, 1.0f);
        return true;
    }
}

class MangroveSnare extends Ability {
    private final ItemEditFull plugin;
    public MangroveSnare(ItemEditFull plugin) {
        super("mangrove_snare", "Mangrove Snare", "Mangrove roots erupt to entangle nearby enemies.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double radius = getDoubleParam(plugin, item, "radius", 4.0);
        double rootDuration = getDoubleParam(plugin, item, "root_duration", 3.0);
        Location loc = player.getLocation();
        loc.getWorld().playSound(loc, Sound.BLOCK_ROOTS_BREAK, 1.0f, 0.8f);
        loc.getWorld().spawnParticle(Particle.COMPOSTER, loc, 20, radius * 0.3, 0.4, radius * 0.3, 0);
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, radius, 2.0, radius)) {
            if (entity instanceof LivingEntity && !entity.equals(player)) {
                ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) (rootDuration * 20), 5));
            }
        }
        return true;
    }
}

class AzaleaBloom extends Ability {
    private final ItemEditFull plugin;
    public AzaleaBloom(ItemEditFull plugin) {
        super("azalea_bloom", "Azalea Bloom", "A burst of azalea petals heals you and nearby allies.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double radius = getDoubleParam(plugin, item, "radius", 4.0);
        double heal = getDoubleParam(plugin, item, "heal", 3.0);
        Location loc = player.getLocation();
        loc.getWorld().playSound(loc, Sound.BLOCK_AZALEA_LEAVES_PLACE, 1.0f, 1.2f);
        loc.getWorld().spawnParticle(Particle.COMPOSTER, loc.add(0, 1, 0), 25, radius * 0.3, 0.4, radius * 0.3, 0);
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, radius, 2.0, radius)) {
            if (entity instanceof Player) {
                Player ally = (Player) entity;
                double maxHealth = ally.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                ally.setHealth(Math.min(maxHealth, ally.getHealth() + heal));
            }
        }
        return true;
    }
}

class WoolenWard extends Ability {
    private final ItemEditFull plugin;
    public WoolenWard(ItemEditFull plugin) {
        super("woolen_ward", "Woolen Ward", "Wraps you in thick wool, absorbing the next hit entirely.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 10.0);
        OverworldExpansionAbilities.activateWoolenWard(player.getUniqueId(), System.currentTimeMillis() + (long) (duration * 1000));
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_WOOL_PLACE, 1.0f, 0.9f);
        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation().add(0, 1, 0), 15, 0.3, 0.4, 0.3, 0);
        return true;
    }
}

class BovineCure extends Ability {
    private final ItemEditFull plugin;
    public BovineCure(ItemEditFull plugin) {
        super("bovine_cure", "Bovine Cure", "Drink deep of restorative milk essence, curing all negative potion effects.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        for (PotionEffect effect : new ArrayList<>(player.getActivePotionEffects())) {
            player.removePotionEffect(effect.getType());
        }
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_COW_AMBIENT, 1.0f, 1.0f);
        player.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, player.getLocation().add(0, 1.5, 0), 10, 0.2, 0.2, 0.2, 0);
        return true;
    }
}

class BoarCharge extends Ability {
    private final ItemEditFull plugin;
    public BoarCharge(ItemEditFull plugin) {
        super("boar_charge", "Boar Charge", "A headlong charge that knocks down anything in your path.");
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
        player.getWorld().playSound(start, Sound.ENTITY_PIG_AMBIENT, 1.3f, 0.6f);
        for (int i = 1; i <= (int) range; i++) {
            Location step = start.clone().add(chargeDir.clone().multiply(i)).add(0, 1, 0);
            for (Entity entity : step.getWorld().getNearbyEntities(step, 1.2, 1.2, 1.2)) {
                if (entity instanceof LivingEntity && !entity.equals(player)) {
                    LivingEntity living = (LivingEntity) entity;
                    living.damage(damage, player);
                    living.setVelocity(chargeDir.clone().multiply(1.2).setY(0.2));
                }
            }
        }
        player.setVelocity(chargeDir.clone().multiply(1.1).setY(Math.max(player.getVelocity().getY(), 0.1)));
        return true;
    }
}

class RabbitsHop extends Ability {
    private final ItemEditFull plugin;
    public RabbitsHop(ItemEditFull plugin) {
        super("rabbits_hop", "Rabbit's Hop", "A springy dodge hop that briefly grants guaranteed evasion.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 1.0);
        OverworldExpansionAbilities.activateDodge(player.getUniqueId(), System.currentTimeMillis() + (long) (duration * 1000), 1.0);
        Vector v = player.getVelocity();
        player.setVelocity(new Vector(v.getX(), 0.5, v.getZ()));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_RABBIT_JUMP, 1.0f, 1.2f);
        return true;
    }
}

class FrogsTongue extends Ability {
    private final ItemEditFull plugin;
    public FrogsTongue(ItemEditFull plugin) {
        super("frogs_tongue", "Frog's Tongue", "Lash out with a sticky tongue, pulling a distant enemy toward you.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double range = getDoubleParam(plugin, item, "range", 8.0);
        Entity target = player.getTargetEntity((int) range);
        if (!(target instanceof LivingEntity)) {
            player.sendMessage("§cNo target in range.");
            return false;
        }
        LivingEntity living = (LivingEntity) target;
        Vector pull = VectorUtils.safeNormalize(player.getLocation().toVector().subtract(living.getLocation().toVector())).multiply(1.3).setY(0.2);
        living.setVelocity(pull);
        living.getWorld().playSound(living.getLocation(), Sound.ENTITY_FROG_TONGUE, 1.0f, 1.0f);
        return true;
    }
}

class ParrotScreech extends Ability {
    private final ItemEditFull plugin;
    public ParrotScreech(ItemEditFull plugin) {
        super("parrot_screech", "Parrot Screech", "Mimics a warning screech that alerts and buffs nearby allies briefly.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double radius = getDoubleParam(plugin, item, "radius", 6.0);
        double duration = getDoubleParam(plugin, item, "duration", 4.0);
        Location loc = player.getLocation();
        loc.getWorld().playSound(loc, Sound.ENTITY_PARROT_IMITATE_WITCH, 1.0f, 1.3f);
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
            if (entity instanceof Player) {
                ((Player) entity).addPotionEffect(new PotionEffect(PotionEffectType.SPEED, (int) (duration * 20), 0));
                ((Player) entity).addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, (int) (duration * 20), 0));
            }
        }
        return true;
    }
}

class AllaysSwiftness extends Ability {
    private final ItemEditFull plugin;
    public AllaysSwiftness(ItemEditFull plugin) {
        super("allays_swiftness", "Allay's Swiftness", "Borrow an allay's darting speed for a short burst.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 4.0);
        int amplifier = getIntParam(plugin, item, "speed_amplifier", 2);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, (int) (duration * 20), amplifier));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ALLAY_AMBIENT_WITH_ITEM, 1.0f, 1.4f);
        player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation(), 10, 0.2, 0.2, 0.2, 0.02);
        return true;
    }
}

class CherryBloomVeil extends Ability {
    private final ItemEditFull plugin;
    public CherryBloomVeil(ItemEditFull plugin) {
        super("cherry_bloom_veil", "Cherry Bloom Veil", "Surrounds you in drifting cherry petals, obscuring enemy aim.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 6.0);
        double evasion = getDoubleParam(plugin, item, "evasion_bonus", 0.2);
        OverworldExpansionAbilities.activateDodge(player.getUniqueId(), System.currentTimeMillis() + (long) (duration * 1000), evasion);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_CHERRY_LEAVES_PLACE, 1.0f, 1.2f);
        player.getWorld().spawnParticle(Particle.COMPOSTER, player.getLocation().add(0, 1, 0), 20, 0.4, 0.6, 0.4, 0);
        return true;
    }
}

class ThicketAmbush extends Ability {
    private final ItemEditFull plugin;
    public ThicketAmbush(ItemEditFull plugin) {
        super("thicket_ambush", "Thicket Ambush", "Camouflage among foliage, becoming invisible while standing still in plants.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 8.0);
        Material below = player.getLocation().clone().subtract(0, 0.1, 0).getBlock().getType();
        Material feet = player.getLocation().getBlock().getType();
        boolean inFoliage = isFoliage(below) || isFoliage(feet);
        if (!inFoliage) {
            player.sendMessage("§cYou need to be standing in foliage to blend in.");
            return false;
        }
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, (int) (duration * 20), 0));
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_GRASS_STEP, 1.0f, 0.7f);
        return true;
    }
    private boolean isFoliage(Material m) {
        String name = m.name();
        return name.contains("LEAVES") || name.contains("GRASS") || name.contains("FERN") || name.contains("VINE") || name.contains("BUSH");
    }
}

class GlowBerryLight extends Ability {
    private final ItemEditFull plugin;
    public GlowBerryLight(ItemEditFull plugin) {
        super("glow_berry_light", "Glow Berry Light", "Scatters glow berries that illuminate the area and briefly blind enemies who look at them.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double radius = getDoubleParam(plugin, item, "radius", 5.0);
        double blindDuration = getDoubleParam(plugin, item, "blind_duration", 2.0);
        Location loc = player.getLocation();
        loc.getWorld().playSound(loc, Sound.BLOCK_CAVE_VINES_PICK_BERRIES, 1.0f, 1.3f);
        loc.getWorld().spawnParticle(Particle.END_ROD, loc.add(0, 1, 0), 30, radius * 0.3, 0.5, radius * 0.3, 0.05);
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
            if (entity instanceof LivingEntity && !entity.equals(player)) {
                ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, (int) (blindDuration * 20), 0));
            }
        }
        return true;
    }
}

class HoneycombSeal extends Ability {
    private final ItemEditFull plugin;
    public HoneycombSeal(ItemEditFull plugin) {
        super("honeycomb_seal", "Honeycomb Seal", "Coats you in honey, granting fire resistance and slow-fall for a short time.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 8.0);
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, (int) (duration * 20), 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, (int) (duration * 20), 0));
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEEHIVE_DRIP, 1.0f, 1.0f);
        return true;
    }
}

class MountainGoatCharge extends Ability {
    private final ItemEditFull plugin;
    public MountainGoatCharge(ItemEditFull plugin) {
        super("mountain_goat_charge", "Mountain Goat Charge", "A powerful ramming headbutt that launches the target away.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double range = getDoubleParam(plugin, item, "range", 5.0);
        double damage = getDoubleParam(plugin, item, "damage", 6.0);
        double knockback = getDoubleParam(plugin, item, "knockback", 1.8);
        Entity target = player.getTargetEntity((int) range);
        if (!(target instanceof LivingEntity)) {
            player.sendMessage("§cNo target in range.");
            return false;
        }
        LivingEntity living = (LivingEntity) target;
        living.damage(damage, player);
        Vector away = VectorUtils.safeNormalize(living.getLocation().toVector().subtract(player.getLocation().toVector())).multiply(knockback).setY(0.4);
        living.setVelocity(away);
        living.getWorld().playSound(living.getLocation(), Sound.ENTITY_GOAT_RAM_IMPACT, 1.2f, 0.9f);
        return true;
    }
}

class MyceliumSpread extends Ability {
    private final ItemEditFull plugin;
    public MyceliumSpread(ItemEditFull plugin) {
        super("mycelium_spread", "Mycelium Spread", "Spreads spores that slowly heal you while standing on natural ground.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 10.0);
        double healPerTick = getDoubleParam(plugin, item, "heal_tick", 1.0);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_GRASS_PLACE, 1.0f, 0.8f);
        new CompatRunnable() {
            int ticksElapsed = 0;
            @Override
            public void run() {
                if (!player.isOnline() || ticksElapsed >= (duration * 20)) {
                    cancel();
                    return;
                }
                Material ground = player.getLocation().clone().subtract(0, 1, 0).getBlock().getType();
                if (isNatural(ground)) {
                    double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                    player.setHealth(Math.min(maxHealth, player.getHealth() + healPerTick));
                    player.getWorld().spawnParticle(Particle.COMPOSTER, player.getLocation(), 3, 0.2, 0.1, 0.2, 0);
                }
                ticksElapsed += 20;
            }
            private boolean isNatural(Material m) {
                String name = m.name();
                return name.contains("GRASS") || name.contains("DIRT") || name.contains("MYCELIUM") || name.contains("MOSS") || name.contains("PODZOL");
            }
        }.runTaskTimer(plugin, player, 20L, 20L);
        return true;
    }
}

class SunflowerTurn extends Ability {
    private final ItemEditFull plugin;
    public SunflowerTurn(ItemEditFull plugin) {
        super("sunflower_turn", "Sunflower Turn", "Tracks the light of the sun to grant a stacking speed buff outdoors.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 8.0);
        int maxStacks = getIntParam(plugin, item, "max_stacks", 3);
        boolean outdoors = player.getLocation().getBlock().getLightFromSky() > 0;
        if (!outdoors) {
            player.sendMessage("§cYou must stand beneath the open sky.");
            return false;
        }
        int stacks = OverworldExpansionAbilities.addSunflowerStack(player.getUniqueId(), maxStacks);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, (int) (duration * 20), stacks - 1));
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_SWEET_BERRY_BUSH_PLACE, 1.0f, 1.3f);
        player.sendMessage("§eSunflower stacks: " + stacks + "/" + maxStacks);
        return true;
    }
}
