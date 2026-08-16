package martydevs.marty.processor;

import martydevs.marty.model.image.Cropping;
import martydevs.marty.model.image.ImageProcessor;
import martydevs.marty.model.image.ImageWork;
import martydevs.marty.model.image.MapArtImage;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.awt.image.BufferedImage;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class CPUImageProcessor implements ImageProcessor {

    private final Executor executor;

    public CPUImageProcessor(Executor executor) {
        this.executor = executor;
    }

    @Override
    public CompletableFuture<MapArtImage> queue(ImageWork work) {
        return CompletableFuture.supplyAsync(() -> doWork(work), executor);
    }

    @ApiStatus.Internal
    public static CroppedView createCenterCroppedView(BufferedImage image, Vector2ic targetBounds) {
        Vector2ic sourceBounds = new Vector2i(image.getWidth(), image.getHeight());

        float imageAspect = (float) sourceBounds.x() / sourceBounds.y();
        float targetAspect = (float) targetBounds.x() / targetBounds.y();

        int cropWidth, cropHeight;
        int cropX, cropY;

        if (imageAspect > targetAspect) {
            cropHeight = sourceBounds.y();
            cropWidth = Math.round(sourceBounds.y() * targetAspect);
            cropX = (sourceBounds.x() - cropWidth) / 2;
            cropY = 0;
        }
        else {
            cropWidth = sourceBounds.x();
            cropHeight = Math.round(sourceBounds.x() / targetAspect);
            cropX = 0;
            cropY = (sourceBounds.y() - cropHeight) / 2;
        }

        float scaleX = (float) targetBounds.x() / cropWidth;
        float scaleY = (float) targetBounds.y() / cropHeight;
        float scale = Math.min(scaleX, scaleY);

        return new CroppedView(
                image,
                new Vector2i(cropX, cropY),
                new Vector2i(cropX + cropWidth, cropY + cropHeight),
                scale
        );
    }

    private static MapArtImage doWork(ImageWork work) {
        // Size calculations.
        BufferedImage image = work.image();

        Vector2ic outputDimensionsInMaps = work.outputDimensionsInMaps();

        Vector2i targetBounds = new Vector2i(outputDimensionsInMaps).mul(128);
        Cropping cropping = work.cropping();

        CroppedView croppedView = switch (cropping) {
            case Cropping.Center center -> createCenterCroppedView(image, targetBounds);
            case Cropping.Manual manual -> {
                yield null;
            }
        };

        return null;
    }

    public static class CroppedView {

        final BufferedImage image;
        final Vector2ic minimum, maximum;
        final float scale;
        public final Vector2ic bounds;

        CroppedView(BufferedImage image, Vector2ic minimum, Vector2ic maximum, float scale) {
            this.image = image;
            this.minimum = minimum;
            this.maximum = maximum;
            this.scale = scale;
            this.bounds = new Vector2i(maximum).sub(minimum);
        }

        public int getRGB(int x, int y) {
            int scaledX = Math.round(x / scale);
            int scaledY = Math.round(y / scale);
            if(scaledX > bounds.x() || scaledY > bounds.y()) throw new ArrayIndexOutOfBoundsException();
            return image.getRGB(minimum.x() + scaledX, minimum.y() + scaledY);
        }

    }

}
