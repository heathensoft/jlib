package io.github.heathensoft.jlib.test.tiles;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.lwjgl.gfx.Framebuffer;
import io.github.heathensoft.jlib.lwjgl.gfx.ShaderProgram;
import io.github.heathensoft.jlib.lwjgl.gfx.Texture;
import io.github.heathensoft.jlib.lwjgl.utils.Input;
import io.github.heathensoft.jlib.lwjgl.utils.OrthographicCamera;
import io.github.heathensoft.jlib.lwjgl.utils.Resources;
import io.github.heathensoft.jlib.lwjgl.window.*;
import io.github.heathensoft.jlib.tiles.neo.Chunk;
import io.github.heathensoft.jlib.tiles.neo.Tile;

import java.util.List;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.opengl.GL11.*;

/**
 * @author Frederik Dahl
 * 17/06/2023
 */


public class TileTest extends Application {

    public static void main(String[] args) {
        Engine.get().run(new TileTest(),args);
    }
    private static final String chunk_tiles_vert_path = "res/jlib/test/tiles/glsl/chunk-tiles.vert";
    private static final String chunk_tiles_geom_path = "res/jlib/test/tiles/glsl/chunk-tiles.geom";
    private static final String chunk_tiles_frag_path = "res/jlib/test/tiles/glsl/chunk-tiles.frag";
    private static final String block_atlas_path = "res/jlib/test/tiles/img/block-atlas.png";
    private static final String terrain_texture_path = "res/jlib/test/tiles/img/terrain-rock.png";

    private static final String UNIFORM_PROJ_VIEW = "u_proj_view";
    private static final String UNIFORM_TILE_SIZE = "u_tile_size";
    private static final String UNIFORM_BLOCK_ATLAS = "u_block_atlas";
    private static final String UNIFORM_TERRAIN_TEXTURE = "u_terrain_texture";

    private static final Resolution TARGET_RESOLUTION = Resolution.R_1280x720;
    private static final float TILE_SIZE = 16;


    private int[][] tilemap = new int[16][16];

    private Chunk test_chunk;
    private Texture block_atlas;
    private Texture terrain_texture;
    private ShaderProgram tile_shader;
    private Resolution resolution;
    private OrthographicCamera camera;

    protected void engine_init(List<Resolution> supported, BootConfiguration config, String[] args) {
        supported.add(TARGET_RESOLUTION);
        config.windowed_mode = true;
        config.settings_width = TARGET_RESOLUTION.width();
        config.settings_height = TARGET_RESOLUTION.height();
        config.auto_resolution = false;
        config.vsync_enabled = true;
        config.limit_fps = false;
        config.resizable_window = true;
    }

    protected void on_start(Resolution resolution) throws Exception {
        this.resolution = resolution.cpy();
        this.camera = new OrthographicCamera();
        float cam_viewport_h = resolution.height() / (TILE_SIZE);
        float cam_viewport_w = resolution.width() / (TILE_SIZE);
        //float cam_viewport_w = cam_viewport_h * resolution.aspect_ratio();
        this.camera.viewport.set(cam_viewport_w,cam_viewport_h);
        this.camera.zoom = 0.125f;
        //this.camera.translateXY(8,8);

        this.camera.refresh();

        Input.initialize();

        Resources io = new Resources(this.getClass());

        // textures here

        block_atlas = io.image(block_atlas_path).asTexture(); // will bind the texture
        block_atlas.nearest();
        block_atlas.clampToEdge();

        terrain_texture = io.image(terrain_texture_path).asTexture(); // will bind the texture
        terrain_texture.nearest();
        terrain_texture.repeat();



        tile_shader = new ShaderProgram(
                io.asString(chunk_tiles_vert_path),
                io.asString(chunk_tiles_geom_path),
                io.asString(chunk_tiles_frag_path)
        );
        tile_shader.createUniforms(
                UNIFORM_TILE_SIZE,
                UNIFORM_PROJ_VIEW,
                UNIFORM_BLOCK_ATLAS,
                UNIFORM_TERRAIN_TEXTURE
        );
        tile_shader.use();
        tile_shader.setUniform1f(UNIFORM_TILE_SIZE,TILE_SIZE);
        tile_shader.setUniformSampler(UNIFORM_BLOCK_ATLAS,0);
        tile_shader.setUniformSampler(UNIFORM_TERRAIN_TEXTURE,1);
        block_atlas.bindToSlot(0);
        terrain_texture.bindToSlot(1);

        for (int r = 0; r < 16; r++) {
            for (int c = 0; c < 16; c++) {
                tilemap[r][c] = Tile.tile_set_block_bit(0,false);
                tilemap[r][c] = Tile.tile_set_block_type(tilemap[r][c],3);
                tilemap[r][c] = Tile.tile_set_block_damage(tilemap[r][c],15);
            }
        }
        tilemap[0][0] = Tile.tile_set_block_bit(tilemap[0][0],true);
        tilemap[1][0] = Tile.tile_set_block_bit(tilemap[1][0],true);
        tilemap[0][1] = Tile.tile_set_block_bit(tilemap[0][1],true);
        tilemap[1][1] = Tile.tile_set_block_bit(tilemap[1][1],true);
        tilemap[1][1] = Tile.tile_set_block_type(tilemap[1][1],14);

        test_chunk = new Chunk();
        test_chunk.update_blocks(tilemap,0,0);
        glDisable(GL_DEPTH_TEST);

    }

    protected void on_update(float delta) {

        Keyboard keyboard = Input.get().keyboard();
        Mouse mouse = Input.get().mouse();


        if (keyboard.pressed(GLFW_KEY_ESCAPE)) {
            Engine.get().exit();
        }

        //Vector2f mpos = new Vector2f();
        //camera.unProject(mpos.set(mouse.ndc()));
        //System.out.println(mpos.x + " , " + mpos.y);

        camera.refresh();
    }

    protected void on_render(float frame_time, float alpha) {

        Framebuffer.bindDefault();
        Framebuffer.clear();
        tile_shader.use();
        tile_shader.setUniform(UNIFORM_PROJ_VIEW,camera.combined());
        test_chunk.draw_tiles();


    }

    protected void on_exit() {
        Disposable.dispose(
                test_chunk,
                terrain_texture,
                block_atlas,
                tile_shader
        );
    }

    protected void resolution_request(Resolution resolution) throws Exception {

    }
}
