package me.kristal.utils;

import me.kristal.Kristal;

import java.text.DecimalFormat;

public class KristalFormatter {

    private static final DecimalFormat df = new DecimalFormat("#,###");

    public static String format(long amount) {
        return df.format(amount).replace(",", ".");
    }

    public static String formatShort(long amount) {
        if (amount >= 1_000_000_000L) {
            return formatValue(amount, 1_000_000_000L, Kristal.getInstance().getConfig().getString("settings.format.billion", "mr"));
        }
        if (amount >= 1_000_000L) {
            return formatValue(amount, 1_000_000L, Kristal.getInstance().getConfig().getString("settings.format.million", "m"));
        }
        if (amount >= 1_000L) {
            return formatValue(amount, 1_000L, Kristal.getInstance().getConfig().getString("settings.format.thousand", "k"));
        }
        return String.valueOf(amount);
    }

    private static String formatValue(long amount, long divisor, String unit) {
        double value = (double) amount / divisor;
        if (value == (long) value) {
            return String.format("%d%s", (long) value, unit);
        }
        return String.format("%.1f%s", value, unit).replace(",", ".");
    }
}
