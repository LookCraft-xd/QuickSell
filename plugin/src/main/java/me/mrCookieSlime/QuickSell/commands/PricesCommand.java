package me.mrCookieSlime.QuickSell.commands;

import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.optional.OptionalArg;
import dev.rollczi.litecommands.annotations.permission.Permission;
import me.mrCookieSlime.QuickSell.QuickSell;
import me.mrCookieSlime.QuickSell.core.utils.enums.Messages;
import me.mrCookieSlime.QuickSell.core.utils.message.MessageHandler;
import me.mrCookieSlime.QuickSell.inventories.ShopMenu;
import me.mrCookieSlime.QuickSell.manager.ShopManager;
import org.bukkit.entity.Player;


@Command(name = "price", aliases = "prices")
@Permission("quicksell.prices")
public class PricesCommand {

    private final ShopMenu shopMenu;
    private final ShopManager shopManager;
    private final MessageHandler messageHandler;

    public PricesCommand(QuickSell plugin) {
        this.shopMenu = plugin.getShopMenu();
        this.shopManager = plugin.getShopManager();
        this.messageHandler = plugin.getMessageHandler();
    }

    @Execute
    public void onDefault(@Context Player player, @OptionalArg String shopName) {

        // 1. Caso: El jugador especificó una tienda: /price [nombre]
        if (shopName != null && !shopName.isEmpty()) {
            shopManager.getShop(shopName).ifPresentOrElse(shop -> {
                if (shop.hasUnlocked(player)) {
                    shopMenu.openPrices(player, shop);
                } else {
                    messageHandler.build(player, Messages.NO_ACCESS).send();
                }
            }, () -> messageHandler.build(player, Messages.UNKNOWN_SHOP).send());
            return;
        }

        // 2. Caso: Solo puso /price. Buscamos su mejor tienda disponible.
        shopManager.getHighestShop(player).ifPresentOrElse(
                highestShop -> shopMenu.openPrices(player, highestShop),
                () -> messageHandler.build(player, Messages.NO_ACCESS).send()
        );
    }
}
