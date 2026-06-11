package dev.cozy.core.web;

import com.google.gson.JsonObject;
import dev.cozy.core.util.JsonUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.Map;

public class RelayClient extends WebSocketClient {

    private final String token;
    private final JavaPlugin plugin;
    private final MessageDispatcher dispatcher;
    private boolean intentionallyClosed = false;

    public RelayClient(String relayUrl, String token, JavaPlugin plugin, MessageDispatcher dispatcher) throws Exception {
        super(new URI(relayUrl + "/server?token=" + token));
        this.token = token;
        this.plugin = plugin;
        this.dispatcher = dispatcher;
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        plugin.getLogger().info("Relay connected");
    }

    @Override
    public void onMessage(String message) {
        dispatcher.dispatch(message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        plugin.getLogger().info("Relay disconnected: " + reason);

        if (!intentionallyClosed && remote) {
            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin,
                    () -> reconnect(), 30 * 20L);
        }
    }

    @Override
    public void onError(Exception ex) {
        plugin.getLogger().warning("Relay error: " + ex.getMessage());
    }

    public void reconnect() {
        if (!isOpen()) {
            this.reconnect();
        }
    }

    public void closeGracefully() {
        this.intentionallyClosed = true;
        close();
    }

    public void sendMessage(String type, Map<String, Object> data) {
        JsonObject json = new JsonObject();
        json.addProperty("type", type);
        if (data != null) {
            data.forEach((k, v) -> JsonUtil.addToObject(json, k, v));
        }
        send(json.toString());
    }

    public boolean isRelayConnected() {
        return isOpen();
    }
}
