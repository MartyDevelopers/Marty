package martydevs.marty.model.image;

import martydevs.marty.model.work.Work;
import org.joml.Vector2ic;
import org.jspecify.annotations.Nullable;

import java.awt.image.BufferedImage;

public interface ImageWork extends Work<MapArtImage> {

    BufferedImage image();

    BlockPalette blockPalette();

    Vector2ic outputDimensionsInMaps();

    boolean useStairCasing();

    @Nullable Dithering ditheringAlgorithm();

    Cropping cropping();

}
