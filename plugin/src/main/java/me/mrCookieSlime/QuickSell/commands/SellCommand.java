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
        // Caso A: El usuario busca una tienda específica (/sell [nombre])
        if (shopName != null && !shopName.isEmpty()) {
            shopManager.getShop(shopName).ifPresentOrElse(shop -> {
                if (shop.hasUnlocked(player)) {
                    shopMenu.open(player, shop);
                } else {
                    messageHandler.build(player, Messages.NO_ACCESS).send();
                }
            }, () -> messageHandler.build(player, Messages.UNKNOWN_SHOP).send());
            return;
        }

        // Caso B: El usuario solo puso /sell
        // Intentamos obtener la tienda de mayor prioridad a la que tenga acceso
        shopManager.getHighestShop(player).ifPresentOrElse(
                shop -> shopMenu.open(player, shop),
                () -> {
                    // Si no tiene acceso a ninguna tienda específica o no hay tiendas,
                    // intentamos abrir el selector general de tiendas.
                    if (shopManager.getAllShops().isEmpty()) {
                        messageHandler.build(player, Messages.UNKNOWN_SHOP).send();
                    } else {
                        shopMenu.openMenu(player);
                    }
                }
        );
    }
}
