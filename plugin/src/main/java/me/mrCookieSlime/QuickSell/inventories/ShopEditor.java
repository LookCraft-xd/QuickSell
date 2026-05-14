package me.mrCookieSlime.QuickSell.inventories;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.PaginatedGui;
import me.mrCookieSlime.QuickSell.QuickSell;
import me.mrCookieSlime.QuickSell.core.utils.enums.Messages;
import me.mrCookieSlime.QuickSell.core.utils.message.MessageHandler;
import me.mrCookieSlime.QuickSell.helpers.input.Input;
import me.mrCookieSlime.QuickSell.helpers.input.InputType;
import me.mrCookieSlime.QuickSell.manager.ShopManager;
import me.mrCookieSlime.QuickSell.shop.Shop;
import me.mrCookieSlime.QuickSell.utils.DoubleHandler;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ShopEditor implements Listener {

    private final QuickSell quicksell;
    private final ShopManager shopManager;
    private final MessageHandler messageHandler;
    private final Map<UUID, Input> input = new HashMap<>();

    public ShopEditor(QuickSell quicksell) {
        this.quicksell = quicksell;
        this.shopManager = quicksell.getShopManager();
        this.messageHandler = quicksell.getMessageHandler();
        quicksell.getServer().getPluginManager().registerEvents(this, quicksell);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        if (!input.containsKey(uuid)) return;

        e.setCancelled(true);
        Player p = e.getPlayer();
        Input inputData = this.input.remove(uuid); // Consumimos el input
        String message = e.getMessage();

        switch (inputData.getType()) {
            case NEW_SHOP: {
                String cleanId = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', message)).toLowerCase();

                // Crear nueva tienda física (archivo)
                Shop newShop = new Shop(cleanId);
                shopManager.saveShop(newShop);

                messageHandler.build(p, Messages.SHOP_CREATED).placeholder("%shop%", message).send();
                openEditor(p);
                break;
            }
            case RENAME: {
                Shop shop = (Shop) inputData.getValue();
                shop.setName(message); // Asumiendo que añadiste el setter en Shop
                shopManager.saveShop(shop);

                messageHandler.build(p, Messages.EDITOR_RENAMED_SHOP).send();
                openShopEditor(p, shop);
                break;
            }
            case SET_PERMISSION: {
                Shop shop = (Shop) inputData.getValue();
                shop.setPermission(message.equalsIgnoreCase("none") ? "" : message);
                shopManager.saveShop(shop);

                messageHandler.build(p, Messages.EDITOR_PERMISSION_SET).send();
                openShopEditor(p, shop);
                break;
            }
        }
    }

    public void openEditor(Player p) {
        Gui gui = Gui.gui()
                .title(Component.text("&6QuickSell - Shop Editor"))
                .rows(6)
                .create();

        gui.setDefaultClickAction(event -> event.setCancelled(true));

        // Usamos el manager para listar las tiendas cargadas
        for (Shop shop : shopManager.getAllShops()) {
            gui.addItem(ItemBuilder.from(shop.getMaterial())
                    .name(Component.text(shop.getName()))
                    .lore(Component.text("&rLeft Click: &7Edit Shop"),
                            Component.text("&rRight Click: &7Edit Contents"),
                            Component.text("&rShift + Right Click: &4Delete Shop"))
                    .asGuiItem(event -> {
                        if (event.isRightClick()) {
                            if (event.isShiftClick()) deleteShop(p, shop);
                            else openShopContentEditor(p, shop);
                        } else openShopEditor(p, shop);
                    }));
        }

        gui.addItem(ItemBuilder.from(Material.GOLD_NUGGET)
                .name(Component.text("&cNew Shop"))
                .lore(Component.text("Click: &7Create a new Shop"))
                .asGuiItem(event -> {
                    input.put(p.getUniqueId(), new Input(InputType.NEW_SHOP, null));
                    messageHandler.build(p, Messages.EDITOR_CREATE_SHOP).send();
                    p.closeInventory();
                }));

        gui.open(p);
    }

    private void deleteShop(Player p, Shop shop) {
        // Lógica para borrar el archivo físico
        File shopFile = new File(quicksell.getDataFolder(), "shops/" + shop.getId() + ".yml");
        if (shopFile.exists()) shopFile.delete();

        //shopManager.loadShops(); // Refrescar memoria
        p.sendMessage("&cLa tienda &f" + shop.getName() + " &cha sido eliminada.");
        openEditor(p);
    }

    public void openShopEditor(Player p, final Shop shop) {
        Gui gui = Gui.gui().title(Component.text("&6Settings: " + shop.getName())).rows(3).create();
        gui.setDefaultClickAction(event -> event.setCancelled(true));

        // Editar Nombre
        gui.setItem(2, 2, ItemBuilder.from(Material.NAME_TAG).name(Component.text("&eName: " + shop.getName())).asGuiItem(e -> {
            input.put(p.getUniqueId(), new Input(InputType.RENAME, shop));
            messageHandler.build(p, Messages.EDITOR_RENAME_SHOP).send();
            p.closeInventory();
        }));

        // Editar Item Visual
        gui.setItem(2, 4, ItemBuilder.from(shop.getMaterial()).name(Component.text("&eDisplay Item")).addLore("&rClick: &7Set item in hand").asGuiItem(e -> {
            ItemStack hand = p.getInventory().getItemInMainHand();
            if (hand.getType() != Material.AIR) {
                shop.setMaterial(hand.clone().getType());
                shopManager.saveShop(shop);
                openShopEditor(p, shop);
            }
        }));

        // Editar Permiso
        gui.setItem(2, 6, ItemBuilder.from(Material.DIAMOND)
                .name(Component.text("&7Permission: &r" + (shop.getPermission().isEmpty() ? "None" : shop.getPermission())))
                .asGuiItem(e -> {
                    input.put(p.getUniqueId(), new Input(InputType.SET_PERMISSION, shop));
                    messageHandler.build(p, Messages.EDITOR_SET_PERMISSION).send();
                    p.closeInventory();
                }));

        // Botón de Herencia
        gui.setItem(2, 8, ItemBuilder.from(Material.COMMAND_BLOCK).name(Component.text("&bInheritance")).asGuiItem(e -> openShopInheritanceEditor(p, shop)));
        gui.setItem(3, 5, ItemBuilder.from(Material.BARRIER).name(Component.text("&cBack")).asGuiItem(e -> openEditor(p)));

        gui.open(p);
    }

    public void openShopContentEditor(Player p, final Shop shop) {
        PaginatedGui gui = Gui.paginated().title(Component.text("&6Editing: " + shop.getName())).rows(6).pageSize(45).create();
        gui.setDefaultTopClickAction(event -> event.setCancelled(true));

        // Iterar sobre los items que tiene la tienda (usando el PriceInfo)
        shop.getPrices().getLocalPrices().forEach((material, price) -> {
            ItemStack stack = new ItemStack(Material.getMaterial(material));

            gui.addItem(ItemBuilder.from(stack)
                    .lore(Component.text("&7Price: &6$" + DoubleHandler.getFancyDouble(price)),
                            Component.empty(),
                            Component.text("&rLeft Click: &7Edit Price"),
                            Component.text("&rShift + Right Click: &cRemove"))
                    .asGuiItem(event -> {
                        if (event.isShiftClick() && event.isRightClick()) {
                            shop.getPrices().removePrice(material);
                            shopManager.saveShop(shop);
                            openShopContentEditor(p, shop);
                        } else {
                            openPriceEditor(p, shop, stack, material, price);
                        }
                    }));
        });

        // Botón añadir item
        gui.addItem(ItemBuilder.from(Material.NETHER_STAR).name(Component.text("&aAdd Item")).asGuiItem(e -> openItemEditor(p, shop)));

        gui.getFiller().fillBottom(ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE).name(Component.empty()).asGuiItem());
        gui.setItem(6, 5, ItemBuilder.from(Material.GOLD_INGOT).name(Component.text("&7⇐ Back")).asGuiItem(e -> openEditor(p)));

        gui.open(p);
    }

    public void openPriceEditor(Player p, final Shop shop, ItemStack item, String id, double currentPrice) {
        Gui gui = Gui.gui().title(Component.text("&6Set Price")).rows(3).create();
        gui.setDefaultClickAction(event -> event.setCancelled(true));

        gui.setItem(1, 5, ItemBuilder.from(item).lore(Component.text("&eCurrent: $" + currentPrice)).asGuiItem());

        long[] values = {1, 10, 100, 1000, 10000};
        int slot = 10;
        for (long val : values) {
            gui.addItem(ItemBuilder.from(Material.GOLD_INGOT).name(Component.text("&e+/- $" + val))
                    .asGuiItem(e -> {
                        double nextPrice = e.isLeftClick() ? currentPrice + val : currentPrice - val;
                        openPriceEditor(p, shop, item, id, Math.max(0.1, nextPrice));
                    }));
        }

        gui.setItem(3, 3, ItemBuilder.from(Material.LIME_WOOL).name(Component.text("&aSave")).asGuiItem(e -> {
            shop.getPrices().setPrice(id, currentPrice);
            shopManager.saveShop(shop);
            openShopContentEditor(p, shop);
        }));

        gui.setItem(3, 7, ItemBuilder.from(Material.RED_WOOL).name(Component.text("&cCancel")).asGuiItem(e -> openShopContentEditor(p, shop)));
        gui.open(p);
    }

    public void openShopInheritanceEditor(final Player p, final Shop s) {
        PaginatedGui gui = Gui.paginated().title(Component.text("&6Inheritance: " + s.getName())).rows(6).pageSize(45).create();
        gui.setDefaultClickAction(event -> event.setCancelled(true));

        for (Shop otherShop : shopManager.getAllShops()) {
            if (otherShop.getId().equalsIgnoreCase(s.getId())) continue;

            boolean inherit = s.getInheritance().contains(otherShop.getId());

            gui.addItem(ItemBuilder.from(otherShop.getMaterial())
                    .name(Component.text(otherShop.getName()))
                    .lore(Component.text("&7Inherit: " + (inherit ? "&a&l✔" : "&c&l✘")))
                    .asGuiItem(e -> {
                        if (inherit) s.getInheritance().remove(otherShop.getId());
                        else s.getInheritance().add(otherShop.getId());

                        shopManager.saveShop(s);
                        //shopManager.loadShops(); // Esto recalcula las herencias en tiempo real
                        openShopInheritanceEditor(p, s);
                    }));
        }

        gui.getFiller().fillBottom(ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE).name(Component.empty()).asGuiItem());
        gui.setItem(6, 5, ItemBuilder.from(Material.ARROW).name(Component.text("&7Back")).asGuiItem(e -> openShopEditor(p, s)));
        gui.open(p);
    }

    public void openItemEditor(Player p, final Shop shop) {
        Gui gui = Gui.gui()
                .title(Component.text("&6Add Item to: " + shop.getName()))
                .rows(3)
                .create();

        gui.setDefaultClickAction(event -> event.setCancelled(true));

        // Opción 1: Añadir el ítem que el jugador tiene en la mano
        ItemStack itemInHand = p.getInventory().getItemInMainHand();
        boolean hasItem = itemInHand.getType() != Material.AIR;

        gui.setItem(2, 4, ItemBuilder.from(hasItem ? itemInHand.clone() : new ItemStack(Material.BARRIER))
                .name(Component.text(hasItem ? "&aClick to add: &f" + itemInHand.getType().name() : "&cPut an item in your hand first!"))
                .lore(hasItem ?
                        List.of(Component.text("&7This item will be added to the shop"), Component.text("&eClick to confirm and set price")) :
                        List.of(Component.text("&7You cannot add AIR to the shop.")))
                .asGuiItem(e -> {
                    if (!hasItem) {
                        p.sendMessage("&c¡Debes tener un ítem en la mano para añadirlo!");
                        return;
                    }

                    // Generamos un ID único para el ítem basado en su material (o NBT si fuera necesario)
                    // Para algo simple, el nombre del Material sirve como ID
                    String itemKey = itemInHand.getType().toString();

                    // Si el ítem ya existe, avisamos o simplemente vamos a editar su precio
                    openPriceEditor(p, shop, itemInHand.clone(), itemKey, 1.0);
                }));

        // Botón para volver atrás
        gui.setItem(2, 6, ItemBuilder.from(Material.ARROW)
                .name(Component.text("&7Go Back"))
                .asGuiItem(e -> openShopContentEditor(p, shop)));

        gui.open(p);
    }
}
