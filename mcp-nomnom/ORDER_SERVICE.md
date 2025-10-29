# OrderService Documentation

Comprehensive order management service for the NomNom Food Service with storage, filtering, and reporting capabilities.

## Overview

The `OrderService` provides a complete solution for managing food orders including:
- Thread-safe in-memory storage
- Advanced filtering and querying
- Comprehensive reporting and analytics
- Revenue tracking and statistics

## Core Components

### Models

#### Order
Main order entity storing complete order information.

**Fields:**
- `orderId` (String) - Unique order identifier
- `location` (String) - Delivery location
- `deliveryDate` (LocalDate) - Date for delivery
- `orderTimestamp` (LocalDateTime) - When order was placed
- `items` (List<OrderItem>) - List of ordered items
- `note` (String) - Optional order notes
- `status` (OrderStatus) - Current order status
- `totalAmount` (double) - Calculated total price

**Methods:**
- `getTotalItemCount()` - Total number of items
- `containsProduct(String productId)` - Check if contains product
- `containsCategory(String categoryName)` - Check if contains category
- `getSummary()` - Formatted order summary

#### OrderItem
Individual item in an order.

**Fields:**
- `product` (Product) - The ordered product
- `quantity` (int) - Quantity ordered

**Methods:**
- `getTotalPrice()` - Calculate item total

#### OrderStatus
Enum representing order lifecycle.

**Values:**
- `PENDING` - Order received, awaiting processing
- `CONFIRMED` - Order confirmed and being prepared
- `PROCESSING` - Order is being processed
- `READY` - Order ready for delivery
- `DELIVERED` - Order delivered successfully
- `CANCELLED` - Order cancelled

### Reporting Models

#### ProductSalesReport
Sales analytics for individual products.

**Fields:**
- `product` - The product
- `totalQuantitySold` - Total units sold
- `numberOfOrders` - Orders containing this product
- `totalRevenue` - Total revenue generated

**Methods:**
- `getAverageQuantityPerOrder()` - Average quantity per order
- `getAverageRevenuePerOrder()` - Average revenue per order

#### CategorySalesReport
Sales analytics by product category.

**Fields:**
- `categoryName` - Category name
- `totalQuantitySold` - Total units sold
- `numberOfOrders` - Orders in this category
- `uniqueProducts` - Number of different products
- `totalRevenue` - Total revenue

**Methods:**
- `getAverageRevenuePerOrder()` - Average revenue per order
- `getRevenuePercentage(double total)` - Percentage of total revenue

#### OrderSummaryReport
Comprehensive overview of orders.

**Fields:**
- `totalOrders` - Total number of orders
- `totalItemsSold` - Total items across all orders
- `totalRevenue` - Total revenue
- `averageOrderValue` - Average order value
- `averageItemsPerOrder` - Average items per order
- `periodStart/periodEnd` - Date range (optional)
- `topProducts` - Top selling products
- `categoryBreakdown` - Sales by category
- `ordersByStatus` - Order count by status

## Service API

### Order Management

#### Store Orders

```java
// Store a complete order
Order storeOrder(Order order)

// Create and store from components
Order createAndStoreOrder(
    String orderId,
    String location,
    LocalDate deliveryDate,
    List<OrderItem> items,
    String note,
    OrderStatus status
)
```

#### Retrieve Orders

```java
// Get order by ID
Optional<Order> getOrderById(String orderId)

// Get all orders
List<Order> getAllOrders()

// Get all orders sorted by date (newest first)
List<Order> getAllOrdersSorted()
```

#### Update Orders

```java
// Update order status
Optional<Order> updateOrderStatus(String orderId, OrderStatus newStatus)

// Delete order
boolean deleteOrder(String orderId)
```

### Filtering and Querying

#### Date-Based Filtering

```java
// Filter by date range
List<Order> getOrdersByDateRange(LocalDate startDate, LocalDate endDate)

// Filter by specific delivery date
List<Order> getOrdersByDeliveryDate(LocalDate date)

// Filter by order date (when placed)
List<Order> getOrdersByOrderDate(LocalDate date)
```

#### Location and Status Filtering

```java
// Filter by location
List<Order> getOrdersByLocation(String location)

// Filter by status
List<Order> getOrdersByStatus(OrderStatus status)
```

#### Content-Based Filtering

```java
// Orders containing specific product
List<Order> getOrdersContainingProduct(String productId)

// Orders containing category
List<Order> getOrdersContainingCategory(String categoryName)

// Orders with minimum amount
List<Order> getOrdersWithMinAmount(double minAmount)
```

### Reporting and Analytics

#### Summary Reports

```java
// Generate comprehensive report for all orders
OrderSummaryReport generateSummaryReport()

// Generate report for date range
OrderSummaryReport generateSummaryReportForDateRange(
    LocalDate startDate, 
    LocalDate endDate
)
```

**Report includes:**
- Total orders, items, and revenue
- Average order value and items per order
- Top 10 best-selling products
- Category breakdown
- Orders by status

#### Product Analytics

```java
// Get most popular products
List<ProductSalesReport> getMostPopularProducts(int limit)

// Get popular products for date range
List<ProductSalesReport> getMostPopularProductsForDateRange(
    LocalDate startDate, 
    LocalDate endDate, 
    int limit
)
```

#### Category Analytics

```java
// Get category sales reports
List<CategorySalesReport> getCategorySalesReports()

// Get category reports for date range
List<CategorySalesReport> getCategorySalesReportsForDateRange(
    LocalDate startDate, 
    LocalDate endDate
)
```

#### Revenue Analytics

```java
// Total revenue across all orders
double getTotalRevenue()

// Total revenue for date range
double getTotalRevenueForDateRange(LocalDate startDate, LocalDate endDate)

// Revenue breakdown by location
Map<String, Double> getRevenueByLocation()

// Order count by location
Map<String, Long> getOrderCountByLocation()

// Statistics for specific location
LocationStatistics getLocationStatistics(String location)
```

#### Location Statistics

```java
public record LocationStatistics(
    String location,
    int orderCount,
    double totalRevenue,
    double averageOrderValue,
    int totalItems
)
```

### Utility Methods

```java
// Get total order count
int getOrderCount()

// Clear all orders (for testing)
void clearAllOrders()
```

## Usage Examples

### Creating an Order

```java
@Autowired
private OrderService orderService;

@Autowired
private MenuService menuService;

// Create order items
List<OrderItem> items = List.of(
    new OrderItem(menuService.findProductByName("Coffee"), 10),
    new OrderItem(menuService.findProductByName("Cookie"), 5)
);

// Create and store order
Order order = orderService.createAndStoreOrder(
    UUID.randomUUID().toString(),
    "TechHub Amsterdam",
    LocalDate.now().plusDays(1),
    items,
    "Morning meeting",
    OrderStatus.PENDING
);

System.out.println("Order created: " + order.orderId());
System.out.println("Total: €" + order.totalAmount());
```

### Filtering Orders

```java
// Get all delivered orders
List<Order> delivered = orderService.getOrdersByStatus(OrderStatus.DELIVERED);

// Get orders for today
List<Order> todayOrders = orderService.getOrdersByDeliveryDate(LocalDate.now());

// Get orders for a location
List<Order> techHubOrders = orderService.getOrdersByLocation("TechHub Amsterdam");

// Get orders in date range
LocalDate start = LocalDate.now().minusDays(7);
LocalDate end = LocalDate.now();
List<Order> weekOrders = orderService.getOrdersByDateRange(start, end);

// Get high-value orders (over €100)
List<Order> highValue = orderService.getOrdersWithMinAmount(100.0);
```

### Generating Reports

```java
// Overall summary
OrderSummaryReport summary = orderService.generateSummaryReport();
System.out.println(summary.getSummaryText());

// Weekly report
LocalDate weekStart = LocalDate.now().minusDays(7);
LocalDate weekEnd = LocalDate.now();
OrderSummaryReport weeklyReport = orderService.generateSummaryReportForDateRange(
    weekStart, 
    weekEnd
);

System.out.println("Weekly Revenue: €" + weeklyReport.totalRevenue());
System.out.println("Average Order: €" + weeklyReport.averageOrderValue());

// Top products
List<ProductSalesReport> topProducts = weeklyReport.topProducts();
topProducts.forEach(report -> 
    System.out.println(report.product().name() + ": " + 
                      report.totalQuantitySold() + " units")
);
```

### Analytics Queries

```java
// Most popular products
List<ProductSalesReport> popular = orderService.getMostPopularProducts(5);
popular.forEach(report -> {
    System.out.println(report.product().name());
    System.out.println("  Sold: " + report.totalQuantitySold());
    System.out.println("  Revenue: €" + report.totalRevenue());
    System.out.println("  Orders: " + report.numberOfOrders());
});

// Category performance
List<CategorySalesReport> categories = orderService.getCategorySalesReports();
double totalRevenue = orderService.getTotalRevenue();
categories.forEach(report -> {
    System.out.println(report.categoryName());
    System.out.println("  Revenue: €" + report.totalRevenue());
    System.out.println("  Share: " + 
        String.format("%.1f%%", report.getRevenuePercentage(totalRevenue)));
});

// Location statistics
OrderService.LocationStatistics stats = 
    orderService.getLocationStatistics("TechHub Amsterdam");
System.out.println("Location: " + stats.location());
System.out.println("Orders: " + stats.orderCount());
System.out.println("Revenue: €" + stats.totalRevenue());
System.out.println("Avg Order: €" + stats.averageOrderValue());
```

### Updating Order Status

```java
// Update status
orderService.updateOrderStatus(orderId, OrderStatus.CONFIRMED);
orderService.updateOrderStatus(orderId, OrderStatus.PROCESSING);
orderService.updateOrderStatus(orderId, OrderStatus.READY);
orderService.updateOrderStatus(orderId, OrderStatus.DELIVERED);
```

## Sample Data

The service is initialized with 10 sample orders on startup via `OrderDataInitializer`:

1. **Coffee meeting** - Yesterday, TechHub Amsterdam (Delivered)
2. **Lunch meeting** - Last week, Luminis Utrecht (Delivered)
3. **Large dinner event** - Yesterday, CityView Rotterdam (Delivered)
4. **Simple coffee order** - Today, TechHub Amsterdam (Ready)
5. **Sushi lunch** - Tomorrow, Harbor Den Haag (Confirmed)
6. **Workshop snacks** - Next week, Campus Eindhoven (Pending)
7. **Simple lunch** - Today, GreenSpace Arnhem (Processing)
8. **Pizza party** - Tomorrow, Loft Amsterdam (Confirmed)
9. **Breakfast meeting** - Today, Villa Hilversum (Delivered)
10. **Mixed order** - Last week, TechHub Amsterdam (Cancelled)

## Thread Safety

The service uses `ConcurrentHashMap` for thread-safe order storage, making it suitable for concurrent access in multi-threaded environments.

## Future Enhancements

Potential improvements for production use:

1. **Persistent Storage** - Database integration (JPA/Hibernate)
2. **Pagination** - For large result sets
3. **Advanced Search** - Full-text search capabilities
4. **Order History** - Track status changes over time
5. **Notifications** - Email/SMS notifications for status changes
6. **Export** - PDF/Excel report generation
7. **Caching** - Redis for high-performance queries
8. **Metrics** - Prometheus metrics for monitoring

## Integration with HandleOrderAgent

The `OrderService` can be integrated with the existing `HandleOrderAgent` to store processed orders:

```java
@Action
ProcessedOrder processOrder(ConfirmedOrder confirmedOrder, Order agentOrder, Ai ai) {
    // ... existing processing logic ...
    
    // Store the order in OrderService
    List<OrderItem> orderItems = Arrays.stream(agentOrder.items().items())
        .map(item -> new org.rag4j.nomnom.orders.OrderItem(
            item.product(), 
            item.quantity()
        ))
        .toList();
    
    orderService.createAndStoreOrder(
        agentOrder.orderId(),
        agentOrder.location(),
        agentOrder.deliveryDate(),
        orderItems,
        agentOrder.items().note(),
        OrderStatus.CONFIRMED
    );
    
    return new ProcessedOrder(true, "Order processed and stored successfully");
}
```

This integration enables full order tracking and analytics for AI-processed orders.
