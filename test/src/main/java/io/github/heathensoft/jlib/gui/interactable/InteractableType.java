package io.github.heathensoft.jlib.gui.interactable;

import io.github.heathensoft.jlib.common.storage.primitive.BitSet;
import io.github.heathensoft.jlib.common.utils.IDPool;


/**
 * @author Frederik Dahl
 * 21/11/2022
 */


public class InteractableType {

    private final Class<? extends Interactable> interactable_class;
    private final IDPool instance_ids;
    private final BitSet group_set;
    private final int id;

    protected InteractableType(Class<? extends Interactable> clazz, int id) {
        this.instance_ids = new IDPool();
        this.interactable_class = clazz;
        this.group_set = new BitSet();
        this.id = id;
    }

    public Class<? extends Interactable> clazz() {
        return interactable_class;
    }

    protected void add_to_group(InteractableGroup<?> group) {
        group_set.set(group.id());
    }

    protected BitSet groups() {return group_set;}

    protected int obtain_instance_id() {
        return instance_ids.obtainID();
    }

    protected void returnID(int id) {
        instance_ids.returnID(id);
    }

    public int id() {
        return id;
    }



}
