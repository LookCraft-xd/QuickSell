package me.mrCookieSlime.QuickSell.listeners;

import dev.dejvokep.boostedyaml.YamlDocument;
import me.mrCookieSlime.QuickSell.QuickSell;
import me.mrCookieSlime.QuickSell.core.utils.enums.Messages;
import me.mrCookieSlime.QuickSell.core.utils.message.MessageHandler;
import me.mrCookieSlime.QuickSell.inventories.ShopMenu;
import me.mrCookieSlime.QuickSell.manager.ShopManager;
import me.mrCookieSlime.QuickSell.utils.SellType;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class SignSellListener implements Listener {

    private final ShopMenu shopMenu;
    private final YamlDocument config;
    private final ShopManager shopManager;
    private final MessageHandler messageHandler;

    public SignSellListener(QuickSell plugin) {
        this.shopMenu = plugin.getShopMenu();
        this.config = plugin.getConfiguration();
        this.shopManager = plugin.getShopManager();
        this.messageHandler = plugin.getMessageHandler();
    }

    @EventHandler
    public void onSignCreate(SignChangeEvent e) {
        // Obtenemos los prefijos desde la config
        String sellPrefix = color(config.getString("options.sign-prefix", "&1[Sell]"));
        String sellAllPrefix = color(config.getString("options.sellall-sign-prefix", "&1[Sell All]"));

        String firstLine = e.getLine(0);
        if (firstLine == null) return;

        // Validamos si es un cartel de QuickSell (limpiando colores para comparar)
        boolean isSell = firstLine.equalsIgnoreCase(ChatColor.stripColor(sellPrefix));
        boolean isSellAll = firstLine.equalsIgnoreCase(ChatColor.stripColor(sellAllPrefix));

        if (isSell || isSellAll) {
            if (!e.getPlayer().hasPermission("quicksell.sign.create")) {
                e.setCancelled(true);
                messageHandler.build(e.getPlayer(), Messages.NO_PERMISSION).send();
                return;
            }
            e.setLine(0, isSell ? sellPrefix : sellAllPrefix);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getClickedBlock() == null || !(e.getClickedBlock().getState() instanceof Sign)) return;

        Sign sign = (Sign) e.getClickedBlock().getState();
        Player p = e.getPlayer();
        String line0 = sign.getLine(0);

        String sellPrefix = color(config.getString("options.sign-prefix", "&1[Sell]"));
        String sellAllPrefix = color(config.getString("options.sellall-sign-prefix", "&1[Sell All]"));

        // --- CLIC DERECHO: Abrir Menús o Vender Todo ---
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {

            // Caso: [Sell] -> Abrir menú de venta
            if (line0.equalsIgnoreCase(sellPrefix)) {
                String targetShop = sign.getLine(1);

                shopManager.getShop(targetShop).ifPresentOrElse(
                        shop -> shopMenu.open(p, shop),
                        () -> shopMenu.openMenu(p) // Fallback al selector si la tienda no existe
                );
                return;
            }

            // Caso: [Sell All] -> Vender inventario instantáneo
            if (line0.equalsIgnoreCase(sellAllPrefix)) {
                String targetShop = sign.getLine(1);
                String itemFilter = sign.getLine(2).toUpperCase().replace(" ", "_");

                shopManager.getShop(targetShop).ifPresentOrElse(shop -> {
                    if (shop.hasUnlocked(p)) {
                        shopManager.sellItemInShop(p, shop, itemFilter);
                    } else {
                        messageHandler.build(p, Messages.NO_ACCESS).send();
                    }
                }, () -> {
                    // Si no hay tienda en la línea 2, usar la mejor disponible
                    shopManager.getHighestShop(p).ifPresentOrElse(
                            highest -> shopManager.sellItemInShop(p, highest, itemFilter),
                            () -> messageHandler.build(p, Messages.UNKNOWN_SHOP).send()
                    );
                });
            }
        }

        // --- CLIC IZQUIERDO: Ver Precios ---
        if (e.getAction() == Action.LEFT_CLICK_BLOCK && p.getGameMode() != GameMode.CREATIVE) {
            if (line0.equalsIgnoreCase(sellPrefix) || line0.equalsIgnoreCase(sellAllPrefix)) {
                String targetShop = sign.getLine(1);

                shopManager.getShop(targetShop).ifPresentOrElse(
                        shop -> {
                            if (shop.hasUnlocked(p)) shopMenu.openPrices(p, shop);
                            else messageHandler.build(p, Messages.NO_ACCESS).send();
                        },
                        () -> shopManager.getHighestShop(p).ifPresent(highest -> shopMenu.openPrices(p, highest))
                );
            }
        }
    }

    private String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
