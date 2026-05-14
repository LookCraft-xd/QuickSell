package me.mrCookieSlime.QuickSell.listeners.custom;

import me.mrCookieSlime.QuickSell.core.events.BaseEvent;
import me.mrCookieSlime.QuickSell.utils.SellType;
import org.bukkit.entity.Player;

public class SellEvent extends BaseEvent {

    private final Player player;
    private final SellType sellType;

    private final int itemsSold;
    private final double soldAmount;

    public SellEvent(Player player, SellType sellType, int itemsSold, double soldAmount) {
        this.player = player;
        this.sellType = sellType;
        this.itemsSold = itemsSold;
        this.soldAmount = soldAmount;
    }

    public Player getPlayer() {
        return player;
    }

    public SellType getSellType() {
        return sellType;
    }

    public int getItemsSold() {
        return itemsSold;
    }

    public double getSoldAmount() {
        return soldAmount;
    }
}
