package me.RocketZ1.AdvancedQuarry.Other;

import me.RocketZ1.AdvancedQuarry.Main;
import org.apache.commons.lang.Validate;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import java.util.ArrayList;

public class QuarryEffects {
    private ArmorStand armorStand;
    //private boolean mining = false;

    public QuarryEffects(Location quarryLoc, Quarry quarry){
        this.quarryLoc = quarryLoc;
        Location spawnLoc = new Location(quarryLoc.getWorld(), quarryLoc.getBlockX() + 0.5, quarryLoc.getBlockY(), quarryLoc.getBlockZ() + 0.5);
        ArmorStand armorStand = (ArmorStand) quarryLoc.getWorld().spawnEntity(spawnLoc, EntityType.ARMOR_STAND);
        armorStand.setInvulnerable(true);
        armorStand.setBasePlate(false);
        armorStand.setArms(false);
        armorStand.setGravity(false);
        armorStand.setPortalCooldown(1000000000);
        armorStand.setCustomName(ChatColor.translateAlternateColorCodes('&', PluginLang.drillName));
        armorStand.setCustomNameVisible(false);
        armorStand.getScoreboardTags().add("quarryStand");
        armorStand.setInvisible(true);
        armorStand.setSmall(true);
//        armorStand.setVisualFire(false);
        this.armorStand = armorStand;
        if(quarry == null) return;
        Location pos1 = quarry.getPos1();
        Location pos2 = quarry.getPos2();
        pos1 = new Location(pos1.getWorld(), pos1.getBlockX()+1, pos1.getBlockY()+1, pos1.getBlockZ()+1);
        pos2 = new Location(pos2.getWorld(), pos2.getBlockX(), pos2.getBlockY(), pos2.getBlockZ());
        this.blocks = getPerimeterBlocks(pos1, pos2);
    }

    public QuarryEffects(Location quarryLoc, ArrayList<BlockVector> blocks){
        this.quarryLoc = quarryLoc;
        Location spawnLoc = new Location(quarryLoc.getWorld(), quarryLoc.getBlockX() + 0.5, quarryLoc.getBlockY(), quarryLoc.getBlockZ() + 0.5);
        ArmorStand armorStand = (ArmorStand) quarryLoc.getWorld().spawnEntity(spawnLoc, EntityType.ARMOR_STAND);
        armorStand.setInvulnerable(true);
        armorStand.setBasePlate(false);
        armorStand.setArms(false);
        armorStand.setGravity(false);
        armorStand.setPortalCooldown(1000000000);
        armorStand.setCustomName(ChatColor.translateAlternateColorCodes('&', PluginLang.drillName));
        armorStand.setCustomNameVisible(false);
        armorStand.getScoreboardTags().add("quarryStand");
        armorStand.setInvisible(true);
        armorStand.setSmall(true);
//        armorStand.setVisualFire(false);
        this.armorStand = armorStand;
        this.blocks = blocks;
    }

    private void tpArmorStand(Location loc){
        this.armorStand.teleport(loc);
    }

    private void playSound(Location location){
        location.getWorld().playSound(location, Sound.BLOCK_PISTON_EXTEND, 1, 1);
    }

    public void runEffects(Location currentLoc, Location quarryLoc, boolean playSounds){
        Location loc = new Location(currentLoc.getWorld(), currentLoc.getBlockX()+0.5, currentLoc.getBlockY(), currentLoc.getBlockZ()+0.5);
        tpArmorStand(loc);
        drawLine(loc, new Location(quarryLoc.getWorld(), quarryLoc.getBlockX()+0.5, quarryLoc.getBlockY(), quarryLoc.getBlockZ()+0.5), 0.5);
        if(playSounds) playSound(loc);
        this.armorStand.setFireTicks(0);
    }

    public void showStand(boolean showStand){
        if(showStand){
           this.armorStand.setSmall(false);
           this.armorStand.setInvisible(false);
           this.armorStand.setCustomNameVisible(true);
        }else{
            this.armorStand.setSmall(true);
            this.armorStand.setInvisible(true);
            this.armorStand.setCustomNameVisible(false);
        }
    }

    private final Location quarryLoc;

    private ArrayList<Player> toggleBorderPlayers = new ArrayList<>();
    private ArrayList<BlockVector> blocks = new ArrayList<>();

    public ArrayList<BlockVector> getBlocks() {
        return this.blocks;
    }

    public void showBorder(Location quarryLoc, Player p, int quarryRadius, Main plugin){
        if(toggleBorderPlayers.contains(p)){
            toggleBorderPlayers.remove(p);
            return;
        }
        boolean isEmpty = toggleBorderPlayers.isEmpty();
        toggleBorderPlayers.add(p);
        if(isEmpty) displayBorder(plugin);
    }

    BukkitRunnable runnable = null;

    private void displayBorder(Main plugin){
        final int[] a = {0};
        runnable = new BukkitRunnable() {
            int i = 0;
            @Override
            public void run() {
                if(toggleBorderPlayers.isEmpty()){
                    cancel();
                    runnable = null;
                    return;
                }
                if(!plugin.quarryManager.existingQuarry(quarryLoc)){
                    cancel();
                    runnable = null;
                    return;
                }
                for(; a[0]<blocks.size(); a[0]++){
                    if(a[0]+1 == blocks.size()){
                        i++;
                        a[0]=0;
                        return;
                    }
                    Location loc = new Location(quarryLoc.getWorld(), blocks.get(a[0]).getX(), blocks.get(a[0]).getY(), blocks.get(a[0]).getZ());
                    Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(255, 0, 0), 1.5F);
                    for(Player player : toggleBorderPlayers){
                        player.spawnParticle(Particle.REDSTONE, loc, 1, dustOptions);
                    }
                    if(a[0] != 0 && a[0] % 50 == 0){
                        a[0]++;
                        return;
                    }
                }
            }
        };
        runnable.runTaskTimerAsynchronously(plugin, 0, 0);
    }
    
    public ArrayList<BlockVector> getPerimeterBlocks(Location maximum, Location minimum) {
        ArrayList<BlockVector> blocks = new ArrayList<>();
        for(int x = minimum.getBlockX(); x <= maximum.getBlockX(); x++){
            for(int z = minimum.getBlockZ(); z <= maximum.getBlockZ(); z++) {
                blocks.add(new BlockVector(x, maximum.getBlockY(), z));
            }
        }
        for (int y = maximum.getBlockY(); y > minimum.getBlockY(); y--) {
            for (int x = minimum.getBlockX(); x <= maximum.getBlockX(); x++) {
                blocks.add(new BlockVector(x, y, minimum.getBlockZ()));
                blocks.add(new BlockVector(x, y, maximum.getBlockZ()));
            }
            for (int z = minimum.getBlockZ(); z <= maximum.getBlockZ(); z++) {
                blocks.add(new BlockVector(minimum.getBlockX(), y, z));
                blocks.add(new BlockVector(maximum.getBlockX(), y, z));
            }
        }
        for(int x = minimum.getBlockX(); x <= maximum.getBlockX(); x++){
            for(int z = minimum.getBlockZ(); z <= maximum.getBlockZ(); z++) {
                blocks.add(new BlockVector(x, minimum.getBlockY(), z));
            }
        }
        //sortYDown(blocks);
        return blocks;
    }


    public void removeAllEffects(){
        if(runnable != null && !runnable.isCancelled()) runnable.cancel();
        this.armorStand.remove();
    }

    private void drawLine(Location point1, Location point2, double space) {
        World world = point1.getWorld();
        Validate.isTrue(point2.getWorld().equals(world), "Lines cannot be in different worlds!");
        double distance = point1.distance(point2);
        Vector p1 = point1.toVector();
        Vector p2 = point2.toVector();
        Vector vector = p2.clone().subtract(p1).normalize().multiply(space);
        double length = 0;
        for (; length < distance; p1.add(vector)) {
            world.spawnParticle(Particle.VILLAGER_HAPPY, p1.getX(), p1.getY(), p1.getZ(), 1);
            length += space;
        }
    }

}
