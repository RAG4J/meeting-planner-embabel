package org.rag4j.nomnom.orders;

import org.rag4j.nomnom.products.Product;

/**
 * Represents an item in an order.
 */
public record OrderItem(
        Product product,
        int quantity
) {
    
    public OrderItem {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
    }
    
    /**
     * Calculate the total price for this order item.
     */
    public double getTotalPrice() {
        return product.price() * quantity;
    }
}
