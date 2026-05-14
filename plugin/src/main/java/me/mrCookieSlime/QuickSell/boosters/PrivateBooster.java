package me.mrCookieSlime.QuickSell.boosters;

import java.util.Map;
import java.util.UUID;

public class PrivateBooster extends Booster {

    public PrivateBooster(BoosterType type, String time, double multiplier, String owner) {
        super(UUID.randomUUID().toString(), type, time, multiplier, owner);
    }

    public PrivateBooster(Map<String, Object> map) {
        super(map);
    }

    @Override
    public boolean isPrivate() {
        return true;
    }

    @Override
    public String getMessage() {
        return "messages.pbooster-use." + getType().toString();
    }
}
