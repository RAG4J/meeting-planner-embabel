package org.rag4j.nomnom.agent;

import com.embabel.agent.api.annotation.*;
import com.embabel.agent.api.common.Ai;
import org.slf4j.Logger;

import java.time.LocalDate;

import static org.rag4j.nomnom.agent.LlmModel.BEST;

@Agent(description = "Handles an incoming food order and processes it accordingly.")
public class HandleOrderAgent {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(HandleOrderAgent.class);

    @Action(description = "Receive an order request from a user.")
    Order receiveOrder(UserMessage userInput, Ai ai) {
        logger.info("Received order request: {}", userInput.message());

        var orderDetails = userInput.message();

        var items = ai.withLlmByRole(BEST.getModelName())
                .createObject(String.format("""
                                 You will be given a request for a food order.
                                 The text contains what the customer wants to order.
                                 Extract the items from the request, match them against available options.
                                 Only use the available options. Map found items to similar available options.
                                 If an item is not available, do not include it in the order.
                                 Return the order request with the items.
                                
                                 Available options are:
                                 Diner:
                                    - Pizza
                                    - Burger
                                    - Salad
                                    - Sushi
                                 Lunch:
                                    - Sandwich
                                    - Wrap
                                    - Soup
                                 Drinks:
                                    - Soda
                                    - Coffee
                                    - Tea
                                    - Juice
                                
                                 # Message to extract the order from
                                 %s
                                
                                """,
                        orderDetails
                ).trim(), OrderItemsList.class);

        return new Order(
                java.util.UUID.randomUUID().toString(),
                userInput.location(),
                userInput.deliveryDate(),
                items
        );
    }

    @Action(description = "Confirm the order with the user before processing.")
    ConfirmedOrder confirmOrder(Order order, Ai ai) {
        logger.info("Confirming order for {} on {} with {} items.", order.location(), order.deliveryDate(),
                order.items().items().length);

        return WaitFor.formSubmission(
                """
                        Great, I have a proposed order. Please confirm if you would like to proceed with this order.
                        
                        Location: %s
                        Delivery Date: %s
                        
                        The order contains the following items:
                        %s
                        """.formatted(order.location(), order.deliveryDate(), order.printOrderItems()),
                ConfirmedOrder.class
        );
    }

    @AchievesGoal(
            description = "Process the food order by validating, preparing, and confirming it.",
            export = @Export(remote = true, name = "acceptOrder", startingInputTypes = {UserMessage.class}))
    @Action
    ProcessedOrder processOrder(ConfirmedOrder confirmedOrder, Order order, Ai ai) {
        logger.info("Processing order for {} on {}. Confirmation status: {}", order.location(),
                order.deliveryDate(), confirmedOrder.confirmed());

        return ai.withDefaultLlm().createObject(
                String.format("""
                                    You are an order processing system for a food delivery service.
                                    You will be given a confirmed order with items to process.
                                    Check the confirmation of the order.
                                    If the order is confirmed, return a success message.
                                    If there are issues with the order, return an appropriate error message.
                        
                                    # Confirmed Order
                                    Location: %s
                                    Delivery Date: %s
                        
                                    Items:
                                    %s
                        
                                    Provide a concise confirmation message if the order is valid.
                        """, order.location(), order.deliveryDate(), order.printOrderItems())
                , ProcessedOrder.class);
    }

    public record UserMessage(String location, LocalDate deliveryDate, String message) {

    }

    public record OrderItem(String itemName, int quantity) {

    }

    public record OrderItemsList(OrderItem[] items) {

    }

    public record Order(String orderId, String location, LocalDate deliveryDate, OrderItemsList items) {

        public String printOrder() {
            return "Order ID: " + orderId + "\n" +
                    "Location: " + location + "\n" +
                    "Delivery Date: " + deliveryDate + "\n" +
                    "Items:\n" +
                    printOrderItems();
        }

        public String printOrderItems() {
            StringBuilder sb = new StringBuilder();
            for (OrderItem item : items.items()) {
                sb.append("- ").append(item.quantity()).append(" x ").append(item.itemName()).append("\n");
            }
            return sb.toString();
        }
    }

    public record ProcessedOrder(boolean success, String message) {

    }

    public record ConfirmedOrder(boolean confirmed) {
    }
}
