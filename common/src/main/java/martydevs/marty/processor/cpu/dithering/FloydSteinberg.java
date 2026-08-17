package martydevs.marty.processor.cpu.dithering;

import martydevs.marty.helper.MapColorHelper;
import martydevs.marty.processor.cpu.CroppedView;

public final class FloydSteinberg implements DitheringAlgorithm {

    @Override
    public DitheringResult dither(CroppedView croppedView, boolean paletteIncludesWater, int[] palette, int[][] out) {
        int width = croppedView.bounds.x();
        int height = croppedView.bounds.y();
        int totalPixels = width * height;

        float[] redError = new float[totalPixels];
        float[] greenError = new float[totalPixels];
        float[] blueError = new float[totalPixels];

        int lowestWaterColorIndex = Integer.MAX_VALUE;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int index = y * width + x;

                int rgb = croppedView.getRGB(x, y);
                if(out[x][y] == -1) continue;

                int r = DitheringUtil.clamp(((rgb >> 16) & 0xFF) + Math.round(redError[index]));
                int g = DitheringUtil.clamp(((rgb >> 8) & 0xFF) + Math.round(greenError[index]));
                int b = DitheringUtil.clamp((rgb & 0xFF) + Math.round(blueError[index]));

                int nearestIndex = DitheringUtil.findNearestColor(r, g, b, palette);
                if(paletteIncludesWater) {
                    int waterColorIndex = MapColorHelper.waterColorIndex(palette[nearestIndex]);
                    if(waterColorIndex != -1 && waterColorIndex < lowestWaterColorIndex)
                        lowestWaterColorIndex = waterColorIndex;
                }
                out[x][y] = (byte) nearestIndex;

                int nearestRgb = palette[nearestIndex];
                int nr = (nearestRgb >> 16) & 0xFF;
                int ng = (nearestRgb >> 8) & 0xFF;
                int nb = nearestRgb & 0xFF;

                float errR = r - nr;
                float errG = g - ng;
                float errB = b - nb;

                if (x + 1 < width) {
                    int i = index + 1;
                    redError[i] += errR * 7.0f / 16.0f;
                    greenError[i] += errG * 7.0f / 16.0f;
                    blueError[i] += errB * 7.0f / 16.0f;
                }

                if (y + 1 < height) {
                    int i = index + width;

                    redError[i] += errR * 5.0f / 16.0f;
                    greenError[i] += errG * 5.0f / 16.0f;
                    blueError[i] += errB * 5.0f / 16.0f;

                    if (x > 0) {
                        int j = i - 1;
                        redError[j] += errR * 3.0f / 16.0f;
                        greenError[j] += errG * 3.0f / 16.0f;
                        blueError[j] += errB * 3.0f / 16.0f;
                    }

                    if (x + 1 < width) {
                        int j = i + 1;
                        redError[j] += errR / 16.0f;
                        greenError[j] += errG / 16.0f;
                        blueError[j] += errB / 16.0f;
                    }
                }
            }
        }

        return new DitheringResult(lowestWaterColorIndex == Integer.MAX_VALUE ? -1 : lowestWaterColorIndex);
    }

}
