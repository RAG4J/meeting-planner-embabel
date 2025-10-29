package org.rag4j.nomnom.orders;

import org.rag4j.nomnom.products.Product;

/**
 * Sales report for a specific product.
 */
public record ProductSalesReport(
        Product product,
        int totalQuantitySold,
        int numberOfOrders,
        double totalRevenue
) implements Comparable<ProductSalesReport> {
    
    /**
     * Get average quantity per order.
     */
    public double getAverageQuantityPerOrder() {
        if (numberOfOrders == 0) return 0.0;
        return (double) totalQuantitySold / numberOfOrders;
    }
    
    /**
     * Get average revenue per order.
     */
    public double getAverageRevenuePerOrder() {
        if (numberOfOrders == 0) return 0.0;
        return totalRevenue / numberOfOrders;
    }
    
    /**
     * Default comparison by total quantity sold (descending).
     */
    @Override
    public int compareTo(ProductSalesReport other) {
        return Integer.compare(other.totalQuantitySold, this.totalQuantitySold);
    }
}
