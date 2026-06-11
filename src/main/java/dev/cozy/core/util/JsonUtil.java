package dev.cozy.core.util;

import com.google.gson.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Map;

public final class JsonUtil {

    private JsonUtil() {
    }

    public static void addToObject(JsonObject json, String key, Object value) {
        if (value == null) {
            json.add(key, null);
        } else if (value instanceof String s) {
            json.addProperty(key, s);
        } else if (value instanceof Number n) {
            json.addProperty(key, n);
        } else if (value instanceof Boolean b) {
            json.addProperty(key, b);
        } else if (value instanceof JsonElement je) {
            json.add(key, je);
        } else if (value instanceof Map<?, ?> map) {
            JsonObject nested = new JsonObject();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                addToObject(nested, entry.getKey().toString(), entry.getValue());
            }
            json.add(key, nested);
        } else if (value instanceof List<?> list) {
            JsonArray arr = new JsonArray();
            for (Object item : list) {
                arr.add(toJsonElement(item));
            }
            json.add(key, arr);
        } else {
            json.addProperty(key, value.toString());
        }
    }

    public static JsonElement toJsonElement(Object value) {
        if (value == null) return JsonNull.INSTANCE;
        if (value instanceof String s) return new JsonPrimitive(s);
        if (value instanceof Number n) return new JsonPrimitive(n);
        if (value instanceof Boolean b) return new JsonPrimitive(b);
        if (value instanceof JsonElement je) return je;
        if (value instanceof Map<?, ?> map) {
            JsonObject obj = new JsonObject();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                addToObject(obj, entry.getKey().toString(), entry.getValue());
            }
            return obj;
        }
        if (value instanceof List<?> list) {
            JsonArray arr = new JsonArray();
            for (Object item : list) {
                arr.add(toJsonElement(item));
            }
            return arr;
        }
        return new JsonPrimitive(value.toString());
    }

    public static JsonObject configToJson(FileConfiguration config) {
        JsonObject root = new JsonObject();
        for (String key : config.getKeys(true)) {
            if (config.isConfigurationSection(key)) continue;
            Object value = config.get(key);
            JsonElement element = toConfigJsonElement(value);
            if (element != null) {
                root.add(key, element);
            }
        }
        return root;
    }

    public static void jsonToConfig(JsonObject json, FileConfiguration config) {
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            String key = entry.getKey();
            JsonElement value = entry.getValue();
            setConfigValue(config, key, value);
        }
    }

    private static JsonElement toConfigJsonElement(Object value) {
        if (value instanceof String s) return new JsonPrimitive(s);
        if (value instanceof Number n) return new JsonPrimitive(n);
        if (value instanceof Boolean b) return new JsonPrimitive(b);
        if (value instanceof List<?> list) {
            JsonArray arr = new JsonArray();
            for (Object item : list) {
                arr.add(toConfigJsonElement(item));
            }
            return arr;
        }
        if (value instanceof ConfigurationSection section) {
            JsonObject obj = new JsonObject();
            for (String key : section.getKeys(false)) {
                obj.add(key, toConfigJsonElement(section.get(key)));
            }
            return obj;
        }
        return value != null ? new JsonPrimitive(value.toString()) : JsonNull.INSTANCE;
    }

    private static void setConfigValue(FileConfiguration config, String key, JsonElement value) {
        if (value.isJsonPrimitive()) {
            JsonPrimitive prim = value.getAsJsonPrimitive();
            if (prim.isNumber()) config.set(key, prim.getAsDouble());
            else if (prim.isBoolean()) config.set(key, prim.getAsBoolean());
            else config.set(key, prim.getAsString());
        } else if (value.isJsonArray()) {
            JsonArray arr = value.getAsJsonArray();
            List<Object> list = arr.asList().stream()
                    .map(e -> e.isJsonPrimitive() ? ((JsonPrimitive) e).getAsString() : e.toString())
                    .toList();
            config.set(key, list);
        } else if (value.isJsonObject()) {
            JsonObject obj = value.getAsJsonObject();
            ConfigurationSection section = config.createSection(key);
            for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                setConfigSectionValue(section, entry.getKey(), entry.getValue());
            }
        }
    }

    private static void setConfigSectionValue(ConfigurationSection section, String key, JsonElement value) {
        if (value.isJsonPrimitive()) {
            JsonPrimitive prim = value.getAsJsonPrimitive();
            if (prim.isNumber()) section.set(key, prim.getAsDouble());
            else if (prim.isBoolean()) section.set(key, prim.getAsBoolean());
            else section.set(key, prim.getAsString());
        } else if (value.isJsonArray()) {
            JsonArray arr = value.getAsJsonArray();
            List<Object> list = arr.asList().stream()
                    .map(e -> e.isJsonPrimitive() ? ((JsonPrimitive) e).getAsString() : e.toString())
                    .toList();
            section.set(key, list);
        } else if (value.isJsonObject()) {
            ConfigurationSection child = section.createSection(key);
            for (Map.Entry<String, JsonElement> entry : value.getAsJsonObject().entrySet()) {
                setConfigSectionValue(child, entry.getKey(), entry.getValue());
            }
        }
    }
}
