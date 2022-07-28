package me.RocketZ1.AdvancedQuarry.Events;

import me.RocketZ1.AdvancedQuarry.Main;
import me.RocketZ1.AdvancedQuarry.Other.PluginLang;
import me.RocketZ1.AdvancedQuarry.Other.Quarry;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UseQuarry implements Listener {
    private Main plugin;

    public UseQuarry(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void clickOption(InventoryClickEvent e) {
        if (e.isCancelled()) return;
        Player p = (Player) e.getWhoClicked();
        if (plugin.quarryManager.isInMenu(p.getUniqueId())) {
            e.setCancelled(true);
            if (e.getCurrentItem() == null) return;
            Quarry quarry = plugin.quarryManager.getQuarryFromMenu(p.getUniqueId());
            if (e.getCurrentItem().isSimilar(plugin.menuItems.showBorder)) {
                p.closeInventory();
                toggleDisplayBorder(p, quarry);
            } else if (e.getCurrentItem().isSimilar(plugin.menuItems.runQuarry)) {
                p.closeInventory();
                if(plugin.consumeOnStart && hasEnoughItems(quarry)) quarry.RunQuarry(plugin, p);
                else p.sendMessage(plugin.format(PluginLang.notEnoughItems));
            } else if (e.getCurrentItem().isSimilar(plugin.menuItems.stopQuarry)) {
                quarry.stopQuarry();
                p.closeInventory();
            }else if(e.getCurrentItem().isSimilar(plugin.menuItems.soundOFF)){
                quarry.setPlaySounds(true);
                p.playSound(quarry.getQuarryLoc(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                p.closeInventory();
            }else if(e.getCurrentItem().isSimilar(plugin.menuItems.soundON)){
                quarry.setPlaySounds(false);
                p.playSound(quarry.getQuarryLoc(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                p.closeInventory();
            }
        }
    }

    Map<UUID, Location> displayBorderON = new HashMap<>();
//    ArrayList<UUID> displayBorderON = new ArrayList<>();

    private void toggleDisplayBorder(Player p, Quarry quarry){
        UUID uuid = p.getUniqueId();
        quarry.showBorder(p, plugin);
        if(displayBorderON.containsKey(uuid)){
            if(!quarry.getQuarryLoc().equals(displayBorderON.get(uuid))){
                Quarry otherQuarry = plugin.quarryManager.getQuarry(displayBorderON.get(uuid));
                if(otherQuarry != null){
                    otherQuarry.showBorder(p, plugin);
                }
                displayBorderON.put(uuid, quarry.getQuarryLoc());
                p.sendMessage(plugin.format(PluginLang.displayBorderON));
                return;
            }
            displayBorderON.remove(uuid);
            p.sendMessage(plugin.format(PluginLang.displayBorderOFF));
            return;
        }
        displayBorderON.put(uuid, quarry.getQuarryLoc());
        p.sendMessage(plugin.format(PluginLang.displayBorderON));
    }


    private boolean hasEnoughItems(Quarry quarry){
        if(!(quarry.getQuarryLoc().getBlock().getRelative(BlockFace.UP).getState() instanceof Chest)) return false;
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
        return itemsStillRequired.isEmpty();
    }

    @EventHandler
    public void cancelArmorStandInteract(PlayerInteractEntityEvent e) {
        if (e.getRightClicked() instanceof ArmorStand) {
            ArmorStand stand = (ArmorStand) e.getRightClicked();
            if (stand.getScoreboardTags().contains("quarryStand")) e.setCancelled(true);
        }
    }

}
