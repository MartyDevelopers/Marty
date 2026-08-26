package martydevs.marty.processor.image.cpu.dithering;

import martydevs.marty.processor.image.cpu.CroppedView;
import martydevs.marty.helper.map.PaletteEntry;
import martydevs.marty.processor.image.cpu.color.NearestColorAlgorithm;

@FunctionalInterface
public interface DitheringAlgorithm {

    /**
     * Dithers {@link CroppedView} using provided palette.
     * @param croppedView image view to read colors from
     * @param palette rgb values allowed to use
     * @param out byte array to write indices of colors from {@code palette}
     */
    DitheringResult dither(CroppedView croppedView, NearestColorAlgorithm nearestColorAlgorithm, boolean paletteIncludesWater, PaletteEntry[] palette, int[][] out);

}
