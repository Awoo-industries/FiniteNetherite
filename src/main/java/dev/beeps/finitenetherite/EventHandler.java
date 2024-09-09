package dev.beeps.finitenetherite;

import org.bukkit.Material;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemMendEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import java.util.HashMap;
import java.util.Map;

public class EventHandler implements Listener {

    public Map<Material, Material> itemMap = new HashMap<Material, Material>();

    public EventHandler(){
        itemMap.put(Material.NETHERITE_HELMET, Material.DIAMOND_HELMET);
        itemMap.put(Material.NETHERITE_CHESTPLATE, Material.DIAMOND_CHESTPLATE);
        itemMap.put(Material.NETHERITE_LEGGINGS, Material.DIAMOND_LEGGINGS);
        itemMap.put(Material.NETHERITE_BOOTS, Material.DIAMOND_BOOTS);
        itemMap.put(Material.NETHERITE_SWORD, Material.DIAMOND_SWORD);
        itemMap.put(Material.NETHERITE_PICKAXE, Material.DIAMOND_PICKAXE);
        itemMap.put(Material.NETHERITE_SHOVEL, Material.DIAMOND_SHOVEL);
        itemMap.put(Material.NETHERITE_AXE, Material.DIAMOND_AXE);
        itemMap.put(Material.NETHERITE_HOE, Material.DIAMOND_HOE);
    }

    @org.bukkit.event.EventHandler(ignoreCancelled = true)
    public void onPlayerItemMend(PlayerItemMendEvent event) {
        // Make mending not work on netherite
        if(itemMap.get(event.getItem().getType()) != null){
            event.setCancelled(true);
        }
    }

    @org.bukkit.event.EventHandler(ignoreCancelled = true)
    public void onPlayerItemBreak(PlayerItemBreakEvent event) {

        ItemStack item = event.getBrokenItem();
        Material type = item.getType();

        if(itemMap.get(type) != null){
            item.setAmount(2);  //Set amount to 2 so the break event still goes trough but we're not left with 0 items.
            item.setType(itemMap.get(type));

            Damageable meta = (Damageable) item.getItemMeta();
            assert meta != null;

            meta.setDamage(0);
            item.setItemMeta((org.bukkit.inventory.meta.ItemMeta) meta);

        }
    }

}
