package dev.cozy.core.web;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import dev.cozy.core.plugin.PluginRegistry;
import dev.cozy.core.util.JsonUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Map;

public class WebModule {

    private final JavaPlugin plugin;

    public WebModule(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void registerHandlers(MessageDispatcher dispatcher, PluginRegistry registry, RelayClient relay) {

        dispatcher.register("ping", payload -> {
            relay.sendMessage("pong", Map.of());
        });

        dispatcher.register("status_request", payload -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                List<Map<String, Object>> plugins = registry.getAll().stream()
                        .map(p -> Map.<String, Object>of(
                                "id", p.getId(),
                                "displayName", p.getDisplayName(),
                                "version", p.getVersion(),
                                "status", p.getStatus()
                        ))
                        .toList();

                relay.sendMessage("status_response", Map.of(
                        "plugins", plugins,
                        "onlinePlayers", Bukkit.getOnlinePlayers().size(),
                        "maxPlayers", Bukkit.getMaxPlayers(),
                        "serverVersion", Bukkit.getVersion(),
                        "tps", Bukkit.getTPS()[0]
                ));
            });
        });

        dispatcher.register("config_get", payload -> {
            String pluginId = payload.get("plugin").getAsString();
            registry.getPlugin(pluginId).ifPresent(p -> {
                JsonObject configJson = JsonUtil.configToJson(p.getConfigManager().getConfig());
                relay.sendMessage("config_response", Map.of("plugin", pluginId, "data", configJson));
            });
        });

        dispatcher.register("config_set", payload -> {
            String pluginId = payload.get("plugin").getAsString();
            JsonObject data = payload.getAsJsonObject("data");
            registry.getPlugin(pluginId).ifPresent(p -> {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    JsonUtil.jsonToConfig(data, p.getConfigManager().getConfig());
                    p.getConfigManager().save();
                    p.reload();
                    relay.sendMessage("config_saved", Map.of("plugin", pluginId, "success", true));
                });
            });
        });
    }
}
