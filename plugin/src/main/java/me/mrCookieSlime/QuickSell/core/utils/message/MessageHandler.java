package me.mrCookieSlime.QuickSell.core.utils.message;

import dev.dejvokep.boostedyaml.YamlDocument;
import me.clip.placeholderapi.PlaceholderAPI;
import me.mrCookieSlime.QuickSell.QuickSell;
import me.mrCookieSlime.QuickSell.core.utils.enums.Messages;
import me.mrCookieSlime.QuickSell.core.utils.message.helpers.MessageUtils;
import me.mrCookieSlime.QuickSell.core.utils.message.helpers.Placeholder;
import me.mrCookieSlime.QuickSell.core.utils.message.helpers.ThrottleManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class MessageHandler {

    private final YamlDocument lang;
    private final boolean usePAPI;

    private final ThrottleManager throttleManager;

    public MessageHandler(QuickSell plugin, YamlDocument messages) {
        this.lang = messages;

        Plugin papi = plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI");
        this.usePAPI = papi != null && papi.isEnabled();

        // Cooldown por defecto de 5 segundos (5000ms)
        this.throttleManager = new ThrottleManager(5000);
    }

    // # ~ # -------- BUILDER METHODS -------- # ~ # //
    public MessageBuilder build(String path) {
        return new MessageBuilder(this, null, path, false);
    }

    public MessageBuilder build(CommandSender sender, String path) {
        return new MessageBuilder(this, sender, path, false);
    }

    public MessageBuilder build(Messages message) {
        return new MessageBuilder(this, null, message.getPath(), false);
    }

    public MessageBuilder build(CommandSender sender, Messages message) {
        return new MessageBuilder(this, sender, message.getPath(), false);
    }

    public MessageBuilder buildManual(CommandSender sender, Object rawContent) {
        return new MessageBuilder(this, sender, rawContent, true);
    }

    public MessageBuilder buildManual(Object rawContent) {
        return new MessageBuilder(this, null, rawContent, true);
    }

    // # ~ # -------- MESSAGE PROCESSING -------- # ~ # //

    protected String process(CommandSender sender, String message, List<Placeholder> placeholders) {
        if (message == null || message.isEmpty()) return "";

        if (usePAPI && sender instanceof Player player) {
            message = PlaceholderAPI.setPlaceholders(player, message);
        }

        // Should?
        if (message.contains("%prefix%")) {
            String prefix = lang.getString("PREFIX", "");
            message = message.replace("%prefix%", prefix);
        }

        if (placeholders != null) {
            for (Placeholder p : placeholders) {
                message = p.apply(message);
            }
        }

        return MessageUtils.parseToLegacy(message);
    }

    // # ~ # -------- THROTTLING -------- # ~ # //

    public boolean canSendMessage(Player player, String key) {
        return throttleManager.canProceed(player.getUniqueId(), key);
    }

    public boolean canSendMessage(Player player, String key, long millis) {
        return throttleManager.canProceed(player.getUniqueId(), key, millis);
    }

    public void invalidatePlayer(UUID uuid) {
        throttleManager.clearPlayer(uuid);
    }

    // # ~ # -------- GET RAWS -------- # ~ # //

    public String getRawMessage(Messages messages) {
        return getRawMessage(messages.getPath());
    }

    public String getRawMessage(String path) {
        if (lang.isList(path)) {
            return String.join("\n", lang.getStringList(path));
        }
        return lang.getString(path, "&cMessage not found: " + path);
    }

    public List<String> getRawList(String path) {
        return lang.isList(path) ? lang.getStringList(path) : Collections.singletonList(getRawMessage(path));
    }

    // File getter

    public YamlDocument getLang() {
        return lang;
    }
}
