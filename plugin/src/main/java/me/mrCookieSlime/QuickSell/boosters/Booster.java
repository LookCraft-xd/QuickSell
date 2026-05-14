package me.mrCookieSlime.QuickSell.boosters;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import me.mrCookieSlime.QuickSell.core.utils.time.TimeUtils;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class Booster {

    // Booster ID and type.
    private final String id;
    private final BoosterType type;

    // Original minutes and multiplier value.
    private final String time;
    private final double multiplier;

    // Booster owner and contributors
    private final String owner;
    private final Map<String, Integer> contributors = new HashMap<>();

    // Modifiable options.
    boolean silent = false;
    boolean extend = false;
    private long expirationMillis;

    public Booster(BoosterType type, String time, double multiplier) {
        this(UUID.randomUUID().toString(), type, time, multiplier);
    }

    public Booster(String id, BoosterType type, String time, double multiplier) {
        this(id, type, time, multiplier, "INTERNAL");
    }

    public Booster(String id, BoosterType type, String time, double multiplier, String owner) {
        this.id = id;
        this.type = type;
        this.time = time;
        this.multiplier = multiplier;
        this.owner = owner;
        this.expirationMillis = System.currentTimeMillis() + TimeUtils.parseTime(time);
    }

    public Booster(Map<String, Object> map) {
        this.id = (String) map.get("id");
        this.type = BoosterType.valueOf(map.get("type").toString());
        this.time = (String) map.get("time");
        this.multiplier = ((Number) map.get("multiplier")).doubleValue();
        this.owner = (String) map.get("owner");
        this.expirationMillis = ((Number) map.get("expirationMillis")).longValue();

        if (map.containsKey("contributors") && map.get("contributors") instanceof Section section) {
            if (section.isEmpty(false)) return;
            for (String key : section.getRoutesAsStrings(false)) {
                this.contributors.put(key.toUpperCase(), section.getInt(key));
            }
        }
    }

    public @NonNull Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();

        map.put("id", id);
        map.put("type", type.name());
        map.put("time", time);
        map.put("multiplier", multiplier);
        map.put("owner", owner);
        map.put("expirationMillis", expirationMillis);
        map.put("private", isPrivate());
        if (!contributors.isEmpty()) {
            map.put("contributors", contributors);
        }

        return map;
    }

    public boolean isAppliedTo(String playerName) {
        if (this.isPrivate()) {
            return this.owner.equalsIgnoreCase(playerName);
        }
        return true;
    }

    /**
     * Gets the owner of the booster
     *
     * @return String
     */
    public String getOwner() {
        return owner;
    }

    /**
     * Gets the multiplier amount
     *
     * @return Double
     */
    public Double getMultiplier() {
        return multiplier;
    }

    /**
     * Gets the duration of the multiplier
     *
     */
    public String getDuration() {
        return time;
    }

    public void setExpirationMillis(long millis) {
        this.expirationMillis = millis;
    }

    /**
     * Calcula cuánto tiempo queda en milisegundos.
     * Si es negativo, el booster ya expiró.
     */
    public long getRemainingMillis() {
        return expirationMillis - System.currentTimeMillis();
    }

    /**
     * Verifica si el booster ha caducado.
     */
    public boolean hasExpired() {
        return getRemainingMillis() <= 0;
    }

    /**
     * Get the boosters ID
     *
     * @return int
     */
    public String getID() {
        return id;
    }

    /**
     * Gets the booster use message
     *
     * @return String
     */
    public String getMessage() {
        return "messages.booster-use." + type.toString();
    }

    /**
     * Gets the booster type
     *
     * @return BoosterType
     */
    public BoosterType getType() {
        return this.type;
    }

    /**
     * Gets if the booster is silent
     *
     * @return Boolean
     */
    public boolean isSilent() {
        return silent;
    }

    public void setSilent(boolean silent) {
        this.silent = silent;
    }

    public boolean isExtend() {
        return extend;
    }

    public void setExtend(boolean extend) {
        this.extend = extend;
    }

    /**
     * Get if the booster is a private booster
     *
     * @return Boolean
     */
    public boolean isPrivate() {
        return this instanceof PrivateBooster;
    }

    /**
     * Get a list of players who contributed to the booster
     *
     * @return Map<String, Integer>
     */
    public Map<String, Integer> getContributors() {
        return this.contributors;
    }

    public void setContributors(Map<String, Integer> contributors) {
        this.contributors.putAll(contributors);
    }

    public void addContributor(String playerName, int minutes) {
        this.contributors.merge(playerName, minutes, Integer::sum);
    }
}
