package com.chrisser.nickskin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class NickScreen extends Screen {
    private final Screen parent;
    private TextFieldWidget nickField;
    private TextFieldWidget skinField;

    public NickScreen(Screen parent) {
        super(Text.literal("NickSkin"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int y = this.height / 2 - 60;

        nickField = new TextFieldWidget(this.textRenderer, cx - 120, y, 240, 20, Text.literal("Nick"));
        nickField.setMaxLength(32);
        nickField.setPlaceholder(Text.literal("Custom nick"));
        nickField.setText(NickConfig.nick == null ? "" : NickConfig.nick);
        this.addDrawableChild(nickField);

        skinField = new TextFieldWidget(this.textRenderer, cx - 120, y + 45, 240, 20, Text.literal("Skin"));
        skinField.setMaxLength(120);
        skinField.setPlaceholder(Text.literal("UUID / username / NameMC link"));
        skinField.setText(NickConfig.skinName == null ? "" : NickConfig.skinName);
        this.addDrawableChild(skinField);

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Save"), b -> save())
            .dimensions(cx - 120, y + 75, 118, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Reset"), b -> reset())
            .dimensions(cx + 2, y + 75, 118, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> close())
            .dimensions(cx - 120, y + 105, 240, 20).build());
    }

    private void save() {
        String n = nickField.getText().trim();
        String s = skinField.getText().trim();
        NickConfig.nick = n.isEmpty() ? null : n;
        if (s.isEmpty()) {
            NickConfig.skinName = null;
            SkinFetcher.clear();
        } else {
            NickConfig.skinName = s;
            SkinFetcher.load(s);
        }
        NickConfig.save();
    }

    private void reset() {
        NickConfig.nick = null;
        NickConfig.skinName = null;
        NickConfig.save();
        SkinFetcher.clear();
        nickField.setText("");
        skinField.setText("");
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        int cx = this.width / 2;
        ctx.drawCenteredTextWithShadow(this.textRenderer, this.title, cx, 20, 0xFFFFFFFF);
        ctx.drawTextWithShadow(this.textRenderer, Text.literal("Nickname (head, tab list, kill messages)"),
            cx - 120, nickField.getY() - 12, 0xFFAAAAAA);
        ctx.drawTextWithShadow(this.textRenderer, Text.literal("Skin (paste NameMC UUID, username or profile link)"),
            cx - 120, skinField.getY() - 12, 0xFFAAAAAA);
        ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal(SkinFetcher.status),
            cx, this.height / 2 + 65, 0xFF55FF55);
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }
}
