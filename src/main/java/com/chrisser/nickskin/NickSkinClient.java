package com.chrisser.nickskin;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;

public class NickSkinClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        NickConfig.load();

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("nick")
                .then(ClientCommandManager.literal("set")
                    .then(ClientCommandManager.argument("name", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            NickConfig.nick = StringArgumentType.getString(ctx, "name");
                            NickConfig.save();
                            ctx.getSource().sendFeedback(Text.literal("Nick set to: " + NickConfig.nick));
                            return 1;
                        })))
                .then(ClientCommandManager.literal("reset").executes(ctx -> {
                    NickConfig.nick = null; NickConfig.save();
                    ctx.getSource().sendFeedback(Text.literal("Nick reset."));
                    return 1;
                })));

            dispatcher.register(ClientCommandManager.literal("skin")
                .then(ClientCommandManager.literal("set")
                    .then(ClientCommandManager.argument("player", StringArgumentType.word())
                        .executes(ctx -> {
                            NickConfig.skinName = StringArgumentType.getString(ctx, "player");
                            NickConfig.save();
                            ctx.getSource().sendFeedback(Text.literal(
                                "Skin set to " + NickConfig.skinName + "'s skin (they must be in the tab list)."));
                            return 1;
                        })))
                .then(ClientCommandManager.literal("reset").executes(ctx -> {
                    NickConfig.skinName = null; NickConfig.save();
                    ctx.getSource().sendFeedback(Text.literal("Skin reset."));
                    return 1;
                })));
        });

        // Replace your real name with your nick in death/kill messages
        ClientReceiveMessageEvents.MODIFY_GAME.register((message, overlay) -> {
            if (overlay || NickConfig.nick == null) return message;
            if (!(message.getContent() instanceof TranslatableTextContent t)) return message;
            if (!t.getKey().startsWith("death.")) return message;
            String real = MinecraftClient.getInstance().getSession().getUsername();
            String s = message.getString();
            if (!s.contains(real)) return message;
            return Text.literal(s.replace(real, NickConfig.nick));
        });
    }
}
