package martydevs.marty.model.image;

import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector2ic;

public record MapArtImage(Vector2ic dimensionsInMaps, Map[][] maps) {

    public sealed interface Map permits FlatMap, StairCaseMap {}

    public record FlatMap(BlockState[][][] blockStates) implements Map {}

    // todo
    public record StairCaseMap() implements Map {}

}
