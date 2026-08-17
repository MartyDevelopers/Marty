package martydevs.marty.processor.cpu.dithering;

import martydevs.marty.model.image.Dithering;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Set;

public final class DitheringAlgorithms {

    private static final Map<Dithering, DitheringAlgorithm> ALGORITHMS = Map.of(
            Dithering.Floyd_Steinberg,
            new FloydSteinberg()
    );

    private DitheringAlgorithms() {}

    public static @Nullable DitheringAlgorithm algorithm(Dithering dithering) {
        return ALGORITHMS.get(dithering);
    }

    public static Set<Dithering> implementedAlgorithms() {
        return ALGORITHMS.keySet();
    }

}
