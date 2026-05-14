package me.mrCookieSlime.QuickSell.listeners;

import dev.dejvokep.boostedyaml.YamlDocument;
import me.mrCookieSlime.QuickSell.QuickSell;
import me.mrCookieSlime.QuickSell.listeners.custom.SellEvent;
import me.mrCookieSlime.QuickSell.logs.LogManager;
import me.mrCookieSlime.QuickSell.utils.SellType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class LoggerListener implements Listener {

    private final QuickSell plugin;
    private final LogManager logManager;
    private final YamlDocument configuration;

    public LoggerListener(QuickSell plugin) {
        this.plugin = plugin;
        this.logManager = plugin.getLogManager();
        this.configuration = plugin.getConfiguration();
    }

    @EventHandler
    public void onPlayerSell(SellEvent event) {
        Player player = event.getPlayer();
        SellType sellType = event.getSellType();
        int itemsSold = event.getItemsSold();
        double money = event.getSoldAmount();

        if (configuration.getBoolean("shop.enable-logging")) {
            logManager.log(player, sellType, itemsSold, money);
        }

    }
}
