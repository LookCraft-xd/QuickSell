package me.mrCookieSlime.QuickSell.commands;

import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import io.github.thebusybiscuit.cscorelib2.item.CustomItem;
import me.mrCookieSlime.QuickSell.boosters.Booster;
import me.mrCookieSlime.QuickSell.boosters.BoosterType;
import me.mrCookieSlime.QuickSell.boosters.menu.BoosterMenu;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

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
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',  (booster.isPrivate() ? "&4&lPrivado": "&2&lGlobal") + "&3" + booster.getMultiplier() + "x &b" + booster.getUniqueName() + "(" + (booster.isInfinite() ? "Infinite": booster.formatTime() + "m") + ")"));
        });
    }

}
