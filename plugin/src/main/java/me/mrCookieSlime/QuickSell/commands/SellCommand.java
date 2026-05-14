package me.mrCookieSlime.QuickSell.commands;

import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.optional.OptionalArg;
import me.mrCookieSlime.QuickSell.QuickSell;
import me.mrCookieSlime.QuickSell.core.utils.enums.Messages;
import me.mrCookieSlime.QuickSell.core.utils.message.MessageHandler;
import me.mrCookieSlime.QuickSell.inventories.ShopMenu;
import me.mrCookieSlime.QuickSell.manager.ShopManager;
import org.bukkit.entity.Player;

@Command(name = "sell")
public class SellCommand {

    private final QuickSell plugin;
    private final ShopMenu shopMenu;
    private final ShopManager shopManager;
    private final MessageHandler messageHandler;

    public SellCommand(QuickSell plugin) {
        this.plugin = plugin;
        this.shopMenu = plugin.getShopMenu();
        this.shopManager = plugin.getShopManager();
        this.messageHandler = plugin.getMessageHandler();
    }

    @Execute
    public void onDefault(@Context Player player, @OptionalArg String shopName) {
        if (shopName != null && !shopName.isEmpty()) {
            shopManager.getShop(shopName).ifPresentOrElse(shop -> {
                if (!shop.hasUnlocked(player)) {
                    messageHandler.build(player, Messages.NO_ACCESS).send();
                    return;
                }

                shopMenu.open(player, shop);
            }, () -> messageHandler.build(player, Messages.UNKNOWN_SHOP).send());
            return;
        }

        shopManager.getHighestShop(player).ifPresentOrElse(shop -> {
                    if (!shop.hasUnlocked(player)) {
                        messageHandler.build(player, Messages.NO_ACCESS).send();
                        return;
                    }
                    shopMenu.open(player, shop);
                }, () -> shopMenu.openMenu(player)
        );
    }
}
