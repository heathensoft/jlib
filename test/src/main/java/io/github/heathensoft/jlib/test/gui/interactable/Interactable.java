package io.github.heathensoft.jlib.test.gui.interactable;

import java.util.List;

/**
 * Interactables are registered to a collection of interactables.
 * They are assigned a type and type-instance id, hashed to a pixel id.
 * You can also assign Interactables (types) to groups. I.e. "HUD" and "ENTITIES".
 * Groups have common super class for its members, that all members can be down cast to.
 *
 * Interactables must be registered in the collection before use. (Could be done in the constructor).
 * And they must be removed from the collection when they are disposed. (No longer in use).
 *
 * data             --> 0xFFFF_FFFF
 * type_id          --> 0xFF00_0000 (255 types)
 * instance         --> 0x00FF_FF00 (65,536 instances of any type)
 * pixel_id         --> 0xFFFF_FF00 (used by the system)
 * lsb8             --> 0x0000_00FF (custom usage)
 *
 * There is a byte of data untouched by the interactable system (lsb8)
 *
 *
 * @author Frederik Dahl
 * 21/11/2022
 */


public abstract class Interactable {

    private int i_data;

    public final void iRegisterInteractable() {
        Interactables.get().add(this);
    }

    public final void iRemoveInteractable() {
        Interactables.get().remove(this);
    }

    public final boolean iCastableTo(Interactable other) {
        return other.getClass().isAssignableFrom(this.getClass());
    }

    public final boolean iCastableTo(Class<? extends Interactable> clazz) {
        return clazz.isAssignableFrom(this.getClass());
    }

    public final boolean iMemberOf(InteractableGroup<? extends Interactable> group) {
        return group.is_member(this);
    }

    public final boolean iSameClass(Interactable other) {
        return other.getClass().equals(this.getClass());
    }

    public final InteractableType iType() {
        return Interactables.get().type(this);
    }

    public final List<InteractableGroup<? extends Interactable>> iGroups() {
        return Interactables.get().groups(this);
    }

    public final int iData() {
        return i_data;
    }

    public final int iTypeInstanceID() {
        return (i_data & 0x00FF_FF00) >> 8;
    }

    public final int iTypeID() {
        return i_data >> 24;
    }

    public final int iPixelID() {
        return (i_data & 0xFFFF_FF00) >> 8;
    }

    public final int iLsb8() {
        return i_data & 0x0000_00FF;
    }

    /**
     * There is a byte of data untouched by the interactable system
     * @param b 8-bit custom data
     */
    public final void iSetLsb8(int b) {
        iSetLsb8((byte)(b & 0xFF));
    }

    /**
     * There is a byte of data untouched by the interactable system
     * @param b 8-bit custom data
     */
    public final void iSetLsb8(byte b) {
        i_data = (i_data & 0xFFFF_FF00) | b;
    }

    protected final void iSetPixelID(int id) {
        i_data = ( (id << 8) | iLsb8());
    }

}
