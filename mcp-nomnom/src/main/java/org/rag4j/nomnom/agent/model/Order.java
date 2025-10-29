package org.rag4j.nomnom.agent.model;

import java.time.LocalDate;

public record Order(String orderId, String location, LocalDate deliveryDate, OrderItemsList items) {

    public String printOrder() {
        return "Order ID: " + orderId + "\n" +
                "Location: " + location + "\n" +
                "Delivery Date: " + deliveryDate + "\n" +
                "Items:\n" +
                printOrderItems();
    }

    public String printOrderItems() {
        return items.printOrderItems();
    }
}
