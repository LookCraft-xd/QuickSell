package me.mrCookieSlime.QuickSell.core.utils.message.helpers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MessageUtils {

    // Serializadores estables
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_SECTION = LegacyComponentSerializer.legacySection();
    private static final LegacyComponentSerializer LEGACY_AMPERSAND = LegacyComponentSerializer.legacyAmpersand();

    public static String strip(String text) {
        return ChatColor.stripColor(translateColor(text));
    }

    /**
     * Traduce códigos '&' a color real de Minecraft (§).
     */
    public static String translateColor(String text) {
        if (text == null || text.isEmpty()) return "";
        // Usar el serializador de Adventure es más seguro que ChatColor en versiones modernas
        return LEGACY_SECTION.serialize(LEGACY_AMPERSAND.deserialize(text));
    }

    /**
     * Convierte MiniMessage o Legacy a un Component sólido.
     */
    public static Component parseToComponent(String message) {
        if (message == null || message.isEmpty()) return Component.empty();

        // Si contiene tags de MiniMessage (como <gray>), lo deserializamos como tal
        if (message.contains("<") && message.contains(">")) {
            return MINI_MESSAGE.deserialize(message);
        }
        // Si no, lo tratamos como Legacy
        return LEGACY_AMPERSAND.deserialize(message);
    }


    @NotNull
    public static List<Component> parseToComponentList(List<String> messages) {
        if (messages == null || messages.isEmpty()) return new ArrayList<>();
        List<Component> parsed = new ArrayList<>();
        for (String m : messages) {
            parsed.add(parseToComponent(m));
        }
        return parsed;
    }

    /**
     * Esta es la clave para tu error: Convierte cualquier entrada a un String
     * con colores (§) que el ItemBuilder entiende sin invocar serializadores complejos.
     */
    @NotNull
    public static String parseToLegacy(String message) {
        if (message == null || message.isEmpty()) return "";

        // Convertimos a Component primero (para soportar MiniMessage y Legacy)
        Component component = parseToComponent(message);

        // Lo devolvemos como String de colores tradicional (§)
        return LEGACY_SECTION.serialize(component);
    }

    @NotNull
    public static List<String> parseToLegacyList(List<String> messages) {
        if (messages == null || messages.isEmpty()) return new ArrayList<>();
        List<String> parsed = new ArrayList<>();
        for (String m : messages) {
            parsed.add(parseToLegacy(m));
        }
        return parsed;
    }
}
