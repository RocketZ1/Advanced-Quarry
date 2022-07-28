package me.RocketZ1.AdvancedQuarry.Events;

import me.RocketZ1.AdvancedQuarry.Main;
import me.RocketZ1.AdvancedQuarry.Other.PluginLang;
import me.RocketZ1.AdvancedQuarry.Other.Quarry;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.SimpleDateFormat;
import java.util.*;

public class openQuarryGUI implements Listener {

    private Main plugin;

    public openQuarryGUI(Main plugin) {
        this.plugin = plugin;
        makeItems();
    }

    ItemStack notValid;
    ItemStack noChest;

    private void makeItems(){
        notValid = new ItemStack(Material.RED_WOOL);
        ItemMeta notValidMeta = notValid.getItemMeta();
        notValidMeta.setDisplayName(plugin.format("&cNot Valid!"));
        notValid.setItemMeta(notValidMeta);

        noChest = new ItemStack(Material.CHEST);
        ItemMeta noChestMeta = noChest.getItemMeta();
        noChestMeta.setDisplayName(plugin.format("&cPlace a chest above the Quarry!"));
        noChest.setItemMeta(noChestMeta);
    }

    @EventHandler
    public void closeQuarryMenu(InventoryCloseEvent e){
        if(plugin.quarryManager.isInMenu(e.getPlayer().getUniqueId())){
            plugin.quarryManager.removeInMenu(e.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void clickQuarry(PlayerInteractEvent e){
        if(plugin.quarryManager.getQuarries().isEmpty()) return;
        if(e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if(e.getHand() == EquipmentSlot.OFF_HAND) return;
        if(e.getClickedBlock() == null) return;
        Player p = e.getPlayer();
        Quarry quarry = null;
        for(Quarry quarry1 : plugin.quarryManager.getQuarries()){
            if(quarry1.getQuarryLoc().equals(e.getClickedBlock().getLocation())){
                quarry = quarry1;
                break;
            }
        }
        if(quarry == null) return;
        if(!p.hasPermission("advancedquarry.admin") && plugin.blacklistedWorlds.contains(p.getWorld().getName().toLowerCase())){
            p.sendMessage(plugin.format(PluginLang.invalidWorld));
            return;
        }
        if(p.hasPermission("advancedquarry.admin") && !quarry.getOwner().equals(p.getUniqueId())){
            String msg = PluginLang.bypassOwnership;
            msg = msg.replaceAll("%player%", quarry.getOwnerName());
            p.sendMessage(plugin.format(msg));
        }else if(!quarry.getOwner().equals(p.getUniqueId())){
            String msg = PluginLang.invalidOwner;
            msg = msg.replaceAll("%player%", quarry.getOwnerName());
            p.sendMessage(plugin.format(msg));
            return;
        }
        Inventory quarryInv = Bukkit.createInventory(null, 27, plugin.format("&6"+quarry.getOwnerName()+"'s Quarry"));
        ItemStack[] inv = new ItemStack[27];
        if(quarry.getQuarryLoc().getBlock().getRelative(BlockFace.UP).getType() != Material.CHEST){
            inv[22] = notValid;
            //
            inv[4] = noChest;
        }else if(quarry.getQuarryLoc().getBlock().getRelative(BlockFace.UP).getType() == Material.CHEST){
            Chest quarryChest = (Chest) quarry.getQuarryLoc().getBlock().getRelative(BlockFace.UP).getState();
            quarry.setQuarryChest(quarryChest);
            Inventory chestInv = quarryChest.getInventory();
            Map<Material, Integer> chestItems = new HashMap<>();
            for(ItemStack item : chestInv.getContents()){
                if(item == null) continue;
                if(chestItems.containsKey(item.getType())){
                    chestItems.put(item.getType(), chestItems.get(item.getType())+item.getAmount());
                }else{
                    chestItems.put(item.getType(), item.getAmount());
                }
            }
            Map<Material, Integer> itemsStillRequired = new HashMap<>();
            for(Material material : plugin.quarryRequirements.keySet()) {
                int requiredAmt = plugin.quarryRequirements.get(material);
                int amtLeft = requiredAmt;
                if (chestItems.containsKey(material)) {
                    int amtInChest = chestItems.get(material);
                    if(amtInChest >= requiredAmt){
                        amtLeft = 0;
                    }else{
                        amtLeft = requiredAmt - amtInChest;
                    }
                }
                if(amtLeft != 0){
                    itemsStillRequired.put(material, amtLeft);
                }
            }

            boolean stillRequiresItems = !itemsStillRequired.isEmpty();
            ItemStack Chest = new ItemStack(Material.CHEST);
            ItemMeta ChestMeta = Chest.getItemMeta();
            ChestMeta.setDisplayName(plugin.format(PluginLang.missingItems));
            ArrayList<String> lore = new ArrayList<>();
            lore.add("");
            for(Material material : itemsStillRequired.keySet()){
                int itemAmt = itemsStillRequired.get(material);
                String itemName = material.toString().toLowerCase().replaceAll("_", " ");
                itemName = itemName.substring(0, 1).toUpperCase()+itemName.substring(1)+"(s)";
                String msg = PluginLang.requiredItemsRemaining;
                msg = msg.replaceAll("%item_name%", itemName);
                msg = msg.replaceAll("%item_amount%", String.valueOf(itemAmt));
                lore.add(plugin.format(msg));
            }
            lore.add("");
            if(stillRequiresItems){
                ChestMeta.setLore(lore);
            }else{
                ArrayList<String> lore2 = new ArrayList<>();
                lore2.add(plugin.format(PluginLang.requirementsMet));
                ChestMeta.setLore(lore2);
            }
            Chest.setItemMeta(ChestMeta);
            inv[4] = Chest;
            ItemStack validWool;
            if(!stillRequiresItems){
                validWool = new ItemStack(Material.LIME_WOOL);
                ItemMeta validMeta = validWool.getItemMeta();
                validMeta.setDisplayName(plugin.format(PluginLang.valid));
                validWool.setItemMeta(validMeta);
            }else{
                validWool = new ItemStack(Material.RED_WOOL);
                ItemMeta validMeta = validWool.getItemMeta();
                validMeta.setDisplayName(plugin.format(PluginLang.invalid));
                validWool.setItemMeta(validMeta);
            }
            inv[22] = validWool;
            if(quarry.inProgress() && !stillRequiresItems){
                inv[26] = plugin.menuItems.stopQuarry;
            }else if(!stillRequiresItems){
                inv[26] = plugin.menuItems.runQuarry;
            }
        }
        if(quarry.inProgress()){
            inv[13] = progress(quarry.getQuarryLoc(), quarry);
            inv[26] = plugin.menuItems.stopQuarry;
        }
        if(quarry.isSoundEnabled()){
            inv[8] = plugin.menuItems.soundON;
        }else{
            inv[8] = plugin.menuItems.soundOFF;
        }
        inv[18] = plugin.menuItems.showBorder;
        quarryInv.setContents(inv);
        p.openInventory(quarryInv);
        plugin.quarryManager.addInMenu(p.getUniqueId(), quarry);
    }

    private ItemStack progress(Location quarryLoc, Quarry quarry){
        getServerVersion();
        Location center = new Location(quarryLoc.getWorld(), quarryLoc.getBlockX(), quarryLoc.getBlockY()-1, quarryLoc.getBlockZ());
        ItemStack item = new ItemStack(Material.EMERALD);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(plugin.format(PluginLang.quarryProgressTxt));
        Location loc1 = quarry.getPos1();
        Location loc2 = quarry.getPos2();
        loc1 = new Location(loc1.getWorld(), loc1.getBlockX()+1, loc1.getBlockY()+1, loc1.getBlockZ()+1);
        loc2 = new Location(loc2.getWorld(), loc2.getBlockX(), loc2.getBlockY(), loc2.getBlockZ());
        int x = Math.min(loc1.getBlockX(), loc2.getBlockX());
        int y = Math.min(loc1.getBlockY(), loc2.getBlockY());
        int z = Math.min(loc1.getBlockZ(), loc2.getBlockZ());
        int maxX = Math.max(loc1.getBlockX(), loc2.getBlockX());
        int maxY = Math.max(loc1.getBlockY(), loc2.getBlockY());
        int maxZ = Math.max(loc1.getBlockZ(), loc2.getBlockZ());
        int totalBlocks = 0;
        int blocksDone = 0;
        for (int x1 = x; x1 < maxX; x1++) {
            for (int y1 = y; y1 < maxY; y1++) {
                for (int z1 = z; z1 < maxZ; z1++) {
                    Location loc = new Location(center.getWorld(), x1, y1, z1);
                    if(plugin.chosenList == 3) {
                        if (plugin.blacklistedBlocks.contains(loc.getBlock().getType()) && !plugin.whitelistedBLocks.contains(loc.getBlock().getType())) blocksDone++;
                        totalBlocks++;
                    }else if (plugin.chosenList == 2){
                        if (!plugin.whitelistedBLocks.contains(loc.getBlock().getType())) blocksDone++;
                        totalBlocks++;
                    }else {
                        if (plugin.blacklistedBlocks.contains(loc.getBlock().getType())) blocksDone++;
                        totalBlocks++;
                    }
                }
            }
        }
        double progress = ((double) blocksDone / totalBlocks) * 100;
        progress = Math.round(progress*100.0)/100.0;
        ArrayList<String> lore = new ArrayList<>();
        lore.add("");
        String progressMsg = PluginLang.quarryProgress;
        lore.add(plugin.format(progressMsg.replaceAll("%progress%", String.valueOf(progress))));
        if(quarry.getQuarryStopTime() != -1) {
            lore.add("");
            lore.add(plugin.format(PluginLang.quarryStopTimeMsg));
            lore.add(plugin.format("&b"+getQuarryStopTime(quarry)));
        }
        lore.add("");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }

    private int minYLevel;

    private void getServerVersion(){
        if(Bukkit.getBukkitVersion().startsWith("1.18")){
            minYLevel = -64;
        }else{
            minYLevel = 0;
        }
    }

    private String getQuarryStopTime(Quarry quarry){
        long stopTime = quarry.getQuarryStopTime();
        if(System.currentTimeMillis() >= stopTime) return "0h, 0m, 0s";
        long difference_In_Time = stopTime-System.currentTimeMillis();
        // Calucalte time difference
        // in milliseconds

        // Calucalte time difference in
        // seconds, minutes, hours, years,
        // and days
        int difference_In_Seconds = (int) (difference_In_Time / 1000) % 60;
        int difference_In_Minutes = (int) (difference_In_Time/ (1000 * 60)) % 60;
        int difference_In_Hours = (int) (difference_In_Time / (1000 * 60 * 60)) % 24;
        return difference_In_Hours+"h, "+difference_In_Minutes+"m, "+difference_In_Seconds+"s.";
    }

}
