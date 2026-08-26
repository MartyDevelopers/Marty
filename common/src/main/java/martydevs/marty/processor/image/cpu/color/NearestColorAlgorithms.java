package martydevs.marty.processor.image.cpu.color;

import martydevs.marty.helper.map.PaletteEntry;
import martydevs.marty.model.image.BetterColor;
import martydevs.marty.util.ColorUtil;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Set;

public final class NearestColorAlgorithms {

    private static final Map<BetterColor, NearestColorAlgorithm> ALGORITHMS = Map.of(
            BetterColor.CIE76_D65, NearestColorAlgorithms::CIE76_D65,
            BetterColor.CIE76_D50, NearestColorAlgorithms::CIE76_D50
    );

    private NearestColorAlgorithms() {}

    private static int CIE76_D65(int red, int green, int blue, PaletteEntry[] palette) {
        return CIE76(red, green, blue, palette, BetterColor.Tristimulus.D65);
    }

    private static int CIE76_D50(int red, int green, int blue, PaletteEntry[] palette) {
        return CIE76(red, green, blue, palette, BetterColor.Tristimulus.D50);
    }

    private static int CIE76(int red, int green, int blue, PaletteEntry[] palette, BetterColor.Tristimulus tristimulus) {
        if (palette == null || palette.length == 0) return -1;

        float[] lab = ColorUtil.rgbToLab(red, green, blue, tristimulus.xyz);
        double l = lab[0];
        double a = lab[1];
        double b = lab[2];

        int nearestIndex = 0;
        double minDistanceSquared = Double.POSITIVE_INFINITY;

        for (int i = 0; i < palette.length; i++) {
            float[] lab0 = palette[i].labRepresentations()[tristimulus.ordinal()];
            double dL = l - lab0[0];
            double dA = a - lab0[1];
            double dB = b - lab0[2];

            double distanceSquared = (dL * dL) + (dA * dA) + (dB * dB);
            if (distanceSquared < minDistanceSquared) {
                minDistanceSquared = distanceSquared;
                nearestIndex = i;
            }
        }

        return nearestIndex;
    }

    public static @Nullable NearestColorAlgorithm algorithm(BetterColor betterColor) {
        return ALGORITHMS.get(betterColor);
    }

    public static Set<BetterColor> implementedAlgorithms() {
        return ALGORITHMS.keySet();
    }


}
