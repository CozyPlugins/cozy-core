# CozyCore

Core plugin for the Cozy plugin ecosystem on Paper 1.21.4.

## Features

- **Plugin System** — Auto-discovers and manages Cozy plugins via `PluginRegistry`
- **Database** — SQLite (embedded) or MySQL via HikariCP connection pooling
- **WebSocket Relay** — Connects to a Cozy Relay server for browser-based editing
- **Web Editor** — Session-based pairing of Minecraft servers with a web dashboard
- **Config API** — Typed config access with `ConfigManager` and hot-reload
- **Event Bus** — In-process pub/sub for decoupled plugin communication
- **Commands** — `/cozy editor`, `/cozy status`, `/cozy reload`, `/cozy debug`

## Dependencies

| Library | Version | Usage |
|---|---|---|
| Paper API | 1.21.4 | Server API (provided) |
| Java-WebSocket | 1.5.6 | Relay client (shaded) |
| HikariCP | 5.1.0 | Connection pooling (shaded) |
| SQLite JDBC | 3.45.3.0 | Embedded database (shaded) |
| Gson | 2.10.1 | JSON serialisation (shaded) |

All shaded dependencies are relocated to `dev.cozy.libs.*` to avoid conflicts.

## Build

```bash
mvn clean package
```

Output: `target/cozy-core-1.0.0.jar`

## Configuration

`plugins/CozyCore/config.yml`:

```yaml
database:
  type: sqlite            # sqlite or mysql
  sqlite:
    file: data.db
  mysql:
    host: localhost
    port: 3306
    database: cozy
    username: root
    password: ""
    pool-size: 10
    use-ssl: false

relay:
  enabled: true
  url: "wss://cozy.cylone.de"
  reconnect-delay-seconds: 30

editor:
  url: "https://editor.cozy.cylone.de"
```

## API (for plugin developers)

Implement `CozyPlugin` to integrate with the ecosystem:

```java
public class MyPlugin extends JavaPlugin implements CozyPlugin {

    public String getId() { return "my-plugin"; }
    public String getDisplayName() { return "MyPlugin"; }
    public String getVersion() { return getDescription().getVersion(); }

    public void onCoreLoad(CozyCore core) {
        // Initialise after CozyCore is ready
    }

    public void onCoreUnload() {
        // Cleanup on shutdown
    }

    public void reload() {
        reloadConfig();
    }

    public Map<String, Object> getStatus() {
        return Map.of("status", "running");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}
```

## Project Structure

```
src/main/java/dev/cozy/core/
├── CozyCore.java              # Main plugin class
├── command/
│   └── CozyCommand.java       # /cozy command tree
├── config/
│   └── ConfigManager.java     # Typed config with save/reload
├── database/
│   ├── Database.java          # Interface
│   ├── MySQLDatabase.java     # HikariCP + MySQL
│   └── SQLiteDatabase.java    # HikariCP + SQLite
├── messaging/
│   └── EventBus.java          # In-process pub/sub
├── plugin/
│   ├── CozyPlugin.java        # Plugin interface
│   └── PluginRegistry.java    # Plugin discovery & management
├── util/
│   ├── JsonUtil.java          # Gson helpers + config conversion
│   └── UpdateChecker.java     # GitHub release checker
└── web/
    ├── MessageDispatcher.java # Relay message routing
    ├── MessageHandler.java    # Handler interface
    ├── RelayClient.java       # WebSocket client
    ├── SessionRequester.java  # HTTP session creation
    └── WebModule.java         # Handler registration
```
