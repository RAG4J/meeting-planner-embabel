package org.rag4j.nomnom.orders.model;

/**
 * Represents the status of an order.
 */
public enum OrderStatus {
    PENDING("Pending", "Order received and awaiting processing"),
    CONFIRMED("Confirmed", "Order confirmed and being prepared"),
    PROCESSING("Processing", "Order is being processed"),
    READY("Ready", "Order is ready for delivery"),
    DELIVERED("Delivered", "Order has been delivered"),
    CANCELLED("Cancelled", "Order has been cancelled");
    
    private final String displayName;
    private final String description;
    
    OrderStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
}
