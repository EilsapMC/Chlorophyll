package me.mrhua269.chlorophyll.utils;

import net.minecraft.network.chat.TextColor;

public class TextColorUtils {
    public static int colorOrException(String hexColor) {
        return TextColor.parseColor(hexColor)
                .getOrThrow((unused) -> new IllegalArgumentException("Invalid color: " + hexColor))
                .getValue();
    }
}
