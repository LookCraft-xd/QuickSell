package me.mrCookieSlime.QuickSell.shop;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.PaginatedGui;
import me.mrCookieSlime.QuickSell.QuickSell;
import me.mrCookieSlime.QuickSell.utils.Variable;
import me.mrCookieSlime.QuickSell.utils.helpers.input.Input;
import me.mrCookieSlime.QuickSell.utils.helpers.input.InputType;
import me.mrCookieSlime.QuickSell.utils.maths.DoubleHandler;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ShopEditor implements Listener {

    Map<UUID, Input> input;
    QuickSell quicksell;

    final int shop_size = 36;

    public ShopEditor(QuickSell quicksell) {
        this.quicksell = quicksell;
        this.input = new HashMap<UUID, Input>();
        quicksell.getServer().getPluginManager().registerEvents(this, quicksell);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent e) {
        if (input.containsKey(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);

            Input input = this.input.get(e.getPlayer().getUniqueId());

            switch (input.getType()) {
                case NEW_SHOP: {
                    List<String> list = new ArrayList<String>();
                    Shop.list().forEach(shop -> list.add(shop.getID()));

                    for (int i = list.size(); i <= (Integer) input.getValue(); i++) {
                        list.add("");
                    }

                    list.set((Integer) input.getValue(), ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', e.getMessage())));

                    QuickSell.cfg.setValue("list", list);
                    QuickSell.cfg.setValue("shops." + ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', e.getMessage())) + ".name", e.getMessage());
                    QuickSell.cfg.save();

                    QuickSell.local.sendMessage(e.getPlayer(), "commands.shop-created", false, new Variable("%shop%", e.getMessage()));

                    openEditor(e.getPlayer());

                    this.input.remove(e.getPlayer().getUniqueId());
                    break;
                }
                case RENAME: {
                    Shop shop = (Shop) input.getValue();

                    QuickSell.cfg.setValue("shops." + shop.getID() + ".name", e.getMessage());
                    QuickSell.cfg.save();
                    quicksell.reload();

                    openShopEditor(e.getPlayer(), Shop.getShop(shop.getID()));
                    QuickSell.local.sendMessage(e.getPlayer(), "editor.renamed-shop", false);

                    this.input.remove(e.getPlayer().getUniqueId());
                    break;
                }
                case SET_PERMISSION: {
                    Shop shop = (Shop) input.getValue();

                    QuickSell.cfg.setValue("shops." + shop.getID() + ".permission", e.getMessage().equals("none") ? "" : e.getMessage());
                    QuickSell.cfg.save();
                    quicksell.reload();

                    openShopEditor(e.getPlayer(), Shop.getShop(shop.getID()));
                    QuickSell.local.sendMessage(e.getPlayer(), "editor.permission-set-shop", false);

                    this.input.remove(e.getPlayer().getUniqueId());
                    break;
                }
                default:
                    break;
            }
        }
    }

    /**
     * Opens the shop editor for a player
     *
     * @param p Player
     */
    public void openEditor(Player p) {
        quicksell.reload();
        Gui gui = Gui.gui()
                .title(Component.text("&6QuickSell - Shop Editor"))
                .rows(6)
                .create();

        gui.setDefaultClickAction(event -> event.setCancelled(true));
        gui.setOpenGuiAction(event -> p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1F, 1F));

        // Tiendas existentes
        for (Shop shop : Shop.list()) {
            gui.addItem(ItemBuilder.from(shop.getItem(ShopStatus.UNLOCKED))
                    .name(Component.text(shop.getName()))
                    .lore(Component.text("&rLeft Click: &7Edit Shop"),
                            Component.text("&rRight Click: &7Edit Contents"),
                            Component.text("&rShift + Right Click: &4Delete Shop"))
                    .asGuiItem(event -> {
                        if (event.isRightClick()) {
                            if (event.isShiftClick()) {
                                deleteShop(p, shop);
                            } else {
                                openShopContentEditor(p, shop);
                            }
                        } else {
                            openShopEditor(p, shop);
                        }
                    }));
        }

        // Botón para nueva tienda (siempre al final o en un slot libre)
        gui.addItem(ItemBuilder.from(Material.GOLD_NUGGET)
                .name(Component.text("&cNew Shop"))
                .lore(Component.text("Click: &7Create a new Shop"))
                .asGuiItem(event -> {
                    input.put(p.getUniqueId(), new Input(InputType.NEW_SHOP, event.getSlot()));
                    QuickSell.local.sendMessage(p, "editor.create-shop", false);
                    gui.close(p);
                }));

        gui.open(p);
    }

    private void deleteShop(Player p, Shop shop) {
        // 1. Obtener la lista actual de IDs de tiendas
        List<String> shopList = QuickSell.cfg.getStringList("list");

        // 2. Eliminar el ID de la lista
        shopList.remove(shop.getID());
        QuickSell.cfg.setValue("list", shopList);

        // 3. Borrar la sección completa de la tienda en la config
        QuickSell.cfg.setValue("shops." + shop.getID(), null);

        // 4. Guardar y recargar
        QuickSell.cfg.save();
        quicksell.reload();

        // 5. Notificar y refrescar el menú del editor
        p.sendMessage("&cLa tienda &f" + shop.getName() + " &cha sido eliminada.");
        openEditor(p);
    }

    /**
     * Opens a GUI to edit a shop with
     *
     * @param p    Player
     * @param shop Shop
     */
    public void openShopEditor(Player p, final Shop shop) {
        quicksell.reload();
        Gui gui = Gui.gui().title(Component.text("&6Settings: " + shop.getName())).rows(3).create();
        gui.setDefaultClickAction(event -> event.setCancelled(true));

        // Botón Nombre
        gui.setItem(2, 2, ItemBuilder.from(Material.NAME_TAG).name(Component.text("&eName: " + shop.getName())).asGuiItem(e -> {
            input.put(p.getUniqueId(), new Input(InputType.RENAME, shop));
            QuickSell.local.sendMessage(p, "editor.rename-shop", false);
            p.closeInventory();
        }));

        // Botón Icono
        gui.setItem(2, 4, ItemBuilder.from(shop.getItem(ShopStatus.UNLOCKED).getType()).name(Component.text("&eDisplay Item")).addLore("&rClick: &7Change Item to the Item held in your Hand").asGuiItem(e -> {
            ItemStack hand = p.getInventory().getItemInMainHand();
            if (hand != null && hand.getType() != Material.AIR) {
                QuickSell.cfg.setValue("shops." + shop.getID() + ".itemtype", hand.getType().toString());
                QuickSell.cfg.save();
                quicksell.reload();
                openShopEditor(p, Shop.getShop(shop.getID()));
            }
        }));

        // Botón Permiso
        gui.setItem(2, 6, ItemBuilder.from(Material.DIAMOND)
                .name(Component.text("&7Shop Permission: &r" + (shop.getPermission().equals("") ? "None": shop.getPermission())))
                .addLore("&rClick: &7Change Permission Node")
                .asGuiItem(e -> {
            input.put(p.getUniqueId(), new Input(InputType.SET_PERMISSION, shop));
            QuickSell.local.sendMessage(p, "editor.set-permission-shop", false);
            p.closeInventory();
        }));

        // Botón Herencia
        gui.setItem(2, 8, ItemBuilder.from(Material.COMMAND_BLOCK).name(Component.text("&bInheritance")).addLore("&rClick: &7Open Inheritance Manager").asGuiItem(e -> openShopInheritanceEditor(p, shop)));

        // Volver
        gui.setItem(3, 5, ItemBuilder.from(Material.BARRIER).name(Component.text("&cBack")).asGuiItem(e -> openEditor(p)));

        gui.open(p);
    }

    /**
     * Opens a shop content editor GUI
     *
     * @param p    Player
     * @param shop Shop
     */
    public void openShopContentEditor(Player p, final Shop shop) {
        quicksell.reload();
        PaginatedGui gui = Gui.paginated()
                .title(Component.text("&6Editing: " + shop.getName()))
                .rows(6)
                .pageSize(45)
                .create();

        gui.setDefaultTopClickAction(event -> event.setCancelled(true));

        // Botones de navegación en la fila inferior
        gui.getFiller().fillBottom(ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE).name(Component.empty()).asGuiItem());

        gui.setItem(6, 5, ItemBuilder.from(Material.GOLD_INGOT).name(Component.text("&7\u21E6 Back")).asGuiItem(e -> openEditor(p)));
        gui.setItem(6, 3, ItemBuilder.from(Material.LIME_STAINED_GLASS_PANE).name(Component.text("&aPrevious Page")).asGuiItem(e -> gui.previous()));
        gui.setItem(6, 7, ItemBuilder.from(Material.LIME_STAINED_GLASS_PANE).name(Component.text("&aNext Page")).asGuiItem(e -> gui.next()));

        // Cargar Items de la tienda
        for (String id : shop.getPrices().getItems()) {
            ItemStack stack = shop.getPrices().getItem(id);
            double price = shop.getPrices().getPrice(id);

            gui.addItem(ItemBuilder.from(stack)
                    .lore(Component.text("&7Price: &6$" + DoubleHandler.getFancyDouble(price)),
                            Component.empty(),
                            Component.text("&rLeft Click: &7Edit Price"),
                            Component.text("&rShift + Right Click: &cRemove"))
                    .asGuiItem(event -> {
                        if (event.isShiftClick() && event.isRightClick()) {
                            removeItem(p, shop, id);
                        } else {
                            openPriceEditor(p, shop, stack, id, price);
                        }
                    }));
        }

        // Botón para añadir nuevo ítem (al final de la lista)
        gui.addItem(ItemBuilder.from(Material.COMMAND_BLOCK)
                .name(Component.text("&cAdd Item"))
                        .addLore("&rLeft Click: &7Add an Item to this Shop")
                .asGuiItem(e -> openItemEditor(p, shop)));

        gui.open(p);
    }

    private void removeItem(Player p, Shop shop, String itemKey) {
        // 1. Verificamos que la sección exista antes de borrar
        if (QuickSell.cfg.getConfiguration().getConfigurationSection("shops." + shop.getID() + ".price") != null) {

            // 2. Removemos el ítem estableciendo su valor a null
            QuickSell.cfg.setValue("shops." + shop.getID() + ".price." + itemKey, null);

            // 3. Guardamos los cambios en el archivo
            QuickSell.cfg.save();

            // 4. Recargamos la caché interna del plugin
            quicksell.reload();

            // 5. Feedback visual/sonoro para el usuario
            p.sendMessage("&cÍtem &f" + itemKey + " &celiminado de la tienda &f" + shop.getName());
            p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_BREAK, 0.5F, 2F);

            // 6. Refrescamos el menú de contenido (usando el shop actualizado de la caché)
            openShopContentEditor(p, Shop.getShop(shop.getID()));
        } else {
            p.sendMessage("&cError: No se pudo encontrar la sección de precios para esta tienda.");
        }
    }

    /**
     * Opens an GUI to edit items from a shop
     *
     * @param p    Player
     * @param shop Shop
     */
    public void openItemEditor(Player p, final Shop shop) {
        final ItemStack item = p.getItemOnCursor();

        if (item == null || item.getType() == Material.AIR) {
            p.sendMessage("Arrastra el item que desear agregar al botón de agregar!");
            return;
        }

        Gui gui = Gui.gui()
                .title(Component.text("&6Modo de adición"))
                .rows(3)
                .create();

        gui.setDefaultClickAction(event -> event.setCancelled(true));

        gui.setItem(1, 5, ItemBuilder.from(item).asGuiItem());

        // Solo Material
        gui.setItem(2, 2, ItemBuilder.from(Material.LIME_WOOL)
                .name(Component.text("&2Solo Material &7(Ej: STONE)"))
                .lore(Component.text("&7Ignora nombres y lore."))
                .asGuiItem(e -> addItemToShop(p, shop, item.getType().toString(), item, "Material")));

        // TODO Agregar material con data valores custom??

        // Material + Nombre
        gui.setItem(2, 5, ItemBuilder.from(Material.CYAN_WOOL)
                .name(Component.text("&2Material + Nombre"))
                .lore(Component.text("&7Respeta el nombre personalizado."))
                .asGuiItem(e -> {
                    if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
                        p.sendMessage("&cEste ítem no tiene un nombre personalizado.");
                        return;
                    }
                    String key = item.getType().toString() + "-" + item.getItemMeta().getDisplayName().replace("&", "&");
                    addItemToShop(p, shop, key, item, "Material + Nombre");
                }));

        // Cancell
        gui.setItem(2, 8, ItemBuilder.from(Material.RED_WOOL).name(Component.text("&cCancelar")).asGuiItem(e -> openShopContentEditor(p, shop)));

        gui.open(p);
    }

    private void addItemToShop(Player p, Shop shop, String key, ItemStack item, String mode) {
        QuickSell.cfg.setValue("shops." + shop.getID() + ".price." + key, 1.0D);
        QuickSell.cfg.save();
        quicksell.reload();

        p.sendMessage("&aAñadido como " + mode);
        openShopContentEditor(p, Shop.getShop(shop.getID()));
    }

    /**
     * Opens the price editor for a shop
     *
     * @param p            Player
     * @param shop         Shop
     * @param item         ItemStack
     * @param id           String
     * @param currentPrice Double
     */
    public void openPriceEditor(Player p, final Shop shop, ItemStack item, String id, double currentPrice) {
        Gui gui = Gui.gui().title(Component.text("&6Set Price")).rows(3).create();
        gui.setDefaultClickAction(event -> event.setCancelled(true));

        // Item central de referencia
        gui.setItem(1, 5, ItemBuilder.from(item).lore(Component.text("&eCurrent: $" + currentPrice)).asGuiItem());

        // Valores de los botones
        long[] values = {1, 10, 100, 1000, 10000, 100000, 1000000};
        int slot = 9; // Empezar en la segunda fila

        for (long val : values) {
            if (slot > 17) break;
            gui.setItem(slot++, ItemBuilder.from(Material.GOLD_INGOT)
                    .name(Component.text("&e+/- $" + val))
                    .lore(Component.text("&7Left Click: &a+" + val),
                            Component.text("&7Right Click: &c-" + val))
                    .asGuiItem(e -> {
                        double nextPrice = e.isLeftClick() ? currentPrice + val : currentPrice - val;
                        openPriceEditor(p, shop, item, id, Math.max(0.1, nextPrice));
                    }));
        }

        // Guardar / Cancelar
        gui.setItem(3, 3, ItemBuilder.from(Material.LIME_WOOL).name(Component.text("&aSave")).asGuiItem(e -> {
            savePrice(shop, id, currentPrice);
            openShopContentEditor(p, shop);
        }));
        gui.setItem(3, 7, ItemBuilder.from(Material.RED_WOOL).name(Component.text("&cCancel")).asGuiItem(e -> openShopContentEditor(p, shop)));

        gui.open(p);
    }

    private void savePrice(Shop shop, String itemKey, double newPrice) {
        // Actualizamos el valor en el archivo de configuración
        QuickSell.cfg.setValue("shops." + shop.getID() + ".price." + itemKey, newPrice);
        QuickSell.cfg.save();

        // Recargamos el plugin para que PriceInfo y Shop actualicen sus mapas internos
        quicksell.reload();
    }

    /**
     * Opens the shop inheritance menu
     *
     * @param p Player
     * @param s Shop
     */
    public void openShopInheritanceEditor(final Player p, final Shop s) {
        quicksell.reload();
        PaginatedGui gui = Gui.paginated().title(Component.text("&6Inheritance: " + s.getName())).rows(6).pageSize(45).create();
        gui.setDefaultClickAction(event -> event.setCancelled(true));

        List<String> currentInheritance = QuickSell.cfg.getStringList("shops." + s.getID() + ".inheritance");

        for (Shop otherShop : Shop.list()) {
            if (otherShop.getID().equalsIgnoreCase(s.getID())) continue;

            boolean inherit = currentInheritance.contains(otherShop.getID());
            gui.addItem(ItemBuilder.from(otherShop.getItem(ShopStatus.UNLOCKED))
                    .name(Component.text(otherShop.getName()))
                    .lore(Component.text("&7Inherit: " + (inherit ? "&a&l✔" : "&c&l✘")))
                    .asGuiItem(e -> {
                        if (inherit) currentInheritance.remove(otherShop.getID());
                        else currentInheritance.add(otherShop.getID());

                        QuickSell.cfg.setValue("shops." + s.getID() + ".inheritance", currentInheritance);
                        QuickSell.cfg.save();
                        openShopInheritanceEditor(p, Shop.getShop(s.getID()));
                    }));
        }

        gui.getFiller().fillBottom(ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE).name(Component.empty()).asGuiItem());
        gui.setItem(6, 5, ItemBuilder.from(Material.ARROW).name(Component.text("&7Back")).asGuiItem(e -> openShopEditor(p, s)));

        gui.open(p);
    }

}
