package me.mrCookieSlime.QuickSell.logs;

import dev.dejvokep.boostedyaml.YamlDocument;
import me.mrCookieSlime.QuickSell.QuickSell;
import me.mrCookieSlime.QuickSell.utils.SellType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogManager {

    private final File logFolder;
    private final QuickSell plugin;
    private final YamlDocument config;

    public LogManager(QuickSell plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        this.logFolder = new File(plugin.getDataFolder(), "logs");
        if (!logFolder.exists()) logFolder.mkdirs();
    }

    public void log(Player p, SellType type, int items, double money) {
        if (!config.getBoolean("shop.enable-logging")) return;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String date = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
            String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
            File logFile = new File(logFolder, date + ".log");

            String entry = String.format("[%s] PLAYER: %s | TYPE: %s | ITEMS: %d | TOTAL: $%.2f",
                    time, p.getName(), type.toString(), items, money);

            try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(logFile, true)))) {
                out.println(entry);
            } catch (IOException e) {
                plugin.getLogger().severe("Could not write to sell log file! " + e.getMessage());
            }
        });
    }
}
