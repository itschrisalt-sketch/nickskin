package com.chrisser.nickskin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.text.Text;
import java.util.List;

public class NickScreen extends Screen {
    private final Screen parent;
    private TextFieldWidget nickField;
    private TextFieldWidget skinField;
    private int pick = -1;
    private Text status = Text.empty();

    public NickScreen(Screen parent) {
        super(Text.literal("NickSkin"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int y = this.height / 2 - 70;

        nickField = new TextFieldWidget(this.textRenderer, cx - 100, y, 200, 20, Text.literal("Nick"));
        nickField.setMaxLength(32);
        nickField.setPlaceholder(Text.literal("Custom nick"));
        nickField.setText(NickConfig.nick == null ? "" : NickConfig.nick);
        this.addDrawableChild(nickField);

        skinField = new TextFieldWidget(this.textRenderer, cx - 100, y + 45, 200, 20, Text.literal("Skin"));
        skinField.setMaxLength(32);
        skinField.setPlaceholder(Text.literal("Online player name"));
        skinField.setText(NickConfig.skinName == null ? "" : NickConfig.skinName);
        this.addDrawableChild(skinField);

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Pick next online player"), b -> cycle())
            .dimensions(cx - 100, y + 70, 200, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Save"), b -> save())
            .dimensions(cx - 100, y + 100, 98, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Reset"), b -> reset())
            .dimensions(cx + 2, y + 100, 98, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> close())
            .dimensions(cx - 100, y + 130, 200, 20).build());
    }

    private void cycle() {
        ClientPlayNetworkHandler h = this.client.getNetworkHandler();
        if (h == null) { status = Text.literal("Join a server/world first."); return; }
        List<String> names = h.getPlayerList().stream().map(e -> e.getProfile().name()).sorted().toList();
        if (names.isEmpty()) return;
        pick = (pick + 1) % names.size();
        skinField.setText(names.get(pick));
    }

    private void save() {
        String n = nickField.getText().trim();
        String s = skinField.getText().trim();
        NickConfig.nick = n.isEmpty() ? null : n;
        NickConfig.skinName = s.isEmpty() ? null : s;
        NickConfig.save();
        status = Text.literal("Saved!");
    }

    private void reset() {
        NickConfig.nick = null;
        NickConfig.skinName = null;
        NickConfig.save();
        nickField.setText("");
        skinField.setText("");
        status = Text.literal("Reset to your real name and skin.");
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        int cx = this.width / 2;
        ctx.drawCenteredTextWithShadow(this.textRenderer, this.title, cx, 20, 0xFFFFFFFF);
        ctx.drawTextWithShadow(this.textRenderer, Text.literal("Nickname (head, tab list, kill messages)"),
            cx - 100, nickField.getY() - 12, 0xFFAAAAAA);
        ctx.drawTextWithShadow(this.textRenderer, Text.literal("Skin (copies an online player's skin)"),
            cx - 100, skinField.getY() - 12, 0xFFAAAAAA);
        ctx.drawCenteredTextWithShadow(this.textRenderer, status, cx, this.height / 2 + 85, 0xFF55FF55);
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }
}
