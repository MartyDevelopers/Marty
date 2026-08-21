package martydevs.marty.processor.image.gpu;

import martydevs.marty.model.image.ImageProcessor;
import martydevs.marty.model.image.ImageWork;
import martydevs.marty.model.image.MapArtImage;

import java.util.concurrent.CompletableFuture;

public final class GPUImageProcessor implements ImageProcessor {

    @Override
    public CompletableFuture<MapArtImage> queue(ImageWork work) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

}
