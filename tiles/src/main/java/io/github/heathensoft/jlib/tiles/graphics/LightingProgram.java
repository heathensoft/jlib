package io.github.heathensoft.jlib.tiles.graphics;

import io.github.heathensoft.jlib.lwjgl.gfx.ShaderProgramOld;
import io.github.heathensoft.jlib.tiles.physics.Light2D;

/**
 * @author Frederik Dahl
 * 12/11/2022
 */


public class LightingProgram extends ShaderProgramOld {
    
    
    public LightingProgram(String vsSource, String fsSource) throws Exception {
        super(vsSource, fsSource);
    }
    
    
    public void createUniformLight2D(String name) {
        createUniform(name + ".origin");
        createUniform(name + ".color");
        createUniform(name + ".radius");
        createUniform(name + ".height");
        createUniform(name + ".intensity");
    }
    
    public void setUniform(String name, Light2D light) {
        setUniform(name + ".origin",light.origin());
        setUniform1f(name + ".color",light.color().floatBits());
        setUniform1f(name + ".radius", light.radius());
        setUniform1f(name + ".height", light.height());
        setUniform1f(name + ".intensity", light.intensity());
    }
}
