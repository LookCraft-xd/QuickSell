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

    // Editor Chat Messages
    EDITOR_RENAME_SHOP("editor.rename-shop"),
    EDITOR_RENAMED_SHOP("editor.renamed-shop"),
    EDITOR_PERMISSION_SET("editor.permission-set-shop"),
    EDITOR_SET_PERMISSION("editor.set-permission-shop"),
    EDITOR_CREATE_SHOP("editor.create-shop"),
    EDITOR_ITEM_ADDED("editor.item-added-success"),
    EDITOR_NO_ITEM_HAND("editor.no-item-in-hand"),

    // GUI Editor Principal
    GUI_EDITOR_TITLE("gui.editor.title"),
    GUI_EDITOR_NEW_SHOP("gui.editor.new-shop"),
    GUI_EDITOR_NEW_SHOP_LORE("gui.editor.new-shop-lore"),
    GUI_EDITOR_SHOP_LORE("gui.editor.shop-lore"),

    // GUI Settings (Individual Shop)
    GUI_SETTINGS_TITLE("gui.settings.title"),
    GUI_SETTINGS_NAME("gui.settings.name"),
    GUI_SETTINGS_NAME_LORE("gui.settings.name-lore"),
    GUI_SETTINGS_ITEM("gui.settings.display-item"),
    GUI_SETTINGS_ITEM_LORE("gui.settings.display-item-lore"),
    GUI_SETTINGS_PERMISSION("gui.settings.permission"),
    GUI_SETTINGS_PERMISSION_LORE("gui.settings.permission-lore"),
    GUI_SETTINGS_PERM_NONE("gui.settings.permission-none"),
    GUI_SETTINGS_INHERITANCE("gui.settings.inheritance"),
    GUI_SETTINGS_BACK("gui.settings.back"),

    // GUI Content Editor
    GUI_CONTENT_TITLE("gui.content.title"),
    GUI_CONTENT_EDIT_LORE("gui.content.edit-lore"),
    GUI_CONTENT_ADD("gui.content.add-item"),
    GUI_CONTENT_BACK("gui.content.back"),

    // GUI Price Editor
    GUI_PRICE_TITLE("gui.price-editor.title"),
    GUI_PRICE_CURRENT("gui.price-editor.current-price"),
    GUI_PRICE_ADJUST("gui.price-editor.adjustment"),
    GUI_PRICE_SAVE("gui.price-editor.save"),
    GUI_PRICE_CANCEL("gui.price-editor.cancel"),

    // GUI Inheritance
    GUI_INHERIT_TITLE("gui.inheritance.title"),
    GUI_INHERIT_STATUS("gui.inheritance.status"),
    GUI_INHERIT_ON("gui.inheritance.enabled"),
    GUI_INHERIT_OFF("gui.inheritance.disabled"),

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