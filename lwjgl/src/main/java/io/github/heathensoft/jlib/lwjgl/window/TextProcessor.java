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
    void keyPress(int key, int mods, boolean repeat);

    /**
     * @param character ascii  [00 - 127]
     */
    void charPress(byte character);

    /**
     * @param key "non-printable-key"
     */
    default void keyRelease(int key, int mods) { }

    default void onTextProcessorActivated() { }

    default void onTextProcessorDeactivated() { }

    default void activateTextProcessor() {
        Engine.get().input().keys().setTextProcessor(this);
    }

    /** Deactivates if this is the current TextProcessor */
    default void deactivateTextProcessor() {
        Engine.get().input().keys().deactivateTextProcessor(this);
    }

    default boolean isActiveTextProcessor() {
        return Engine.get().input().keys().isActiveTextProcessor(this);
    }
}
