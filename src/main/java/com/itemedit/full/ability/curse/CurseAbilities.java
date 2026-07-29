package com.itemedit.full.ability.curse;

import com.itemedit.full.ItemEditFull;
import com.itemedit.full.ability.Ability;
import com.itemedit.full.utils.CompatRunnable;
import com.itemedit.full.utils.EffectUtils;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Hex Ledger: 10 curse/debuff-themed abilities (marks, binds, and debts paid
 * in blood) - a brand-new school with no prior abilities.
 */
public class CurseAbilities implements Listener {

    // ---- Hex of Fragility: multiplies all damage taken while active ----
    private static final Map<UUID, Long> fragilityActive = new ConcurrentHashMap<>();
    private static final Map<UUID, Double> fragilityMultiplier = new ConcurrentHashMap<>();

    // ---- Plague Hex: poisons whoever strikes the marked target ----
    private static final Map<UUID, Long> plagueActive = new ConcurrentHashMap<>();

    private static ItemEditFull pluginInstance;

    public static void register(ItemEditFull plugin) {
        pluginInstance = plugin;

        plugin.getAbilityManager().registerAbility(new HexOfWeakness(plugin));
        plugin.getAbilityManager().registerAbility(new DoomMark(plugin));
        plugin.getAbilityManager().registerAbility(new SoulShackle(plugin));
        plugin.getAbilityManager().registerAbility(new WitheringGaze(plugin));
        plugin.getAbilityManager().registerAbility(new CurseOfSilence(plugin));
        plugin.getAbilityManager().registerAbility(new UnluckyOmen(plugin));
        plugin.getAbilityManager().registerAbility(new PlagueHex(plugin));
        plugin.getAbilityManager().registerAbility(new HexOfFragility(plugin));
        plugin.getAbilityManager().registerAbility(new NightmareWhisper(plugin));
        plugin.getAbilityManager().registerAbility(new SacrificialPact(plugin));

        plugin.getServer().getPluginManager().registerEvents(new CurseAbilities(), plugin);
    }

    static void markFragility(UUID targetUuid, long expireAt, double multiplier) {
        fragilityActive.put(targetUuid, expireAt);
        fragilityMultiplier.put(targetUuid, multiplier);
    }

    static void markPlague(UUID targetUuid, long expireAt) {
        plagueActive.put(targetUuid, expireAt);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onFragilityDamage(EntityDamageEvent event) {
        UUID id = event.getEntity().getUniqueId();
        Long expire = fragilityActive.get(id);
        if (expire == null) {
            return;
        }
        if (System.currentTimeMillis() >= expire) {
            fragilityActive.remove(id);
            fragilityMultiplier.remove(id);
            return;
        }
        double multiplier = fragilityMultiplier.getOrDefault(id, 1.25);
        event.setDamage(event.getDamage() * multiplier);
    }

    @EventHandler
    public void onPlagueHit(EntityDamageByEntityEvent event) {
        UUID victimId = event.getEntity().getUniqueId();
        Long expire = plagueActive.get(victimId);
        if (expire == null) {
            return;
        }
        if (System.currentTimeMillis() >= expire) {
            plagueActive.remove(victimId);
            return;
        }
        if (event.getDamager() instanceof LivingEntity) {
            LivingEntity attacker = (LivingEntity) event.getDamager();
            attacker.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 60, 0));
            attacker.getWorld().playSound(attacker.getLocation(), Sound.ENTITY_WITCH_HURT, 1.0f, 0.8f);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        fragilityActive.remove(id);
        fragilityMultiplier.remove(id);
        plagueActive.remove(id);
    }
}

// ==================== 10 HEX LEDGER ABILITIES ====================

class HexOfWeakness extends Ability {
    private final ItemEditFull plugin;
    public HexOfWeakness(ItemEditFull plugin) {
        super("hex_of_weakness", "Hex of Weakness", "Curses a target, sapping their strength for a time.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 8.0);
        Entity target = player.getTargetEntity(8);
        if (!(target instanceof LivingEntity)) {
            player.sendMessage("§cNo target in sight.");
            return false;
        }
        LivingEntity living = (LivingEntity) target;
        living.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, (int) (duration * 20), 1));
        EffectUtils.fanfare(living.getLocation(),
                new EffectUtils.SoundLayer(Sound.ENTITY_WITCH_CELEBRATE, 1.0f, 0.6f),
                new EffectUtils.SoundLayer(Sound.ENTITY_WITCH_AMBIENT, 0.6f, 0.5f));
        EffectUtils.burst(living.getLocation().add(0, 1, 0),
                new EffectUtils.Layer(Particle.SPELL_WITCH, 12, 0.3, 0.4, 0.3, 0),
                new EffectUtils.Layer(Particle.DUST, 10, 0.3, 0.4, 0.3, 0, new Particle.DustOptions(Color.fromRGB(60, 0, 90), 1.1f)));
        return true;
    }
}

class DoomMark extends Ability {
    private final ItemEditFull plugin;
    public DoomMark(ItemEditFull plugin) {
        super("doom_mark", "Doom Mark", "Marks a target with a curse that detonates for heavy damage after a delay.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double delay = getDoubleParam(plugin, item, "delay", 4.0);
        double damage = getDoubleParam(plugin, item, "damage", 8.0);
        Entity target = player.getTargetEntity(10);
        if (!(target instanceof LivingEntity)) {
            player.sendMessage("§cNo target in sight.");
            return false;
        }
        LivingEntity living = (LivingEntity) target;
        UUID targetId = living.getUniqueId();
        EffectUtils.fanfare(living.getLocation(), new EffectUtils.SoundLayer(Sound.ENTITY_WITCH_AMBIENT, 1.0f, 0.6f));
        EffectUtils.burst(living.getLocation().add(0, 1, 0),
                new EffectUtils.Layer(Particle.SMOKE_NORMAL, 10, 0.2, 0.3, 0.2, 0.01),
                new EffectUtils.Layer(Particle.DUST, 8, 0.2, 0.3, 0.2, 0, new Particle.DustOptions(Color.fromRGB(20, 0, 20), 1.2f)));
        new CompatRunnable() {
            @Override
            public void run() {
                Entity refreshed = plugin.getServer().getEntity(targetId);
                if (refreshed instanceof LivingEntity && refreshed.isValid()) {
                    ((LivingEntity) refreshed).damage(damage, player);
                    EffectUtils.fanfare(refreshed.getLocation(),
                            new EffectUtils.SoundLayer(Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.2f),
                            new EffectUtils.SoundLayer(Sound.ENTITY_WITCH_HURT, 0.8f, 0.6f));
                    EffectUtils.burst(refreshed.getLocation().add(0, 1, 0),
                            new EffectUtils.Layer(Particle.SMOKE_LARGE, 25, 0.3, 0.4, 0.3, 0.05),
                            new EffectUtils.Layer(Particle.DUST, 16, 0.3, 0.5, 0.3, 0, new Particle.DustOptions(Color.fromRGB(40, 0, 60), 1.4f)));
                }
            }
        }.runTaskLater(plugin, player, (long) (delay * 20));
        return true;
    }
}

class SoulShackle extends Ability {
    private final ItemEditFull plugin;
    public SoulShackle(ItemEditFull plugin) {
        super("soul_shackle", "Soul Shackle", "Binds a target's soul, rooting and slowly draining their health.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 5.0);
        double drainPerTick = getDoubleParam(plugin, item, "drain_tick", 1.0);
        Entity target = player.getTargetEntity(8);
        if (!(target instanceof LivingEntity)) {
            player.sendMessage("§cNo target in sight.");
            return false;
        }
        LivingEntity living = (LivingEntity) target;
        living.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) (duration * 20) + 10, 6));
        EffectUtils.fanfare(living.getLocation(),
                new EffectUtils.SoundLayer(Sound.BLOCK_CHAIN_PLACE, 1.0f, 0.6f),
                new EffectUtils.SoundLayer(Sound.ENTITY_WITCH_AMBIENT, 0.6f, 0.4f));
        EffectUtils.burst(living.getLocation().add(0, 1, 0),
                new EffectUtils.Layer(Particle.SOUL, 10, 0.3, 0.4, 0.3, 0.02),
                new EffectUtils.Layer(Particle.DUST, 8, 0.3, 0.4, 0.3, 0, new Particle.DustOptions(Color.fromRGB(30, 0, 40), 1.1f)));
        new CompatRunnable() {
            int ticksElapsed = 0;
            @Override
            public void run() {
                if (!living.isValid() || living.isDead() || ticksElapsed >= (duration * 20)) {
                    cancel();
                    return;
                }
                living.damage(drainPerTick, player);
                EffectUtils.burst(living.getLocation().add(0, 1, 0),
                        new EffectUtils.Layer(Particle.SOUL, 4, 0.2, 0.3, 0.2, 0.01),
                        new EffectUtils.Layer(Particle.CRIT_MAGIC, 3, 0.2, 0.3, 0.2, 0));
                ticksElapsed += 20;
            }
        }.runTaskTimer(plugin, living, 20L, 20L);
        return true;
    }
}

class WitheringGaze extends Ability {
    private final ItemEditFull plugin;
    public WitheringGaze(ItemEditFull plugin) {
        super("withering_gaze", "Withering Gaze", "A cursed gaze that applies wither to anyone who meets your eyes.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double range = getDoubleParam(plugin, item, "range", 10.0);
        double duration = getDoubleParam(plugin, item, "duration", 5.0);
        Entity target = player.getTargetEntity((int) range);
        if (!(target instanceof LivingEntity)) {
            player.sendMessage("§cNo target in sight.");
            return false;
        }
        LivingEntity living = (LivingEntity) target;
        living.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, (int) (duration * 20), 0));
        EffectUtils.fanfare(living.getLocation(),
                new EffectUtils.SoundLayer(Sound.ENTITY_WITHER_AMBIENT, 0.7f, 0.7f),
                new EffectUtils.SoundLayer(Sound.ENTITY_WITCH_CELEBRATE, 0.5f, 0.5f));
        EffectUtils.burst(living.getLocation().add(0, 1.6, 0),
                new EffectUtils.Layer(Particle.SMOKE_LARGE, 14, 0.25, 0.3, 0.25, 0.01),
                new EffectUtils.Layer(Particle.DUST, 10, 0.25, 0.3, 0.25, 0, new Particle.DustOptions(Color.fromRGB(20, 20, 20), 1.3f)));
        return true;
    }
}

class CurseOfSilence extends Ability {
    private final ItemEditFull plugin;
    public CurseOfSilence(ItemEditFull plugin) {
        super("curse_of_silence", "Curse of Silence", "Prevents the target from using item abilities for a short time.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 4.0);
        double range = getDoubleParam(plugin, item, "range", 8.0);
        Entity target = player.getTargetEntity((int) range);
        if (!(target instanceof Player)) {
            player.sendMessage("§cNo player target in sight to silence.");
            return false;
        }
        Player targetPlayer = (Player) target;
        plugin.getAbilityManager().silencePlayer(targetPlayer, (long) (duration * 1000));
        EffectUtils.fanfare(targetPlayer.getLocation(),
                new EffectUtils.SoundLayer(Sound.ENTITY_WITCH_CELEBRATE, 0.8f, 1.4f),
                new EffectUtils.SoundLayer(Sound.BLOCK_CONDUIT_DEACTIVATE, 0.6f, 1.2f));
        EffectUtils.burst(targetPlayer.getLocation().add(0, 1.5, 0),
                new EffectUtils.Layer(Particle.SPELL_MOB, 15, 0.3, 0.4, 0.3, 0),
                new EffectUtils.Layer(Particle.DUST, 8, 0.3, 0.4, 0.3, 0, new Particle.DustOptions(Color.fromRGB(50, 0, 70), 1.0f)));
        targetPlayer.sendMessage("§5A curse silences your abilities!");
        return true;
    }
}

class UnluckyOmen extends Ability {
    private final ItemEditFull plugin;
    public UnluckyOmen(ItemEditFull plugin) {
        super("unlucky_omen", "Unlucky Omen", "Curses a target with bad luck, reducing their loot and hit chance.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 10.0);
        Entity target = player.getTargetEntity(8);
        if (!(target instanceof Player)) {
            player.sendMessage("§cNo player target in sight.");
            return false;
        }
        Player targetPlayer = (Player) target;
        targetPlayer.addPotionEffect(new PotionEffect(PotionEffectType.UNLUCK, (int) (duration * 20), 0));
        EffectUtils.fanfare(targetPlayer.getLocation(),
                new EffectUtils.SoundLayer(Sound.ENTITY_VILLAGER_NO, 1.0f, 0.7f),
                new EffectUtils.SoundLayer(Sound.BLOCK_GLASS_BREAK, 0.5f, 0.5f));
        EffectUtils.burst(targetPlayer.getLocation().add(0, 1, 0),
                new EffectUtils.Layer(Particle.SPELL_MOB, 10, 0.3, 0.3, 0.3, 0),
                new EffectUtils.Layer(Particle.DUST, 8, 0.3, 0.3, 0.3, 0, new Particle.DustOptions(Color.fromRGB(40, 30, 10), 1.0f)));
        return true;
    }
}

class PlagueHex extends Ability {
    private final ItemEditFull plugin;
    public PlagueHex(ItemEditFull plugin) {
        super("plague_hex", "Plague Hex", "A curse that spreads poison from the target to anyone who strikes them.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 8.0);
        Entity target = player.getTargetEntity(8);
        if (!(target instanceof LivingEntity)) {
            player.sendMessage("§cNo target in sight.");
            return false;
        }
        LivingEntity living = (LivingEntity) target;
        CurseAbilities.markPlague(living.getUniqueId(), System.currentTimeMillis() + (long) (duration * 1000));
        living.addPotionEffect(new PotionEffect(PotionEffectType.POISON, (int) (duration * 20), 0));
        EffectUtils.fanfare(living.getLocation(),
                new EffectUtils.SoundLayer(Sound.ENTITY_WITCH_DRINK, 1.0f, 0.8f),
                new EffectUtils.SoundLayer(Sound.ENTITY_WITCH_HURT, 0.5f, 0.7f));
        EffectUtils.burst(living.getLocation().add(0, 1, 0),
                new EffectUtils.Layer(Particle.SPELL_WITCH, 14, 0.3, 0.4, 0.3, 0),
                new EffectUtils.Layer(Particle.DUST, 8, 0.3, 0.4, 0.3, 0, new Particle.DustOptions(Color.fromRGB(0, 60, 0), 1.1f)));
        return true;
    }
}

class HexOfFragility extends Ability {
    private final ItemEditFull plugin;
    public HexOfFragility(ItemEditFull plugin) {
        super("hex_of_fragility", "Hex of Fragility", "Cracks a target's defences, increasing the damage they take.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double duration = getDoubleParam(plugin, item, "duration", 6.0);
        double bonus = getDoubleParam(plugin, item, "damage_bonus", 0.25);
        Entity target = player.getTargetEntity(8);
        if (!(target instanceof LivingEntity)) {
            player.sendMessage("§cNo target in sight.");
            return false;
        }
        LivingEntity living = (LivingEntity) target;
        CurseAbilities.markFragility(living.getUniqueId(), System.currentTimeMillis() + (long) (duration * 1000), 1.0 + bonus);
        EffectUtils.fanfare(living.getLocation(),
                new EffectUtils.SoundLayer(Sound.BLOCK_GLASS_BREAK, 1.0f, 0.7f),
                new EffectUtils.SoundLayer(Sound.ENTITY_WITCH_CELEBRATE, 0.5f, 0.9f));
        EffectUtils.burst(living.getLocation().add(0, 1, 0),
                new EffectUtils.Layer(Particle.CRIT, 15, 0.3, 0.4, 0.3, 0),
                new EffectUtils.Layer(Particle.DUST, 10, 0.3, 0.4, 0.3, 0, new Particle.DustOptions(Color.fromRGB(70, 10, 10), 1.1f)));
        return true;
    }
}

class NightmareWhisper extends Ability {
    private final ItemEditFull plugin;
    public NightmareWhisper(ItemEditFull plugin) {
        super("nightmare_whisper", "Nightmare Whisper", "Whispers a nightmare into the target's mind, fearing and blinding them.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double range = getDoubleParam(plugin, item, "range", 8.0);
        double duration = getDoubleParam(plugin, item, "duration", 3.0);
        Entity target = player.getTargetEntity((int) range);
        if (!(target instanceof LivingEntity)) {
            player.sendMessage("§cNo target in sight.");
            return false;
        }
        LivingEntity living = (LivingEntity) target;
        living.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, (int) (duration * 20), 0));
        living.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) (duration * 20), 2));
        EffectUtils.fanfare(living.getLocation(),
                new EffectUtils.SoundLayer(Sound.AMBIENT_CAVE, 1.0f, 0.5f),
                new EffectUtils.SoundLayer(Sound.ENTITY_VEX_AMBIENT, 0.5f, 0.6f));
        EffectUtils.burst(living.getLocation().add(0, 1.5, 0),
                new EffectUtils.Layer(Particle.SPELL_MOB, 12, 0.3, 0.4, 0.3, 0),
                new EffectUtils.Layer(Particle.DUST, 8, 0.3, 0.4, 0.3, 0, new Particle.DustOptions(Color.fromRGB(10, 0, 30), 1.2f)));
        return true;
    }
}

class SacrificialPact extends Ability {
    private final ItemEditFull plugin;
    public SacrificialPact(ItemEditFull plugin) {
        super("sacrificial_pact", "Sacrificial Pact", "Trade a portion of your own health for a burst of cursed damage on your target.");
        this.plugin = plugin;
    }
    @Override
    public boolean trigger(Player player, ItemStack item) {
        double selfDamage = getDoubleParam(plugin, item, "self_damage", 2.0);
        double bonusDamage = getDoubleParam(plugin, item, "bonus_damage", 8.0);
        Entity target = player.getTargetEntity(6);
        if (!(target instanceof LivingEntity)) {
            player.sendMessage("§cNo target in range.");
            return false;
        }
        if (player.getHealth() <= selfDamage) {
            player.sendMessage("§cYou lack the vitality to pay this pact's price.");
            return false;
        }
        LivingEntity living = (LivingEntity) target;
        player.damage(selfDamage);
        living.damage(bonusDamage, player);
        EffectUtils.fanfare(living.getLocation(),
                new EffectUtils.SoundLayer(Sound.ENTITY_WITCH_HURT, 1.0f, 0.7f),
                new EffectUtils.SoundLayer(Sound.ENTITY_PLAYER_HURT, 0.6f, 0.5f));
        EffectUtils.burst(living.getLocation().add(0, 1, 0),
                new EffectUtils.Layer(Particle.SPELL_WITCH, 20, 0.3, 0.4, 0.3, 0),
                new EffectUtils.Layer(Particle.DUST, 14, 0.3, 0.4, 0.3, 0, new Particle.DustOptions(Color.fromRGB(80, 0, 0), 1.3f)));
        return true;
    }
}
