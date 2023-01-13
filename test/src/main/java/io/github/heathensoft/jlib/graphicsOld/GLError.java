package io.github.heathensoft.jlib.graphicsOld;

import org.tinylog.Logger;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30C.GL_INVALID_FRAMEBUFFER_OPERATION;

/**
 * @author Frederik Dahl
 * 30/10/2022
 */


public class GLError {
    
    
    public static void check(String bookmark) {
        Logger.debug("checking glError, {}",bookmark);
        check();
    }

    public static void check() {
        int code;
        while ((code = glGetError()) != GL_NO_ERROR) {
            String error = switch (code) {
                case GL_INVALID_ENUM                    -> "INVALID_ENUM";
                case GL_INVALID_VALUE                   -> "INVALID_VALUE";
                case GL_INVALID_OPERATION               -> "INVALID_OPERATION";
                case GL_STACK_OVERFLOW                  -> "STACK_OVERFLOW";
                case GL_STACK_UNDERFLOW                 -> "STACK_UNDERFLOW";
                case GL_OUT_OF_MEMORY                   -> "OUT_OF_MEMORY";
                case GL_INVALID_FRAMEBUFFER_OPERATION   -> "INVALID_FRAMEBUFFER_OPERATION";
                default                                 -> "ERROR CODE: " + code;
            };
            Logger.error("GL_ERROR: {}",error);
        }
    }
}
