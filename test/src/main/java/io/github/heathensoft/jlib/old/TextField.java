package io.github.heathensoft.jlib.old;

import io.github.heathensoft.jlib.common.utils.Area;
import io.github.heathensoft.jlib.lwjgl.graphics.Color;
import io.github.heathensoft.jlib.lwjgl.graphics.SpriteBatch;
import io.github.heathensoft.jlib.lwjgl.graphics.TextureRegion;
import org.joml.Vector2f;
import org.joml.Vector2i;

import java.nio.charset.StandardCharsets;

/**
 * @author Frederik Dahl
 * 16/11/2022
 */


public class TextField {

    private float color;
    private Area area;
    private int padding;
    public Vector2i position;
    private byte[] characters;
    
    public TextField(String text, Area area, int padding, Color color) {
        this.color = color.toFloatBits();
        this.characters = text.getBytes(StandardCharsets.US_ASCII);
        this.position = new Vector2i();
        this.padding = padding;
        this.area = area;
    }
    
    public void render(SpriteBatch batch, DebugFont font) {
        float leading = font.leading();
        float advance = font.advance();
        int areaWidth = area.cols() - (2 * padding);
        int areaHeight = area.rows() - (2 * padding);
        Vector2f pointer = new Vector2f();
        pointer.y = padding + leading;
        pointer.x = padding;
        for (byte character : characters) {
            if (pointer.x >= areaWidth) {
                pointer.x = padding;
                pointer.y += leading;
                if (pointer.y > areaHeight)
                    break;
            }
            float x = position.x + pointer.x;
            float y = position.y - pointer.y;
            TextureRegion region = font.get(character);
            batch.draw(region, x, y, advance, leading,color,0);
            pointer.x += advance;
        }
    }
    
}
