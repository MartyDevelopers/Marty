package martydevs.marty.processor.image.cpu.color;

import martydevs.marty.helper.map.PaletteEntry;

public interface NearestColorAlgorithm {

    /**
     * Finds the most similar color in palette
     * @param r red component of the color to search
     * @param g green component of the color to search
     * @param b blue component of the color to search
     * @param palette palette of the colors for search
     * @return nearest color index in the palette
     */
    int findNearestColor(int r, int g, int b, PaletteEntry[] palette);

}
