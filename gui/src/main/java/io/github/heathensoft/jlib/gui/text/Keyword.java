package io.github.heathensoft.jlib.gui.text;

import java.nio.charset.StandardCharsets;

/**
 * Mostly intended for gameplay logging.
 *
 * Tags:
 *   value          0v
 *   Hexadecimal	0x  (Just a bonus tag for hexadecimals)
 *   Highlighted    %   (Base Keyword object)
 *   InlineComment	#
 *   Entity	        $   (Neutral)
 *     Friendly	    $$
 *     Hostile      $$$
 *   Action	        &   (Neutral)
 *     Negative	    &&
 *     Positive	    &&&
 *
 * But the tags have no real meaning outside of coloring them.
 * So you could tag words with any.
 *
 * @author Frederik Dahl
 * 04/09/2023
 */


public class Keyword extends Word {

    protected Keyword(String string) { this(string.getBytes(StandardCharsets.US_ASCII)); }

    protected Keyword(byte[] value) {
        super(value);
    }

    public static class InlineComment extends Keyword {

        protected InlineComment(String string) {
            this(string.getBytes(StandardCharsets.US_ASCII));
        }

        protected InlineComment(byte[] value) {
            super(value);
        }
    }
    public static class Entity extends Keyword {

        protected Entity(String string) {
            this(string.getBytes(StandardCharsets.US_ASCII));
        }

        protected Entity(byte[] value) {
            super(value);
        }

        public static class Friendly extends Entity {

            protected Friendly(String string) { this(string.getBytes(StandardCharsets.US_ASCII)); }

            protected Friendly(byte[] value) { super(value); }

        } public static class Hostile extends Entity {

            protected Hostile(String string) { this(string.getBytes(StandardCharsets.US_ASCII)); }

            protected Hostile(byte[] value) { super(value); }
        }
    }
    public static class Action extends Keyword {

        protected Action(String string) {
            this(string.getBytes(StandardCharsets.US_ASCII));
        }

        protected Action(byte[] value) {
            super(value);
        }

        public static class Success extends Action {

            protected Success(String string) {
                this(string.getBytes(StandardCharsets.US_ASCII));
            }

            protected Success(byte[] value) {
                super(value);
            }
        }
        public static class Failure extends Action {

            protected Failure(String string) {
                this(string.getBytes(StandardCharsets.US_ASCII));
            }

            protected Failure(byte[] value) {
                super(value);
            }
        }
    }
    public static class Value extends Keyword {

        protected Value(String string) {
            this(string.getBytes(StandardCharsets.US_ASCII));
        }

        protected Value(byte[] value) {
            super(value);
        }

    }


}
