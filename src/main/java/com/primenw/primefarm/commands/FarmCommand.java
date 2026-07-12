package com.primenw.primefarm.commands;

import com.primenw.primefarm.PrimeFarm;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FarmCommand implements CommandExecutor {

    private final PrimeFarm plugin;

    public FarmCommand(PrimeFarm plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("primefarm.use")) {
            plugin.getLanguageManager().send(player, "no-permission");
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "storage":
            case "depo":
                plugin.getStorageGUI().open(player, 1);
                return true;

            case "settings":
            case "ayarlar":
                plugin.getSettingsGUI().open(player);
                return true;

            case "lang":
            case "dil":
                handleLang(player, args);
                return true;

            case "reload":
                if (!player.hasPermission("primefarm.admin")) {
                    plugin.getLanguageManager().send(player, "no-permission");
                    return true;
                }
                plugin.reloadEverything();
                plugin.getLanguageManager().send(player, "reload-success");
                return true;

            case "help":
            default:
                sendHelp(player);
                return true;
        }
    }

    private void handleLang(Player player, String[] args) {
        if (args.length < 2) {
            plugin.getLanguageManager().send(player, "lang-usage");
            return;
        }
        String lang = args[1].toLowerCase();
        if (!plugin.getLanguageManager().setLanguage(player, lang)) {
            plugin.getLanguageManager().send(player, "lang-unknown");
            return;
        }
        plugin.getLanguageManager().send(player, "lang-set", "%lang%", lang);
    }

    private void sendHelp(Player player) {
        plugin.getLanguageManager().send(player, "help-header");
        plugin.getLanguageManager().send(player, "help-storage");
        plugin.getLanguageManager().send(player, "help-settings");
        plugin.getLanguageManager().send(player, "help-lang");
        if (player.hasPermission("primefarm.admin")) {
            plugin.getLanguageManager().send(player, "help-reload");
        }
    }
}
