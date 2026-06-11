package dev.cozy.core.messaging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class EventBus {

    private final Map<String, List<EventHandler<?>>> handlers = new HashMap<>();
    private final Logger logger = Logger.getLogger("CozyCore-EventBus");

    @FunctionalInterface
    public interface EventHandler<T> {
        void handle(T data);
    }

    @SuppressWarnings("unchecked")
    public <T> void subscribe(String event, EventHandler<T> handler) {
        handlers.computeIfAbsent(event, k -> new ArrayList<>()).add(handler);
    }

    @SuppressWarnings("unchecked")
    public <T> void publish(String event, T data) {
        List<EventHandler<?>> eventHandlers = handlers.getOrDefault(event, List.of());
        for (EventHandler<?> handler : eventHandlers) {
            try {
                ((EventHandler<T>) handler).handle(data);
            } catch (Exception e) {
                logger.severe("Error in handler for event '" + event + "': " + e.getMessage());
            }
        }
    }

    public void unsubscribeAll(String event) {
        handlers.remove(event);
    }

    public interface CozyEvents {
        String HUB_PLAYER_JOINED       = "hub.player_joined";
        String ECONOMY_BALANCE_CHANGED = "economy.balance_changed";
        String HOLOGRAM_CREATED        = "holograms.created";
        String HOLOGRAM_DELETED        = "holograms.deleted";
    }
}
