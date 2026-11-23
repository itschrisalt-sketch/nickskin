package com.chrisser.nickskin;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.util.AssetInfo;
import net.minecraft.util.Identifier;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

public class SkinFetcher {
    private static final HttpClient HTTP = HttpClient.newHttpClient();

    public static volatile String status = "";
    private static volatile AssetInfo.TextureAssetInfo body = null;
    private static volatile boolean loading = false;
    private static volatile String lastAttempt = null;
    private static Identifier currentId = null;
    private static int counter = 0;

    private static SkinTextures lastOrig, lastResult;
    private static AssetInfo.TextureAssetInfo lastBody;

    /** Called every time the game asks for your skin. */
    public static SkinTextures apply(SkinTextures orig) {
        if (NickConfig.skinName == null) return orig;
        if (body == null) {
            // lazy-load the saved skin once per session
            if (!loading && !NickConfig.skinName.equals(lastAttempt)) load(NickConfig.skinName);
            return orig;
        }
        AssetInfo.TextureAssetInfo b = body;
        if (orig == lastOrig && b == lastBody) return lastResult;
        SkinTextures result = new SkinTextures(b, orig.cape(), orig.elytra(), orig.model(), orig.secure());
        lastOrig = orig; lastBody = b; lastResult = result;
        return result;
    }

    public static void clear() {
        body = null;
        lastAttempt = null;
        status = "Skin reset.";
        MinecraftClient mc = MinecraftClient.getInstance();
        mc.execute(() -> {
            if (currentId != null) { mc.getTextureManager().destroyTexture(currentId); currentId = null; }
        });
    }

    /** input = UUID (with or without dashes), username, or NameMC profile link */
    public static void load(String input) {
        lastAttempt = input;
        loading = true;
        status = "Loading skin...";
        Thread t = new Thread(() -> {
            try {
                String uuid = resolveUuid(input);
                String skinUrl = getSkinUrl(uuid);
                HttpResponse<byte[]> r = HTTP.send(
                    HttpRequest.newBuilder(URI.create(skinUrl)).header("User-Agent", "NickSkin").build(),
                    HttpResponse.BodyHandlers.ofByteArray());
                if (r.statusCode() != 200) throw new RuntimeException("Skin download failed (" + r.statusCode() + ")");
                byte[] png = r.body();

                MinecraftClient mc = MinecraftClient.getInstance();
                mc.execute(() -> {
                    try {
                        NativeImage img = NativeImage.read(new ByteArrayInputStream(png));
                        Identifier id = Identifier.of("nickskin", "skin/" + (++counter));
                        mc.getTextureManager().registerTexture(id, new NativeImageBackedTexture(() -> "nickskin_skin", img));
                        Identifier old = currentId;
                        currentId = id;
                        body = new AssetInfo.TextureAssetInfo(id, id);
                        if (old != null) mc.getTextureManager().destroyTexture(old);
                        status = "Skin loaded!";
                    } catch (Exception e) {
                        status = "Failed: " + e.getMessage();
                    } finally {
                        loading = false;
                    }
                });
            } catch (Exception e) {
                status = "Failed: " + e.getMessage();
                loading = false;
            }
        }, "NickSkin-fetch");
        t.setDaemon(true);
        t.start();
    }

    private static String resolveUuid(String in) throws Exception {
        in = in.trim();
        if (in.contains("/")) in = in.substring(in.lastIndexOf('/') + 1);
        in = in.split("[?#]")[0];
        String compact = in.replace("-", "");
        if (compact.matches("[0-9a-fA-F]{32}")) return compact.toLowerCase();

        String name = in.replaceAll("\\.\\d+$", ""); // NameMC style "Name.1"
        JsonObject o = getJson("https://api.mojang.com/users/profiles/minecraft/" + name, "Player not found");
        return o.get("id").getAsString();
    }

    private static String getSkinUrl(String uuid) throws Exception {
        JsonObject o = getJson("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid,
            "Profile not found (or rate limited, try again in a minute)");
        JsonArray props = o.getAsJsonArray("properties");
        for (JsonElement e : props) {
            JsonObject p = e.getAsJsonObject();
            if ("textures".equals(p.get("name").getAsString())) {
                String json = new String(Base64.getDecoder().decode(p.get("value").getAsString()));
                JsonObject tex = JsonParser.parseString(json).getAsJsonObject().getAsJsonObject("textures");
                if (tex.has("SKIN")) return tex.getAsJsonObject("SKIN").get("url").getAsString();
            }
        }
        throw new RuntimeException("That player has no custom skin");
    }

    private static JsonObject getJson(String url, String notFoundMsg) throws Exception {
        HttpResponse<String> r = HTTP.send(
            HttpRequest.newBuilder(URI.create(url)).header("User-Agent", "NickSkin").build(),
            HttpResponse.BodyHandlers.ofString());
        if (r.statusCode() != 200) throw new RuntimeException(notFoundMsg + " (" + r.statusCode() + ")");
        return JsonParser.parseString(r.body()).getAsJsonObject();
    }
}
