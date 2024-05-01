package io.github.heathensoft.jlib.test.guinew.tt;


import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.common.utils.Color;
import io.github.heathensoft.jlib.common.utils.U;
import io.github.heathensoft.jlib.lwjgl.gfx.Bitmap;
import io.github.heathensoft.jlib.lwjgl.gfx.Sprite;
import io.github.heathensoft.jlib.lwjgl.gfx.Texture;
import io.github.heathensoft.jlib.lwjgl.utils.Resources;
import io.github.heathensoft.jlib.test.ui.ColorList;
import io.github.heathensoft.jlib.test.ui.ColorPickerBox;
import io.github.heathensoft.jlib.test.ui.EventGenerator;
import io.github.heathensoft.jlib.ui.box.*;
import io.github.heathensoft.jlib.ui.box.scroll.Textfield;
import io.github.heathensoft.jlib.ui.gfx.RendererGUI;
import io.github.heathensoft.jlib.ui.text.Paragraph;
import io.github.heathensoft.jlib.ui.text.Text;
import io.github.heathensoft.jlib.ui.text.Word;
import org.joml.Vector4f;
import org.joml.primitives.Rectanglef;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.GL_LINEAR_MIPMAP_LINEAR;
import static org.lwjgl.opengl.GL11.GL_NEAREST;

/**
 * @author Frederik Dahl
 * 15/04/2024
 */


public class ColorTest extends DefaultRoot {

    private EventGenerator event_generator;
    private List<Paragraph> new_events;
    //private SelectionMenu<Vector4f> selectionMenu;
    private ColorPickerBox pickerBox;
    private ColorList colorList;
    private Texture texture;
    private Text text;
    private float time_accumulator;

    public ColorTest() throws Exception { super(); }


    protected Box createContent() throws Exception {

        Bitmap bitmap = Resources.image("res/jlib/ui/img/rpg_icons_16x16_atlas.png");
        texture = bitmap.asTexture(true);
        texture.textureFilter(GL_LINEAR_MIPMAP_LINEAR,GL_NEAREST);
        texture.textureRepeat(); texture.generateMipmap(); bitmap.dispose();
        PictureBox pictureBox = new PictureBox(new Sprite(texture),200,100,6);

        text = new Text();
        text.setCapacity(100);
        text.setListOrder(false);
        Textfield textField = new Textfield(text,400,200);
        textField.setFont(3);
        textField.enableEditing(true);
        textField.lockScrollBar(true);


        colorList = new ColorList();
        for (int i = 0; i < Word.Type.count; i++) {
            colorList.addItem(Word.Type.get(i).color,Word.Type.get(i).name);
        } colorList.enableGrabbing(true);

        //selectionMenu = new SelectionMenu<>(200,200);
        //for (int i = 0; i < Word.Type.count; i++) {
        //    Word.Type type = Word.Type.get(i);
        //    selectionMenu.addItem(type.color,type.name);
        //} selectionMenu.addItem(navbar_color,"NavBar");
        //selectionMenu.addItem(border_color,"Border");
        //selectionMenu.addItem(GUI.variables.select_menu_color_button_active_default,"Button Active");
        //selectionMenu.addItem(GUI.variables.select_menu_color_button_inactive_default,"Button Inactive");
        //selectionMenu.addItem(GUI.variables.select_menu_color_text_active_default,"Text Active");
        //selectionMenu.addItem(GUI.variables.select_menu_color_text_inactive_default,"Text Inactive");

        pickerBox = new ColorPickerBox();
        VBoxContainer vBoxContainer = new VBoxContainer();
        vBoxContainer.addBoxes(pickerBox,colorList);

        HBoxContainer hBoxContainer = new HBoxContainer();
        //hBoxContainer.setInnerSpacing(3f);

        VBoxContainer boxContainer2 = new VBoxContainer();
        boxContainer2.addBoxes(textField,pictureBox);

        hBoxContainer.addBoxes(boxContainer2,vBoxContainer);


        this.event_generator = new EventGenerator(113);
        this.new_events = new ArrayList<>(4);

        return hBoxContainer;
    }


    protected void renderContainer(BoxWindow window, RendererGUI renderer, float x, float y, float dt, int parent_id) {
        super.renderContainer(window, renderer, x, y, dt, parent_id);

        Vector4f color = colorList.getLastSelected(false);
        if (color != null) pickerBox.editColor(color);

        //time_accumulator += dt;
        //if (time_accumulator >= .5f) {
        //    time_accumulator = 0;
        //    new_events.clear();
        //    event_generator.nextEvent(new_events);
        //    text.addMultiple(new_events);
        //}

    }

    private static final class PictureBox extends Box {
        private final Sprite sprite;
        private final float border_width;
        private final int border_color = Color.hex_to_intBits("7B5C5CFF");
        PictureBox(Sprite sprite, float width, float height, float border_width) {
            this.border_width = border_width;
            this.desired_height = height + border_width;
            this.desired_width = width + border_width;
            this.sprite = sprite;
        } protected void renderBox(BoxWindow window, RendererGUI renderer, float x, float y, float dt, int parent_id) {
            Rectanglef bounds = bounds(U.popRect(),x,y);
           // renderer.drawGadgetBorders(bounds,border_width,border_color,parent_id);
           // U.rectExpand(bounds,-border_width);
            renderer.drawSprite(sprite,bounds,0xFFFFFFFF,parent_id,false);
            U.pushRect();
        }
    }


    @Override
    public void dispose() {
        super.dispose();
        Disposable.dispose(texture);
    }
}
