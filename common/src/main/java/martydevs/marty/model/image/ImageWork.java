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

    @Nullable Dithering dithering();

    @Nullable BetterColor betterColor();

    Cropping cropping();

    boolean useTransparency();

    WaterPalette waterPalette();

    record ImageWorkImpl(
            BufferedImage image,
            BlockPalette blockPalette,
            Vector2ic outputDimensionsInMaps,
            boolean useStairCasing,
            @Nullable Dithering dithering,
            @Nullable BetterColor betterColor,
            Cropping cropping,
            boolean useTransparency,
            WaterPalette waterPalette
    ) implements ImageWork {}

}
