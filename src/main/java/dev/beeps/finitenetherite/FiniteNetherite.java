package dev.beeps.finitenetherite;

import com.tcoded.folialib.FoliaLib;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public final class FiniteNetherite extends JavaPlugin {

    private MetricContainer metrics;
    private FoliaLib foliaLib;

    @Override
    public void onEnable() {
        // Plugin startup logic
        // FoliaLib picks the right scheduler at runtime, so the same jar works on
        // Folia's regionised threads and on plain Spigot/Paper.
        foliaLib = new FoliaLib(this);
        metrics = new MetricContainer(this);
        getServer().getPluginManager().registerEvents(new EventHandler(foliaLib, metrics), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
