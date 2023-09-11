package io.github.heathensoft.jlib.gui.text;

import java.nio.charset.StandardCharsets;

/**
 * @author Frederik Dahl
 * 04/09/2023
 */


public class Keyword extends Word {

    protected Keyword(String string) {
        this(string.getBytes(StandardCharsets.US_ASCII));
    }

    protected Keyword(byte[] value) {
        super(value);
    }

    public static class Comment extends Keyword {

        protected Comment(String string) {
            this(string.getBytes(StandardCharsets.US_ASCII));
        }

        protected Comment(byte[] value) {
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
}
