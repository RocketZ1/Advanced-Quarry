package me.RocketZ1.AdvancedQuarry.Other;

import me.RocketZ1.AdvancedQuarry.Main;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class QuarryManager {
    private Main plugin;

    public QuarryManager(Main plugin) {
        this.plugin = plugin;
    }

    private ArrayList<Quarry> quarries = new ArrayList<>();
    private Map<UUID, Quarry> inMenu = new HashMap<>();

    public void addInMenu(UUID uuid, Quarry quarry){
        this.inMenu.put(uuid, quarry);
    }
    public void removeInMenu(UUID uuid){
        this.inMenu.remove(uuid);
    }
    public boolean isInMenu(UUID uuid){
        return this.inMenu.containsKey(uuid);
    }
    public Quarry getQuarryFromMenu(UUID uuid){
        if(this.inMenu.containsKey(uuid)){
            return this.inMenu.get(uuid);
        }
        return null;
    }

    public ArrayList<Quarry> getQuarries(){
        return this.quarries;
    }

    public Quarry getQuarry(Location location){
        if(existingQuarry(location)){
            for(Quarry quarry : this.quarries){
                if(quarry.getQuarryLoc().equals(location)) return quarry;
            }
        }
        return null;
    }

    public boolean existingQuarry(Location quarryLoc){
        for(Quarry quarry : this.quarries){
            if(quarryLoc.equals(quarry.getQuarryLoc())) return true;
        }
        return false;
    }

    public boolean addQuarry(Quarry quarry){
        if(!this.quarries.contains(quarry)){
            this.quarries.add(quarry);
            return true;
        }
        return false;
    }

    public boolean removeQuarry(Quarry quarry){
        if(this.quarries.contains(quarry)){
            this.quarries.remove(quarry);
            return true;
        }
        return false;
    }

    public Quarry isOtherQuarriesInRegion(Location location){
        if(this.quarries.isEmpty()) return null;
        ArrayList<Location> newQuarryLocs = getQuarryRegionNoY(location);
        for(Quarry quarry : this.quarries){
            for(Location loc : getQuarryRegionNoY(quarry.getQuarryLoc())){
                if(newQuarryLocs.contains(loc)) return quarry;
            }
        }
        return null;
    }

    private ArrayList<Location> getQuarryRegionNoY(Location quarryLoc) {
        Location loc1 = new Location(quarryLoc.getWorld(), quarryLoc.getBlockX() + plugin.quarryRadius, 0, quarryLoc.getBlockZ() + plugin.quarryRadius);
        Location loc2 = new Location(quarryLoc.getWorld(), quarryLoc.getBlockX() - plugin.quarryRadius, 0, quarryLoc.getBlockZ() - plugin.quarryRadius);
        ArrayList<Location> locs = new ArrayList<>();
        int x = Math.min(loc1.getBlockX(), loc2.getBlockX());
        int z = Math.min(loc1.getBlockZ(), loc2.getBlockZ());
        int maxX = Math.max(loc1.getBlockX(), loc2.getBlockX()) + 1;
        int maxZ = Math.max(loc1.getBlockZ(), loc2.getBlockZ()) + 1;
        for (int x1 = x; x1 < maxX; x1++) {
            for (int z1 = z; z1 < maxZ; z1++) {
                Location loc = new Location(quarryLoc.getWorld(), x1, 0, z1);
                locs.add(loc);
            }
        }
        return locs;
    }

    public void showConflictingBorders(Player p, Location quarryLoc, Location attemptedQuarryLoc){
        Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(0, 127, 255), 1.0F);
        Particle.DustOptions dustOptions2 = new Particle.DustOptions(Color.fromRGB(255, 0, 0), 1.0F);
        displayParticles(p, quarryLoc, dustOptions);
        displayParticles(p, attemptedQuarryLoc, dustOptions2);
    }
    private void displayParticles(Player p, Location quarryLoc, Particle.DustOptions dustOptions){
        Location center = new Location(quarryLoc.getWorld(), quarryLoc.getBlockX(), p.getLocation().getBlockY(), quarryLoc.getBlockZ());
        Location loc1 = new Location(center.getWorld(), center.getBlockX() - plugin.quarryRadius, center.getBlockY(), center.getBlockZ() - plugin.quarryRadius);
        Location loc2 = new Location(center.getWorld(), center.getBlockX() + plugin.quarryRadius, center.getBlockY(), center.getBlockZ() + plugin.quarryRadius);
        int x = Math.min(loc1.getBlockX(), loc2.getBlockX());
        int z = Math.min(loc1.getBlockZ(), loc2.getBlockZ());
        int maxX = Math.max(loc1.getBlockX(), loc2.getBlockX());
        int maxZ = Math.max(loc1.getBlockZ(), loc2.getBlockZ());
        for (int x1 = x; x1 < maxX; x1++) {
            for (int z1 = z; z1 < maxZ; z1++) {
                Location particleLoc = new Location(center.getWorld(), x1 + 0.5, center.getBlockY() + 0.5, z1 + 0.5);
                BukkitRunnable runnable = new BukkitRunnable() {
                    int i = 0;
                    @Override
                    public void run() {
                        if(i >=2) cancel();
                        i++;
                        p.spawnParticle(Particle.REDSTONE, particleLoc, 3, dustOptions);
                    }
                };
                runnable.runTaskTimerAsynchronously(plugin, 0, 10);
            }
        }
    }
}
