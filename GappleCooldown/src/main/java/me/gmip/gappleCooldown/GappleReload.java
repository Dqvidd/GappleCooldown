package me.gmip.gappleCooldown;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class GappleReload implements CommandExecutor {

    private final JavaPlugin plugin;

    public GappleReload(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("gapple")) {
            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                if (sender.hasPermission("gapple.reload")) {
                    plugin.reloadConfig();
                    ((GappleCooldown) plugin).loadConfigValues();
                    sender.sendMessage("§aLa configuración ha sido recargada exitosamente.");
                    return true;
                } else {
                    sender.sendMessage("§cNo tienes permiso para ejecutar este comando.");
                    return true;
                }
            }
        }
        return false;
    }
}
