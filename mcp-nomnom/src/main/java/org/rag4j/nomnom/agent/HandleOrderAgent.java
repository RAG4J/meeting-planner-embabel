package org.rag4j.nomnom.agent;

import com.embabel.agent.api.annotation.*;
import com.embabel.agent.api.common.Ai;
import org.rag4j.nomnom.agent.model.*;
import org.rag4j.nomnom.orders.OrderService;
import org.rag4j.nomnom.orders.model.OrderStatus;
import org.rag4j.nomnom.products.MenuService;
import org.slf4j.Logger;

import java.util.Arrays;

import static org.rag4j.nomnom.agent.LlmModel.BALANCED;
import static org.rag4j.nomnom.agent.LlmModel.BEST;

@Agent(description = "Handles an incoming food order and processes it accordingly.")
public class HandleOrderAgent {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(HandleOrderAgent.class);

    private final MenuService menuService;
    private final OrderService orderService;

    public HandleOrderAgent(MenuService menuService, OrderService orderService) {
        this.menuService = menuService;
        this.orderService = orderService;
    }

    @Action(description = "Receive an order request from a user.")
    Order receiveOrder(UserMessage userInput, Ai ai) {
        logger.info("Received order request: {}", userInput.message());

        var orderDetails = userInput.message();

        var items = ai.withLlmByRole(BEST.getModelName())
                .withToolObject(menuService)
                .createObject(String.format("""
                                 You will be given a request for a food order.
                                 The text contains what the customer wants to order.
                                 Extract the items from the request, use tools to find the best matching product.
                                 In some cases, the result is an alternative product that is similar to the requested one.
                                 Replace the requested product with the best matching product from the menu.
                                 Return the list of order items with product and quantity.
                                 In case there is not alternative, skip that product and a message to the note field in the 
                                 order that you do not have the requested product and that there is no alternative.
                                
                                 # Message to extract the order from
                                 %s
                                
                                """,
                        orderDetails
                ).trim(), OrderItemsList.class);

        logger.info("Found items: {}", items.printOrderItems());

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
        logger.info("Confirmed items: {}", order.printOrderItems());

        return new ConfirmedOrder(true);

//        I commented this code out to simplify the flow and avoid waiting for user input in this example.
//        logger.info("Asking user to confirm the order.");
//        return WaitFor.formSubmission(
//                """
//                        Great, I have a proposed order. Please confirm if you would like to proceed with this order.
//
//                        Location: %s
//                        Delivery Date: %s
//
//                        The order contains the following items:
//                        %s
//                        """.formatted(order.location(), order.deliveryDate(), order.printOrderItems()),
//                ConfirmedOrder.class
//        );
    }

    @Action(description = "Store the confirmed order in the system.")
    ConfirmedAndStoredOrder storeOrder(ConfirmedOrder confirmedOrder, Order order) {
        logger.info("Storing confirmed order. Confirmation status: {}", confirmedOrder.confirmed());

        if (!confirmedOrder.confirmed()) {
            logger.info("Order not confirmed, skipping storage.");
            return new ConfirmedAndStoredOrder(false, false);
        }

        orderService.createAndStoreOrder(
                order.orderId(),
                order.location(),
                order.deliveryDate(),
                Arrays.stream(order.items().items()).map(i -> new org.rag4j.nomnom.orders.model.OrderItem(i.product(), i.quantity())).toList(),
                order.items().note(),
                OrderStatus.CONFIRMED
        );

        logger.info("Order stored successfully with ID: {}", order.orderId());
        return new ConfirmedAndStoredOrder(true, true);
    }

    @AchievesGoal(
            description = "Process the food order by validating, preparing, and confirming it.",
            export = @Export(remote = true, name = "acceptOrder", startingInputTypes = {UserMessage.class}))
    @Action
    ProcessedOrder processOrder(ConfirmedAndStoredOrder confirmedOrder, Order order, Ai ai) {
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

}
