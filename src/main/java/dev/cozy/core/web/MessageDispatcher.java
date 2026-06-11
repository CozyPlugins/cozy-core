package dev.cozy.core.web;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class MessageDispatcher {

    private final Map<String, List<MessageHandler>> handlers = new HashMap<>();
    private final Logger logger = Logger.getLogger("CozyCore-Dispatcher");

    public void register(String type, MessageHandler handler) {
        handlers.computeIfAbsent(type, k -> new ArrayList<>()).add(handler);
    }

    public void dispatch(String rawJson) {
        try {
            JsonObject json = JsonParser.parseString(rawJson).getAsJsonObject();
            String type = json.get("type").getAsString();

            List<MessageHandler> eventHandlers = handlers.getOrDefault(type, List.of());
            if (eventHandlers.isEmpty()) {
                logger.fine("Unhandled relay message: " + type);
                return;
            }

            for (MessageHandler handler : eventHandlers) {
                try {
                    handler.handle(json);
                } catch (Exception e) {
                    logger.warning("Handler error for message '" + type + "': " + e.getMessage());
                }
            }
        } catch (Exception e) {
            logger.warning("Failed to parse relay message: " + e.getMessage());
        }
    }
}
