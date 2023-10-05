package io.github.heathensoft.jlib.lwjgl.window;

/**
 *
 * Processor for "legacy" ascii characters [ 0-127 ]
 *
 * @author Frederik Dahl
 * 13/11/2022
 */


public interface TextProcessor {
    
    /**
     * @param key "non-printable-key"
     */
    void keyPress(int key, int mods);
    
    /**
     * @param key "non-printable-key"
     */
    void keyRelease(int key, int mods);
    
    /**
     * @param character ascii  [00 - 127]
     */
    void characterStream(byte character);
}
