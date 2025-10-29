package org.rag4j.nomnom.orders;

/**
 * Sales report for a specific category.
 */
public record CategorySalesReport(
        String categoryName,
        int totalQuantitySold,
        int numberOfOrders,
        int uniqueProducts,
        double totalRevenue
) implements Comparable<CategorySalesReport> {
    
    /**
     * Get average revenue per order.
     */
    public double getAverageRevenuePerOrder() {
        if (numberOfOrders == 0) return 0.0;
        return totalRevenue / numberOfOrders;
    }
    
    /**
     * Get percentage of total revenue.
     */
    public double getRevenuePercentage(double totalSystemRevenue) {
        if (totalSystemRevenue == 0) return 0.0;
        return (totalRevenue / totalSystemRevenue) * 100;
    }
    
    /**
     * Default comparison by total revenue (descending).
     */
    @Override
    public int compareTo(CategorySalesReport other) {
        return Double.compare(other.totalRevenue, this.totalRevenue);
    }
}
