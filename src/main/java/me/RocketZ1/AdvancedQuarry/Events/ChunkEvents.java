package me.RocketZ1.AdvancedQuarry.Events;

import me.RocketZ1.AdvancedQuarry.Main;
import me.RocketZ1.AdvancedQuarry.Other.Quarry;
import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

public class ChunkEvents implements Listener {
    private Main plugin;

    public ChunkEvents(Main plugin) {
        this.plugin = plugin;
    }

//    @EventHandler
//    public void chunkLoadEvent(ChunkLoadEvent e){
//        for(Quarry quarry : plugin.quarryManager.getQuarries()){
//            Chunk chunk = e.getChunk();
//            if(quarry.getChunkX() == chunk.getX() && quarry.getChunkZ() == chunk.getZ()){
////                quarry.load();
//                break;
//            }
//        }
//    }
}
