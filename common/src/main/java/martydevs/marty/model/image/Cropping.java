package martydevs.marty.model.image;

import org.joml.Vector2ic;

public sealed interface Cropping permits Cropping.Center, Cropping.Manual {

    record Center() implements Cropping {}

    record Manual(float zoom, Vector2ic shift) implements Cropping {}

}
