package io.github.heathensoft.jlib.test.ui;

import io.github.heathensoft.jlib.common.utils.Rand;
import io.github.heathensoft.jlib.ui.text.Paragraph;

import java.util.List;

/**
 * @author Frederik Dahl
 * 06/04/2024
 */


public class EventGenerator {

    private static final String[] hostile_entities = new String[] {"Bandit","Sword Spider","Dragon","Horse","Goblin Shaman","Ork Warrior","Sorcerer","Wyvern","Outlaw","Red Mage","Cleric","Desert Ranger","Guard","Drow","Gnoll"};
    private static final String[] friendly_entities = new String[] {"Franz","Garret","Minthara","Khalid","Eldoth Kron","Kagain","Durin","Ivan Red","Yban'de Bull","Safane","Montaron"};
    private static final String[] neutral_entities = new String[] {"Villager","Blacksmith","Commoner","Merchant","Female Slave","Guard","Knight","Nurse","Dog","Cat","Horse","Owl","Prostitute","Bouncer","Monk","Civillian","Child"};
    private static final String[] common_items = new String[] {"Shortsword","Longsword","Glaive","Ring","Potion","Spear","Crossbow","Axe","Medium Shield","String","Leather Belt","Pouch","Gem","Boar Hide","Staff","Key","Dagger"};
    private static final String[] rare_items = new String[] {"Katana","Ring of Protection","Cloak of Displacement","Longsword +1","Gladius","Scroll of Fireball","Great Axe","Flamberge","Emerald","Trident","Lightning Rod"};
    private static final String[] unique_items = new String[] {"Sword of Chaos","Vorpal Axe","Blue Dragon Plate", "Drow Full Plate +5", "Ring of Princes", "Dagger of the Stars", "Dragon Scale Shield","Abyssal Blade"};
    private static final String[] locations = new String[] { "Smallville","Shire","Nashkel","Carnival","Gnoll Stronhold","Fishing Village","Red Canyon","Temple Plaza","Undercity","Caverns","Bandit Camp","High Hedge","Cloakwood","Durlags Tower"};
    private static final String[] objects = new String[] {"Lever","Chest","Door","Table","Horse Cart","Candle","Book Shelf","Bed","Cheer","Button","Wall","Floor","Ceiling","Boat","Closet","Crate","Barrel","Shelf","Book"};
    private static final String[] resources = new String[] {"Wood","Gold Coin","Stone","Food","iron Ore","Obsidian chunk","Marble","Tiberium Crystal","Silver Coin"};
    private static final String[] damage_types = new String[] {"Fire","Physical","Cold","Elemental","Physical","Physical","Lightning","Occult","Holy","Poison"};
    private static final String[] actions = new String[] {"Searches","Drops","Throws","Pulls","Pushes","Climbs onto","Looks at","Picks Up"};


    private static final int damage_accumulator_threshold = 200;
    private static final int damage_max = 200;


    private enum EventType {
        COMBAT(9),
        EXPLORATION(1),
        GATHERING(6),
        INTERACTION(4);
        int turns;
        EventType(int turns) {
            this.turns = turns;
        }
    }


    private final Rand rng;
    private int damage_accumulator;
    private int turn;
    private boolean missed_last;
    private EventType eventType;


    public EventGenerator(int seed) {
        rng = new Rand(seed);
        eventType = randomEventType();
    }

    public void nextEvent(List<Paragraph> dst) {
        if (turn >= eventType.turns) {
            turn = 0;
            eventType = randomEventType();
        } else turn++;
        switch (eventType) {
            case COMBAT -> combatEvent(dst);
            case EXPLORATION -> explorationEvent(dst);
            case GATHERING -> gatheringEvent(dst);
            case INTERACTION -> interactionEvent(dst);
        }
    }


    private void gatheringEvent(List<Paragraph> dst) {
        if (randomBool()) dst.add(new Paragraph().appendEntityFriendly(randomElement(friendly_entities)).appendRegular("gathered").appendValue("x"+rng.next_int(6)).appendResource(randomElement(resources)));
    }

    private void interactionEvent(List<Paragraph> dst) {
        int random_event = rng.next_int(2);
        switch (random_event) {
            case 0 -> {
                String subject = randomElement(friendly_entities);
                String action = randomElement(actions);
                String object = randomElement(objects);
                Paragraph interaction = new Paragraph().appendEntityFriendly(subject).appendAction(action).appendObject(object);
                dst.add(interaction);
                if (action.equals("Searches")) {
                    Paragraph drop = new Paragraph().appendRegular("And found:");
                    int drop_amount = rng.next_int(100);
                    if (drop_amount > 90) {
                        drop.appendItemUnique(randomElement(unique_items));
                    } else if (drop_amount > 60) {
                        drop.appendItemRare(randomElement(rare_items));
                    } else  drop.appendItem(randomElement(common_items));
                    if (randomBool()) {
                        int gold_amount = rng.next_int(500);
                        drop.appendRegular("and").appendValue(gold_amount+"x Gold");
                    }
                } else if (action.equals("Throws")) {
                    if (randomBool()) {
                        object = randomElement(objects);
                        interaction.appendRegular("on").appendObject(object);
                    } else {
                        object = randomElement(hostile_entities);
                        interaction.appendRegular("on").appendEntityHostile(object);
                        dst.add(new Paragraph().appendEntityHostile(object).appendRegular("takes").appendValue(rng.next_int(50) + "").appendRegular("physical damage"));
                    }
                }
            } case 1 -> {
                String subject = randomElement(friendly_entities);
                String object = randomElement(neutral_entities);
                Paragraph interaction = new Paragraph().appendEntityFriendly(subject).appendRegular("buys");
                int drop_amount = rng.next_int(100);
                if (drop_amount > 90) {
                    interaction.appendItemUnique(randomElement(unique_items));
                } else if (drop_amount > 60) {
                    interaction.appendItemRare(randomElement(rare_items));
                } else interaction.appendItem(randomElement(common_items));
                int gold_amount = rng.next_int(500);
                interaction.appendRegular("from").appendEntity(object);
                interaction.appendRegular("for").appendValue(gold_amount+"x Gold");
            } case 2 -> {
                String subject = randomElement(friendly_entities);
                String object = randomElement(neutral_entities);
                Paragraph interaction = new Paragraph().appendEntityFriendly(subject).appendRegular("sells");
                int drop_amount = rng.next_int(100);
                if (drop_amount > 90) {
                    interaction.appendItemUnique(randomElement(unique_items));
                } else if (drop_amount > 60) {
                    interaction.appendItemRare(randomElement(rare_items));
                } else interaction.appendItem(randomElement(common_items));
                int gold_amount = rng.next_int(500);
                interaction.appendRegular("to").appendEntity(object);
                interaction.appendRegular("for").appendValue(gold_amount+"x Gold");
            }
        }
    }

    private void explorationEvent(List<Paragraph> dst) {
        int random_event = rng.next_int(3);
        Paragraph line = new Paragraph();
        switch (random_event) {
            case 0 -> line.appendRegular("The Party enters area:").appendLocation(randomElement(locations));
            case 1 -> line.appendRegular("The Party has left the area:").appendLocation(randomElement(locations));
            case 2 -> line.appendLocation(randomElement(locations)).appendRegular("is a town  with:").appendValue(rng.next_int(1000)+"").appendRegular("inhabitants");
            case 3 -> line.appendRegular("Are you sure you want to travel to:").appendLocation(randomElement(locations)+"?");
        } dst.add(line);
    }

    private void combatEvent(List<Paragraph> dst) {
        Paragraph attack = new Paragraph();
        Paragraph died = null;
        Paragraph drop = null;
        switch (rng.next_int(7)) {
            case 0 -> {     // hostile -> friendly
                String subject = randomElement(hostile_entities);
                String object = randomElement(friendly_entities);
                attack.appendEntityHostile(subject).appendAction("attacks").appendEntityFriendly(object);
                if (randomBool()) { missed_last = false;
                    int damage = randomDamage();
                    damage_accumulator += damage;
                    String damage_type = randomElement(damage_types);
                    attack.appendRegular("for").appendValue(damage + " " + damage_type).appendRegular("damage.");
                    if (damage > ( (3 * damage_max) / 4)) {
                        attack.appendSuccess("(Critical Hit)");
                    } if (damage_accumulator >= damage_accumulator_threshold) {
                        damage_accumulator -= damage_accumulator_threshold;
                        died = new Paragraph();
                        died.appendEntityFriendly(object).appendFailure("(Died)");
                    }
                } else {
                    missed_last = true;
                    attack.appendFailure(". (Miss)");
                }
            } case 1 -> {   // hostile -> neutral
                String subject = randomElement(hostile_entities);
                String object = randomElement(neutral_entities);
                attack.appendEntityHostile(subject).appendAction("attacks").appendEntity(object);
                if (randomBool()) { missed_last = false;
                    int damage = randomDamage();
                    damage_accumulator += damage;
                    String damage_type = randomElement(damage_types);
                    attack.appendRegular("for").appendValue(damage + " " + damage_type).appendRegular("damage.");
                    if (damage > ( (3 * damage_max) / 4)) {
                        attack.appendSuccess("(Critical Hit)");
                    } if (damage_accumulator >= damage_accumulator_threshold) {
                        damage_accumulator -= damage_accumulator_threshold;
                        died = new Paragraph();
                        died.appendEntity(object).appendFailure("(Died)");
                    }
                } else {
                    missed_last = true;
                    attack.appendFailure(". (Miss)");
                }

            } case 2 -> {   // hostile -> hostile
                String subject = randomElement(hostile_entities);
                String object = randomElement(hostile_entities);
                attack.appendEntityHostile(subject).appendAction("attacks").appendEntityHostile(object);
                if (randomBool()) { missed_last = false;
                    int damage = randomDamage();
                    damage_accumulator += damage;
                    String damage_type = randomElement(damage_types);
                    attack.appendRegular("for").appendValue(damage + " " + damage_type).appendRegular("damage.");
                    if (damage > ( (3 * damage_max) / 4)) {
                        attack.appendSuccess("(Critical Hit)");
                    } if (damage_accumulator >= damage_accumulator_threshold) {
                        damage_accumulator -= damage_accumulator_threshold;
                        died = new Paragraph();
                        died.appendEntityHostile(object).appendFailure("(Died)");
                    }
                } else {
                    missed_last = true;
                    attack.appendFailure(". (Miss)");
                }
            } case 3 -> {   // neutral -> hostile
                String subject = randomElement(neutral_entities);
                String object = randomElement(hostile_entities);
                attack.appendEntity(subject).appendAction("attacks").appendEntityHostile(object);
                if (randomBool()) { missed_last = false;
                    int damage = randomDamage();
                    damage_accumulator += damage;
                    String damage_type = randomElement(damage_types);
                    attack.appendRegular("for").appendValue(damage + " " + damage_type).appendRegular("damage.");
                    if (damage > ( (3 * damage_max) / 4)) {
                        attack.appendSuccess("(Critical Hit)");
                    } if (damage_accumulator >= damage_accumulator_threshold) {
                        damage_accumulator -= damage_accumulator_threshold;
                        died = new Paragraph();
                        died.appendEntityHostile(object).appendFailure("(Died)");
                    }
                } else {
                    missed_last = true;
                    attack.appendFailure(". (Miss)");
                }
            } case 4 -> {   // neutral -> neutral
                String subject = randomElement(neutral_entities);
                String object = randomElement(neutral_entities);
                attack.appendEntity(subject).appendAction("attacks").appendEntity(object);
                if (randomBool()) { missed_last = false;
                    int damage = randomDamage();
                    damage_accumulator += damage;
                    String damage_type = randomElement(damage_types);
                    attack.appendRegular("for").appendValue(damage + " " + damage_type).appendRegular("damage.");
                    if (damage > ( (3 * damage_max) / 4)) {
                        attack.appendSuccess("(Critical Hit)");
                    } if (damage_accumulator >= damage_accumulator_threshold) {
                        damage_accumulator -= damage_accumulator_threshold;
                        died = new Paragraph();
                        died.appendEntity(object).appendFailure("(Died)");
                    }
                } else {
                    missed_last = true;
                    attack.appendFailure(". (Miss)");
                }
            } case 5 -> {   // friendly -> hostile
                String subject = randomElement(friendly_entities);
                String object = randomElement(hostile_entities);
                attack.appendEntityFriendly(subject).appendAction("attacks").appendEntityHostile(object);
                if (randomBool() || missed_last) { missed_last = false;
                    int damage = randomDamage();
                    damage_accumulator += damage;
                    String damage_type = randomElement(damage_types);
                    attack.appendRegular("for").appendValue(damage + " " + damage_type).appendRegular("damage.");
                    if (damage > ( (3 * damage_max) / 4)) {
                        attack.appendSuccess("(Critical Hit)");
                    } if (damage_accumulator >= damage_accumulator_threshold) {
                        damage_accumulator -= damage_accumulator_threshold;
                        died = new Paragraph();
                        died.appendEntityHostile(object).appendFailure("(Died)");
                        if (rng.next_int(3) > 0) {
                            drop = new Paragraph();
                            drop.appendRegular("Dropped:");
                            int drop_amount = rng.next_int(100);
                            if (drop_amount > 90) {
                                drop.appendItemUnique(randomElement(unique_items));
                            } else if (drop_amount > 60) {
                                drop.appendItemRare(randomElement(rare_items));
                            } else  drop.appendItem(randomElement(common_items));
                            if (randomBool()) {
                                int gold_amount = rng.next_int(2000);
                                drop.appendRegular("and").appendValue(gold_amount+"x Gold");
                            }
                        }
                    }
                } else {
                    missed_last = true;
                    attack.appendFailure(". (Miss)");
                }
            } case 6 -> {   // friendly -> neutral
                String subject = randomElement(friendly_entities);
                String object = randomElement(neutral_entities);
                attack.appendEntityFriendly(subject).appendAction("attacks").appendEntity(object);
                if (randomBool()  || missed_last) { missed_last = false;
                    int damage = randomDamage();
                    damage_accumulator += damage;
                    String damage_type = randomElement(damage_types);
                    attack.appendRegular("for").appendValue(damage + " " + damage_type).appendRegular("damage.");
                    if (damage > ( (3 * damage_max) / 4)) {
                        attack.appendSuccess("(Critical Hit)");
                    } if (damage_accumulator >= damage_accumulator_threshold) {
                        damage_accumulator -= damage_accumulator_threshold;
                        died = new Paragraph();
                        died.appendEntity(object).appendFailure("(Died)");
                        if (rng.next_int(3) > 0) {
                            drop = new Paragraph();
                            drop.appendRegular("Dropped:");
                            int drop_amount = rng.next_int(100);
                            if (drop_amount > 90) {
                                drop.appendItemUnique(randomElement(unique_items));
                            } else if (drop_amount > 60) {
                                drop.appendItemRare(randomElement(rare_items));
                            } else  drop.appendItem(randomElement(common_items));
                            if (randomBool()) {
                                int gold_amount = rng.next_int(2000);
                                drop.appendRegular("and").appendValue(gold_amount+"x Gold");
                            }
                        }
                    }
                } else {
                    missed_last = true;
                    attack.appendFailure(". (Miss)");
                }
            } case 7 -> {   // friendly -> friendly
                String subject = randomElement(friendly_entities);
                String object = randomElement(friendly_entities);
                attack.appendEntityFriendly(subject).appendAction("attacks").appendEntityFriendly(object);
                if (randomBool() || missed_last) { missed_last = false;
                    int damage = randomDamage();
                    damage_accumulator += damage;
                    String damage_type = randomElement(damage_types);
                    attack.appendRegular("for").appendValue(damage + " " + damage_type).appendRegular("damage.");
                    if (damage > ( (3 * damage_max) / 4)) {
                        attack.appendSuccess("(Critical Hit)");
                    } if (damage_accumulator >= damage_accumulator_threshold) {
                        damage_accumulator -= damage_accumulator_threshold;
                        died = new Paragraph();
                        died.appendEntityFriendly(object).appendFailure("(Died)");
                    }
                } else attack.appendFailure(". (Miss)");
            }
            default -> throw new IllegalStateException("Unexpected value");
        }

        dst.add(attack);
        if (died != null) dst.add(died);
        if (drop != null) dst.add(drop);
    }






    private EventType randomEventType() {
        EventType[] array = EventType.values();
        return array[rng.next_int(array.length - 1)];
    }

    private boolean randomBool() { return rng.next_int(1) == 1; }

    private int randomDamage() { return rng.next_int(damage_max); }

    private String randomElement(String[] array) {
        return array[rng.next_int(array.length - 1)];
    }

}
