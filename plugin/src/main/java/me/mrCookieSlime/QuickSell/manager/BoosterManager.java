package me.mrCookieSlime.QuickSell.manager;

import dev.dejvokep.boostedyaml.YamlDocument;
import me.mrCookieSlime.QuickSell.QuickSell;
import me.mrCookieSlime.QuickSell.boosters.Booster;
import me.mrCookieSlime.QuickSell.boosters.BoosterType;
import me.mrCookieSlime.QuickSell.boosters.PrivateBooster;
import me.mrCookieSlime.QuickSell.core.utils.message.MessageBuilder;
import me.mrCookieSlime.QuickSell.core.utils.time.TimeUtils;
import me.mrCookieSlime.QuickSell.utils.DoubleHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class BoosterManager {

    private final QuickSell plugin;
    private final File storageFolder;
    private final List<Booster> activeBoosters = new ArrayList<>();
    private final YamlDocument config;

    public BoosterManager(QuickSell plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        this.storageFolder = new File(plugin.getDataFolder(), "data-storage/boosters");
        if (!storageFolder.exists()) storageFolder.mkdirs();

        runScheduler();
    }

    // --- CRUD & PERSISTENCIA ---

    /**
     * Carga todos los boosters desde la carpeta storage.
     */
    public void loadBoosters() {
        activeBoosters.clear();
        File[] files = storageFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null || files.length == 0) return;

        for (File file : files) {

//            if (file.length() <= 3) {
//                if (!file.delete()) {
//                    file.deleteOnExit();
//                }
//                continue;
//            }

            try {
                YamlDocument config = YamlDocument.create(file);
                String id = file.getName().replace(".yml", "");
                plugin.getLogger().warning("Intentando cargar booster: " + id);

                Booster booster = deserialize(config.getStringRouteMappedValues(false));

                if (booster.hasExpired()) {
                    plugin.getLogger().info("Eliminando booster expirado: " + file.getName());
                    if (!file.delete()) {
                        file.deleteOnExit();
                    }
                    continue;
                }

                activeBoosters.add(booster);
            } catch (Exception e) {
                plugin.getLogger().warning("Error cargando booster: " + file.getName());
                e.printStackTrace();
            }
        }

        plugin.getLogger().info("Se han cargado correctamente " + activeBoosters.size() + " boosters.");
    }

    public void saveBoosters() {
        if (activeBoosters.isEmpty()) return;
        activeBoosters.forEach(this::saveToYaml);
    }

    /**
     * Activa un booster: decide si extiende uno existente o crea uno nuevo.
     */
    public void activateBooster(Booster booster) {
        activeBoosters.add(booster);
        saveToYaml(booster);
        notifyActivation(booster);
    }

    private void attemptExtend(Booster booster) {
        // Option is disabled
        if (!plugin.getConfiguration().getBoolean("boosters.extension-mode")) return;
        // Booster already have been extended
        if (booster.isExtend()) return;

        // Should have check if booster is private and same owner if actually needed.
        Bukkit.broadcastMessage("Extending " + booster.getType() + " booster.");
        Bukkit.broadcastMessage(booster.getDuration() + " by " + booster.getOwner());

        // Filters same type and multiplier.
        Optional<Booster> existing = activeBoosters.stream()
                .filter(b -> b.getType() == booster.getType())
                .filter(b -> Double.compare(b.getMultiplier(), booster.getMultiplier()) == 0)
                .findFirst();

        // Should run if else action?
        existing.ifPresent(chosen -> extendBooster(chosen, booster));
    }

    // Kinda dont like this
    private void extendBooster(Booster active, Booster extra) {
        long extraMillis = TimeUtils.parseTime(extra.getDuration());
        active.setExpirationMillis(active.getRemainingMillis() + extraMillis);
        active.getContributors().putAll(extra.getContributors());
        notifyExtension(active);
    }

    public void deactivateBooster(Booster booster) {
        deleteFile(booster);

        notifyDeactivation(booster);
        activeBoosters.remove(booster);
    }

    private void deleteFile(Booster booster) {
        File file = new File(storageFolder, booster.getID() + ".yml");
        if (file.exists()) file.delete();
    }

    // --- MULTIPLIERS METHODS ---

    public double getTotalMultiplier(Player player, BoosterType type) {
        double multiplier = 1.0;
        for (Booster b : getBoostersForPlayer(player.getName(), type)) {
            multiplier *= b.getMultiplier();
        }
        return DoubleHandler.fixDouble(multiplier, 2);
    }

    public List<Booster> getBoostersForPlayer(String playerName) {
        return activeBoosters.stream()
                .filter(b -> b.isAppliedTo(playerName))
                .collect(Collectors.toList());
    }

    public List<Booster> getBoostersForPlayer(String playerName, BoosterType type) {
        return activeBoosters.stream()
                .filter(b -> b.getType() == type)
                .filter(b -> b.isAppliedTo(playerName))
                .collect(Collectors.toList());
    }

    public List<Booster> getActiveBoosters() {
        return activeBoosters;
    }

    // --- UTILIDADES DE ARCHIVO & DESERIALIZER ---

    private void saveToYaml(Booster b) {
        File file = new File(storageFolder, b.getID() + ".yml");
        try {
            YamlDocument document = YamlDocument.create(file);
            b.serialize().forEach(document::set);
            document.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Booster deserialize(Map<String, Object> map) {
        boolean isPrivate = (boolean) map.getOrDefault("private", false);
        return isPrivate ? new PrivateBooster(map) : new Booster(map);
    }

    // --- TAREAS Y NOTIFICACIONES ---

    private void runUpdateCheck() {
        Iterator<Booster> it = activeBoosters.iterator();
        while (it.hasNext()) {
            Booster booster = it.next();
            if (booster.hasExpired()) {
                deleteFile(booster);
                notifyDeactivation(booster);
                it.remove();
            }
        }
    }

    private void runScheduler() {
        int ticks = plugin.getConfiguration().getInt("boosters.refresh-every", 30) * 20;
        Bukkit.getScheduler().runTaskTimer(plugin, this::runUpdateCheck, 20L, ticks);
    }

    private void notifyActivation(Booster booster) {
        if (booster.isSilent()) return;
        String path = (booster instanceof PrivateBooster) ? "pbooster.activate." : "booster.activate.";
        sendBoosterMessage(booster, path, true);
    }

    private void notifyExtension(Booster booster) {
        if (booster.isSilent()) return;
        String path = (booster instanceof PrivateBooster) ? "pbooster.extended." : "booster.extended.";
        sendBoosterMessage(booster, path, true);
    }

    private void notifyDeactivation(Booster booster) {
        if (booster.isSilent()) return;
        String path = (booster instanceof PrivateBooster) ? "pbooster.deactivate." : "booster.deactivate.";
        sendBoosterMessage(booster, path, false);
    }

    private void sendBoosterMessage(Booster booster, String path, boolean broadcastIfGlobal) {
        MessageBuilder builder = QuickSell.getInstance().getMessageHandler().build(path + booster.getType())
                .placeholder("%player%", booster.getOwner())
                .placeholder("%time%", booster.getDuration())
                .placeholder("%multiplier%", booster.getMultiplier().toString());

        if (booster instanceof PrivateBooster) {
            Player p = Bukkit.getPlayer(booster.getOwner());
            if (p != null) builder.to(p).send();
            return;
        }

        if (broadcastIfGlobal) builder.broadcast();
    }

    // Thinking about it

    /**
     * Get the boosters readable name
     *
     * @return String
     */
    public String getUniqueName(Booster booster) {
        switch (booster.getType()) {
            case EXP:
                return "Booster (Experience)";
            case MONETARY:
                return "Booster (Money)";
            default:
                return "Booster";
        }
    }

    /**
     * Gets a list of players the booster is applying to
     *
     * @return List<String>
     */
    public List<String> getAppliedPlayers() {
        List<String> players = new ArrayList<String>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            players.add(p.getName());
        }
        return players;
    }

    /**
     * Format the remaining time on the booster
     *
     * @return Formatted remaining time.
     */
    public String formattedRemainingTime(Booster booster) {
        boolean shorter = config.getBoolean("some.value.shorter", false);
        return shorter ? TimeUtils.millisToHumanReadableShort(booster.getRemainingMillis()) : TimeUtils.millisToHumanReadable(booster.getRemainingMillis());
    }

}
