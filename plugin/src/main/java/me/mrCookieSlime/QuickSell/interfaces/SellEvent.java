package me.mrCookieSlime.QuickSell.interfaces;

import org.bukkit.entity.Player;

public interface SellEvent {
	public enum Type {
		SELL,
		SELLALL,
		AUTOSELL, 
		UNKNOWN;
	}
	
	public void onSell(Player p, Type type, int itemsSold, double money);
}
