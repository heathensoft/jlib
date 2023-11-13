package io.github.heathensoft.jlib.gui.deprecated;

/**
 * Mostly intended for gameplay logging.
 * Keywords are chained together with underscore
 *
 * Tags:
 *   Custom         0cX  (where X >= 0 <= 9)
 *   Value          0v
 *      Hexadecimal	0x
 *   Highlighted    %   (Base Keyword object)
 *   InlineComment	#
 *   Entity	        $   (Neutral)
 *     Friendly	    $$
 *     Hostile      $$$
 *   Action	        &   (Neutral)
 *     Success	    &&
 *     Failure	    &&&
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
    public static class Custom extends Keyword { // C0

        protected Custom(String string) { super(string); }

        protected Custom(byte[] value) { super(value); }

        public static class C1 extends Custom {

            protected C1(String string) { super(string); }

            protected C1(byte[] value) { super(value); }
        }

        public static class C2 extends Custom {

            protected C2(String string) { super(string); }

            protected C2(byte[] value) { super(value); }
        }

        public static class C3 extends Custom {

            protected C3(String string) { super(string); }

            protected C3(byte[] value) { super(value); }
        }

        public static class C4 extends Custom {

            protected C4(String string) { super(string); }

            protected C4(byte[] value) { super(value); }
        }

        public static class C5 extends Custom {

            protected C5(String string) { super(string); }

            protected C5(byte[] value) { super(value); }
        }

        public static class C6 extends Custom {

            protected C6(String string) { super(string); }

            protected C6(byte[] value) { super(value); }
        }

        public static class C7 extends Custom {

            protected C7(String string) { super(string); }

            protected C7(byte[] value) { super(value); }
        }

        public static class C8 extends Custom {

            protected C8(String string) { super(string); }

            protected C8(byte[] value) { super(value); }
        }

        public static class C9 extends Custom {

            protected C9(String string) { super(string); }

            protected C9(byte[] value) { super(value); }
        }


    }

}
