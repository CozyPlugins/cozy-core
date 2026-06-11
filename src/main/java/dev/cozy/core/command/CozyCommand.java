package dev.cozy.core.command;

import dev.cozy.core.CozyCore;
import dev.cozy.core.config.ConfigManager;
import dev.cozy.core.database.Database;
import dev.cozy.core.plugin.PluginRegistry;
import dev.cozy.core.web.RelayClient;
import dev.cozy.core.web.SessionRequester;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CozyCommand implements CommandExecutor, TabCompleter {

    private final CozyCore core;
    private final PluginRegistry registry;
    private final ConfigManager configManager;
    private final RelayClient relayClient;
    private final SessionRequester sessionRequester;
    private final Database database;

    public CozyCommand(CozyCore core, PluginRegistry registry, ConfigManager configManager,
                       RelayClient relayClient, SessionRequester sessionRequester, Database database) {
        this.core = core;
        this.registry = registry;
        this.configManager = configManager;
        this.relayClient = relayClient;
        this.sessionRequester = sessionRequester;
        this.database = database;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "editor" -> handleEditor(sender);
            case "status" -> handleStatus(sender);
            case "reload" -> handleReload(sender);
            case "debug" -> handleDebug(sender);
            default -> sendUsage(sender);
        }

        return true;
    }

    private void handleEditor(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this");
            return;
        }

        if (!sender.hasPermission("cozycore.editor")) {
            sender.sendMessage("No permission");
            return;
        }

        player.sendMessage("§7Opening editor...");

        sessionRequester.requestSession(player,
                token -> {
                    String url = configManager.getString("editor.url", "https://cozy.cyzlone.de/editor")
                            + "?session=" + token;
                    Component message = Component.text("[✎ Open Editor]")
                            .color(NamedTextColor.GREEN)
                            .clickEvent(ClickEvent.openUrl(url))
                            .hoverEvent(HoverEvent.showText(Component.text("Click to open")));
                    player.sendMessage(message);
                    player.sendActionBar(Component.text("§aSession expires in 15 minutes"));
                },
                () -> player.sendMessage("§cCould not connect to relay server")
        );
    }

    private void handleStatus(CommandSender sender) {
        if (!sender.hasPermission("cozycore.admin")) {
            sender.sendMessage("No permission");
            return;
        }

        sender.sendMessage("§6§lCozy Plugin Status");
        registry.getAll().forEach(p ->
                sender.sendMessage("§7- §f" + p.getDisplayName() + " §7v" + p.getVersion())
        );
        sender.sendMessage("§7Relay: " + (relayClient.isRelayConnected() ? "§aConnected" : "§cDisconnected"));
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("cozycore.admin")) {
            sender.sendMessage("No permission");
            return;
        }

        configManager.reload();
        sender.sendMessage("§aCozyCore config reloaded");
    }

    private void handleDebug(CommandSender sender) {
        if (!sender.hasPermission("cozycore.admin")) {
            sender.sendMessage("No permission");
            return;
        }

        sender.sendMessage("§6§lDebug Info");
        sender.sendMessage("§7Relay connected: " + relayClient.isRelayConnected());
        sender.sendMessage("§7Registered plugins: " + registry.getAll().size());
        sender.sendMessage("§7Database: " + (database.isConnected() ? "§aOK" : "§cERROR"));
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage("§6§lCozyCore Commands:");
        sender.sendMessage("§7/cozy editor §f- Open the web editor");
        sender.sendMessage("§7/cozy status §f- Show plugin status");
        sender.sendMessage("§7/cozy reload §f- Reload config");
        sender.sendMessage("§7/cozy debug §f- Debug information");
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                @NotNull Command command,
                                                @NotNull String alias,
                                                @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("editor", "status", "reload", "debug");
        }
        return List.of();
    }
}
