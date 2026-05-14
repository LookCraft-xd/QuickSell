package me.mrCookieSlime.QuickSell.commands;

import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.optional.OptionalArg;
import me.mrCookieSlime.QuickSell.QuickSell;
import me.mrCookieSlime.QuickSell.core.utils.enums.Messages;
import me.mrCookieSlime.QuickSell.core.utils.message.MessageHandler;
import me.mrCookieSlime.QuickSell.manager.ShopManager;
import me.mrCookieSlime.QuickSell.shop.Shop;
import org.bukkit.entity.Player;

import java.util.Optional;

@Command(name = "sellall")
public class SellAllCommand {

    private final ShopManager shopManager;
    private final MessageHandler messageHandler;

    public SellAllCommand(QuickSell plugin) {
        this.shopManager = plugin.getShopManager();
        this.messageHandler = plugin.getMessageHandler();
    }

    @Execute
    public void onDefault(@Context Player player, @OptionalArg("shop") String shopName) {
        if (shopName != null && !shopName.isEmpty()) {
            Optional<Shop> optionalShop = shopManager.getShop(shopName);
            optionalShop.ifPresentOrElse(shop -> {
                if (!shop.hasUnlocked(player)) {
                    messageHandler.build(player, Messages.NO_ACCESS).send();
                    return;
                }

                shopManager.sellAll(player, shop);
            }, () -> messageHandler.build(Messages.UNKNOWN_SHOP).send());
            return;
        }

        shopManager.getHighestShop(player).ifPresentOrElse(shop -> shopManager.sellAll(player),
                () -> messageHandler.buildManual("<red>No hay tiendas disponibles que tengas acceso.").send());
    }

}
