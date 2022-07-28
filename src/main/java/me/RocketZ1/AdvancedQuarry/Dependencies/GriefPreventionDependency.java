package me.RocketZ1.AdvancedQuarry.Dependencies;

import me.RocketZ1.AdvancedQuarry.Main;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class GriefPreventionDependency {
    private Main plugin;

    private GriefPrevention griefPrevention;

    public GriefPreventionDependency(Main plugin) {
        this.plugin = plugin;
        this.griefPrevention = getGriefPreventionPlugin();
    }

    public boolean isGriefPreventionValid(){
        return griefPrevention != null;
    }

    private GriefPrevention getGriefPreventionPlugin(){
        Plugin plugin = this.plugin.getServer().getPluginManager().getPlugin("GriefPrevention");
        // WorldGuard may not be loaded
        if (plugin == null || !(plugin instanceof GriefPrevention)) {
            return null; // Maybe you want throw an exception instead
        }
        return (GriefPrevention) plugin;
    }

    public boolean hasClaimAccess(Player p, Location loc){
        if(!isGriefPreventionValid()) return true;
        for(Claim claim : griefPrevention.dataStore.getClaims()){
            if(claim.contains(loc, true, true)){
                if(griefPrevention.dataStore.getPlayerData(p.getUniqueId()).ignoreClaims) return true;
                String msg = claim.allowAccess(p);
                return msg == null;
            }
        }
        return true;
    }

    public boolean hasIgnoreClaims(Player p){
        return this.griefPrevention.dataStore.getPlayerData(p.getUniqueId()).ignoreClaims;
    }


}
