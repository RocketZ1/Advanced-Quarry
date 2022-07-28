package me.RocketZ1.AdvancedQuarry.Other;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class MenuItems {

    public MenuItems(){
        createRunQuarry();
        createStopQuarry();
        createShowBorder();
        createSoundOFF();
        createSoundON();
    }

    public ItemStack runQuarry;
    public ItemStack stopQuarry;
    public ItemStack showBorder;
    public ItemStack soundON;
    public ItemStack soundOFF;

    private void createRunQuarry(){
        runQuarry = new ItemStack(Material.DIAMOND_PICKAXE);
        ItemMeta itemMeta = runQuarry.getItemMeta();
        itemMeta.setDisplayName(format(PluginLang.runQuarry));
        runQuarry.setItemMeta(itemMeta);
    }
    private void createStopQuarry(){
        stopQuarry = new ItemStack(Material.RED_CONCRETE);
        ItemMeta stopMeta = stopQuarry.getItemMeta();
        stopMeta.setDisplayName(format(PluginLang.stopQuarry));
        stopQuarry.setItemMeta(stopMeta);
    }
    private void createShowBorder(){
        showBorder = new ItemStack(Material.ORANGE_DYE);
        ItemMeta borderMeta = showBorder.getItemMeta();
        borderMeta.setDisplayName(format(PluginLang.displayBorder));
        showBorder.setItemMeta(borderMeta);
    }
    private void createSoundON(){
        soundON = new ItemStack(Material.NOTE_BLOCK);
        ItemMeta soundONMeta = soundON.getItemMeta();
        soundONMeta.setDisplayName(format(PluginLang.soundON));
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(format(PluginLang.soundONLore));
        soundONMeta.setLore(lore);
        soundON.setItemMeta(soundONMeta);
    }
    private void createSoundOFF(){
        soundOFF = new ItemStack(Material.NOTE_BLOCK);
        ItemMeta soundOFFMeta = soundOFF.getItemMeta();
        soundOFFMeta.setDisplayName(format(PluginLang.soundOFF));
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(format(PluginLang.soundOFFLore));
        soundOFFMeta.setLore(lore);
        soundOFF.setItemMeta(soundOFFMeta);
    }


    private String format(String msg){
        return ChatColor.translateAlternateColorCodes('&', msg);
    }
}
