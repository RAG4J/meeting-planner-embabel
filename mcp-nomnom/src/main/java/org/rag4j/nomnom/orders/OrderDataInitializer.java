package org.rag4j.nomnom.orders;

import org.rag4j.nomnom.orders.model.OrderItem;
import org.rag4j.nomnom.orders.model.OrderStatus;
import org.rag4j.nomnom.products.MenuService;
import org.rag4j.nomnom.products.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Initializes sample order data for demonstration purposes.
 */
@Component
public class OrderDataInitializer implements ApplicationRunner {
    private static final Logger logger = LoggerFactory.getLogger(OrderDataInitializer.class);
    
    private final OrderService orderService;
    private final MenuService menuService;
    
    public OrderDataInitializer(OrderService orderService, MenuService menuService) {
        this.orderService = orderService;
        this.menuService = menuService;
    }
    
    @Override
    public void run(ApplicationArguments args) {
        logger.info("Initializing sample order data...");
        createSampleOrders();
        logger.info("Sample order data initialized. Total orders: {}", orderService.getOrderCount());
    }
    
    private void createSampleOrders() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate lastWeek = today.minusDays(7);
        LocalDate tomorrow = today.plusDays(1);
        LocalDate nextWeek = today.plusDays(7);
        
        // Order 1 - Coffee meeting (yesterday, delivered)
        createOrder(
                "TechHub Amsterdam",
                yesterday,
                OrderStatus.DELIVERED,
                List.of(
                        new OrderItem(findProduct("Coffee"), 5),
                        new OrderItem(findProduct("Cookie"), 5)
                ),
                "Morning team meeting"
        );
        
        // Order 2 - Lunch meeting (last week, delivered)
        createOrder(
                "Luminis Utrecht",
                lastWeek,
                OrderStatus.DELIVERED,
                List.of(
                        new OrderItem(findProduct("Sandwich"), 8),
                        new OrderItem(findProduct("Juice"), 8),
                        new OrderItem(findProduct("Chips"), 4)
                ),
                "Client lunch presentation"
        );
        
        // Order 3 - Large dinner event (yesterday, delivered)
        createOrder(
                "CityView Rotterdam",
                yesterday,
                OrderStatus.DELIVERED,
                List.of(
                        new OrderItem(findProduct("Pizza"), 10),
                        new OrderItem(findProduct("Burger"), 5),
                        new OrderItem(findProduct("Salad"), 8),
                        new OrderItem(findProduct("Soda"), 15),
                        new OrderItem(findProduct("Juice"), 8)
                ),
                "Company celebration dinner"
        );
        
        // Order 4 - Simple coffee order (today, ready)
        createOrder(
                "TechHub Amsterdam",
                today,
                OrderStatus.READY,
                List.of(
                        new OrderItem(findProduct("Coffee"), 10),
                        new OrderItem(findProduct("Tea"), 3)
                ),
                "Daily standup"
        );
        
        // Order 5 - Sushi lunch (tomorrow, confirmed)
        createOrder(
                "Harbor Den Haag",
                tomorrow,
                OrderStatus.CONFIRMED,
                List.of(
                        new OrderItem(findProduct("Sushi"), 12),
                        new OrderItem(findProduct("Soup"), 6),
                        new OrderItem(findProduct("Tea"), 12)
                ),
                "Important client meeting"
        );
        
        // Order 6 - Workshop snacks (next week, pending)
        createOrder(
                "Campus Eindhoven",
                nextWeek,
                OrderStatus.PENDING,
                List.of(
                        new OrderItem(findProduct("Wrap"), 15),
                        new OrderItem(findProduct("Sandwich"), 10),
                        new OrderItem(findProduct("Chips"), 20),
                        new OrderItem(findProduct("Cookie"), 25),
                        new OrderItem(findProduct("Coffee"), 20),
                        new OrderItem(findProduct("Juice"), 15)
                ),
                "All-day workshop with lunch"
        );
        
        // Order 7 - Simple lunch (today, processing)
        createOrder(
                "GreenSpace Arnhem",
                today,
                OrderStatus.PROCESSING,
                List.of(
                        new OrderItem(findProduct("Salad"), 6),
                        new OrderItem(findProduct("Soup"), 6),
                        new OrderItem(findProduct("Tea"), 6)
                ),
                "Health-conscious team lunch"
        );
        
        // Order 8 - Pizza party (tomorrow, confirmed)
        createOrder(
                "Loft Amsterdam",
                tomorrow,
                OrderStatus.CONFIRMED,
                List.of(
                        new OrderItem(findProduct("Pizza"), 8),
                        new OrderItem(findProduct("Soda"), 20),
                        new OrderItem(findProduct("Cookie"), 12)
                ),
                "Team building pizza party"
        );
        
        // Order 9 - Breakfast meeting (today, delivered)
        createOrder(
                "Villa Hilversum",
                today,
                OrderStatus.DELIVERED,
                List.of(
                        new OrderItem(findProduct("Coffee"), 8),
                        new OrderItem(findProduct("Tea"), 4),
                        new OrderItem(findProduct("Sandwich"), 12)
                ),
                "Early morning strategy meeting"
        );
        
        // Order 10 - Mixed order (last week, cancelled)
        createOrder(
                "TechHub Amsterdam",
                lastWeek,
                OrderStatus.CANCELLED,
                List.of(
                        new OrderItem(findProduct("Burger"), 10),
                        new OrderItem(findProduct("Soda"), 10)
                ),
                "Cancelled due to venue change"
        );
    }
    
    private void createOrder(String location, LocalDate deliveryDate, OrderStatus status,
                            List<OrderItem> items, String note) {
        try {
            orderService.createAndStoreOrder(
                    UUID.randomUUID().toString(),
                    location,
                    deliveryDate,
                    items,
                    note,
                    status
            );
        } catch (Exception e) {
            logger.error("Failed to create sample order: {}", e.getMessage());
        }
    }
    
    private Product findProduct(String name) {
        Product product = menuService.findBestMatchingProduct(name);
        if (product == null) {
            throw new IllegalArgumentException("Product not found: " + name);
        }
        return product;
    }
}
