package io.github.heathensoft.jlib.common.utils;

/**
 *
 * Should return a value n: 1 >= n >= -1
 *
 * @author Frederik Dahl
 * 23/06/2022
 */


public interface NoiseFunction {
    
    /**
     * @param x arbitrary axis
     * @param y arbitrary axis
     * @return Should return a value n: 1 >= n >= -1
     */
    float get(float x, float y);
}
