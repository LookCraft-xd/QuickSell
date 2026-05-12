package me.mrCookieSlime.QuickSell.listeners;

import me.mrCookieSlime.QuickSell.QuickSell;
import me.mrCookieSlime.QuickSell.interfaces.SellEvent;
import me.mrCookieSlime.QuickSell.shop.Shop;
import me.mrCookieSlime.QuickSell.shop.ShopMenu;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class SignSellListener implements Listener {

    @EventHandler
    public void onSignCreate(SignChangeEvent e) {
        // SELL SIGN
        String prefix = ChatColor.translateAlternateColorCodes('&', QuickSell.cfg.getString("options.sign-prefix"));
        if (e.getLines()[0].equalsIgnoreCase(ChatColor.stripColor(prefix))) {

            if (e.getPlayer().hasPermission("QuickSell.sign.create")) {
                e.setLine(0, prefix);
                return;
            }

            e.setCancelled(true);
            QuickSell.local.sendMessage(e.getPlayer(), "messages.no-permission", false);
        }

        // SELLALL SIGN
        prefix = ChatColor.translateAlternateColorCodes('&', QuickSell.cfg.getString("options.sellall-sign-prefix"));
        if (e.getLines()[0].equalsIgnoreCase(ChatColor.stripColor(prefix))) {
            if (e.getPlayer().hasPermission("QuickSell.sign.create")) {
                e.setLine(0, prefix);
                return;
            }

            e.setCancelled(true);
            QuickSell.local.sendMessage(e.getPlayer(), "messages.no-permission", false);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (e.getClickedBlock().getState() instanceof Sign) {

                // IF SIGN IS [SELL] OPEN SELL MENU
                Sign sign = (Sign) e.getClickedBlock().getState();
                if (sign.getLine(0).equalsIgnoreCase(ChatColor.translateAlternateColorCodes('&', QuickSell.cfg.getString("options.sign-prefix")))) {
                    Shop shop = Shop.getShop(sign.getLine(1));
                    if (shop != null) {
                        ShopMenu.open(e.getPlayer(), shop);
                        return;
                    }

                    ShopMenu.openMenu(e.getPlayer());
                    e.setCancelled(true);
                    return;
                }

                // IF SIGN IS [SELLALL] THEN SELL ALL INVENTORY
                if (sign.getLine(0).equalsIgnoreCase(ChatColor.translateAlternateColorCodes('&', QuickSell.cfg.getString("options.sellall-sign-prefix")))) {
                    Shop shop = Shop.getShop(sign.getLine(1));
                    if (shop != null) {
                        if (shop.hasUnlocked(e.getPlayer())) {
                            String item = sign.getLine(2);
                            item = item.toUpperCase();

                            if (item.contains(" ")) {
                                item = item.replace(" ", "_");
                            }

                            shop.sellall(e.getPlayer(), item, SellEvent.Type.SELLALL);
                            return;
                        }
                        QuickSell.local.sendMessage(e.getPlayer(), "messages.no-access", false);
                        return;
                    }

                    if (Shop.getHighestShop(e.getPlayer()) != null) {
                        String item = sign.getLine(2);
                        item = item.toUpperCase();

                        if (item.contains(" ")) {
                            item = item.replace(" ", "_");
                        }

                        Shop.getHighestShop(e.getPlayer()).sellall(e.getPlayer(), item, SellEvent.Type.SELLALL);
                    } else {
                        QuickSell.local.sendMessage(e.getPlayer(), "messages.unknown-shop", false);
                        return;
                    }

                    e.setCancelled(true);
                }
            }
            return;
        }

        // SHOW PRICES
        if (e.getAction() == Action.LEFT_CLICK_BLOCK && e.getPlayer().getGameMode() != GameMode.CREATIVE) {
            if (e.getClickedBlock().getState() instanceof Sign) {
                Sign sign = (Sign) e.getClickedBlock().getState();

                // IF SIGN IS A [SELL] SIGN
                if (sign.getLine(0).equalsIgnoreCase(ChatColor.translateAlternateColorCodes('&', QuickSell.cfg.getString("options.sign-prefix")))) {
                    Shop shop = Shop.getShop(sign.getLine(1));
                    if (shop != null) {
                        shop.showPrices(e.getPlayer());
                        return;
                    }

                    if (Shop.getHighestShop(e.getPlayer()) != null) {
                        Shop.getHighestShop(e.getPlayer()).showPrices(e.getPlayer());
                        return;
                    }
                    return;
                }

                // IF SIGN IS A [SELLALL] SIGN
                if (sign.getLine(0).equalsIgnoreCase(ChatColor.translateAlternateColorCodes('&', QuickSell.cfg.getString("options.sellall-sign-prefix")))) {
                    Shop shop = Shop.getShop(sign.getLine(1));
                    if (shop != null) {
                        if (shop.hasUnlocked(e.getPlayer())) {
                            shop.showPrices(e.getPlayer());
                            return;
                        }
                        QuickSell.local.sendMessage(e.getPlayer(), "messages.no-access", false);
                        return;
                    }

                    if (Shop.getHighestShop(e.getPlayer()) != null) {
                        Shop.getHighestShop(e.getPlayer()).showPrices(e.getPlayer());
                    }
                }
            }
        }
    }
}
