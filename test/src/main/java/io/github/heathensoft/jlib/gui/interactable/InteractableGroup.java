package io.github.heathensoft.jlib.gui.interactable;

import io.github.heathensoft.jlib.common.storage.generic.Container;

import java.util.ArrayList;
import java.util.List;

/**
 * Any member of a group can be down-cast to the group common super.
 *
 * @author Frederik Dahl
 * 21/11/2022
 */


public class InteractableGroup<T extends Interactable> {

    private final Class<T> super_class;
    private final List<InteractableType> types;
    private final Interactables instance;
    private final String name;
    private final int id;

    protected InteractableGroup(String name, Class<T> super_class, Interactables instance, List<InteractableType> types, int id) {
        this.super_class = super_class;
        this.instance = instance;
        this.types = types;
        this.name = name;
        this.id = id;
        for (InteractableType type : types) {
            type.add_to_group(this);
        }
    }

    public boolean is_member(InteractableType type) {
        return type.groups().get(id);
    }

    public boolean is_member(Interactable interactable) {
        return is_member(instance.type(interactable));
    }

    public T cast(Interactable interactable) {
        return super_class.cast(interactable);
    }

    public Class<T> super_class() {
        return super_class;
    }

    public List<InteractableType> types() {
        return types;
    }

    public List<T> members() {
        List<T> list = new ArrayList<>();
        for (InteractableType type : types) {
            Container<Interactable> container = instance.allOfType(type);
            container.read(item -> list.add(super_class.cast(item)));
        } return list;
    }

    public String name() {
        return name;
    }

    protected int id() {
        return id;
    }
}
