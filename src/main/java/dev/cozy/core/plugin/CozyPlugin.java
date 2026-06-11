package dev.cozy.core.plugin;

import dev.cozy.core.CozyCore;
import dev.cozy.core.config.ConfigManager;

import java.util.Map;

/**
 * Interface that all Cozy plugins must implement to integrate with CozyCore.
 */
public interface CozyPlugin {

    /**
     * Unique identifier for the plugin, e.g. "cozy-hub".
     */
    String getId();

    /**
     * Human-readable display name, e.g. "CozyHub".
     */
    String getDisplayName();

    /**
     * Plugin version string.
     */
    String getVersion();

    /**
     * Called after CozyCore has finished its own initialisation.
     */
    void onCoreLoad(CozyCore core);

    /**
     * Called when the server shuts down or the plugin is unloaded.
     */
    void onCoreUnload();

    /**
     * Reloads the plugin's configuration at runtime.
     */
    void reload();

    /**
     * Returns a map of status data for the web dashboard.
     */
    Map<String, Object> getStatus();

    /**
     * Returns the plugin's configuration manager.
     */
    ConfigManager getConfigManager();
}
