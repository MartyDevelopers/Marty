package martydevs.marty.processor.image.cpu;

import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.awt.image.BufferedImage;

public final class CroppedView {

    final BufferedImage image;
    final Vector2ic minimum, maximum;
    final float scale;
    public final Vector2ic bounds;

    CroppedView(BufferedImage image, Vector2ic minimum, Vector2ic maximum, float scale) {
        this.image = image;
        this.minimum = minimum;
        this.maximum = maximum;
        this.scale = scale;
        this.bounds = new Vector2i(
                Math.round((maximum.x() - minimum.x()) * scale),
                Math.round((maximum.y() - minimum.y()) * scale)
        );
    }

    private int getARGB(int x, int y) {
        int scaledX = (int) Math.floor(x / scale);
        int scaledY = (int) Math.floor(y / scale);
        if (x > bounds.x() || y > bounds.y()) throw new ArrayIndexOutOfBoundsException(String.format(
                "values %s %s != %s %s",
                x,
                y,
                scaledX,
                scaledY
        ));
        return image.getRGB(minimum.x() + scaledX, minimum.y() + scaledY);
    }

    public int alpha(int x, int y) {
        return (getARGB(x, y) >> 24) & 0xff;
    }

    public int getRGB(int x, int y) {
        return getARGB(x, y);
    }

}
