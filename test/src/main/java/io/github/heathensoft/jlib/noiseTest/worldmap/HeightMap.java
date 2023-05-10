package io.github.heathensoft.jlib.noiseTest.worldmap;

import io.github.heathensoft.jlib.common.noise.FastNoiseLite;
import io.github.heathensoft.jlib.common.noise.Noise;
import io.github.heathensoft.jlib.common.noise.NoiseFunction;
import io.github.heathensoft.jlib.common.utils.Rand;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;

import static io.github.heathensoft.jlib.common.utils.U.*;
import static org.lwjgl.stb.STBImageWrite.stbi_write_png;

/**
 * @author Frederik Dahl
 * 19/04/2023
 */


public class HeightMap {

    private int movement_penalty_factor;
    private float movement_penalty_threshold; // no penalty below
    private float[][] elevation;
    private Noise.Sampler2D sampler;



    public HeightMap(int width, int height, int seed) {
        float x0 = (Rand.hash(width,seed) % 99999) * Rand.white_noise(height + width,seed);
        float y0 = (Rand.hash(height,seed) % 99999) * Rand.white_noise(height - width,seed);
        HeightFunction heightFunction = new HeightFunction(seed);
        elevation = Noise.generate_amplified(heightFunction,height,width,x0,y0);
        /*
        int[] minima = Noise.local_minima(elevation);
        for (int i = 0; i < minima.length; i+=2) {
            System.out.println(minima[i] + " , " + minima[i+1]);
        }

         */
    }


    public float get(int x, int y) {
        return elevation[y][x];
    }

    public int rows() {
        return elevation.length;
    }

    public int cols() {
        return elevation[0].length;
    }

    public boolean contains(int x, int y) {
        return x >= 0 && y >= 0 && x < cols() && y < rows();
    }

    public void toDisk(String path) {
        int rows = rows();
        int cols = cols();
        ByteBuffer pixels = BufferUtils.createByteBuffer(rows * cols);
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                float h = elevation[r][c];
                pixels.put((byte) (round(h * 255) & 0xFF));
            }
        } stbi_write_png(path,cols,rows,1,pixels.flip(),cols);
    }

    private static final class HeightFunction implements NoiseFunction {

        private final FastNoiseLite noise_generator;
        public float y_frequency_scale = 0.5f;
        public float exponent = 1.2f;
        public boolean smoothen = true;


        HeightFunction(int seed) {
            float frequency = 0.35f;
            float fractal_gain = 0.5f;
            float fractal_lacunarity = 1.90f;
            float fractal_weighted_strength = 0.25f;
            noise_generator = new FastNoiseLite(seed);
            noise_generator.SetNoiseType(FastNoiseLite.NoiseType.Cellular);
            noise_generator.SetFractalGain(fractal_gain);
            noise_generator.SetFractalType(FastNoiseLite.FractalType.FBm);
            noise_generator.SetFractalWeightedStrength(fractal_weighted_strength);
            noise_generator.SetFractalLacunarity(fractal_lacunarity);
            noise_generator.SetCellularDistanceFunction(FastNoiseLite.CellularDistanceFunction.EuclideanSq);
            noise_generator.SetFrequency(frequency);
        }

        public float get(float x, float y) {
            float noise = noise_generator.GetNoise(x, y * y_frequency_scale);
            noise = clamp((noise + 1.0f) / 2);
            noise = (float) Math.pow(noise,exponent);
            noise = smoothen ? smooth(noise) : noise;
            return noise;
        }
    }


}
