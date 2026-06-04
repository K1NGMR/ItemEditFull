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
    public static ItemEditFull getPlugin() { return pluginInstance; }
    private static final Map<UUID, Long> activeManaShields = new HashMap<>();
    private static final Map<UUID, Long> activeLastStands = new HashMap<>();
    private static final Map<UUID, Long> activeResurrections = new HashMap<>();
    private static final Map<UUID, Long> activeDodges = new HashMap<>();

    public static void addRes(UUID uuid, long expire) { activeResurrections.put(uuid, expire); }
    public static boolean hasRes(UUID uuid) { Long exp = activeResurrections.get(uuid); return exp != null && System.currentTimeMillis() < exp; }
    public static void consumeRes(UUID uuid) { activeResurrections.remove(uuid); }

    public static void addDodge(UUID uuid, long expire) { activeDodges.put(uuid, expire); }
    public static boolean hasDodge(UUID uuid) { Long exp = activeDodges.get(uuid); return exp != null && System.currentTimeMillis() < exp; }

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
        } else if (proj.hasMetadata("golden_barrage")) {
            hitLoc.getWorld().spawnParticle(Particle.CRIT_MAGIC, hitLoc, 15, 0.2, 0.2, 0.2, 0.1);
            hitLoc.getWorld().playSound(hitLoc, Sound.BLOCK_METAL_BREAK, 1.0f, 1.2f);
            if (event.getHitEntity() instanceof LivingEntity) {
                LivingEntity le = (LivingEntity) event.getHitEntity();
                if (!le.equals(proj.getShooter())) {
                    le.damage(4.0, (Entity) proj.getShooter());
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player p = (Player) event.getEntity();
            
            // Ninja Dodge
            if (MoreExpansionAbilities.hasDodge(p.getUniqueId())) {
                if (Math.random() < 0.35) {
                    event.setCancelled(true);
                    p.getWorld().playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.8f, 1.8f);
                    p.getWorld().spawnParticle(Particle.SMOKE_NORMAL, p.getLocation(), 10, 0.2, 0.2, 0.2, 0.05);
                    p.sendMessage("§7§oDODGED!");
                    return;
                }
            }

            // Resurrection Guard
            if (p.getHealth() - event.getFinalDamage() <= 0) {
                if (MoreExpansionAbilities.hasRes(p.getUniqueId())) {
                    MoreExpansionAbilities.consumeRes(p.getUniqueId());
                    event.setCancelled(true);
                    p.setHealth(p.getMaxHealth() * 0.5);
                    p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 2));
                    p.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 200, 0));
                    p.getWorld().playSound(p.getLocation(), Sound.ITEM_TOTEM_USE, 1.0f, 1.0f);
                    p.getWorld().spawnParticle(Particle.TOTEM, p.getLocation(), 100, 0.5, 1.0, 0.5, 0.35);
                    p.sendMessage("§e§lRESURRECTED!");
                    return;
                }
            }

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
                    p.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 300, 4));
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
    private final ItemEditFull plugin;
    public WardenSculkInfection(ItemEditFull pl) { super("warden_sculk_infection", "Sculk Infection", "Infests ground."); this.plugin = pl; }
    @Override public boolean trigger(Player p, ItemStack i) {
        Location target = p.getTargetBlock(null, 15).getLocation();
        p.getWorld().playSound(target, Sound.BLOCK_SCULK_SHRIEKER_SHRIEK, 1.2f, 1.0f);
        p.getWorld().spawnParticle(Particle.SPELL_WITCH, target, 40, 3.0, 0.5, 3.0, 0.05);
        for (Entity ent : target.getWorld().getNearbyEntities(target, 3.5, 2.0, 3.5)) {
            if (ent instanceof LivingEntity && !ent.equals(p)) {
                LivingEntity le = (LivingEntity) ent;
                le.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 2));
                le.damage(4.0, p);
            }
        }
        return true;
    }
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
    private final ItemEditFull plugin;
    public PhantomSpectre(ItemEditFull pl) { super("phantom_spectre", "Phantom Decoy", "Decoy explosion."); this.plugin = pl; }
    @Override public boolean trigger(Player p, ItemStack i) {
        Location loc = p.getLocation();
        p.getWorld().playSound(loc, Sound.ENTITY_PHANTOM_AMBIENT, 1.0f, 1.2f);
        ArmorStand stand = p.getWorld().spawn(loc, ArmorStand.class);
        stand.setVisible(false);
        stand.setSmall(true);
        stand.setCustomName("§7Phantom Spectre");
        stand.setCustomNameVisible(true);
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks++ > 30) {
                    cancel();
                    Location standLoc = stand.getLocation();
                    standLoc.getWorld().createExplosion(standLoc, 3.0f, false, false);
                    standLoc.getWorld().spawnParticle(Particle.SQUID_INK, standLoc, 30, 1.5, 1.5, 1.5, 0.1);
                    stand.remove();
                    return;
                }
                stand.getWorld().spawnParticle(Particle.PORTAL, stand.getLocation().add(0, 0.5, 0), 5, 0.2, 0.2, 0.2, 0.05);
            }
        }.runTaskTimer(plugin, 0L, 2L);
        return true;
    }
}
class StrayFrostHail extends Ability {
    private final ItemEditFull plugin;
    public StrayFrostHail(ItemEditFull pl) { super("stray_frost_hail", "Frost Hail", "Hail slowness storm."); this.plugin = pl; }
    @Override public boolean trigger(Player p, ItemStack i) {
        Location target = p.getTargetBlock(null, 15).getLocation();
        p.getWorld().playSound(target, Sound.BLOCK_SNOW_BREAK, 1.2f, 1.0f);
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks++ > 40) {
                    cancel();
                    return;
                }
                target.getWorld().spawnParticle(Particle.SNOWBALL, target.clone().add(0, 3, 0), 10, 3.0, 0.1, 3.0, 0.1);
                target.getWorld().spawnParticle(Particle.SNOW_SHOVEL, target, 5, 3.0, 0.5, 3.0, 0.01);
                if (ticks % 10 == 0) {
                    for (Entity ent : target.getWorld().getNearbyEntities(target, 4.0, 3.0, 4.0)) {
                        if (ent instanceof LivingEntity && !ent.equals(p)) {
                            LivingEntity le = (LivingEntity) ent;
                            le.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, 2));
                            le.damage(1.5, p);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
        return true;
    }
}
class HuskDesiccation extends Ability {
    public HuskDesiccation(ItemEditFull pl) { super("husk_desiccation", "Husk Desiccation", "Drain target hunger."); }
    @Override public boolean trigger(Player p, ItemStack i) {
        Location loc = p.getLocation();
        p.getWorld().playSound(loc, Sound.ENTITY_HUSK_CONVERTED_TO_ZOMBIE, 1.2f, 0.8f);
        p.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, loc, 15, 3.0, 1.0, 3.0, 0.1);
        int targetsDrained = 0;
        for (Entity ent : p.getWorld().getNearbyEntities(loc, 5.0, 2.5, 5.0)) {
            if (ent instanceof Player && !ent.equals(p)) {
                Player targetPlayer = (Player) ent;
                targetPlayer.setFoodLevel(Math.max(0, targetPlayer.getFoodLevel() - 6));
                targetPlayer.damage(3.0, p);
                targetsDrained++;
            } else if (ent instanceof LivingEntity && !ent.equals(p)) {
                ((LivingEntity) ent).damage(3.0, p);
                targetsDrained++;
            }
        }
        if (targetsDrained > 0) {
            p.setFoodLevel(Math.min(20, p.getFoodLevel() + (targetsDrained * 3)));
            p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60, 1));
        }
        return true;
    }
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
    private final ItemEditFull plugin;
    public PiglinGoldenBarrage(ItemEditFull pl) { super("piglin_golden_barrage", "Gold Barrage", "Golden barrage."); this.plugin = pl; }
    @Override public boolean trigger(Player p, ItemStack i) {
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.5f, 1.5f);
        new BukkitRunnable() {
            int shots = 0;
            @Override
            public void run() {
                if (shots++ > 8 || !p.isOnline()) {
                    cancel();
                    return;
                }
                p.getWorld().playSound(p.getLocation(), Sound.ENTITY_SNOWBALL_THROW, 0.8f, 1.5f);
                Snowball nugget = p.launchProjectile(Snowball.class);
                nugget.setMetadata("golden_barrage", new FixedMetadataValue(plugin, true));
            }
        }.runTaskTimer(plugin, 0L, 2L);
        return true;
    }
}
class DrownedWhirlpool extends Ability {
    private final ItemEditFull plugin;
    public DrownedWhirlpool(ItemEditFull pl) { super("drowned_whirlpool", "Drowned Whirlpool", "Water whirlpool pull."); this.plugin = pl; }
    @Override public boolean trigger(Player p, ItemStack i) {
        Location target = p.getTargetBlock(null, 15).getLocation();
        p.getWorld().playSound(target, Sound.ENTITY_PLAYER_SPLASH, 1.2f, 0.7f);
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks++ > 60) {
                    cancel();
                    return;
                }
                target.getWorld().spawnParticle(Particle.WATER_SPLASH, target, 15, 3.0, 0.2, 3.0, 0.05);
                for (Entity ent : target.getWorld().getNearbyEntities(target, 4.0, 2.0, 4.0)) {
                    if (ent instanceof LivingEntity && !ent.equals(p)) {
                        Vector pullDir = target.toVector().subtract(ent.getLocation().toVector()).normalize().multiply(0.25).setY(0.05);
                        ent.setVelocity(pullDir);
                        if (ticks % 10 == 0) {
                            ((LivingEntity) ent).damage(1.0, p);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
        return true;
    }
}
class GuardianLaserBurst extends Ability {
    private final ItemEditFull plugin;
    public GuardianLaserBurst(ItemEditFull pl) { super("guardian_laser_burst", "Laser Burst", "Escalating magic damage."); this.plugin = pl; }
    @Override public boolean trigger(Player p, ItemStack i) {
        LivingEntity target = null;
        for (Entity ent : p.getWorld().getNearbyEntities(p.getLocation(), 15.0, 5.0, 15.0)) {
            if (ent instanceof LivingEntity && !ent.equals(p)) {
                target = (LivingEntity) ent;
                break;
            }
        }
        if (target == null) return false;
        final LivingEntity finalTarget = target;
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_GUARDIAN_ATTACK, 1.0f, 1.2f);
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks++ > 30 || !p.isOnline() || !finalTarget.isValid()) {
                    cancel();
                    return;
                }
                Location pEye = p.getEyeLocation().subtract(0, 0.3, 0);
                Location tLoc = finalTarget.getLocation().add(0, 1.0, 0);
                Vector direction = tLoc.toVector().subtract(pEye.toVector()).normalize();
                double distance = pEye.distance(tLoc);
                for (double d = 0; d < distance; d += 0.5) {
                    Location point = pEye.clone().add(direction.clone().multiply(d));
                    point.getWorld().spawnParticle(Particle.REDSTONE, point, 1, 0, 0, 0, 0, new Particle.DustOptions(Color.fromRGB(0, 255, 255), 1.0f));
                }
                if (ticks % 10 == 0) {
                    finalTarget.damage(2.0 + (ticks / 10.0), p);
                    finalTarget.getWorld().playSound(finalTarget.getLocation(), Sound.ENTITY_GUARDIAN_HURT, 0.8f, 1.5f);
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
        return true;
    }
}
class ElderGuardianFatigueBlast extends Ability {
    public ElderGuardianFatigueBlast(ItemEditFull pl) { super("elder_guardian_fatigue_blast", "Fatigue Blast", "Elder slowness waves."); }
    @Override public boolean trigger(Player p, ItemStack i) {
        Location loc = p.getLocation();
        p.getWorld().playSound(loc, Sound.ENTITY_ELDER_GUARDIAN_AMBIENT, 1.5f, 1.0f);
        p.getWorld().spawnParticle(Particle.SPELL_MOB, loc, 30, 5.0, 1.5, 5.0, 0.05);
        for (Entity ent : p.getWorld().getNearbyEntities(loc, 8.0, 3.0, 8.0)) {
            if (ent instanceof LivingEntity && !ent.equals(p)) {
                LivingEntity le = (LivingEntity) ent;
                le.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 160, 2));
                le.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 160, 2));
                le.damage(3.0, p);
            }
        }
        return true;
    }
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
    private final ItemEditFull plugin;
    public IllusionerMirror(ItemEditFull pl) { super("illusioner_mirror", "Mirror Illusion", "Duplicate illusions."); this.plugin = pl; }
    @Override public boolean trigger(Player p, ItemStack i) {
        Location loc = p.getLocation();
        p.getWorld().playSound(loc, Sound.ENTITY_ILLUSIONER_MIRROR_MOVE, 1.0f, 1.2f);
        List<ArmorStand> stands = new ArrayList<>();
        for (int k = 0; k < 3; k++) {
            ArmorStand stand = p.getWorld().spawn(loc.clone().add((Math.random() - 0.5) * 4, 0, (Math.random() - 0.5) * 4), ArmorStand.class);
            stand.setCustomName("§f" + p.getName());
            stand.setCustomNameVisible(true);
            stand.getEquipment().setHelmet(new ItemStack(Material.PLAYER_HEAD));
            stand.getEquipment().setChestplate(p.getEquipment().getChestplate());
            stand.getEquipment().setLeggings(p.getEquipment().getLeggings());
            stand.getEquipment().setBoots(p.getEquipment().getBoots());
            stands.add(stand);
        }
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks++ > 40 || !p.isOnline()) {
                    cancel();
                    for (ArmorStand stand : stands) stand.remove();
                    return;
                }
                for (ArmorStand stand : stands) {
                    stand.getWorld().spawnParticle(Particle.SPELL_INSTANT, stand.getLocation().add(0, 1.0, 0), 2, 0.2, 0.5, 0.2, 0.01);
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
        return true;
    }
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
    private final ItemEditFull plugin;
    public SlimeBounceAbility(ItemEditFull pl) { super("slime_bounce", "Slime Bounce", "Safe fall bounce."); this.plugin = pl; }
    @Override public boolean trigger(Player p, ItemStack i) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 160, 3));
        p.getWorld().playSound(p.getLocation(), Sound.BLOCK_SLIME_BLOCK_STEP, 1.2f, 1.0f);
        p.getWorld().spawnParticle(Particle.SLIME, p.getLocation(), 20, 0.5, 0.2, 0.5, 0.1);
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks++ > 80 || !p.isOnline()) {
                    cancel();
                    return;
                }
                if (p.getFallDistance() > 2.0 && p.isOnGround()) {
                    cancel();
                    p.setVelocity(new Vector(0, 1.2, 0));
                    p.setFallDistance(0);
                    p.getWorld().playSound(p.getLocation(), Sound.BLOCK_SLIME_BLOCK_FALL, 1.5f, 1.0f);
                    p.getWorld().spawnParticle(Particle.SLIME, p.getLocation(), 35, 1.0, 0.2, 1.0, 0.15);
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
        return true;
    }
}
class SlimeSplit extends Ability {
    public SlimeSplit(ItemEditFull pl) { super("slime_split", "Slime Split", "Spawn slimes on damage."); }
    @Override public boolean trigger(Player p, ItemStack i) {
        Location loc = p.getLocation();
        p.getWorld().playSound(loc, Sound.ENTITY_SLIME_DEATH, 1.0f, 1.2f);
        for (int k = 0; k < 2; k++) {
            Slime s = p.getWorld().spawn(loc.clone().add((Math.random() - 0.5) * 2, 0.1, (Math.random() - 0.5) * 2), Slime.class);
            s.setSize(1);
        }
        return true;
    }
}
class IronGolemToss extends Ability {
    public IronGolemToss(ItemEditFull pl) { super("iron_golem_toss", "Golem Toss", "Throw targets."); }
    @Override public boolean trigger(Player p, ItemStack i) {
        Location loc = p.getLocation();
        boolean hit = false;
        for (Entity ent : p.getWorld().getNearbyEntities(loc, 4.0, 2.0, 4.0)) {
            if (ent instanceof LivingEntity && !ent.equals(p)) {
                LivingEntity le = (LivingEntity) ent;
                le.setVelocity(new Vector(0, 1.2, 0));
                le.damage(4.0, p);
                hit = true;
            }
        }
        if (hit) {
            p.getWorld().playSound(p.getLocation(), Sound.ENTITY_IRON_GOLEM_ATTACK, 1.2f, 1.0f);
            p.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, p.getLocation(), 15, 0.5, 0.5, 0.5, 0.05);
        }
        return hit;
    }
}
class IronGolemShield extends Ability {
    public IronGolemShield(ItemEditFull pl) { super("iron_golem_shield", "Iron Golem Shield", "Projectile immunity."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 160, 2)); return true; }
}
class SpiderNestTrap extends Ability {
    private final ItemEditFull plugin;
    public SpiderNestTrap(ItemEditFull pl) { super("spider_nest_trap", "Spider Nest Trap", "Spawn cobwebs trap."); this.plugin = pl; }
    @Override public boolean trigger(Player p, ItemStack i) {
        Location target = p.getTargetBlock(null, 15).getLocation();
        p.getWorld().playSound(target, Sound.ENTITY_SPIDER_AMBIENT, 1.2f, 1.0f);
        final List<Block> cobwebs = new ArrayList<>();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                Block b = target.clone().add(x, 0, z).getBlock();
                if (b.getType() == Material.AIR) {
                    b.setType(Material.COBWEB);
                    cobwebs.add(b);
                }
            }
        }
        for (int k = 0; k < 2; k++) {
            p.getWorld().spawn(target.clone().add(0, 0.5, 0), CaveSpider.class);
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Block b : cobwebs) {
                    if (b.getType() == Material.COBWEB) b.setType(Material.AIR);
                }
            }
        }.runTaskLater(plugin, 100L);
        return true;
    }
}

// Subclasses (31-60 Elemental Spells)
class FireMeteor extends Ability {
    private final ItemEditFull plugin;
    public FireMeteor(ItemEditFull pl) { super("fire_meteor", "Fire Meteor", "Sky explosions."); this.plugin = pl; }
    @Override public boolean trigger(Player p, ItemStack i) {
        Location target = p.getTargetBlock(null, 15).getLocation();
        Location sky = target.clone().add(0, 10, 0);
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_GHAST_SHOOT, 1.2f, 1.0f);
        new BukkitRunnable() {
            int ticks = 0;
            Location current = sky.clone();
            Vector dir = target.toVector().subtract(sky.toVector()).normalize().multiply(0.5);
            @Override
            public void run() {
                if (ticks++ > 20 || current.distance(target) < 1.0) {
                    cancel();
                    target.getWorld().createExplosion(target, 4.0f, true, true);
                    target.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, target, 3, 0.5, 0.5, 0.5, 0.05);
                    return;
                }
                current.add(dir);
                current.getWorld().spawnParticle(Particle.FLAME, current, 10, 0.2, 0.2, 0.2, 0.05);
                current.getWorld().spawnParticle(Particle.SMOKE_NORMAL, current, 5, 0.1, 0.1, 0.1, 0.02);
            }
        }.runTaskTimer(plugin, 0L, 1L);
        return true;
    }
}
class FireWall extends Ability {
    public FireWall(ItemEditFull pl) { super("fire_wall", "Fire Wall", "Spawn walls of flame."); }
    @Override public boolean trigger(Player p, ItemStack i) {
        Location loc = p.getLocation();
        Vector dir = loc.getDirection().setY(0).normalize();
        Vector left = new Vector(-dir.getZ(), 0, dir.getX()).normalize();
        p.getWorld().playSound(loc, Sound.ITEM_FIRECHARGE_USE, 1.2f, 0.8f);
        for (int k = -2; k <= 2; k++) {
            Location step = loc.clone().add(dir.clone().multiply(2)).add(left.clone().multiply(k));
            for (int y = 0; y <= 1; y++) {
                Location flameLoc = step.clone().add(0, y, 0);
                flameLoc.getWorld().spawnParticle(Particle.FLAME, flameLoc, 15, 0.3, 0.5, 0.3, 0.02);
            }
            for (Entity ent : step.getWorld().getNearbyEntities(step, 1.5, 1.5, 1.5)) {
                if (ent instanceof LivingEntity && !ent.equals(p)) {
                    LivingEntity le = (LivingEntity) ent;
                    le.setFireTicks(100);
                    le.damage(3.0, p);
                }
            }
        }
        return true;
    }
}
class FireNova extends Ability {
    private final ItemEditFull plugin;
    public FireNova(ItemEditFull pl) { super("fire_nova", "Fire Nova", "Expanding ring of fire."); this.plugin = pl; }
    @Override public boolean trigger(Player p, ItemStack i) {
        Location base = p.getLocation().add(0, 0.5, 0);
        p.getWorld().playSound(base, Sound.ENTITY_ENDER_DRAGON_FLAP, 1.2f, 0.8f);
        new BukkitRunnable() {
            int step = 1;
            @Override
            public void run() {
                if (step > 5) {
                    cancel();
                    return;
                }
                p.getWorld().playSound(base, Sound.BLOCK_FIRE_AMBIENT, 0.8f, 1.2f);
                double radius = step * 1.5;
                for (double d = 0; d < 360; d += 15) {
                    double rad = Math.toRadians(d);
                    Location particleLoc = base.clone().add(Math.cos(rad) * radius, 0, Math.sin(rad) * radius);
                    particleLoc.getWorld().spawnParticle(Particle.FLAME, particleLoc, 2, 0.1, 0.1, 0.1, 0.01);
                }
                for (Entity ent : base.getWorld().getNearbyEntities(base, radius, 1.5, radius)) {
                    if (ent instanceof LivingEntity && !ent.equals(p)) {
                        LivingEntity le = (LivingEntity) ent;
                        if (le.getLocation().distance(base) >= radius - 1.0) {
                            le.setFireTicks(80);
                            le.damage(2.0, p);
                        }
                    }
                }
                step++;
            }
        }.runTaskTimer(plugin, 0L, 2L);
        return true;
    }
}
class IceNovaAbility extends Ability {
    private final ItemEditFull plugin;
    public IceNovaAbility(ItemEditFull pl) { super("ice_nova", "Ice Nova", "Expanding ring of frost."); this.plugin = pl; }
    @Override public boolean trigger(Player p, ItemStack i) {
        Location base = p.getLocation().add(0, 0.5, 0);
        p.getWorld().playSound(base, Sound.BLOCK_GLASS_BREAK, 1.2f, 0.8f);
        new BukkitRunnable() {
            int step = 1;
            @Override
            public void run() {
                if (step > 5) {
                    cancel();
                    return;
                }
                p.getWorld().playSound(base, Sound.BLOCK_SNOW_BREAK, 0.8f, 1.2f);
                double radius = step * 1.5;
                for (double d = 0; d < 360; d += 15) {
                    double rad = Math.toRadians(d);
                    Location particleLoc = base.clone().add(Math.cos(rad) * radius, 0, Math.sin(rad) * radius);
                    particleLoc.getWorld().spawnParticle(Particle.SNOW_SHOVEL, particleLoc, 2, 0.1, 0.1, 0.1, 0.01);
                    particleLoc.getWorld().spawnParticle(Particle.SNOWBALL, particleLoc, 1, 0.1, 0.1, 0.1, 0.01);
                }
                for (Entity ent : base.getWorld().getNearbyEntities(base, radius, 1.5, radius)) {
                    if (ent instanceof LivingEntity && !ent.equals(p)) {
                        LivingEntity le = (LivingEntity) ent;
                        if (le.getLocation().distance(base) >= radius - 1.0) {
                            le.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 80, 3));
                            le.damage(2.5, p);
                        }
                    }
                }
                step++;
            }
        }.runTaskTimer(plugin, 0L, 2L);
        return true;
    }
}
class IcePrison extends Ability {
    private final ItemEditFull plugin;
    public IcePrison(ItemEditFull pl) { super("ice_prison", "Ice Prison", "Ice block cage."); this.plugin = pl; }
    @Override public boolean trigger(Player p, ItemStack i) {
        LivingEntity target = null;
        for (Entity ent : p.getWorld().getNearbyEntities(p.getLocation(), 15.0, 5.0, 15.0)) {
            if (ent instanceof LivingEntity && !ent.equals(p)) {
                target = (LivingEntity) ent;
                break;
            }
        }
        if (target == null) return false;
        Location targetLoc = target.getLocation();
        p.getWorld().playSound(targetLoc, Sound.BLOCK_GLASS_BREAK, 1.5f, 0.8f);
        final List<Block> iceBlocks = new ArrayList<>();
        int[][] offset = {{1,0}, {-1,0}, {0,1}, {0,-1}};
        for (int[] off : offset) {
            for (int y = 0; y <= 1; y++) {
                Block b = targetLoc.clone().add(off[0], y, off[1]).getBlock();
                if (b.getType() == Material.AIR) {
                    b.setType(Material.PACKED_ICE);
                    iceBlocks.add(b);
                }
            }
        }
        Block top = targetLoc.clone().add(0, 2, 0).getBlock();
        if (top.getType() == Material.AIR) {
            top.setType(Material.PACKED_ICE);
            iceBlocks.add(top);
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Block b : iceBlocks) {
                    if (b.getType() == Material.PACKED_ICE) b.setType(Material.AIR);
                }
            }
        }.runTaskLater(plugin, 80L);
        return true;
    }
}
class IceBlizzard extends Ability {
    private final ItemEditFull plugin;
    public IceBlizzard(ItemEditFull pl) { super("ice_blizzard", "Blizzard", "Ice storm area."); this.plugin = pl; }
    @Override public boolean trigger(Player p, ItemStack i) {
        Location target = p.getTargetBlock(null, 15).getLocation();
        p.getWorld().playSound(target, Sound.BLOCK_SNOW_BREAK, 1.2f, 0.5f);
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks++ > 80) {
                    cancel();
                    return;
                }
                target.getWorld().spawnParticle(Particle.SNOW_SHOVEL, target, 25, 4.0, 2.0, 4.0, 0.05);
                for (Entity ent : target.getWorld().getNearbyEntities(target, 4.5, 2.5, 4.5)) {
                    if (ent instanceof LivingEntity && !ent.equals(p)) {
                        LivingEntity le = (LivingEntity) ent;
                        le.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 2));
                        if (ticks % 10 == 0) {
                            le.damage(1.0, p);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
        return true;
    }
}
class LightningChainAbility extends Ability {
    public LightningChainAbility(ItemEditFull pl) { super("lightning_chain", "Lightning Chain", "Bounces lightning."); }
    @Override public boolean trigger(Player p, ItemStack i) {
        LivingEntity target = null;
        for (Entity ent : p.getWorld().getNearbyEntities(p.getLocation(), 15.0, 5.0, 15.0)) {
            if (ent instanceof LivingEntity && !ent.equals(p)) {
                target = (LivingEntity) ent;
                break;
            }
        }
        if (target == null) return false;
        List<LivingEntity> chain = new ArrayList<>();
        chain.add(target);
        for (Entity ent : target.getWorld().getNearbyEntities(target.getLocation(), 8.0, 3.0, 8.0)) {
            if (ent instanceof LivingEntity && !ent.equals(p) && !chain.contains(ent) && chain.size() < 4) {
                chain.add((LivingEntity) ent);
            }
        }
        for (LivingEntity le : chain) {
            le.getWorld().strikeLightning(le.getLocation());
            le.damage(5.0, p);
        }
        return true;
    }
}
class LightningStorm extends Ability {
    private final ItemEditFull plugin;
    public LightningStorm(ItemEditFull pl) { super("lightning_storm", "Lightning Storm", "Call lightning area."); this.plugin = pl; }
    @Override public boolean trigger(Player p, ItemStack i) {
        Location target = p.getTargetBlock(null, 15).getLocation();
        new BukkitRunnable() {
            int strikes = 0;
            @Override
            public void run() {
                if (strikes++ > 5) {
                    cancel();
                    return;
                }
                double rx = (Math.random() - 0.5) * 10;
                double rz = (Math.random() - 0.5) * 10;
                Location strikeLoc = target.clone().add(rx, 0, rz);
                strikeLoc.getWorld().strikeLightning(strikeLoc);
                for (Entity ent : strikeLoc.getWorld().getNearbyEntities(strikeLoc, 3.0, 2.0, 3.0)) {
                    if (ent instanceof LivingEntity && !ent.equals(p)) {
                        ((LivingEntity) ent).damage(6.0, p);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 6L);
        return true;
    }
}
class LightningDash extends Ability {
    public LightningDash(ItemEditFull pl) { super("lightning_dash", "Lightning Dash", "Blink and strike."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.teleport(p.getLocation().add(p.getLocation().getDirection().multiply(6))); return true; }
}
class EarthWall extends Ability {
    private final ItemEditFull plugin;
    public EarthWall(ItemEditFull pl) { super("earth_wall", "Earth Wall", "Temp walls block."); this.plugin = pl; }
    @Override public boolean trigger(Player p, ItemStack i) {
        Location loc = p.getLocation();
        Vector dir = loc.getDirection().setY(0).normalize();
        Vector left = new Vector(-dir.getZ(), 0, dir.getX()).normalize();
        p.getWorld().playSound(loc, Sound.BLOCK_GRAVEL_BREAK, 1.2f, 0.8f);
        final List<Block> raisedBlocks = new ArrayList<>();
        for (int k = -1; k <= 1; k++) {
            Location step = loc.clone().add(dir.clone().multiply(2)).add(left.clone().multiply(k));
            for (int y = 0; y <= 1; y++) {
                Block b = step.clone().add(0, y, 0).getBlock();
                if (b.getType() == Material.AIR) {
                    b.setType(Material.DIRT);
                    raisedBlocks.add(b);
                }
            }
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Block b : raisedBlocks) {
                    if (b.getType() == Material.DIRT) b.setType(Material.AIR);
                }
            }
        }.runTaskLater(plugin, 100L);
        return true;
    }
}
class EarthQuakeAbility extends Ability {
    public EarthQuakeAbility(ItemEditFull pl) { super("earth_quake", "Earthquake", "Ground damage wave."); }
    @Override public boolean trigger(Player p, ItemStack i) {
        Location loc = p.getLocation();
        p.getWorld().playSound(loc, Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 1.5f, 0.5f);
        p.getWorld().spawnParticle(Particle.BLOCK_DUST, loc, 60, 5.0, 0.2, 5.0, Material.DIRT.createBlockData());
        for (Entity ent : p.getWorld().getNearbyEntities(loc, 6.0, 2.0, 6.0)) {
            if (ent instanceof LivingEntity && !ent.equals(p)) {
                LivingEntity le = (LivingEntity) ent;
                le.damage(4.0, p);
                le.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 2));
                le.setVelocity(new Vector((Math.random() - 0.5) * 0.5, 0.3, (Math.random() - 0.5) * 0.5));
            }
        }
        return true;
    }
}
class EarthTomb extends Ability {
    private final ItemEditFull plugin;
    public EarthTomb(ItemEditFull pl) { super("earth_tomb", "Earth Tomb", "Pull target underground."); this.plugin = pl; }
    @Override public boolean trigger(Player p, ItemStack i) {
        LivingEntity target = null;
        for (Entity ent : p.getWorld().getNearbyEntities(p.getLocation(), 15.0, 5.0, 15.0)) {
            if (ent instanceof LivingEntity && !ent.equals(p)) {
                target = (LivingEntity) ent;
                break;
            }
        }
        if (target == null) return false;
        Location tLoc = target.getLocation();
        p.getWorld().playSound(tLoc, Sound.BLOCK_STONE_BREAK, 1.2f, 0.8f);
        final List<Block> cage = new ArrayList<>();
        int[][] offsets = {{0,0}, {1,0}, {-1,0}, {0,1}, {0,-1}};
        for (int[] off : offsets) {
            for (int y = 0; y <= 2; y++) {
                Block b = tLoc.clone().add(off[0], y, off[1]).getBlock();
                if (b.getType() == Material.AIR) {
                    b.setType(Material.COBBLESTONE);
                    cage.add(b);
                }
            }
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Block b : cage) {
                    if (b.getType() == Material.COBBLESTONE) b.setType(Material.AIR);
                }
            }
        }.runTaskLater(plugin, 80L);
        return true;
    }
}
class WindPushAbility extends Ability {
    public WindPushAbility(ItemEditFull pl) { super("wind_push", "Wind Push", "Cone wind push."); }
    @Override public boolean trigger(Player p, ItemStack i) {
        Location loc = p.getLocation();
        Vector dir = loc.getDirection().normalize();
        p.getWorld().playSound(loc, Sound.ENTITY_ENDER_DRAGON_FLAP, 1.2f, 1.2f);
        p.getWorld().spawnParticle(Particle.CLOUD, loc.clone().add(dir.clone().multiply(2)), 30, 2.0, 1.0, 2.0, 0.1);
        for (Entity ent : p.getWorld().getNearbyEntities(loc, 6.0, 3.0, 6.0)) {
            if (ent instanceof LivingEntity && !ent.equals(p)) {
                LivingEntity le = (LivingEntity) ent;
                Vector toTarget = le.getLocation().toVector().subtract(loc.toVector()).normalize();
                if (toTarget.dot(dir) > 0.4) {
                    le.setVelocity(dir.clone().multiply(1.8).setY(0.4));
                    le.damage(2.0, p);
                }
            }
        }
        return true;
    }
}
class WindPull extends Ability {
    public WindPull(ItemEditFull pl) { super("wind_pull", "Wind Pull", "Vacuum draw."); }
    @Override public boolean trigger(Player p, ItemStack i) {
        Location target = p.getTargetBlock(null, 15).getLocation();
        p.getWorld().playSound(target, Sound.ENTITY_ENDER_DRAGON_FLAP, 1.2f, 0.6f);
        p.getWorld().spawnParticle(Particle.CLOUD, target, 30, 3.0, 1.5, 3.0, 0.1);
        for (Entity ent : target.getWorld().getNearbyEntities(target, 6.0, 3.0, 6.0)) {
            if (ent instanceof LivingEntity && !ent.equals(p)) {
                Vector dir = target.toVector().subtract(ent.getLocation().toVector()).normalize().multiply(1.5).setY(0.2);
                ent.setVelocity(dir);
            }
        }
        return true;
    }
}
class WindCyclone extends Ability {
    private final ItemEditFull plugin;
    public WindCyclone(ItemEditFull pl) { super("wind_cyclone", "Wind Cyclone", "Localized tornado."); this.plugin = pl; }
    @Override public boolean trigger(Player p, ItemStack i) {
        Location target = p.getTargetBlock(null, 15).getLocation();
        p.getWorld().playSound(target, Sound.ENTITY_PHANTOM_FLAP, 1.2f, 0.8f);
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks++ > 40) {
                    cancel();
                    return;
                }
                target.getWorld().spawnParticle(Particle.CLOUD, target, 10, 1.0, 2.0, 1.0, 0.2);
                for (Entity ent : target.getWorld().getNearbyEntities(target, 3.0, 4.0, 3.0)) {
                    if (ent instanceof LivingEntity && !ent.equals(p)) {
                        LivingEntity le = (LivingEntity) ent;
                        le.setVelocity(new Vector((Math.random() - 0.5) * 0.3, 0.5, (Math.random() - 0.5) * 0.3));
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
        return true;
    }
}
class LightHeal extends Ability {
    public LightHeal(ItemEditFull pl) { super("light_heal", "Holy Light Heal", "Heals allies."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.addPotionEffect(new PotionEffect(PotionEffectType.HEAL, 1, 1)); return true; }
}
class LightBeam extends Ability {
    public LightBeam(ItemEditFull pl) { super("light_beam", "Light Beam", "Burns undead."); }
    @Override public boolean trigger(Player p, ItemStack i) {
        Location pEye = p.getEyeLocation();
        Vector dir = pEye.getDirection().normalize();
        p.getWorld().playSound(p.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.2f, 1.5f);
        for (double d = 0; d < 15.0; d += 0.5) {
            Location point = pEye.clone().add(dir.clone().multiply(d));
            point.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, point, 2, 0.1, 0.1, 0.1, 0.02);
            for (Entity ent : point.getWorld().getNearbyEntities(point, 1.0, 1.0, 1.0)) {
                if (ent instanceof LivingEntity && !ent.equals(p)) {
                    LivingEntity le = (LivingEntity) ent;
                    if (le.getCategory() == EntityCategory.UNDEAD) {
                        le.setFireTicks(100);
                        le.damage(8.0, p);
                    } else {
                        le.damage(4.0, p);
                    }
                }
            }
        }
        return true;
    }
}
class LightFlash extends Ability {
    public LightFlash(ItemEditFull pl) { super("light_flash", "Light Flash", "Blinds targets."); }
    @Override public boolean trigger(Player p, ItemStack i) {
        Location loc = p.getLocation();
        p.getWorld().playSound(loc, Sound.BLOCK_AMETHYST_CLUSTER_BREAK, 1.5f, 1.5f);
        p.getWorld().spawnParticle(Particle.FLASH, loc, 5, 2.0, 1.5, 2.0, 0.05);
        for (Entity ent : p.getWorld().getNearbyEntities(loc, 8.0, 3.0, 8.0)) {
            if (ent instanceof LivingEntity && !ent.equals(p)) {
                LivingEntity le = (LivingEntity) ent;
                le.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 0));
                le.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 100, 0));
            }
        }
        return true;
    }
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
    private final ItemEditFull plugin;
    public ShadowClone(ItemEditFull pl) { super("shadow_clone", "Shadow Clone", "Target decoy clone."); this.plugin = pl; }
    @Override public boolean trigger(Player p, ItemStack i) {
        Location loc = p.getLocation();
        p.getWorld().playSound(loc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.8f);
        ArmorStand stand = p.getWorld().spawn(loc, ArmorStand.class);
        stand.setVisible(false);
        stand.getEquipment().setHelmet(new ItemStack(Material.WITHER_SKELETON_SKULL));
        stand.getEquipment().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
        stand.getEquipment().setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
        stand.getEquipment().setBoots(new ItemStack(Material.LEATHER_BOOTS));
        stand.setCustomName("§8Shadow Clone");
        stand.setCustomNameVisible(true);
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks++ > 30) {
                    cancel();
                    stand.getWorld().spawnParticle(Particle.SMOKE_LARGE, stand.getLocation().add(0, 1.0, 0), 10, 0.3, 0.5, 0.3, 0.02);
                    stand.remove();
                    return;
                }
                stand.getWorld().spawnParticle(Particle.PORTAL, stand.getLocation().add(0, 1.0, 0), 4, 0.2, 0.4, 0.2, 0.05);
            }
        }.runTaskTimer(plugin, 0L, 2L);
        return true;
    }
}
class VoidCollapse extends Ability {
    private final ItemEditFull plugin;
    public VoidCollapse(ItemEditFull pl) { super("void_collapse", "Void Collapse", "Black hole pulls."); this.plugin = pl; }
    @Override public boolean trigger(Player p, ItemStack i) {
        Location target = p.getTargetBlock(null, 15).getLocation();
        p.getWorld().playSound(target, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 0.6f);
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks++ > 40) {
                    cancel();
                    target.getWorld().createExplosion(target, 3.5f, false, false);
                    return;
                }
                target.getWorld().spawnParticle(Particle.DRAGON_BREATH, target, 15, 2.0, 2.0, 2.0, 0.02);
                target.getWorld().spawnParticle(Particle.PORTAL, target, 20, 1.0, 1.0, 1.0, 0.05);
                for (Entity ent : target.getWorld().getNearbyEntities(target, 5.0, 3.0, 5.0)) {
                    if (ent instanceof LivingEntity && !ent.equals(p)) {
                        Vector pull = target.toVector().subtract(ent.getLocation().toVector()).normalize().multiply(0.3).setY(0.05);
                        ent.setVelocity(pull);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
        return true;
    }
}
class VoidGrasp extends Ability {
    private final ItemEditFull plugin;
    public VoidGrasp(ItemEditFull pl) { super("void_grasp", "Void Grasp", "Grapples targets."); this.plugin = pl; }
    @Override public boolean trigger(Player p, ItemStack i) {
        LivingEntity target = null;
        for (Entity ent : p.getWorld().getNearbyEntities(p.getLocation(), 15.0, 5.0, 15.0)) {
            if (ent instanceof LivingEntity && !ent.equals(p)) {
                target = (LivingEntity) ent;
                break;
            }
        }
        if (target == null) return false;
        final LivingEntity finalTarget = target;
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_SCREAM, 1.0f, 0.8f);
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks++ > 30 || !finalTarget.isValid()) {
                    cancel();
                    return;
                }
                finalTarget.setVelocity(new Vector(0, 0.15, 0));
                finalTarget.getWorld().spawnParticle(Particle.PORTAL, finalTarget.getLocation().add(0, 1.0, 0), 8, 0.3, 0.3, 0.3, 0.05);
                if (ticks % 10 == 0) {
                    finalTarget.damage(2.0, p);
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
        return true;
    }
}
class VoidWarp extends Ability {
    public VoidWarp(ItemEditFull pl) { super("void_warp", "Void Warp", "Warp blink."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.teleport(p.getLocation().add(p.getLocation().getDirection().multiply(6))); return true; }
}
class CosmicShower extends Ability {
    private final ItemEditFull plugin;
    public CosmicShower(ItemEditFull pl) { super("cosmic_shower", "Cosmic Shower", "Rains stardust."); this.plugin = pl; }
    @Override public boolean trigger(Player p, ItemStack i) {
        Location target = p.getTargetBlock(null, 15).getLocation();
        p.getWorld().playSound(target, Sound.BLOCK_AMETHYST_CLUSTER_STEP, 1.2f, 1.5f);
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks++ > 40) {
                    cancel();
                    return;
                }
                for (int j = 0; j < 5; j++) {
                    double rx = (Math.random() - 0.5) * 6;
                    double rz = (Math.random() - 0.5) * 6;
                    Location fall = target.clone().add(rx, 5, rz);
                    fall.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, fall, 0, 0, -1.0, 0, 0.15);
                }
                if (ticks % 10 == 0) {
                    for (Entity ent : target.getWorld().getNearbyEntities(target, 4.0, 3.0, 4.0)) {
                        if (ent instanceof LivingEntity && !ent.equals(p)) {
                            ((LivingEntity) ent).damage(2.0, p);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
        return true;
    }
}
class CosmicSingularity extends Ability {
    private final ItemEditFull plugin;
    public CosmicSingularity(ItemEditFull pl) { super("cosmic_singularity", "Cosmic Singularity", "Implodes targets."); this.plugin = pl; }
    @Override public boolean trigger(Player p, ItemStack i) {
        Location target = p.getTargetBlock(null, 15).getLocation();
        p.getWorld().playSound(target, Sound.BLOCK_PORTAL_TRAVEL, 1.0f, 1.5f);
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks++ > 30) {
                    cancel();
                    target.getWorld().spawnParticle(Particle.FLASH, target, 10, 2.0, 2.0, 2.0, 0.05);
                    target.getWorld().playSound(target, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 1.2f);
                    for (Entity ent : target.getWorld().getNearbyEntities(target, 5.0, 3.0, 5.0)) {
                        if (ent instanceof LivingEntity && !ent.equals(p)) {
                            ((LivingEntity) ent).damage(6.0, p);
                        }
                    }
                    return;
                }
                target.getWorld().spawnParticle(Particle.PORTAL, target, 15, 2.0, 2.0, 2.0, 0.05);
                for (Entity ent : target.getWorld().getNearbyEntities(target, 5.0, 3.0, 5.0)) {
                    if (ent instanceof LivingEntity && !ent.equals(p)) {
                        Vector pull = target.toVector().subtract(ent.getLocation().toVector()).normalize().multiply(0.4).setY(0.05);
                        ent.setVelocity(pull);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
        return true;
    }
}
class CosmicShield extends Ability {
    public CosmicShield(ItemEditFull pl) { super("cosmic_shield", "Cosmic Shield", "Shield orbits."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 200, 1)); return true; }
}
class AcidSpray extends Ability {
    private final ItemEditFull plugin;
    public AcidSpray(ItemEditFull pl) { super("acid_spray", "Acid Spray", "Acid spray poison."); this.plugin = pl; }
    @Override public boolean trigger(Player p, ItemStack i) {
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_LLAMA_SPIT, 1.0f, 0.8f);
        Snowball b = p.launchProjectile(Snowball.class);
        b.setMetadata("acid_spray", new FixedMetadataValue(plugin, true));
        new BukkitRunnable() {
            @Override
            public void run() {
                if (b.isDead() || !b.isValid()) {
                    cancel();
                    return;
                }
                b.getWorld().spawnParticle(Particle.SLIME, b.getLocation(), 3, 0.1, 0.1, 0.1, 0.01);
            }
        }.runTaskTimer(plugin, 0L, 1L);
        return true;
    }
}
class AcidRain extends Ability {
    private final ItemEditFull plugin;
    public AcidRain(ItemEditFull pl) { super("acid_rain", "Acid Rain", "Corrosive rain."); this.plugin = pl; }
    @Override public boolean trigger(Player p, ItemStack i) {
        Location target = p.getTargetBlock(null, 15).getLocation();
        p.getWorld().playSound(target, Sound.WEATHER_RAIN, 1.0f, 0.7f);
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks++ > 60) {
                    cancel();
                    return;
                }
                for (int j = 0; j < 10; j++) {
                    double rx = (Math.random() - 0.5) * 6;
                    double rz = (Math.random() - 0.5) * 6;
                    Location dropLoc = target.clone().add(rx, 4, rz);
                    dropLoc.getWorld().spawnParticle(Particle.WATER_DROP, dropLoc, 0, 0, -1.0, 0, 0.2);
                    Location floorLoc = target.clone().add(rx, 0.1, rz);
                    floorLoc.getWorld().spawnParticle(Particle.SLIME, floorLoc, 1, 0.1, 0, 0.1, 0.01);
                }
                if (ticks % 10 == 0) {
                    for (Entity ent : target.getWorld().getNearbyEntities(target, 4.0, 3.0, 4.0)) {
                        if (ent instanceof LivingEntity && !ent.equals(p)) {
                            LivingEntity le = (LivingEntity) ent;
                            le.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 80, 1));
                            le.damage(1.5, p);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
        return true;
    }
}
class AcidPuddle extends Ability {
    private final ItemEditFull plugin;
    public AcidPuddle(ItemEditFull pl) { super("acid_puddle", "Acid Puddle", "Continuous damage."); this.plugin = pl; }
    @Override public boolean trigger(Player p, ItemStack i) {
        Location target = p.getLocation();
        p.getWorld().playSound(target, Sound.BLOCK_LAVA_AMBIENT, 1.0f, 0.6f);
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks++ > 100) {
                    cancel();
                    return;
                }
                target.getWorld().spawnParticle(Particle.SLIME, target, 5, 2.0, 0.1, 2.0, 0.01);
                if (ticks % 10 == 0) {
                    for (Entity ent : target.getWorld().getNearbyEntities(target, 2.5, 1.0, 2.5)) {
                        if (ent instanceof LivingEntity && !ent.equals(p)) {
                            LivingEntity le = (LivingEntity) ent;
                            le.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 2));
                            le.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 60, 0));
                            le.damage(2.0, p);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
        return true;
    }
}

// Subclasses (61-90 Combat styles)
class AssassinBackstab extends Ability {
    public AssassinBackstab(ItemEditFull pl) { super("assassin_backstab", "Assassin Backstab", "Melee critical backstab."); }
    @Override public boolean trigger(Player p, ItemStack i) {
        LivingEntity target = null;
        for (Entity ent : p.getWorld().getNearbyEntities(p.getLocation(), 4.0, 2.0, 4.0)) {
            if (ent instanceof LivingEntity && !ent.equals(p)) {
                target = (LivingEntity) ent;
                break;
            }
        }
        if (target == null) return false;
        Vector dir = target.getLocation().getDirection().normalize();
        Location behind = target.getLocation().subtract(dir.multiply(1.0));
        behind.setDirection(target.getLocation().getDirection());
        p.teleport(behind);
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.2f, 1.5f);
        p.getWorld().spawnParticle(Particle.CRIT_MAGIC, target.getLocation().add(0, 1, 0), 15, 0.2, 0.2, 0.2, 0.1);
        target.damage(8.0, p);
        return true;
    }
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
    @Override public boolean trigger(Player p, ItemStack i) {
        Location loc = p.getLocation();
        p.getWorld().playSound(loc, Sound.ENTITY_GOAT_SCREAMING_AMBIENT, 1.2f, 0.8f);
        p.getWorld().spawnParticle(Particle.VILLAGER_ANGRY, loc, 15, 6.0, 1.5, 6.0, 0.05);
        int provokedCount = 0;
        for (Entity ent : p.getWorld().getNearbyEntities(loc, 8.0, 3.0, 8.0)) {
            if (ent instanceof Mob) {
                ((Mob) ent).setTarget(p);
                provokedCount++;
            }
        }
        return provokedCount > 0;
    }
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
    @Override public boolean trigger(Player p, ItemStack i) {
        double healthPct = p.getHealth() / p.getMaxHealth();
        int amp = (healthPct < 0.3) ? 2 : (healthPct < 0.6) ? 1 : 0;
        p.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 160, amp));
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.2f);
        p.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, p.getLocation(), 20, 0.5, 0.5, 0.5, 0.1);
        return true;
    }
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
    @Override public boolean trigger(Player p, ItemStack i) {
        MoreExpansionAbilities.addRes(p.getUniqueId(), System.currentTimeMillis() + 30000L);
        p.getWorld().playSound(p.getLocation(), Sound.BLOCK_AMETHYST_CLUSTER_STEP, 1.2f, 1.2f);
        p.getWorld().spawnParticle(Particle.TOTEM, p.getLocation(), 20, 0.5, 1.0, 0.5, 0.1);
        p.sendMessage("§a§lResurrection Guard activated for 30s!");
        return true;
    }
}
class ArcherVolley extends Ability {
    public ArcherVolley(ItemEditFull pl) { super("archer_volley", "Arrow Volley", "Arrows wave."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.launchProjectile(Arrow.class); return true; }
}
class ArcherSnipe extends Ability {
    public ArcherSnipe(ItemEditFull pl) { super("archer_snipe", "Archer Snipe", "Ranged bonus."); }
    @Override public boolean trigger(Player p, ItemStack i) {
        Arrow arrow = p.launchProjectile(Arrow.class);
        arrow.setVelocity(arrow.getVelocity().multiply(2.0));
        arrow.setMetadata("archer_snipe", new FixedMetadataValue(MoreExpansionAbilities.getPlugin(), true));
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1.0f, 0.5f);
        return true;
    }
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
    @Override public boolean trigger(Player p, ItemStack i) {
        LivingEntity target = null;
        for (Entity ent : p.getWorld().getNearbyEntities(p.getLocation(), 8.0, 3.0, 8.0)) {
            if (ent instanceof LivingEntity && !ent.equals(p)) {
                target = (LivingEntity) ent;
                break;
            }
        }
        if (target == null) return false;
        boolean stole = false;
        for (PotionEffect pe : target.getActivePotionEffects()) {
            if (pe.getType().equals(PotionEffectType.SPEED) ||
                pe.getType().equals(PotionEffectType.INCREASE_DAMAGE) ||
                pe.getType().equals(PotionEffectType.DAMAGE_RESISTANCE) ||
                pe.getType().equals(PotionEffectType.REGENERATION)) {
                p.addPotionEffect(pe);
                target.removePotionEffect(pe.getType());
                stole = true;
            }
        }
        if (stole) {
            p.getWorld().playSound(p.getLocation(), Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1.2f, 1.5f);
            p.getWorld().spawnParticle(Particle.SPELL_WITCH, p.getLocation(), 15, 0.3, 0.5, 0.3, 0.05);
        }
        return stole;
    }
}
class PaladinSmiteAbility extends Ability {
    public PaladinSmiteAbility(ItemEditFull pl) { super("paladin_smite", "Paladin Smite", "Holy strike fire."); }
    @Override public boolean trigger(Player p, ItemStack i) {
        LivingEntity target = null;
        for (Entity ent : p.getWorld().getNearbyEntities(p.getLocation(), 5.0, 2.0, 5.0)) {
            if (ent instanceof LivingEntity && !ent.equals(p)) {
                target = (LivingEntity) ent;
                break;
            }
        }
        if (target == null) return false;
        target.getWorld().strikeLightningEffect(target.getLocation());
        target.setFireTicks(80);
        target.damage(6.0, p);
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.5f);
        return true;
    }
}
class PaladinShieldAbility extends Ability {
    public PaladinShieldAbility(ItemEditFull pl) { super("paladin_shield", "Paladin Shield", "Resistance boost."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 200, 1)); return true; }
}
class PaladinAuraAbility extends Ability {
    public PaladinAuraAbility(ItemEditFull pl) { super("paladin_aura", "Holy Paladin Aura", "Aura resistance."); }
    @Override public boolean trigger(Player p, ItemStack i) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 200, 1));
        p.getWorld().playSound(p.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.5f);
        p.getWorld().spawnParticle(Particle.SPELL_INSTANT, p.getLocation(), 20, 4.0, 1.0, 4.0, 0.01);
        for (Entity ent : p.getWorld().getNearbyEntities(p.getLocation(), 6.0, 2.0, 6.0)) {
            if (ent instanceof Player && !ent.equals(p)) {
                ((Player) ent).addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 200, 0));
                ent.sendMessage("§aReceived Holy Paladin Aura from " + p.getName());
            }
        }
        return true;
    }
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
    @Override public boolean trigger(Player p, ItemStack i) {
        LivingEntity target = null;
        for (Entity ent : p.getWorld().getNearbyEntities(p.getLocation(), 4.0, 2.0, 4.0)) {
            if (ent instanceof LivingEntity && !ent.equals(p)) {
                target = (LivingEntity) ent;
                break;
            }
        }
        if (target == null) return false;
        target.setVelocity(new Vector(0, 1.3, 0));
        target.damage(4.0, p);
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR, 1.2f, 1.2f);
        p.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, target.getLocation(), 10, 0.3, 0.3, 0.3, 0.05);
        return true;
    }
}
class BrawlerTackle extends Ability {
    public BrawlerTackle(ItemEditFull pl) { super("brawler_tackle", "Brawler Tackle", "Pin target slowness."); }
    @Override public boolean trigger(Player p, ItemStack i) {
        LivingEntity target = null;
        for (Entity ent : p.getWorld().getNearbyEntities(p.getLocation(), 5.0, 2.0, 5.0)) {
            if (ent instanceof LivingEntity && !ent.equals(p)) {
                target = (LivingEntity) ent;
                break;
            }
        }
        if (target == null) return false;
        Vector dir = target.getLocation().toVector().subtract(p.getLocation().toVector()).normalize();
        p.setVelocity(dir.multiply(1.2));
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, 10));
        target.damage(2.0, p);
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK, 1.2f, 0.8f);
        return true;
    }
}
class BrawlerShockwave extends Ability {
    public BrawlerShockwave(ItemEditFull pl) { super("brawler_shockwave", "Shockwave Slam", "Knocks surrounding."); }
    @Override public boolean trigger(Player p, ItemStack i) {
        Location loc = p.getLocation();
        p.getWorld().playSound(loc, Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 1.5f, 0.8f);
        p.getWorld().spawnParticle(Particle.CLOUD, loc, 35, 4.0, 0.2, 4.0, 0.1);
        boolean hit = false;
        for (Entity ent : p.getWorld().getNearbyEntities(loc, 4.5, 2.0, 4.5)) {
            if (ent instanceof LivingEntity && !ent.equals(p)) {
                LivingEntity le = (LivingEntity) ent;
                Vector push = le.getLocation().toVector().subtract(loc.toVector()).normalize().multiply(1.2).setY(0.3);
                le.setVelocity(push);
                le.damage(3.5, p);
                hit = true;
            }
        }
        return hit;
    }
}
class NinjaTeleport extends Ability {
    public NinjaTeleport(ItemEditFull pl) { super("ninja_teleport", "Ninja Teleport", "Blink last target."); }
    @Override public boolean trigger(Player p, ItemStack i) {
        LivingEntity lastTarget = null;
        for (Entity ent : p.getWorld().getNearbyEntities(p.getLocation(), 15.0, 5.0, 15.0)) {
            if (ent instanceof LivingEntity && !ent.equals(p)) {
                lastTarget = (LivingEntity) ent;
                break;
            }
        }
        if (lastTarget == null) return false;
        p.getWorld().spawnParticle(Particle.SMOKE_NORMAL, p.getLocation(), 15, 0.2, 0.5, 0.2, 0.02);
        p.teleport(lastTarget.getLocation().subtract(lastTarget.getLocation().getDirection().multiply(1.0)));
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.5f);
        p.getWorld().spawnParticle(Particle.SMOKE_LARGE, p.getLocation(), 15, 0.2, 0.5, 0.2, 0.02);
        return true;
    }
}
class NinjaDodge extends Ability {
    public NinjaDodge(ItemEditFull pl) { super("ninja_dodge", "Ninja Dodge", "Dodge rate percent."); }
    @Override public boolean trigger(Player p, ItemStack i) {
        MoreExpansionAbilities.addDodge(p.getUniqueId(), System.currentTimeMillis() + 15000L);
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_SHOOT, 1.2f, 1.5f);
        p.getWorld().spawnParticle(Particle.PORTAL, p.getLocation(), 15, 0.3, 0.5, 0.3, 0.1);
        p.sendMessage("§7§lNinja Dodge activated (15s)!");
        return true;
    }
}
class NinjaStarVolley extends Ability {
    public NinjaStarVolley(ItemEditFull pl) { super("ninja_star_volley", "Shuriken Volley", "Throws stars."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.launchProjectile(Snowball.class); return true; }
}

// Subclasses (91-100 Utility)
class GrapplingHook extends Ability {
    public GrapplingHook(ItemEditFull pl) { super("grappling_hook", "Grappling Hook", "Pull player hook."); }
    @Override public boolean trigger(Player p, ItemStack i) {
        Block target = p.getTargetBlock(null, 25);
        if (target.getType() == Material.AIR) return false;
        Vector dir = target.getLocation().toVector().subtract(p.getLocation().toVector()).normalize().multiply(1.6).setY(0.65);
        p.setVelocity(dir);
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_FISHING_BOBBER_RETRIEVE, 1.0f, 1.0f);
        p.getWorld().spawnParticle(Particle.CRIT, p.getLocation(), 15, 0.3, 0.3, 0.3, 0.1);
        return true;
    }
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
    @Override public boolean trigger(Player p, ItemStack i) {
        Location loc = p.getLocation();
        p.getWorld().playSound(loc, Sound.ITEM_BONE_MEAL_USE, 1.2f, 1.0f);
        p.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, loc, 30, 4.0, 1.0, 4.0, 0.05);
        boolean grew = false;
        for (int x = -4; x <= 4; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -4; z <= 4; z++) {
                    Block b = loc.clone().add(x, y, z).getBlock();
                    if (b.getBlockData() instanceof org.bukkit.block.data.Ageable) {
                        org.bukkit.block.data.Ageable ageable = (org.bukkit.block.data.Ageable) b.getBlockData();
                        if (ageable.getAge() < ageable.getMaximumAge()) {
                            ageable.setAge(Math.min(ageable.getMaximumAge(), ageable.getAge() + 2));
                            b.setBlockData(ageable);
                            grew = true;
                        }
                    }
                }
            }
        }
        return grew;
    }
}
class MineralSense extends Ability {
    public MineralSense(ItemEditFull pl) { super("mineral_sense", "Mineral Sense", "Highlight ores."); }
    @Override public boolean trigger(Player p, ItemStack i) {
        Location loc = p.getLocation();
        p.getWorld().playSound(loc, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.2f, 1.2f);
        boolean found = false;
        for (int x = -8; x <= 8; x++) {
            for (int y = -8; y <= 8; y++) {
                for (int z = -8; z <= 8; z++) {
                    Block b = loc.clone().add(x, y, z).getBlock();
                    if (b.getType() == Material.DIAMOND_ORE || b.getType() == Material.DEEPSLATE_DIAMOND_ORE ||
                        b.getType() == Material.GOLD_ORE || b.getType() == Material.DEEPSLATE_GOLD_ORE ||
                        b.getType() == Material.ANCIENT_DEBRIS) {
                        b.getWorld().spawnParticle(Particle.SPELL_INSTANT, b.getLocation().add(0.5, 0.5, 0.5), 3, 0.2, 0.2, 0.2, 0.01);
                        found = true;
                    }
                }
            }
        }
        if (found) {
            p.sendMessage("§bOres highlighted nearby!");
        }
        return found;
    }
}
class TimeLeap extends Ability {
    private static final Map<UUID, List<Location>> history = new HashMap<>();
    private final ItemEditFull plugin;
    public TimeLeap(ItemEditFull pl) {
        super("time_leap", "Time Leap", "Time warp back.");
        this.plugin = pl;
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player online : Bukkit.getOnlinePlayers()) {
                    List<Location> locs = history.computeIfAbsent(online.getUniqueId(), k -> new ArrayList<>());
                    locs.add(online.getLocation());
                    if (locs.size() > 10) locs.remove(0);
                }
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }
    @Override public boolean trigger(Player p, ItemStack i) {
        List<Location> locs = history.get(p.getUniqueId());
        if (locs == null || locs.isEmpty()) return false;
        Location past = locs.get(0);
        p.getWorld().spawnParticle(Particle.PORTAL, p.getLocation(), 25, 0.3, 0.5, 0.3, 0.1);
        p.teleport(past);
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.2f, 0.5f);
        p.getWorld().spawnParticle(Particle.PORTAL, p.getLocation(), 25, 0.3, 0.5, 0.3, 0.1);
        p.setHealth(Math.min(p.getMaxHealth(), p.getHealth() + 4.0));
        p.sendMessage("§d§oLeaped back in time!");
        return true;
    }
}
class GravityWellAbility extends Ability {
    public GravityWellAbility(ItemEditFull pl) { super("gravity_well_passive", "Gravity Well", "Reverses gravity."); }
    @Override public boolean trigger(Player p, ItemStack i) {
        Location loc = p.getLocation();
        p.getWorld().playSound(loc, Sound.BLOCK_BEACON_DEACTIVATE, 1.2f, 0.5f);
        p.getWorld().spawnParticle(Particle.PORTAL, loc, 40, 5.0, 1.5, 5.0, 0.05);
        for (Entity ent : p.getWorld().getNearbyEntities(loc, 6.0, 3.0, 6.0)) {
            if (ent instanceof LivingEntity && !ent.equals(p)) {
                LivingEntity le = (LivingEntity) ent;
                le.setVelocity(new Vector(0, 0.8, 0));
                le.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 40, 1));
            }
        }
        return true;
    }
}
class SpeedRun extends Ability {
    public SpeedRun(ItemEditFull pl) { super("speed_run", "Speed Run Boost", "Double jump speed."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 3)); return true; }
}
class SuperDrill extends Ability {
    public SuperDrill(ItemEditFull pl) { super("super_drill", "Super Drill", "3x3 mine pattern."); }
    @Override public boolean trigger(Player p, ItemStack i) {
        Block target = p.getTargetBlock(null, 5);
        if (target.getType() == Material.AIR) return false;
        p.getWorld().playSound(target.getLocation(), Sound.BLOCK_STONE_BREAK, 1.2f, 1.5f);
        Vector dir = p.getLocation().getDirection();
        int rx = Math.abs(dir.getX()) > Math.abs(dir.getZ()) ? 0 : 1;
        int rz = rx == 0 ? 1 : 0;
        boolean broke = false;
        for (int y = -1; y <= 1; y++) {
            for (int off = -1; off <= 1; off++) {
                Block rel = target.getRelative(rx * off, y, rz * off);
                if (rel.getType() != Material.BEDROCK && rel.getType() != Material.AIR && rel.getType().isBlock()) {
                    rel.breakNaturally(i);
                    broke = true;
                }
            }
        }
        return broke;
    }
}
class EnderSwapStrike extends Ability {
    public EnderSwapStrike(ItemEditFull pl) { super("ender_swap_strike", "Swap Strike", "Swaps targets locations."); }
    @Override public boolean trigger(Player p, ItemStack i) {
        LivingEntity target = null;
        for (Entity ent : p.getWorld().getNearbyEntities(p.getLocation(), 15.0, 5.0, 15.0)) {
            if (ent instanceof LivingEntity && !ent.equals(p)) {
                target = (LivingEntity) ent;
                break;
            }
        }
        if (target == null) return false;
        Location pLoc = p.getLocation();
        Location tLoc = target.getLocation();
        p.getWorld().playSound(pLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.2f);
        p.getWorld().playSound(tLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.2f);
        p.teleport(tLoc);
        target.teleport(pLoc);
        target.damage(4.0, p);
        p.getWorld().spawnParticle(Particle.PORTAL, pLoc, 15, 0.3, 0.5, 0.3, 0.1);
        p.getWorld().spawnParticle(Particle.PORTAL, tLoc, 15, 0.3, 0.5, 0.3, 0.1);
        return true;
    }
}
class LunarBlessing extends Ability {
    public LunarBlessing(ItemEditFull pl) { super("lunar_blessing", "Lunar Blessing", "Night speed stats."); }
    @Override public boolean trigger(Player p, ItemStack i) { p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 600, 0)); return true; }
}
