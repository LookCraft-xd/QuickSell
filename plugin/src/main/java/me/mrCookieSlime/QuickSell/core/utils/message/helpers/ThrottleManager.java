package me.mrCookieSlime.QuickSell.core.utils.message.helpers;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ThrottleManager {

    private final Map<UUID, Map<String, Long>> cooldowns = new ConcurrentHashMap<>();
    private final long defaultCooldown;

    public ThrottleManager(long defaultCooldownMillis) {
        this.defaultCooldown = defaultCooldownMillis;
    }

    /**
     * Verifica si un jugador puede realizar una acción basada en una clave.
     * Si puede, actualiza el timestamp automáticamente.
     *
     * @param uuid El UUID del jugador.
     * @param key  La clave única de la acción (ej: "xp-booster-msg").
     * @return true si el cooldown ha pasado, false de lo contrario.
     */
    public boolean canProceed(UUID uuid, String key) {
        return canProceed(uuid, key, this.defaultCooldown);
    }

    public boolean canProceed(UUID uuid, String key, long customCooldown) {
        long now = System.currentTimeMillis();

        Map<String, Long> playerMessages = cooldowns.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>());
        long lastSent = playerMessages.getOrDefault(key, 0L);

        if (now - lastSent >= customCooldown) {
            playerMessages.put(key, now);
            return true;
        }

        return false;
    }
    
    public void clearPlayer(UUID uuid) {
        cooldowns.remove(uuid);
    }
}