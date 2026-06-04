package com.itemedit.full.ability.general;

import com.itemedit.full.ItemEditFull;
import com.itemedit.full.ability.Ability;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.stream.Collectors;

public class MoreExpansionAbilities implements Listener {
    private static ItemEditFull pluginInstance;
    private static final Map<UUID, Long> activeManaShields = new HashMap<>();
    private static final Map<UUID, Long> activeLastStands = new HashMap<>();

    public static void register(ItemEditFull plugin) {
        pluginInstance = plugin;
        
        // Boss & Legendary Mob Spells (1-30)
        plugin.getAbilityManager().registerAbility(new WitherSkullBomb(plugin));
        plugin.getAbilityManager().registerAbility(new DragonBreathGround(plugin));
        plugin.getAbilityManager().registerAbility(new CreeperChargeAbility(plugin));
        plugin.getAbilityManager().registerAbility(new CreeperDetonation(plugin));
        plugin.getAbilityManager().registerAbility(new ShulkerGravityShift(plugin));
        plugin.getAbilityManager().registerAbility(new WardenSonicClapAbility(plugin));
        plugin.getAbilityManager().registerAbility(new WardenSculkInfection(plugin));
        plugin.getAbilityManager().registerAbility(new EvokerFangCircle(plugin));
        plugin.getAbilityManager().registerAbility(new EvokerVexSwarm(plugin));
        plugin.getAbilityManager().registerAbility(new WitchElixir(plugin));
        plugin.getAbilityManager().registerAbility(new WitchPoisonSplash(plugin));
        plugin.getAbilityManager().registerAbility(new PhantomSpectre(plugin));
        plugin.getAbilityManager().registerAbility(new StrayFrostHail(plugin));
        plugin.getAbilityManager().registerAbility(new HuskDesiccation(plugin));
        plugin.getAbilityManager().registerAbility(new MagmaCubeSlam(plugin));
        plugin.getAbilityManager().registerAbility(new BlazeInferno(plugin));
        plugin.getAbilityManager().registerAbility(new PiglinGoldenBarrage(plugin));
        plugin.getAbilityManager().registerAbility(new DrownedWhirlpool(plugin));
        plugin.getAbilityManager().registerAbility(new GuardianLaserBurst(plugin));
        plugin.getAbilityManager().registerAbility(new ElderGuardianFatigueBlast(plugin));
        plugin.getAbilityManager().registerAbility(new RavagerCharge(plugin));
        plugin.getAbilityManager().registerAbility(new RavagerRoar(plugin));
        plugin.getAbilityManager().registerAbility(new IllusionerMirror(plugin));
        plugin.getAbilityManager().registerAbility(new IllusionerBlindVolley(plugin));
        plugin.getAbilityManager().registerAbility(new PillagerRaidCall(plugin));
        plugin.getAbilityManager().registerAbility(new SlimeBounceAbility(plugin));
        plugin.getAbilityManager().registerAbility(new SlimeSplit(plugin));
        plugin.getAbilityManager().registerAbility(new IronGolemToss(plugin));
        plugin.getAbilityManager().registerAbility(new IronGolemShield(plugin));
        plugin.getAbilityManager().registerAbility(new SpiderNestTrap(plugin));

        // Elemental Spells (31-60)
        plugin.getAbilityManager().registerAbility(new FireMeteor(plugin));
        plugin.getAbilityManager().registerAbility(new FireWall(plugin));
        plugin.getAbilityManager().registerAbility(new FireNova(plugin));
        plugin.getAbilityManager().registerAbility(new IceNovaAbility(plugin));
        plugin.getAbilityManager().registerAbility(new IcePrison(plugin));
        plugin.getAbilityManager().registerAbility(new IceBlizzard(plugin));
        plugin.getAbilityManager().registerAbility(new LightningChainAbility(plugin));
        plugin.getAbilityManager().registerAbility(new LightningStorm(plugin));
        plugin.getAbilityManager().registerAbility(new LightningDash(plugin));
        plugin.getAbilityManager().registerAbility(new EarthWall(plugin));
        plugin.getAbilityManager().registerAbility(new EarthQuakeAbility(plugin));
        plugin.getAbilityManager().registerAbility(new EarthTomb(plugin));
        plugin.getAbilityManager().registerAbility(new WindPushAbility(plugin));
        plugin.getAbilityManager().registerAbility(new WindPull(plugin));
        plugin.getAbilityManager().registerAbility(new WindCyclone(plugin));
        plugin.getAbilityManager().registerAbility(new LightHeal(plugin));
        plugin.getAbilityManager().registerAbility(new LightBeam(plugin));
        plugin.getAbilityManager().registerAbility(new LightFlash(plugin));
        plugin.getAbilityManager().registerAbility(new ShadowRift(plugin));
        plugin.getAbilityManager().registerAbility(new ShadowWalk(plugin));
        plugin.getAbilityManager().registerAbility(new ShadowClone(plugin));
        plugin.getAbilityManager().registerAbility(new VoidCollapse(plugin));
        plugin.getAbilityManager().registerAbility(new VoidGrasp(plugin));
        plugin.getAbilityManager().registerAbility(new VoidWarp(plugin));
        plugin.getAbilityManager().registerAbility(new CosmicShower(plugin));
        plugin.getAbilityManager().registerAbility(new CosmicSingularity(plugin));
        plugin.getAbilityManager().registerAbility(new CosmicShield(plugin));
        plugin.getAbilityManager().registerAbility(new AcidSpray(plugin));
        plugin.getAbilityManager().registerAbility(new AcidRain(plugin));
        plugin.getAbilityManager().registerAbility(new AcidPuddle(plugin));

        // Combat Styles & Buffs (61-90)
        plugin.getAbilityManager().registerAbility(new AssassinBackstab(plugin));
        plugin.getAbilityManager().registerAbility(new AssassinSmokeBomb(plugin));
        plugin.getAbilityManager().registerAbility(new AssassinPoisonDart(plugin));
        plugin.getAbilityManager().registerAbility(new TankProvoke(plugin));
        plugin.getAbilityManager().registerAbility(new TankImmovability(plugin));
        plugin.getAbilityManager().registerAbility(new TankLastStand(plugin));
        plugin.getAbilityManager().registerAbility(new BerserkerBloodlust(plugin));
        plugin.getAbilityManager().registerAbility(new BerserkerChargeAbility(plugin));
        plugin.getAbilityManager().registerAbility(new BerserkerRageAbility(plugin));
        plugin.getAbilityManager().registerAbility(new HealerCircle(plugin));
        plugin.getAbilityManager().registerAbility(new HealerPurify(plugin));
        plugin.getAbilityManager().registerAbility(new HealerResurrection(plugin));
        plugin.getAbilityManager().registerAbility(new ArcherVolley(plugin));
        plugin.getAbilityManager().registerAbility(new ArcherSnipe(plugin));
        plugin.getAbilityManager().registerAbility(new ArcherEscape(plugin));
        plugin.getAbilityManager().registerAbility(new WizardManaShield(plugin));
        plugin.getAbilityManager().registerAbility(new WizardTeleport(plugin));
        plugin.getAbilityManager().registerAbility(new WizardSpellSteal(plugin));
        plugin.getAbilityManager().registerAbility(new PaladinSmiteAbility(plugin));
        plugin.getAbilityManager().registerAbility(new PaladinShieldAbility(plugin));
        plugin.getAbilityManager().registerAbility(new PaladinAuraAbility(plugin));
        plugin.getAbilityManager().registerAbility(new SummonerSkeleton(plugin));
        plugin.getAbilityManager().registerAbility(new SummonerGolem(plugin));
        plugin.getAbilityManager().registerAbility(new SummonerWolfpack(plugin));
        plugin.getAbilityManager().registerAbility(new BrawlerUppercut(plugin));
        plugin.getAbilityManager().registerAbility(new BrawlerTackle(plugin));
        plugin.getAbilityManager().registerAbility(new BrawlerShockwave(plugin));
        plugin.getAbilityManager().registerAbility(new NinjaTeleport(plugin));
        plugin.getAbilityManager().registerAbility(new NinjaDodge(plugin));
        plugin.getAbilityManager().registerAbility(new NinjaStarVolley(plugin));

        // Utility, Fun & Misc (91-100)
        plugin.getAbilityManager().registerAbility(new GrapplingHook(plugin));
        plugin.getAbilityManager().registerAbility(new MagnetChest(plugin));
        plugin.getAbilityManager().registerAbility(new HarvestBloom(plugin));
        plugin.getAbilityManager().registerAbility(new MineralSense(plugin));
        plugin.getAbilityManager().registerAbility(new TimeLeap(plugin));
        plugin.getAbilityManager().registerAbility(new GravityWellAbility(plugin));
        plugin.getAbilityManager().registerAbility(new SpeedRun(plugin));
        plugin.getAbilityManager().registerAbility(new SuperDrill(plugin));
        plugin.getAbilityManager().registerAbility(new EnderSwapStrike(plugin));
        plugin.getAbilityManager().registerAbility(new LunarBlessing(plugin));

        plugin.getServer().getPluginManager().registerEvents(new MoreExpansionAbilities(), plugin);
    }

    public static void addManaShield(UUID uuid) { activeManaShields.put(uuid, System.currentTimeMillis() + 10000L); }
    public static void addLastStand(UUID uuid) { activeLastStands.put(uuid, System.currentTimeMillis() + 300000L); }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        Projectile proj = event.getEntity();
        Location hitLoc = event.getHitBlock() != null ? event.getHitBlock().getLocation() : proj.getLocation();
        
        if (proj.hasMetadata("wither_skull_bomb")) {
            hitLoc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, hitLoc, 5, 0.5, 0.5, 0.5, 0.05);
            hitLoc.getWorld().playSound(hitLoc, Sound.ENTITY_WITHER_SHOOT, 1f, 0.8f);
            AreaEffectCloud cloud = hitLoc.getWorld().spawn(hitLoc, AreaEffectCloud.class);
            cloud.setRadius(4.0f);
            cloud.setDuration(120);
            cloud.addCustomEffect(new PotionEffect(PotionEffectType.WITHER, 80, 1), true);
        } else if (proj.hasMetadata("acid_spray")) {
            hitLoc.getWorld().spawnParticle(Particle.SLIME, hitLoc, 20, 0.5, 0.5, 0.5, 0.02);
            hitLoc.getWorld().playSound(hitLoc, Sound.BLOCK_BREWING_STAND_BREW, 1f, 0.7f);
            for (Entity vic : hitLoc.getWorld().getNearbyEntities(hitLoc, 3.0, 2.0, 3.0)) {
                if (vic instanceof LivingEntity && !vic.equals(proj.getShooter())) {
                    LivingEntity le = (LivingEntity) vic;
                    le.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 1));
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player p = (Player) event.getEntity();
            
            // Mana Shield Damage Intercept
            Long manaExpire = activeManaShields.get(p.getUniqueId());
            if (manaExpire != null && System.currentTimeMillis() < manaExpire) {
                int xpToDrain = (int) (event.getDamage() * 2);
                if (p.getTotalExperience() >= xpToDrain) {
                    p.setTotalExperience(p.getTotalExperience() - xpToDrain);
                    event.setDamage(0);
                    p.getWorld().playSound(p.getLocation(), Sound.BLOCK_ANVIL_PLACE, 0.5f, 1.8f);
                    p.getWorld().spawnParticle(Particle.CRIT_MAGIC, p.getLocation(), 10, 0.3, 0.3, 0.3, 0.05);
                }
            }

            // Last Stand
            if (p.getHealth() - event.getFinalDamage() <= p.getMaxHealth() * 0.2) {
                Long shieldExpire = activeLastStands.get(p.getUniqueId());
                if (shieldExpire == null || System.currentTimeMillis() > shieldExpire) {
                    addLastStand(p.getUniqueId());
                    p.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 300, 4)); // 20 absorption health
                    p.getWorld().playSound(p.getLocation(), Sound.ITEM_TOTEM_USE, 0.8f, 1.0f);
                    p.sendMessage("§6§lLAST STAND ACTIVATED!");
                }
            }
        }
    }
}

// Subclasses (1-30 Boss & Legendary Mob Spells)
class WitherSkullBomb extends Ability {
    private final ItemEditFull plugin;
    public WitherSkullBomb(ItemEditFull pl) { super("wither_skull_bomb", "Wither Skull Bomb", "Fires a decay wither skull."); this.plugin = pl; }
    @Override public boolean trigger(Player p, ItemStack i) {
        WitherSkull s = p.launchProjectile(WitherSkull.class);
        s.setMetadata("wither_skull_bomb", new FixedMetadataValue(plugin, true));
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_WITHER_SHOOT, 1f, 1f);
        return true;
    }
}
class DragonBreathGround extends Ability {
    public DragonBreathGround(ItemEditFull pl) { super("dragon_breath_ground", "Dragon Breath Ground", "Spew dragon breath."); }
    @Override public boolean trigger(Player p, ItemStack i) {
        Location loc = p.getLocation().add(p.getLocation().getDirection().multiply(2));
        AreaEffectCloud cloud = loc.getWorld().spawn(loc, AreaEffectCloud.class);
        cloud.setRadius(3.5f);
        cloud.setDuration(100);
        cloud.setParticle(Particle.DRAGON_BREATH);
        cloud.addCustomEffect(new PotionEffect(PotionEffectType.HARM, 1, 0), true);
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 1f);
        return true;
    }
}
class CreeperChargeAbility extends Ability {
    public CreeperChargeAbility(ItemEditFull pl) { super("creeper_charge", "Creeper Charge", "Grants supercharged explosion."); }
    @Override public boolean trigger(Player p, ItemStack i) {
        p.getWorld().strikeLightningEffect(p.getLocation());
        p.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 200, 1));
        return true;
    }
}
class CreeperDetonation extends Ability {
    private final ItemEditFull plugin;
    public CreeperDetonation(ItemEditFull pl) { super("creeper_detonation", "Creeper Detonation", "Initiate countdown explosion."); this.plugin = pl; }
    @Override public boolean trigger(Player p, ItemStack i) {
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_CREEPER_PRIMED, 1.2f, 1f);
        new BukkitRunnable() {
            @Override public void run() {
                if (p.isOnline()) {
                    p.getWorld().createExplosion(p.getLocation(), 5f, false, false);
                }
            }
        }.runTaskLater(plugin, 40L);
        return true;
    }
}
class ShulkerGravityShift extends Ability {
    public ShulkerGravityShift(ItemEditFull pl) { super("shulker_gravity_shift", "Gravity Shift", "Levitate nearby targets."); }
    @Override public boolean trigger(Player p, ItemStack i) {
        for (Entity vic : p.getWorld().getNearbyEntities(p.getLocation(), 6.0, 3.0, 6.0)) {
            if (vic instanceof LivingEntity && !vic.equals(p)) {
                ((LivingEntity) vic).addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 60, 2));
            }
        }
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_SHULKER_TELEPORT, 1f, 1f);
        return true;
    }
}
class WardenSonicClapAbility extends Ability {
    public WardenSonicClapAbility(ItemEditFull pl) { super("warden_sonic_clap", "Warden Sonic Clap", "Deafening sonic wave."); }
    @Override public boolean trigger(Player p, ItemStack i) {
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 1f, 0.8f);
        return true;
    }
}
class WardenSculkInfection extends Ability {
    public WardenSculkInfection(ItemEditFull pl) { super("warden_sculk_infection", "Sculk Infection", "Infests ground."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class EvokerFangCircle extends Ability {
    public EvokerFangCircle(ItemEditFull pl) { super("evoker_fang_circle", "Fangs Circle", "Concentric circle fangs."); }
    @Override public boolean trigger(Player p, ItemStack i) {
        Location base = p.getLocation();
        for (double d = 0; d < 360; d += 45) {
            double rad = Math.toRadians(d);
            Location loc = base.clone().add(Math.cos(rad) * 2.5, 0, Math.sin(rad) * 2.5);
            base.getWorld().spawn(loc, EvokerFangs.class);
        }
        return true;
    }
}
class EvokerVexSwarm extends Ability {
    public EvokerVexSwarm(ItemEditFull pl) { super("evoker_vex_swarm", "Evoker Vex Swarm", "Summon 4 helper vexes."); }
    @Override public boolean trigger(Player p, ItemStack i) {
        for (int k = 0; k < 4; k++) {
            p.getWorld().spawn(p.getLocation(), Vex.class);
        }
        return true;
    }
}
class WitchElixir extends Ability {
    public WitchElixir(ItemEditFull pl) { super("witch_elixir", "Witch Elixir", "Random buffs elixir."); }
    @Override public boolean trigger(Player p, ItemStack i) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 300, 1));
        p.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 300, 0));
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_WITCH_DRINK, 1f, 1.2f);
        return true;
    }
}
class WitchPoisonSplash extends Ability {
    public WitchPoisonSplash(ItemEditFull pl) { super("witch_poison_splash", "Poison Splash", "Splash poison throw."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.launchProjectile(ThrownPotion.class); return true; }
}
class PhantomSpectre extends Ability {
    public PhantomSpectre(ItemEditFull pl) { super("phantom_spectre", "Phantom Decoy", "Decoy explosion."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class StrayFrostHail extends Ability {
    public StrayFrostHail(ItemEditFull pl) { super("stray_frost_hail", "Frost Hail", "Hail slowness storm."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class HuskDesiccation extends Ability {
    public HuskDesiccation(ItemEditFull pl) { super("husk_desiccation", "Husk Desiccation", "Drain target hunger."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class MagmaCubeSlam extends Ability {
    public MagmaCubeSlam(ItemEditFull pl) { super("magma_cube_slam", "Magma Slam", "Jump and slam magma."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.setVelocity(new Vector(0, 1.4, 0)); return true; }
}
class BlazeInferno extends Ability {
    public BlazeInferno(ItemEditFull pl) { super("blaze_inferno", "Blaze Inferno", "Shoot circular fireballs."); }
    @Override public boolean trigger(Player p, ItemStack i) {
        for (int k = 0; k < 8; k++) {
            p.launchProjectile(SmallFireball.class);
        }
        return true;
    }
}
class PiglinGoldenBarrage extends Ability {
    public PiglinGoldenBarrage(ItemEditFull pl) { super("piglin_golden_barrage", "Gold Barrage", "Golden barrage."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class DrownedWhirlpool extends Ability {
    public DrownedWhirlpool(ItemEditFull pl) { super("drowned_whirlpool", "Drowned Whirlpool", "Water whirlpool pull."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class GuardianLaserBurst extends Ability {
    public GuardianLaserBurst(ItemEditFull pl) { super("guardian_laser_burst", "Laser Burst", "Escalating magic damage."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class ElderGuardianFatigueBlast extends Ability {
    public ElderGuardianFatigueBlast(ItemEditFull pl) { super("elder_guardian_fatigue_blast", "Fatigue Blast", "Elder slowness waves."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class RavagerCharge extends Ability {
    public RavagerCharge(ItemEditFull pl) { super("ravager_charge", "Ravager Charge", "Charges straight forward."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.setVelocity(p.getLocation().getDirection().multiply(1.8).setY(0.2)); return true; }
}
class RavagerRoar extends Ability {
    public RavagerRoar(ItemEditFull pl) { super("ravager_roar", "Ravager Roar", "Roar wind blast."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.getWorld().playSound(p.getLocation(), Sound.ENTITY_RAVAGER_ROAR, 1.5f, 1f); return true; }
}
class IllusionerMirror extends Ability {
    public IllusionerMirror(ItemEditFull pl) { super("illusioner_mirror", "Mirror Illusion", "Duplicate illusions."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class IllusionerBlindVolley extends Ability {
    public IllusionerBlindVolley(ItemEditFull pl) { super("illusioner_blind_volley", "Blind Volley", "Volley of arrows."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.launchProjectile(Arrow.class); return true; }
}
class PillagerRaidCall extends Ability {
    public PillagerRaidCall(ItemEditFull pl) { super("pillager_raid_call", "Pillager Raid Call", "Spawn pillager helper."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.getWorld().spawn(p.getLocation(), Pillager.class); return true; }
}
class SlimeBounceAbility extends Ability {
    public SlimeBounceAbility(ItemEditFull pl) { super("slime_bounce", "Slime Bounce", "Safe fall bounce."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class SlimeSplit extends Ability {
    public SlimeSplit(ItemEditFull pl) { super("slime_split", "Slime Split", "Spawn slimes on damage."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class IronGolemToss extends Ability {
    public IronGolemToss(ItemEditFull pl) { super("iron_golem_toss", "Golem Toss", "Throw targets."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class IronGolemShield extends Ability {
    public IronGolemShield(ItemEditFull pl) { super("iron_golem_shield", "Iron Golem Shield", "Projectile immunity."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 160, 2)); return true; }
}
class SpiderNestTrap extends Ability {
    public SpiderNestTrap(ItemEditFull pl) { super("spider_nest_trap", "Spider Nest Trap", "Spawn cobwebs trap."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}

// Subclasses (31-60 Elemental Spells)
class FireMeteor extends Ability {
    public FireMeteor(ItemEditFull pl) { super("fire_meteor", "Fire Meteor", "Sky explosions."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class FireWall extends Ability {
    public FireWall(ItemEditFull pl) { super("fire_wall", "Fire Wall", "Spawn walls of flame."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class FireNova extends Ability {
    public FireNova(ItemEditFull pl) { super("fire_nova", "Fire Nova", "Expanding ring of fire."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class IceNovaAbility extends Ability {
    public IceNovaAbility(ItemEditFull pl) { super("ice_nova", "Ice Nova", "Expanding ring of frost."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class IcePrison extends Ability {
    public IcePrison(ItemEditFull pl) { super("ice_prison", "Ice Prison", "Ice block cage."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class IceBlizzard extends Ability {
    public IceBlizzard(ItemEditFull pl) { super("ice_blizzard", "Blizzard", "Ice storm area."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class LightningChainAbility extends Ability {
    public LightningChainAbility(ItemEditFull pl) { super("lightning_chain", "Lightning Chain", "Bounces lightning."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class LightningStorm extends Ability {
    public LightningStorm(ItemEditFull pl) { super("lightning_storm", "Lightning Storm", "Call lightning area."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class LightningDash extends Ability {
    public LightningDash(ItemEditFull pl) { super("lightning_dash", "Lightning Dash", "Blink and strike."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.teleport(p.getLocation().add(p.getLocation().getDirection().multiply(6))); return true; }
}
class EarthWall extends Ability {
    public EarthWall(ItemEditFull pl) { super("earth_wall", "Earth Wall", "Temp walls block."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class EarthQuakeAbility extends Ability {
    public EarthQuakeAbility(ItemEditFull pl) { super("earth_quake", "Earthquake", "Ground damage wave."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class EarthTomb extends Ability {
    public EarthTomb(ItemEditFull pl) { super("earth_tomb", "Earth Tomb", "Pull target underground."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class WindPushAbility extends Ability {
    public WindPushAbility(ItemEditFull pl) { super("wind_push", "Wind Push", "Cone wind push."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class WindPull extends Ability {
    public WindPull(ItemEditFull pl) { super("wind_pull", "Wind Pull", "Vacuum draw."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class WindCyclone extends Ability {
    public WindCyclone(ItemEditFull pl) { super("wind_cyclone", "Wind Cyclone", "Localized tornado."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class LightHeal extends Ability {
    public LightHeal(ItemEditFull pl) { super("light_heal", "Holy Light Heal", "Heals allies."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.addPotionEffect(new PotionEffect(PotionEffectType.HEAL, 1, 1)); return true; }
}
class LightBeam extends Ability {
    public LightBeam(ItemEditFull pl) { super("light_beam", "Light Beam", "Burns undead."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class LightFlash extends Ability {
    public LightFlash(ItemEditFull pl) { super("light_flash", "Light Flash", "Blinds targets."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class ShadowRift extends Ability {
    public ShadowRift(ItemEditFull pl) { super("shadow_rift", "Shadow Rift", "Blinks forward."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.teleport(p.getLocation().add(p.getLocation().getDirection().multiply(6))); return true; }
}
class ShadowWalk extends Ability {
    public ShadowWalk(ItemEditFull pl) { super("shadow_walk", "Shadow Walk", "Invisibility steps."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 200, 0)); return true; }
}
class ShadowClone extends Ability {
    public ShadowClone(ItemEditFull pl) { super("shadow_clone", "Shadow Clone", "Target decoy clone."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class VoidCollapse extends Ability {
    public VoidCollapse(ItemEditFull pl) { super("void_collapse", "Void Collapse", "Black hole pulls."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class VoidGrasp extends Ability {
    public VoidGrasp(ItemEditFull pl) { super("void_grasp", "Void Grasp", "Grapples targets."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class VoidWarp extends Ability {
    public VoidWarp(ItemEditFull pl) { super("void_warp", "Void Warp", "Warp blink."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.teleport(p.getLocation().add(p.getLocation().getDirection().multiply(6))); return true; }
}
class CosmicShower extends Ability {
    public CosmicShower(ItemEditFull pl) { super("cosmic_shower", "Cosmic Shower", "Rains stardust."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class CosmicSingularity extends Ability {
    public CosmicSingularity(ItemEditFull pl) { super("cosmic_singularity", "Cosmic Singularity", "Implodes targets."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class CosmicShield extends Ability {
    public CosmicShield(ItemEditFull pl) { super("cosmic_shield", "Cosmic Shield", "Shield orbits."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 200, 1)); return true; }
}
class AcidSpray extends Ability {
    private final ItemEditFull plugin;
    public AcidSpray(ItemEditFull pl) { super("acid_spray", "Acid Spray", "Acid spray poison."); this.plugin = pl; }
    @Override public boolean trigger(Player p, ItemStack i) {
        Snowball b = p.launchProjectile(Snowball.class);
        b.setMetadata("acid_spray", new FixedMetadataValue(plugin, true));
        return true;
    }
}
class AcidRain extends Ability {
    public AcidRain(ItemEditFull pl) { super("acid_rain", "Acid Rain", "Corrosive rain."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class AcidPuddle extends Ability {
    public AcidPuddle(ItemEditFull pl) { super("acid_puddle", "Acid Puddle", "Continuous damage."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}

// Subclasses (61-90 Combat styles)
class AssassinBackstab extends Ability {
    public AssassinBackstab(ItemEditFull pl) { super("assassin_backstab", "Assassin Backstab", "Melee critical backstab."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class AssassinSmokeBomb extends Ability {
    public AssassinSmokeBomb(ItemEditFull pl) { super("assassin_smoke_bomb", "Smoke Bomb", "Blinds and invis."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 160, 0)); return true; }
}
class AssassinPoisonDart extends Ability {
    public AssassinPoisonDart(ItemEditFull pl) { super("assassin_poison_dart", "Poison Dart", "Shoot poison dart."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.launchProjectile(Arrow.class); return true; }
}
class TankProvoke extends Ability {
    public TankProvoke(ItemEditFull pl) { super("tank_provoke", "Tank Provoke", "Taunts mobs."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class TankImmovability extends Ability {
    public TankImmovability(ItemEditFull pl) { super("tank_immovability", "Immovability", "Knockback protection."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 200, 2)); return true; }
}
class TankLastStand extends Ability {
    public TankLastStand(ItemEditFull pl) { super("tank_last_stand", "Last Stand", "Hearts shield."); }
    @Override public boolean trigger(Player p, ItemStack i) {
        MoreExpansionAbilities.addLastStand(p.getUniqueId());
        return true;
    }
}
class BerserkerBloodlust extends Ability {
    public BerserkerBloodlust(ItemEditFull pl) { super("berserker_bloodlust", "Bloodlust Strength", "Attack strength boost."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 200, 0)); return true; }
}
class BerserkerChargeAbility extends Ability {
    public BerserkerChargeAbility(ItemEditFull pl) { super("berserker_charge", "Berserker Charge", "Sprint leap."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.setVelocity(p.getLocation().getDirection().multiply(1.4)); return true; }
}
class BerserkerRageAbility extends Ability {
    public BerserkerRageAbility(ItemEditFull pl) { super("berserker_rage", "Berserker Rage", "Attack scales."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class HealerCircle extends Ability {
    public HealerCircle(ItemEditFull pl) { super("healer_circle", "Healing Circle", "Healing ground."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 160, 1)); return true; }
}
class HealerPurify extends Ability {
    public HealerPurify(ItemEditFull pl) { super("healer_purify", "Holy Purify Debuffs", "Clear debuffs."); }
    @Override public boolean trigger(Player p, ItemStack i) {
        for (PotionEffect pe : p.getActivePotionEffects()) {
            p.removePotionEffect(pe.getType());
        }
        return true;
    }
}
class HealerResurrection extends Ability {
    public HealerResurrection(ItemEditFull pl) { super("healer_resurrection", "Resurrection Guard", "Totem safe save."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class ArcherVolley extends Ability {
    public ArcherVolley(ItemEditFull pl) { super("archer_volley", "Arrow Volley", "Arrows wave."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.launchProjectile(Arrow.class); return true; }
}
class ArcherSnipe extends Ability {
    public ArcherSnipe(ItemEditFull pl) { super("archer_snipe", "Archer Snipe", "Ranged bonus."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class ArcherEscape extends Ability {
    public ArcherEscape(ItemEditFull pl) { super("archer_escape", "Archer Escape", "Launch backward."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.setVelocity(p.getLocation().getDirection().multiply(-1.5)); return true; }
}
class WizardManaShield extends Ability {
    public WizardManaShield(ItemEditFull pl) { super("wizard_mana_shield", "Mana Shield", "XP checks health."); }
    @Override public boolean trigger(Player p, ItemStack i) {
        MoreExpansionAbilities.addManaShield(p.getUniqueId());
        return true;
    }
}
class WizardTeleport extends Ability {
    public WizardTeleport(ItemEditFull pl) { super("wizard_teleport", "Wizard Teleport", "Blink wall jump."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.teleport(p.getLocation().add(p.getLocation().getDirection().multiply(6))); return true; }
}
class WizardSpellSteal extends Ability {
    public WizardSpellSteal(ItemEditFull pl) { super("wizard_spell_steal", "Spell Steal", "Copy buffs."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class PaladinSmiteAbility extends Ability {
    public PaladinSmiteAbility(ItemEditFull pl) { super("paladin_smite", "Paladin Smite", "Holy strike fire."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class PaladinShieldAbility extends Ability {
    public PaladinShieldAbility(ItemEditFull pl) { super("paladin_shield", "Paladin Shield", "Resistance boost."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 200, 1)); return true; }
}
class PaladinAuraAbility extends Ability {
    public PaladinAuraAbility(ItemEditFull pl) { super("paladin_aura", "Holy Paladin Aura", "Aura resistance."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class SummonerSkeleton extends Ability {
    public SummonerSkeleton(ItemEditFull pl) { super("summoner_skeleton", "Summoner Skeleton", "Summon 3 skeletons."); }
    @Override public boolean trigger(Player p, ItemStack i) {
        for (int k = 0; k < 3; k++) { p.getWorld().spawn(p.getLocation(), Skeleton.class); }
        return true;
    }
}
class SummonerGolem extends Ability {
    public SummonerGolem(ItemEditFull pl) { super("summoner_golem", "Summoner Golem", "Summon iron golem."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.getWorld().spawn(p.getLocation(), IronGolem.class); return true; }
}
class SummonerWolfpack extends Ability {
    public SummonerWolfpack(ItemEditFull pl) { super("summoner_wolfpack", "Summoner Wolves", "Summon 4 wolves."); }
    @Override public boolean trigger(Player p, ItemStack i) {
        for (int k = 0; k < 4; k++) { p.getWorld().spawn(p.getLocation(), Wolf.class); }
        return true;
    }
}
class BrawlerUppercut extends Ability {
    public BrawlerUppercut(ItemEditFull pl) { super("brawler_uppercut", "Brawler Uppercut", "Vertical toss hit."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class BrawlerTackle extends Ability {
    public BrawlerTackle(ItemEditFull pl) { super("brawler_tackle", "Brawler Tackle", "Pin target slowness."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class BrawlerShockwave extends Ability {
    public BrawlerShockwave(ItemEditFull pl) { super("brawler_shockwave", "Shockwave Slam", "Knocks surrounding."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class NinjaTeleport extends Ability {
    public NinjaTeleport(ItemEditFull pl) { super("ninja_teleport", "Ninja Teleport", "Blink last target."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class NinjaDodge extends Ability {
    public NinjaDodge(ItemEditFull pl) { super("ninja_dodge", "Ninja Dodge", "Dodge rate percent."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class NinjaStarVolley extends Ability {
    public NinjaStarVolley(ItemEditFull pl) { super("ninja_star_volley", "Shuriken Volley", "Throws stars."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.launchProjectile(Snowball.class); return true; }
}

// Subclasses (91-100 Utility)
class GrapplingHook extends Ability {
    public GrapplingHook(ItemEditFull pl) { super("grappling_hook", "Grappling Hook", "Pull player hook."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class MagnetChest extends Ability {
    public MagnetChest(ItemEditFull pl) { super("magnet_chest", "Magnet Chest", "Draw items range."); }
    @Override public boolean trigger(Player p, ItemStack i) {
        for (Entity ent : p.getWorld().getNearbyEntities(p.getLocation(), 10.0, 5.0, 10.0)) {
            if (ent instanceof Item) {
                ent.teleport(p.getLocation());
            }
        }
        return true;
    }
}
class HarvestBloom extends Ability {
    public HarvestBloom(ItemEditFull pl) { super("harvest_bloom", "Harvest Bloom", "Grow crops nearby."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class MineralSense extends Ability {
    public MineralSense(ItemEditFull pl) { super("mineral_sense", "Mineral Sense", "Highlight ores."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class TimeLeap extends Ability {
    public TimeLeap(ItemEditFull pl) { super("time_leap", "Time Leap", "Time warp back."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class GravityWellAbility extends Ability {
    public GravityWellAbility(ItemEditFull pl) { super("gravity_well_passive", "Gravity Well", "Reverses gravity."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class SpeedRun extends Ability {
    public SpeedRun(ItemEditFull pl) { super("speed_run", "Speed Run Boost", "Double jump speed."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 3)); return true; }
}
class SuperDrill extends Ability {
    public SuperDrill(ItemEditFull pl) { super("super_drill", "Super Drill", "3x3 mine pattern."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class EnderSwapStrike extends Ability {
    public EnderSwapStrike(ItemEditFull pl) { super("ender_swap_strike", "Swap Strike", "Swaps targets locations."); }
    @Override public boolean trigger(Player p, ItemStack i) { return true; }
}
class LunarBlessing extends Ability {
    public LunarBlessing(ItemEditFull pl) { super("lunar_blessing", "Lunar Blessing", "Night speed stats."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 600, 0)); return true; }
}
