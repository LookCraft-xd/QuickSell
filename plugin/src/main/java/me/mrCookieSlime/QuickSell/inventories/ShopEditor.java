package me.mrCookieSlime.QuickSell.inventories;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.PaginatedGui;
import me.mrCookieSlime.QuickSell.QuickSell;
import me.mrCookieSlime.QuickSell.core.utils.enums.Messages;
import me.mrCookieSlime.QuickSell.core.utils.message.MessageHandler;
import me.mrCookieSlime.QuickSell.core.utils.message.helpers.MessageUtils;
import me.mrCookieSlime.QuickSell.helpers.input.Input;
import me.mrCookieSlime.QuickSell.helpers.input.InputType;
import me.mrCookieSlime.QuickSell.manager.ShopManager;
import me.mrCookieSlime.QuickSell.shop.Shop;
import me.mrCookieSlime.QuickSell.utils.DoubleHandler;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemFlag;
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
                String cleanId = MessageUtils.strip(message).toLowerCase();

                // Crear nueva tienda física (archivo)
                Shop newShop = new Shop(cleanId);
                shopManager.saveShop(newShop);

                messageHandler.build(p, Messages.SHOP_CREATED).placeholder("%shop%", message).send();
                quicksell.getServer().getScheduler().runTask(quicksell, () -> openEditor(p));
                break;
            }
            case RENAME: {
                Shop shop = (Shop) inputData.getValue();
                shop.setName(message);
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
                .title(messageHandler.build(Messages.GUI_EDITOR_TITLE).asComponent())
                .rows(6)
                .create();

        gui.setDefaultClickAction(event -> event.setCancelled(true));

        // Usamos el manager para listar las tiendas cargadas
        for (Shop shop : shopManager.getAllShops()) {
            ItemBuilder itemBuilder = ItemBuilder.from(shop.getMaterial())
                    .name(Component.text(shop.getName()))
                    .lore(messageHandler.build(Messages.GUI_EDITOR_SHOP_LORE).asComponentList())
                    .flags(ItemFlag.HIDE_ATTRIBUTES);

            gui.addItem(itemBuilder.asGuiItem(event -> {
                if (event.isRightClick()) {
                    if (event.isShiftClick()) deleteShop(p, shop);
                    else openShopContentEditor(p, shop);
                } else openShopEditor(p, shop);
            }));
        }

        gui.addItem(ItemBuilder.from(Material.GOLD_NUGGET)
                .name(messageHandler.build(Messages.EDITOR_CREATE_SHOP).asComponent())
                .lore(messageHandler.build(Messages.GUI_EDITOR_NEW_SHOP_LORE).asComponentList())
                .asGuiItem(event -> {
                    input.put(p.getUniqueId(), new Input(InputType.NEW_SHOP, null));
                    messageHandler.build(p, Messages.EDITOR_CREATE_SHOP).send();
                    p.closeInventory();
                }));

        gui.open(p);
    }

    private void deleteShop(Player p, Shop shop) {
        File shopFile = new File(quicksell.getDataFolder(), "shops/" + shop.getId() + ".yml");
        if (shopFile.exists()) shopFile.delete();

        messageHandler.build(Messages.SHOP_DELETED).placeholder("%shop%", shop.getName()).send();
        openEditor(p);
    }

    public void openShopEditor(Player p, final Shop shop) {
        Gui gui = Gui.gui().title(messageHandler.build(Messages.GUI_SETTINGS_TITLE).placeholder("%shop%", shop.getName()).asComponent()).rows(3).create();
        gui.setDefaultClickAction(event -> event.setCancelled(true));

        // Editar Nombre
        gui.setItem(2, 2, ItemBuilder.from(Material.NAME_TAG)
                .name(messageHandler.build(Messages.GUI_SETTINGS_NAME).placeholder("%name%", shop.getName()).asComponent())
                .lore(messageHandler.build(Messages.GUI_SETTINGS_NAME_LORE).asComponentList())
                .asGuiItem(e -> {
                    input.put(p.getUniqueId(), new Input(InputType.RENAME, shop));
                    messageHandler.build(p, Messages.EDITOR_RENAME_SHOP).send();
                    p.closeInventory();
                }));

        // Editar Item Visual
        gui.setItem(2, 4, ItemBuilder.from(shop.getMaterial())
                .name(messageHandler.build(Messages.GUI_SETTINGS_ITEM).asComponent())
                .lore(messageHandler.build(Messages.GUI_SETTINGS_ITEM_LORE).asComponentList())
                .asGuiItem(e -> {
                    ItemStack hand = p.getInventory().getItemInMainHand();
                    if (hand.getType() != Material.AIR) {
                        shop.setMaterial(hand.clone().getType());
                        shopManager.saveShop(shop);
                        openShopEditor(p, shop);
                    }
                }));

        // Editar Permiso

        String none = messageHandler.getRawMessage("permission-none");
        gui.setItem(2, 6, ItemBuilder.from(Material.DIAMOND)
                .name(messageHandler.build(Messages.GUI_SETTINGS_PERMISSION)
                        .placeholder("%permission%", shop.getPermission().isEmpty() ? none : shop.getPermission()).asComponent())
                .lore(messageHandler.build(Messages.GUI_SETTINGS_PERMISSION_LORE).asComponentList())
                .asGuiItem(e -> {
                    input.put(p.getUniqueId(), new Input(InputType.SET_PERMISSION, shop));
                    messageHandler.build(p, Messages.EDITOR_SET_PERMISSION).send();
                    p.closeInventory();
                }));

        // Botón de Herencia
        gui.setItem(2, 8, ItemBuilder.from(Material.COMMAND_BLOCK)
                .name(messageHandler.build(Messages.GUI_SETTINGS_INHERITANCE).asComponent())
                .asGuiItem(e -> openShopInheritanceEditor(p, shop)));
        gui.setItem(3, 5, ItemBuilder.from(Material.BARRIER)
                .name(messageHandler.build(Messages.GUI_SETTINGS_BACK).asComponent())
                .asGuiItem(e -> openEditor(p)));

        gui.open(p);
    }

    public void openShopContentEditor(Player p, final Shop shop) {
        PaginatedGui gui = Gui.paginated().title(messageHandler.build(Messages.GUI_CONTENT_TITLE).placeholder("%shop%", shop.getName()).asComponent()).rows(6).pageSize(45).create();
        gui.setDefaultTopClickAction(event -> event.setCancelled(true));

        // Iterar sobre los items que tiene la tienda (usando el PriceInfo)
        shop.getPrices().getLocalPrices().forEach((material, price) -> {
            ItemStack stack = new ItemStack(Material.getMaterial(material));

            gui.addItem(ItemBuilder.from(stack)
                    .lore(messageHandler.build(Messages.GUI_CONTENT_EDIT_LORE).placeholder("%price%", DoubleHandler.getFancyDouble(price)).asComponentList())
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
        gui.addItem(ItemBuilder.from(Material.NETHER_STAR).name(messageHandler.build(Messages.GUI_CONTENT_ADD).asComponent()).asGuiItem(e -> openItemEditor(p, shop)));

        gui.getFiller().fillBottom(ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE).name(Component.empty()).asGuiItem());
        gui.setItem(6, 5, ItemBuilder.from(Material.GOLD_INGOT).name(messageHandler.build(Messages.GUI_CONTENT_BACK).asComponent())
                .asGuiItem(e -> openEditor(p)));

        gui.open(p);
    }

    public void openPriceEditor(Player p, final Shop shop, ItemStack item, String id, double currentPrice) {
        Gui gui = Gui.gui().title(messageHandler.build(Messages.GUI_PRICE_TITLE).asComponent()).rows(4).create();
        gui.setDefaultClickAction(event -> event.setCancelled(true));

        gui.setItem(1, 5, ItemBuilder.from(item)
                .lore(messageHandler.build(Messages.GUI_PRICE_CURRENT).placeholder("%price%", currentPrice).asComponent()).asGuiItem());

        long[] values = {1, 10, 100, 1000, 10000};
        int slot = 11;
        for (long val : values) {
            gui.setItem(slot++, ItemBuilder.from(Material.GOLD_INGOT)
                    .name(messageHandler.build(Messages.GUI_PRICE_ADJUST).placeholder("%value%", val).asComponent())
                    .asGuiItem(e -> {
                        double nextPrice = e.isLeftClick() ? currentPrice + val : currentPrice - val;
                        openPriceEditor(p, shop, item, id, Math.max(0.1, nextPrice));
                    }));
        }

        gui.setItem(4, 3, ItemBuilder.from(Material.LIME_WOOL).name(messageHandler.build(Messages.GUI_PRICE_SAVE).asComponent()).asGuiItem(e -> {
            shop.getPrices().setPrice(id, currentPrice);
            shopManager.saveShop(shop);
            openShopContentEditor(p, shop);
        }));

        gui.setItem(4, 7, ItemBuilder.from(Material.RED_WOOL).name(messageHandler.build(Messages.GUI_PRICE_CANCEL).asComponent()).asGuiItem(e -> openShopContentEditor(p, shop)));
        gui.open(p);
    }

    public void openShopInheritanceEditor(final Player p, final Shop s) {
        PaginatedGui gui = Gui.paginated().title(messageHandler.build(Messages.GUI_INHERIT_TITLE).asComponent()).rows(6).pageSize(45).create();
        gui.setDefaultClickAction(event -> event.setCancelled(true));

        for (Shop otherShop : shopManager.getAllShops()) {
            if (otherShop.getId().equalsIgnoreCase(s.getId())) continue;

            boolean inherit = s.getInheritance().contains(otherShop.getId());

            String enabled = messageHandler.getRawMessage(Messages.GUI_INHERIT_ON);
            String disabled = messageHandler.getRawMessage(Messages.GUI_INHERIT_OFF);
            gui.addItem(ItemBuilder.from(otherShop.getMaterial())
                    .name(Component.text(otherShop.getName()))
                    .lore(messageHandler.build(Messages.GUI_INHERIT_STATUS).placeholder("%status%", inherit ? enabled : disabled).asComponent())
                    .asGuiItem(e -> {
                        if (inherit) s.getInheritance().remove(otherShop.getId());
                        else s.getInheritance().add(otherShop.getId());

                        shopManager.saveShop(s);
                        openShopInheritanceEditor(p, s);
                    }));
        }

        gui.getFiller().fillBottom(ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE).name(Component.empty()).asGuiItem());
        gui.setItem(6, 5, ItemBuilder.from(Material.ARROW).name(messageHandler.build(Messages.GUI_CONTENT_BACK).asComponent()).asGuiItem(e -> openShopEditor(p, s)));
        gui.open(p);
    }

    // TODO Change system.
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
                .name(messageHandler.build(Messages.GUI_CONTENT_BACK).asComponent())
                .asGuiItem(e -> openShopContentEditor(p, shop)));

        gui.open(p);
    }
}
