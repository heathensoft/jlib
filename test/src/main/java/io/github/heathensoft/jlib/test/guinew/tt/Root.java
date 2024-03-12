package io.github.heathensoft.jlib.test.guinew.tt;

import io.github.heathensoft.jlib.lwjgl.utils.MathLib;
import io.github.heathensoft.jlib.ui.box.*;
import io.github.heathensoft.jlib.ui.gfx.RendererGUI;
import org.joml.primitives.Rectanglef;

/**
 * @author Frederik Dahl
 * 08/03/2024
 */


public class Root extends RootContainer {


    public Root() {
        setBorderPadding(5);
        TextField textField = new TextField(300,300,4,7,20);
        textField.enableEditing(true);

        TextField textField2 = new TextField(300,300,4,7,20);
        textField2.text().set("Nam nec magna convallis, lacinia felis id, vestibulum neque. Nunc vitae sem in lectus lobortis laoreet. Donec vehicula, turpis et molestie interdum, purus ipsum vulputate sapien, eu venenatis eros elit lobortis leo. Suspendisse et erat et arcu eleifend molestie tincidunt sit amet ante. Quisque non porttitor massa. Nam eget efficitur turpis. Pellentesque suscipit malesuada lectus, in molestie ipsum vulputate non. Phasellus nec quam faucibus, fringilla ipsum quis, mattis risus. Etiam gravida ligula ac arcu pellentesque, eu lacinia lorem finibus. Duis aliquam risus sit amet aliquam lacinia. Maecenas sollicitudin ex magna, vitae varius diam lobortis vitae. Duis pulvinar sit amet arcu fermentum mattis. Sed id nisl quis lacus bibendum vestibulum. Quisque gravida nulla eget erat vestibulum, sed efficitur velit facilisis. Proin sit amet risus gravida, gravida quam vitae, maximus erat.");
        textField2.enableWrapping(true);


        HBoxContainer hBoxContainer = new HBoxContainer();
        hBoxContainer.setInnerSpacing(3f);
        hBoxContainer.addBoxes(textField,textField2);
        VBoxContainer vBoxContainer = new VBoxContainer();
        vBoxContainer.setInnerSpacing(3);
        vBoxContainer.addBoxes(new NavBar(0xFF232323,18),hBoxContainer);
        addBox(vBoxContainer);
        build();
    }

    protected void onWindowInitContainer(BoxWindow boxWindow, BoxContainer parent) {
        interactable_id = iObtainID();
    }

    protected void onWindowOpenContainer(BoxWindow boxWindow) {

    }

    protected void onWindowCloseContainer(BoxWindow boxWindow) {

    }

    protected void onWindowPrepare(BoxWindow window, float dt) {

    }

    protected void renderContainer(BoxWindow window, RendererGUI renderer, float x, float y, float dt, int parent_id) {
        Rectanglef quad = bounds(MathLib.rectf(),x,y);
        renderer.drawElement(quad,0xFF000000,interactable_id);
        processRootInteraction(window,x,y);
    }
}
