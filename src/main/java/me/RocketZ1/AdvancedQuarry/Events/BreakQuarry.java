package me.RocketZ1.AdvancedQuarry.Events;

import me.RocketZ1.AdvancedQuarry.Main;
import me.RocketZ1.AdvancedQuarry.Other.PluginLang;
import me.RocketZ1.AdvancedQuarry.Other.Quarry;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BreakQuarry implements Listener {
    private Main plugin;

    public BreakQuarry(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void removeQuarry(BlockBreakEvent e){
        if(e.isCancelled()) return;
        if(plugin.quarryManager.existingQuarry(e.getBlock().getLocation())){
            Quarry quarry = plugin.quarryManager.getQuarry(e.getBlock().getLocation());
            if(!quarry.getOwner().equals(e.getPlayer().getUniqueId()) && !e.getPlayer().hasPermission("advancedquarry.admin")){
                e.setCancelled(true);
                String msg = PluginLang.incorrectQuarryUserBreak;
                msg = msg.replaceAll("%player%", quarry.getOwnerName());
                e.getPlayer().sendMessage(plugin.format(msg));
            }else{
                if(quarry.inProgress()){
                    e.getPlayer().sendMessage(plugin.format(PluginLang.breakQuarryInProgress));
                    e.setCancelled(true);
                }else{
                    e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), plugin.Quarry());
                    e.setDropItems(false);
                    quarry.destroyQuarry();
                    plugin.quarryManager.removeQuarry(quarry);
                }
            }
        }
    }
}
