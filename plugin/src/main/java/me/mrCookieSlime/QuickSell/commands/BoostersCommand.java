package me.mrCookieSlime.QuickSell.commands;

import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import me.mrCookieSlime.QuickSell.QuickSell;
import me.mrCookieSlime.QuickSell.boosters.Booster;
import me.mrCookieSlime.QuickSell.core.utils.message.MessageBuilder;
import me.mrCookieSlime.QuickSell.core.utils.message.MessageHandler;
import me.mrCookieSlime.QuickSell.manager.BoosterManager;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.StringJoiner;

@Command(name = "boosters")
public class BoostersCommand {

    private final BoosterManager boosterManager;
    private final MessageHandler messageHandler;

    public BoostersCommand(QuickSell plugin) {
        this.boosterManager = plugin.getBoosterManager();
        this.messageHandler = plugin.getMessageHandler();
    }

    @Execute
    public void onDefault(@Context Player player) {
        if (boosterManager.getActiveBoosters().isEmpty()) {
            player.sendMessage("No available Boosters");
            return;
        }

        List<Booster> boostersForPlayer = boosterManager.getBoostersForPlayer(player.getName());
        List<String> message = List.of("&7Actualmente tienes&f: &e%amount% &7boosters.",
                "&7Boosters disponibles:&f",
                "%boosters%"
        );

        MessageBuilder builder = messageHandler.buildManual(player, message)
                .placeholder("%amount%", boostersForPlayer.size());

        StringJoiner joiner = new StringJoiner("\n");
        boostersForPlayer.forEach(booster -> {
            String typeTag = booster.isPrivate() ? "&6&l[Privado]" : "&9&l[Global]";
            String timeStr = boosterManager.formattedRemainingTime(booster);
            joiner.add(typeTag + " &3" + booster.getMultiplier() + "x &b" + boosterManager.getUniqueName(booster) + " &e(" + timeStr + ")");
        });

        builder.placeholder("%boosters%", joiner.toString());
        builder.send();
    }
}