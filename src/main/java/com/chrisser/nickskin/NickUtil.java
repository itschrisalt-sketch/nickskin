package com.chrisser.nickskin;

import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import java.util.Optional;

public class NickUtil {
    /** Replaces the real name with the nick but keeps every colour/style of the original text. */
    public static Text replaceName(Text original, String real, String nick) {
        MutableText out = Text.empty();
        original.visit((style, str) -> {
            out.append(Text.literal(str.replace(real, nick)).setStyle(style));
            return Optional.empty();
        }, Style.EMPTY);
        return out;
    }
}
