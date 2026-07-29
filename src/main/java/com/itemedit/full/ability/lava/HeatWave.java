package com.itemedit.full.ability.lava;

import com.itemedit.full.ItemEditFull;
import com.itemedit.full.ability.Ability;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import com.itemedit.full.utils.CompatRunnable;
import com.itemedit.full.utils.EffectUtils;

public class HeatWave extends Ability {
    private final ItemEditFull plugin;

    public HeatWave(ItemEditFull plugin) {
        super("heat_wave", "Heat Wave", "Releases an expanding ring of fire that burns nearby enemies.");
        this.plugin = plugin;
    }

    @Override
    public boolean trigger(Player player, ItemStack item) {
        double maxRadius = getDoubleParam(plugin, item, "radius", 5.0);
        double damage = getDoubleParam(plugin, item, "damage", 4.0);
        int fireTicks = getIntParam(plugin, item, "fire_ticks", 80);

        Location origin = player.getLocation();
        EffectUtils.burst(origin.clone().add(0, 0.2, 0),
                new EffectUtils.Layer(Particle.EXPLOSION_LARGE, 1, 0.0, 0.0, 0.0, 0.0),
                new EffectUtils.Layer(Particle.LAVA, 10, 0.3, 0.1, 0.3, 0.05));
        EffectUtils.fanfare(origin,
                new EffectUtils.SoundLayer(Sound.ENTITY_BLAZE_SHOOT, 1.2f, 0.8f),
                new EffectUtils.SoundLayer(Sound.BLOCK_FIRE_AMBIENT, 1.0f, 0.6f));

        final java.util.Set<java.util.UUID> hitEntities = new java.util.HashSet<>();
        new CompatRunnable() {
            double currentRadius = 1.0;

            @Override
            public void run() {
                if (currentRadius > maxRadius) {
                    // Final flare as the shockwave dissipates at its outer edge.
                    EffectUtils.ring(origin, maxRadius, (int) (maxRadius * 10),
                            new EffectUtils.Layer(Particle.ASH, 1, 0.05, 0.1, 0.05, 0.01));
                    cancel();
                    return;
                }

                // Layered ring: flame core, trailing smoke, and molten embers scattered behind it.
                EffectUtils.ring(origin.clone().add(0, 0.5, 0), currentRadius, (int) (currentRadius * 8),
                        new EffectUtils.Layer(Particle.FLAME, 1, 0, 0, 0, 0.02));
                EffectUtils.ring(origin.clone().add(0, 0.5, 0), Math.max(0.2, currentRadius - 0.5), (int) (currentRadius * 5),
                        new EffectUtils.Layer(Particle.SMOKE_NORMAL, 1, 0, 0.05, 0, 0.01));
                if ((int) currentRadius % 2 == 0) {
                    origin.getWorld().playSound(origin, Sound.BLOCK_LAVA_POP, 0.6f, 1.0f + (float) (currentRadius * 0.05));
                }

                for (Entity entity : origin.getWorld().getNearbyEntities(origin, currentRadius, 2.0, currentRadius)) {
                    if (entity instanceof LivingEntity && !entity.equals(player)) {
                        LivingEntity living = (LivingEntity) entity;
                        double distance = living.getLocation().distance(origin);
                        // Only damage each entity once as the ring passes over it.
                        if (Math.abs(distance - currentRadius) <= 1.0 && hitEntities.add(living.getUniqueId())) {
                            living.damage(damage, player);
                            living.setFireTicks(fireTicks);
                            EffectUtils.burst(living.getLocation().add(0, 1, 0),
                                    new EffectUtils.Layer(Particle.FLAME, 10, 0.3, 0.4, 0.3, 0.04),
                                    new EffectUtils.Layer(Particle.ASH, 6, 0.2, 0.3, 0.2, 0.02));
                        }
                    }
                }

                currentRadius += 1.0;
            }
        }.runTaskTimer(plugin, player, 0L, 2L);

        return true;
    }
}
