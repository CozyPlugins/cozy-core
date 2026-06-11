package dev.cozy.core.web;

import com.google.gson.JsonObject;

@FunctionalInterface
public interface MessageHandler {
    void handle(JsonObject payload);
}
