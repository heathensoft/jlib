package io.github.heathensoft.jlib.tiles.neo.generation;

/**
 * @author Frederik Dahl
 * 12/07/2023
 */


public class MapRegion {


    public enum Temperature {
        COLD(0,"Cold"),
        TEMPERATE(1,"Temperate"),
        HOT(2,"Hot");
        public static final Temperature[] ALL = values();
        public static final String DESCRIPTOR = "Temperature";
        Temperature(int mask, String descriptor) {
            this.descriptor = descriptor;
            this.mask = mask;
        } public String descriptor;
        public int mask;
        public static Temperature get(int region) {
            return ALL[((region) & 0b11) % ALL.length];
        } public static int set(int region, Temperature temperature) {
            return (region &~ 0b11) | (temperature.mask);
        }
    }

    public enum Elevation {
        LOW(0,"Low"), // Basin
        MID(1 << 2,"Mid"),
        HIGH(1 << 3,"High");
        public static final Elevation[] ALL = values();
        public static final String DESCRIPTOR = "Elevation";
        Elevation(int mask, String descriptor) {
            this.descriptor = descriptor;
            this.mask = mask;
        } public String descriptor;
        public int mask;
        public static Elevation get(int region) {
            return ALL[((region >> 2) & 0b11) % ALL.length];
        } public static int set(int region, Elevation elevation) {
            return (region &~ 0b1100) | (elevation.mask);
        }
    }

    public enum Humidity {
        DRY(0,"Dry"),
        MODERATE(1 << 4,"Moderate"),
        WET(1 << 5,"Wet");
        public static final Humidity[] ALL = values();
        public static final String DESCRIPTOR = "Humidity";
        Humidity(int mask, String descriptor) {
            this.descriptor = descriptor;
            this.mask = mask;
        } public String descriptor;
        public int mask;
        public static Humidity get(int region) {
            return ALL[((region >> 4) & 0b11) % ALL.length];
        } public static int set(int region, Humidity humidity) {
            return (region &~ 0b110000) | (humidity.mask);
        }
    }

    public enum Tier {
        ZERO(0,"0"),
        ONE(1 << 6,"1"),
        TWO(1 << 7,"2"),
        THREE(3 << 6, "3");
        public static final Tier[] ALL = values();
        public static final String DESCRIPTOR = "Tier";
        Tier(int mask, String descriptor) {
            this.descriptor = descriptor;
            this.mask = mask;
        } public String descriptor;
        public int mask;
        public static Tier get(int region) {
            return ALL[((region >> 6) & 0b11) % ALL.length];
        } public static int set(int region, Tier tier) {
            return (region &~ 0b11000000) | (tier.mask);
        }
    }

}
