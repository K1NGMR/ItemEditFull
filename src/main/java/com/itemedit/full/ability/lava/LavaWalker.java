package com.itemedit.full.ability.lava;

import com.itemedit.full.ItemEditFull;
import com.itemedit.full.ability.Ability;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Particle;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import com.itemedit.full.utils.CompatRunnable;
import com.itemedit.full.utils.EffectUtils;

import java.util.HashSet;
import java.util.Set;

public class LavaWalker extends Ability {
    private final ItemEditFull plugin;
    private final Set<Location> modifiedBlocks = java.util.concurrent.ConcurrentHashMap.newKeySet();

    public LavaWalker(ItemEditFull plugin) {
        super("lava_walker", "Lava Walker", "Allows you to walk on lava by turning it to obsidian/magma.");
        this.plugin = plugin;
    }

    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 10.0);
        int radius = getIntParam(plugin, item, "radius", 3);
        double decayDuration = getDoubleParam(plugin, item, "decay_duration", 4.0);

        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, (int) (duration * 20), 0));

        // Crust of cooling stone rippling outward from the player's feet as the ability engages.
        EffectUtils.ring(player.getLocation(), Math.max(1.0, radius * 0.6), 16,
                new EffectUtils.Layer(Particle.SMOKE_NORMAL, 1, 0.05, 0.05, 0.05, 0.01));
        EffectUtils.fanfare(player.getLocation(),
                new EffectUtils.SoundLayer(Sound.ITEM_FIRECHARGE_USE, 1.0f, 0.8f),
                new EffectUtils.SoundLayer(Sound.BLOCK_LAVA_POP, 0.8f, 1.2f));

        new CompatRunnable() {
            int ticksElapsed = 0;

            @Override
            public void run() {
                if (!player.isOnline() || ticksElapsed >= (duration * 20)) {
                    cancel();
                    return;
                }

                Location loc = player.getLocation().subtract(0, 1, 0);
                for (int x = -radius; x <= radius; x++) {
                    for (int z = -radius; z <= radius; z++) {
                        if (x * x + z * z <= radius * radius) {
                            Block block = loc.clone().add(x, 0, z).getBlock();
                            if (block.getType() == Material.LAVA) {
                                Material originalType = block.getType();
                                Location blockLoc = block.getLocation();

                                if (!modifiedBlocks.contains(blockLoc)) {
                                    modifiedBlocks.add(blockLoc);
                                    block.setType(Material.MAGMA_BLOCK);

                                    // Quick hiss of crust forming as each lava block solidifies underfoot.
                                    EffectUtils.burst(blockLoc.clone().add(0.5, 0.6, 0.5),
                                            new EffectUtils.Layer(Particle.SMOKE_NORMAL, 3, 0.15, 0.1, 0.15, 0.01));

                                    new CompatRunnable() {
                                        @Override
                                        public void run() {
                                            if (blockLoc.getBlock().getType() == Material.MAGMA_BLOCK) {
                                                blockLoc.getBlock().setType(originalType);
                                                EffectUtils.burst(blockLoc.clone().add(0.5, 0.6, 0.5),
                                                        new EffectUtils.Layer(Particle.LAVA, 4, 0.2, 0.1, 0.2, 0.02));
                                            }
                                            modifiedBlocks.remove(blockLoc);
                                        }
                                    }.runTaskLater(plugin, blockLoc, (long) (decayDuration * 20));
                                }
                            }
                        }
                    }
                }
                ticksElapsed += 2;
            }
        }.runTaskTimer(plugin, player, 0L, 2L);

        return true;
    }
}
