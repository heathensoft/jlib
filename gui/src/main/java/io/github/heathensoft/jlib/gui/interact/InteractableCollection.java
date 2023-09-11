package io.github.heathensoft.jlib.gui.interact;

import io.github.heathensoft.jlib.common.storage.generic.Container;
import io.github.heathensoft.jlib.common.utils.IDPool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * Must be initialized before use.
 * Can be reinitialized multiple times in the lifetime of a program. But Must be disposed between.
 * Disposing the Interactables instance set the pixelID of all active Interactable components to 0,
 * And deletes the collection (all external references).
 * Calling deactivate on interactable components after the Interactable Collection instance has been disposed
 * will throw errors.
 *
 *
 * @author Frederik Dahl
 * 02/09/2023
 */


public class InteractableCollection {

    private static InteractableCollection instance;

    private final Map<Class<? extends Interactable>, InteractableType> types_by_class;
    private final Container<Container<Interactable>> interactables;
    private final Container<InteractableType> types_by_id;
    private int new_type_id;

    private InteractableCollection() {
        types_by_id = new Container<>(16);
        interactables = new Container<>(16);
        types_by_class = new HashMap<>(37);
        add(new VoidComponent()); // the void interactable takes up TypeID 0
    }

    public static void initialize() {
        if (instance == null) instance = new InteractableCollection();
    }

    /**
     * When cleared, all active interactables are deactivated,
     * and the Interactables instance is nullified / deleted.
     * Using deleted type instances will cause errors.
     * Do not call this, then remove interactables manually afterwards.
     */
    public static void dispose() {
        if (instance != null) {
            instance.interactables.read(container -> container.read(interactable -> instance.remove(interactable)));
            instance = null;
        }
    }

    public static InteractableCollection get() {
        return instance;
    }

    public Interactable interactable(int pixelID) throws IllegalStateException {
        if (pixelID <= 0) return null;
        try { int type_id = (pixelID >> 16) & 0xFF;
            int instance_id = pixelID & 0x0000_FFFF;
            return interactables.get(type_id).get(instance_id);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalStateException("invalid interactable",e);
        }
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

    public <T extends Interactable> List<T> allAssignableTo(Class<T> superClass) {
        List<T> list = new ArrayList<>();
        types_by_id.read(type -> {
            if (superClass.isAssignableFrom(type.clazz())) {
                if (type.clazz() != VoidComponent.class) {
                    Container<Interactable> of_type = interactables.get(type.id);
                    of_type.read(interactable -> list.add(superClass.cast(interactable)));
                }
            }
        }); return list;
    }

    public int count(Class<? extends Interactable> clazz) {
        return count(type(clazz));
    }

    /**
     * Adds Interactable to collection, giving it a unique pixelID.
     * @param interactable The interactable to register
     * @throws IllegalStateException Cannot add interactable already in collection
     */
    public void add(Interactable interactable) throws IllegalStateException {
        if (interactable.isRegistered()) {
            throw new IllegalStateException("attempting to add already assigned interactable");
        }
        InteractableType type = type(interactable);
        int instance_id = type.obtain_instance_id();
        Interactable existing = interactables.get(type.id()).get(instance_id);
        if (existing != null) {
            throw new IllegalStateException("illegal attempt to overwrite interactable");
        }
        interactables.get(type.id()).set(instance_id,interactable);
        int pixel_id = (type.id() << 16) | instance_id;
        interactable.setPixelID(pixel_id);
    }

    /**
     * Removes Interactable from collection. Resets its pixelID to 0 (Unregistered)
     * @param interactable the interactable to remove
     * @throws IllegalStateException Attempting to remove unregistered Interactable
     */
    public void remove(Interactable interactable) throws IllegalStateException {
        if (!interactable.isRegistered()) {
            if (!interactable.iSameClass(VoidComponent.class))
                throw new IllegalStateException("attempting to remove unassigned interactable");
        }
        InteractableType type = type(interactable);
        int instance_id = interactable.iTypeInstanceID();
        Interactable removed;
        try {
            removed = interactables.get(type.id()).get(instance_id);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalStateException("array out of bounds: corrupted pixelID");
        }
        if (removed != interactable) {
            throw new IllegalStateException("interactable passed to remove is not the same as the one in collection");
        }
        interactables.get(type.id()).set(instance_id,null);
        type.returnID(instance_id);
        interactable.setPixelID(0);
    }

    private int count(InteractableType type) {
        return interactables.get(type.id()).count();
    }

    private InteractableType type(Interactable interactable) {
        return type(interactable.getClass());
    }

    private InteractableType type(Class<? extends Interactable> clazz) {
        InteractableType type = types_by_class.get(clazz);
        if (type == null) {
            type = new InteractableType(clazz,new_type_id++);
            types_by_class.put(clazz,type);
            types_by_id.add(type);
            interactables.add(new Container<>(8));
        } return type;
    }

    private static final class InteractableType {
        private final Class<? extends Interactable> interactable_class;
        private final IDPool instance_ids;
        private final int id;
        InteractableType(Class<? extends Interactable> clazz, int id) {
            this.instance_ids = new IDPool();
            this.interactable_class = clazz;
            this.id = id;
        } Class<? extends Interactable> clazz() {
            return interactable_class;
        }
        int obtain_instance_id() {
            return instance_ids.obtainID();
        } void returnID(int id) {
            instance_ids.returnID(id);
        }
        int id() {
            return id;
        }
    }

    private static final class VoidComponent implements Interactable {
        private int pixelID;
        public int pixelID() {
            return pixelID;
        }
        public void setPixelID(int id) {
            pixelID = id;
        }
    }

}
