package martydevs.marty.processor.image.cpu.dithering;

import martydevs.marty.helper.map.PaletteEntry;

public final class DitheringUtil {

    private DitheringUtil() {}

    public static int clamp(int value) {
        return Math.clamp(value, 0, 255);
    }

    /**
     * Finds the most similar color in palette
     * @param r red component of the color to search
     * @param g green component of the color to search
     * @param b blue component of the color to search
     * @param palette palette of the colors for search
     * @return nearest color index
     */
    public static int findNearestColor(int r, int g, int b, PaletteEntry[] palette) {
        int nearestIndex = 0;
        int minDistance = Integer.MAX_VALUE;

        for (int i = 0; i < palette.length; i++) {
            int rgb = palette[i].rgb();
            int pr = (rgb >> 16) & 0xFF;
            int pg = (rgb >> 8) & 0xFF;
            int pb = rgb & 0xFF;

            int dr = r - pr;
            int dg = g - pg;
            int db = b - pb;
            int distance = dr * dr + dg * dg + db * db;

            if (distance < minDistance) {
                minDistance = distance;
                nearestIndex = i;
            }
        }

        return nearestIndex;
    }

}
