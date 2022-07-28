package me.RocketZ1.AdvancedQuarry.Events;

import me.RocketZ1.AdvancedQuarry.Main;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CraftQuarry implements Listener {
    private Main plugin;

    public CraftQuarry(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void dropItem(PlayerDropItemEvent e){
        if(e.isCancelled()) return;
        if(!plugin.canCraftQuarry) return;
        if(plugin.craftQuarryRequirements.isEmpty()) return;
        if(!e.getPlayer().hasPermission("advancedquarry.craft")) return;
        if(plugin.craftQuarryRequirements.containsKey(e.getItemDrop().getItemStack().getType())){
            BukkitRunnable runnable = new BukkitRunnable() {
                @Override
                public void run() {
                    if (!e.getItemDrop().isValid()) return;
                    Location loc = e.getItemDrop().getLocation().getBlock().getRelative(BlockFace.DOWN).getLocation();
                    if (loc.getBlock().getType() == Material.PISTON && !plugin.quarryManager.existingQuarry(loc)) {
                        boolean canBuild = dependenciesCanBuild(loc, e.getPlayer());
                        if(!canBuild) return;
                        onPiston(e.getItemDrop());
                    }
                    cancel();
                }
            }; runnable.runTaskLater(plugin, 20);
        }
    }

    private boolean dependenciesCanBuild(Location loc, Player p){
        if (plugin.worldGuardDependency.isWorldGuardValid() && !plugin.worldGuardDependency.canBuild(p, loc)) return false;
        if (plugin.griefPreventionDependency.isGriefPreventionValid()){
            boolean hasClaimBypass = plugin.griefPreventionDependency.hasIgnoreClaims(p);
            if(hasClaimBypass) return true;
            boolean hasClaimAccess = plugin.griefPreventionDependency.hasClaimAccess(p, loc);
            if(hasClaimAccess) return true;
            return false;
        }
        return true;
    }

    private void onPiston(Item item){
        ArrayList<Item> groundItems = new ArrayList<>();
        Map<Material, Integer> items = new HashMap<>();
        items.put(item.getItemStack().getType(), item.getItemStack().getAmount());
        groundItems.add(item);
        for(Entity entity : item.getNearbyEntities(0.5,0.5,0.5)){
            if(entity instanceof Item){
                Item item1 = (Item) entity;
                ItemStack itemStack = item1.getItemStack();
                if(!plugin.craftQuarryRequirements.containsKey(itemStack.getType())) continue;
                if(items.containsKey(itemStack.getType())){
                    items.put(itemStack.getType(), items.get(itemStack.getType()) + itemStack.getAmount());
                }else{
                    items.put(itemStack.getType(), itemStack.getAmount());
                }
                groundItems.add(item1);
            }
        }
        Map<Material, Integer> itemsStillRequired = new HashMap<>();
        for(Material material : plugin.craftQuarryRequirements.keySet()) {
            int requiredAmt = plugin.craftQuarryRequirements.get(material);
            int amtLeft = requiredAmt;
            if (items.containsKey(material)) {
                int amtOnGround = items.get(material);
                if(amtOnGround >= requiredAmt){
                    amtLeft = 0;
                }else{
                    amtLeft = requiredAmt - amtOnGround;
                }
            }
            if(amtLeft != 0){
                itemsStillRequired.put(material, amtLeft);
            }
        }
        if(itemsStillRequired.isEmpty()) {
            Location pistonLoc = item.getLocation().getBlock().getRelative(BlockFace.DOWN).getLocation();
            pistonLoc.getBlock().setType(Material.AIR);
            pistonLoc.getWorld().dropItemNaturally(pistonLoc, plugin.Quarry());
            pistonLoc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, pistonLoc, 5);
            pistonLoc.getWorld().playSound(pistonLoc, Sound.BLOCK_ANVIL_LAND, 1, 1);
            Map<Material, Integer> itemsToRemove = new HashMap<>(plugin.craftQuarryRequirements);
            for (Item groundItem : groundItems) {
                ItemStack itemStack = groundItem.getItemStack();
                int requiredAmt = itemsToRemove.get(itemStack.getType());
                if (itemStack.getAmount() > requiredAmt) {
                    itemsToRemove.remove(itemStack.getType());
                    itemStack.setAmount(itemStack.getAmount() - requiredAmt);
                    groundItem.setItemStack(itemStack);
                }else {
                    itemsToRemove.put(itemStack.getType(), requiredAmt - itemStack.getAmount());
                    groundItem.remove();
                }
            }
        }
    }
}
