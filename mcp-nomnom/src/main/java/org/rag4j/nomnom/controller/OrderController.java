package org.rag4j.nomnom.controller;

import org.rag4j.nomnom.orders.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for order management and reporting.
 */
@Controller
@RequestMapping("/orders")
public class OrderController {
    
    private final OrderService orderService;
    
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }
    
    /**
     * Display all orders with optional filtering.
     */
    @GetMapping
    public String listOrders(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model
    ) {
        List<Order> orders;
        String filterDescription = "All Orders";
        
        // Apply filters
        if (location != null && !location.isBlank()) {
            orders = orderService.getOrdersByLocation(location);
            filterDescription = "Orders for " + location;
        } else if (status != null) {
            orders = orderService.getOrdersByStatus(status);
            filterDescription = status.getDisplayName() + " Orders";
        } else if (date != null) {
            orders = orderService.getOrdersByDeliveryDate(date);
            filterDescription = "Orders for " + date;
        } else {
            orders = orderService.getAllOrdersSorted();
        }
        
        // Get unique locations for filter dropdown
        List<String> locations = orderService.getAllOrders().stream()
                .map(Order::location)
                .distinct()
                .sorted()
                .toList();
        
        // Calculate summary statistics
        double totalRevenue = orders.stream()
                .mapToDouble(Order::totalAmount)
                .sum();
        
        int totalItems = orders.stream()
                .mapToInt(Order::getTotalItemCount)
                .sum();
        
        model.addAttribute("orders", orders);
        model.addAttribute("filterDescription", filterDescription);
        model.addAttribute("locations", locations);
        model.addAttribute("statuses", OrderStatus.values());
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("selectedLocation", location);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedDate", date);
        
        return "orders/list";
    }
    
    /**
     * Display detailed view of a single order.
     */
    @GetMapping("/{orderId}")
    public String viewOrder(@PathVariable String orderId, Model model) {
        Optional<Order> orderOpt = orderService.getOrderById(orderId);
        
        if (orderOpt.isEmpty()) {
            return "redirect:/orders?error=notfound";
        }
        
        Order order = orderOpt.get();
        model.addAttribute("order", order);
        model.addAttribute("allStatuses", OrderStatus.values());
        
        return "orders/detail";
    }
    
    /**
     * Display comprehensive reports and analytics.
     */
    @GetMapping("/reports")
    public String reports(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model
    ) {
        // Default to last 30 days if no dates provided
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        
        // Generate comprehensive report
        OrderSummaryReport summary = orderService.generateSummaryReportForDateRange(startDate, endDate);
        
        // Get top products (limit to 10)
        List<ProductSalesReport> topProducts = summary.topProducts();
        
        // Get category breakdown
        List<CategorySalesReport> categoryReports = summary.categoryBreakdown();
        
        // Get revenue by location
        Map<String, Double> revenueByLocation = orderService.getRevenueByLocation();
        
        // Get order count by location
        Map<String, Long> orderCountByLocation = orderService.getOrderCountByLocation();
        
        model.addAttribute("summary", summary);
        model.addAttribute("topProducts", topProducts);
        model.addAttribute("categoryReports", categoryReports);
        model.addAttribute("revenueByLocation", revenueByLocation);
        model.addAttribute("orderCountByLocation", orderCountByLocation);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("totalRevenue", orderService.getTotalRevenue());
        
        return "orders/reports";
    }
    
    /**
     * Display analytics by location.
     */
    @GetMapping("/analytics/location/{location}")
    public String locationAnalytics(@PathVariable String location, Model model) {
        OrderService.LocationStatistics stats = orderService.getLocationStatistics(location);
        List<Order> locationOrders = orderService.getOrdersByLocation(location);
        
        // Get most popular products for this location
        List<ProductSalesReport> popularProducts = orderService.getAllOrders().stream()
                .filter(order -> order.location().equalsIgnoreCase(location))
                .flatMap(order -> order.items().stream())
                .collect(java.util.stream.Collectors.groupingBy(
                        OrderItem::product,
                        java.util.stream.Collectors.summingInt(OrderItem::quantity)
                ))
                .entrySet().stream()
                .map(entry -> new ProductSalesReport(
                        entry.getKey(),
                        entry.getValue(),
                        (int) locationOrders.stream()
                                .filter(o -> o.containsProduct(entry.getKey().id()))
                                .count(),
                        entry.getValue() * entry.getKey().price()
                ))
                .sorted()
                .limit(10)
                .toList();
        
        model.addAttribute("location", location);
        model.addAttribute("stats", stats);
        model.addAttribute("orders", locationOrders);
        model.addAttribute("popularProducts", popularProducts);
        
        return "orders/location-analytics";
    }
    
    /**
     * Display orders by status with counts.
     */
    @GetMapping("/by-status")
    public String ordersByStatus(Model model) {
        Map<OrderStatus, List<Order>> ordersByStatus = java.util.Arrays.stream(OrderStatus.values())
                .collect(java.util.stream.Collectors.toMap(
                        status -> status,
                        orderService::getOrdersByStatus
                ));
        
        model.addAttribute("ordersByStatus", ordersByStatus);
        model.addAttribute("allStatuses", OrderStatus.values());
        
        return "orders/by-status";
    }
    
    /**
     * Display popular products.
     */
    @GetMapping("/popular-products")
    public String popularProducts(
            @RequestParam(defaultValue = "20") int limit,
            Model model
    ) {
        List<ProductSalesReport> popularProducts = orderService.getMostPopularProducts(limit);
        
        model.addAttribute("popularProducts", popularProducts);
        model.addAttribute("limit", limit);
        
        return "orders/popular-products";
    }
}
