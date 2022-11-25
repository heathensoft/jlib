package io.github.heathensoft.jlib.common.utils.interactable;

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
 *
 * @author Frederik Dahl
 * 21/11/2022
 */


public abstract class Interactable {

    private int pixel_id;

    public final void registerInteractable() {
        Interactables.get().add(this);
    }

    public final void removeInteractable() {
        Interactables.get().remove(this);
    }

    public final boolean castableTo(Interactable other) {
        return other.getClass().isAssignableFrom(this.getClass());
    }

    public final boolean memberOf(InteractableGroup<? extends Interactable> group) {
        return group.is_member(this);
    }

    public final boolean sameClass(Interactable other) {
        return other.getClass().equals(this.getClass());
    }

    public final InteractableType interactableType() {
        return Interactables.get().type(this);
    }

    public final List<InteractableGroup<? extends Interactable>> groups() {
        return Interactables.get().groups(this);
    }

    public final int typeInstanceID() {
        return pixel_id & 0x000F_FFFF;
    }

    public final int typeID() {
        return pixel_id >> 20;
    }

    public final int pixelID() {
        return pixel_id;
    }

    protected final void setPixelID(int id) {
        pixel_id = id;
    }


}
