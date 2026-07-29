package dev.beeps.finitenetherite;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.SingleLineChart;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.Callable;

public class MetricContainer {

    // https://bstats.org/plugin/bukkit/FiniteNetherite/15123
    private static final int PLUGIN_ID = 15123;

    public int itemsDegraded = 0;
    public int mendingXpRedirected = 0;

    private Metrics metrics;

    public MetricContainer(JavaPlugin plugin) {
        // bStats reporting is best-effort: if it can't initialize (e.g. metrics are
        // disabled server-wide), keep the plugin and the in-memory counters working
        // instead of failing onEnable.
        try {
            metrics = new Metrics(plugin, PLUGIN_ID);

            // Both charts drain their counter on read, so each submission reports the
            // activity since the previous one rather than a running total.
            metrics.addCustomChart(new SingleLineChart("items_degraded", new Callable<Integer>() {
                @Override
                public Integer call() {
                    int amount = itemsDegraded;
                    itemsDegraded = 0;
                    return amount;
                }
            }));

            metrics.addCustomChart(new SingleLineChart("mending_xp_redirected", new Callable<Integer>() {
                @Override
                public Integer call() {
                    int amount = mendingXpRedirected;
                    mendingXpRedirected = 0;
                    return amount;
                }
            }));
        } catch (Throwable t) {
            plugin.getLogger().warning("bStats metrics disabled: " + t.getMessage());
        }
    }
}
