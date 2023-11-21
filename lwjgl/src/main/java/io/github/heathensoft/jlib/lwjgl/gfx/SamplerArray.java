package io.github.heathensoft.jlib.lwjgl.gfx;

import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

/**
 * @author Frederik Dahl
 * 16/10/2023
 */


public class SamplerArray {

    private final Texture[] slots;
    private final int glTextureTarget;
    private final int glActiveSlotOffset;
    private final int length;

    private int prev_slot = 0;
    private int next_slot = 0;

    public SamplerArray(int length, int glTextureTarget, int glActiveSlotOffset) {
        this.glActiveSlotOffset = glActiveSlotOffset;
        this.glTextureTarget = glTextureTarget;
        this.slots = new Texture[length];
        this.length = length;
    }

    /**
     * Assigns a shader texture slot to texture
     * If the array is full it returns the array capacity (length)
     * @param texture texture to bind
     * @return assigned slot to upload to shader
     */
    public int assignSlot(Texture texture) {
        if (texture.target() != glTextureTarget)
            throw new RuntimeException("invalid texture target");
        if (next_slot == 0) {
            slots[next_slot] = texture;
            prev_slot = next_slot++;
            return prev_slot;
        } if (slots[prev_slot] == texture) return prev_slot;
        for (int slot = 0; slot < next_slot; slot++) {
            if (slots[slot] == texture) {
                prev_slot = slot;
                return prev_slot;
            }
        }
        if (next_slot == length)
            return length;
        else { slots[next_slot] = texture;
            prev_slot = next_slot++;
            return prev_slot;
        }
    }

    /**
     * Uploads the uniform and clears the array
     * @param shader used shader
     * @param uniform uniform name
     */
    public void uploadUniform(ShaderProgram shader, String uniform) {
        if (next_slot > 0) {
            try (MemoryStack stack = MemoryStack.stackPush()){
                IntBuffer buffer = stack.mallocInt(next_slot);
                for (int slot = 0; slot < next_slot; slot++) { buffer.put(slot);
                } shader.use().setUniform1iv(uniform,buffer.flip());
                for (int slot = 0; slot < next_slot; slot++) {
                    slots[slot].bindToSlot(slot + glActiveSlotOffset);
                    slots[slot] = null;
                } next_slot = prev_slot = 0;
            }
        }
    }

}
