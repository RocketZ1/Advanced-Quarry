package me.RocketZ1.AdvancedQuarry.Events;

import me.RocketZ1.AdvancedQuarry.Main;
import me.RocketZ1.AdvancedQuarry.Other.PluginLang;
import me.RocketZ1.AdvancedQuarry.Other.Quarry;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;

public class placeQuarry implements Listener {

    private Main plugin;

    public placeQuarry(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void QuarryPlacement(BlockPlaceEvent e) {
        if (e.isCancelled()) return;
        if (e.getItemInHand().isSimilar(plugin.Quarry())) {
            if(plugin.oneQuarryPerRadius){
                Quarry quarry = plugin.quarryManager.isOtherQuarriesInRegion(e.getBlock().getLocation());
                if(quarry != null){
                    String msg = PluginLang.quarryRegionOverlap;
                    msg = msg.replaceAll("%radius%", String.valueOf(plugin.quarryRadius));
                    e.getPlayer().sendMessage(plugin.format(msg));
                    plugin.quarryManager.showConflictingBorders(e.getPlayer(), quarry.getQuarryLoc(), e.getBlock().getLocation());
                    e.setCancelled(true);
                    return;
                }
            }
            if(!e.getPlayer().hasPermission("advancedquarry.admin") && plugin.blacklistedWorlds.contains(e.getBlock().getWorld().getName().toLowerCase())){
                e.getPlayer().sendMessage(plugin.format(PluginLang.invalidWorld));
                e.setCancelled(true);
                return;
            }
            Quarry quarry = new Quarry(e.getBlock().getLocation(), e.getPlayer().getUniqueId(), e.getPlayer().getName(), plugin.quarryRadius, plugin.quarryDepthLimit);
            BlockData blockData = e.getBlock().getBlockData();
            if (blockData instanceof Directional) {
                ((Directional) blockData).setFacing(BlockFace.DOWN);
                e.getBlock().setBlockData(blockData);
            }
            plugin.quarryManager.addQuarry(quarry);
        }
        if(e.getItemInHand().getType() == Material.CHEST){
            if(plugin.quarryManager.existingQuarry(e.getBlock().getRelative(BlockFace.DOWN).getLocation())){
                Chest chest = (Chest) e.getBlock().getState();
                Quarry quarry = plugin.quarryManager.getQuarry(e.getBlock().getRelative(BlockFace.DOWN).getLocation());
                quarry.setQuarryChest(chest);
            }
        }
    }

    @EventHandler
    public void cancelPushingQuarry(BlockPistonExtendEvent e){
        if(e.isCancelled()) return;
        if(plugin.quarryManager.getQuarries().isEmpty()) return;
        if(plugin.quarryManager.existingQuarry(e.getBlock().getLocation())){
            e.setCancelled(true);
        }
        for(Block block : e.getBlocks()){
            if(plugin.quarryManager.existingQuarry(block.getLocation())) e.setCancelled(true);
        }
    }

    @EventHandler
    public void cancelPullingQuarry(BlockPistonRetractEvent e){
        if(e.isCancelled()) return;
        if(plugin.quarryManager.getQuarries().isEmpty()) return;
        if(plugin.quarryManager.existingQuarry(e.getBlock().getLocation())){
            e.setCancelled(true);
        }
        for(Block block : e.getBlocks()){
            if(plugin.quarryManager.existingQuarry(block.getLocation())) e.setCancelled(true);
        }

    }

    @EventHandler
    public void CancelQuarryExplode(EntityExplodeEvent e){
        final HashMap<Location, Material> blocks = new HashMap<>();
        final HashMap<Location, BlockData> data = new HashMap<>();
        for(Block block : e.blockList()){
            if(plugin.quarryManager.existingQuarry(block.getLocation())) {
                blocks.put(block.getLocation(), block.getType());
                data.put(block.getLocation(), block.getBlockData());
                block.setType(Material.AIR);
            }
        }

        if(blocks.isEmpty()) return;
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                for(Location loc : blocks.keySet()){
                    loc.getBlock().setType(blocks.get(loc));
                    loc.getBlock().setBlockData(data.get(loc));
                }
                cancel();
            }
        };runnable.runTaskLater(plugin, 0);
    }
}
