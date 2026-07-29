package com.itemedit.full.ability.bard;

import com.itemedit.full.ItemEditFull;
import com.itemedit.full.ability.Ability;
import com.itemedit.full.utils.CompatRunnable;
import com.itemedit.full.utils.EffectUtils;
import com.itemedit.full.utils.VectorUtils;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Resonant Choir: 10 sound-magic abilities (song, tempo, and sound turned to
 * weapon or ward) - distinct from the Warden's raw sonic violence. A brand-new
 * school with no prior abilities.
 */
public class BardAbilities implements Listener {

    // ---- Harmonic Shield: absorbs exactly one hit ----
    private static final Map<UUID, Long> shieldActive = new ConcurrentHashMap<>();

    private static ItemEditFull pluginInstance;

    public static void register(ItemEditFull plugin) {
        pluginInstance = plugin;

        plugin.getAbilityManager().registerAbility(new RallyAnthem(plugin));
        plugin.getAbilityManager().registerAbility(new DiscordantScreech(plugin));
        plugin.getAbilityManager().registerAbility(new TempoShift(plugin));
        plugin.getAbilityManager().registerAbility(new LullabyWave(plugin));
        plugin.getAbilityManager().registerAbility(new BattleHymn(plugin));
        plugin.getAbilityManager().registerAbility(new DirgeOfSorrow(plugin));
        plugin.getAbilityManager().registerAbility(new HarmonicShield(plugin));
        plugin.getAbilityManager().registerAbility(new CrescendoBurst(plugin));
        plugin.getAbilityManager().registerAbility(new MusicDiscThrow(plugin));
        plugin.getAbilityManager().registerAbility(new SoundwavePush(plugin));

        plugin.getServer().getPluginManager().registerEvents(new BardAbilities(), plugin);
    }

    static void activateShield(UUID uuid, long expireAt) {
        shieldActive.put(uuid, expireAt);
    }

    @EventHandler
    public void onShieldDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity();
        Long expire = shieldActive.get(player.getUniqueId());
        if (expire == null) {
            return;
        }
        shieldActive.remove(player.getUniqueId());
        if (System.currentTimeMillis() >= expire) {
            return;
        }
        event.setCancelled(true);
        EffectUtils.fanfare(player.getLocation(),
                new EffectUtils.SoundLayer(Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.2f),
                new EffectUtils.SoundLayer(Sound.BLOCK_GLASS_BREAK, 0.4f, 1.6f));
        EffectUtils.burst(player.getLocation().add(0, 1, 0),
                new EffectUtils.Layer(Particle.NOTE, 10, 0.3, 0.4, 0.3, 1.0),
                new EffectUtils.Layer(Particle.DUST, 8, 0.3, 0.4, 0.3, 0, new Particle.DustOptions(Color.fromRGB(80, 220, 255), 1.0f)));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        shieldActive.remove(event.getPlayer().getUniqueId());
    }
}

// ==================== 10 RESONANT CHOIR ABILITIES ====================

class RallyAnthem extends Ability {
    private final ItemEditFull plugin;
    public RallyAnthem(ItemEditFull plugin) {
        super("rally_anthem", "Rally Anthem", "Plays a rousing anthem that boosts nearby allies' strength.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double radius = getDoubleParam(plugin, item, "radius", 8.0);
        double duration = getDoubleParam(plugin, item, "duration", 10.0);
        Location loc = player.getLocation();
        EffectUtils.fanfare(loc,
                new EffectUtils.SoundLayer(Sound.BLOCK_NOTE_BLOCK_PLING, 1.2f, 1.0f),
                new EffectUtils.SoundLayer(Sound.BLOCK_NOTE_BLOCK_BIT, 1.0f, 1.3f),
                new EffectUtils.SoundLayer(Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.4f));
        EffectUtils.ring(loc.clone().add(0, 0.2, 0), radius * 0.6, 12,
                new EffectUtils.Layer(Particle.DUST, 1, 0, 0.1, 0, 0, new Particle.DustOptions(Color.fromRGB(255, 200, 40), 1.1f)));
        EffectUtils.burst(loc.add(0, 2, 0),
                new EffectUtils.Layer(Particle.NOTE, 15, 0.4, 0.3, 0.4, 1.0),
                new EffectUtils.Layer(Particle.FIREWORK_SPARK, 10, 0.4, 0.3, 0.4, 0.02));
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
            if (entity instanceof Player) {
                ((Player) entity).addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, (int) (duration * 20), 0));
            }
        }
        return true;
    }
}

class DiscordantScreech extends Ability {
    private final ItemEditFull plugin;
    public DiscordantScreech(ItemEditFull plugin) {
        super("discordant_screech", "Discordant Screech", "A jarring note that weakens and slows nearby enemies.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double radius = getDoubleParam(plugin, item, "radius", 5.0);
        double duration = getDoubleParam(plugin, item, "duration", 5.0);
        Location loc = player.getLocation();
        EffectUtils.fanfare(loc,
                new EffectUtils.SoundLayer(Sound.BLOCK_NOTE_BLOCK_BASS, 1.3f, 0.5f),
                new EffectUtils.SoundLayer(Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 0.9f, 0.4f));
        EffectUtils.burst(loc.clone().add(0, 1, 0),
                new EffectUtils.Layer(Particle.NOTE, 12, radius * 0.3, 0.4, radius * 0.3, 2.5),
                new EffectUtils.Layer(Particle.DUST, 14, radius * 0.3, 0.4, radius * 0.3, 0, new Particle.DustOptions(Color.fromRGB(120, 20, 20), 1.2f)));
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
            if (entity instanceof LivingEntity && !entity.equals(player)) {
                LivingEntity living = (LivingEntity) entity;
                living.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, (int) (duration * 20), 0));
                living.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) (duration * 20), 1));
            }
        }
        return true;
    }
}

class TempoShift extends Ability {
    private final ItemEditFull plugin;
    public TempoShift(ItemEditFull plugin) {
        super("tempo_shift", "Tempo Shift", "Quickens your own tempo, granting a burst of speed and attack haste.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 6.0);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, (int) (duration * 20), 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, (int) (duration * 20), 1));
        EffectUtils.fanfare(player.getLocation(),
                new EffectUtils.SoundLayer(Sound.BLOCK_NOTE_BLOCK_XYLOPHONE, 1.0f, 1.6f),
                new EffectUtils.SoundLayer(Sound.BLOCK_NOTE_BLOCK_BIT, 0.8f, 1.8f));
        EffectUtils.burst(player.getLocation().add(0, 0.2, 0),
                new EffectUtils.Layer(Particle.NOTE, 10, 0.3, 0.1, 0.3, 1.0),
                new EffectUtils.Layer(Particle.CRIT, 8, 0.3, 0.1, 0.3, 0.15));
        return true;
    }
}

class LullabyWave extends Ability {
    private final ItemEditFull plugin;
    public LullabyWave(ItemEditFull plugin) {
        super("lullaby_wave", "Lullaby Wave", "A soothing melody that slows nearby enemies to a crawl.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double radius = getDoubleParam(plugin, item, "radius", 5.0);
        double duration = getDoubleParam(plugin, item, "duration", 4.0);
        Location loc = player.getLocation();
        EffectUtils.fanfare(loc,
                new EffectUtils.SoundLayer(Sound.BLOCK_NOTE_BLOCK_FLUTE, 1.0f, 0.7f),
                new EffectUtils.SoundLayer(Sound.ENTITY_CAT_PURR, 0.5f, 0.8f));
        EffectUtils.burst(loc.add(0, 1, 0),
                new EffectUtils.Layer(Particle.NOTE, 20, radius * 0.3, 0.4, radius * 0.3, 1.0),
                new EffectUtils.Layer(Particle.DUST, 12, radius * 0.3, 0.4, radius * 0.3, 0, new Particle.DustOptions(Color.fromRGB(120, 170, 255), 1.2f)));
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
            if (entity instanceof LivingEntity && !entity.equals(player)) {
                ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) (duration * 20), 3));
            }
        }
        return true;
    }
}

class BattleHymn extends Ability {
    private final ItemEditFull plugin;
    public BattleHymn(ItemEditFull plugin) {
        super("battle_hymn", "Battle Hymn", "Sings a hymn of war, boosting your own damage for the fight.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 10.0);
        int amplifier = getIntParam(plugin, item, "amplifier", 1);
        player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, (int) (duration * 20), amplifier));
        EffectUtils.fanfare(player.getLocation(),
                new EffectUtils.SoundLayer(Sound.BLOCK_NOTE_BLOCK_BIT, 1.2f, 0.8f),
                new EffectUtils.SoundLayer(Sound.ENTITY_PLAYER_ATTACK_STRONG, 0.6f, 0.9f));
        EffectUtils.burst(player.getLocation().add(0, 1.5, 0),
                new EffectUtils.Layer(Particle.NOTE, 12, 0.3, 0.3, 0.3, 1.0),
                new EffectUtils.Layer(Particle.DUST, 10, 0.3, 0.3, 0.3, 0, new Particle.DustOptions(Color.fromRGB(220, 50, 30), 1.2f)));
        return true;
    }
}

class DirgeOfSorrow extends Ability {
    private final ItemEditFull plugin;
    public DirgeOfSorrow(ItemEditFull plugin) {
        super("dirge_of_sorrow", "Dirge of Sorrow", "A mournful dirge that weakens enemy resolve, reducing their damage output.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double radius = getDoubleParam(plugin, item, "radius", 5.0);
        double duration = getDoubleParam(plugin, item, "duration", 6.0);
        Location loc = player.getLocation();
        EffectUtils.fanfare(loc,
                new EffectUtils.SoundLayer(Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1.0f, 0.6f),
                new EffectUtils.SoundLayer(Sound.ENTITY_WOLF_WHINE, 0.4f, 0.5f));
        EffectUtils.burst(loc.clone().add(0, 1, 0),
                new EffectUtils.Layer(Particle.SMOKE_NORMAL, 14, radius * 0.25, 0.4, radius * 0.25, 0.01),
                new EffectUtils.Layer(Particle.NOTE, 10, radius * 0.25, 0.4, radius * 0.25, 1.0));
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
            if (entity instanceof LivingEntity && !entity.equals(player)) {
                ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, (int) (duration * 20), 1));
            }
        }
        return true;
    }
}

class HarmonicShield extends Ability {
    private final ItemEditFull plugin;
    public HarmonicShield(ItemEditFull plugin) {
        super("harmonic_shield", "Harmonic Shield", "A shimmering wall of sound blocks the next incoming hit entirely.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 8.0);
        BardAbilities.activateShield(player.getUniqueId(), System.currentTimeMillis() + (long) (duration * 1000));
        EffectUtils.fanfare(player.getLocation(),
                new EffectUtils.SoundLayer(Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.4f),
                new EffectUtils.SoundLayer(Sound.BLOCK_AMETHYST_BLOCK_RESONATE, 0.5f, 1.3f));
        EffectUtils.ring(player.getLocation().clone().add(0, 1, 0), 1.0, 10,
                new EffectUtils.Layer(Particle.DUST, 1, 0, 0.05, 0, 0, new Particle.DustOptions(Color.fromRGB(80, 220, 255), 1.0f)));
        EffectUtils.burst(player.getLocation().add(0, 1, 0),
                new EffectUtils.Layer(Particle.NOTE, 15, 0.3, 0.4, 0.3, 1.0));
        return true;
    }
}

class CrescendoBurst extends Ability {
    private final ItemEditFull plugin;
    public CrescendoBurst(ItemEditFull plugin) {
        super("crescendo_burst", "Crescendo Burst", "Builds tension for a moment before releasing a powerful burst of sound damage.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double chargeSeconds = getDoubleParam(plugin, item, "charge", 1.5);
        double radius = getDoubleParam(plugin, item, "radius", 5.0);
        double damage = getDoubleParam(plugin, item, "damage", 8.0);
        EffectUtils.fanfare(player.getLocation(), new EffectUtils.SoundLayer(Sound.BLOCK_NOTE_BLOCK_HARP, 0.7f, 0.6f));
        EffectUtils.burst(player.getLocation().add(0, 1, 0),
                new EffectUtils.Layer(Particle.NOTE, 6, 0.2, 0.2, 0.2, 1.0));
        new CompatRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    return;
                }
                Location loc = player.getLocation();
                EffectUtils.fanfare(loc,
                        new EffectUtils.SoundLayer(Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1.3f, 1.2f),
                        new EffectUtils.SoundLayer(Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 0.6f));
                EffectUtils.burst(loc.add(0, 1, 0),
                        new EffectUtils.Layer(Particle.NOTE, 40, radius * 0.4, 0.5, radius * 0.4, 1.0),
                        new EffectUtils.Layer(Particle.DUST, 20, radius * 0.3, 0.5, radius * 0.3, 0, new Particle.DustOptions(Color.fromRGB(255, 150, 220), 1.3f)));
                for (Entity entity : loc.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
                    if (entity instanceof LivingEntity && !entity.equals(player)) {
                        ((LivingEntity) entity).damage(damage, player);
                    }
                }
            }
        }.runTaskLater(plugin, player, (long) (chargeSeconds * 20));
        return true;
    }
}

class MusicDiscThrow extends Ability {
    private final ItemEditFull plugin;
    public MusicDiscThrow(ItemEditFull plugin) {
        super("music_disc_throw", "Music Disc Throw", "Throws a spinning music disc that slices through enemies in a line.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double range = getDoubleParam(plugin, item, "range", 10.0);
        double damage = getDoubleParam(plugin, item, "damage", 6.0);
        Location origin = player.getEyeLocation();
        Vector dir = origin.getDirection().normalize();
        EffectUtils.fanfare(origin,
                new EffectUtils.SoundLayer(Sound.BLOCK_NOTE_BLOCK_XYLOPHONE, 1.0f, 1.3f),
                new EffectUtils.SoundLayer(Sound.BLOCK_NOTE_BLOCK_BASEDRUM, 0.5f, 1.6f));
        for (int i = 0; i < range; i++) {
            Location point = origin.clone().add(dir.clone().multiply(i));
            EffectUtils.burst(point,
                    new EffectUtils.Layer(Particle.NOTE, 1, 0, 0, 0, 1.0),
                    new EffectUtils.Layer(Particle.DUST, 1, 0.05, 0.05, 0.05, 0, new Particle.DustOptions(Color.fromRGB(255, 90, 200), 1.0f)));
            for (Entity entity : point.getWorld().getNearbyEntities(point, 1.0, 1.0, 1.0)) {
                if (entity instanceof LivingEntity && !entity.equals(player)) {
                    ((LivingEntity) entity).damage(damage, player);
                }
            }
        }
        return true;
    }
}

class SoundwavePush extends Ability {
    private final ItemEditFull plugin;
    public SoundwavePush(ItemEditFull plugin) {
        super("soundwave_push", "Soundwave Push", "A wave of sound that knocks back all nearby enemies.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double radius = getDoubleParam(plugin, item, "radius", 5.0);
        double knockback = getDoubleParam(plugin, item, "knockback", 1.3);
        Location loc = player.getLocation();
        loc.getWorld().playSound(loc, Sound.BLOCK_NOTE_BLOCK_BASS, 1.5f, 0.4f);
        loc.getWorld().spawnParticle(Particle.NOTE, loc.add(0, 1, 0), 25, radius * 0.3, 0.4, radius * 0.3, 1.0);
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
            if (entity instanceof LivingEntity && !entity.equals(player)) {
                LivingEntity living = (LivingEntity) entity;
                Vector away = VectorUtils.safeNormalize(living.getLocation().toVector().subtract(loc.toVector())).multiply(knockback).setY(0.3);
                living.setVelocity(away);
            }
        }
        return true;
    }
}
