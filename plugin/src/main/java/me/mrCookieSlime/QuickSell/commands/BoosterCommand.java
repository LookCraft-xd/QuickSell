package me.mrCookieSlime.QuickSell.commands;

import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.execute.ExecuteDefault;
import dev.rollczi.litecommands.annotations.permission.Permission;
import me.mrCookieSlime.QuickSell.boosters.Booster;
import me.mrCookieSlime.QuickSell.boosters.BoosterType;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

@Command(name = "booster")
@Permission("quicksell.booster")
public class BoosterCommand {

    @Execute()
    //@Syntax("<Booster Type> <Player> <Multi> <Duration In Mins>")
    public void onDefault(@Context CommandSender sender, @Arg String type, @Arg String player, @Arg Double multi, @Arg int duration) {
        BoosterType boosterType = type.equalsIgnoreCase("all") ? null : BoosterType.valueOf(type.toUpperCase());

        if (boosterType != null) {
            Booster booster = new Booster(boosterType, player, multi, duration);
            booster.activate();
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&eYou have activated a " + multi + "x " + boosterType + " booster for " + duration + " minutes!"));
            return;
        }

        for (BoosterType bt: BoosterType.values()) {
            Booster booster = new Booster(bt, player, multi, duration);
            booster.activate();
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&eYou have activated a " + multi + "x " + bt + " booster for " + duration + " minutes!"));
        }
    }

    @ExecuteDefault
    public void help(@Context CommandSender sender) {
        sendHelpMessage(sender);
    }


    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7\u21E8 /boosters <all/monetary/prisongems/exp/mcmmo/casino> <Player> <Multiplier> <Duration in Minutes>"));
    }

}
