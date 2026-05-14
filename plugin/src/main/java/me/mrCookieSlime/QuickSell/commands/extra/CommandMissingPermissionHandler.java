package me.mrCookieSlime.QuickSell.commands.extra;

import dev.rollczi.litecommands.handler.result.ResultHandlerChain;
import dev.rollczi.litecommands.invocation.Invocation;
import dev.rollczi.litecommands.permission.MissingPermissions;
import dev.rollczi.litecommands.permission.MissingPermissionsHandler;
import me.mrCookieSlime.QuickSell.core.utils.enums.Messages;
import me.mrCookieSlime.QuickSell.core.utils.message.MessageHandler;
import org.bukkit.command.CommandSender;

public class CommandMissingPermissionHandler implements MissingPermissionsHandler<CommandSender> {

    private MessageHandler messageHandler;

    public CommandMissingPermissionHandler(MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    @Override
    public void handle(Invocation<CommandSender> invocation, MissingPermissions missingPermissions, ResultHandlerChain<CommandSender> chain) {
        String permission = missingPermissions.asJoinedText();
        CommandSender sender = invocation.sender();

        messageHandler.build(sender, Messages.NO_PERMISSION).send();
    }

}
