package me.mrCookieSlime.QuickSell.core.utils.enums;

public enum Messages {

    RELOAD("commands.reload.done"),
    NO_ACCESS("messages.no-access"),
    NO_PERMISSION("messages.no-permission"),

    // # Menu messages
    MENU_TITLE("menu.title"),
    MENU_ACCEPT("menu.accept"),
    MENU_CANCEL("menu.cancel"),
    MENU_ESTIMATE("menu.estimate"),

    // Editor messages
    EDITOR_RENAME_SHOP("editor.rename-shop"),
    EDITOR_RENAMED_SHOP("editor.renamed-shop"),
    EDITOR_PERMISSION_SET("editor.permission-set-shop"),
    EDITOR_SET_PERMISSION("editor.set-permission-shop"),
    EDITOR_CREATE_SHOP("editor.create-shop"),

    // # Commands messages

    // Price

    PRICE_SET("commands.price-set"),

    // Shop messages
    UNKNOWN_SHOP("messages.unknown-shop"),
    SHOP_CREATED("commands.shop-created"),
    SHOP_DELETED("commands.shop-deleted"),

    // # Boosters messages
    BOOSTER_RESET("boosters.reset"),


    // # Sell messages
    SELL("messages.sell"),

    ESTIMATE("messages.estimate");

    private final String path;

    Messages(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}