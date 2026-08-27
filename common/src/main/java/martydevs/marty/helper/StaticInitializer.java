package martydevs.marty.helper;

import martydevs.marty.helper.map.MapColorHelper;
import martydevs.marty.helper.palette.PaletteHelper;

public final class StaticInitializer {

    private StaticInitializer() {}

    private static void load(Class<?> clazz) {
        try {
            Class.forName(clazz.getName(), true, clazz.getClassLoader());
        }
        catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void initialize() {
        load(MapColorHelper.class);
        load(PaletteHelper.class);
    }

}
