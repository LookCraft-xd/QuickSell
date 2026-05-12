package me.mrCookieSlime.QuickSell.listeners;

import me.mrCookieSlime.QuickSell.QuickSell;
import me.mrCookieSlime.QuickSell.boosters.Booster;
import me.mrCookieSlime.QuickSell.boosters.BoosterType;
import me.mrCookieSlime.QuickSell.interfaces.SellEvent.Type;
import me.mrCookieSlime.QuickSell.shop.Shop;
import me.mrCookieSlime.QuickSell.transaction.SellProfile;
import me.mrCookieSlime.QuickSell.utils.Variable;
import me.mrCookieSlime.QuickSell.utils.maths.DoubleHandler;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class SellListener implements Listener {

	/**
	 * Sells the item to the shop upon inventory close
	 * @param e InventoryCloseEvent
	 */
	@EventHandler
	public void onClose(InventoryCloseEvent e) {
		Player p = (Player) e.getPlayer();

		if (QuickSell.shop.containsKey(p.getUniqueId())) {
			List<ItemStack> items = new ArrayList<ItemStack>();
			int size = e.getInventory().getSize();
			if (QuickSell.cfg.getBoolean("options.enable-menu-line")) {
				size = size - 9;
			}

			for (int i = 0; i < size; i++) {
				items.add(e.getInventory().getContents()[i]);
			}

			Shop shop = QuickSell.shop.get(p.getUniqueId());
			QuickSell.shop.remove(p.getUniqueId());
			shop.sell(p, false, Type.SELL, items.toArray(new ItemStack[items.size()]));
		}
	}
	
	@EventHandler
	public void onClick(InventoryClickEvent e) {
		if (QuickSell.cfg.getBoolean("options.enable-menu-line") && e.getRawSlot() < e.getInventory().getSize()) {
			Player p = (Player) e.getWhoClicked();
			if (QuickSell.shop.containsKey(p.getUniqueId())) {
				Shop shop = QuickSell.shop.get(p.getUniqueId());

				if (e.getSlot() == 9 * QuickSell.cfg.getInt("options.sell-gui-rows") - 9
						|| e.getSlot() == 9 * QuickSell.cfg.getInt("options.sell-gui-rows") - 7
						|| e.getSlot() == 9 * QuickSell.cfg.getInt("options.sell-gui-rows") - 6
						|| e.getSlot() == 9 * QuickSell.cfg.getInt("options.sell-gui-rows") - 4
						|| e.getSlot() == 9 * QuickSell.cfg.getInt("options.sell-gui-rows") - 3
						|| e.getSlot() == 9 * QuickSell.cfg.getInt("options.sell-gui-rows") - 1) {
					e.setCancelled(true);
				}

				// Close GUI
				if (e.getSlot() == 9 * QuickSell.cfg.getInt("options.sell-gui-rows") - 8) {
					e.setCancelled(true);
					p.closeInventory();
				}

				// Estimate sold price
				if (e.getSlot() == 9 * QuickSell.cfg.getInt("options.sell-gui-rows") - 5) {
					e.setCancelled(true);
					double money = 0.0;

					for (int i = 0; i < e.getInventory().getSize() - 9; i++) {
						ItemStack item = e.getInventory().getContents()[i];
						if (item != null) {
							money = money + shop.getPrices().getPrice(item);
						}
					}
					
					money = DoubleHandler.fixDouble(money, 2);
					if (money > 0.0) {
						for (Booster booster: Booster.getBoosters(p.getName(), BoosterType.MONETARY)) {
							money = money + money * (booster.getMultiplier() - 1);
						}
					}

					QuickSell.local.sendMessage(p, "messages.estimate", false, new Variable("{MONEY}", String.valueOf(DoubleHandler.fixDouble(money, 2))));
				}

				// Sell Items
				if (e.getSlot() == 9 * QuickSell.cfg.getInt("options.sell-gui-rows") - 2) {
					e.setCancelled(true);
					QuickSell.shop.remove(p.getUniqueId());

					for (int i = 0; i < e.getInventory().getSize() - 9; i++) {
						ItemStack item = e.getInventory().getContents()[i];

						if (item != null) {
							if (item.getType() != Material.AIR) {
								if (fits(p.getInventory(), item)) p.getInventory().addItem(item);
								else p.getWorld().dropItemNaturally(p.getLocation(), item);
							}
						}

					}
					p.closeInventory();
				}
			}
		}
	}

	public static boolean fits(Inventory inv, ItemStack item, int... slots) {
		if (!isItemAllowed(item.getType(), inv.getType())) {
			return false;
		}

		if (slots.length == 0) {
			slots = IntStream.range(0, inv.getSize()).toArray();
		}

		for (int slot : slots) {
			ItemStack stack = inv.getItem(slot);

			if (stack == null || stack.getType() == Material.AIR) {
				return true;
			}

			if (isValidStackSize(stack, item, inv) && canStack(stack, item)) {
				return true;
			}
		}

		return false;
	}

	public static boolean canStack(ItemStack a, ItemStack b) {
		if (a == null || b == null) {
			return false;
		}

		if (a.getType() != b.getType() || a.hasItemMeta() != b.hasItemMeta()) {
			return false;
		}

		if (a.hasItemMeta()) {
			ItemMeta aMeta = a.getItemMeta();
			ItemMeta bMeta = b.getItemMeta();

			// Item Damage
			if (aMeta instanceof Damageable != bMeta instanceof Damageable) {
				return false;
			}

			if (aMeta instanceof Damageable && ((Damageable) aMeta).getDamage() != ((Damageable) bMeta).getDamage()) {
				return false;
			}

			// Leather Armor Color
			if (aMeta instanceof LeatherArmorMeta != bMeta instanceof LeatherArmorMeta) {
				return false;
			}

			if (aMeta instanceof LeatherArmorMeta && !((LeatherArmorMeta) aMeta).getColor().equals(((LeatherArmorMeta) bMeta).getColor())) {
				return false;
			}

			// Custom Model Data
			if (aMeta.hasCustomModelData() != bMeta.hasCustomModelData()) {
				return false;
			}

			if (aMeta.hasCustomModelData() && aMeta.getCustomModelData() != bMeta.getCustomModelData()) {
				return false;
			}

			// Enchantments
			if (!aMeta.getEnchants().equals(bMeta.getEnchants())) {
				return false;
			}

			// Display Name
			if (aMeta.hasDisplayName() != bMeta.hasDisplayName()) {
				return false;
			}

			if (aMeta.hasDisplayName() && !aMeta.getDisplayName().equals(bMeta.getDisplayName())) {
				return false;
			}

			// Lore
			if (aMeta.hasLore() != bMeta.hasLore()) {
				return false;
			}

			if (aMeta.hasLore()) {
				List<String> aLore = aMeta.getLore();
				List<String> bLore = bMeta.getLore();

				if (aLore.size() != bLore.size()) {
					return false;
				}

				for (int i = 0; i < aLore.size(); i++) {
					if (!aLore.get(i).equals(bLore.get(i))) {
						return false;
					}
				}
			}
		}

		return true;
	}

	public static boolean isValidStackSize(ItemStack stack, ItemStack item, Inventory inv) {
		int newStackSize = stack.getAmount() + item.getAmount();
		return newStackSize <= stack.getMaxStackSize() && newStackSize <= inv.getMaxStackSize();
	}

	public static boolean isItemAllowed(Material itemType, InventoryType inventoryType) {
		switch (inventoryType) {
			case LECTERN:
				// Lecterns only allow written books or writable books
				return itemType == Material.WRITABLE_BOOK || itemType == Material.WRITTEN_BOOK;
			case SHULKER_BOX:
				// Shulker Boxes do not allow Shulker boxes
				return itemType != Material.SHULKER_BOX && !itemType.name().endsWith("_SHULKER_BOX");
			default:
				return true;
		}
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		SellProfile.getProfile(e.getPlayer()).unregister();
	}
}
