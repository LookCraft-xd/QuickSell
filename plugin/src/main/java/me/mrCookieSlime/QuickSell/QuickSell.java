package me.mrCookieSlime.QuickSell;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import dev.rollczi.litecommands.LiteCommands;
import dev.rollczi.litecommands.argument.ArgumentKey;
import dev.rollczi.litecommands.bukkit.LiteBukkitFactory;
import me.mrCookieSlime.QuickSell.commands.*;
import me.mrCookieSlime.QuickSell.commands.extra.BoosterPlayerArgument;
import me.mrCookieSlime.QuickSell.commands.extra.BoosterTypeArgument;
import me.mrCookieSlime.QuickSell.commands.extra.CommandInvalidUsage;
import me.mrCookieSlime.QuickSell.commands.extra.CommandMissingPermissionHandler;
import me.mrCookieSlime.QuickSell.core.utils.message.MessageHandler;
import me.mrCookieSlime.QuickSell.inventories.ShopEditor;
import me.mrCookieSlime.QuickSell.inventories.ShopMenu;
import me.mrCookieSlime.QuickSell.listeners.LoggerListener;
import me.mrCookieSlime.QuickSell.listeners.SignSellListener;
import me.mrCookieSlime.QuickSell.listeners.XPBoosterListener;
import me.mrCookieSlime.QuickSell.logs.LogManager;
import me.mrCookieSlime.QuickSell.manager.BoosterManager;
import me.mrCookieSlime.QuickSell.manager.ShopManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class QuickSell extends JavaPlugin {

    private static QuickSell instance;
    private static Logger logger;

    public static Economy economy = null;

    private LiteCommands<CommandSender> liteCommands;

    private LogManager logManager;

    private YamlDocument messages;
    private YamlDocument configuration;
    private MessageHandler messageHandler;

    private ShopEditor editor;
    private ShopMenu shopMenu;
    private ShopManager shopManager;
    private BoosterManager boosterManager;

    @Override
    public void onEnable() {
        instance = this;
        logger = getLogger();

        // Setup Messages, Configs and Economy
        setupConfiguration();
        setupMessages();
        setupEconomy();

        // Initiate Variables
        logManager = new LogManager(this);
        shopManager = new ShopManager(this);
        shopManager.loadShops();
        boosterManager = new BoosterManager(this);
        boosterManager.loadBoosters();

        shopMenu = new ShopMenu(this);
        editor = new ShopEditor(this);

        // Listeners
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new LoggerListener(this), this);
        pluginManager.registerEvents(new SignSellListener(this), this);
        pluginManager.registerEvents(new XPBoosterListener(this), this);

        // Commands
        this.liteCommands = LiteBukkitFactory.builder("QuickSell", this)
                .commands(
                        new SellCommand(this),
                        new PricesCommand(this),
                        new SellAllCommand(this),
                        new BoosterCommand(this),
                        new BoostersCommand(this),
                        new QuickSellCommand(this)
                )
                .argument(String.class, ArgumentKey.of("booster-type"), new BoosterTypeArgument())
                .argument(String.class, ArgumentKey.of("booster-player"), new BoosterPlayerArgument())
                .missingPermission(new CommandMissingPermissionHandler(messageHandler))
                .invalidUsage(new CommandInvalidUsage(messageHandler))
                .build();
    }

    @Override
    public void onDisable() {
        economy = null;
        editor = null;
        shopMenu = null;

        if (shopManager != null) {
            shopManager.saveShops();
            shopManager = null;
        }

        if (boosterManager != null) {
            boosterManager.saveBoosters();
            boosterManager = null;
        }

        if (liteCommands != null) {
            liteCommands.unregister();
        }
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
        if (economyProvider != null) {
            economy = (Economy) economyProvider.getProvider();
        }

        return economy != null;
    }

    private void setupConfiguration() {
        try {
            this.configuration = YamlDocument.create(new File(getDataFolder(), "config.yml"),
                    Objects.requireNonNull(getResource("config.yml")),
                    GeneralSettings.DEFAULT,
                    LoaderSettings.builder().setAutoUpdate(true).build(),
                    DumperSettings.DEFAULT,
                    UpdaterSettings.builder().setVersioning(new BasicVersioning("config-version")).build());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void setupMessages() {
        try {
            messages = YamlDocument.create(new File(getDataFolder(), "messages.yml"),
                    Objects.requireNonNull(getResource("messages.yml")),
                    LoaderSettings.builder().setAutoUpdate(true).build(),
                    DumperSettings.DEFAULT,
                    UpdaterSettings.builder().setVersioning(new BasicVersioning("config-version")).build()
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.messageHandler = new MessageHandler(this, messages);
    }

    /**
     * Logs something in the console
     *
     * @param level   Level
     * @param message Message
     */
    public static void log(Level level, String message) {
        if (level == Level.SEVERE)
            logger.warning("[QuickSell] " + message);

        if (level == Level.WARNING)
            logger.warning("[QuickSell] " + message);

        if (level == Level.INFO) {
            logger.info("[QuickSell] " + message);
        }

    }

    public static QuickSell getInstance() {
        return instance;
    }

    public static Economy getEconomy() {
        return economy;
    }

    public LogManager getLogManager() {
        return logManager;
    }

    public YamlDocument getMessages() {
        return messages;
    }

    public YamlDocument getConfiguration() {
        return configuration;
    }

    public ShopEditor getEditor() {
        return editor;
    }

    public ShopMenu getShopMenu() {
        return shopMenu;
    }

    public ShopManager getShopManager() {
        return shopManager;
    }

    public MessageHandler getMessageHandler() {
        return messageHandler;
    }

    public BoosterManager getBoosterManager() {
        return boosterManager;
    }
}
