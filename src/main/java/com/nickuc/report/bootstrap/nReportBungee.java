/*
 * This file is part of a NickUC project
 *
 * Copyright (c) NickUC <nickuc.com>
 * https://github.com/nickuc
 */

package com.nickuc.report.bootstrap;

import com.nickuc.report.command.report.BungeeReportCommand;
import com.nickuc.report.listener.BungeeListener;
import com.nickuc.report.model.Settings;
import com.nickuc.report.nReport;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class nReportBungee extends Plugin implements ProxyPlatform<ProxiedPlayer> {

    private nReport plugin;
    private File configFile;

    @Override
    public void onEnable() {
        configFile = new File(getDataFolder(), "config.yml");

        plugin = new nReport(this);
        plugin.enablePlugin();

        PluginManager pluginManager = getProxy().getPluginManager();

        // /report
        pluginManager.registerCommand(this,
                new BungeeReportCommand(plugin, this).createCommand("report", "reportar"));

        // register listeners
        pluginManager.registerListener(this, new BungeeListener(plugin.getUserManagement()));
    }

    @Override
    public void onDisable() {
        if (plugin != null) {
            plugin.disablePlugin();
            plugin = null;
        }
    }

    @Override
    public void print(String message) {
        getProxy().getConsole().sendMessage(TextComponent.fromLegacyText(message));
    }

    @Override
    public Settings loadSettings() {
        ConfigurationProvider provider = YamlConfiguration.getProvider(ConfigurationProvider.class);
        if (!configFile.exists()) {
            try (InputStream inputStream = getResourceAsStream("/config.yml")) {
                Files.copy(inputStream, configFile.toPath());
            } catch (IOException exception) {
                throw new RuntimeException("Could not copy config.yml from JAR!", exception);
            }
        }

        try {
            Configuration configuration = provider.load(configFile);

            List<String> loadedReports = configuration.getStringList("Config.reports");
            int delayReports = configuration.getInt("Config.reportsDelay", 5);
            boolean allowOtherReports = configuration.getBoolean("Config.allow-other-reports");

            return new Settings(loadedReports, delayReports, allowOtherReports);
        } catch (IOException exception) {
            throw new RuntimeException("Could not load config.yml!", exception);
        }
    }

    @Override
    public Stream<ProxiedPlayer> getOnlinePlayers() {
        return getProxy().getPlayers().stream();
    }

    @Nullable
    @Override
    public ProxiedPlayer findPlayer(String playerName) {
        return getProxy().getPlayer(playerName);
    }

    @Override
    public void sendMessage(ProxiedPlayer player, String message) {
        player.sendMessage(TextComponent.fromLegacyText(message));
    }

    @Override
    public void sendMessage(ProxiedPlayer player, String message, @Nullable String hover, @Nullable String suggest) {
        TextComponent textComponent = new TextComponent(TextComponent.fromLegacyText(message));
        if (hover != null) {
            textComponent.setHoverEvent(
                    new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(hover)));
        }
        if (suggest != null) {
            textComponent.setClickEvent(
                    new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, suggest));
        }
        player.sendMessage(textComponent);
    }

    @Override
    public void sendTitle(ProxiedPlayer player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        player.sendTitle(getProxy()
                .createTitle()
                .title(TextComponent.fromLegacyText(title))
                .subTitle(TextComponent.fromLegacyText(subtitle))
                .fadeIn(fadeIn)
                .stay(stay)
                .fadeOut(fadeOut));
    }

    @Override
    public void sendServerConnectMessage(ProxiedPlayer adminPlayer, ProxiedPlayer sender, ProxiedPlayer target) {
        Server targetServer = target.getServer();
        if (targetServer != null && !targetServer.equals(sender.getServer())) {
            String serverName = targetServer.getInfo().getName();
            sendMessage(
                    adminPlayer,
                    " §8▪ §7Servidor: §f\"" + serverName + "\"",
                    "§7Clique aqui para conectar para este servidor.",
                    "/server " + serverName
            );
        }
    }

    @Override
    public String getName(ProxiedPlayer player) {
        return player.getName();
    }

    @Override
    public UUID getUniqueId(ProxiedPlayer player) {
        return player.getUniqueId();
    }

    @Override
    public boolean hasPermission(ProxiedPlayer player, String permission) {
        return player.hasPermission(permission);
    }
}