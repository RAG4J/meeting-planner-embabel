package org.rag4j.nomnom.orders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a stored order in the system.
 */
public record Order(
        String orderId,
        String location,
        LocalDate deliveryDate,
        LocalDateTime orderTimestamp,
        List<OrderItem> items,
        String note,
        OrderStatus status,
        double totalAmount
) {
    
    public Order {
        // Ensure immutability with defensive copies
        items = List.copyOf(items);
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("Order ID cannot be null or blank");
        }
        if (location == null || location.isBlank()) {
            throw new IllegalArgumentException("Location cannot be null or blank");
        }
        if (deliveryDate == null) {
            throw new IllegalArgumentException("Delivery date cannot be null");
        }
        if (orderTimestamp == null) {
            throw new IllegalArgumentException("Order timestamp cannot be null");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }
        if (status == null) {
            throw new IllegalArgumentException("Order status cannot be null");
        }
    }
    
    /**
     * Create a new order with calculated total amount.
     */
    public static Order create(
            String orderId,
            String location,
            LocalDate deliveryDate,
            LocalDateTime orderTimestamp,
            List<OrderItem> items,
            String note,
            OrderStatus status
    ) {
        double total = items.stream()
                .mapToDouble(item -> item.product().price() * item.quantity())
                .sum();
        
        return new Order(orderId, location, deliveryDate, orderTimestamp, items, note, status, total);
    }
    
    /**
     * Get the total number of items in the order.
     */
    public int getTotalItemCount() {
        return items.stream()
                .mapToInt(OrderItem::quantity)
                .sum();
    }
    
    /**
     * Check if order contains a specific product.
     */
    public boolean containsProduct(String productId) {
        return items.stream()
                .anyMatch(item -> item.product().id().equals(productId));
    }
    
    /**
     * Check if order contains a product from a specific category.
     */
    public boolean containsCategory(String categoryName) {
        return items.stream()
                .anyMatch(item -> item.product().category().name().equalsIgnoreCase(categoryName));
    }
    
    /**
     * Get formatted order summary.
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Order #").append(orderId).append("\n");
        sb.append("Location: ").append(location).append("\n");
        sb.append("Delivery: ").append(deliveryDate).append("\n");
        sb.append("Status: ").append(status).append("\n");
        sb.append("Items:\n");
        items.forEach(item -> 
            sb.append("  - ").append(item.quantity()).append(" x ")
              .append(item.product().name()).append(" (€")
              .append(String.format("%.2f", item.product().price())).append(")\n")
        );
        sb.append("Total: €").append(String.format("%.2f", totalAmount));
        if (note != null && !note.isBlank()) {
            sb.append("\nNote: ").append(note);
        }
        return sb.toString();
    }
}
