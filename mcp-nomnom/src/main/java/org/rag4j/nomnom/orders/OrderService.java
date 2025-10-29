package org.rag4j.nomnom.orders;

import org.rag4j.nomnom.orders.model.*;
import org.rag4j.nomnom.products.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service for managing orders with storage, filtering, and reporting capabilities.
 */
@Service
public class OrderService {
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    
    // In-memory storage for orders (thread-safe)
    private final Map<String, Order> orders = new ConcurrentHashMap<>();
    
    /**
     * Store a new order in the system.
     */
    public Order storeOrder(Order order) {
        logger.info("Storing order: {} for location: {}", order.orderId(), order.location());
        orders.put(order.orderId(), order);
        return order;
    }
    
    /**
     * Create and store an order from components.
     */
    public Order createAndStoreOrder(
            String orderId,
            String location,
            LocalDate deliveryDate,
            List<OrderItem> items,
            String note,
            OrderStatus status
    ) {
        Order order = Order.create(
                orderId,
                location,
                deliveryDate,
                LocalDateTime.now(),
                items,
                note,
                status
        );
        return storeOrder(order);
    }
    
    /**
     * Get an order by ID.
     */
    public Optional<Order> getOrderById(String orderId) {
        return Optional.ofNullable(orders.get(orderId));
    }
    
    /**
     * Update order status.
     */
    public Optional<Order> updateOrderStatus(String orderId, OrderStatus newStatus) {
        return getOrderById(orderId).map(existingOrder -> {
            Order updatedOrder = new Order(
                    existingOrder.orderId(),
                    existingOrder.location(),
                    existingOrder.deliveryDate(),
                    existingOrder.orderTimestamp(),
                    existingOrder.items(),
                    existingOrder.note(),
                    newStatus,
                    existingOrder.totalAmount()
            );
            orders.put(orderId, updatedOrder);
            logger.info("Updated order {} status to {}", orderId, newStatus);
            return updatedOrder;
        });
    }
    
    /**
     * Delete an order by ID.
     */
    public boolean deleteOrder(String orderId) {
        boolean removed = orders.remove(orderId) != null;
        if (removed) {
            logger.info("Deleted order: {}", orderId);
        }
        return removed;
    }
    
    // ===== LISTING AND FILTERING =====
    
    /**
     * Get all orders.
     */
    public List<Order> getAllOrders() {
        return new ArrayList<>(orders.values());
    }
    
    /**
     * Get orders sorted by date (newest first).
     */
    public List<Order> getAllOrdersSorted() {
        return orders.values().stream()
                .sorted(Comparator.comparing(Order::orderTimestamp).reversed())
                .toList();
    }
    
    /**
     * Filter orders by delivery date range.
     */
    public List<Order> getOrdersByDateRange(LocalDate startDate, LocalDate endDate) {
        logger.info("Filtering orders by date range: {} to {}", startDate, endDate);
        return orders.values().stream()
                .filter(order -> !order.deliveryDate().isBefore(startDate) 
                        && !order.deliveryDate().isAfter(endDate))
                .sorted(Comparator.comparing(Order::deliveryDate))
                .toList();
    }
    
    /**
     * Filter orders by specific delivery date.
     */
    public List<Order> getOrdersByDeliveryDate(LocalDate date) {
        return orders.values().stream()
                .filter(order -> order.deliveryDate().equals(date))
                .sorted(Comparator.comparing(Order::orderTimestamp))
                .toList();
    }
    
    /**
     * Filter orders by order date (timestamp).
     */
    public List<Order> getOrdersByOrderDate(LocalDate date) {
        return orders.values().stream()
                .filter(order -> order.orderTimestamp().toLocalDate().equals(date))
                .sorted(Comparator.comparing(Order::orderTimestamp))
                .toList();
    }
    
    /**
     * Filter orders by location.
     */
    public List<Order> getOrdersByLocation(String location) {
        return orders.values().stream()
                .filter(order -> order.location().equalsIgnoreCase(location))
                .sorted(Comparator.comparing(Order::orderTimestamp).reversed())
                .toList();
    }
    
    /**
     * Filter orders by status.
     */
    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orders.values().stream()
                .filter(order -> order.status() == status)
                .sorted(Comparator.comparing(Order::orderTimestamp).reversed())
                .toList();
    }
    
    /**
     * Filter orders containing a specific product.
     */
    public List<Order> getOrdersContainingProduct(String productId) {
        return orders.values().stream()
                .filter(order -> order.containsProduct(productId))
                .sorted(Comparator.comparing(Order::orderTimestamp).reversed())
                .toList();
    }
    
    /**
     * Filter orders containing products from a specific category.
     */
    public List<Order> getOrdersContainingCategory(String categoryName) {
        return orders.values().stream()
                .filter(order -> order.containsCategory(categoryName))
                .sorted(Comparator.comparing(Order::orderTimestamp).reversed())
                .toList();
    }
    
    /**
     * Get orders with minimum total amount.
     */
    public List<Order> getOrdersWithMinAmount(double minAmount) {
        return orders.values().stream()
                .filter(order -> order.totalAmount() >= minAmount)
                .sorted(Comparator.comparing(Order::totalAmount).reversed())
                .toList();
    }
    
    // ===== REPORTING AND ANALYTICS =====
    
    /**
     * Generate comprehensive summary report for all orders.
     */
    public OrderSummaryReport generateSummaryReport() {
        return generateSummaryReport(getAllOrders());
    }
    
    /**
     * Generate summary report for a specific date range.
     */
    public OrderSummaryReport generateSummaryReportForDateRange(LocalDate startDate, LocalDate endDate) {
        List<Order> filteredOrders = getOrdersByDateRange(startDate, endDate);
        return generateSummaryReport(filteredOrders, startDate, endDate);
    }
    
    /**
     * Generate summary report for a list of orders.
     */
    private OrderSummaryReport generateSummaryReport(List<Order> orderList) {
        return generateSummaryReport(orderList, null, null);
    }
    
    /**
     * Generate summary report for a list of orders with date range.
     */
    private OrderSummaryReport generateSummaryReport(List<Order> orderList, LocalDate startDate, LocalDate endDate) {
        logger.info("Generating summary report for {} orders", orderList.size());
        
        int totalOrders = orderList.size();
        
        if (totalOrders == 0) {
            return new OrderSummaryReport(
                    0, 0, 0.0, 0.0, 0.0, 
                    startDate, endDate, 
                    List.of(), List.of(),
                    new OrderSummaryReport.OrdersByStatus(0, 0, 0, 0, 0, 0)
            );
        }
        
        // Calculate totals
        int totalItemsSold = orderList.stream()
                .mapToInt(Order::getTotalItemCount)
                .sum();
        
        double totalRevenue = orderList.stream()
                .mapToDouble(Order::totalAmount)
                .sum();
        
        double averageOrderValue = totalRevenue / totalOrders;
        double averageItemsPerOrder = (double) totalItemsSold / totalOrders;
        
        // Orders by status
        OrderSummaryReport.OrdersByStatus ordersByStatus = new OrderSummaryReport.OrdersByStatus(
                (int) orderList.stream().filter(o -> o.status() == OrderStatus.PENDING).count(),
                (int) orderList.stream().filter(o -> o.status() == OrderStatus.CONFIRMED).count(),
                (int) orderList.stream().filter(o -> o.status() == OrderStatus.PROCESSING).count(),
                (int) orderList.stream().filter(o -> o.status() == OrderStatus.READY).count(),
                (int) orderList.stream().filter(o -> o.status() == OrderStatus.DELIVERED).count(),
                (int) orderList.stream().filter(o -> o.status() == OrderStatus.CANCELLED).count()
        );
        
        // Top products and category breakdown
        List<ProductSalesReport> topProducts = generateProductSalesReports(orderList)
                .stream()
                .sorted()
                .limit(10)
                .toList();
        
        List<CategorySalesReport> categoryBreakdown = generateCategorySalesReports(orderList)
                .stream()
                .sorted()
                .toList();
        
        return new OrderSummaryReport(
                totalOrders,
                totalItemsSold,
                totalRevenue,
                averageOrderValue,
                averageItemsPerOrder,
                startDate,
                endDate,
                topProducts,
                categoryBreakdown,
                ordersByStatus
        );
    }
    
    /**
     * Get most popular products across all orders.
     */
    public List<ProductSalesReport> getMostPopularProducts(int limit) {
        return generateProductSalesReports(getAllOrders())
                .stream()
                .sorted()
                .limit(limit)
                .toList();
    }
    
    /**
     * Get most popular products for a date range.
     */
    public List<ProductSalesReport> getMostPopularProductsForDateRange(
            LocalDate startDate, LocalDate endDate, int limit) {
        List<Order> filteredOrders = getOrdersByDateRange(startDate, endDate);
        return generateProductSalesReports(filteredOrders)
                .stream()
                .sorted()
                .limit(limit)
                .toList();
    }
    
    /**
     * Generate product sales reports from a list of orders.
     */
    private List<ProductSalesReport> generateProductSalesReports(List<Order> orderList) {
        // Group items by product
        Map<Product, List<OrderItem>> itemsByProduct = orderList.stream()
                .flatMap(order -> order.items().stream())
                .collect(Collectors.groupingBy(OrderItem::product));
        
        // Generate reports
        return itemsByProduct.entrySet().stream()
                .map(entry -> {
                    Product product = entry.getKey();
                    List<OrderItem> items = entry.getValue();
                    
                    int totalQuantity = items.stream()
                            .mapToInt(OrderItem::quantity)
                            .sum();
                    
                    double totalRevenue = items.stream()
                            .mapToDouble(OrderItem::getTotalPrice)
                            .sum();
                    
                    // Count distinct orders containing this product
                    Set<String> distinctOrders = orderList.stream()
                            .filter(order -> order.containsProduct(product.id()))
                            .map(Order::orderId)
                            .collect(Collectors.toSet());
                    
                    return new ProductSalesReport(
                            product,
                            totalQuantity,
                            distinctOrders.size(),
                            totalRevenue
                    );
                })
                .toList();
    }
    
    /**
     * Generate category sales reports.
     */
    public List<CategorySalesReport> getCategorySalesReports() {
        return generateCategorySalesReports(getAllOrders());
    }
    
    /**
     * Generate category sales reports for a date range.
     */
    public List<CategorySalesReport> getCategorySalesReportsForDateRange(
            LocalDate startDate, LocalDate endDate) {
        List<Order> filteredOrders = getOrdersByDateRange(startDate, endDate);
        return generateCategorySalesReports(filteredOrders);
    }
    
    /**
     * Generate category sales reports from a list of orders.
     */
    private List<CategorySalesReport> generateCategorySalesReports(List<Order> orderList) {
        // Group items by category
        Map<String, List<OrderItem>> itemsByCategory = orderList.stream()
                .flatMap(order -> order.items().stream())
                .collect(Collectors.groupingBy(item -> item.product().category().name()));
        
        // Generate reports
        return itemsByCategory.entrySet().stream()
                .map(entry -> {
                    String categoryName = entry.getKey();
                    List<OrderItem> items = entry.getValue();
                    
                    int totalQuantity = items.stream()
                            .mapToInt(OrderItem::quantity)
                            .sum();
                    
                    double totalRevenue = items.stream()
                            .mapToDouble(OrderItem::getTotalPrice)
                            .sum();
                    
                    // Count distinct orders and unique products
                    Set<String> distinctOrders = orderList.stream()
                            .filter(order -> order.containsCategory(categoryName))
                            .map(Order::orderId)
                            .collect(Collectors.toSet());
                    
                    Set<String> uniqueProducts = items.stream()
                            .map(item -> item.product().id())
                            .collect(Collectors.toSet());
                    
                    return new CategorySalesReport(
                            categoryName,
                            totalQuantity,
                            distinctOrders.size(),
                            uniqueProducts.size(),
                            totalRevenue
                    );
                })
                .sorted()
                .toList();
    }
    
    /**
     * Get total revenue for all orders.
     */
    public double getTotalRevenue() {
        return orders.values().stream()
                .mapToDouble(Order::totalAmount)
                .sum();
    }
    
    /**
     * Get total revenue for a date range.
     */
    public double getTotalRevenueForDateRange(LocalDate startDate, LocalDate endDate) {
        return getOrdersByDateRange(startDate, endDate).stream()
                .mapToDouble(Order::totalAmount)
                .sum();
    }
    
    /**
     * Get total revenue by location.
     */
    public Map<String, Double> getRevenueByLocation() {
        return orders.values().stream()
                .collect(Collectors.groupingBy(
                        Order::location,
                        Collectors.summingDouble(Order::totalAmount)
                ));
    }
    
    /**
     * Get order count by location.
     */
    public Map<String, Long> getOrderCountByLocation() {
        return orders.values().stream()
                .collect(Collectors.groupingBy(
                        Order::location,
                        Collectors.counting()
                ));
    }
    
    /**
     * Get statistics for a specific location.
     */
    public LocationStatistics getLocationStatistics(String location) {
        List<Order> locationOrders = getOrdersByLocation(location);
        
        if (locationOrders.isEmpty()) {
            return new LocationStatistics(location, 0, 0.0, 0.0, 0);
        }
        
        int orderCount = locationOrders.size();
        double totalRevenue = locationOrders.stream()
                .mapToDouble(Order::totalAmount)
                .sum();
        double averageOrderValue = totalRevenue / orderCount;
        int totalItems = locationOrders.stream()
                .mapToInt(Order::getTotalItemCount)
                .sum();
        
        return new LocationStatistics(location, orderCount, totalRevenue, averageOrderValue, totalItems);
    }
    
    /**
     * Statistics for a specific location.
     */
    public record LocationStatistics(
            String location,
            int orderCount,
            double totalRevenue,
            double averageOrderValue,
            int totalItems
    ) {}
    
    /**
     * Clear all orders (useful for testing).
     */
    public void clearAllOrders() {
        logger.warn("Clearing all orders from the system");
        orders.clear();
    }
    
    /**
     * Get order count.
     */
    public int getOrderCount() {
        return orders.size();
    }
}
