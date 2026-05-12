package me.mrCookieSlime.QuickSell.commands;

import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.execute.ExecuteDefault;
import dev.rollczi.litecommands.annotations.flag.Flag;
import dev.rollczi.litecommands.annotations.permission.Permission;
import me.mrCookieSlime.QuickSell.boosters.Booster;
import me.mrCookieSlime.QuickSell.boosters.BoosterType;
import me.mrCookieSlime.QuickSell.boosters.PrivateBooster;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

@Command(name = "booster")
@Permission("quicksell.booster")
public class BoosterCommand {

    @Execute()
    public void onDefault(@Context CommandSender sender, @Arg("booster-type") String type, @Arg("booster-player") String player, @Arg Double multi, @Arg int duration, @Flag("-s") boolean silent, @Flag("-e") boolean extend) {
        if (type == null) {
            handleGlobal(sender, player, multi, duration, silent, extend);
            return;
        }

        BoosterType boosterType = BoosterType.valueOf(type.toUpperCase());

        Booster booster =
                player != null ?
                        new PrivateBooster(boosterType, player, multi, duration) :
                        new Booster(boosterType, multi, duration);
        booster.setExtend(extend);
        booster.setSilent(silent);
        booster.activate();

        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&eYou have activated a " + multi + "x " + type + " booster for " + duration + " minutes!"));
        // Private booster message.
        //sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&eYou have given " + player + " a " + multi + "x " + boosterType + " booster for " + duration + " minutes!"));
    }

    private static void handleGlobal(CommandSender sender, String player, Double multi, int duration, boolean silent, boolean extend) {
        for (BoosterType bt : BoosterType.values()) {

            boolean target = player != null;
            Booster booster = target ? new PrivateBooster(bt, player, multi, duration) : new Booster(bt, multi, duration);
            booster.setExtend(extend);
            booster.setSilent(silent);
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
