package com.itemedit.full.ability.lava;

import com.itemedit.full.ItemEditFull;
import com.itemedit.full.ability.Ability;
import com.itemedit.full.utils.EffectUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class LavaAbsorption extends Ability {
    private final ItemEditFull plugin;

    public LavaAbsorption(ItemEditFull plugin) {
        super("lava_absorption", "Lava Absorption", "Absorbs nearby lava to heal, restore hunger, and grant fire resistance.");
        this.plugin = plugin;
    }

    @Override
    public boolean trigger(Player player, ItemStack item) {
        int radius = getIntParam(plugin, item, "radius", 4);
        int hungerHeal = getIntParam(plugin, item, "hunger_heal", 6);
        double healthHeal = getDoubleParam(plugin, item, "health_heal", 4.0);
        double duration = getDoubleParam(plugin, item, "duration", 10.0);

        Location loc = player.getLocation();
        int convertedCount = 0;

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x * x + y * y + z * z <= radius * radius) {
                        Block block = loc.clone().add(x, y, z).getBlock();
                        if (block.getType() == Material.LAVA) {
                            if (block.getBlockData() instanceof Levelled) {
                                Levelled levelled = (Levelled) block.getBlockData();
                                if (levelled.getLevel() == 0) {
                                    block.setType(Material.OBSIDIAN);
                                } else {
                                    block.setType(Material.COBBLESTONE);
                                }
                            } else {
                                block.setType(Material.OBSIDIAN);
                            }
                            convertedCount++;
                        }
                    }
                }
            }
        }

        if (convertedCount == 0) {
            player.sendMessage("§cNo lava nearby to absorb!");
            return false;
        }

        // Heal & Feed
        player.setFoodLevel(Math.min(20, player.getFoodLevel() + hungerHeal));
        double maxHealth = player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
        player.setHealth(Math.min(maxHealth, player.getHealth() + healthHeal));

        // Add Fire Resistance
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, (int) (duration * 20), 0));

        // Molten light draws inward from the absorbed lava toward the player in shrinking rings,
        // then vents off as a plume of steam once it reaches them - visually distinct "inhale" shape.
        double drawRadius = Math.max(2.0, radius);
        int pulses = 4;
        EffectUtils.staggered(plugin, player, pulses, 3L, index -> {
            double r = drawRadius * (1.0 - ((double) index / pulses));
            EffectUtils.ring(loc.clone().add(0, 0.2, 0), Math.max(0.3, r), 14,
                    new EffectUtils.Layer(Particle.LAVA, 1, 0.05, 0.05, 0.05, 0.01));
        });
        EffectUtils.burst(player.getLocation().add(0, 1, 0),
                new EffectUtils.Layer(Particle.LAVA, 10, 0.4, 0.5, 0.4, 0.05),
                new EffectUtils.Layer(Particle.CLOUD, 18, 0.5, 0.6, 0.5, 0.08));
        EffectUtils.fanfare(player.getLocation(),
                new EffectUtils.SoundLayer(Sound.BLOCK_LAVA_EXTINGUISH, 1.0f, 1.0f),
                new EffectUtils.SoundLayer(Sound.ENTITY_PLAYER_BURP, 1.0f, 1.0f),
                new EffectUtils.SoundLayer(Sound.BLOCK_FIRE_EXTINGUISH, 0.8f, 1.2f));

        player.sendMessage("§6Absorbed " + convertedCount + " lava blocks!");
        return true;
    }
}
