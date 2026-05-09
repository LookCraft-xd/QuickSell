package me.mrCookieSlime.QuickSell.commands;

import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import me.mrCookieSlime.QuickSell.QuickSell;
import me.mrCookieSlime.QuickSell.shop.Shop;
import me.mrCookieSlime.QuickSell.interfaces.SellEvent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@Command(name = "sellall")
public class SellAllCommand {

    @Execute
    public void onDefault(@Context Player player) {
        if (!QuickSell.cfg.getBoolean("options.enable-commands")) {
            QuickSell.local.sendMessage(player, "commands.disabled", false);
            return;
        }

        Shop highestShop = Shop.getHighestShop(player);

        if (highestShop == null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "No shops found"));
            return;
        }

        if (!highestShop.hasUnlocked(player)) {
            QuickSell.local.sendMessage(player, "messages.no-access", false);
            return;
        }

        highestShop.sellall(player, "", SellEvent.Type.SELLALL);
    }

}
