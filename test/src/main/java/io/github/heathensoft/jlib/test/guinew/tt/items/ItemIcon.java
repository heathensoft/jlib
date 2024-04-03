package io.github.heathensoft.jlib.test.guinew.tt.items;

import io.github.heathensoft.jlib.lwjgl.gfx.Sprite;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Frederik Dahl
 * 02/04/2024
 */


public class ItemIcon implements Comparable<ItemIcon>{

    private final Sprite sprite;
    private final String atlas_string;
    private final Type sprite_item_type;
    private final int sprite_type_index;

    public ItemIcon(Sprite sprite, String atlas_string) throws Exception {
        if (atlas_string == null || sprite == null)
            throw new RuntimeException("null arguments for ItemSprite");
        String[] split = atlas_string.split(".png");
        if (split.length != 1) throw new Exception("ItemSprite string format exception");
        split = split[0].split("_");
        if (split.length != 2) throw new Exception("ItemSprite string format exception");
        sprite_item_type = Type.typeMap.getOrDefault(split[0], Type.UNKNOWN);
        try { sprite_type_index = Integer.parseInt(split[1]);
        } catch (NumberFormatException e) {
            throw new Exception("ItemSprite string format exception");
        } this.atlas_string = atlas_string;
        this.sprite = sprite;
    }

    public Sprite sprite() { return sprite; }
    public String atlasString() { return atlas_string; }
    public Type spriteItemType() { return sprite_item_type; }
    public int spriteTypeIndex() { return sprite_type_index; }
    public int hashCode() { return (sprite_item_type.id << 16 | sprite_type_index) * 31; }
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemIcon itemIcon = (ItemIcon) o;
        int itemID = itemIcon.sprite_item_type.id << 16 | itemIcon.sprite_type_index;
        int thisID = this.sprite_item_type.id << 16 | this.sprite_type_index;
        return itemID == thisID;
    }

    public int compareTo(ItemIcon o) { ItemIcon t = this; // compare class -> type -> index
        if (t.sprite_item_type.item_class.id < o.sprite_item_type.item_class.id) { return 1;
        } else if (t.sprite_item_type.item_class.id > o.sprite_item_type.item_class.id) { return -1;
        } else if (t.sprite_item_type.id < o.sprite_item_type.id){ return 1;
        } else if (t.sprite_item_type.id > o.sprite_item_type.id){ return -1;
        } else return Integer.compare(t.sprite_type_index,o.sprite_type_index);
    }

    public enum Class {
        ACCESSORY(0,"Accessory"),
        ARMOR(1,"Armor"),
        BOOK(2,"Book"),
        CONSUMABLE(3,"Consumable"),
        MATERIAL(4,"Material"),
        MISCELLANEOUS(5,"Miscellaneous"),
        VALUABLE(6,"Valuable"),
        WEAPON(7,"Weapon");
        public final String name;
        public final int id;
        Class(int id, String name) {
            this.name = name;
            this.id = id;
        }
    }

    public enum Type {
        AMULET(0, Class.ACCESSORY,"Amulet",new String[]{"amulet"}),
        ARROW(1, Class.CONSUMABLE,"Arrow",new String[]{"arrow"}),
        AXE(2, Class.WEAPON,"Axe",new String[]{"axe"}),
        BAG(3, Class.MISCELLANEOUS,"Bag",new String[]{"bag"}),
        BELT(4, Class.ACCESSORY,"Belt",new String[]{"belt"}),
        BLOW_DART(5, Class.WEAPON,"Blow Dart",new String[]{"blowdart"}),
        BODY_ARMOR(6, Class.ARMOR,"Body Armor",new String[]{"armor"}),
        BOOK(7, Class.BOOK,"Book",new String[]{"book"}),
        BOOTS(8, Class.ARMOR,"Boots",new String[]{"shoe"}),
        BOW(9, Class.WEAPON,"Bow",new String[]{"bow"}),
        BROOM(10, Class.WEAPON,"Broom",new String[]{"broom"}),
        COINS(11, Class.VALUABLE,"Coins",new String[]{"coin","coins"}),
        CLOTH(12, Class.MATERIAL,"Cloth",new String[]{"cloth"}),
        CRYSTAL(13, Class.MATERIAL,"Crystal",new String[]{"crystal"}),
        CRYSTAL_BALL(14, Class.MISCELLANEOUS,"Crystal Ball",new String[]{"crystalball"}),
        CUP(15, Class.VALUABLE,"Cup",new String[]{"cup"}),
        DUST(16, Class.MATERIAL,"Dust",new String[]{"dust"}),
        FABRIC(17, Class.MATERIAL,"Fabric",new String[]{"fabric"}),
        GEM(18, Class.VALUABLE,"Gem",new String[]{"gem"}),
        GLOVES(19, Class.ARMOR,"Gloves",new String[]{"glove"}),
        GUN(20, Class.WEAPON,"Gun",new String[]{"gun"}),
        HAMMER(21, Class.WEAPON,"Hammer",new String[]{"hammer"}),
        HELMET(22, Class.ARMOR,"Helmet",new String[]{"helmet"}),
        INGOT(23, Class.MATERIAL,"Ingot",new String[]{"ingot"}),
        KEY(24, Class.MISCELLANEOUS,"Key",new String[]{"key"}),
        LEG_ARMOR(25, Class.ARMOR,"Leg Armor",new String[]{"leg"}),
        MAGIC_WEAPON(26, Class.WEAPON,"Magic Weapon",new String[]{"magicweapon"}),
        MISC_WEAPON(27, Class.WEAPON,"Misc Weapon",new String[]{"weapon"}),
        PLATE(28, Class.VALUABLE,"Plate",new String[]{"plate"}),
        SHIELD(29, Class.WEAPON,"Shield",new String[]{"shield"}),
        STRING(30, Class.MATERIAL,"String",new String[]{"string"}),
        SWORD(31, Class.WEAPON,"Sword",new String[]{"sword"}),
        TORCH(32, Class.WEAPON,"Torch",new String[]{"torch"}),
        POTION(33, Class.CONSUMABLE,"Potion",new String[]{"potion","vial"}),
        RING(34, Class.ACCESSORY,"Ring",new String[]{"ring"}),
        TOOL(35, Class.WEAPON,"Tool",new String[]{"tool"}),
        WOOD(36, Class.MATERIAL,"Wood",new String[]{"log","logs","plank","planks"}),
        UNKNOWN(99, Class.MISCELLANEOUS,"Unknown",new String[]{""});

        public static final Map<String, Type> typeMap = new HashMap<>((int) (values().length * 1.75));
        public final Class item_class;
        public final String[] atlas_prefix;
        public final String name;
        public final int id;
        static {
            Type[] array = values();
            for (Type type : array) {
                for (String prefix : type.atlas_prefix) {
                    typeMap.put(prefix,type);
                }
            }
        }
        Type(int id, Class itemClass, String name, String[] atlas_prefix) {
            this.atlas_prefix = atlas_prefix;
            this.item_class = itemClass;
            this.name = name;
            this.id = id;
        }
    }

    


}
