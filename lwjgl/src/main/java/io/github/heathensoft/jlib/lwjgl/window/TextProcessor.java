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
     * @param character "non-printable-character"
     */
    void npcPress(int character);
    
    /**
     * @param character "non-printable-character"
     */
    void npcRelease(int character);
    
    /**
     * @param character ascii  [32 - 126]
     */
    void printable(byte character);
}
