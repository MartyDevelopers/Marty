package martydevs.marty.model.image;

import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector2ic;

public record MapArtImage(Vector2ic dimensionsInMaps, Map[][] maps) {

    public static int encodedBlockCoordinates(int x, int y, int z) {
        return (x & 255) << 16 | (y & 255) << 8 | z & 255;
    }

    public static int decodedX(int encoded) {
        return (encoded >> 16) & 255;
    }

    public static int decodedY(int encoded) {
        return (encoded >> 8) & 255;
    }

    public static int decodedZ(int encoded) {
        return encoded & 255;
    }

    public record Map(java.util.Map<Integer, BlockState> blocks, int height) {}

}
