package dev.cozy.core.web;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.cozy.core.CozyCore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class SessionRequester {

    private final String relayUrl;
    private final JavaPlugin plugin;

    public SessionRequester(String relayUrl, JavaPlugin plugin) {
        this.relayUrl = relayUrl;
        this.plugin = plugin;
    }

    public void requestSession(Player player, Consumer<String> onSuccess, Runnable onFailure) {
        CozyCore core = CozyCore.getInstance();
        RelayClient relay = core.getRelayClient();

        if (relay != null && !relay.isOpen()) {
            relay.reconnect();
        }

        CompletableFuture.runAsync(() -> {
            try {
                HttpClient client = HttpClient.newHttpClient();

                JsonObject body = new JsonObject();
                body.addProperty("playerName", player.getName());
                body.addProperty("playerUuid", player.getUniqueId().toString());
                body.addProperty("serverIp", Bukkit.getServer().getMotd());

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(relayUrl + "/api/session/create"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                        .timeout(Duration.ofSeconds(10))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    Bukkit.getScheduler().runTask(plugin, onFailure);
                    return;
                }

                JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                String token = json.get("token").getAsString();

                Bukkit.getScheduler().runTask(plugin, () -> onSuccess.accept(token));

            } catch (Exception e) {
                plugin.getLogger().warning("Session request failed: " + e.getMessage());
                Bukkit.getScheduler().runTask(plugin, onFailure);
            }
        });
    }
}
