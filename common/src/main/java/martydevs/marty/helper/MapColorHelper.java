package martydevs.marty.helper;

import net.minecraft.util.ARGB;
import net.minecraft.world.level.material.MapColor;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public final class MapColorHelper {

    public static final MapColor[] MATERIAL_COLORS;
    private static final AdditionalMapColorData[] ADDITIONAL_MAP_COLOR_DATA;
    private static final Map<Integer, MapColor> RGB_TO_MAP_COLOR;

    private static final int[] WATER_COLORS;

    static {
        try {
            Field field = MapColor.class.getDeclaredField("MATERIAL_COLORS");
            field.setAccessible(true);
            MATERIAL_COLORS = (MapColor[]) field.get(null);
        }
        catch (Throwable throwable) {
            throw new ExceptionInInitializerError(throwable);
        }

        ADDITIONAL_MAP_COLOR_DATA = new AdditionalMapColorData[MATERIAL_COLORS.length];
        RGB_TO_MAP_COLOR = new HashMap<>();
        for (int i = 0; i < MATERIAL_COLORS.length; i++) {
            MapColor mapColor = MATERIAL_COLORS[i];
            if(mapColor == null) break;

            int rgbValue = mapColor.col & 0x00FFFFFF;
            ADDITIONAL_MAP_COLOR_DATA[i] = new AdditionalMapColorData(
                    rgbValue,
                    new int[]{
                            ARGB.scaleRGB(rgbValue, MapColor.Brightness.LOW.modifier),
                            ARGB.scaleRGB(rgbValue, MapColor.Brightness.NORMAL.modifier),
                            ARGB.scaleRGB(rgbValue, MapColor.Brightness.HIGH.modifier)
                    }
            );

            for (int scaledValue : ADDITIONAL_MAP_COLOR_DATA[i].scaledValues) {
                RGB_TO_MAP_COLOR.put(scaledValue, mapColor);
            }
        }

        WATER_COLORS = new int[3];
        for (int i = 0; i < 3; i++) {
            WATER_COLORS[i] = scaledRgbValue(MapColor.WATER, MapColor.Brightness.byId(i));
        }
    }

    private MapColorHelper() {}

    public static @Nullable MapColor colorByRgbValue(int rgbValue) {
        return RGB_TO_MAP_COLOR.get(rgbValue);
    }

    public static int waterColorIndex(int rgb) {
        for (int i = 0; i < WATER_COLORS.length; i++) {
            if(WATER_COLORS[i] == rgb) return i;
        }
        return -1;
    }

    public static boolean blockRepresentable(MapColor color) {
        return color != MapColor.WATER;
    }

    public static int rgbValue(MapColor color) {
        return ADDITIONAL_MAP_COLOR_DATA[color.id].rgbValue();
    }

    public static int scaledRgbValue(MapColor color, MapColor.Brightness brightness) {
        AdditionalMapColorData additionalMapColorData = ADDITIONAL_MAP_COLOR_DATA[color.id];

        // It's useless to pre-calculate LOWEST brightness - it is not obtainable through blocks
        if(brightness == MapColor.Brightness.LOWEST)
            return ARGB.scaleRGB(additionalMapColorData.rgbValue, MapColor.Brightness.LOWEST.modifier);

        return additionalMapColorData.scaledValues[brightness.id];
    }

    private record AdditionalMapColorData(int rgbValue, int[] scaledValues) {

    }

}
