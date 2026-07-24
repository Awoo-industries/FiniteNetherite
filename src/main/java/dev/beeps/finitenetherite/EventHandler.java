package dev.beeps.finitenetherite;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemMendEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

public class EventHandler implements Listener {

    // Vanilla mending converts 1 XP into 2 points of durability.
    private static final int DURABILITY_PER_XP = 2;

    // Slots mending considers on a player: both hands plus the four armor pieces.
    private static final EquipmentSlot[] MENDABLE_SLOTS = {
            EquipmentSlot.HAND, EquipmentSlot.OFF_HAND,
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    private final Plugin plugin;

    public Map<Material, Material> itemMap = new HashMap<Material, Material>();

    public EventHandler(Plugin plugin){
        this.plugin = plugin;

        itemMap.put(Material.NETHERITE_HELMET, Material.DIAMOND_HELMET);
        itemMap.put(Material.NETHERITE_CHESTPLATE, Material.DIAMOND_CHESTPLATE);
        itemMap.put(Material.NETHERITE_LEGGINGS, Material.DIAMOND_LEGGINGS);
        itemMap.put(Material.NETHERITE_BOOTS, Material.DIAMOND_BOOTS);
        itemMap.put(Material.NETHERITE_SWORD, Material.DIAMOND_SWORD);
        itemMap.put(Material.NETHERITE_PICKAXE, Material.DIAMOND_PICKAXE);
        itemMap.put(Material.NETHERITE_SHOVEL, Material.DIAMOND_SHOVEL);
        itemMap.put(Material.NETHERITE_AXE, Material.DIAMOND_AXE);
        itemMap.put(Material.NETHERITE_HOE, Material.DIAMOND_HOE);

        putIfPresent("NETHERITE_SPEAR", "DIAMOND_SPEAR");
    }

    // Adds a mapping only if both materials exist on the running server,
    // keeping support for newer items without bumping the minimum version.
    private void putIfPresent(String from, String to) {
        Material fromMaterial = Material.getMaterial(from);
        Material toMaterial = Material.getMaterial(to);
        if (fromMaterial != null && toMaterial != null) {
            itemMap.put(fromMaterial, toMaterial);
        }
    }

    @org.bukkit.event.EventHandler(ignoreCancelled = true)
    public void onPlayerItemMend(PlayerItemMendEvent event) {
        // Only intervene when mending targets one of our finite (netherite) items.
        if (!itemMap.containsKey(event.getItem().getType())) {
            return;
        }

        // Netherite can't be mended. Cancelling here means the server hands the full orb XP
        // straight to the player (vanilla's behaviour for a cancelled mend).
        event.setCancelled(true);

        ExperienceOrb orb = event.getExperienceOrb();
        int orbXp = orb != null ? orb.getExperience() : 0;
        if (orbXp <= 0) {
            return;
        }

        // Try to spend the XP on another eligible item instead of wasting the mend.
        int spentXp = redirectMending(event.getPlayer(), orbXp);
        if (spentXp > 0) {
            // The player is about to receive the full orb XP (from the cancel, next tick).
            // Claw back the portion we spent repairing the redirected item so the XP is
            // genuinely redirected rather than duplicated. If no item was found, this is
            // skipped and the player simply keeps the XP.
            plugin.getServer().getScheduler().runTask(plugin, () -> event.getPlayer().giveExp(-spentXp));
        }
    }

    // Repairs the most-damaged eligible item the player is wearing/holding using the orb's XP.
    // Eligible = damaged, Mending-enchanted, and not one of our finite items.
    // Returns the XP consumed by the repair (0 if there was no valid candidate).
    private int redirectMending(Player player, int orbXp) {
        PlayerInventory inventory = player.getInventory();

        EquipmentSlot targetSlot = null;
        ItemStack targetItem = null;
        int targetDamage = 0;

        for (EquipmentSlot slot : MENDABLE_SLOTS) {
            ItemStack item = inventory.getItem(slot);
            if (item == null || item.getType().isAir()) {
                continue;
            }
            if (itemMap.containsKey(item.getType())) {
                continue; // never redirect onto another finite item
            }
            if (!item.containsEnchantment(Enchantment.MENDING)) {
                continue;
            }
            if (!(item.getItemMeta() instanceof Damageable)) {
                continue;
            }
            int damage = ((Damageable) item.getItemMeta()).getDamage();
            if (damage > targetDamage) {
                targetDamage = damage;
                targetSlot = slot;
                targetItem = item;
            }
        }

        if (targetSlot == null) {
            return 0;
        }

        int repair = Math.min(orbXp * DURABILITY_PER_XP, targetDamage);

        Damageable meta = (Damageable) targetItem.getItemMeta();
        meta.setDamage(targetDamage - repair);
        targetItem.setItemMeta(meta);
        inventory.setItem(targetSlot, targetItem);

        return repair / DURABILITY_PER_XP;
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
