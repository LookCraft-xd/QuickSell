package me.mrCookieSlime.QuickSell.listeners;

import me.mrCookieSlime.QuickSell.QuickSell;
import me.mrCookieSlime.QuickSell.boosters.Booster;
import me.mrCookieSlime.QuickSell.boosters.BoosterType;
import me.mrCookieSlime.QuickSell.core.utils.message.MessageHandler;
import me.mrCookieSlime.QuickSell.manager.BoosterManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;

import java.util.List;

public class XPBoosterListener implements Listener {

    private final QuickSell plugin;
    private final BoosterManager boosterManager;
    private final MessageHandler messageHandler;

    public XPBoosterListener(QuickSell plugin) {
        this.plugin = plugin;
        this.boosterManager = plugin.getBoosterManager();
        this.messageHandler = plugin.getMessageHandler();
    }

    @EventHandler
    public void onXPGain(PlayerExpChangeEvent e) {
        Player p = e.getPlayer();
        int originalXp = e.getAmount();
        if (originalXp <= 0) return;

        double totalMultiplier = boosterManager.getTotalMultiplier(p, BoosterType.EXP);
        // Si es 1.0 (o menos), no hay boosters activos, no hacemos nada.
        if (totalMultiplier <= 1.0) return;

        double finalXp = originalXp * totalMultiplier;
        e.setAmount((int) finalXp);

        List<Booster> activeBoosters = boosterManager.getBoostersForPlayer(p.getName(), BoosterType.EXP);

        for (Booster booster : activeBoosters) {
            if (!booster.isSilent()) {
                // TODO REDUCE MESSAGE SPAM BY THROTTLING THEM
                if (!messageHandler.canSendMessage(p, "xp-msg-" + booster.getID())) return;

                double currentBonus = originalXp * (booster.getMultiplier() - 1.0);

                messageHandler.build(p, booster.getMessage())
                        .placeholder("%multiplier%", booster.getMultiplier())
                        .placeholder("%time%", boosterManager.formattedRemainingTime(booster))
                        .placeholder("%xp%", String.format("%.2f", currentBonus))
                        .send();
            }
        }
    }


    /**
     * Crea el contenido del Hover usando la lógica que ya tenías
     */
    private String getHoverContent(Booster booster) {
        StringBuilder builder = new StringBuilder("&3" + booster.getMultiplier() + "x &b" + boosterManager.getUniqueName(booster) + "\n \n");
        builder.append("&7Multiplicador: &e").append(booster.getMultiplier()).append("x\n");
        builder.append("&7Tiempo restante: &e").append(boosterManager.formattedRemainingTime(booster)).append("\n");
        builder.append("&7Global: ").append(booster.isPrivate() ? "&4&l\u2718" : "&2&l\u2714").append("\n\n&7Contribuidores:\n");

        booster.getContributors().forEach((name, mins) ->
                builder.append(" &8\u21E8 &f").append(name).append(": &a+").append(mins).append("m\n")
        );

        return builder.toString();
    }
}
