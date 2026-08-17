package martydevs.marty.model.schematic;

import martydevs.marty.model.image.MapArtImage;
import martydevs.marty.model.work.Work;

import java.io.InputStream;

public interface NBTSchematicWork extends Work<InputStream> {

    MapArtImage source();

    record NBTSchematicWorkImpl(MapArtImage source) implements NBTSchematicWork {}

}
