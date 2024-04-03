package io.github.heathensoft.jlib.test.guinew.tt;

import io.github.heathensoft.jlib.common.utils.Color;
import io.github.heathensoft.jlib.ui.GUI;
import io.github.heathensoft.jlib.ui.box.Box;
import io.github.heathensoft.jlib.ui.box.BoxWindow;
import io.github.heathensoft.jlib.ui.box.HBoxContainer;
import io.github.heathensoft.jlib.ui.gfx.RendererGUI;
import io.github.heathensoft.jlib.ui.text.Paragraph;
import io.github.heathensoft.jlib.ui.text.Text;
import io.github.heathensoft.jlib.ui.text.Word;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Frederik Dahl
 * 01/04/2024
 */


public class TextTest extends DefaultRoot {

    private List<Paragraph> stream;
    private int index = 0;
    private float accumulator = 0;
    private Text text;

    public TextTest() throws Exception { super(); }

    protected Box createContent() throws Exception {

        TextFieldOld textField = new TextFieldOld(400,200,4,5,22);
        textField.enableEditing(false);
        textField.lockScrollBar(true);
        text = textField.text();
        text.setCapacity(100);
        text.setListOrder(false);

        //textField.enableWrapping(true);
        textField.setFont(3);
        TextFieldOld textField2 = new TextFieldOld(200,200,4,5,22);
        textField2.text().set("Nam nec magna convallis, lacinia felis id, vestibulum neque. Nunc vitae sem in lectus lobortis laoreet. Donec vehicula, turpis et molestie interdum, purus ipsum vulputate sapien, eu venenatis eros elit lobortis leo. Suspendisse et erat et arcu eleifend molestie tincidunt sit amet ante. Quisque non porttitor massa. Nam eget efficitur turpis. Pellentesque suscipit malesuada lectus, in molestie ipsum vulputate non. Phasellus nec quam faucibus, fringilla ipsum quis, mattis risus. Etiam gravida ligula ac arcu pellentesque, eu lacinia lorem finibus. Duis aliquam risus sit amet aliquam lacinia. Maecenas sollicitudin ex magna, vitae varius diam lobortis vitae. Duis pulvinar sit amet arcu fermentum mattis. Sed id nisl quis lacus bibendum vestibulum. Quisque gravida nulla eget erat vestibulum, sed efficitur velit facilisis. Proin sit amet risus gravida, gravida quam vitae, maximus erat.");
        textField2.enableWrapping(true);
        textField2.enableEditing(true);
        textField2.setFont(3);

        HBoxContainer hBoxContainer = new HBoxContainer();
        hBoxContainer.setInnerSpacing(3f);
        hBoxContainer.addBoxes(textField,textField2);

        GUI.fonts.setColor(Word.Type.REGULAR, Color.intBits_to_rgb(0xFFAACCCC,new Vector4f()));
        String string = """
                > Task :test:App2.main()
                22:54:09.35 INFO: logger configured, welcome
                22:54:09.35 INFO: running on: Windows 10 version 10.0, amd64 platform
                22:54:09.35 INFO: java version: 17.0.4.1
                22:54:09.35 INFO: lwjgl version: 3.3.1 build 7
                22:54:09.35 INFO: reserved memory: 2120MB
                22:54:09.35 INFO: available processors: 8
                22:54:09.35 INFO: application has provided: 2 resolution options
                22:54:09.35 INFO: loading window user settings
                22:54:09.36 INFO: initializing window
                22:54:09.36 DEBUG: setting glfw error callback
                22:54:09.39 DEBUG: glfw library initialized
                22:54:09.39 DEBUG: querying for primary monitor
                22:54:09.39 DEBUG: current monitor display: 1920:1080 and hz: 50
                22:54:09.39 DEBUG: using opengl core profile for non-MAC user
                22:54:09.68 DEBUG: querying actual window and framebuffer size
                22:54:09.68 DEBUG: created window: 138,161,1280,720
                22:54:09.68 DEBUG: with a window framebuffer of res: 1280:720
                22:54:09.68 DEBUG: choosing resolution for application
                22:54:09.68 DEBUG: application resolution: 1280:720
                22:54:09.68 DEBUG: setting up callbacks
                22:54:09.72 DEBUG: opengl-context current in Thread: main
                22:54:09.72 DEBUG: initializing standard cursor objects
                22:54:09.81 INFO: opengl version: 4.4
                22:54:09.81 INFO: opengl core profile: true
                22:54:09.81 INFO: opengl client limitations:
                22:54:09.81 INFO: opengl max texture units: 32
                22:54:09.81 INFO: opengl max shader output draw buffers: 8
                22:54:09.81 INFO: opengl max uniform buffer bindings: 84
                22:54:09.81 INFO: opengl max uniform buffer block size: 65536 Bytes
                22:54:09.81 INFO: opengl max vertex attributes: 16
                22:54:09.81 INFO: starting application
                22:54:09.86 DEBUG: deserializing entry: BaiJamjuree64.png from repository
                22:54:09.86 DEBUG: deserializing entry: BaiJamjuree64.txt from repository
                22:54:09.92 DEBUG: deserializing entry: LiberationMono64.png from repository
                22:54:09.92 DEBUG: deserializing entry: LiberationMono64.txt from repository
                22:54:09.94 DEBUG: deserializing entry: Gotu64.png from repository
                22:54:09.94 DEBUG: deserializing entry: Gotu64.txt from repository
                22:54:09.96 DEBUG: deserializing entry: Play64.png from repository
                22:54:09.96 DEBUG: deserializing entry: Play64.txt from repository
                22:54:10.00 DEBUG: Texture Atlas: Extracting Sprite Data...
                22:54:10.00 DEBUG: Texture Atlas: "default-icons", Extracted 100/100 sprites, Width: 1024, Height: 1024
                22:54:10.00 DEBUG: Texture Wrap: GL_CLAMP_TO_EDGE, Min Filter: GL_LINEAR_MIPMAP_LINEAR, Mag Filter: GL_LINEAR, Srgb to linear: false
                22:54:10.00 DEBUG: Region[0]--> "sharp_brush_white_36dp": (x: 936, y: 0, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[1]--> "sharp_arrow_upward_white_36dp": (x: 864, y: 0, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[2]--> "sharp_arrow_right_white_36dp": (x: 792, y: 0, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[3]--> "sharp_arrow_left_white_36dp": (x: 720, y: 0, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[4]--> "sharp_arrow_forward_white_36dp": (x: 648, y: 0, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[5]--> "sharp_arrow_forward_ios_white_36dp": (x: 576, y: 0, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[6]--> "sharp_arrow_drop_up_white_36dp": (x: 504, y: 0, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[7]--> "sharp_arrow_drop_down_white_36dp": (x: 432, y: 0, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[8]--> "sharp_arrow_downward_white_36dp": (x: 360, y: 0, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[9]--> "sharp_arrow_back_white_36dp": (x: 288, y: 0, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[10]--> "sharp_arrow_back_ios_white_36dp": (x: 216, y: 0, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[11]--> "sharp_arrow_back_ios_new_white_36dp": (x: 144, y: 0, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[12]--> "sharp_apps_white_36dp": (x: 72, y: 0, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[13]--> "sharp_add_circle_outline_white_36dp": (x: 0, y: 0, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[14]--> "sharp_download_white_36dp": (x: 936, y: 72, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[15]--> "sharp_delete_forever_white_36dp": (x: 864, y: 72, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[16]--> "sharp_crop_white_36dp": (x: 792, y: 72, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[17]--> "sharp_create_new_folder_white_36dp": (x: 720, y: 72, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[18]--> "sharp_construction_white_36dp": (x: 648, y: 72, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[19]--> "sharp_color_lens_white_36dp": (x: 576, y: 72, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[20]--> "sharp_colorize_white_36dp": (x: 504, y: 72, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[21]--> "sharp_close_white_36dp": (x: 432, y: 72, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[22]--> "sharp_chevron_right_white_36dp": (x: 360, y: 72, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[23]--> "sharp_chevron_left_white_36dp": (x: 288, y: 72, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[24]--> "sharp_check_white_36dp": (x: 216, y: 72, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[25]--> "sharp_check_circle_outline_white_36dp": (x: 144, y: 72, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[26]--> "sharp_cached_white_36dp": (x: 72, y: 72, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[27]--> "sharp_bug_report_white_36dp": (x: 0, y: 72, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[28]--> "sharp_fullscreen_exit_white_36dp": (x: 936, y: 144, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[29]--> "sharp_format_color_fill_white_36dp": (x: 864, y: 144, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[30]--> "sharp_folder_white_36dp": (x: 792, y: 144, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[31]--> "sharp_first_page_white_36dp": (x: 720, y: 144, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[32]--> "sharp_filter_white_36dp": (x: 648, y: 144, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[33]--> "sharp_favorite_white_36dp": (x: 576, y: 144, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[34]--> "sharp_fast_rewind_white_36dp": (x: 504, y: 144, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[35]--> "sharp_fast_forward_white_36dp": (x: 432, y: 144, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[36]--> "sharp_expand_more_white_36dp": (x: 360, y: 144, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[37]--> "sharp_expand_less_white_36dp": (x: 288, y: 144, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[38]--> "sharp_equalizer_white_36dp": (x: 216, y: 144, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[39]--> "sharp_edit_white_36dp": (x: 144, y: 144, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[40]--> "sharp_edit_note_white_36dp": (x: 72, y: 144, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[41]--> "sharp_east_white_36dp": (x: 0, y: 144, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[42]--> "sharp_menu_white_36dp": (x: 936, y: 216, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[43]--> "sharp_manage_accounts_white_36dp": (x: 864, y: 216, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[44]--> "sharp_login_white_36dp": (x: 792, y: 216, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[45]--> "sharp_library_add_white_36dp": (x: 720, y: 216, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[46]--> "sharp_last_page_white_36dp": (x: 648, y: 216, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[47]--> "sharp_info_white_36dp": (x: 576, y: 216, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[48]--> "sharp_image_white_36dp": (x: 504, y: 216, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[49]--> "sharp_home_white_36dp": (x: 432, y: 216, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[50]--> "sharp_history_white_36dp": (x: 360, y: 216, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[51]--> "sharp_highlight_off_white_36dp": (x: 288, y: 216, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[52]--> "sharp_help_outline_white_36dp": (x: 216, y: 216, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[53]--> "sharp_groups_white_36dp": (x: 144, y: 216, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[54]--> "sharp_grade_white_36dp": (x: 72, y: 216, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[55]--> "sharp_fullscreen_white_36dp": (x: 0, y: 216, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[56]--> "sharp_play_arrow_white_36dp": (x: 936, y: 288, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[57]--> "sharp_photo_camera_white_36dp": (x: 864, y: 288, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[58]--> "sharp_person_white_36dp": (x: 792, y: 288, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[59]--> "sharp_person_add_white_36dp": (x: 720, y: 288, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[60]--> "sharp_pause_white_36dp": (x: 648, y: 288, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[61]--> "sharp_not_interested_white_36dp": (x: 576, y: 288, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[62]--> "sharp_notifications_white_36dp": (x: 504, y: 288, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[63]--> "sharp_note_add_white_36dp": (x: 432, y: 288, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[64]--> "sharp_north_white_36dp": (x: 360, y: 288, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[65]--> "sharp_north_west_white_36dp": (x: 288, y: 288, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[66]--> "sharp_north_east_white_36dp": (x: 216, y: 288, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[67]--> "sharp_more_vert_white_36dp": (x: 144, y: 288, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[68]--> "sharp_more_horiz_white_36dp": (x: 72, y: 288, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[69]--> "sharp_minimize_white_36dp": (x: 0, y: 288, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[70]--> "sharp_south_west_white_36dp": (x: 936, y: 360, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[71]--> "sharp_south_east_white_36dp": (x: 864, y: 360, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[72]--> "sharp_skip_previous_white_36dp": (x: 792, y: 360, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[73]--> "sharp_skip_next_white_36dp": (x: 720, y: 360, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[74]--> "sharp_shuffle_white_36dp": (x: 648, y: 360, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[75]--> "sharp_settings_white_36dp": (x: 576, y: 360, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[76]--> "sharp_search_white_36dp": (x: 504, y: 360, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[77]--> "sharp_science_white_36dp": (x: 432, y: 360, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[78]--> "sharp_remove_circle_outline_white_36dp": (x: 360, y: 360, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[79]--> "sharp_refresh_white_36dp": (x: 288, y: 360, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[80]--> "sharp_question_answer_white_36dp": (x: 216, y: 360, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[81]--> "sharp_public_white_36dp": (x: 144, y: 360, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[82]--> "sharp_psychology_white_36dp": (x: 72, y: 360, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[83]--> "sharp_power_settings_new_white_36dp": (x: 0, y: 360, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[84]--> "sharp_zoom_in_white_36dp": (x: 936, y: 432, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[85]--> "sharp_wysiwyg_white_36dp": (x: 864, y: 432, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[86]--> "sharp_west_white_36dp": (x: 792, y: 432, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[87]--> "sharp_wb_sunny_white_36dp": (x: 720, y: 432, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[88]--> "sharp_volume_up_white_36dp": (x: 648, y: 432, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[89]--> "sharp_volume_off_white_36dp": (x: 576, y: 432, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[90]--> "sharp_volume_down_white_36dp": (x: 504, y: 432, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[91]--> "sharp_visibility_white_36dp": (x: 432, y: 432, w: 72, h: 72)
                22:54:10.00 DEBUG: Region[92]--> "sharp_visibility_off_white_36dp": (x: 360, y: 432, w: 72, h: 72)
                22:54:10.01 DEBUG: Region[93]--> "sharp_upload_white_36dp": (x: 288, y: 432, w: 72, h: 72)
                22:54:10.01 DEBUG: Region[94]--> "sharp_text_snippet_white_36dp": (x: 216, y: 432, w: 72, h: 72)
                22:54:10.01 DEBUG: Region[95]--> "sharp_subdirectory_arrow_left_white_36dp": (x: 144, y: 432, w: 72, h: 72)
                22:54:10.01 DEBUG: Region[96]--> "sharp_stop_white_36dp": (x: 72, y: 432, w: 72, h: 72)
                22:54:10.01 DEBUG: Region[97]--> "sharp_south_white_36dp": (x: 0, y: 432, w: 72, h: 72)
                22:54:10.01 DEBUG: Region[98]--> "baseline_circle_white_36dp": (x: 72, y: 504, w: 60, h: 60)
                22:54:10.01 DEBUG: Region[99]--> "sharp_zoom_out_white_36dp": (x: 0, y: 504, w: 72, h: 72)
                22:54:10.01 DEBUG: Texture Atlas: Generating x1 Textures...
                22:54:10.01 DEBUG: Allocating Texture[0]: Width: 1024, Height: 1024, Channels: 4, SRGB Format: false
                22:54:10.01 DEBUG: Generating Mip Map
                22:54:10.01 INFO: Texture Atlas: "default-icons" Extraction Complete
                22:54:10.02 INFO: setting window processor: DefaultInput
                22:54:10.03 DEBUG: GUI: Registering Window: "Window"
                22:54:10.03 DEBUG: GUI: Registered Window: "Window" to group: 0
                22:54:10.03 INFO: application is running
                22:56:12.20 INFO: exiting main loop
                22:56:12.20 INFO: exiting application
                22:56:12.20 DEBUG: GUI: Disposing Window Manager...
                22:56:12.20 DEBUG: Disposing GUI Window: "Window"
                22:56:12.20 INFO: terminating window
                22:56:12.20 INFO: saving user settings
                22:56:12.21 DEBUG: freeing cursor objects
                22:56:12.21 DEBUG: clearing opengl capabilities
                22:56:12.21 DEBUG: freeing glfw input and display callbacks
                22:56:12.21 DEBUG: destroying the glfw window
                22:56:12.22 DEBUG: terminating glfw
                22:56:12.23 DEBUG: freeing error callback""";

        List<String> lines = string.trim().lines().collect(Collectors.toList());

        this.stream = new ArrayList<>(lines.size());
        for (String line : lines) {
            this.stream.add(new Paragraph(line));
        }

        return hBoxContainer;
    }


    protected void renderContainer(BoxWindow window, RendererGUI renderer, float x, float y, float dt, int parent_id) {
        super.renderContainer(window, renderer, x, y, dt, parent_id);
        accumulator += dt;
        if (accumulator >= .25f) {
            accumulator = 0;
            int length = stream.size();
            index = index % length;
            text.add(stream.get(index));
            index++;
        }
    }
}
