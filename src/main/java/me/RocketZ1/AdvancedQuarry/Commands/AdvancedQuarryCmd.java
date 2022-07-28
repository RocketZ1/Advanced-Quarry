package me.RocketZ1.AdvancedQuarry.Commands;

import me.RocketZ1.AdvancedQuarry.Main;
import me.RocketZ1.AdvancedQuarry.Other.PluginLang;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class AdvancedQuarryCmd implements CommandExecutor, TabCompleter {

    private Main plugin;
    public AdvancedQuarryCmd(Main plugin){
        this.plugin = plugin;
        this.plugin.getServer().getPluginCommand("advancedquarry").setExecutor(this);
        this.plugin.getServer().getPluginCommand("advancedquarry").setTabCompleter(this);
        arguments1.add("give");
        arguments1.add("reload");
        //arguments1.add("list-config");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player p = null;
        if (sender instanceof Player) p = (Player) sender;
        if (p != null && !p.getPlayer().hasPermission("advancedquarry.admin")) {
            p.sendMessage(plugin.format(PluginLang.noPerms));
            return true;
        }
        if(args.length >= 1){
            if(!arguments1.contains(args[0].toLowerCase())){
                sendInvalidCmdMsg(sender, "");
                return true;
            }else if(args[0].equalsIgnoreCase("give")){
                if(!(args.length >= 2)){
                    sendInvalidCmdMsg(sender, " (player)");
                    return true;
                }
                give(sender, args[1]);
            }else if(args[0].equalsIgnoreCase("reload")){
                reloadConfig(sender);
//            }else if(args[0].equalsIgnoreCase("list-config")){
//                listConfig(sender);
            }else{
                sendInvalidCmdMsg(sender, "");
            }
        }else{
            sendInvalidCmdMsg(sender, "");
        }
        return false;
    }

    private void sendInvalidCmdMsg(CommandSender sender, String extra){
        sender.sendMessage(plugin.format(PluginLang.invalidCmdArgs+getArgumentAsString(arguments1)+extra));
    }

    private void give(CommandSender sender, String playerName){
        Player argsPlayer = null;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getName().equalsIgnoreCase(playerName)) {
                argsPlayer = player;
                break;
            }
        }
        if (argsPlayer == null) {
            sender.sendMessage(plugin.format(placeholders(PluginLang.notOnline, playerName)));
            return;
        }
        if (argsPlayer.getInventory().firstEmpty() == -1) {
            argsPlayer.getWorld().dropItemNaturally(argsPlayer.getLocation(), plugin.Quarry());
            sender.sendMessage(plugin.format(placeholders(PluginLang.itemGivenInvFull, playerName)));
        } else {
            argsPlayer.getInventory().addItem(plugin.Quarry());
            sender.sendMessage(plugin.format(placeholders(PluginLang.itemGiven, playerName)));
        }
    }

    private String placeholders(String msg, String playerName){
        msg = msg.replaceAll("%player%", playerName);
        return msg;
    }

    private void listConfig(CommandSender sender){
        String msg = "&a------- &6&lConfig settings &a-------";
        msg += "\n&bquarry-radius: &e"+plugin.quarryRadius;
        msg += "\n&bquarry-depth-limit: &e"+plugin.quarryDepthLimit;
        msg += "\n&bone-quarry-per-radius: &e"+plugin.oneQuarryPerRadius;
        msg += "\n&bquarry-delay: &e"+plugin.quarryDelay;
        msg += "\n&bquarry-requirements: &e";
        for(Material material : plugin.quarryRequirements.keySet()){
            msg += "\n&7 - &e"+material.toString().toLowerCase() + "\n       &bamount: &e"+plugin.quarryRequirements.get(material);
        }
        msg += "\n&bconsume-requirements-on-start: &e"+plugin.consumeOnStart;
        msg += "\n&bchosen-list: &e"+plugin.chosenList;
        if(plugin.chosenList == 3){
            msg += "\n&bblacklisted-blocks: &e";
            for(Material material : plugin.blacklistedBlocks){
                msg += "\n&7 - &e"+material.toString().toLowerCase();
            }
            msg += "\n&bwhitelisted-blocks: &e";
            for(Material material : plugin.whitelistedBLocks){
                msg += "\n&7 - &e"+material.toString().toLowerCase();
            }
        }else if(plugin.chosenList == 2){
            msg += "\n&bwhitelisted-blocks: &e";
            for(Material material : plugin.whitelistedBLocks){
                msg += "\n&7 - &e"+material.toString().toLowerCase();
            }
        }else{
            msg += "\n&bblacklisted-blocks: &e";
            for(Material material : plugin.blacklistedBlocks){
                msg += "\n&7 - &e"+material.toString().toLowerCase();
            }
        }
        msg += "\n&bcraftable-quarry: &e"+plugin.canCraftQuarry;
        msg += "\n&bcraftable-quarry-requirements: &e";
        for(Material material : plugin.craftQuarryRequirements.keySet()){
            msg += "\n&7 - &e"+material.toString().toLowerCase() + "\n       &bamount: &e"+plugin.craftQuarryRequirements.get(material);
        }
        msg += "\n&bstop-quarry-no-room: &e"+plugin.stopQuarryWhenChestFull;
        msg +="\n&bquarry-runtime: &e"+plugin.quarryRuntime;
        sender.sendMessage(plugin.format(msg));
    }

    private void reloadConfig(CommandSender sender){
        plugin.config.reloadConfig();
        plugin.getConfigInfo();
        sender.sendMessage(plugin.format(PluginLang.configReloaded));
    }

    private String getArgumentAsString(List<String> arguments) {
        String returnString = "<";
        for (String arg : arguments) {
            returnString += arg + "/";
        }
        returnString = returnString.substring(0, returnString.length() - 1);
        returnString += ">";
        return returnString;
    }


    List<String> arguments1 = new ArrayList<>();
    List<String> arguments2 = new ArrayList<>();


    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return null;
        //Player p = (Player) sender;
        if (!sender.hasPermission("advancedquarry.admin")) return null;
        if (args.length == 1) {
            List<String> result = new ArrayList<>();
            for (String a : arguments1) {
                if (a.toLowerCase().startsWith(args[0].toLowerCase()))
                    result.add(a);
            }
            return result;
        }
        return null;
    }
}
