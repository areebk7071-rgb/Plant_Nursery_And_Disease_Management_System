package com.plantmanager.model;

/**
 * Built-in plant icons and metadata for the icon picker.
 * Custom photos use imageRef prefix {@code file:} (see PlantImageService).
 */
public enum PlantIcon {

    // ── Fruit ──────────────────────────────────────────────────────────────
    FRUIT(     "builtin:fruit",       "Default Fruit 🍊",        "Fruit",     "🍊", "#40916c"),
    TOMATO(    "builtin:tomato",      "Tomato 🍅",               "Fruit",     "🍅", "#d62828"),
    APPLE(     "builtin:apple",       "Apple 🍎",                "Fruit",     "🍎", "#bc4749"),
    STRAWBERRY("builtin:strawberry",  "Strawberry 🍓",           "Fruit",     "🍓", "#c1121f"),
    MANGO(     "builtin:mango",       "Mango 🥭",                "Fruit",     "🥭", "#e9892a"),
    BANANA(    "builtin:banana",      "Banana 🍌",               "Fruit",     "🍌", "#f4d35e"),
    LEMON(     "builtin:lemon",       "Lemon 🍋",                "Fruit",     "🍋", "#f7b731"),
    PEACH(     "builtin:peach",       "Peach 🍑",                "Fruit",     "🍑", "#f08080"),
    CHERRY(    "builtin:cherry",      "Cherry 🍒",               "Fruit",     "🍒", "#9b2226"),
    WATERMELON("builtin:watermelon",  "Watermelon 🍉",           "Fruit",     "🍉", "#2d6a4f"),
    PINEAPPLE( "builtin:pineapple",   "Pineapple 🍍",            "Fruit",     "🍍", "#e9c46a"),
    PEAR(      "builtin:pear",        "Pear 🍐",                 "Fruit",     "🍐", "#90be6d"),
    KIWI(      "builtin:kiwi",        "Kiwi 🥝",                 "Fruit",     "🥝", "#4d7c0f"),
    BLUEBERRY( "builtin:blueberry",   "Blueberry 🍒",            "Fruit",     "🍒", "#4361ee"),
    MELON(     "builtin:melon",       "Melon 🍈",                "Fruit",     "🍈", "#a7c957"),

    // ── Flower ─────────────────────────────────────────────────────────────
    FLOWER(    "builtin:flower",      "Default Flower 🌸",       "Flower",    "🌸", "#e9c46a"),
    ROSE(      "builtin:rose",        "Rose 🌹",                 "Flower",    "🌹", "#e76f51"),
    SUNFLOWER( "builtin:sunflower",   "Sunflower 🌻",            "Flower",    "🌻", "#f4a261"),
    TULIP(     "builtin:tulip",       "Tulip 🌷",                "Flower",    "🌷", "#e63946"),
    HIBISCUS(  "builtin:hibiscus",    "Hibiscus 🌺",             "Flower",    "🌺", "#c1121f"),
    DAISY(     "builtin:daisy",       "Daisy 🌼",                "Flower",    "🌼", "#f7dc6f"),
    BOUQUET(   "builtin:bouquet",     "Bouquet 💐",              "Flower",    "💐", "#a29bfe"),
    BLOSSOM(   "builtin:blossom",     "Cherry Blossom 🌸",       "Flower",    "🌸", "#ffb7c5"),
    LOTUS(     "builtin:lotus",       "Lotus 🌸",                "Flower",    "🌸", "#e84393"),

    // ── Herb ───────────────────────────────────────────────────────────────
    HERB(      "builtin:herb",        "Default Herb 🌿",         "Herb",      "🌿", "#457b9d"),
    BASIL(     "builtin:basil",       "Basil 🌿",                "Herb",      "🌿", "#52b788"),
    ALOE(      "builtin:aloe",        "Aloe / Succulent 🌿",     "Herb",      "🌿", "#2d6a4f"),
    MINT(      "builtin:mint",        "Mint 🌱",                 "Herb",      "🌱", "#00b4d8"),
    SPROUT(    "builtin:sprout",      "Sprout 🌱",               "Herb",      "🌱", "#40916c"),
    SEEDLING(  "builtin:seedling",    "Seedling 🌿",             "Herb",      "🌿", "#1b4332"),
    MUSHROOM(  "builtin:mushroom",    "Mushroom 🍄",             "Herb",      "🍄", "#774936"),
    FOUR_LEAF( "builtin:fourleaf",    "Four Leaf Clover 🍀",     "Herb",      "🍀", "#388e3c"),

    // ── Vegetable ──────────────────────────────────────────────────────────
    VEGETABLE( "builtin:vegetable",   "Default Vegetable 🥕",    "Vegetable", "🥕", "#e67e22"),
    CARROT(    "builtin:carrot",      "Carrot 🥕",               "Vegetable", "🥕", "#f39c12"),
    LETTUCE(   "builtin:lettuce",     "Lettuce 🥬",              "Vegetable", "🥬", "#2ecc71"),
    BROCCOLI(  "builtin:broccoli",    "Broccoli 🥦",             "Vegetable", "🥦", "#27ae60"),
    CORN(      "builtin:corn",        "Corn 🌽",                 "Vegetable", "🌽", "#f1c40f"),
    POTATO(    "builtin:potato",      "Potato 🥔",               "Vegetable", "🥔", "#a0522d"),
    PEPPER(    "builtin:pepper",      "Pepper 🌶",               "Vegetable", "🌶", "#c0392b"),
    CUCUMBER(  "builtin:cucumber",    "Cucumber 🥒",             "Vegetable", "🥒", "#1e8449"),
    EGGPLANT(  "builtin:eggplant",    "Eggplant 🍆",             "Vegetable", "🍆", "#6c3483"),
    ONION(     "builtin:onion",       "Onion 🧅",                "Vegetable", "🧅", "#d4ac0d"),
    GARLIC(    "builtin:garlic",      "Garlic 🧄",               "Vegetable", "🧄", "#f0e6d3"),
    PEAS(      "builtin:peas",        "Peas 🥬",                 "Vegetable", "🥬", "#4caf50"),
    BEANS(     "builtin:beans",       "Beans 🌰",                "Vegetable", "🌰", "#795548"),

    // ── Tree ───────────────────────────────────────────────────────────────
    TREE(      "builtin:tree",        "Default Tree 🌳",         "Tree",      "🌳", "#27ae60"),
    OAK(       "builtin:oak",         "Oak Tree 🌳",             "Tree",      "🌳", "#1e8449"),
    PINE(      "builtin:pine",        "Pine Tree 🌲",            "Tree",      "🌲", "#1a5276"),
    PALM(      "builtin:palm",        "Palm Tree 🌴",            "Tree",      "🌴", "#f39c12"),
    BAMBOO(    "builtin:bamboo",      "Bamboo 🎋",               "Tree",      "🎋", "#52b788"),
    CACTUS(    "builtin:cactus",      "Cactus 🌵",               "Tree",      "🌵", "#2d6a4f"),
    MAPLE(     "builtin:maple",       "Maple 🍁",                "Tree",      "🍁", "#e74c3c"),
    EVERGREEN( "builtin:evergreen",   "Evergreen 🌲",            "Tree",      "🌲", "#145a32"),
    DECIDUOUS( "builtin:deciduous",   "Deciduous 🌳",            "Tree",      "🌳", "#239b56"),
    CHESTNUT(  "builtin:chestnut",    "Chestnut 🌰",             "Tree",      "🌰", "#784212"),

    // ── Vine ───────────────────────────────────────────────────────────────
    VINE(      "builtin:vine",        "Default Vine 🌿",         "Vine",      "🌿", "#8e44ad"),
    GRAPE(     "builtin:grape",       "Grape Vine 🍇",           "Vine",      "🍇", "#6c3483"),
    IVY(       "builtin:ivy",         "Ivy 🌿",                  "Vine",      "🌿", "#1abc9c"),
    PUMPKIN(   "builtin:pumpkin",     "Pumpkin 🎃",              "Vine",      "🎃", "#e67e22"),
    SQUASH(    "builtin:squash",      "Squash 🥬",               "Vine",      "🥬", "#f39c12"),
    PASSIONFLOWER("builtin:passion",  "Passion Flower 🌺",       "Vine",      "🌺", "#8e44ad"),
    CLIMBING_ROSE("builtin:climbrose","Climbing Rose 🌹",        "Vine",      "🌹", "#c0392b"),
    WISTERIA(  "builtin:wisteria",    "Wisteria 💜",             "Vine",      "💜", "#7d3c98");

    private final String key;
    private final String label;
    private final String plantType;
    private final String emoji;
    private final String color;

    PlantIcon(String key, String label, String plantType, String emoji, String color) {
        this.key = key;
        this.label = label;
        this.plantType = plantType;
        this.emoji = emoji;
        this.color = color;
    }

    public String getKey() {
        return key;
    }

    public String getLabel() {
        return label;
    }

    public String getPlantType() {
        return plantType;
    }

    public String getEmoji() {
        return emoji;
    }

    public String getColor() {
        return color;
    }

    public static PlantIcon defaultForType(String plantType) {
        return switch (plantType) {
            case "Flower" -> FLOWER;
            case "Herb" -> HERB;
            case "Vegetable" -> VEGETABLE;
            case "Tree" -> TREE;
            case "Vine" -> VINE;
            default -> FRUIT;
        };
    }

    public static PlantIcon fromKey(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }
        for (PlantIcon icon : values()) {
            if (icon.key.equals(key.trim())) {
                return icon;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return label;
    }
}
