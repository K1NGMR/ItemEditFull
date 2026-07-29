package com.itemedit.full.ability.lava;

import com.itemedit.full.ItemEditFull;
import com.itemedit.full.ability.Ability;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import com.itemedit.full.utils.CompatRunnable;
import com.itemedit.full.utils.EffectUtils;

public class LavaPour extends Ability {
    private final ItemEditFull plugin;

    public LavaPour(ItemEditFull plugin) {
        super("lava_pour", "Lava Pour", "Places a temporary lava block that disappears after a few seconds.");
        this.plugin = plugin;
    }

    @Override
    public boolean trigger(Player player, ItemStack item) {
        Block targetBlock = player.getTargetBlockExact(5);
        if (targetBlock == null) {
            player.sendMessage("§cNo block in range to place lava.");
            return false;
        }

        Block lavaBlock = targetBlock.getRelative(org.bukkit.block.BlockFace.UP);
        if (lavaBlock.getType() != Material.AIR) {
            player.sendMessage("§cCannot place lava here.");
            return false;
        }

        double duration = getDoubleParam(plugin, item, "duration", 5.0);

        Material originalMaterial = lavaBlock.getType();
        lavaBlock.setType(Material.LAVA);

        Location pourLoc = lavaBlock.getLocation().add(0.5, 0.3, 0.5);
        EffectUtils.burst(pourLoc,
                new EffectUtils.Layer(Particle.LAVA, 10, 0.3, 0.2, 0.3, 0.04),
                new EffectUtils.Layer(Particle.FLAME, 14, 0.35, 0.25, 0.35, 0.03),
                new EffectUtils.Layer(Particle.SMOKE_NORMAL, 10, 0.3, 0.3, 0.3, 0.02));
        EffectUtils.fanfare(pourLoc,
                new EffectUtils.SoundLayer(Sound.ITEM_BUCKET_EMPTY_LAVA, 1.0f, 1.0f),
                new EffectUtils.SoundLayer(Sound.BLOCK_LAVA_POP, 1.0f, 1.1f));

        new CompatRunnable() {
            @Override
            public void run() {
                if (lavaBlock.getType() == Material.LAVA) {
                    lavaBlock.setType(originalMaterial);
                    Location coolLoc = lavaBlock.getLocation().add(0.5, 0.3, 0.5);
                    EffectUtils.burst(coolLoc,
                            new EffectUtils.Layer(Particle.SMOKE_NORMAL, 16, 0.3, 0.3, 0.3, 0.03),
                            new EffectUtils.Layer(Particle.CLOUD, 10, 0.3, 0.2, 0.3, 0.02));
                    EffectUtils.fanfare(coolLoc,
                            new EffectUtils.SoundLayer(Sound.ITEM_BUCKET_FILL_LAVA, 1.0f, 1.0f),
                            new EffectUtils.SoundLayer(Sound.BLOCK_FIRE_EXTINGUISH, 0.8f, 1.3f));
                }
            }
        }.runTaskLater(plugin, lavaBlock.getLocation(), (long) (duration * 20));

        return true;
    }
}
