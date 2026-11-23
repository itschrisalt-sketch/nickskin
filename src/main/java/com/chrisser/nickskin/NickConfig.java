package com.chrisser.nickskin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.*;

public class NickConfig {
    public static String nick = null;      // shown instead of your real name in kill messages
    public static String skinName = null;  // name of an online player whose skin you copy

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("nickskin.json");

    private static class Data { String nick; String skinName; }

    public static void load() {
        try {
            if (Files.exists(FILE)) {
                Data d = GSON.fromJson(Files.readString(FILE), Data.class);
                if (d != null) { nick = d.nick; skinName = d.skinName; }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void save() {
        try {
            Data d = new Data(); d.nick = nick; d.skinName = skinName;
            Files.writeString(FILE, GSON.toJson(d));
        } catch (Exception e) { e.printStackTrace(); }
    }
}
