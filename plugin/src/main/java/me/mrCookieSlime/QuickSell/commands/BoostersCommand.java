package me.mrCookieSlime.QuickSell.commands;

import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import me.mrCookieSlime.QuickSell.boosters.Booster;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@Command(name = "boosters")
public class BoostersCommand {

    @Execute
    public void onDefault(@Context Player sender) {
        if (Booster.getBoosters(sender.getName()).isEmpty()) {
            sender.sendMessage("No available Boosters");
            return;
        }

        sender.sendMessage("Available Boosters:");
        Booster.getBoosters(sender.getName()).forEach(booster -> {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', (booster.isPrivate() ? "&4&lPrivado" : "&2&lGlobal") + " &3" + booster.getMultiplier() + "x &b" + booster.getUniqueName() + " &e(" + (booster.isInfinite() ? "Infinite" : booster.formatTime() + "m") + ")"));
        });
    }

}
