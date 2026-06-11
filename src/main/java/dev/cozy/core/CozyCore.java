package dev.cozy.core;

import dev.cozy.core.command.CozyCommand;
import dev.cozy.core.config.ConfigManager;
import dev.cozy.core.database.Database;
import dev.cozy.core.database.MySQLDatabase;
import dev.cozy.core.database.SQLiteDatabase;
import dev.cozy.core.messaging.EventBus;
import dev.cozy.core.plugin.PluginRegistry;
import dev.cozy.core.web.MessageDispatcher;
import dev.cozy.core.web.RelayClient;
import dev.cozy.core.web.SessionRequester;
import dev.cozy.core.web.WebModule;
import org.bukkit.plugin.java.JavaPlugin;

public final class CozyCore extends JavaPlugin {

    private static CozyCore instance;

    private ConfigManager configManager;
    private Database database;
    private PluginRegistry pluginRegistry;
    private EventBus eventBus;
    private MessageDispatcher messageDispatcher;
    private RelayClient relayClient;
    private SessionRequester sessionRequester;
    private WebModule webModule;

    @Override
    public void onEnable() {
        instance = this;

        getLogger().info(" ██████╗ ██████╗ ███████╗██╗   ██╗");
        getLogger().info("██╔════╝██╔═══██╗╚══███╔╝╚██╗ ██╔╝");
        getLogger().info("██║     ██║   ██║  ███╔╝  ╚████╔╝ ");
        getLogger().info("██║     ██║   ██║ ███╔╝    ╚██╔╝  ");
        getLogger().info("╚██████╗╚██████╔╝███████╗   ██║   ");
        getLogger().info(" ╚═════╝ ╚═════╝ ╚══════╝   ╚═╝   ");

        this.configManager = new ConfigManager(this, "config.yml");

        try {
            String dbType = configManager.getString("database.type", "sqlite");
            if (dbType.equalsIgnoreCase("mysql")) {
                this.database = new MySQLDatabase(
                        getLogger(),
                        configManager.getString("database.mysql.host", "localhost"),
                        configManager.getInt("database.mysql.port", 3306),
                        configManager.getString("database.mysql.database", "cozy"),
                        configManager.getString("database.mysql.username", "root"),
                        configManager.getString("database.mysql.password", ""),
                        configManager.getInt("database.mysql.pool-size", 10),
                        configManager.getBoolean("database.mysql.use-ssl", false)
                );
            } else {
                this.database = new SQLiteDatabase(getDataFolder(), getLogger());
            }
            database.connect();
        } catch (Exception e) {
            getLogger().severe("Database connection failed: " + e.getMessage());
        }

        this.eventBus = new EventBus();
        this.pluginRegistry = new PluginRegistry(this);
        this.messageDispatcher = new MessageDispatcher();

        boolean relayEnabled = configManager.getBoolean("relay.enabled", true);
        if (relayEnabled) {
            String relayUrl = configManager.getString("relay.url", "wss://cozy.cyzlone.de");
            this.sessionRequester = new SessionRequester(relayUrl, this);
            this.webModule = new WebModule(this);
        }

        pluginRegistry.autoDiscover();

        if (webModule != null && relayClient != null) {
            webModule.registerHandlers(messageDispatcher, pluginRegistry, relayClient);
        }

        CozyCommand commandExecutor = new CozyCommand(this, pluginRegistry, configManager,
                relayClient, sessionRequester, database);
        getCommand("cozy").setExecutor(commandExecutor);
        getCommand("cozy").setTabCompleter(commandExecutor);

        getLogger().info("CozyCore v" + getDescription().getVersion()
                + " enabled · " + pluginRegistry.getAll().size() + " plugins connected");
    }

    @Override
    public void onDisable() {
        if (relayClient != null && relayClient.isRelayConnected()) {
            relayClient.closeGracefully();
        }
        if (pluginRegistry != null) {
            pluginRegistry.getAll().forEach(p -> pluginRegistry.unregister(p.getId()));
        }
        if (database != null) {
            database.disconnect();
        }
        getLogger().info("CozyCore disabled");
    }

    public void lazyInitRelay() {
        if (relayClient != null && !relayClient.isOpen()) {
            relayClient.reconnect();
        }
    }

    public static CozyCore getInstance() {
        return instance;
    }

    public static Database getDatabase() {
        return instance.database;
    }

    public static PluginRegistry getPluginRegistry() {
        return instance.pluginRegistry;
    }

    public static EventBus getEventBus() {
        return instance.eventBus;
    }

    public static MessageDispatcher getMessageDispatcher() {
        return instance.messageDispatcher;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public RelayClient getRelayClient() {
        return relayClient;
    }

    public SessionRequester getSessionRequester() {
        return sessionRequester;
    }
}
