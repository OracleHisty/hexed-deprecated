package net.backupcup.hexed.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.regex.Pattern;

public final class TextWrapUtils {
    private TextWrapUtils() {}

    private static Pattern pattern(int screenWidth) {
        return Pattern.compile(String.format("(\\S.{1,%d})(?:\\s+|$)", Width.lineWidth(screenWidth)));
    }

    public static List<Component> wrapText(int screenWidth, String translationKey, ChatFormatting... formatting) {
        return wrapText(screenWidth, Component.translatable(translationKey), formatting);
    }

    public static List<Component> wrapText(int screenWidth, Component text, ChatFormatting... formatting) {
        return pattern(screenWidth).matcher(text.getString()).results()
            .<Component>map(result -> Component.literal(result.group(1)).withStyle(formatting))
            .toList();
    }

    private enum Width {
        SMALL(320, 20),
        NORMAL(450, 35),
        WIDE(590, 50),
        ULTRAWIDE(750, 65);

        private final int screen;
        private final int line;

        Width(int screen, int line) {
            this.screen = screen;
            this.line = line;
        }

        static int lineWidth(int screenWidth) {
            int line = SMALL.line;
            for (Width width : values()) {
                if (width.screen <= screenWidth) line = width.line;
            }
            return line;
        }
    }
}
