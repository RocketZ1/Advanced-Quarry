package me.RocketZ1.AdvancedQuarry.Other;

import me.RocketZ1.AdvancedQuarry.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockVector;

import java.util.*;

public class Quarry {
    private Location location;
    private UUID owner;
    private String ownerName;

    private Chest quarryChest;
    private boolean inProgress = false;
    private QuarryEffects quarryEffects;
    private boolean playSounds = true;

    private int quarryRadius;
    private int depthLimit;
    private int quarryDelay = 20;
    private int minYLevel;

    private long quarryStopTime;

    private Location pos1 = null;
    private Location pos2 = null;

    private final int chunkX;
    private final int chunkZ;

    private QuarryManager manager;

    public Quarry(Location location, UUID owner_uuid, String owner_name, int quarryRadius, int depthLimit){
        this.location = location;
        this.owner = owner_uuid;
        this.ownerName = owner_name;
        this.quarryRadius = quarryRadius;
        this.depthLimit = depthLimit;
        this.quarryEffects = new QuarryEffects(this.location, this);
        getServerVersion();
        this.chunkX = location.getChunk().getX();
        this.chunkZ = location.getChunk().getZ();
    }

    private void getServerVersion(){
        if(Bukkit.getBukkitVersion().startsWith("1.18")){
            minYLevel = -64;
        }else{
            minYLevel = 0;
        }
    }

    private void setPos(){
        pos1 = new Location(location.getWorld(), location.getBlockX()+quarryRadius, location.getBlockY()-1, location.getBlockZ()+quarryRadius);
        if(depthLimit == -1){
            pos2 = new Location(location.getWorld(), location.getBlockX()-quarryRadius, minYLevel, location.getBlockZ()-quarryRadius);
        }else{
            pos2 = new Location(location.getWorld(), location.getBlockX()-quarryRadius, location.getBlockY()-depthLimit, location.getBlockZ()-quarryRadius);
        }
    }

    public Location getPos1(){
        if(pos1 == null) setPos();
        return pos1;
    }
    public Location getPos2(){
        if(pos2 == null) setPos();
        return pos2;
    }
    public Location getQuarryLoc(){
        return this.location;
    }
    public UUID getOwner(){
        return this.owner;
    }
    public String getOwnerName(){
        return this.ownerName;
    }
    public void setQuarryChest(Chest chest){
        this.quarryChest = chest;
    }
    public Chest getQuarryChest(){
        return this.quarryChest;
    }
    public boolean inProgress(){
        return this.inProgress;
    }
    public void showBorder(Player p, Main plugin){
        this.quarryEffects.showBorder(this.location, p, this.quarryRadius, plugin);
    }
    public boolean isSoundEnabled(){
        return this.playSounds;
    }
    public void setPlaySounds(boolean bool){
        this.playSounds = bool;
    }
    public long getQuarryStopTime() {
        if(System.currentTimeMillis() >= quarryStopTime) return 0;
        return quarryStopTime;
    }
    public long getRunnableTimeLeft(){
        if(this.quarryStopTime != -1){
            return quarryStopTime - System.currentTimeMillis();
        }
        return -1;
    }
    public void setRunnableTimeLeft(long time){
        this.unloadedQuarryStopTime = time;
    }
    public int getChunkX(){
        return this.chunkX;
    }
    public int getChunkZ(){
        return this.chunkZ;
    }
    public Player getStartedPlayer(){
        return this.quarryStarter;
    }
    public void setStartedPlayer(UUID uuid){
        for(Player p : Bukkit.getOnlinePlayers()){
            if(p.getUniqueId().equals(uuid)){
                this.quarryStarter = p;
                return;
            }
        }
        this.quarryStarter = Bukkit.getOfflinePlayer(uuid).getPlayer();
    }

    public boolean RunQuarry(Main plugin, Player p){
        if(this.quarryChest == null) return false;
        if(this.inProgress){
            stopQuarry();
            return true;
        }
        this.quarryDelay = plugin.quarryDelay;
        this.inProgress = true;
        this.quarryStarter = p;
        if(plugin.consumeOnStart){
            consumeItems(plugin);
        }
        Location loc1 = new Location(location.getWorld(), location.getBlockX()+quarryRadius, location.getBlockY()-1, location.getBlockZ()+quarryRadius);
        Location loc2;
        if(plugin.quarryDepthLimit == -1){
            loc2 = new Location(location.getWorld(), location.getBlockX()-quarryRadius, minYLevel, location.getBlockZ()-quarryRadius);
        }else{
            loc2 = new Location(location.getWorld(), location.getBlockX()-quarryRadius, location.getBlockY()-plugin.quarryDepthLimit-1, location.getBlockZ()-quarryRadius);
        }
        int minX = Math.min(loc1.getBlockX(), loc2.getBlockX());
        int minY = Math.min(loc1.getBlockY(), loc2.getBlockY());
        int minZ = Math.min(loc1.getBlockZ(), loc2.getBlockZ());
        int maxX = Math.max(loc1.getBlockX(), loc2.getBlockX()) + 1;
        int maxY = Math.max(loc1.getBlockY(), loc2.getBlockY());
        int maxZ = Math.max(loc1.getBlockZ(), loc2.getBlockZ()) + 1;
        int xLength = Math.abs(maxX-minX);
        int yLength = Math.abs(maxY-minY);
        int zLength = Math.abs(maxZ-minZ);
        int size = xLength * yLength * zLength;
        int[] xCords = new int[size];
        int[] yCords = new int[size];
        int[] zCords = new int[size];
        int i=0;
        for (int y = maxY; y >= minY; y--) {
           for (int x = minX; maxX > x; x++) {
                for (int z = minZ; maxZ > z; z++) {
                    Location loc = new Location(getQuarryLoc().getWorld(), x, y,z);
                    if(loc.getBlock().getType().isAir()) continue;
                    if (plugin.chosenList == 2) {
                        if (!plugin.whitelistedBLocks.contains(loc.getBlock().getType())) continue;
                    } else {
                        if (plugin.blacklistedBlocks.contains(loc.getBlock().getType())) continue;
                    }
                    if(!dependenciesCanBuild(loc, p, plugin)) continue;
                    xCords[i] = x;
                    yCords[i] = y;
                    zCords[i] = z;
                    i++;
                }
            }
        }
        quarryRunnable(plugin, xCords, yCords, zCords, false, i+1);
        return true;
    }

    private boolean dependenciesCanBuild(Location loc, Player p, Main plugin){
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

    private void consumeItems(Main plugin){
        Map<Material, Integer> requirements = new HashMap<>();
        for(Material material : plugin.quarryRequirements.keySet()){
            requirements.put(material, plugin.quarryRequirements.get(material));
        }
        for(ItemStack chestItems : this.quarryChest.getInventory().getContents()){
            if(chestItems == null) continue;
            if(requirements.containsKey(chestItems.getType())){
                int requiredAmt = requirements.get(chestItems.getType());
                int chestItemAmt = chestItems.getAmount();
                if(chestItemAmt > requiredAmt) {
                    requirements.remove(chestItems.getType());
                    chestItems.setAmount(chestItemAmt - requiredAmt);
                }else if(requiredAmt == chestItemAmt){
                    requirements.remove(chestItems.getType());
                    chestItems.setAmount(0);
                }else {
                    requirements.put(chestItems.getType(), requiredAmt-chestItemAmt);
                    chestItems.setAmount(0);
                }
            }
        }
    }

    private Player quarryStarter = null;

    private void quarryRunnable(Main plugin, int[] xCords, int[] yCords, int[] zCords, boolean fromUnloaded, int blockAmtLength) {
        if (quarryEffects != null){
            quarryEffects.removeAllEffects();
            quarryEffects = new QuarryEffects(location, quarryEffects.getBlocks());
        }else{
            quarryEffects = new QuarryEffects(location, this);
        }
        quarryEffects.showStand(true);
        if (!fromUnloaded) {
            if (plugin.quarryRuntime != -1) {
                this.quarryStopTime = System.currentTimeMillis() + (plugin.quarryRuntime * 1000L);
            } else {
                this.quarryStopTime = -1;
            }
        } else {
            if (plugin.quarryRuntime != -1) {
                this.quarryStopTime = System.currentTimeMillis() + this.unloadedQuarryStopTime;
            } else {
                this.quarryStopTime = -1;
            }
        }
        int[] i = {0};
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                if(i[0] == blockAmtLength){
                    cancel();
                    stopQuarry();
                    return;
                }
                boolean cancel = runQuarryAt(new Location(getQuarryLoc().getWorld(), xCords[i[0]], yCords[i[0]], zCords[i[0]]), plugin);
                i[0]++;
                if (cancel) {
                    cancel();
                    stopQuarry();
                    return;
                }
            }
        };runnable.runTaskTimer(plugin, 0, this.quarryDelay);
    }

    private boolean runQuarryAt(Location loc, Main plugin){
        if(!loc.getChunk().isLoaded()) return true;
        if(loc.getBlock().getType().isAir()) return false;
        if (quarryStopTime != -1 && System.currentTimeMillis() >= quarryStopTime) {
            stopQuarry();
            return true;
        }
        if (quarryChest.getBlock().getType() != Material.CHEST) {
            stopQuarry();
            return true;
        }
        if (plugin.stopQuarryWhenChestFull && quarryChest.getInventory().firstEmpty() == -1) {
            stopQuarry();
            return true;
        }
        if (!inProgress) {
            stopQuarry();
            return true;
        }
        Inventory chestInv = quarryChest.getInventory();
        if (plugin.quarryManager.existingQuarry(loc)) {
            return false;
        }
        Block currentBlock = loc.getBlock();
        quarryEffects.runEffects(currentBlock.getLocation(), location, playSounds);
        if (playSounds) location.getWorld().playSound(location, Sound.ENTITY_MINECART_RIDING, 1, 1);
        if(plugin.mineButNoCollect.contains(currentBlock.getType())){
            currentBlock.setType(Material.AIR);
            return false;
        }
        Collection<ItemStack> items = currentBlock.getDrops();
        if (currentBlock.getState() instanceof Container) {
            Inventory inv = ((Container) currentBlock.getState()).getInventory();
            for (ItemStack item : inv.getContents()) {
                if (item == null) continue;
                items.add(item);
            }
        }
        if (quarryChest.getBlock().getType() != Material.CHEST) {
            for (ItemStack item : items) {
                currentBlock.getWorld().dropItemNaturally(currentBlock.getLocation(), item);
            }
        } else {
            for (ItemStack item : items) {
                HashMap<Integer, ItemStack> itemsLeft = chestInv.addItem(item);
                if (!itemsLeft.isEmpty()) {
                    for (ItemStack itemsLeftOver : itemsLeft.values()) {
                        currentBlock.getWorld().dropItemNaturally(currentBlock.getLocation(), itemsLeftOver);
                    }
                }
            }
        }
        currentBlock.setType(Material.AIR);
        return false;
    }

    public void destroyQuarry(){
        quarryEffects.removeAllEffects();
    }

    public void stopQuarry(){
        this.inProgress = false;
        quarryEffects.showStand(false);
        quarryEffects.removeAllEffects();
        quarryEffects = new QuarryEffects(location, quarryEffects.getBlocks()); //Get players and replace it in the list for the border
    }

    public void load(Main plugin, BlockVector lowPos1, BlockVector highPos2){
        int minX = Math.min(lowPos1.getBlockX(), highPos2.getBlockX());
        int minY = Math.min(lowPos1.getBlockY(), highPos2.getBlockY());
        int minZ = Math.min(lowPos1.getBlockZ(), highPos2.getBlockZ());
        int maxX = Math.max(lowPos1.getBlockX(), highPos2.getBlockX()) + 1;
        int maxY = Math.max(lowPos1.getBlockY(), highPos2.getBlockY());
        int maxZ = Math.max(lowPos1.getBlockZ(), highPos2.getBlockZ()) + 1;
        int size = (maxX-minX) * (maxY-minY) * (maxZ-minZ);
        int[] xCords = new int[size];
        int[] yCords = new int[size];
        int[] zCords = new int[size];
        int i=0;
        for (int y = minY; y >= minY; y--) {
            for (int x = minX; maxX >= x; x++) {
                for (int z = minZ; maxZ >= z; z++) {
                    xCords[i] = x;
                    yCords[i] = y;
                    zCords[i] = z;
                    i++;
                }
            }
        }
        quarryRunnable(plugin, xCords, yCords, zCords, true, i+1);
        this.unloadedQuarryStopTime = -1;
    }

    private long unloadedQuarryStopTime = -1;
    private BlockVector unloadedPos1;
    private BlockVector unloadedPos2;

    private void unload(BlockVector lowPos1, BlockVector highPos2){
        this.unloadedPos1 = lowPos1;
        this.unloadedPos2 = highPos2;
        if(this.quarryStopTime != -1){
            this.unloadedQuarryStopTime = quarryStopTime - System.currentTimeMillis();
        }
    }
}
