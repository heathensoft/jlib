package io.github.heathensoft.jlib.hud.interactable;


import io.github.heathensoft.jlib.common.storage.generic.Container;
import io.github.heathensoft.jlib.common.storage.primitive.BitSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Collection of Interactables.
 *
 * Meant to be used to uniquely identify interactable objects
 * by a R32UI (texture integer) pixel id.
 *
 * there are 8 bits available for Type and 16 bits for Type instance.
 *
 * the 8 LSB in the interactable data is available for any other usage.
 * Like sending a texture id to shader.
 *
 * A pixel ID is automatically assigned to any Object of a class extending
 * Interactable ->
 * All you have to do is to add the interactable to the collection.
 * Do this by calling the interactables' registerInteractable() method.
 * This could be done in the constructor.
 *
 * By doing so. After reading a pixel value from a framebuffer texture
 * you can query this collection to get the corresponding Interactable.
 * Given that the pixel id is used when rendering the Interactable.
 *
 * There are also "Groups" you can create, where you assign a bundle of classes as members.
 * Doing so can be helpful to determine what class to cast the Interactable to.
 * Especially if there is a multitude of various Interactable types (classes extending Interactable)
 *
 * @author Frederik Dahl
 * 21/11/2022
 */


public class Interactables {

    private static Interactables instance;

    private final Map<String,InteractableGroup<? extends Interactable>> groups_by_name;
    private final Map<Class<? extends Interactable>, InteractableType> types_by_class;
    private final Container<Container<Interactable>> interactables;
    private final Container<InteractableGroup<? extends Interactable>> groups_by_id;
    private final Container<InteractableType> types_by_id;

    private int new_type_id;
    private int new_group_id;

    private Interactables() {
        types_by_id = new Container<>(16);
        groups_by_id = new Container<>(16);
        interactables = new Container<>(16);
        types_by_class = new HashMap<>(37);
        groups_by_name = new HashMap<>(37);
        add(new Void()); // the void interactable takes up TypeID 0
    }

    public static Interactables get() {
        if (instance == null)
            instance = new Interactables();
        return instance;
    }

    /**
     * When cleared, all registered interactables are unregistered.
     * All types and groups are deleted.
     * Using deleted group or type instances will cause errors.
     * Be careful to call this AFTER external disposing of interactables.
     * I.e. do not call this, then unregister interactables afterwards.
     */
    public static void clear() {
        if (instance != null) {
            instance.interactables.read(container -> container.read(interactable -> interactable.iSetPixelID(0)));
            instance.interactables.clear();
            instance.types_by_id.clear();
            instance.groups_by_id.clear();
            instance.groups_by_name.clear();
            instance.types_by_class.clear();
            instance.new_group_id = 0;
            instance.new_type_id = 0;
            instance.add(new Void());
        }
    }

    public InteractableGroup<?> createGroup(String name, List<Class<? extends Interactable>> list) {
        return createGroup(name,Interactable.class,list);
    }

    public <T extends Interactable>InteractableGroup<T> createGroup(String name, Class<T> common_super, List<Class<? extends T>> list) {
        if (!list.isEmpty()) {
            List<InteractableType> types = new ArrayList<>(list.size());
            for (Class<? extends T> clazz : list) {
                types.add(type(clazz));
            } InteractableGroup<T> group;
            group = new InteractableGroup<T>(name,common_super,this,types,new_group_id++);
            groups_by_name.put(name,group);
            groups_by_id.add(group);
            return group;
        } return null;
    }

    public InteractableGroup<?> group(String name) {
        return groups_by_name.get(name);
    }

    @SuppressWarnings("unchecked")
    public <T extends Interactable> InteractableGroup<T> group(String name, Class<T> super_class) {
        InteractableGroup<? extends Interactable> group = groups_by_name.get(name);
        if (group.super_class().equals(super_class)) {
            return (InteractableGroup<T>)group;
        } return null;
    }

    /**
     * @param data interactable data
     * @return interactable or null if pixel id = (data >> 8) == 0
     * @throws IllegalStateException If the pixel id is invalid
     */
    public Interactable interactable(int data) throws IllegalStateException {
        int pixelID = data >> 8;
        if (pixelID == 0) return null;
        try { int type_id = pixelID >> 16;
            int instance_id = pixelID & 0x0000_FFFF;
            return interactables.get(type_id).get(instance_id);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalStateException("invalid interactable",e);
        }
    }

    protected List<InteractableGroup<? extends Interactable>> groups(Interactable interactable) {
        InteractableType type = type(interactable);
        BitSet group_set = type.groups();
        int type_group_count = group_set.cardinality();
        List<InteractableGroup<? extends Interactable>> list = new ArrayList<>(type_group_count);
        if (type_group_count > 0) {
            int group_count_total = groups_by_id.count();
            for (int i = 0; i < group_count_total; i++)
                if (group_set.get(i)) list.add(groups_by_id.get(i));
        } return list;
    }

    protected void add(Interactable interactable) {
        if (interactable.iPixelID() != 0) {
            throw new IllegalStateException("attempting to add interactable already in collection");
        } InteractableType type = type(interactable.getClass());
        int instance_id = type.obtain_instance_id();
        interactables.get(type.id()).set(instance_id,interactable);
        int pixel_id = (type.id() << 16) | instance_id;
        interactable.iSetPixelID(pixel_id);
    }

    protected void remove(Interactable interactable) {
        if (interactable.iPixelID() == 0) {
            throw new IllegalStateException("attempting to remove interactable not in collection");
        } InteractableType type = type(interactable.getClass());
        int instance_id = interactable.iTypeInstanceID();
        Interactable removed = interactables.get(type.id()).get(instance_id);
        if (removed != interactable) {
            throw new IllegalStateException("interactable not found");
        } interactables.get(type.id()).set(instance_id,null);
        type.returnID(instance_id);
        interactable.iSetPixelID(0);
    }

    public int count(InteractableType type) {
        return interactables.get(type.id()).count();
    }

    public int count(Class<? extends Interactable> clazz) {
        return count(type(clazz));
    }

    protected InteractableType type(Interactable interactable) {
        return type(interactable.getClass());
    }

    public InteractableType type(Class<? extends Interactable> clazz) {
        InteractableType type = types_by_class.get(clazz);
        if (type == null) {
            type = new InteractableType(clazz,new_type_id++);
            types_by_class.put(clazz,type);
            types_by_id.add(type);
            interactables.add(new Container<>(8));
        } return type;
    }

    // Remember, this could include the "void" interactable.
    public <T extends Interactable> List<T> allAssignableTo(Class<T> superClass) {
        List<T> list = new ArrayList<>();
        types_by_id.read(i1 -> {
            if (superClass.isAssignableFrom(i1.clazz())) {
                Container<Interactable> of_type = interactables.get(i1.id());
                of_type.read(i2 -> list.add(superClass.cast(i2)));
            }
        }); return list;
    }

    public <T extends Interactable> List<T> allOfType(Class<T> clazz) {
        InteractableType type = type(clazz);
        Container<Interactable> types = interactables.get(type.id());
        List<T> list = new ArrayList<>(types.count());
        types.read(item -> list.add(clazz.cast(item)));
        return list;
    }

    public <T extends Interactable> void allOfType(Class<T> clazz, List<T> dest) {
        InteractableType type = type(clazz);
        Container<Interactable> types = interactables.get(type.id());
        types.read(item -> dest.add(clazz.cast(item)));
    }

    protected Container<Interactable> allOfType(InteractableType type) {
        return interactables.get(type.id());
    }

    private static final class Void extends Interactable { }
}
