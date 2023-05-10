package io.github.heathensoft.jlib.common;

import java.nio.ByteBuffer;

/**
 * A predictably structured object.
 * Where it's properties is all you would need to create a new Defined Object.
 * The size of its properties does not need to be a constant. It just needs
 * to reflect it's current size.
 *
 * @author Frederik Dahl
 * 09/03/2023
 */


public interface Defined {


    static int sizeOf(Defined defined) {
        return defined.sizeOfProperties();
    }

    static int sizeOf(Defined ...defined) {
        int size = 0;
        for (Defined d : defined) size += d.sizeOfProperties();
        return size;
    }

    /**
     * Set properties of defined object.
     * The buffer argument position will move by object size
     * @param buffer the buffer to read from
     */
    void setProperties(ByteBuffer buffer);

    /**
     * Get properties of defined object.
     * The buffer argument position will move by object size
     * @param buffer the buffer to read from
     */
    void getProperties(ByteBuffer buffer);

    int sizeOfProperties();
}
