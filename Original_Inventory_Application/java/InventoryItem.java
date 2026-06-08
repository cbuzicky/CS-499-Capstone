package com.zybooks.cs360_buzicky_cheryl;

public class InventoryItem {
    private int id;

    private String name;
    private String sku;
    private int quantity;

    public InventoryItem(int id, String name, String sku, int quantity) {
        this.id = id;
        this.name = name;
        this.sku = sku;
        this.quantity = quantity;
    }

    // Getter methods
    public int getId() { return id; }

    public String getName() { return name; }
    public String getSku() { return sku; }
    public int getQuantity() { return quantity; }

    // Setter methods
    public void setName(String name) { this.name = name; }
    public void setSku(String sku) { this.sku = sku; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}

