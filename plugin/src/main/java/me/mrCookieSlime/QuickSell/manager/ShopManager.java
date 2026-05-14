package me.mrCookieSlime.QuickSell.manager;

import dev.dejvokep.boostedyaml.YamlDocument;
import me.mrCookieSlime.QuickSell.QuickSell;
import me.mrCookieSlime.QuickSell.boosters.BoosterType;
import me.mrCookieSlime.QuickSell.core.utils.enums.Messages;
import me.mrCookieSlime.QuickSell.listeners.custom.SellEvent;
import me.mrCookieSlime.QuickSell.shop.PriceInfo;
import me.mrCookieSlime.QuickSell.shop.Shop;
import me.mrCookieSlime.QuickSell.utils.DoubleHandler;
import me.mrCookieSlime.QuickSell.utils.InventoryUtils;
import me.mrCookieSlime.QuickSell.utils.SellType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ShopManager {

    private final QuickSell plugin;
    private final File shopsFolder;
    private final Map<String, Shop> shops = new LinkedHashMap<>();

    public ShopManager(QuickSell plugin) {
        this.plugin = plugin;
        this.shopsFolder = new File(plugin.getDataFolder(), "shops");
        if (!shopsFolder.exists()) shopsFolder.mkdirs();
    }

    // --- CARGA Y HERENCIA ---

    public void loadShops() {
        shops.clear();
        File[] files = shopsFolder.listFiles((dir, name) -> name.endsWith(".yml"));

        if (files == null || files.length == 0) {
            return;
        }

        List<Shop> loaded = new ArrayList<>();
        for (File file : files) {
            try {
                YamlDocument config = YamlDocument.create(file);
                loaded.add(new Shop(config.getStringRouteMappedValues(false)));
            } catch (IOException e) {
                plugin.getLogger().severe("Error cargando tienda: " + file.getName());
            }
        }

        // 1. Ordenar por prioridad (Crucial para getHighestShop)
        loaded.sort(Comparator.comparingInt(Shop::getPriority).reversed());

        // 2. Pasarlas al Mapa principal
        loaded.forEach(shop -> shops.put(shop.getId().toLowerCase(), shop));

        // 3. RESOLVER HERENCIA (Ahora que todas están cargadas y en orden)
        shops.values().forEach(this::resolveShopInheritance);

        plugin.getLogger().info("Se han cargado " + shops.size() + " tiendas con herencia resuelta.");
    }

    private void resolveShopInheritance(Shop shop) {
        PriceInfo info = shop.getPrices();

        info.sync();

        // 3. Recorremos los IDs de las tiendas de las que hereda
        for (String parentId : shop.getInheritance()) {
            getShop(parentId).ifPresent(parentShop -> {
                // Recursividad: Nos aseguramos de que el padre ya tenga sus herencias resueltas
                resolveShopInheritance(parentShop);

                // 4. Fusionamos los precios consolidados del padre en el hijo
                // mergePrices internamente usa putIfAbsent, respetando los precios locales del hijo
                info.mergePrices(parentShop.getPrices());
            });
        }
    }

    public void saveShops() {
        if (shops.isEmpty()) return;
        shops.values().forEach(this::saveShop);
    }

    // --- MÉTODOS DE VENTA (EL UNIFICADOR) ---

    /**
     * El comando /sellall llama aquí.
     */
    public void sellAll(Player p) {
        getHighestShop(p).ifPresent(shop -> prepareSellAll(p, shop, null, SellType.SELLALL));
    }

    public void sellAll(Player p, Shop shop) {
        prepareSellAll(p, shop, null, SellType.SELLALL);
    }

    public void sellItemInShop(Player p, Shop shop, String itemName) {
        Material mat = Material.matchMaterial(itemName.replace(" ", "_").toUpperCase());
        prepareSellAll(p, shop, mat, SellType.SELLALL);
    }

    private void prepareSellAll(Player p, Shop shop, Material filter, SellType type) {
        List<ItemStack> itemsToSell = new ArrayList<>();
        Inventory inv = p.getInventory();

        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack is = inv.getItem(i);
            if (is == null || is.getType() == Material.AIR) continue;

            // Filtrar por material si es necesario
            if (filter != null && is.getType() != filter) continue;

            // Solo añadimos a la lista si la tienda realmente lo compra
            if (shop.getPrices().getPrice(is) > 0) {
                itemsToSell.add(is.clone());
                inv.setItem(i, null); // Lo quitamos del inventarios antes de procesar
            }
        }

        if (itemsToSell.isEmpty()) {
            plugin.getMessageHandler().build(p, "messages.no-items").send();
            return;
        }

        executeSale(p, shop, itemsToSell, type, false);
    }


    /**
     * Lógica central de transacciones. Reemplaza a 'sell', 'sellItems' y 'handoutReward'.
     */
    private void executeSale(Player p, Shop shop, List<ItemStack> items, SellType type, boolean silent) {
        double rawMoney = 0.0;
        int totalItems = 0;

        for (ItemStack is : items) {
            double unitPrice = shop.getPrices().getPrice(is);
            if (unitPrice > 0) {
                rawMoney += (unitPrice * is.getAmount());
                totalItems += is.getAmount();
            } else {
                InventoryUtils.handleRest(p, is);
            }
        }

        // Aplicar Boosters
        double multiplier = plugin.getBoosterManager().getTotalMultiplier(p, BoosterType.MONETARY);
        double finalMoney = DoubleHandler.fixDouble(rawMoney * multiplier, 2);

        // Pagar
        QuickSell.economy.depositPlayer(p, finalMoney);

        // Feedback
        if (!silent) {
            sendFeedback(p, rawMoney, finalMoney, totalItems, multiplier);
            executeCommands(p, finalMoney);
        }

        new SellEvent(p, type, totalItems, finalMoney).call();
        p.updateInventory();
    }

    private void sendFeedback(Player p, double raw, double total, int count, double mult) {
        // Mensaje estándar
        plugin.getMessageHandler().build(p, Messages.SELL)
                .placeholder("{MONEY}", DoubleHandler.getFancyDouble(raw))
                .placeholder("{ITEMS}", String.valueOf(count))
                .send();

        // Mensaje de Booster (si aplica)
        if (mult > 1.0) {
            plugin.getMessageHandler().build(p, "messages.total-with-boosters")
                    .placeholder("{EXTRA}", DoubleHandler.getFancyDouble(total - raw))
                    .placeholder("{TOTAL}", DoubleHandler.getFancyDouble(total))
                    .placeholder("{MULTIPLIER}", String.valueOf(mult))
                    .send();
        }

        // Sonido
        YamlDocument config = plugin.getConfiguration();
        if (config.getBoolean("sound.enabled")) {
            p.playSound(p.getLocation(), Sound.valueOf(config.getString("sound.sound", "ENTITY_EXPERIENCE_ORB_PICKUP")), 1F, 1F);
        }
    }

    private void executeCommands(Player p, double money) {
        plugin.getConfiguration().getStringList("commands-on-sell").forEach(cmd -> {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                    cmd.replace("%player%", p.getName()).replace("%money%", String.valueOf(money)));
        });
    }

    // --- UTILIDADES ---

    public Collection<Shop> getAllShops() {
        return shops.values();
    }

    public Optional<Shop> getHighestShop(Player p) {
        return shops.values().stream().filter(s -> s.hasUnlocked(p)).findFirst();
    }

    public Optional<Shop> getShop(String id) {
        return Optional.ofNullable(shops.get(id.toLowerCase()));
    }

    public void saveShop(Shop shop) {
        File file = new File(shopsFolder, shop.getId() + ".yml");
        try {
            YamlDocument config = YamlDocument.create(file);
            shop.serialize().forEach(config::set);
            config.save();
        } catch (IOException e) {
            plugin.getLogger().severe("No se pudo guardar la tienda " + shop.getId());
        }
    }
}