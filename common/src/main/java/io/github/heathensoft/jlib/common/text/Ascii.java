package io.github.heathensoft.jlib.common.text;

import io.github.heathensoft.jlib.common.utils.UndoStack;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * The Ascii.Buffer is much like a StringBuilder, but for 7-bit ascii characters.
 * Used to store raw text.
 *
 * Does not allow lengths beyond Integer.MAX_VALUE. Not checking for this and the Program Will Crash.
 * That's over 2 GB of raw text.
 *
 * Made a decision. The Tokenizer re-tokenizes the entire Buffer on modification.
 * No delete or insert etc. Rebuild the entire structure.
 *
 * @author Frederik Dahl
 * 04/05/2024
 */


public class Ascii {

    public static final int TAB = 9;
    public static final int LINE_FEED = 10;
    public static final int CARRIAGE_RETURN = 13;
    public static final int BACK_SPACE = 8;
    public static final int UNDERSCORE = 95;


    public static class Editor {
        private final UndoStack<UndoObject> history;
        private final Buffer buffer;
        private int mark;
        private int cursor;
        private int cursor_desired;
        private boolean insert;
        private boolean tokenize;

        private static final class UndoObject {
            Buffer buffer;
            Type type;
            int cursor;
            enum Type {
                LINE_FEED, // start = cursor
                CHAR_STREAM, // start = cursor
                DELETE_STREAM, // start = cursor - count
                CHUNK_INSERT, // cursor = min(mark,cursor)
                CHUNK_CUT; // cursor = min(mark,cursor)
            }
        }

        public Editor() { this(0); }
        public Editor(int buffer_initial_cap) { this(null,buffer_initial_cap); }
        public Editor(String str, int buffer_padding) { this(str,buffer_padding,8); }
        public Editor(String str, int buffer_padding, int undo_cap) {
            this.buffer = str == null ? new Buffer(buffer_padding) : new Buffer(str,buffer_padding);
            this.history = new UndoStack<>(undo_cap, this::undoObject, this::redoObject);
            this.tokenize = true;
        }



        public int length() { return buffer.count; }
        public int cursor() { return cursor; }
        public void setCursor(int cursor) { this.cursor = Math.min(Math.max(0,cursor), buffer.count); }
        public int markRange() { return isMarking() ? Math.abs(cursor - mark) : 0; }
        public int markStart() { return isMarking() ? Math.min(cursor,mark) : cursor; }
        public int markEnd() { return isMarking() ? Math.max(cursor,mark) : cursor; }
        public void setMark() { mark = cursor; }
        public void releaseMark() { mark = -1; }
        public boolean isMarking() { return mark >= 0; }
        public void setTokenize(boolean recompile) { tokenize = recompile; }
        public boolean shouldTokenize() { return tokenize; }
        public int cursorHorizontalIndex() { return cursor - moveToSolInternal(cursor); }
        public void enableInsertMode(boolean enable) { insert = enable; }
        public boolean insertModeEnabled() { return insert; }
        public boolean isEmpty() { return buffer.isEmpty(); }
        public Buffer bufferCopy() { return buffer.copy(); }
        /**New Buffer Object Wrapped around the same data*/
        public Buffer bufferView() { return buffer.view(); } // use this for tokenizer
        public Buffer buffer() { return buffer; }

        public void moveCursorEOL() {
            int move = moveToEolInternal(cursor);
            if (move != cursor) { cursor = move;
                cursor_desired = cursorHorizontalIndex();
                registerMoveEdit();
            }
        }

        public void moveCursorSOL() {
            int move = moveToSolInternal(cursor);
            if (move != cursor) { cursor = move;
                cursor_desired = cursorHorizontalIndex();
                registerMoveEdit();
            }
        }

        public void moveCursorLeft() {
            int move = Math.max(cursor-1,0);
            if (move != cursor) { cursor = move;
                cursor_desired = cursorHorizontalIndex();
                registerMoveEdit();
            }
        }

        public void moveCursorRight() {
            int move = Math.min(cursor+1,buffer.count);
            if (move != cursor) { cursor = move;
                cursor_desired = cursorHorizontalIndex();
                registerMoveEdit();
            }
        }

        public void moveCursorUp() {
            if (cursor > 0) {
                int move = moveToSolInternal(cursor);
                if (move > 0) {
                    int cursor_sol = moveToSolInternal(--move);
                    int desired = cursor_desired + cursor_sol;
                    move = Math.min(move,desired);
                } cursor = move;
                registerMoveEdit();
            }
        }

        public void moveCursorDown() {
            int count = buffer.count;
            if (cursor < count) {
                int move = moveToEolInternal(cursor);
                if (move < count) {
                    int cursor_eol = moveToEolInternal(++move);
                    int desired = cursor_desired + move;
                    move = Math.min(cursor_eol,desired);
                } cursor = move;
                registerMoveEdit();
            }
        }

        public void undo() { history.undo(); }
        private void undoObject(UndoObject object) { // moving interrupt stream
            releaseMark();
            setTokenize(true);
            UndoObject.Type type = object.type;
            int len = object.buffer.count;
            switch (type) {
                case CHAR_STREAM, CHUNK_INSERT, LINE_FEED -> {
                    int start = object.cursor;
                    buffer.delete(start,start + len);
                    if (cursor > start) {
                        cursor = Math.min(Math.max(cursor - len,start),buffer.count);
                        cursor_desired = cursorHorizontalIndex();
                    }
                } case DELETE_STREAM -> {
                    int start = object.cursor - len;
                    buffer.insert(object.buffer,start);
                    if (cursor >= start) {
                        cursor = Math.min(Math.max(cursor + len,0),buffer.count);
                        cursor_desired = cursorHorizontalIndex();
                    }
                } case CHUNK_CUT -> {
                    int start = object.cursor;
                    buffer.insert(object.buffer,start);
                    if (cursor >= start) {
                        cursor = Math.min(Math.max(cursor + len,0),buffer.count);
                        cursor_desired = cursorHorizontalIndex();
                    }
                }
            }
        }

        public void redo() { history.redo(); }
        private void redoObject(UndoObject object) {
            releaseMark();
            setTokenize(true);
            UndoObject.Type type = object.type;
            int len = object.buffer.count;
            switch (type) {
                case CHAR_STREAM, CHUNK_INSERT, LINE_FEED -> {
                    int start = object.cursor;
                    buffer.insert(object.buffer,start);
                    if (cursor >= start) {
                        cursor = Math.min(Math.max(cursor + len,0),buffer.count);
                        cursor_desired = cursorHorizontalIndex();
                    }
                }
                case DELETE_STREAM -> {
                    int start = object.cursor - len;
                    buffer.delete(start,object.cursor);
                    if (cursor > start) {
                        cursor = Math.min(Math.max(cursor - len,start),buffer.count);
                        cursor_desired = cursorHorizontalIndex();
                    }
                }
                case CHUNK_CUT -> {
                    int start = object.cursor;
                    buffer.delete(start,start + len);
                    if (cursor > start) {
                        cursor = Math.min(Math.max(cursor - len,start),buffer.count);
                        cursor_desired = cursorHorizontalIndex();
                    }
                }
            }
        }

        public String cut(boolean iFormat) {
            Buffer cut;
            if (isMarking()) {
                cut = buffer.cut(cursor,mark);
                if (cut.count > 0) {
                    cursor = markStart();
                    cursor_desired = cursorHorizontalIndex();
                    registerChunkEdit(cut,cursor,false);
                    setTokenize(true);
                } releaseMark();
            } else { cut = bufferCopy();
                if (cut.count > 0) {
                    buffer.clear();
                    cursor = 0;
                    cursor_desired = 0;
                    setTokenize(true);
                    registerChunkEdit(cut,0,false); }
            } return iFormat ? cut.toString() : cut.toExternalFormat();
        }

        public String copy(boolean iFormat) {
            String str;
            if (isMarking()) {
                int start = markStart();
                int end = markEnd();
                if (iFormat) str = buffer.toString(start,end - start);
                else str = buffer.toExternalFormat(start, end - start);
                releaseMark();
            } else { if (iFormat) str = buffer.toString();
                else str = buffer.toExternalFormat();
            } return str;
        }

        public Buffer copy() {
            Buffer buffer;
            if (isMarking()) {
                buffer = this.buffer.copy(cursor,mark);
                releaseMark();
            } else { buffer = bufferCopy();
            } return buffer;
        }

        public void paste(String str) {
            if (isMarking()) {
                Buffer cut = buffer.cut(cursor,mark);
                if (cut.count > 0) {
                    cursor = markStart();
                    cursor_desired = cursorHorizontalIndex();
                    registerChunkEdit(cut,cursor,false);
                    setTokenize(true);
                } releaseMark();
            } Buffer insert = new Buffer(str);
            if (insert.count > 0) {
                buffer.insert(insert,cursor);
                registerChunkEdit(insert,cursor,true);
                cursor += insert.count;
                cursor_desired = cursorHorizontalIndex();
                setTokenize(true);
            }
        }

        public void set(String str) {
            history.dispose();
            buffer.set(str);
            releaseMark();
            cursor = Math.min(Math.max(cursor,0),buffer.count);
            cursor_desired = cursorHorizontalIndex();
            setTokenize(true);
        }

        public void clear(boolean clear_history) {
            if (clear_history) {
                history.dispose();
                if (!buffer.isEmpty()) {
                    setTokenize(true);
                    buffer.clear();
                    cursor_desired = 0;
                    cursor = 0;
                }
            } else if (!buffer.isEmpty()) {
                Buffer cut = bufferCopy();
                registerChunkEdit(cut,0,false);
                setTokenize(true);
                buffer.clear();
                cursor_desired = 0;
                cursor = 0;
            } releaseMark();
        }


        public void newLine() {
            if (isMarking()) {
                releaseMark();
            } buffer.insert(LINE_FEED,cursor);
            registerCharInsert(LINE_FEED,cursor);
            setTokenize(true);
            cursor_desired = 0;
            cursor++;
        }


        public void tabulator() {
            if (isMarking()) {
                releaseMark();
            } buffer.insert(TAB,cursor);
            registerCharInsert(TAB,cursor);
            setTokenize(true);
            cursor_desired++;
            cursor++;
        }

        public void backspace() {
            if (isMarking()) {
                Buffer cut = buffer.cut(cursor,mark);
                if (cut.count > 0) {
                    cursor = markStart();
                    cursor_desired = cursorHorizontalIndex();
                    registerChunkEdit(cut,cursor,false);
                    setTokenize(true);
                } releaseMark();
            } else if (cursor > 0) {
                int index = cursor - 1;
                int c = buffer.get(index);
                buffer.delete(index);
                registerCharDelete(c,cursor);
                cursor = index;
                cursor_desired = cursorHorizontalIndex();
                setTokenize(true);
            }
        }

        public void enterChar(int c) {
            if (isMarking()) {
                Buffer cut = buffer.cut(cursor,mark);
                if (cut.count > 0) {
                    cursor = markStart();
                    cursor_desired = cursorHorizontalIndex();
                    registerChunkEdit(cut,cursor,false);
                    setTokenize(true);
                } releaseMark();
            } if (insert) {
                if (buffer.set(cursor,c)) {
                    registerCharInsert(c,cursor);
                    setTokenize(true);
                    cursor++;
                    cursor_desired = cursorHorizontalIndex();
                }
            } else {
                int len = buffer.count;
                buffer.insert(c,cursor);
                if (len < buffer.count) {
                    registerCharInsert(c,cursor);
                    setTokenize(true);
                    cursor++;
                    cursor_desired = cursorHorizontalIndex();
                }
            }
        }

        private int moveToSolInternal(int cursor) {
            while (cursor > 0) {
                int c = buffer.value[cursor-1];
                if (c == LINE_FEED) break;
                cursor--;
            } return cursor;
        }

        private int moveToEolInternal(int cursor) {
            int count = buffer.count;
            while (cursor < count) {
                int c = buffer.value[cursor];
                if (c == LINE_FEED) break;
                cursor++;
            } return cursor;
        }

        private void registerMoveEdit() {
            // moving cursor while streaming ends the edit
            UndoObject current_edit = history.current();
            if (current_edit != null) {
                UndoObject.Type type = current_edit.type;
                if (type == UndoObject.Type.CHAR_STREAM || type == UndoObject.Type.DELETE_STREAM) {
                    history.pushCurrent();
                }
            }
        }

        private void registerChunkEdit(Buffer chunk, int cursor, boolean insert) {
            UndoObject new_edit = new UndoObject();
            new_edit.type = insert ? UndoObject.Type.CHUNK_INSERT : UndoObject.Type.CHUNK_CUT;
            new_edit.cursor = cursor;
            new_edit.buffer = chunk;
            history.newEdit(new_edit);
        }

        private void registerCharDelete(int c, int cursor) { // cursor before backspace
            UndoObject current_edit = history.current();
            if (current_edit == null || current_edit.type != UndoObject.Type.DELETE_STREAM) {
                current_edit = new UndoObject();
                current_edit.buffer = new Buffer(8);
                current_edit.type = UndoObject.Type.DELETE_STREAM;
                current_edit.cursor = cursor;
                history.newEdit(current_edit);
            }else if (current_edit.buffer.isFull()) {
                current_edit.buffer.makeRoom(8);
            } current_edit.buffer.insert(c,0);
        }

        private void registerCharInsert(int c, int cursor) { // cursor before append
            UndoObject current_edit = history.current();
            boolean line_feed = c == LINE_FEED;
            if (current_edit == null) {
                current_edit = new UndoObject();
                current_edit.cursor = cursor;
                current_edit.buffer = new Buffer(8);
                if (line_feed) current_edit.type = UndoObject.Type.LINE_FEED;
                else current_edit.type = UndoObject.Type.CHAR_STREAM;
                history.newEdit(current_edit);
            } else {
                UndoObject.Type type = current_edit.type;
                if (line_feed) {
                    if (type != UndoObject.Type.LINE_FEED) {
                        current_edit = new UndoObject();
                        current_edit.buffer = new Buffer(8);
                        current_edit.type = UndoObject.Type.LINE_FEED;
                        current_edit.cursor = cursor;
                        history.newEdit(current_edit);
                    }
                } else if (type != UndoObject.Type.CHAR_STREAM) {
                    current_edit = new UndoObject();
                    current_edit.buffer = new Buffer(8);
                    current_edit.type = UndoObject.Type.CHAR_STREAM;
                    current_edit.cursor = cursor;
                    history.newEdit(current_edit);
                }
            }
            if (current_edit.buffer.isFull()) {
                current_edit.buffer.makeRoom(8);
            } current_edit.buffer.append(c);
        }

    }

    public static final class Buffer {
        static final byte[] EMPTY_ARRAY = new byte[0];
        byte[] value;
        int count;

        private Buffer(Buffer original) {
            this.value = original.value;
            this.count = original.count;
        } public Buffer() { this(0); }
        public Buffer(int capacity) {
            if (capacity <= 0) value = EMPTY_ARRAY;
            else value = new byte[capacity];
        } public Buffer(String string) { this(string,0); }
        public Buffer(String string, int padding) {
            padding = Math.max(0,padding);
            if (string == null) {
                if (padding == 0) value = EMPTY_ARRAY;
                else value = new byte[padding];
            } else { int capacity = string.length() + padding;
                if (capacity == 0) value = EMPTY_ARRAY;
                else { value = new byte[capacity];
                    append(string);
                }
            }
        }

        public int length() { return count; }
        public int capacity() { return value.length; }
        public byte get(int index) { return value[index]; }
        public boolean isEmpty() { return count == 0; }
        public boolean isFull() { return count == value.length; }
        public Buffer clear() { count = 0; return this; }
        public Buffer set(String string) { return clear().append(string); }
        /**Returns a new buffer object wrapped around this array*/
        public Buffer view() { return new Buffer(this); }

        public Buffer append(String str) {
            if (str == null) str = "null";
            int len = str.length();
            if (len > 0) {
                ensureCapacity(count + len);
                count += put(str,count);
            } return this;
        }

        public Buffer append(Buffer buffer) {
            int len = buffer.count;
            if (len > 0) {
                ensureCapacity(count + len);
                put(buffer.value,value,count,len);
                count += len;
            } return this;
        }

        public Buffer append(int c) {
            if (isValidInternalFormat(c)) {
                if (isFull()) makeRoom(64);
                value[count++] = (byte) c;
            } return this;
        }

        public Buffer insert(String str, int off) {
            if (off == count) return append(str);
            checkArrayOffset(off,count);
            if (str == null) str = "null";
            int str_len = str.length();
            if (str_len > 0) {
                byte[] arr = new byte[str_len];
                int arr_len = stringToInternalFormat(str,arr);
                ensureCapacity(count + arr_len);
                shift(off,arr_len);
                put(arr,value,off,arr_len);
                count += arr_len;
            } return this;
        }

        public Buffer insert(Buffer buffer, int off) {
            if (off == count) return append(buffer);
            checkArrayOffset(off,count);
            int len = buffer.count;
            if (len > 0) {
                ensureCapacity(count + len);
                shift(off,len);
                put(buffer.value,value,off,len);
                count += len;
            } return this;
        }

        public Buffer insert(int c, int off) {
            if (off == count) return append(c);
            checkArrayOffset(off,count);
            if (isValidInternalFormat(c)) {
                if (isFull()) makeRoom(64);
                shift(off,1);
                value[off] = (byte) c;
                count++;
            } return this;
        }

        public Buffer delete(int index) {
            checkArrayIndex(index,count);
            shift(index + 1, -1);
            count--; return this;
        } public Buffer delete(int start, int end) {
            int count = this.count;
            if (end > count) {
                end = count;
            } checkArrayRange(start, end, count);
            int len = end - start;
            if (len > 0) {
                shift(end, -len);
                this.count = count - len;
            } return this;
        }

        public boolean set(int index, int c) {
            checkArrayIndex(index,count);
            if (isValidInternalFormat(c)) {
                value[index] = (byte) c;
                return true;
            } return false;
        }

        public Buffer replace(int start, int end, int c) {
            int count = this.count;
            if (start > end) {
                int tmp = start;
                start = end;
                end = tmp;
            } if (end > count) {
                end = count;
            } checkArrayRange(start,end,count);
            int range = end - start;
            if (isValidInternalFormat(c)) {
                int new_count = count + 1 - range;
                ensureCapacity(new_count);
                shift(end,new_count - count);
                value[start] = (byte) c;
                this.count = new_count;
            } else if (range > 0) {
                shift(end,-range);
                this.count = count - range;
            } return this;
        }

        public Buffer replace(int start, int end, String str) {
            return replace(start,end,new Buffer(str));
        } public Buffer replace(int start, int end, Buffer buffer) {
            int count = this.count;
            if (start > end) {
                int tmp = start;
                start = end;
                end = tmp;
            } if (end > count) {
                end = count;
            } checkArrayRange(start,end,count);
            int len = buffer.count;
            int range = end - start;
            int new_count = count + len - range;
            ensureCapacity(new_count);
            shift(end,new_count - count);
            put(buffer.value,value,start,len);
            this.count = new_count;
            return this;
        }

        public Buffer copy() { return copy(0,count); }
        public Buffer copy(int start, int end) {
            int count = this.count;
            if (start > end) {
                int tmp = start;
                start = end;
                end = tmp;
            } if (end > count) {
                end = count;
            } checkArrayRange(start, end, count);
            Buffer buffer;
            int len = end - start;
            if (len == 0) buffer = new Buffer();
            else { buffer = new Buffer(len);
                System.arraycopy(value,start, buffer.value,0,len);
                buffer.count = len;
            } return buffer;
        }

        public Buffer cut(int start, int end) {
            int count = this.count;
            if (start > end) {
                int tmp = start;
                start = end;
                end = tmp;
            } if (end > count) {
                end = count;
            } checkArrayRange(start, end, count);
            Buffer buffer;
            int range = end - start;
            if (range == 0) buffer = new Buffer();
            else { buffer = new Buffer(range);
                System.arraycopy(value,start,buffer.value,0,range);
                int newCount = count - range;
                shift(end,newCount - count);
                buffer.count = range;
                this.count = newCount;
            } return buffer;
        }

        public void trim() {
            if (count < value.length) {
                value = Arrays.copyOf(value, count);
            }
        }

        public void makeRoom(int num_chars) {
            ensureCapacity(count + num_chars);
        }

        public void ensureCapacity(int capacity) {
            int current = value.length;
            int growth = capacity - current;
            if (growth > 0) {
                value = Arrays.copyOf(value,capacity);
            }
        }

        public String toString(int start, int len) { // internal format (Line feed only)
            return count == 0 ? "" : new String(value,start,len);
        } public String toString() { // internal format (Line feed only)
            return count == 0 ? "" : new String(value,0,count);
        } public String toExternalFormat() {
            return count == 0 ? "" : internalFormatToExternal(value,0,count);
        } public String toExternalFormat(int start, int len) {
            return count == 0 ? "" : internalFormatToExternal(value,start,start + len);
        }

        private int put(String str, int index) { // returns num characters added
            return stringToInternalFormat(str,value,index); // convert to internal format
        } private void put(byte[] src, byte[] dst, int index, int len) {
            System.arraycopy(src, 0, dst, index, len); // src length should be count
        } private void shift(int offset, int n) {
            System.arraycopy(value, offset, value, (offset + n), (count - offset));
        }

    }

    public static class Trie {

        private static class Node {
            boolean terminal;
            Node[] children;
            Node() { children = new Node[95]; }
        }
        private Node root;


        public boolean tryInsert(String string) {
            if (string == null || string.isBlank()) return false;
            byte[] chars = string.getBytes(StandardCharsets.US_ASCII);
            for (int c : chars) if (c < 32 || c >= 127) return false; // Control check
            if (root == null) root = new Node();
            Node node = root;
            for (int c : chars) {
                int index = c - 32;
                if (node.children[index] == null) {
                    node.children[index] = new Node();
                } node = node.children[index];
            } if (node.terminal) return false;
            node.terminal = true;
            return true;
        }

        public boolean containsWord(Buffer buffer, int start, int end) {
            int len = buffer.count;
            if (root == null || len <= 0) return false;
            if (start > end) {
                int tmp = start;
                start = end;
                end = tmp;
            } if (end > len) {
                end = len;
            } checkArrayRange(start, end, len);
            Node node = root;
            byte[] chars = buffer.value;
            for (int i = start; i < end; i++) {
                int index = chars[i] - 32;
                node = node.children[index];
                if (node == null) return false;
            } return node.terminal;
        }

        public boolean containsWord(String string) {
            if (root == null || string == null || string.isBlank()) return false;
            int len = string.length();
            Node node = root;
            for (int i = 0; i < len; i++) {
                int index = string.charAt(i) - 32;
                if (index < 0 || index >= 95) return false;
                node = node.children[index];
                if (node == null) return false;
            } return node.terminal;
        }

        public boolean containsPrefix(String string) {
            if (root == null || string == null || string.isBlank()) return false;
            int len = string.length();
            Node node = root;
            for (int i = 0; i < len; i++) {
                int index = string.charAt(i) - 32;
                if (index < 0 || index >= 95) return false;
                node = node.children[index];
                if (node == null) return false;
            } return true;
        }

        public void print() {
            if (root == null) System.out.println("Empty Trie");
            else printRec(root, new Buffer(256));
        }

        private void printRec(Node node, Buffer buffer) {
            if (node.terminal) System.out.println(buffer);
            for (int i = 0; i < 95; i++) {
                if (node.children[i] != null) {
                    buffer.append((byte) (i + 32));
                    printRec(node.children[i],buffer);
                }
            }
        }


    }
    
    public static final class OutputStream extends java.io.OutputStream {
        private final Buffer buffer;
        private boolean carriage_return;
        public OutputStream(Buffer buffer) { this.buffer = buffer; }
        public void write(byte[] b, int off, int len) {
            checkArrayRange(off,off + len,b.length);
            buffer.makeRoom(len);
            for (int i = 0; i < len; i++)
                write(b[off + i]);
        } public void write(byte[] b){
            write(b,0,b.length);
        }
        public void write(int b) {
            if (buffer.isFull()) {
                buffer.makeRoom(128);
            } if (carriage_return) {
                if (b == CARRIAGE_RETURN) {
                    buffer.append(LINE_FEED);
                } else { carriage_return = false;
                    buffer.append(b);
                }
            } else {
                if (b == CARRIAGE_RETURN) {
                    carriage_return = true;
                } else buffer.append(b);
            }
        } public void flush() {
            if (carriage_return) {
                buffer.append(LINE_FEED);
                carriage_return = false;
            }
        } public void close()  { flush(); }
    }
    
    
    
    public static String internalFormatToExternal(byte[] iFormat, int start, int end) {
        checkArrayRange(start,end,iFormat.length);
        int len = end - start;
        String string;
        if (len > 0) {
            String lineSepString = System.lineSeparator();
            if (lineSepString == null || lineSepString.isEmpty()) {
                string = new String(iFormat,start,len);
            } else if (lineSepString.length() == 1) {
                char sep = lineSepString.charAt(0);
                if (sep != LINE_FEED) {
                    replaceChars(iFormat,(byte)LINE_FEED,(byte)sep);
                } string = new String(iFormat,start,len);
            } else if (lineSepString.equals("\r\n")) {
                    StringBuilder builder = new StringBuilder(len + 64);
                    for (int index = start; index < end; index++) {
                        char c = (char)iFormat[index];
                        if (c == '\n') builder.append('\r');
                        builder.append(c);
                    } string = builder.toString();
            } else string = new String(iFormat,start,len);
        } else string = "";
        return string;
    }
    
    public static void transferDirect(InputStream inputStream, Buffer buffer) throws IOException {
        try (java.io.OutputStream outputStream = new OutputStream(buffer)){
            inputStream.transferTo(outputStream);
        }
    }
    
    public static int stringToInternalFormat(String str, byte[] dst) {
        return stringToInternalFormat(str, dst, 0);
    }
    
    public static int stringToInternalFormat(String str, byte[] dst, int array_offset) {
        // returns how many characters of the string put into the array
        // Only accepts control characters: TAB and Line Feed.
        // Isolated Carriage Return Characters is converted to Line Feed
        // Carriage Return followed by Line Feed is ignored (keep LF only)
        // Extended Ascii Characters Are Ignored
        if (str == null) str = "null";
        final int str_len = str.length();
        final int arr_len = dst.length;
        checkArrayOffset(array_offset,arr_len);
        checkArrayRange(array_offset,array_offset + str_len, arr_len);
        int count = 0;
        for (int i = 0; i < str_len; i++) {
            int c = str.charAt(i);
            if (c < 0x7F) {
                if (c < 32) {
                    if (c == CARRIAGE_RETURN) {
                        if (i < (str_len - 1)) {
                            if (str.charAt(i+1) == '\n') {
                                // CR then LF, skip CR
                                continue;
                            }
                        } c = LINE_FEED; // Turn isolated CR to LF
                    } else if (!(c == LINE_FEED || c == TAB)) {
                        continue;
                    }
                }
                dst[array_offset + count] = (byte) c;
                count++;
            }
        } return count;
    }
    
    public static boolean isValidInternalFormat(int c) {
        if (c > 0 && c < 127) {
            if (c < 32) return c == LINE_FEED || c == TAB;
            return true;
        } return false;
    }
    
    public static void replaceChars(byte[] array, byte replace, byte with) {
        int len = array.length;
        if (replace != with) {
            for (int i = 0; i < len; i++) {
                if (array[i] == replace) {
                    array[i] = with;
                }
            }
        }
    }
    
    public static void checkArrayOffset(int offset, int length) {
        if (offset < 0 || offset > length) {
            throw new IndexOutOfBoundsException("offset " + offset + ", length " + length);
        }
    }
    
    public static void checkArrayIndex(int index, int length) {
        if (index < 0 || index >= length) {
            throw new IndexOutOfBoundsException("index " + index + ", length " + length);
        }
    }
    
    public static void checkArrayRange(int start, int end, int len) {
        if (start < 0 || start > end || end > len) {
            throw new IndexOutOfBoundsException(
                    "start " + start + ", end " + end + ", length " + len);
        }
    }
}
