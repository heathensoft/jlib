package io.github.heathensoft.jlib.test.ui;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.lwjgl.gfx.Bitmap;
import io.github.heathensoft.jlib.lwjgl.gfx.Sprite;
import io.github.heathensoft.jlib.lwjgl.gfx.TextureAtlas;
import io.github.heathensoft.jlib.lwjgl.gfx.TextureRegion;
import io.github.heathensoft.jlib.lwjgl.utils.Resources;
import io.github.heathensoft.jlib.test.guinew.tt.DefaultRoot;
import io.github.heathensoft.jlib.ui.box.Box;
import io.github.heathensoft.jlib.ui.box.BoxWindow;
import io.github.heathensoft.jlib.ui.box.HBoxContainer;
import io.github.heathensoft.jlib.ui.box.VBoxContainer;
import io.github.heathensoft.jlib.ui.gfx.RendererGUI;

import java.util.Map;

/**
 * @author Frederik Dahl
 * 28/04/2024
 */


public class GridRoot extends DefaultRoot {

    private TextureAtlas atlas;
    private GridTest grid1, grid2;

    public GridRoot() throws Exception { super(); }

    protected Box createContent() throws Exception {
        Bitmap atlas_png = Resources.image("res/jlib/ui/atlas/pixeltier_icon_pack.png");
        String atlas_info = Resources.asString("res/jlib/ui/atlas/pixeltier_icon_pack.txt");
        atlas = new TextureAtlas(atlas_info,atlas_png);
        grid1 = new GridTest(12,10,32);
        grid1.enableMultiSelect(true);
        grid1.enableGrabbing(true);
        grid1.enableDoubleClick(true);
        Map<String, TextureRegion> map = atlas.textureRegions();
        for (var entry : map.entrySet()) {
            Sprite sprite = new Sprite(atlas.texture(0),entry.getValue());
            grid1.addItem(sprite,entry.getKey());
        } grid1.sortByNames();

        grid2 = new GridTest(12,6,32);
        grid2.enableMultiSelect(true);
        grid2.enableGrabbing(true);
        grid2.enableDoubleClick(true);

        HBoxContainer vBoxContainer = new HBoxContainer();
        vBoxContainer.setInnerSpacing(3);
        vBoxContainer.addBoxes(grid1,grid2);

        return vBoxContainer;
    }


    protected void renderContainer(BoxWindow window, RendererGUI renderer, float x, float y, float dt, int parent_id) {
        super.renderContainer(window, renderer, x, y, dt, parent_id);
        Sprite sprite = grid1.getDoubleClicked(true);
        if (sprite != null) {
            grid2.addItem(sprite,"Some_name");
        } sprite = grid2.getDoubleClicked(true);
        if (sprite != null) {
            grid1.addItem(sprite,"Some_name");

        }
    }

    public void dispose() {
        super.dispose();
        Disposable.dispose(atlas);
    }
}
