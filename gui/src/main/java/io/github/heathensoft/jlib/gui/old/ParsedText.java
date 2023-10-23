package io.github.heathensoft.jlib.gui.old;


import java.util.LinkedList;
import java.util.List;

/**
 * @author Frederik Dahl
 * 20/09/2023
 */


public class ParsedText extends ParagraphList<ParsedParagraph> {

    public ParsedText() {
        super();
    }

    public ParsedText(int capacity) {
        super(capacity);
    }

    public ParsedText(String text) {
        super(text);
    }

    public ParsedText(List<ParsedParagraph> list) {
        super(list);
    }

    public ParsedText search(String string) {
        return new ParsedText(matching(string));
    }

    public PlainText toPlain() {
        List<PlainParagraph> list = new LinkedList<>();
        for (ParsedParagraph paragraph : this) {
            list.add(TextUtils.toPlain(paragraph));
        } return new PlainText(list);
    }

    protected ParsedParagraph paragraphOf(String string) {
        return TextUtils.stringToParsed(string);
    }
}
