package martydevs.marty.model.image;

import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector2i;
import org.joml.Vector2ic;

public record MapArtImage(Vector2ic dimensionsInMaps, Map[] maps) {

    public static final Vector2ic SINGLE_DIMENSIONS = new Vector2i(1, 1);

    public static MapArtImage single(Map map) {
        return new MapArtImage(SINGLE_DIMENSIONS, new Map[]{map});
    }

    public sealed interface Map permits FlatMap, StairCaseMap {}

    public record FlatMap(BlockState[] blockStates) implements Map {}

    // todo
    public record StairCaseMap() implements Map {}

}
