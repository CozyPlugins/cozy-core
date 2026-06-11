package dev.cozy.core.plugin;

import dev.cozy.core.CozyCore;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.logging.Logger;

/**
 * Central registry that discovers, loads and manages all Cozy plugins.
 * <p>
 * Plugins register themselves here during initialisation and the
 * registry provides lookup methods for the rest of the system.
 */
public class PluginRegistry {

    private final Map<String, CozyPlugin> plugins = new LinkedHashMap<>();
    private final CozyCore core;
    private final Logger logger;

    public PluginRegistry(CozyCore core) {
        this.core = core;
        this.logger = core.getLogger();
    }

    /**
     * Registers a Cozy plugin, calls its load hook and wires up
     * web-module message handlers for config requests.
     */
    public void register(CozyPlugin plugin) {
        plugins.put(plugin.getId(), plugin);
        plugin.onCoreLoad(core);

        String display = plugin.getDisplayName();
        logger.info("§a[CozyCore] §f" + display + " connected");
    }

    /**
     * Unregisters a Cozy plugin and calls its unload hook.
     */
    public void unregister(String id) {
        CozyPlugin plugin = plugins.get(id);
        if (plugin == null) return;

        plugin.onCoreUnload();
        plugins.remove(id);

        String display = plugin.getDisplayName();
        logger.info("§c[CozyCore] §f" + display + " disconnected");
    }

    /**
     * Returns the plugin registered under the given id, if present.
     */
    public Optional<CozyPlugin> getPlugin(String id) {
        return Optional.ofNullable(plugins.get(id));
    }

    /**
     * Returns all currently registered Cozy plugins.
     */
    public Collection<CozyPlugin> getAll() {
        return plugins.values();
    }

    /**
     * Returns whether a plugin with the given id is registered.
     */
    public boolean isRegistered(String id) {
        return plugins.containsKey(id);
    }

    /**
     * Scans all Bukkit plugins loaded on the server and auto-registers
     * any that implement {@link CozyPlugin}.
     */
    public void autoDiscover() {
        int count = 0;

        for (Plugin bukkitPlugin : Bukkit.getPluginManager().getPlugins()) {
            if (bukkitPlugin instanceof CozyPlugin cozy) {
                if (!isRegistered(cozy.getId())) {
                    register(cozy);
                    count++;
                }
            }
        }

        logger.info("Auto-discovered " + count + " Cozy plugins");
    }
}
