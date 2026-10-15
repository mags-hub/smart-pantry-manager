package com.example.smartpantrymanager;

/** Model class for one ingredient in the user's pantry. */
public class PantryItem {
    private long id;
    private String name;
    private double quantity;
    private String unit;
    private String expiry; // yyyy-MM-dd or empty

    public PantryItem(long id, String name, double quantity, String unit, String expiry) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.expiry = expiry;
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public double getQuantity() { return quantity; }
    public String getUnit() { return unit; }
    public String getExpiry() { return expiry; }
}
