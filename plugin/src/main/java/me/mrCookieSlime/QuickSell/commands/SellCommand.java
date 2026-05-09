package me.mrCookieSlime.QuickSell.commands;

import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.optional.OptionalArg;
import me.mrCookieSlime.QuickSell.QuickSell;
import me.mrCookieSlime.QuickSell.shop.Shop;
import me.mrCookieSlime.QuickSell.shop.ShopMenu;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@Command(name = "sell")
public class SellCommand {

    @Execute
    public void onDefault(@Context CommandSender sender, @OptionalArg("[Shop Name]") String shopName) {
        if (!QuickSell.cfg.getBoolean("options.enable-commands")) {
            QuickSell.local.sendMessage(sender, "commands.disabled", false);
            return;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "This Command is only for Players"));
            return;
        }

        if (Shop.list().size() == 1) {
            ShopMenu.open((Player) sender, Shop.list().get(0));
            return;
        }

        if (shopName == null) {
            QuickSell.local.sendMessage(sender, "messages.no-access", false);
            return;
        }

        Shop shop = Shop.getShop(shopName);
        if (shop != null) {
            if (!shop.hasUnlocked((Player) sender)) {
                QuickSell.local.sendMessage(sender, "messages.no-access", false);
                return;
            }

            ShopMenu.open((Player) sender, shop);
            return;
        } else {
            QuickSell.local.sendMessage(sender, "messages.unknown-shop", false);
        }

        if (QuickSell.cfg.getBoolean("options.open-only-shop-with-permission")) {
            if (Shop.getHighestShop((Player) sender) == null) {
                QuickSell.local.sendMessage(sender, "messages.no-access", false);
                return;
            }
            ShopMenu.open((Player) sender, Shop.getHighestShop((Player) sender));
        }
    }

}
