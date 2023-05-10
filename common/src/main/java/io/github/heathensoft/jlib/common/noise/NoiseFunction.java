package io.github.heathensoft.jlib.common.noise;

import io.github.heathensoft.jlib.common.utils.U;

/**
 *
 * Should return a value n: 1 >= n >= 0
 *
 * @author Frederik Dahl
 * 23/06/2022
 */


public interface NoiseFunction {
    
    /**
     * @param x arbitrary axis
     * @param y arbitrary axis
     * @return Should return a value n: 1 >= n >= 0
     */
    float get(float x, float y);



    final class Rigged implements NoiseFunction {

        public final FastNoiseLite noiseLite;
        public boolean smooth;
        public boolean inverted;

        public Rigged(float frequency,int seed) {
            this(frequency,0.5f,2.0f,0.0f,false,false,seed);
        }

        public Rigged(float frequency, float gain, float lacunarity, float weighted_strength, boolean smooth, boolean inverted, int seed) {
            noiseLite = new FastNoiseLite(seed);
            noiseLite.SetNoiseType(FastNoiseLite.NoiseType.Cellular);
            noiseLite.SetCellularDistanceFunction(FastNoiseLite.CellularDistanceFunction.Euclidean);
            noiseLite.SetFractalType(FastNoiseLite.FractalType.FBm);
            noiseLite.SetFractalWeightedStrength(weighted_strength);
            noiseLite.SetFractalLacunarity(lacunarity);
            noiseLite.SetFractalGain(gain);
            noiseLite.SetFractalOctaves(6);
            noiseLite.SetFrequency(frequency);
            this.inverted = inverted;
            this.smooth = smooth;
        }

        public float get(float x, float y) {
            float n = (noiseLite.GetNoise(x, y) + 1.0f) * 0.5f;
            n = inverted ? 1 - n : n;
            return smooth ? U.smooth(n) : n;
        }
    }



    final class Classic implements NoiseFunction {

        public final FastNoiseLite noise;
        public boolean smooth;

        public Classic(float frequency, int seed) {
            this(frequency,0.5f,2.0f,0.0f,false,seed);
        }

        public Classic(float frequency, float gain, float lacunarity, float weighted_strength, boolean smooth, int seed) {
            noise = new FastNoiseLite(seed);
            noise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2S);
            noise.SetFractalType(FastNoiseLite.FractalType.FBm);
            noise.SetFractalWeightedStrength(weighted_strength);
            noise.SetFractalLacunarity(lacunarity);
            noise.SetFractalGain(gain);
            noise.SetFractalOctaves(6);
            noise.SetFrequency(frequency);
            this.smooth = smooth;
        }

        public float get(float x, float y) {
            float n = (noise.GetNoise(x, y) + 1.0f) * 0.5f;
            return smooth ? U.smooth(n) : n;
        }
    }
}
