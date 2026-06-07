package com.zybooks.c499_buzicky_cheryl;

public class InventoryTracking {

    private int trackingId;
    private String name;
    private String sku;
    private String changeType;
    private String oldValue;
    private String newValue;
    private String timestamp;

    public InventoryTracking(int trackingId, String name, String sku, String changeType, String oldValue,
                             String newValue, String timestamp) {
        this.trackingId = trackingId;
        this.name = name;
        this.sku = sku;
        this.changeType = changeType;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.timestamp = timestamp;
    }

    // Getters

    public int getTrackingId() {
        return trackingId;
    }

    public String getName() {
        return name;
    }

    public String getSku() {
        return sku;
    }

    public String getChangeType() {
        return changeType;
    }

    public String getOldValue() {
        return oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public String getTimestamp() {
        return timestamp;
    }

    // Setters

    public void setTrackingId(int trackingId) {
        this.trackingId = trackingId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public void setChangeType(String changeType) {
        this.changeType = changeType;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}

