package me.mrCookieSlime.QuickSell.commands;

import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.permission.Permission;
import me.mrCookieSlime.QuickSell.QuickSell;
import me.mrCookieSlime.QuickSell.shop.Shop;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@Command(name = "price", aliases = "prices")
@Permission("quicksell.prices")
public class PricesCommand {

    @Execute
    public void onDefault(@Context CommandSender sender, @Arg("<Shop Name>") String shopName) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "This Command is only for Players"));
            return;
        }

        Shop shop = Shop.getShop(shopName);
        if (shop != null) {
            if (!shop.hasUnlocked((Player) sender)) {
                QuickSell.local.sendMessage(sender, "messages.no-access", false);
                return;
            }

            shop.showPrices((Player) sender);
            return;
        } else {
            QuickSell.local.sendMessage(sender, "messages.unknown-shop", false);
        }

        if (QuickSell.cfg.getBoolean("options.open-only-shop-with-permission")) {
            if (Shop.getHighestShop((Player) sender) == null) {
                QuickSell.local.sendMessage(sender, "messages.no-access", false);
                return;
            }
            Shop.getHighestShop((Player) sender).showPrices((Player) sender);
            return;
        }
        QuickSell.local.sendMessage(sender, "commands.prices.usage", false);
    }

}
