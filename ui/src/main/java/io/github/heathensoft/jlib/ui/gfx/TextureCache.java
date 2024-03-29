package io.github.heathensoft.jlib.ui.gfx;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.lwjgl.gfx.Texture;
import io.github.heathensoft.jlib.ui.text.Text;

import java.util.Set;

/**
 * @author Frederik Dahl
 * 26/03/2024
 */


public class TextureCache implements Disposable {

    private Set<Entry> cache;
    private Text log;
    private int size;

    public void dispose() {

    }

    private static final class Entry implements Disposable {
        private String name;
        private String path;
        private Texture texture;
        private boolean keep;
        private int reference_count;
        private int size() { return texture.width() * texture.height() * texture.depth(); }
        public void dispose() { Disposable.dispose(texture); }
    }
}
