package com.itemedit.full.ability.lava;

import com.itemedit.full.ItemEditFull;
import com.itemedit.full.ability.Ability;
import com.itemedit.full.utils.CompatRunnable;
import com.itemedit.full.utils.EffectUtils;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.SmallFireball;
import org.bukkit.inventory.ItemStack;

public class LavaSpit extends Ability {
    private final ItemEditFull plugin;

    public LavaSpit(ItemEditFull plugin) {
        super("lava_spit", "Lava Spit", "Launches a fireball that burns enemies.");
        this.plugin = plugin;
    }

    @Override
    public boolean trigger(Player player, ItemStack item) {
        double yield = getDoubleParam(plugin, item, "yield", 1.0);
        boolean incendiary = getBooleanParam(plugin, item, "incendiary", true);

        Location muzzle = player.getEyeLocation();
        // Windup: a gob of molten rock spat from the mouth before the fireball leaves.
        EffectUtils.burst(muzzle,
                new EffectUtils.Layer(Particle.LAVA, 6, 0.15, 0.15, 0.15, 0.02),
                new EffectUtils.Layer(Particle.CRIT_MAGIC, 10, 0.2, 0.2, 0.2, 0.05));
        EffectUtils.fanfare(muzzle,
                new EffectUtils.SoundLayer(Sound.ENTITY_GHAST_SHOOT, 1.0f, 1.0f),
                new EffectUtils.SoundLayer(Sound.ENTITY_BLAZE_SHOOT, 0.6f, 1.3f));

        SmallFireball fireball = player.launchProjectile(SmallFireball.class);
        fireball.setYield((float) yield);
        fireball.setIsIncendiary(incendiary);

        // Trailing embers follow the glob in flight until it dies or times out.
        new CompatRunnable() {
            int ticks = 0;
            Location lastLoc = fireball.getLocation();

            @Override
            public void run() {
                if (ticks++ > 100 || fireball.isDead() || !fireball.isValid()) {
                    EffectUtils.burst(lastLoc,
                            new EffectUtils.Layer(Particle.LAVA, 8, 0.25, 0.25, 0.25, 0.04),
                            new EffectUtils.Layer(Particle.ASH, 12, 0.3, 0.3, 0.3, 0.03));
                    this.cancel();
                    return;
                }
                lastLoc = fireball.getLocation();
                EffectUtils.burst(lastLoc,
                        new EffectUtils.Layer(Particle.LAVA, 1, 0.05, 0.05, 0.05, 0.0),
                        new EffectUtils.Layer(Particle.SMOKE_NORMAL, 1, 0.03, 0.03, 0.03, 0.0));
            }
        }.runTaskTimer(plugin, player, 1L, 1L);

        return true;
    }
}
