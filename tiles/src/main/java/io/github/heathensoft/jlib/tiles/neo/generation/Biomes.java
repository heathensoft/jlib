package io.github.heathensoft.jlib.tiles.neo.generation;

import static io.github.heathensoft.jlib.common.utils.Rand.white_noise;

/**
 * @author Frederik Dahl
 * 12/05/2023
 */


public class Biomes {


    public static int[][] grow(int[][] src, int[] noise_position, int seed) {
        if (src[0].length != src.length) {
            throw new IllegalArgumentException("argument array must be of size: n * n");
        } int[][] dst = prepare_for_growth(src);
        int rows = dst.length;
        int cols = dst[0].length;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (r % 2 == 0) {
                    if (c % 2 == 1) {
                        float n = white_noise(noise_position,seed);
                        int tl = dst[r][c-1];
                        int tr = dst[r][c+1];
                        dst[r][c] = n < 0.5f ? tl : tr;
                    }
                } else {
                    if (c % 2 == 0) {
                        float n = white_noise(noise_position,seed);
                        int tb = dst[r-1][c];
                        int tt = dst[r+1][c];
                        dst[r][c] = n < 0.5f ? tb : tt;
                    }
                }
            }
        }
        for (int r = 1; r < rows; r += 2) {
            for (int c = 1; c < cols; c += 2) {
                int t1;
                int t2;
                {
                    float n = white_noise(noise_position,seed);
                    int tl = dst[r][c-1];
                    int tr = dst[r][c+1];
                    t1 = n < 0.5f ? tl : tr;
                }
                {
                    float n = white_noise(noise_position,seed);
                    int tb = dst[r-1][c];
                    int tt = dst[r+1][c];
                    t2 = n < 0.5f ? tb : tt;
                }
                float n = white_noise(noise_position,seed);
                dst[r][c] = n < 0.5f ? t1 : t2;;
            }
        }
        return dst;
    }

    private static int[][] prepare_for_growth(int[][] biomes) {
        int rows = biomes.length;
        int cols = biomes[0].length;
        int[][] result = new int[rows * 2 - 1][cols * 2 - 1];
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                result[r*2][c*2] = biomes[r][c];
        return result;
    }
}
