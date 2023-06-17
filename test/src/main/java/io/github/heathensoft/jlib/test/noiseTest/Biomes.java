package io.github.heathensoft.jlib.test.noiseTest;

import static io.github.heathensoft.jlib.common.utils.Rand.white_noise;
import static io.github.heathensoft.jlib.common.utils.U.scale_array;

/**
 * @author Frederik Dahl
 * 12/05/2023
 */


public class Biomes {


    public static int[][] grow(int[][] src, int pow_2, int[] noise_position, int seed) {
        if (src[0].length != src.length) throw new IllegalArgumentException("array must be of size: n * n");
        int[][] dst = scale_array(src,next_valid_sample_size(src.length));
        int target_size = (int)Math.pow(2,pow_2);
        while (dst.length < target_size) {
            dst = grow(dst,noise_position,seed);
        } return scale_array(dst,target_size);
    }

    public static int[][] grow(int[][] src, int[] noise_position, int seed) {
        int[][] dst = prepare_for_growth(src);
        int rows = dst.length;
        int cols = dst[0].length;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (r % 2 == 0) {
                    if (c % 2 == 1) {
                        float n = white_noise(noise_position,seed);
                        dst[r][c] = n < 0.5f ? dst[r][c-1] : dst[r][c+1];
                    }
                } else {
                    if (c % 2 == 0) {
                        float n = white_noise(noise_position,seed);
                        dst[r][c] = n < 0.5f ? dst[r-1][c] : dst[r+1][c];
                    }
                }
            }
        }
        for (int r = 1; r < rows; r += 2) {
            for (int c = 1; c < cols; c += 2) {
                float n = white_noise(noise_position,seed);
                int t1 = n < 0.5f ? dst[r][c-1] : dst[r][c+1];
                n = white_noise(noise_position,seed);
                int t2 = n < 0.5f ? dst[r-1][c] : dst[r+1][c];
                n = white_noise(noise_position,seed);
                dst[r][c] = n < 0.5f ? t1 : t2;
            }
        } return dst;
    }

    private static int next_valid_sample_size(int size) {
        int valid_size = 2;
        while (valid_size < size) {
            valid_size = valid_size * 2 - 1;
        } return valid_size;
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
