package me.gmip.gappleCooldown;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class GappleCooldown extends JavaPlugin implements Listener {

    private HashMap<UUID, Long> cooldowns;
    private HashMap<UUID, Long> pearlCooldowns;
    private long gappleCooldownTime;
    private long pearlCooldownTime;
    private String gappleCooldownMessage;
    private String pearlCooldownMessage;
    private String gappleFinalMessage;
    private String pearlFinalMessage;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadConfig();

        // Cargar los valores del config.yml
        loadConfigValues();

        cooldowns = new HashMap<>();
        pearlCooldowns = new HashMap<>();

        Bukkit.getPluginManager().registerEvents(this, this);

        this.getCommand("gapple").setExecutor(new GappleReload(this));
    }

    void loadConfigValues() {
        long gappleCooldownSeconds = getConfig().getLong("cooldowns.gapple.time");
        long pearlCooldownSeconds = getConfig().getLong("cooldowns.pearl.time");

        gappleCooldownTime = gappleCooldownSeconds * 1000;
        pearlCooldownTime = pearlCooldownSeconds * 1000;

        // Leer los mensajes de COOLDOWN desde el archivo de configuración
        gappleCooldownMessage = getConfig().getString("cooldowns.gapple.message");
        pearlCooldownMessage = getConfig().getString("cooldowns.pearl.message");

        // Leer los mensajes FINALES desde el archivo de configuración
        gappleFinalMessage = getConfig().getString("cooldowns.gapple.finalMessage");
        pearlFinalMessage = getConfig().getString("cooldowns.pearl.finalMessage");
    }

    @EventHandler
    public void onConsumeGoldenApple(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();

        // Comprueba que el Item consumido sea la Gapple
        if (event.getItem().getType() == Material.ENCHANTED_GOLDEN_APPLE) {
            UUID playerId = player.getUniqueId();

            if (cooldowns.containsKey(playerId)) {
                long timeSinceLastUse = System.currentTimeMillis() - cooldowns.get(playerId);

                if (timeSinceLastUse < gappleCooldownTime) {
                    long timeLeft = (gappleCooldownTime - timeSinceLastUse) / 1000;
                    player.sendMessage(formatMessage(gappleCooldownMessage, timeLeft));
                    event.setCancelled(true);
                    return;
                }
            }

            cooldowns.put(playerId, System.currentTimeMillis());
            startCooldownTask(player, gappleCooldownTime, gappleCooldownMessage, gappleFinalMessage, cooldowns, true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Player player = event.getPlayer();
            UUID playerId = player.getUniqueId();

            EquipmentSlot hand = event.getHand();

            // Verifica si el objeto usado se trata de una EnderPearl
            if ((hand == EquipmentSlot.HAND && player.getInventory().getItemInMainHand().getType() == Material.ENDER_PEARL) ||
                    (hand == EquipmentSlot.OFF_HAND && player.getInventory().getItemInOffHand().getType() == Material.ENDER_PEARL)) {
                if (pearlCooldowns.containsKey(playerId)) {
                    long timeSinceLastUse = System.currentTimeMillis() - pearlCooldowns.get(playerId);

                    if (timeSinceLastUse < pearlCooldownTime) {
                        long timeLeft = (pearlCooldownTime - timeSinceLastUse) / 1000;
                        player.sendMessage(formatMessage(pearlCooldownMessage, timeLeft));
                        event.setCancelled(true);
                        return;
                    }
                }

                pearlCooldowns.put(playerId, System.currentTimeMillis());
                startCooldownTask(player, pearlCooldownTime, pearlCooldownMessage, pearlFinalMessage, pearlCooldowns, false);
            }
        }
    }

    private String formatMessage(String message, long timeLeft) {
        return message.replace("%time%", String.valueOf(timeLeft));
    }

    private void startCooldownTask(Player player, long cooldownTime, String messageTemplate, String finalMessage, HashMap<UUID, Long> cooldownMap, boolean useActionBar) {
        new BukkitRunnable() {
            @Override
            public void run() {
                UUID playerId = player.getUniqueId();
                long currentTime = System.currentTimeMillis();

                if (cooldownMap.containsKey(playerId)) {
                    long timeSinceLastUse = currentTime - cooldownMap.get(playerId);
                    if (timeSinceLastUse < cooldownTime) {
                        long timeLeft = (cooldownTime - timeSinceLastUse) / 1000;
                        if (useActionBar) {
                            sendActionBar(player, formatMessage(messageTemplate, timeLeft));
                        }
                    } else {
                        if (useActionBar) {
                            sendActionBar(player, finalMessage);
                        }
                        player.sendMessage(finalMessage);
                        cooldownMap.remove(playerId);
                        cancel();
                    }
                } else {
                    cancel();
                }
            }
        }.runTaskTimer(this, 0, 20); // Se ejecuta cada 20 ticks (1 segundo)
    }

    private void sendActionBar(Player player, String message) {
        player.sendActionBar(message);
    }
}
