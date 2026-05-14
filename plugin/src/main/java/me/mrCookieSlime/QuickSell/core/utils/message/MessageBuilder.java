package me.mrCookieSlime.QuickSell.core.utils.message;

import me.mrCookieSlime.QuickSell.core.utils.message.helpers.MessageUtils;
import me.mrCookieSlime.QuickSell.core.utils.message.helpers.Placeholder;
import me.mrCookieSlime.QuickSell.core.utils.message.utils.CenteredString;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MessageBuilder {

    private final MessageHandler handler;
    private final Object target;
    private final boolean manual;

    private boolean centered = false;
    private final Set<CommandSender> recipients = new HashSet<>();
    private final List<Placeholder> placeholders = new ArrayList<>();

    public MessageBuilder(MessageHandler handler, Object target, boolean manual) {
        this(handler, null, target, manual);
    }

    public MessageBuilder(MessageHandler handler, CommandSender recipient, Object target, boolean manual) {
        this.manual = manual;
        this.target = target;
        this.handler = handler;

        if (recipient != null) {
            this.recipients.add(recipient);
        }
    }

    public MessageBuilder placeholder(String key, Object value) {
        this.placeholders.add(new Placeholder(key, String.valueOf(value)));
        return this;
    }

    public MessageBuilder to(CommandSender sender) {
        this.recipients.add(sender);
        return this;
    }

    public MessageBuilder to(Set<Player> players) {
        this.recipients.addAll(players);
        return this;
    }

    public MessageBuilder centered() {
        this.centered = true;
        return this;
    }

    public void send() {
        if (recipients.isEmpty()) {
            Bukkit.getLogger().warning("Message " + target + " has no recipients");
            return;
        }

        if (isActuallyAList()) {
            asList().forEach(this::dispatch);
            return;
        }

        String msg = asString();
        if (!msg.isEmpty()) dispatch(msg);
    }

    private void dispatch(String message) {
        String finalMsg = centered ? CenteredString.formatMessage(message) : message;
        recipients.forEach(r -> r.sendMessage(finalMsg));
    }

    public void broadcast() {
        if (isActuallyAList()) {
            asList().forEach(this::dispatchBroadcast);
            return;
        }

        String msg = asString();
        if (!msg.isEmpty()) dispatchBroadcast(msg);
    }

    private void dispatchBroadcast(String message) {
        String finalMsg = centered ? CenteredString.formatMessage(message) : message;
        Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(finalMsg));
        Bukkit.getConsoleSender().sendMessage(finalMsg);
    }

    private boolean isActuallyAList() {
        if (manual) return target instanceof List;
        return handler.getLang().isList(target.toString());
    }

    public String asString() {
        CommandSender context = recipients.stream().findFirst().orElse(null);

        if (!manual) {
            return handler.process(context, handler.getRawMessage(target.toString()), placeholders);
        }

        if (target instanceof List<?> list) {
            String combined = String.join("\n", list.stream().map(Object::toString).toList());
            return handler.process(context, combined, placeholders);
        }

        return handler.process(context, target.toString(), placeholders);
    }

    public List<String> asList() {
        CommandSender context = recipients.stream().findFirst().orElse(null);

        return getRawLines().stream()
                .map(line -> handler.process(context, line, placeholders))
                .toList();
    }

    public List<Component> asComponentList() {
        return MessageUtils.parseToComponentList(asList());
    }

    private List<String> getRawLines() {
        if (!manual) {
            return handler.getRawList(target.toString());
        }

        if (target instanceof List<?> list) {
            return list.stream().map(Object::toString).toList();
        }

        return List.of(target.toString());
    }

    public Component asComponent() {
        return MessageUtils.parseToComponent(asString());
    }
}