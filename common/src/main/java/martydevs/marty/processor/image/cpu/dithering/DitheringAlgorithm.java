package martydevs.marty.processor.image.cpu.dithering;

import martydevs.marty.annotation.ThreadSafe;
import martydevs.marty.processor.image.cpu.CroppedView;
import martydevs.marty.processor.image.cpu.PaletteEntry;

@FunctionalInterface
public interface DitheringAlgorithm {

    /**
     * Dithers {@link CroppedView} using provided palette.
     * @param croppedView image view to read colors from
     * @param palette rgb values allowed to use
     * @param out byte array to write indices of colors from {@code palette}
     */
    @ThreadSafe
    DitheringResult dither(CroppedView croppedView, boolean paletteIncludesWater, PaletteEntry[] palette, int[][] out);

}
