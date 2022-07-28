package me.RocketZ1.AdvancedQuarry.Dependencies;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import me.RocketZ1.AdvancedQuarry.Main;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class WorldGuardDependency {
    private Main plugin;

    private WorldGuardPlugin worldGuardPlugin;
    private WorldGuard worldGuard;

    public WorldGuardDependency(Main plugin) {
        this.plugin = plugin;
        this.worldGuardPlugin = getWorldGuardPlugin();
        if(this.worldGuardPlugin != null) this.worldGuard = getWorldGuard();
    }

    public boolean isWorldGuardValid(){
        return this.worldGuardPlugin != null;
    }

    private WorldGuardPlugin getWorldGuardPlugin() {
        Plugin plugin = this.plugin.getServer().getPluginManager().getPlugin("WorldGuard");
        if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
            return null;
        }
        return (WorldGuardPlugin) plugin;
    }
    private WorldGuard getWorldGuard() {
        WorldGuard worldGuard = WorldGuard.getInstance();
        if (worldGuard == null || !(worldGuard instanceof WorldGuard)) {
            return null;
        }
        return worldGuard;
    }

    public boolean canBuild(Player p, Location location) {
        if(!isWorldGuardValid()) return true;
        com.sk89q.worldedit.util.Location loc = BukkitAdapter.adapt(location);
        LocalPlayer localPlayer = worldGuardPlugin.wrapPlayer(p);
        RegionContainer container = getWorldGuard().getPlatform().getRegionContainer();
        RegionManager regions = container.get(localPlayer.getWorld());
        if(regions == null) return true;
        RegionQuery query = container.createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(loc);
        boolean returnValue = true;
        for(ProtectedRegion region : set){
            if(region.contains(loc.toVector().toBlockPoint())){
                if(region.getOwners().contains(localPlayer.getUniqueId())) return true;
                if(region.getMembers().contains(localPlayer.getUniqueId())) return true;
                returnValue = false;
            }
        }
        return returnValue;
    }
}
