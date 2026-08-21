package martydevs.marty.processor.image.cpu;

import net.minecraft.world.level.material.MapColor;

public record PaletteEntry(int rgb, MapColor mapColor, MapColor.Brightness brightness) {
}
