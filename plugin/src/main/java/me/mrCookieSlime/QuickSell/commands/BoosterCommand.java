package me.mrCookieSlime.QuickSell.commands;

import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.flag.Flag;
import dev.rollczi.litecommands.annotations.permission.Permission;
import me.mrCookieSlime.QuickSell.QuickSell;
import me.mrCookieSlime.QuickSell.boosters.Booster;
import me.mrCookieSlime.QuickSell.boosters.BoosterType;
import me.mrCookieSlime.QuickSell.boosters.PrivateBooster;
import me.mrCookieSlime.QuickSell.manager.BoosterManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Command(name = "booster")
@Permission("quicksell.booster")
public class BoosterCommand {

    private final BoosterManager boosterManager;

    public BoosterCommand(QuickSell quickSell) {
        boosterManager = quickSell.getBoosterManager();
    }

    @Execute
    public void onDefault(@Context CommandSender sender, @Arg("booster-type") String type, @Arg("booster-player") String player, @Arg Double multi, @Arg String duration, @Flag("-s") boolean silent, @Flag("-e") boolean extend) {
        List<BoosterType> types = (type == null) ? Arrays.asList(BoosterType.values()) : Collections.singletonList(BoosterType.valueOf(type));

        for (BoosterType bt : types) {
            activateBooster(bt, player, multi, duration, silent, extend);
        }

        String targetName = (player == null) ? "GLOBAL" : player;
        String typeName = (type == null) ? "GLOBAL" : type;
        String msg = (player == null)
                ? "&eActivated a " + multi + "x " + typeName + " booster!"
                : "&eGiven " + targetName + " a " + multi + "x " + typeName + " booster!";

        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
    }

    private void activateBooster(BoosterType type, String player, double multi, String duration, boolean silent, boolean extend) {
        Booster booster = (player != null)
                ? new PrivateBooster(type, duration, multi, player)
                : new Booster(type, duration, multi);

        booster.setExtend(extend);
        booster.setSilent(silent);

        boosterManager.activateBooster(booster);
    }

}
