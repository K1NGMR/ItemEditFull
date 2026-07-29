package com.itemedit.full.utils;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared library of layered/animated particle+sound primitives, extracted from the
 * patterns that made GalaxyMeteorStrikeAbility visually stand out (layered particle
 * stacks, staggered timing, shape-based patterning, cast fanfare + impact combo sounds)
 * so every ability can reach that bar without hand-rolling its own CompatRunnable.
 */
public final class EffectUtils {
    private EffectUtils() {}

    /** One particle call: type + count + spread box + extra (speed/size depending on particle). */
    public static final class Layer {
        public final Particle particle;
        public final int count;
        public final double dx, dy, dz;
        public final double extra;
        public final Object data;

        public Layer(Particle particle, int count, double dx, double dy, double dz, double extra) {
            this(particle, count, dx, dy, dz, extra, null);
        }

        public Layer(Particle particle, int count, double dx, double dy, double dz, double extra, Object data) {
            this.particle = particle;
            this.count = count;
            this.dx = dx;
            this.dy = dy;
            this.dz = dz;
            this.extra = extra;
            this.data = data;
        }

        public void spawn(Location loc) {
            World w = loc.getWorld();
            if (w == null) return;
            if (data != null) {
                w.spawnParticle(particle, loc, count, dx, dy, dz, extra, data);
            } else {
                w.spawnParticle(particle, loc, count, dx, dy, dz, extra);
            }
        }
    }

    public static List<Layer> layers(Layer... layers) {
        List<Layer> list = new ArrayList<>();
        for (Layer l : layers) list.add(l);
        return list;
    }

    /** Spawns every layer stacked at one location - the "layered burst" pattern from Galaxy Meteor Strike's impact. */
    public static void burst(Location loc, List<Layer> layers) {
        for (Layer l : layers) l.spawn(loc);
    }

    public static void burst(Location loc, Layer... layers) {
        for (Layer l : layers) l.spawn(loc);
    }

    /** Plays several sounds together at once - the "cast fanfare" pattern. */
    public static void fanfare(Location loc, SoundLayer... sounds) {
        World w = loc.getWorld();
        if (w == null) return;
        for (SoundLayer s : sounds) {
            w.playSound(loc, s.sound, s.volume, s.pitch);
        }
    }

    public static final class SoundLayer {
        public final Sound sound;
        public final float volume, pitch;
        public SoundLayer(Sound sound, float volume, float pitch) {
            this.sound = sound;
            this.volume = volume;
            this.pitch = pitch;
        }
    }

    /** Points evenly spaced around a horizontal circle centered on {@code center} at the given radius. */
    public static List<Location> circle(Location center, double radius, int points) {
        List<Location> out = new ArrayList<>(points);
        for (int i = 0; i < points; i++) {
            double angle = (2 * Math.PI * i) / points;
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            out.add(new Location(center.getWorld(), x, center.getY(), z));
        }
        return out;
    }

    /** Draws a static ring of particles instantly (one layer at every point on the circle). */
    public static void ring(Location center, double radius, int points, Layer layer) {
        for (Location loc : circle(center, radius, points)) {
            layer.spawn(loc);
        }
    }

    /**
     * Animates an expanding/rotating/rising spiral of particles over {@code steps} ticks -
     * useful for cast-charge buildups or ground-to-sky beams. Each step advances the angle by
     * {@code angleStepRad} and the height by {@code riseStep}.
     */
    public static void spiral(Plugin plugin, Entity anchor, Location center, double radius, int steps,
                               double angleStepRad, double riseStep, long tickInterval, Layer layer) {
        new CompatRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick >= steps || anchor == null || !anchor.isValid()) { this.cancel(); return; }
                double angle = angleStepRad * tick;
                double x = center.getX() + radius * Math.cos(angle);
                double z = center.getZ() + radius * Math.sin(angle);
                double y = center.getY() + riseStep * tick;
                Location point = new Location(center.getWorld(), x, y, z);
                layer.spawn(point);
                tick++;
            }
        }.runTaskTimer(plugin, anchor, 0L, tickInterval);
    }

    /**
     * Generic falling-projectile trail: ticks a point from {@code start} toward {@code impact}
     * at {@code speed} blocks/tick, spawning {@code trail} layers every tick, and firing
     * {@code onImpact} once it reaches the target or hits a solid block. Mirrors the meteor
     * pattern used by GalaxyMeteorStrikeAbility but is reusable for any "thing falls/flies and
     * explodes" ability (comets, bombs, divine judgement beams, etc).
     */
    public static void fallingTrail(Plugin plugin, Player caster, Location start, Location impact,
                                     double speed, int maxDurationTicks, List<Layer> trail, Runnable onImpact) {
        new CompatRunnable() {
            int ticks = 0;
            final Location current = start.clone();
            final Vector dir = VectorUtils.safeNormalize(impact.clone().toVector().subtract(start.toVector())).multiply(speed);

            @Override public void run() {
                if (ticks++ > maxDurationTicks || caster == null || !caster.isOnline()) { this.cancel(); return; }
                current.add(dir);
                for (Layer l : trail) l.spawn(current);
                boolean solidHit = current.getBlock().getType().isSolid();
                boolean reachedImpact = current.getY() <= impact.getY();
                if (solidHit || reachedImpact) {
                    onImpact.run();
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, caster, 0L, 1L);
    }

    /** Impact combo: real explosion + layered particle burst + sound, all in one call. */
    public static void impactCombo(Location loc, float explosionYield, boolean setFire, boolean breakBlocks,
                                    List<Layer> burstLayers, SoundLayer sound) {
        World w = loc.getWorld();
        if (w == null) return;
        if (explosionYield > 0f) {
            w.createExplosion(loc, explosionYield, setFire, breakBlocks);
        }
        burst(loc, burstLayers);
        if (sound != null) {
            w.playSound(loc, sound.sound, sound.volume, sound.pitch);
        }
    }

    /** Pushes a living entity away from (or toward, with a negative multiplier) a center point. */
    public static void knockback(LivingEntity entity, Location center, double horizontalMultiplier, double verticalBoost) {
        Vector push = VectorUtils.safeNormalize(entity.getLocation().toVector().subtract(center.toVector()))
                .multiply(horizontalMultiplier).setY(verticalBoost);
        entity.setVelocity(entity.getVelocity().add(push));
    }

    /** Random horizontal offset, matching the scatter pattern used for meteor spawn/impact jitter. */
    public static Location jitterXZ(Location base, double spread) {
        double ox = (Math.random() - 0.5) * spread;
        double oz = (Math.random() - 0.5) * spread;
        return base.clone().add(ox, 0, oz);
    }

    /**
     * Runs {@code count} staggered pulses of {@code action}, each delayed by {@code index * delayTicks} -
     * the "staggered multi-meteor" timing pattern, generalized for any repeated timed effect
     * (barrages, summon waves, chained strikes).
     */
    public static void staggered(Plugin plugin, Entity anchor, int count, long delayTicks, java.util.function.IntConsumer action) {
        for (int i = 0; i < count; i++) {
            final int index = i;
            new CompatRunnable() {
                @Override public void run() {
                    action.accept(index);
                }
            }.runTaskLater(plugin, anchor, index * delayTicks);
        }
    }
}
