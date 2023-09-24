package io.github.heathensoft.jlib.gui.textnew;

/**
 * Mostly intended for gameplay logging.
 *
 * Tags:
 *   value          0v
 *      Hexadecimal	0x
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
 * 19/09/2023
 */


public class Keyword extends Word {

    protected Keyword(String string) {
        super(string);
    }

    protected Keyword(byte[] value) {
        super(value);
    }

    public static class InlineComment extends Keyword {

        protected InlineComment(String string) {
            super(string);
        }

        protected InlineComment(byte[] value) {
            super(value);
        }
    }
    public static class Entity extends Keyword {

        protected Entity(String string) {
            super(string);
        }

        protected Entity(byte[] value) {
            super(value);
        }

        public static class Friendly extends Entity {

            protected Friendly(String string) {
                super(string);
            }

            protected Friendly(byte[] value) {
                super(value);
            }

        } public static class Hostile extends Entity {

            protected Hostile(String string) {
                super(string);
            }

            protected Hostile(byte[] value) {
                super(value);
            }
        }
    }
    public static class Action extends Keyword {

        protected Action(String string) {
            super(string);
        }

        protected Action(byte[] value) {
            super(value);
        }

        public static class Success extends Action {

            protected Success(String string) {
                super(string);
            }

            protected Success(byte[] value) {
                super(value);
            }
        }
        public static class Failure extends Action {

            protected Failure(String string) {
                super(string);
            }

            protected Failure(byte[] value) {
                super(value);
            }
        }
    }
    public static class Value extends Keyword {

        protected Value(String string) {
            super(string);
        }

        protected Value(byte[] value) {
            super(value);
        }

        public static class Hexadecimal extends Value {

            protected Hexadecimal(String string) {
                super(string);
            }

            protected Hexadecimal(byte[] value) {
                super(value);
            }
        }
    }

}
