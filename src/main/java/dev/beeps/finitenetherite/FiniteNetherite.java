package dev.beeps.finitenetherite;

import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public final class FiniteNetherite extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(new EventHandler(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
