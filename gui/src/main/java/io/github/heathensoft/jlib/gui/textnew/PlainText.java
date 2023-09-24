package io.github.heathensoft.jlib.gui.textnew;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Frederik Dahl
 * 20/09/2023
 */


public class PlainText extends ParagraphList<PlainParagraph> {

    public PlainText() {
        super();
    }

    public PlainText(int capacity) {
        super(capacity);
    }

    public PlainText(String text) {
        super(text);
    }

    public PlainText(List<PlainParagraph> list) {
        super(list);
    }

    public PlainText search(String string) { return new PlainText(matching(string)); }

    public ParsedText toParsed() {
        List<ParsedParagraph> list = new ArrayList<>(size());
        for (PlainParagraph paragraph : this) {
            list.add(TextUtils.toParsed(paragraph));
        } return new ParsedText(list);
    }

    protected PlainParagraph paragraphOf(String string) {
        return new PlainParagraph(string);
    }
}
