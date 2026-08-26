package martydevs.marty.util;

public final class ColorUtil {

    private ColorUtil() {}

    private static int getComponent(int value, int shift) {
        return (value >> shift) & 0xFF;
    }

    public static int redComponent(int rgb) {
        return getComponent(rgb, 16);
    }

    public static int greenComponent(int rgb) {
        return getComponent(rgb, 8);
    }

    public static int blueComponent(int rgb) {
        return getComponent(rgb, 0);
    }

    private static int writeComponent(int component, int shift) {
        return (component & 0xFF) << shift;
    }

    public static int rgb(int redComponent, int greenComponent, int blueComponent) {
        return writeComponent(redComponent, 16) | writeComponent(greenComponent, 8) | writeComponent(0, blueComponent);
    }

    public static float[] rgbToLab(int redComponent, int greenComponent, int blueComponent, float[] tristimulus) {
        float[] out = new float[3];
        rgbToXyz(redComponent, greenComponent, blueComponent, out);
        xyzToLab(out[0], out[1], out[2], tristimulus, out);
        return out;
    }

    public static void rgbToXyz(int redComponent, int greenComponent, int blueComponent, float[] out) {
        float red = redComponent / 255f;
        float green = greenComponent / 255f;
        float blue = blueComponent / 255f;

        if (red > 0.04045) red = (float)Math.pow(( ( red + 0.055f ) / 1.055f ), 2.4f);
        else red /= 12.92f;

        if (green > 0.04045) green = (float) Math.pow(( ( green + 0.055f ) / 1.055f ), 2.4f);
        else green /= 12.92f;

        if (blue > 0.04045) blue = (float)Math.pow(( ( blue + 0.055f ) / 1.055f ), 2.4f);
        else blue /= 12.92f;

        red *= 100;
        green *= 100;
        blue *= 100;

        float x = 0.412453f * red + 0.35758f * green + 0.180423f * blue;
        float y = 0.212671f * red + 0.71516f * green + 0.072169f * blue;
        float z = 0.019334f * red + 0.119193f * green + 0.950227f * blue;

        out[0] = x;
        out[1] = y;
        out[2] = z;
    }

    public static void xyzToLab(float x, float y, float z, float[] tristimulus, float[] out) {
        x /= tristimulus[0];
        y /= tristimulus[1];
        z /= tristimulus[2];

        if (x > 0.008856)
            x = (float)Math.pow(x,0.33f);
        else
            x = (7.787f * x) + ( 0.1379310344827586f );

        if (y > 0.008856)
            y = (float)Math.pow(y,0.33f);
        else
            y = (7.787f * y) + ( 0.1379310344827586f );

        if (z > 0.008856)
            z = (float)Math.pow(z,0.33f);
        else
            z = (7.787f * z) + ( 0.1379310344827586f );

        out[0] = (116 * y) - 16;
        out[1] = 500 * (x - y);
        out[2] = 200 * (y - z);
    }

}
