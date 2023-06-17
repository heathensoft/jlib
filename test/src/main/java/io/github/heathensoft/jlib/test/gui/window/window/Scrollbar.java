package io.github.heathensoft.jlib.test.gui.window.window;


import io.github.heathensoft.jlib.test.gui.interactable.OnScroll;
import io.github.heathensoft.jlib.test.gui.interactable.UInteractable;

/**
 * @author Frederik Dahl
 * 22/12/2022
 */


public class Scrollbar extends UInteractable implements OnScroll {

    private final ScrollableBox<?> scrollableBox;

    public Scrollbar(ScrollableBox<?> scrollableBox) {
        setOnScroll(this);
        iRegisterInteractable();
        this.scrollableBox = scrollableBox;
    }

    public void dispose() {
        iRemoveInteractable();
    }

    public void executeOnScroll(float amount) {
        scrollableBox.onScroll(-amount);
    }
}
