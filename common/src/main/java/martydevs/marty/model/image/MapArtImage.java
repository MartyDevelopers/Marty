package martydevs.marty.model.image;

import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector2ic;

public record MapArtImage(Vector2ic dimensionsInMaps, Map[][] maps) {

    public static int encodedBlockCoordinates(int x, int y, int z) {
        if (y < -256 || y > 256)
            throw new IllegalArgumentException("Y must be between -256 and 256: " + y);

        int encodedY = y + 256;

        return (x << 18)
                | (encodedY << 8)
                | z;
    }

    public static int decodedX(int encoded) {
        return (encoded >> 18) & 0xFF;
    }

    public static int decodedY(int encoded) {
        return ((encoded >> 8) & 0x3FF) - 256;
    }

    public static int decodedZ(int encoded) {
        return encoded & 0xFF;
    }

    public record Map(java.util.Map<Integer, BlockState> blocks, int height) {}

}
