package martydevs.marty.model.schematic;

import martydevs.marty.model.image.MapArtImage;
import martydevs.marty.model.work.Work;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.io.InputStream;

public interface NBTSchematicWork extends Work<InputStream> {

    MapArtImage source();

    boolean addShadePreservingLine();

    @Nullable BlockState shadePreservingMaterial();

    record NBTSchematicWorkImpl(MapArtImage source, boolean addShadePreservingLine, @Nullable BlockState shadePreservingMaterial) implements NBTSchematicWork {}

}
