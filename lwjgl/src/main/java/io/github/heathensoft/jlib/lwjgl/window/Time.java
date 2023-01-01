package io.github.heathensoft.jlib.lwjgl.window;

import static java.lang.System.nanoTime;

/**
 * @author Frederik Dahl
 * 15/06/2022
 */


public class Time {
    
    private long initTime;
    private double lastFrame;
    private float timeAccumulator;
    private float frameTimeLimit;
    private int fpsCount;
    private int upsCount;
    private int fps;
    private int ups;
    
    protected Time() { this(0.25f); }
    
    protected Time(float frameTimeLimit) { this.frameTimeLimit = frameTimeLimit; }
    
    protected void init() {
        initTime = nanoTime();
        lastFrame = timeSeconds();
    }
    
    protected float frameTime() {
        double timeSeconds = timeSeconds();
        float frameTime = (float) (timeSeconds - lastFrame);
        frameTime = Math.min(frameTime, frameTimeLimit);
        lastFrame = timeSeconds;
        timeAccumulator += frameTime;
        return frameTime;
    }
    
    protected void update() {
        if (timeAccumulator > 1) {
            fps = fpsCount;
            ups = upsCount;
            fpsCount = upsCount = 0;
            timeAccumulator -= 1;
        }
    }
    
    public double timeSeconds() { return nanoTime() / 1_000_000_000.0; }
    
    public long runTime() {
        return nanoTime() - initTime; }
    
    public double runTimeSeconds() { return (nanoTime() - initTime) / 1_000_000_000.0 ; }
    
    protected void incFpsCount() { fpsCount++; }
    
    protected void incUpsCount() { upsCount++; }
    
    public int fps() {
        return fps > 0 ? fps : fpsCount;
    }
    
    public int ups() { return ups > 0 ? ups : upsCount; }
    
    protected double lastFrame() { return lastFrame; }
    
    public float frameTimeLimit() { return frameTimeLimit; }
    
    public void setFrameTimeLimit(float limit) { frameTimeLimit = limit; }
    
}
