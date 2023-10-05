package io.github.heathensoft.jlib.lwjgl.gfx;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.common.io.WorkingDirectory;
import io.github.heathensoft.jlib.common.utils.RectPacker;
import io.github.heathensoft.jlib.lwjgl.utils.Resources;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * Opens a folder and creates an atlas of all it's .png images.
 *
 * @author Frederik Dahl
 * 27/07/2023
 */


public class TextureAtlas implements Disposable {

    private final Bitmap image;
    private final List<String> region_names;
    private final Map<String,TextureRegion> map;


    public TextureAtlas(String directoryPath) throws Exception {
        WorkingDirectory workingDirectory = new WorkingDirectory(directoryPath,".png");
        if (workingDirectory.validFileCount() == 0) throw new Exception("No valid images in directory: " + directoryPath);
        this.region_names = new ArrayList<>(workingDirectory.validFileCount());
        workingDirectory.getValidFiles(region_names);
        this.map = new HashMap<>((int)(region_count() * 1.75));
        Bitmap[] images = new Bitmap[region_count()];
        IntBuffer rectangles = IntBuffer.allocate(region_count() * 5);
        Resources io = new Resources();
        int channels = 0;
        for (int i = 0; i < region_count(); i++) {
            Path path = workingDirectory.resolveFile(region_names.get(i));
            ByteBuffer image_data = io.toBufferExternal(path);
            images[i] = new Bitmap(image_data);
            channels = Math.max(channels,images[i].channels());
            rectangles.put(i).put(images[i].width()).put(images[i].height());
            MemoryUtil.memFree(image_data);
        } IntBuffer bounds = IntBuffer.allocate(2);
        RectPacker.pack(rectangles.flip(),bounds);
        this.image = new Bitmap(bounds.get(),bounds.get(),channels);
        for (int i = 0; i < region_count(); i++) {
            int id = rectangles.get();
            int w = rectangles.get();
            int h = rectangles.get();
            int x = rectangles.get();
            int y = rectangles.get();
            TextureRegion region = new TextureRegion(x,y,w,h,image.width(),image.height());
            map.put(region_names.get(id),region);
            image.drawNearest(images[id],x,y,w,h,0,0,1,1);
        } Disposable.dispose(images);
    }


    public TextureRegion region(String name) {
        return map.get(name);
    }

    public List<String> region_names() {
        return region_names;
    }

    public Bitmap image() {
        return image;
    }

    public int region_count() {
        return region_names.size();
    }

    public void dispose() {
        Disposable.dispose(image);
    }
}
