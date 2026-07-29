package com.itemedit.full.ability.lava;

import com.itemedit.full.ItemEditFull;
import com.itemedit.full.ability.Ability;
import com.itemedit.full.utils.EffectUtils;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FireAura extends Ability implements Listener {
    private final ItemEditFull plugin;
    private final Map<UUID, AuraData> activeAuras = new java.util.concurrent.ConcurrentHashMap<>();

    private static class AuraData {
        final long expireTime;
        final int fireTicks;

        AuraData(long expireTime, int fireTicks) {
            this.expireTime = expireTime;
            this.fireTicks = fireTicks;
        }
    }

    public FireAura(ItemEditFull plugin) {
        super("fire_aura", "Fire Aura", "Grants Fire Resistance and burns attackers when you are damaged.");
        this.plugin = plugin;
    }

    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 15.0);
        int fireTicks = getIntParam(plugin, item, "fire_ticks", 60);

        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, (int) (duration * 20), 0));

        Location center = player.getLocation();

        // Ground ring of flame igniting around the player's feet as the cloak forms.
        EffectUtils.ring(center, 1.3, 20, new EffectUtils.Layer(Particle.FLAME, 1, 0.05, 0.1, 0.05, 0.01));
        EffectUtils.burst(center.clone().add(0, 1, 0),
                new EffectUtils.Layer(Particle.LAVA, 8, 0.4, 0.5, 0.4, 0.05),
                new EffectUtils.Layer(Particle.CRIT_MAGIC, 25, 0.5, 0.8, 0.5, 0.1));
        EffectUtils.fanfare(center,
                new EffectUtils.SoundLayer(Sound.ITEM_ARMOR_EQUIP_NETHERITE, 1.0f, 0.8f),
                new EffectUtils.SoundLayer(Sound.BLOCK_FIRE_AMBIENT, 1.0f, 1.0f),
                new EffectUtils.SoundLayer(Sound.ENTITY_BLAZE_AMBIENT, 0.6f, 1.2f));

        // Cloak of flame spiraling up and around the player as the aura settles in.
        EffectUtils.spiral(plugin, player, center.clone(), 1.0, 18, Math.PI / 4.5, 0.12, 2L,
                new EffectUtils.Layer(Particle.FLAME, 3, 0.05, 0.05, 0.05, 0.01));

        activeAuras.put(player.getUniqueId(), new AuraData(
                System.currentTimeMillis() + (long) (duration * 1000),
                fireTicks
        ));
        player.sendMessage("§6Fire Aura activated!");
        return true;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        AuraData data = activeAuras.get(player.getUniqueId());
        if (data == null) {
            return;
        }

        if (System.currentTimeMillis() > data.expireTime) {
            activeAuras.remove(player.getUniqueId());
            return;
        }

        if (event.getDamager() instanceof LivingEntity) {
            LivingEntity attacker = (LivingEntity) event.getDamager();
            attacker.setFireTicks(data.fireTicks);
            Location retaliationLoc = attacker.getLocation().add(0, 1, 0);
            EffectUtils.burst(retaliationLoc,
                    new EffectUtils.Layer(Particle.FLAME, 12, 0.3, 0.4, 0.3, 0.05),
                    new EffectUtils.Layer(Particle.LAVA, 3, 0.2, 0.3, 0.2, 0.02));
            EffectUtils.fanfare(retaliationLoc,
                    new EffectUtils.SoundLayer(Sound.ITEM_FIRECHARGE_USE, 1.0f, 1.0f),
                    new EffectUtils.SoundLayer(Sound.ENTITY_BLAZE_HURT, 0.8f, 1.1f));
        }
    }

    @EventHandler
    public void onQuit(org.bukkit.event.player.PlayerQuitEvent event) {
        activeAuras.remove(event.getPlayer().getUniqueId());
    }
}
