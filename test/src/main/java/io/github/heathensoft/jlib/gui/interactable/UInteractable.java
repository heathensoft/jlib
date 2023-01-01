package io.github.heathensoft.jlib.gui.interactable;

import io.github.heathensoft.jlib.common.Disposable;

/**
 * For ui elements - window coordinates
 *
 * @author Frederik Dahl
 * 27/11/2022
 */


public abstract class UInteractable extends CoreInteractable implements Disposable {


    /**
     * default is 0 for core hud atlas
     * @param slot texture slot. used in shader
     */
    public void useTexture(int slot) {
        iSetLsb8(slot);
    }


}
