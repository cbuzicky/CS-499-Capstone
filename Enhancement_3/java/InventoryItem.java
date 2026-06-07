package com.zybooks.c499_buzicky_cheryl;

public class InventoryItem {
    private int id;
    private String name;
    private String sku;
    private int quantity;
    private String imageUrl;


    public InventoryItem(int id, String name, String sku, int quantity, String imageUrl) {
        this.id = id;
        this.name = name;
        this.sku = sku;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
    }

    // Getter methods
    public int getId() { return id; }

    public String getName() { return name; }

    public String getSku() { return sku; }
    public int getQuantity() { return quantity; }

    public String getImageUrl() {return imageUrl; }


    // Setter methods
    public void setName(String name) { this.name = name; }

}

