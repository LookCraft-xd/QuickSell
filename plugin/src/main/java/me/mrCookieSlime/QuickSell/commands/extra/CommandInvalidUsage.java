package me.mrCookieSlime.QuickSell.commands.extra;

import dev.rollczi.litecommands.handler.result.ResultHandlerChain;
import dev.rollczi.litecommands.invalidusage.InvalidUsage;
import dev.rollczi.litecommands.invalidusage.InvalidUsageHandler;
import dev.rollczi.litecommands.invocation.Invocation;
import dev.rollczi.litecommands.schematic.Schematic;
import me.mrCookieSlime.QuickSell.QuickSell;
import me.mrCookieSlime.QuickSell.utils.Variable;
import org.bukkit.command.CommandSender;

public class CommandInvalidUsage implements InvalidUsageHandler<CommandSender> {

    @Override
    public void handle(Invocation<CommandSender> invocation, InvalidUsage<CommandSender> result, ResultHandlerChain<CommandSender> chain) {
        CommandSender sender = invocation.sender();
        Schematic schematic = result.getSchematic();

        String usagePath = "commands.usage";
        if (schematic.isOnlyFirst()) {
            QuickSell.local.sendMessage(sender, usagePath, false, new Variable("%usage%", schematic.first()));
            return;
        }

        for (String scheme : schematic.all()) {
            QuickSell.local.sendMessage(sender, usagePath, false, new Variable("%usage%", scheme));
        }
    }
}
