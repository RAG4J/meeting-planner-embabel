package org.rag4j.nomnom.products.model;

public enum Category {
    SNACKS("Snacks", "Tasty bites to keep you going."),
    DRINKS("Drinks", "Refreshing beverages to quench your thirst."),
    DINER("Diner", "Hearty meals to satisfy your hunger."),
    LUNCH("Lunch", "Delicious midday meals.");

    private final String name;
    private final String description;

    Category(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public static Category fromName(String name) {
        return  Category.valueOf(name.toUpperCase());
    }
}
