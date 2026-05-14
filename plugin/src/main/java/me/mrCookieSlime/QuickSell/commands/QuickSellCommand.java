package me.mrCookieSlime.QuickSell.commands;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.optional.OptionalArg;
import dev.rollczi.litecommands.annotations.permission.Permission;
import me.mrCookieSlime.QuickSell.QuickSell;
import me.mrCookieSlime.QuickSell.boosters.Booster;
import me.mrCookieSlime.QuickSell.core.utils.enums.Messages;
import me.mrCookieSlime.QuickSell.core.utils.message.MessageBuilder;
import me.mrCookieSlime.QuickSell.core.utils.message.MessageHandler;
import me.mrCookieSlime.QuickSell.manager.BoosterManager;
import me.mrCookieSlime.QuickSell.shop.Shop;
import me.mrCookieSlime.QuickSell.utils.DoubleHandler;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

@Command(name = "quicksell", aliases = "qs")
@Permission("quicksell.manage")
public class QuickSellCommand {

    public final QuickSell plugin;
    private final BoosterManager boosterManager;
    private final MessageHandler messageHandler;

    public QuickSellCommand(QuickSell plugin) {
        this.plugin = plugin;
        this.boosterManager = plugin.getBoosterManager();
        this.messageHandler = plugin.getMessageHandler();
    }

    @Execute(name = "reload")
    public void onReload(@Context CommandSender sender) throws IOException {
        plugin.getConfiguration().reload();
        plugin.getMessages().reload();
        plugin.getShopManager().loadShops();
        plugin.getBoosterManager().loadBoosters();
        messageHandler.build(sender, Messages.RELOAD).send();
    }

    @Execute(name = "editor")
    public void onEditor(@Context CommandSender sender) {
        if (sender instanceof Player) plugin.getEditor().openEditor((Player) sender);
    }

    // Might need some work...
//    @Execute(name = "edit")
//    public void onEdit(@Context CommandSender sender, @Arg String shop, @Arg String item, @Arg Double price) {
//        if (Shop.getShop(shop) == null) {
//            messageHandler.build(sender, Messages.UNKNOWN_SHOP).send();
//            return;
//        }
//
//        YamlDocument config = plugin.getConfiguration();
//        config.set("shops." + shop + ".price." + item.toUpperCase(), price);
//
//        saveConfig(config);
//        //plugin.reload();
//
//        messageHandler.build(sender, Messages.PRICE_SET)
//                .placeholder("%shop%", shop)
//                .placeholder("%price%", DoubleHandler.getFancyDouble(price))
//                .placeholder("%item%", item.toUpperCase())
//                .send();
//    }


    //TODO Create own shop manager commands ?
//
//    @Execute(name = "create")
//    public void onCreate(@Context CommandSender sender, @Arg String shopName) {
//        YamlDocument config = plugin.getConfiguration();
//        List<String> shops = config.getStringList("list");
//
//        if (!shops.contains(shopName)) {
//            shops.add(shopName);
//            config.set("list", shops);
//            // Opcional: inicializar valores básicos para la tienda
//            config.set("shops." + shopName + ".name", "&9" + shopName);
//
//            saveConfig(config);
//            //plugin.reload();
//            messageHandler.build(sender, Messages.SHOP_CREATED).placeholder("%shop%", shopName).send();
//        }
//    }
//
//    @Execute(name = "delete")
//    public void onDelete(@Context CommandSender sender, @Arg String shopName) {
//        YamlDocument config = plugin.getConfiguration();
//        List<String> shops = config.getStringList("list");
//
//        if (shops.remove(shopName)) {
//            config.set("list", shops);
//            config.set("shops." + shopName, null); // Elimina la sección completa
//
//            saveConfig(config);
//            //plugin.reload();
//            messageHandler.build(sender, Messages.SHOP_DELETED).placeholder("%shop%", shopName).send();
//        }
//    }

    private void saveConfig(YamlDocument config) {
        try {
            config.save();
        } catch (IOException e) {
            QuickSell.log(Level.SEVERE, "Could not save config.yml: " + e.getMessage());
        }
    }

    @Execute(name = "stopboosters")
    public void stopBoosters(@Context CommandSender sender, @OptionalArg String player) {
        List<Booster> active = boosterManager.getActiveBoosters();

        if (active.isEmpty()) {
            sender.sendMessage("No hay boosters activos para detener.");
            return;
        }

        Iterator<Booster> it = active.iterator();
        boolean found = false;

        while (it.hasNext()) {
            Booster booster = it.next();

            // Si player es null (reset global) o el dueño coincide
            if (player == null || booster.getOwner().equalsIgnoreCase(player)) {
                boosterManager.deactivateBooster(booster);
                found = true;
            }
        }

        if (!found && player != null) {
            sender.sendMessage("No se encontraron boosters para el jugador: " + player);
            return;
        }

        MessageBuilder builder = messageHandler.build(sender, Messages.BOOSTER_RESET);
        if (player != null) builder.placeholder("%player%", player);
        builder.send();
    }
}
