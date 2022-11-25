package io.github.heathensoft.jlib.hud;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Frederik Dahl
 * 23/11/2022
 */


public abstract class Container extends Content {

    protected final List<Content> contents;
    protected final Size desiredSize;
    protected final Size currentSize;
    protected final Spacing padding;
    protected final Spacing margin;
    protected final Spacing border;

    public Container(Spacing margin, Spacing border, Spacing padding) {
        this.padding = padding;
        this.margin = margin;
        this.border = border;
        this.desiredSize = minimumSize();
        this.currentSize = new Size(desiredSize);
        this.contents = new ArrayList<>(2);
    }

    public Container(Spacing border, Spacing padding) {
        this(new Spacing(),border,padding);
    }

    public Container(Spacing padding) {
        this(new Spacing(),padding);
    }

    public Container() {
        this(new Spacing());
    }

    public boolean isEmpty() {
        return contents.isEmpty();
    }

    public int contentCount() {
        return contents.size();
    }

    protected Spacing margin() {
        return margin;
    }

    protected Spacing border() {
        return border;
    }

    protected Spacing padding() {
        return padding;
    }

    protected List<Content> contents() {
        return contents;
    }

    protected Size minimumSize() {
        return new Size(
                margin.horizontal() +
                        border.horizontal() +
                        padding.horizontal(),
                margin.vertical() +
                        border.vertical() +
                        padding.vertical()
        );
    }

    public abstract void addContent(Content content);


}
