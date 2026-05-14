package me.mrCookieSlime.QuickSell.commands.extra;

import dev.rollczi.litecommands.handler.result.ResultHandlerChain;
import dev.rollczi.litecommands.invalidusage.InvalidUsage;
import dev.rollczi.litecommands.invalidusage.InvalidUsageHandler;
import dev.rollczi.litecommands.invocation.Invocation;
import dev.rollczi.litecommands.schematic.Schematic;
import me.mrCookieSlime.QuickSell.core.utils.message.MessageHandler;
import org.bukkit.command.CommandSender;

public class CommandInvalidUsage implements InvalidUsageHandler<CommandSender> {

    private final MessageHandler messageHandler;

    public CommandInvalidUsage(MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    @Override
    public void handle(Invocation<CommandSender> invocation, InvalidUsage<CommandSender> result, ResultHandlerChain<CommandSender> chain) {
        CommandSender sender = invocation.sender();
        Schematic schematic = result.getSchematic();

        String usagePath = "commands.usage";
        if (schematic.isOnlyFirst()) {
            messageHandler.build(sender, usagePath).placeholder("%usage%", schematic.first()).send();
            return;
        }

        for (String scheme : schematic.all()) {
            messageHandler.build(sender, usagePath).placeholder("%usage%", scheme).send();
        }
    }
}
