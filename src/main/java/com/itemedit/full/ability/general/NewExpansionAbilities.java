package com.itemedit.full.ability.general;

import com.itemedit.full.ItemEditFull;
import com.itemedit.full.ability.Ability;
import org.bukkit.*;
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

public class NewExpansionAbilities implements Listener {
    private static ItemEditFull pluginInstance;
    private static final Map<UUID, Long> piglinRageActive = new HashMap<>();
    private static final Map<UUID, Long> activeBreezeDeflects = new HashMap<>();
    private static final List<Location> magmaTrailLocations = new ArrayList<>();

    public static void playSoundSafe(Location loc, String soundName, Sound fallbackSound, float volume, float pitch) {
        try {
            loc.getWorld().playSound(loc, Sound.valueOf(soundName), volume, pitch);
        } catch (Exception e) {
            loc.getWorld().playSound(loc, fallbackSound, volume, pitch);
        }
    }

    public static void register(ItemEditFull plugin) {
        pluginInstance = plugin;
        
        // Nether Expansion (1-20)
        plugin.getAbilityManager().registerAbility(new MagmaShield(plugin));
        plugin.getAbilityManager().registerAbility(new MagmaJump(plugin));
        plugin.getAbilityManager().registerAbility(new MagmaTrail(plugin));
        plugin.getAbilityManager().registerAbility(new MagmaFist(plugin));
        plugin.getAbilityManager().registerAbility(new BlazeSpeed(plugin));
        plugin.getAbilityManager().registerAbility(new BlazeAura(plugin));
        plugin.getAbilityManager().registerAbility(new BlazeFlight(plugin));
        plugin.getAbilityManager().registerAbility(new BlazeBarrage(plugin));
        plugin.getAbilityManager().registerAbility(new GhastFloat(plugin));
        plugin.getAbilityManager().registerAbility(new GhastFireball(plugin));
        plugin.getAbilityManager().registerAbility(new GhastScream(plugin));
        plugin.getAbilityManager().registerAbility(new GhastTear(plugin));
        plugin.getAbilityManager().registerAbility(new PiglinGreed(plugin));
        plugin.getAbilityManager().registerAbility(new PiglinCrossbow(plugin));
        plugin.getAbilityManager().registerAbility(new PiglinRage(plugin));
        plugin.getAbilityManager().registerAbility(new PiglinBarter(plugin));
        plugin.getAbilityManager().registerAbility(new NetherQuake(plugin));
        plugin.getAbilityManager().registerAbility(new NetherHeat(plugin));
        plugin.getAbilityManager().registerAbility(new NetherPortalRift(plugin));
        plugin.getAbilityManager().registerAbility(new NetherSoulDrain(plugin));

        // Ocean / Aquatic (21-40)
        plugin.getAbilityManager().registerAbility(new GuardianBeam(plugin));
        plugin.getAbilityManager().registerAbility(new GuardianThorns(plugin));
        plugin.getAbilityManager().registerAbility(new ElderGuardianFatigue(plugin));
        plugin.getAbilityManager().registerAbility(new ElderGuardianGhost(plugin));
        plugin.getAbilityManager().registerAbility(new DrownedTridentAbility(plugin));
        plugin.getAbilityManager().registerAbility(new DrownedWaterLeap(plugin));
        plugin.getAbilityManager().registerAbility(new DrownedConduitPower(plugin));
        plugin.getAbilityManager().registerAbility(new DrownedDepthStrider(plugin));
        plugin.getAbilityManager().registerAbility(new DolphinGraceAbility(plugin));
        plugin.getAbilityManager().registerAbility(new DolphinLeap(plugin));
        plugin.getAbilityManager().registerAbility(new DolphinSonic(plugin));
        plugin.getAbilityManager().registerAbility(new TurtleShell(plugin));
        plugin.getAbilityManager().registerAbility(new TurtleFortitude(plugin));
        plugin.getAbilityManager().registerAbility(new SquidInkAbility(plugin));
        plugin.getAbilityManager().registerAbility(new SquidBlindness(plugin));
        plugin.getAbilityManager().registerAbility(new SquidPropulsion(plugin));
        plugin.getAbilityManager().registerAbility(new FishAgility(plugin));
        plugin.getAbilityManager().registerAbility(new FishWaterBreathing(plugin));
        plugin.getAbilityManager().registerAbility(new OceanTempest(plugin));
        plugin.getAbilityManager().registerAbility(new OceanTsunami(plugin));

        // Sky / Aero (41-60)
        plugin.getAbilityManager().registerAbility(new PhantomGlide(plugin));
        plugin.getAbilityManager().registerAbility(new PhantomSwoop(plugin));
        plugin.getAbilityManager().registerAbility(new PhantomInsomnia(plugin));
        plugin.getAbilityManager().registerAbility(new PhantomBite(plugin));
        plugin.getAbilityManager().registerAbility(new BreezeWindCharge(plugin));
        plugin.getAbilityManager().registerAbility(new BreezeDeflect(plugin));
        plugin.getAbilityManager().registerAbility(new BreezeLeap(plugin));
        plugin.getAbilityManager().registerAbility(new BreezeGust(plugin));
        plugin.getAbilityManager().registerAbility(new BatSonar(plugin));
        plugin.getAbilityManager().registerAbility(new BatScreech(plugin));
        plugin.getAbilityManager().registerAbility(new BatFlight(plugin));
        plugin.getAbilityManager().registerAbility(new ChickenEggVolley(plugin));
        plugin.getAbilityManager().registerAbility(new ChickenFeatherShield(plugin));
        plugin.getAbilityManager().registerAbility(new ChickenSlowFall(plugin));
        plugin.getAbilityManager().registerAbility(new BeeSting(plugin));
        plugin.getAbilityManager().registerAbility(new BeeHoneyTrap(plugin));
        plugin.getAbilityManager().registerAbility(new BeeSwarmSummon(plugin));
        plugin.getAbilityManager().registerAbility(new BeePollinate(plugin));
        plugin.getAbilityManager().registerAbility(new SkyLightningStrike(plugin));
        plugin.getAbilityManager().registerAbility(new SkyZephyr(plugin));

        // Ancient / Sculk (61-70)
        plugin.getAbilityManager().registerAbility(new SculkShriekerSound(plugin));
        plugin.getAbilityManager().registerAbility(new SculkSensorPing(plugin));
        plugin.getAbilityManager().registerAbility(new SculkBlindnessAura(plugin));
        plugin.getAbilityManager().registerAbility(new SculkInfectionStrike(plugin));
        plugin.getAbilityManager().registerAbility(new SculkVeinSpread(plugin));
        plugin.getAbilityManager().registerAbility(new SculkCatalystHeal(plugin));
        plugin.getAbilityManager().registerAbility(new WardenSonicStrike(plugin));
        plugin.getAbilityManager().registerAbility(new WardenDarknessBurst(plugin));
        plugin.getAbilityManager().registerAbility(new WardenSculkStep(plugin));
        plugin.getAbilityManager().registerAbility(new WardenVibrationSense(plugin));

        // Ice / Tundra (71-80)
        plugin.getAbilityManager().registerAbility(new StraySlownessArrow(plugin));
        plugin.getAbilityManager().registerAbility(new StrayFreezeTouch(plugin));
        plugin.getAbilityManager().registerAbility(new StraySnowStorm(plugin));
        plugin.getAbilityManager().registerAbility(new SnowGolemSnowball(plugin));
        plugin.getAbilityManager().registerAbility(new SnowGolemTrail(plugin));
        plugin.getAbilityManager().registerAbility(new SnowGolemFreeze(plugin));
        plugin.getAbilityManager().registerAbility(new IceSpikeSummon(plugin));
        plugin.getAbilityManager().registerAbility(new IceShield(plugin));
        plugin.getAbilityManager().registerAbility(new IceSkate(plugin));
        plugin.getAbilityManager().registerAbility(new IceFrostbite(plugin));

        // Desert / Mesa (81-90)
        plugin.getAbilityManager().registerAbility(new HuskHungerStrike(plugin));
        plugin.getAbilityManager().registerAbility(new HuskSandStorm(plugin));
        plugin.getAbilityManager().registerAbility(new HuskDesertHeat(plugin));
        plugin.getAbilityManager().registerAbility(new ShulkerLevitationBulletAbility(plugin));
        plugin.getAbilityManager().registerAbility(new ShulkerShellClose(plugin));
        plugin.getAbilityManager().registerAbility(new ShulkerTeleport(plugin));
        plugin.getAbilityManager().registerAbility(new DesertMirage(plugin));
        plugin.getAbilityManager().registerAbility(new DesertQuicksand(plugin));
        plugin.getAbilityManager().registerAbility(new DesertCactusThorns(plugin));
        plugin.getAbilityManager().registerAbility(new DesertSunstroke(plugin));

        // Magical / Illager (91-100)
        plugin.getAbilityManager().registerAbility(new EvokerFangsAbility(plugin));
        plugin.getAbilityManager().registerAbility(new EvokerVexesAbility(plugin));
        plugin.getAbilityManager().registerAbility(new VindicatorAxeRush(plugin));
        plugin.getAbilityManager().registerAbility(new VindicatorRage(plugin));
        plugin.getAbilityManager().registerAbility(new IllusionerClone(plugin));
        plugin.getAbilityManager().registerAbility(new IllusionerBlindness(plugin));
        plugin.getAbilityManager().registerAbility(new WitchPotionThrowAbility(plugin));
        plugin.getAbilityManager().registerAbility(new WitchPotionDrink(plugin));
        plugin.getAbilityManager().registerAbility(new WitchBrewCauldron(plugin));
        plugin.getAbilityManager().registerAbility(new WitchPoisonCloud(plugin));

        // Custom Legendary Combat Strikes (101-129)
        plugin.getAbilityManager().registerAbility(new FlameStrike(plugin));
        plugin.getAbilityManager().registerAbility(new TsunamiSlash(plugin));
        plugin.getAbilityManager().registerAbility(new WindGustSlash(plugin));
        plugin.getAbilityManager().registerAbility(new VoidSlayer(plugin));
        plugin.getAbilityManager().registerAbility(new EarthSmash(plugin));
        plugin.getAbilityManager().registerAbility(new ThunderClap(plugin));
        plugin.getAbilityManager().registerAbility(new HolyPurify(plugin));
        plugin.getAbilityManager().registerAbility(new ShadowStrike(plugin));
        plugin.getAbilityManager().registerAbility(new MeteorSlam(plugin));
        plugin.getAbilityManager().registerAbility(new LunarCrescent(plugin));
        plugin.getAbilityManager().registerAbility(new SolarFlare(plugin));
        plugin.getAbilityManager().registerAbility(new CosmicRift(plugin));
        plugin.getAbilityManager().registerAbility(new PlagueStrike(plugin));
        plugin.getAbilityManager().registerAbility(new FrostGiantSlam(plugin));
        plugin.getAbilityManager().registerAbility(new VampiricEdge(plugin));
        plugin.getAbilityManager().registerAbility(new ReaperScythe(plugin));
        plugin.getAbilityManager().registerAbility(new ExecutionerChop(plugin));
        plugin.getAbilityManager().registerAbility(new GravityPullStrike(plugin));
        plugin.getAbilityManager().registerAbility(new MagneticDraw(plugin));
        plugin.getAbilityManager().registerAbility(new ToxicSlash(plugin));
        plugin.getAbilityManager().registerAbility(new WitherCleave(plugin));
        plugin.getAbilityManager().registerAbility(new PhoenixStrike(plugin));
        plugin.getAbilityManager().registerAbility(new GlacierCrash(plugin));
        plugin.getAbilityManager().registerAbility(new TempestStrike(plugin));
        plugin.getAbilityManager().registerAbility(new SculkShatter(plugin));
        plugin.getAbilityManager().registerAbility(new VolcanicRupture(plugin));
        plugin.getAbilityManager().registerAbility(new BreezeBurstStrike(plugin));
        plugin.getAbilityManager().registerAbility(new DragonClawSlash(plugin));
        plugin.getAbilityManager().registerAbility(new AbyssalDrownStrike(plugin));

        plugin.getServer().getPluginManager().registerEvents(new NewExpansionAbilities(), plugin);

        // Magma Trail Damage Loop
        new BukkitRunnable() {
            @Override
            public void run() {
                synchronized (magmaTrailLocations) {
                    Iterator<Location> it = magmaTrailLocations.iterator();
                    while (it.hasNext()) {
                        Location loc = it.next();
                        loc.getWorld().spawnParticle(Particle.FLAME, loc, 3, 0.2, 0.1, 0.2, 0.02);
                        for (Entity ent : loc.getWorld().getNearbyEntities(loc, 1.2, 1.2, 1.2)) {
                            if (ent instanceof LivingEntity) {
                                LivingEntity le = (LivingEntity) ent;
                                le.setFireTicks(40);
                                le.damage(1.5);
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 10L, 10L);
    }

    public static void addMagmaTrailLocation(Location loc) {
        synchronized (magmaTrailLocations) {
            magmaTrailLocations.add(loc);
            new BukkitRunnable() {
                @Override
                public void run() {
                    synchronized (magmaTrailLocations) {
                        magmaTrailLocations.remove(loc);
                    }
                }
            }.runTaskLater(pluginInstance, 100L); // 5 seconds duration
        }
    }

    public static void addBreezeDeflect(UUID uuid) {
        activeBreezeDeflects.put(uuid, System.currentTimeMillis() + 10000L); // 10s
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        Projectile entity = event.getEntity();
        if (entity.hasMetadata("ghast_fireball")) {
            Location loc = event.getHitBlock() != null ? event.getHitBlock().getLocation() : event.getEntity().getLocation();
            loc.getWorld().createExplosion(loc, 4.0f, true, true);
        } else if (entity.hasMetadata("shulker_bullet")) {
            if (event.getHitEntity() instanceof LivingEntity) {
                ((LivingEntity) event.getHitEntity()).addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 100, 1));
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player p = (Player) event.getEntity();
            Long expire = activeBreezeDeflects.get(p.getUniqueId());
            if (expire != null && System.currentTimeMillis() < expire) {
                if (event.getDamager() instanceof Projectile) {
                    event.setCancelled(true);
                    p.getWorld().spawnParticle(Particle.CLOUD, p.getLocation(), 15, 0.4, 0.4, 0.4, 0.1);
                    playSoundSafe(p.getLocation(), "ENTITY_WIND_CHARGE_THROW", Sound.ENTITY_FIREWORK_ROCKET_SHOOT, 0.8f, 1.4f);
                }
            }
        }

        if (event.getDamager() instanceof Player && event.getEntity() instanceof LivingEntity) {
            Player p = (Player) event.getDamager();
            LivingEntity t = (LivingEntity) event.getEntity();
            ItemStack hand = p.getInventory().getItemInMainHand();
            List<String> abs = pluginInstance.getAbilityManager().getItemAbilities(hand);
            
            if (abs.contains("magma_fist")) {
                t.setFireTicks(100);
                t.damage(3.0);
                t.getWorld().spawnParticle(Particle.FLAME, t.getLocation(), 8, 0.3, 0.3, 0.3, 0.05);
            }
            if (abs.contains("husk_hunger_strike")) {
                t.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 120, 2));
                t.getWorld().playSound(t.getLocation(), Sound.ENTITY_HUSK_AMBIENT, 0.8f, 0.9f);
            }
            if (abs.contains("piglin_rage")) {
                double mult = 1.0 + (1.0 - (p.getHealth() / p.getMaxHealth()));
                event.setDamage(event.getDamage() * mult);
            }
            if (abs.contains("vampiric_edge")) {
                double heal = Math.min(2.0, event.getFinalDamage() * 0.15);
                p.setHealth(Math.min(p.getMaxHealth(), p.getHealth() + heal));
                p.getWorld().spawnParticle(Particle.HEART, p.getLocation().add(0, 1.5, 0), 3, 0.2, 0.2, 0.2, 0.01);
            }
        }
    }
}

// Subclasses (1-20 Nether)
class MagmaShield extends Ability {
    public MagmaShield(ItemEditFull pl) { super("magma_shield", "Magma Shield", "Defends against fire and damage."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 300, 0)); p.getWorld().playSound(p.getLocation(), Sound.ITEM_FIRECHARGE_USE, 1f, 1f); return true; }
}
class MagmaJump extends Ability {
    public MagmaJump(ItemEditFull pl) { super("magma_jump", "Magma Jump", "Leap high using thermal energy."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.setVelocity(new Vector(0, 1.2, 0)); p.getWorld().spawnParticle(Particle.FLAME, p.getLocation(), 20, 0.5, 0.5, 0.5, 0.1); return true; }
}
class MagmaTrail extends Ability {
    private final ItemEditFull plugin;
    public MagmaTrail(ItemEditFull pl) { super("magma_trail", "Magma Trail", "Leaves a blazing path."); this.plugin = pl; }
    @Override public boolean trigger(Player p, ItemStack i) {
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (!p.isOnline() || ticks++ > 10) { cancel(); return; }
                NewExpansionAbilities.addMagmaTrailLocation(p.getLocation().add(0, 0.1, 0));
            }
        }.runTaskTimer(plugin, 0L, 10L);
        return true;
    }
}
class MagmaFist extends Ability {
    public MagmaFist(ItemEditFull pl) { super("magma_fist", "Magma Fist", "Strike with fiery impact."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.getWorld().playSound(p.getLocation(), Sound.ENTITY_BLAZE_AMBIENT, 0.8f, 1.1f); return true; }
}
class BlazeSpeed extends Ability {
    public BlazeSpeed(ItemEditFull pl) { super("blaze_speed", "Blaze Speed", "High velocity flame dash."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 1)); p.getWorld().spawnParticle(Particle.FLAME, p.getLocation(), 15, 0.2, 0.2, 0.2, 0.1); return true; }
}
class BlazeAura extends Ability {
    private final ItemEditFull plugin;
    public BlazeAura(ItemEditFull pl) { super("blaze_aura", "Blaze Aura", "Burns nearby targets."); this.plugin = pl; }
    @Override public boolean trigger(Player p, ItemStack i) {
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (!p.isOnline() || ticks++ > 5) { cancel(); return; }
                Location loc = p.getLocation();
                loc.getWorld().spawnParticle(Particle.FLAME, loc, 15, 4.0, 0.5, 4.0, 0.05);
                for (Entity ent : loc.getWorld().getNearbyEntities(loc, 4.0, 2.0, 4.0)) {
                    if (ent instanceof LivingEntity && !ent.equals(p)) {
                        LivingEntity le = (LivingEntity) ent;
                        le.setFireTicks(80);
                        le.damage(2.0, p);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
        return true;
    }
}
class BlazeFlight extends Ability {
    public BlazeFlight(ItemEditFull pl) { super("blaze_flight", "Blaze Flight", "Propels player upwards."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.setVelocity(p.getLocation().getDirection().multiply(1.5)); return true; }
}
class BlazeBarrage extends Ability {
    public BlazeBarrage(ItemEditFull pl) { super("blaze_barrage", "Blaze Barrage", "Fires fireballs."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.launchProjectile(SmallFireball.class); return true; }
}
class GhastFloat extends Ability {
    public GhastFloat(ItemEditFull pl) { super("ghast_float", "Ghast Float", "Safe slow falling."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 200, 0)); return true; }
}
class GhastFireball extends Ability {
    private final ItemEditFull pl;
    public GhastFireball(ItemEditFull pl) { super("ghast_fireball", "Ghast Fireball", "Spawns giant explosions."); this.pl = pl; }
    @Override public boolean trigger(Player p, ItemStack i) { Fireball f = p.launchProjectile(LargeFireball.class); f.setMetadata("ghast_fireball", new FixedMetadataValue(pl, true)); return true; }
}
class GhastScream extends Ability {
    public GhastScream(ItemEditFull pl) { super("ghast_scream", "Ghast Scream", "Knocks back targets."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.getWorld().playSound(p.getLocation(), Sound.ENTITY_GHAST_SCREAM, 1.5f, 1f); return true; }
}
class GhastTear extends Ability {
    public GhastTear(ItemEditFull pl) { super("ghast_tear", "Ghast Tear", "Quick health restore."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 1)); return true; }
}
class PiglinGreed extends Ability {
    public PiglinGreed(ItemEditFull pl) { super("piglin_greed", "Piglin Greed", "Adrenaline boost."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 160, 0)); return true; }
}
class PiglinCrossbow extends Ability {
    public PiglinCrossbow(ItemEditFull pl) { super("piglin_crossbow", "Piglin Crossbow", "Ranged barrage."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.launchProjectile(Arrow.class); return true; }
}
class PiglinRage extends Ability {
    public PiglinRage(ItemEditFull pl) { super("piglin_rage", "Piglin Rage", "Damage scales with missing health."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class PiglinBarter extends Ability {
    public PiglinBarter(ItemEditFull pl) { super("piglin_barter", "Piglin Barter", "Boosts luck."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, 600, 1)); return true; }
}
class NetherQuake extends Ability {
    public NetherQuake(ItemEditFull pl) { super("nether_quake", "Nether Quake", "Slam waves."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class NetherHeat extends Ability {
    public NetherHeat(ItemEditFull pl) { super("nether_heat", "Nether Heat", "Igniation aura."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class NetherPortalRift extends Ability {
    public NetherPortalRift(ItemEditFull pl) { super("nether_portal_rift", "Nether Rift", "Portal dash."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.teleport(p.getLocation().add(p.getLocation().getDirection().multiply(6))); return true; }
}
class NetherSoulDrain extends Ability {
    public NetherSoulDrain(ItemEditFull pl) { super("nether_soul_drain", "Nether Soul Drain", "Drain life force."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}

// Subclasses (21-40 Ocean)
class GuardianBeam extends Ability {
    public GuardianBeam(ItemEditFull pl) { super("guardian_beam", "Guardian Beam", "Beam blast."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class GuardianThorns extends Ability {
    public GuardianThorns(ItemEditFull pl) { super("guardian_thorns", "Guardian Thorns", "Recoil defense."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class ElderGuardianFatigue extends Ability {
    public ElderGuardianFatigue(ItemEditFull pl) { super("elder_guardian_fatigue", "Elder Fatigue", "Fatigues enemies."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class ElderGuardianGhost extends Ability {
    public ElderGuardianGhost(ItemEditFull pl) { super("elder_guardian_ghost", "Elder Curse", "Curse targets."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class DrownedTridentAbility extends Ability {
    public DrownedTridentAbility(ItemEditFull pl) { super("drowned_trident", "Drowned Trident", "Trident strike."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.launchProjectile(Trident.class); return true; }
}
class DrownedWaterLeap extends Ability {
    public DrownedWaterLeap(ItemEditFull pl) { super("drowned_water_leap", "Water Leap", "Quick water vault."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.setVelocity(new Vector(0, 1.1, 0)); return true; }
}
class DrownedConduitPower extends Ability {
    public DrownedConduitPower(ItemEditFull pl) { super("drowned_conduit_power", "Conduit Grace", "Conduit strength."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.addPotionEffect(new PotionEffect(PotionEffectType.CONDUIT_POWER, 300, 0)); return true; }
}
class DrownedDepthStrider extends Ability {
    public DrownedDepthStrider(ItemEditFull pl) { super("drowned_depth_strider", "Drowned Speed", "Agile swimming."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 300, 0)); return true; }
}
class DolphinGraceAbility extends Ability {
    public DolphinGraceAbility(ItemEditFull pl) { super("dolphin_grace", "Dolphin Grace", "Speed in water."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 400, 0)); return true; }
}
class DolphinLeap extends Ability {
    public DolphinLeap(ItemEditFull pl) { super("dolphin_leap", "Dolphin Leap", "Launch out of water."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.setVelocity(p.getLocation().getDirection().multiply(1.8).setY(0.5)); return true; }
}
class DolphinSonic extends Ability {
    public DolphinSonic(ItemEditFull pl) { super("dolphin_sonic", "Dolphin Sonic", "Sonic burst."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class TurtleShell extends Ability {
    public TurtleShell(ItemEditFull pl) { super("turtle_shell", "Turtle Shell", "Crouch defense."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 200, 2)); return true; }
}
class TurtleFortitude extends Ability {
    public TurtleFortitude(ItemEditFull pl) { super("turtle_fortitude", "Turtle Fortitude", "Steady ground."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class SquidInkAbility extends Ability {
    public SquidInkAbility(ItemEditFull pl) { super("squid_ink", "Squid Ink", "Blind targets nearby."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class SquidBlindness extends Ability {
    public SquidBlindness(ItemEditFull pl) { super("squid_blindness", "Ink Strike", "Strikes blind."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class SquidPropulsion extends Ability {
    public SquidPropulsion(ItemEditFull pl) { super("squid_propulsion", "Ink Jets", "Rapid dash backwards."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.setVelocity(p.getLocation().getDirection().multiply(-1.5)); return true; }
}
class FishAgility extends Ability {
    public FishAgility(ItemEditFull pl) { super("fish_agility", "Fish Agility", "Swift aquatic maneuvers."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class FishWaterBreathing extends Ability {
    public FishWaterBreathing(ItemEditFull pl) { super("fish_water_breathing", "Water Gills", "Breathe underwater."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 1200, 0)); return true; }
}
class OceanTempest extends Ability {
    public OceanTempest(ItemEditFull pl) { super("ocean_tempest", "Water Tempest", "Launches entities."); }
    @Override public boolean trigger(Player p, ItemStack i) {
        Location loc = p.getLocation();
        p.getWorld().playSound(loc, Sound.BLOCK_WATER_AMBIENT, 1.2f, 0.8f);
        p.getWorld().spawnParticle(Particle.WATER_SPLASH, loc, 50, 3.0, 1.0, 3.0, 0.1);
        for (Entity ent : p.getWorld().getNearbyEntities(loc, 4.0, 3.0, 4.0)) {
            if (ent instanceof LivingEntity && !ent.equals(p)) {
                LivingEntity le = (LivingEntity) ent;
                le.setVelocity(new Vector(0, 0.8, 0));
                le.damage(4.0, p);
            }
        }
        return true;
    }
}
class OceanTsunami extends Ability {
    public OceanTsunami(ItemEditFull pl) { super("ocean_tsunami", "Tsunami Wave", "Wave push."); }
    @Override public boolean trigger(Player p, ItemStack i) {
        Location loc = p.getLocation();
        Vector dir = loc.getDirection().setY(0).normalize();
        p.getWorld().playSound(loc, Sound.ITEM_BUCKET_EMPTY, 1.5f, 0.7f);
        for (int k = 1; k <= 6; k++) {
            Location step = loc.clone().add(dir.clone().multiply(k));
            p.getWorld().spawnParticle(Particle.WATER_SPLASH, step, 10, 0.5, 0.5, 0.5, 0.05);
            for (Entity ent : step.getWorld().getNearbyEntities(step, 1.5, 1.5, 1.5)) {
                if (ent instanceof LivingEntity && !ent.equals(p)) {
                    LivingEntity le = (LivingEntity) ent;
                    le.setVelocity(dir.clone().multiply(1.4).setY(0.25));
                    le.damage(3.5, p);
                }
            }
        }
        return true;
    }
}

// Subclasses (41-60 Sky)
class PhantomGlide extends Ability {
    public PhantomGlide(ItemEditFull pl) { super("phantom_glide", "Phantom Glide", "Slow glide."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 200, 0)); return true; }
}
class PhantomSwoop extends Ability {
    public PhantomSwoop(ItemEditFull pl) { super("phantom_swoop", "Phantom Swoop", "Aerial slam."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.setVelocity(p.getLocation().getDirection().multiply(1.8).setY(-0.5)); return true; }
}
class PhantomInsomnia extends Ability {
    public PhantomInsomnia(ItemEditFull pl) { super("phantom_insomnia", "Insomnia Strike", "Apply darkness."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class PhantomBite extends Ability {
    public PhantomBite(ItemEditFull pl) { super("phantom_bite", "Phantom Bite", "Life leech."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class BreezeWindCharge extends Ability {
    public BreezeWindCharge(ItemEditFull pl) { super("breeze_wind_charge", "Wind Charge", "Fires blast."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.launchProjectile(WindCharge.class); return true; }
}
class BreezeDeflect extends Ability {
    public BreezeDeflect(ItemEditFull pl) { super("breeze_deflect", "Wind Shield", "Arrows bounce."); }
    @Override public boolean trigger(Player p, ItemStack i) {
        NewExpansionAbilities.addBreezeDeflect(p.getUniqueId());
        NewExpansionAbilities.playSoundSafe(p.getLocation(), "ENTITY_WIND_CHARGE_WIND_BURST", Sound.ENTITY_FIREWORK_ROCKET_SHOOT, 1.2f, 1.2f);
        return true;
    }
}
class BreezeLeap extends Ability {
    public BreezeLeap(ItemEditFull pl) { super("breeze_leap", "Breeze Leap", "Vault high."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.setVelocity(new Vector(0, 1.4, 0)); return true; }
}
class BreezeGust extends Ability {
    public BreezeGust(ItemEditFull pl) { super("breeze_gust", "Gust Blast", "Wind push."); }
    @Override public boolean trigger(Player p, ItemStack i) {
        Location loc = p.getLocation();
        NewExpansionAbilities.playSoundSafe(loc, "ENTITY_WIND_CHARGE_WIND_BURST", Sound.ENTITY_FIREWORK_ROCKET_SHOOT, 1.5f, 0.9f);
        p.getWorld().spawnParticle(Particle.CLOUD, loc, 30, 3.0, 0.5, 3.0, 0.15);
        for (Entity ent : p.getWorld().getNearbyEntities(loc, 4.0, 2.0, 4.0)) {
            if (ent instanceof LivingEntity && !ent.equals(p)) {
                LivingEntity le = (LivingEntity) ent;
                Vector push = le.getLocation().toVector().subtract(loc.toVector()).normalize().multiply(1.5).setY(0.3);
                le.setVelocity(push);
                le.damage(2.0, p);
            }
        }
        return true;
    }
}
class BatSonar extends Ability {
    public BatSonar(ItemEditFull pl) { super("bat_sonar", "Bat Sonar", "Highlights targets."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class BatScreech extends Ability {
    public BatScreech(ItemEditFull pl) { super("bat_screech", "Bat Screech", "Disorients mobs."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class BatFlight extends Ability {
    public BatFlight(ItemEditFull pl) { super("bat_flight", "Bat Flight", "Flaps upwards."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.setVelocity(new Vector(0, 0.7, 0)); return true; }
}
class ChickenEggVolley extends Ability {
    public ChickenEggVolley(ItemEditFull pl) { super("chicken_egg_volley", "Egg Volley", "Explosive egg shower."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.launchProjectile(Egg.class); return true; }
}
class ChickenFeatherShield extends Ability {
    public ChickenFeatherShield(ItemEditFull pl) { super("chicken_feather_shield", "Feather Shield", "Fall safety."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 100, 0)); return true; }
}
class ChickenSlowFall extends Ability {
    public ChickenSlowFall(ItemEditFull pl) { super("chicken_slow_fall", "Poultry Glide", "Slow fall glide."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 300, 0)); return true; }
}
class BeeSting extends Ability {
    public BeeSting(ItemEditFull pl) { super("bee_sting", "Bee Sting", "Poison attack."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class BeeHoneyTrap extends Ability {
    public BeeHoneyTrap(ItemEditFull pl) { super("bee_honey_trap", "Honey Trap", "Slow pool."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class BeeSwarmSummon extends Ability {
    public BeeSwarmSummon(ItemEditFull pl) { super("bee_swarm", "Summon Bee Swarm", "Summon helpers."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class BeePollinate extends Ability {
    public BeePollinate(ItemEditFull pl) { super("bee_pollinate", "Bee Pollen", "Heals."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class SkyLightningStrike extends Ability {
    public SkyLightningStrike(ItemEditFull pl) { super("sky_lightning_strike", "Lightning Strike", "Lightning strike."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.getWorld().strikeLightning(p.getTargetBlock(null, 30).getLocation()); return true; }
}
class SkyZephyr extends Ability {
    public SkyZephyr(ItemEditFull pl) { super("sky_zephyr", "Sky Zephyr", "Wind lift."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}

// Subclasses (61-70 Ancient/Sculk)
class SculkShriekerSound extends Ability {
    public SculkShriekerSound(ItemEditFull pl) { super("sculk_shrieker_sound", "Sculk Shriek", "Sound blast."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.getWorld().playSound(p.getLocation(), Sound.BLOCK_SCULK_SHRIEKER_SHRIEK, 1.5f, 1f); return true; }
}
class SculkSensorPing extends Ability {
    public SculkSensorPing(ItemEditFull pl) { super("sculk_sensor_ping", "Sensor Ping", "Detects motion."); }
    @Override public boolean trigger(Player p, ItemStack i) {
        Location loc = p.getLocation();
        NewExpansionAbilities.playSoundSafe(loc, "BLOCK_SCULK_SENSOR_CLICK", Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
        p.getWorld().spawnParticle(Particle.SPELL_INSTANT, loc, 25, 10.0, 1.0, 10.0, 0.05);
        for (Entity ent : p.getWorld().getNearbyEntities(loc, 10.0, 3.0, 10.0)) {
            if (ent instanceof LivingEntity && !ent.equals(p)) {
                ((LivingEntity) ent).addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 100, 0));
            }
        }
        return true;
    }
}
class SculkBlindnessAura extends Ability {
    public SculkBlindnessAura(ItemEditFull pl) { super("sculk_blindness_aura", "Sculk Darkness", "Apply darkness."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 200, 0)); return true; }
}
class SculkInfectionStrike extends Ability {
    public SculkInfectionStrike(ItemEditFull pl) { super("sculk_infection_strike", "Sculk Infection", "Infects targets."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class SculkVeinSpread extends Ability {
    public SculkVeinSpread(ItemEditFull pl) { super("sculk_vein_spread", "Sculk Spread", "Trail blocks."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class SculkCatalystHeal extends Ability {
    public SculkCatalystHeal(ItemEditFull pl) { super("sculk_catalyst_heal", "Sculk Catalyst", "Drains XP."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class WardenSonicStrike extends Ability {
    public WardenSonicStrike(ItemEditFull pl) { super("warden_sonic_strike", "Sonic Strike", "Sonic wave."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.getWorld().playSound(p.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 1.5f, 1f); return true; }
}
class WardenDarknessBurst extends Ability {
    public WardenDarknessBurst(ItemEditFull pl) { super("warden_darkness_burst", "Darkness Burst", "Dark wave."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 200, 0)); return true; }
}
class WardenSculkStep extends Ability {
    public WardenSculkStep(ItemEditFull pl) { super("warden_sculk_step", "Sculk Step", "Speed step."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 300, 1)); return true; }
}
class WardenVibrationSense extends Ability {
    public WardenVibrationSense(ItemEditFull pl) { super("warden_vibration_sense", "Vibration Sense", "Highlights targets."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}

// Subclasses (71-80 Ice)
class StraySlownessArrow extends Ability {
    public StraySlownessArrow(ItemEditFull pl) { super("stray_slowness_arrow", "Slowness Arrow", "Slowness shots."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.launchProjectile(Arrow.class); return true; }
}
class StrayFreezeTouch extends Ability {
    public StrayFreezeTouch(ItemEditFull pl) { super("stray_freeze_touch", "Freeze Touch", "Freezes targets."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class StraySnowStorm extends Ability {
    public StraySnowStorm(ItemEditFull pl) { super("stray_snow_storm", "Blizzard Storm", "Snow wave."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class SnowGolemSnowball extends Ability {
    public SnowGolemSnowball(ItemEditFull pl) { super("snow_golem_snowball", "Snowball Barrage", "Shoot snowballs."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.launchProjectile(Snowball.class); return true; }
}
class SnowGolemTrail extends Ability {
    public SnowGolemTrail(ItemEditFull pl) { super("snow_golem_trail", "Snow Trail", "Leaves snow."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class SnowGolemFreeze extends Ability {
    public SnowGolemFreeze(ItemEditFull pl) { super("snow_golem_freeze", "Snow Golem Freeze", "Freeze block."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class IceSpikeSummon extends Ability {
    public IceSpikeSummon(ItemEditFull pl) { super("ice_spike_summon", "Ice Spike", "Summons spikes."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class IceShield extends Ability {
    public IceShield(ItemEditFull pl) { super("ice_shield", "Ice Shield", "Defensive shield."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 200, 1)); return true; }
}
class IceSkate extends Ability {
    public IceSkate(ItemEditFull pl) { super("ice_skate", "Ice Skate", "Speed on ice."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 2)); return true; }
}
class IceFrostbite extends Ability {
    public IceFrostbite(ItemEditFull pl) { super("ice_frostbite", "Ice Frostbite", "Continuous freeze."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}

// Subclasses (81-90 Desert)
class HuskHungerStrike extends Ability {
    public HuskHungerStrike(ItemEditFull pl) { super("husk_hunger_strike", "Hunger Strike", "Applies hunger."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class HuskSandStorm extends Ability {
    public HuskSandStorm(ItemEditFull pl) { super("husk_sand_storm", "Sand Storm", "Sand wave."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class HuskDesertHeat extends Ability {
    public HuskDesertHeat(ItemEditFull pl) { super("husk_desert_heat", "Desert Heat", "Heat blast."); }
    @Override public boolean trigger(Player p, ItemStack i) {
        Location loc = p.getLocation();
        p.getWorld().playSound(loc, Sound.ENTITY_HUSK_AMBIENT, 1.2f, 1.2f);
        p.getWorld().spawnParticle(Particle.FLAME, loc, 40, 4.0, 1.0, 4.0, 0.05);
        for (Entity ent : p.getWorld().getNearbyEntities(loc, 4.0, 2.0, 4.0)) {
            if (ent instanceof LivingEntity && !ent.equals(p)) {
                LivingEntity le = (LivingEntity) ent;
                le.setFireTicks(60);
                le.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 1));
                le.damage(3.0, p);
            }
        }
        return true;
    }
}
class ShulkerLevitationBulletAbility extends Ability {
    private final ItemEditFull pl;
    public ShulkerLevitationBulletAbility(ItemEditFull pl) { super("shulker_levitation_bullet", "Levitation Bullet", "Levitation bullet."); this.pl = pl; }
    @Override public boolean trigger(Player p, ItemStack i) { ShulkerBullet b = p.launchProjectile(ShulkerBullet.class); b.setMetadata("shulker_bullet", new FixedMetadataValue(pl, true)); return true; }
}
class ShulkerShellClose extends Ability {
    public ShulkerShellClose(ItemEditFull pl) { super("shulker_shell_close", "Shell Close", "High defense."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 160, 3)); return true; }
}
class ShulkerTeleport extends Ability {
    public ShulkerTeleport(ItemEditFull pl) { super("shulker_teleport", "Shulker Teleport", "Rift blink."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.teleport(p.getLocation().add(new Vector((Math.random() - 0.5) * 10, 0, (Math.random() - 0.5) * 10))); return true; }
}
class DesertMirage extends Ability {
    public DesertMirage(ItemEditFull pl) { super("desert_mirage", "Desert Mirage", "Clones."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class DesertQuicksand extends Ability {
    public DesertQuicksand(ItemEditFull pl) { super("desert_quicksand", "Quicksand", "Trap block."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class DesertCactusThorns extends Ability {
    public DesertCactusThorns(ItemEditFull pl) { super("desert_cactus_thorns", "Cactus Thorns", "Thorns recoil."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class DesertSunstroke extends Ability {
    public DesertSunstroke(ItemEditFull pl) { super("desert_sunstroke", "Sunstroke", "Blinds targets."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}

// Subclasses (91-100 Magical)
class EvokerFangsAbility extends Ability {
    public EvokerFangsAbility(ItemEditFull pl) { super("evoker_fangs", "Evoker Fangs", "Fang strike."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.getWorld().spawn(p.getLocation().add(p.getLocation().getDirection().multiply(2)), org.bukkit.entity.EvokerFangs.class); return true; }
}
class EvokerVexesAbility extends Ability {
    public EvokerVexesAbility(ItemEditFull pl) { super("evoker_vexes", "Summon Vexes", "Summons assistants."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.getWorld().spawn(p.getLocation(), org.bukkit.entity.Vex.class); return true; }
}
class VindicatorAxeRush extends Ability {
    public VindicatorAxeRush(ItemEditFull pl) { super("vindicator_axe_rush", "Axe Rush", "Axe dash."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.setVelocity(p.getLocation().getDirection().multiply(1.6)); return true; }
}
class VindicatorRage extends Ability {
    public VindicatorRage(ItemEditFull pl) { super("vindicator_rage", "Vindicator Rage", "Attack boost."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 200, 1)); return true; }
}
class IllusionerClone extends Ability {
    public IllusionerClone(ItemEditFull pl) { super("illusioner_clone", "Mirror Clone", "Distraction images."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class IllusionerBlindness extends Ability {
    public IllusionerBlindness(ItemEditFull pl) { super("illusioner_blindness", "Blindness Shot", "Blindness arrows."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class WitchPotionThrowAbility extends Ability {
    public WitchPotionThrowAbility(ItemEditFull pl) { super("witch_potion_throw", "Potion Throw", "Throws debuffs."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.launchProjectile(ThrownPotion.class); return true; }
}
class WitchPotionDrink extends Ability {
    public WitchPotionDrink(ItemEditFull pl) { super("witch_potion_drink", "Witch Drink", "Self heal potions."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.addPotionEffect(new PotionEffect(PotionEffectType.HEAL, 1, 1)); return true; }
}
class WitchBrewCauldron extends Ability {
    public WitchBrewCauldron(ItemEditFull pl) { super("witch_brew_cauldron", "Witch Cauldron", "Brew cloud."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class WitchPoisonCloud extends Ability {
    public WitchPoisonCloud(ItemEditFull pl) { super("witch_poison_cloud", "Poison Cloud", "Poison wave."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}

// Subclasses (101-129 Combat Strikes)
class FlameStrike extends Ability {
    public FlameStrike(ItemEditFull pl) { super("flame_strike", "Flame Strike", "Splash fire strike."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class TsunamiSlash extends Ability {
    public TsunamiSlash(ItemEditFull pl) { super("tsunami_slash", "Tsunami Slash", "Slash wave."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class WindGustSlash extends Ability {
    public WindGustSlash(ItemEditFull pl) { super("wind_gust_slash", "Wind Slash", "Pierces targets."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class VoidSlayer extends Ability {
    public VoidSlayer(ItemEditFull pl) { super("void_slayer", "Void Slayer", "Void damage strike."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class EarthSmash extends Ability {
    public EarthSmash(ItemEditFull pl) { super("earth_smash", "Earth Smash", "Ground slam."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class ThunderClap extends Ability {
    public ThunderClap(ItemEditFull pl) { super("thunder_clap", "Thunder Clap", "Sonic boom."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.getWorld().playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.5f, 1f); return true; }
}
class HolyPurify extends Ability {
    public HolyPurify(ItemEditFull pl) { super("holy_purify", "Holy Purify", "Burns undead."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.addPotionEffect(new PotionEffect(PotionEffectType.HEAL, 1, 1)); return true; }
}
class ShadowStrike extends Ability {
    public ShadowStrike(ItemEditFull pl) { super("shadow_strike", "Shadow Strike", "Teleport behind target."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class MeteorSlam extends Ability {
    public MeteorSlam(ItemEditFull pl) { super("meteor_slam", "Meteor Slam", "Fire impact slam."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.setVelocity(new Vector(0, -1.5, 0)); return true; }
}
class LunarCrescent extends Ability {
    public LunarCrescent(ItemEditFull pl) { super("lunar_crescent", "Lunar Crescent", "Crescent wave."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class SolarFlare extends Ability {
    public SolarFlare(ItemEditFull pl) { super("solar_flare", "Solar Flare", "Ignites targets."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class CosmicRift extends Ability {
    public CosmicRift(ItemEditFull pl) { super("cosmic_rift", "Cosmic Rift", "Pull-in rift."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class PlagueStrike extends Ability {
    public PlagueStrike(ItemEditFull pl) { super("plague_strike", "Plague Strike", "Poison strike."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class FrostGiantSlam extends Ability {
    public FrostGiantSlam(ItemEditFull pl) { super("frost_giant_slam", "Frost Slam", "Slowness slam."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class VampiricEdge extends Ability {
    public VampiricEdge(ItemEditFull pl) { super("vampiric_edge", "Vampiric Edge", "Life leech slash."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class ReaperScythe extends Ability {
    public ReaperScythe(ItemEditFull pl) { super("reaper_scythe", "Reaper Scythe", "Execution strike."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class ExecutionerChop extends Ability {
    public ExecutionerChop(ItemEditFull pl) { super("executioner_chop", "Executioner Chop", "Critical damage."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class GravityPullStrike extends Ability {
    public GravityPullStrike(ItemEditFull pl) { super("gravity_pull_strike", "Gravity Pull", "Pulls targets."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class MagneticDraw extends Ability {
    public MagneticDraw(ItemEditFull pl) { super("magnetic_draw", "Magnetic Draw", "Draws arrows."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class ToxicSlash extends Ability {
    public ToxicSlash(ItemEditFull pl) { super("toxic_slash", "Toxic Slash", "Fatal poison slash."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class WitherCleave extends Ability {
    public WitherCleave(ItemEditFull pl) { super("wither_cleave", "Wither Cleave", "Decay slash."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class PhoenixStrike extends Ability {
    public PhoenixStrike(ItemEditFull pl) { super("phoenix_strike", "Phoenix Strike", "Fire healing."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class GlacierCrash extends Ability {
    public GlacierCrash(ItemEditFull pl) { super("glacier_crash", "Glacier Crash", "Ice crash."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class TempestStrike extends Ability {
    public TempestStrike(ItemEditFull pl) { super("tempest_strike", "Tempest Strike", "Thunder cloud."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class SculkShatter extends Ability {
    public SculkShatter(ItemEditFull pl) { super("sculk_shatter", "Sculk Shatter", "Shatters armor."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class VolcanicRupture extends Ability {
    public VolcanicRupture(ItemEditFull pl) { super("volcanic_rupture", "Volcanic Rupture", "Magma spewer."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class BreezeBurstStrike extends Ability {
    public BreezeBurstStrike(ItemEditFull pl) { super("breeze_burst_strike", "Breeze Strike", "Wind sweep."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class DragonClawSlash extends Ability {
    public DragonClawSlash(ItemEditFull pl) { super("dragon_claw_slash", "Dragon Claw", "Sweep claw slash."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class AbyssalDrownStrike extends Ability {
    public AbyssalDrownStrike(ItemEditFull pl) { super("abyssal_drown_strike", "Abyssal Drown", "Fatigues targets."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
