package me.mrCookieSlime.QuickSell.shop;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.*;

public class Shop {

    private final String id;
    private final int priority;

    private String name;
    private PriceInfo prices;

    private Material material = Material.STONE;
    private String permission = null;
    private List<String> inheritance = new ArrayList<>();

    public Shop(String id) {
        this(id, 1);
    }

    public Shop(String id, int priority) {
        this(id, priority, Material.STONE);
    }

    public Shop(String id, int priority, Material material) {
        this(id, id, priority, material, "quickshop.shop." + id.toLowerCase(), Collections.emptyList(), new PriceInfo());
    }

    public Shop(String id, String name, int priority, Material material, String permission, List<String> inheritance, PriceInfo prices) {
        this.id = id.toLowerCase();
        this.name = name;
        this.priority = priority;
        this.material = material;
        this.permission = permission;
        this.inheritance = inheritance;
        this.prices = prices;
    }

    public Shop(Map<String, Object> map) {
        this.id = (String) map.get("id");
        this.name = (String) map.get("name");
        this.priority = (int) map.get("priority");
        this.material = Material.matchMaterial((String) map.get("material"));
        this.permission = (String) map.get("permission");
        this.inheritance = (List<String>) map.get("inheritance");
        this.prices = new PriceInfo((Section) map.get("price"));
    }

    public Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", this.name);
        map.put("priority", this.priority);
        map.put("material", this.material.name());
        map.put("permission", this.permission);

        if (!inheritance.isEmpty()) {
            map.put("inheritance", this.inheritance);
        }

        map.put("price", prices.serialize());

        return map;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPriority() {
        return priority;
    }

    public PriceInfo getPrices() {
        return prices;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public List<String> getInheritance() {
        return inheritance;
    }

    public boolean hasUnlocked(Player p) {
        if (permission == null || permission.isEmpty()) {
            return true;
        }
        return p.hasPermission(permission);
    }
}
