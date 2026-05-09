package me.mrCookieSlime.QuickSell.commands;

import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.execute.ExecuteDefault;
import dev.rollczi.litecommands.annotations.optional.OptionalArg;
import dev.rollczi.litecommands.annotations.permission.Permission;
import me.mrCookieSlime.QuickSell.QuickSell;
import me.mrCookieSlime.QuickSell.shop.Shop;
import me.mrCookieSlime.QuickSell.boosters.Booster;
import me.mrCookieSlime.QuickSell.utils.Variable;
import me.mrCookieSlime.QuickSell.utils.maths.DoubleHandler;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Iterator;
import java.util.List;

@Command(name = "quicksell", aliases = "qs")
@Permission("quicksell.manage")
public class QuickSellCommand {

    public final QuickSell plugin;

    public QuickSellCommand(QuickSell plugin) {
        this.plugin = plugin;
    }

    @ExecuteDefault
    public void onDefault(@Context CommandSender sender) {
        sendHelpMessager(sender);
    }

    @Execute(name = "reload")
    public void onReload(@Context CommandSender sender) {
        plugin.reload();
        QuickSell.local.sendMessage(sender, "commands.reload.done", false);
    }

    @Execute(name = "editor")
    public void onEditor(@Context CommandSender sender) {
        if (sender instanceof Player) plugin.editor.openEditor((Player) sender);
    }

    @Execute(name = "edit")
    //@Syntax("<Shop Name> <Item> <Price>")
    public void onEdit(@Context CommandSender sender, @Arg String shop, @Arg String item, @Arg Double price) {
        if (Shop.getShop(shop) == null) {
            QuickSell.local.sendMessage(sender, "messages.unknown-shop", false);
            return;
        }

        if (item == null || price == null) {
            QuickSell.local.sendMessage(sender, "commands.usage", false, new Variable("%usage%", "/quicksell edit <ShopName> <Item> <Price>"));
        }

        QuickSell.cfg.setValue("shops." + shop + ".price." + item.toUpperCase(), price);
        QuickSell.cfg.save();
        plugin.reload();
        QuickSell.local.sendMessage(sender, "commands.price-set", false
                , new Variable("%item%", item.toUpperCase())
                , new Variable("%price%", DoubleHandler.getFancyDouble(price))
                , new Variable("%shop%", shop));
    }

    @Execute(name = "create")
    //@Syntax("<Shop Name>")
    public void onCreate(@Context CommandSender sender, @Arg String shopName) {
        if (shopName == null) {
            QuickSell.local.sendMessage(sender, "commands.usage", false, new Variable("%usage%", "/quicksell create <ShopName>"));
        }

        List<String> shops = QuickSell.cfg.getStringList("list");
        shops.add(shopName);
        QuickSell.cfg.setValue("list", shops);
        QuickSell.cfg.save();

        plugin.reload();

        QuickSell.local.sendMessage(sender, "commands.shop-created", false, new Variable("%shop%", shopName));
    }

    @Execute(name = "delete")
    //@Syntax("<Shop Name>")
    public void onDelete(@Context CommandSender sender, @Arg String shopName) {
        if (shopName == null) {
            QuickSell.local.sendMessage(sender, "commands.usage", false, new Variable("%usage%", "/quicksell delete <ShopName>"));
        }

        List<String> shops = QuickSell.cfg.getStringList("list");
        shops.remove(shopName);
        QuickSell.cfg.setValue("list", shops);
        QuickSell.cfg.save();

        plugin.reload();

        QuickSell.local.sendMessage(sender, "commands.shop-deleted", false, new Variable("%shop%", shopName));
    }

    @Execute(name = "stopboosters")
    //@Syntax("[Player]")
    public void stopBoosters(@Context CommandSender sender, @OptionalArg String player) {
        Iterator<Booster> boosters = Booster.iterate();

        if (player == null) {
            while (boosters.hasNext()) {
                Booster booster = boosters.next();
                boosters.remove();
                booster.deactivate();
            }
            QuickSell.local.sendMessage(sender, "boosters.reset", false);
            return;
        }

        while (boosters.hasNext()) {
            Booster booster = boosters.next();
            if (booster.getAppliedPlayers().contains(player)) {
                boosters.remove();
                booster.deactivate();
            }
        }
        QuickSell.local.sendMessage(sender, "booster.reset", false, new Variable("%player%", player));
    }

    private void sendHelpMessager(CommandSender sender) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', ""));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e&lQuickSell v" + plugin.getDescription().getVersion() + " by &6mrCookieSlime"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', ""));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7\u21E8 /quicksell: &bDisplays this Help Menu"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7\u21E8 /quicksell reload: &bReloads all of QuickSell's Files and Systems"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7\u21E8 /quicksell editor: &bOpens up the Ingame Shop Editor"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7\u21E8 /quicksell stopboosters [Player]: &bStops certain Boosters"));
    }

    public QuickSell getPlugin() {
        return plugin;
    }
}
