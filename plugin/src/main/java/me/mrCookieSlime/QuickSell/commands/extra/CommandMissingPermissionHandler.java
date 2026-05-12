package me.mrCookieSlime.QuickSell.commands.extra;

import dev.rollczi.litecommands.handler.result.ResultHandlerChain;
import dev.rollczi.litecommands.invocation.Invocation;
import dev.rollczi.litecommands.permission.MissingPermissions;
import dev.rollczi.litecommands.permission.MissingPermissionsHandler;
import me.mrCookieSlime.QuickSell.QuickSell;
import org.bukkit.command.CommandSender;

public class CommandMissingPermissionHandler implements MissingPermissionsHandler<CommandSender> {

    @Override
    public void handle(Invocation<CommandSender> invocation, MissingPermissions missingPermissions, ResultHandlerChain<CommandSender> chain) {
        String permission = missingPermissions.asJoinedText();
        CommandSender sender = invocation.sender();

        QuickSell.local.sendMessage(sender, "messages.no-permission", false);
    }

}
