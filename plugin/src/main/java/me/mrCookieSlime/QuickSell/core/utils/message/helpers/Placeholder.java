package me.mrCookieSlime.QuickSell.core.utils.message.helpers;

public class Placeholder {

    private final String replaced;
    private final String replacement;

    public Placeholder(final String replaced, final String replacement) {
        this.replaced = replaced;
        this.replacement = replacement;
    }

    public Placeholder(final String replaced, final Number amount) {
        this.replaced = replaced;
        this.replacement = String.valueOf(amount);
    }

    public String getReplaced() {
        return replaced;
    }

    public String getReplacement() {
        return replacement;
    }

    public String apply(String string) {
        return string.replace(this.replaced, this.replacement);
    }
}
