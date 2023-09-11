package io.github.heathensoft.jlib.gui.interact;


/**
 *
 * @author Frederik Dahl
 * 02/09/2023
 */


public interface Interactable {


    int pixelID();

    void setPixelID(int id);

    default int iTypeInstanceID() {
        return (pixelID() & 0x0000_FFFF);
    }

    default int iTypeID() {
        return (pixelID() >> 16) & 0xFF;
    }

    default boolean iCastableTo(Interactable o) {
        return o.getClass().isAssignableFrom(this.getClass());
    }

    default boolean iCastableTo(Class<? extends Interactable> clazz) {
        return clazz.isAssignableFrom(this.getClass());
    }

    default boolean iSameClass(Interactable o) {
        return iSameClass(o.getClass());
    }

    default boolean iSameClass(Class<? extends Interactable> clazz) {
        return clazz.equals(this.getClass());
    }

    default boolean isRegistered() {
        return pixelID() != 0;
    }

}
