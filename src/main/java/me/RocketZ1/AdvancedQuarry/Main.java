package me.RocketZ1.AdvancedQuarry;

import me.RocketZ1.AdvancedQuarry.Commands.AdvancedQuarryCmd;
import me.RocketZ1.AdvancedQuarry.Dependencies.GriefPreventionDependency;
import me.RocketZ1.AdvancedQuarry.Dependencies.WorldGuardDependency;
import me.RocketZ1.AdvancedQuarry.Events.*;
import me.RocketZ1.AdvancedQuarry.Files.ConfigManager;
import me.RocketZ1.AdvancedQuarry.Files.DataManager;
import me.RocketZ1.AdvancedQuarry.Files.LangManager;
import me.RocketZ1.AdvancedQuarry.Other.MenuItems;
import me.RocketZ1.AdvancedQuarry.Other.PluginLang;
import me.RocketZ1.AdvancedQuarry.Other.Quarry;
import me.RocketZ1.AdvancedQuarry.Other.QuarryManager;
import org.bukkit.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.logging.Level;

public class Main extends JavaPlugin {

    //Perm list
    //advancedquarry.admin
    //advancedquarry.craft

    public ArrayList<Material> blacklistedBlocks = new ArrayList<>();
    public ArrayList<Material> whitelistedBLocks = new ArrayList<>();
    public ArrayList<Material> mineButNoCollect = new ArrayList<>();
    public List<String> blacklistedWorlds = new ArrayList<>();
    public int chosenList = 1;

    public int quarryRuntime = -1;

    public int quarryDelay = 20;
    public int quarryRadius = 10;
    public int quarryDepthLimit = -1;
    public Map<Material, Integer> quarryRequirements = new HashMap<>();
    public boolean consumeOnStart = true;
    public boolean oneQuarryPerRadius = true;
    public boolean stopQuarryWhenChestFull = false;

    public Map<Material, Integer> craftQuarryRequirements = new HashMap<>();
    public boolean canCraftQuarry = false;

    public ConfigManager config;
    public DataManager data;
    public LangManager language;

    public QuarryManager quarryManager;
    public MenuItems menuItems;

    public WorldGuardDependency worldGuardDependency = null;
    public GriefPreventionDependency griefPreventionDependency = null;

    @Override
    public void onEnable(){
        this.config = new ConfigManager(this);
        this.data = new DataManager(this);
        this.language = new LangManager(this);
        setupDependencies();
        getConfigInfo();
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new placeQuarry(this),this);
        pm.registerEvents(new openQuarryGUI(this), this);
        pm.registerEvents(new UseQuarry(this), this);
        pm.registerEvents(new BreakQuarry(this), this);
        pm.registerEvents(new CraftQuarry(this), this);
        new AdvancedQuarryCmd(this);
        this.quarryManager = new QuarryManager(this);
        this.menuItems = new MenuItems();
        loadQuarries();
    }

    private void setupDependencies() {
        worldGuardDependency = new WorldGuardDependency(this);
        griefPreventionDependency = new GriefPreventionDependency(this);
    }

    private void loadQuarries(){
        int i = 1;
        while(data.getConfig().contains("quarries."+i)){
            World world = null;
            if(data.getConfig().contains("quarries."+i+".location.world")){
                world = Bukkit.getWorld(data.getConfig().getString("quarries."+i+".location.world"));
                if(world == null){
                    i++;
                    continue;
                }
            }
            int x = data.getConfig().getInt("quarries."+i+".location.x");
            int y = data.getConfig().getInt("quarries."+i+".location.y");
            int z = data.getConfig().getInt("quarries."+i+".location.z");
            Location loc = new Location(world, x, y, z);
            UUID uuid = UUID.fromString(data.getConfig().getString("quarries."+i+".owner_uuid"));
            String name = data.getConfig().getString("quarries."+i+".owner_name");
            Quarry quarry = new Quarry(loc, uuid, name, this.quarryRadius, this.quarryDepthLimit);
            long timeLeft = data.getConfig().getLong("quarries."+i+".time_left");
            if(timeLeft != -1){
                quarry.setRunnableTimeLeft(timeLeft);
            }
            String stringUUID = data.getConfig().getString("quarries."+i+".started_player_uuid");
            UUID startedPlayerUUID;
            try{
                startedPlayerUUID = UUID.fromString(stringUUID);
                quarry.setStartedPlayer(startedPlayerUUID);
            }catch (Exception ignored){}
            quarryManager.addQuarry(quarry);
            i++;
        }
    }

    @Override
    public void onDisable() {
        data.getConfig().set("quarries", null);
        int i = 1;
        if (!quarryManager.getQuarries().isEmpty()) {
            for (Quarry quarry : quarryManager.getQuarries()) {
                data.getConfig().set("quarries." + i + ".location.world", quarry.getQuarryLoc().getWorld().getName());
                data.getConfig().set("quarries." + i + ".location.x", quarry.getQuarryLoc().getBlockX());
                data.getConfig().set("quarries." + i + ".location.y", quarry.getQuarryLoc().getBlockY());
                data.getConfig().set("quarries." + i + ".location.z", quarry.getQuarryLoc().getBlockZ());
                data.getConfig().set("quarries." + i + ".owner_uuid", quarry.getOwner().toString());
                data.getConfig().set("quarries." + i + ".owner_name", quarry.getOwnerName());
                data.getConfig().set("quarries."+i+".time_left", quarry.getRunnableTimeLeft());
                if(quarry.getStartedPlayer() != null){
                    data.getConfig().set("quarries."+i+".started_player_uuid", quarry.getStartedPlayer().getUniqueId().toString());
                }
                i++;
                data.saveConfig();
                quarry.destroyQuarry();
            }
        }
    }

    public void getConfigInfo(){
        if(config.getConfig().contains("quarry-radius")){
            this.quarryRadius = config.getConfig().getInt("quarry-radius");
        }
        if(config.getConfig().contains("quarry-vert-depth")){
            this.quarryDepthLimit = config.getConfig().getInt("quarry-vert-depth");
        }
        if(config.getConfig().contains("one-quarry-per-radius")){
            this.oneQuarryPerRadius = config.getConfig().getBoolean("one-quarry-per-radius");
        }
        if(config.getConfig().contains("quarry-requirements")){
            for(String itemString : config.getConfig().getConfigurationSection("quarry-requirements").getKeys(false)){
                if(Material.matchMaterial(itemString) != null) {
                    int itemAmt = config.getConfig().getInt("quarry-requirements."+itemString+".amount");
                    this.quarryRequirements.put((Material.matchMaterial(itemString)), itemAmt);
                }
            }
        }
        if(config.getConfig().contains("consume-requirements-on-start")){
            this.consumeOnStart = config.getConfig().getBoolean("consume-requirements-on-start");
        }
        if(config.getConfig().contains("quarry-delay")){
            this.quarryDelay = config.getConfig().getInt("quarry-delay");
        }
        if(config.getConfig().contains("chosen-list")){
            this.chosenList = config.getConfig().getInt("chosen-list");
        }else{
            this.chosenList = 1;
        }
        if(this.chosenList == 2){
            setChosenList("whitelist");
        }else {
            //chosen list must be one or invalid
            setChosenList("blacklist");
        }
        blacklistedBlocks.add(Material.AIR);
        blacklistedBlocks.add(Material.CAVE_AIR);
        blacklistedBlocks.add(Material.VOID_AIR);
        if(config.getConfig().contains("craftable-quarry")){
            this.canCraftQuarry = config.getConfig().getBoolean("craftable-quarry");
        }
        if(this.canCraftQuarry && config.getConfig().contains("craftable-quarry-requirements")){
            for(String itemString : config.getConfig().getConfigurationSection("craftable-quarry-requirements").getKeys(false)){
                if(Material.matchMaterial(itemString) != null) {
                    int itemAmt = config.getConfig().getInt("craftable-quarry-requirements."+itemString+".amount");
                    this.craftQuarryRequirements.put((Material.matchMaterial(itemString)), itemAmt);
                }
            }
        }
        if(this.config.getConfig().contains("stop-quarry-no-room")){
            this.stopQuarryWhenChestFull = this.config.getConfig().getBoolean("stop-quarry-no-room");
        }
        if(this.config.getConfig().contains("quarry-runtime")) {
            this.quarryRuntime = this.config.getConfig().getInt("quarry-runtime");
        }else{
            this.quarryRuntime = -1;
        }
        if(this.config.getConfig().contains("blacklisted-worlds")){
            this.blacklistedWorlds = this.config.getConfig().getStringList("blacklisted-worlds");
        }
        if(this.config.getConfig().contains("mine-blocks-without-collecting-list")){
            List<String> list = config.getConfig().getStringList("mine-blocks-without-collecting-list");
            if (!list.isEmpty()) {
                list.forEach(mat -> {
                    if (Material.getMaterial(mat) != null) {
                        mineButNoCollect.add(Material.getMaterial(mat));
                    }
                });
            }
        }
        setupLang();
    }

    private void setupLang(){
        PluginLang.quarryName = language.getConfig().getString("quarry-name");
        PluginLang.quarryLore = new ArrayList<>(language.getConfig().getStringList("quarry-lore"));
        PluginLang.invalidCmd = language.getConfig().getString("invalid-command");
        PluginLang.invalidCmdArgs = language.getConfig().getString("invalid-command-args");
        PluginLang.invalidWorld = language.getConfig().getString("invalid-world");
        PluginLang.noPerms = language.getConfig().getString("no-permission");
        PluginLang.notOnline = language.getConfig().getString("not-online");
        PluginLang.bypassOwnership = language.getConfig().getString("bypass-ownership");
        PluginLang.itemGivenInvFull = language.getConfig().getString("item-given-inventory-full");
        PluginLang.itemGiven = language.getConfig().getString("item-given");
        PluginLang.incorrectQuarryUserBreak = language.getConfig().getString("incorrect-user-quarry-break");
        PluginLang.breakQuarryInProgress = language.getConfig().getString("cannot-break-quarry-inprogress");
        PluginLang.missingItems = language.getConfig().getString("missing-items");
        PluginLang.requiredItemsRemaining = language.getConfig().getString("required-item-remaining");
        PluginLang.requirementsMet = language.getConfig().getString("requirements-met");
        PluginLang.valid = language.getConfig().getString("valid");
        PluginLang.invalid = language.getConfig().getString("invalid");
        PluginLang.quarryProgressTxt = language.getConfig().getString("quarry-progress-txt");
        PluginLang.quarryProgress = language.getConfig().getString("quarry-progress");
        PluginLang.quarryStopTimeMsg = language.getConfig().getString("quarry-stoptime-msg");
        PluginLang.quarryRegionOverlap = language.getConfig().getString("quarry-region-overlap");
        PluginLang.notEnoughItems = language.getConfig().getString("not-enough-items");
        PluginLang.displayBorderON = language.getConfig().getString("display-border-on");
        PluginLang.displayBorderOFF = language.getConfig().getString("display-border-off");
        PluginLang.drillName = language.getConfig().getString("drill-name");
        PluginLang.runQuarry = language.getConfig().getString("run-quarry-txt");
        PluginLang.stopQuarry = language.getConfig().getString("stop-quarry-txt");
        PluginLang.displayBorder = language.getConfig().getString("display-border-txt");
        PluginLang.soundON = language.getConfig().getString("sound-on-txt");
        PluginLang.soundONLore = language.getConfig().getString("sound-on-lore");
        PluginLang.soundOFF = language.getConfig().getString("sound-off-txt");
        PluginLang.soundOFFLore = language.getConfig().getString("sound-off-lore");
        PluginLang.configReloaded = language.getConfig().getString("config-reloaded");
    }

    private void setChosenList(String option){
        if(option.equalsIgnoreCase("blacklist")) {
            if (config.getConfig().contains("blacklisted-blocks")) {
                List<String> list = config.getConfig().getStringList("blacklisted-blocks");
                if (!list.isEmpty()) {
                    list.forEach(mat -> {
                        if (Material.getMaterial(mat) != null) {
                            blacklistedBlocks.add(Material.getMaterial(mat));
                        }
                    });
                }
            } else {
                getServer().getLogger().log(Level.INFO, format("blacklisted-blocks in config.yml not found! There are no BlackListed blocks."));
            }
        }else if(option.equalsIgnoreCase("whitelist")) {
            if (config.getConfig().contains("whitelisted-blocks")) {
                List<String> list = config.getConfig().getStringList("whitelisted-blocks");
                if (!list.isEmpty()) {
                    list.forEach(mat -> {
                        if (Material.getMaterial(mat) != null) {
                            whitelistedBLocks.add(Material.getMaterial(mat));
                        }
                    });
                }
            } else {
                getServer().getLogger().log(Level.INFO, format("whitelisted-blocks in config.yml not found! There are no Whitelisted blocks."));
            }
        }
    }

    public String format(String msg){
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public ItemStack Quarry(){
        ItemStack quarry = new ItemStack(Material.PISTON);
        ItemMeta quarryMeta = quarry.getItemMeta();
        quarryMeta.setDisplayName(format(PluginLang.quarryName));
        ArrayList<String> lore = new ArrayList<>();
        for(String line : PluginLang.quarryLore){
            lore.add(format(line));
        }
        quarryMeta.setLore(lore);
        quarry.setItemMeta(quarryMeta);
        return quarry;
    }
}
