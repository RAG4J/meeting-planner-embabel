package org.rag4j.nomnom.orders;

import java.time.LocalDate;
import java.util.List;

/**
 * Summary report for orders.
 */
public record OrderSummaryReport(
        int totalOrders,
        int totalItemsSold,
        double totalRevenue,
        double averageOrderValue,
        double averageItemsPerOrder,
        LocalDate periodStart,
        LocalDate periodEnd,
        List<ProductSalesReport> topProducts,
        List<CategorySalesReport> categoryBreakdown,
        OrdersByStatus ordersByStatus
) {
    
    /**
     * Get formatted summary text.
     */
    public String getSummaryText() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Order Summary Report ===\n");
        if (periodStart != null && periodEnd != null) {
            sb.append("Period: ").append(periodStart).append(" to ").append(periodEnd).append("\n");
        }
        sb.append("Total Orders: ").append(totalOrders).append("\n");
        sb.append("Total Items Sold: ").append(totalItemsSold).append("\n");
        sb.append("Total Revenue: €").append(String.format("%.2f", totalRevenue)).append("\n");
        sb.append("Average Order Value: €").append(String.format("%.2f", averageOrderValue)).append("\n");
        sb.append("Average Items per Order: ").append(String.format("%.1f", averageItemsPerOrder)).append("\n");
        
        if (ordersByStatus != null) {
            sb.append("\nOrders by Status:\n");
            sb.append("  Pending: ").append(ordersByStatus.pending()).append("\n");
            sb.append("  Confirmed: ").append(ordersByStatus.confirmed()).append("\n");
            sb.append("  Processing: ").append(ordersByStatus.processing()).append("\n");
            sb.append("  Ready: ").append(ordersByStatus.ready()).append("\n");
            sb.append("  Delivered: ").append(ordersByStatus.delivered()).append("\n");
            sb.append("  Cancelled: ").append(ordersByStatus.cancelled()).append("\n");
        }
        
        return sb.toString();
    }
    
    /**
     * Count of orders by status.
     */
    public record OrdersByStatus(
            int pending,
            int confirmed,
            int processing,
            int ready,
            int delivered,
            int cancelled
    ) {}
}
