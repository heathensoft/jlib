package io.github.heathensoft.jlib.ui;

import io.github.heathensoft.jlib.common.utils.Area;
import io.github.heathensoft.jlib.common.utils.interactable.Interactable;
import io.github.heathensoft.jlib.lwjgl.graphics.Color;
import io.github.heathensoft.jlib.lwjgl.graphics.Sprite;
import io.github.heathensoft.jlib.lwjgl.graphics.SpriteBatch;
import io.github.heathensoft.jlib.lwjgl.graphics.TextureRegion;
import io.github.heathensoft.jlib.ui.HUD;
import io.github.heathensoft.jlib.ui.HudAssets;
import org.joml.Vector2f;

/**
 * @author Frederik Dahl
 * 19/11/2022
 */


public class UIWindow extends Interactable {

    private int id;
    public Vector2f position;
    public float content_width;
    public float content_height;
    public float border_thickness;
    public Area windowArea;
    private Color color;

    public UIWindow(Vector2f position, float cw, float ch, float border_thickness, Color color) {
        registerInteractable();
        this.position = position;
        this.color = color;
        this.content_width = cw;
        this.content_height = ch;
        this.border_thickness = border_thickness;
        this.windowArea = new Area((int) position.x,(int) (position.y - ch),(int)(position.x+cw),(int) position.y);
    }

    public void draw() {

        /*
        HUD hud = HUD.get();
        SpriteBatch batch = hud.SPRITE_BATCH;
        HudAssets assets = hud.ASSETS;
        TextureRegion blank = assets.getAsset("blank");
        float X0 = position.x;
        float Y0 = position.y - (border_thickness + content_height);
        batch.draw(blank,X0 + border_thickness, Y0 + border_thickness, content_width,content_height,color.toFloatBits(),pixelID());



         */
    }


}
