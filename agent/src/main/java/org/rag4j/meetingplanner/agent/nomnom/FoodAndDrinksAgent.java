package org.rag4j.meetingplanner.agent.nomnom;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import org.rag4j.meetingplanner.agent.nomnom.model.NomNomOrderRequest;
import org.rag4j.meetingplanner.agent.nomnom.model.NomNomOrderResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.rag4j.meetingplanner.agent.config.LlmModel.BALANCED;

@Agent(
        name = "FoodAndDrinksAgent",
        description = "An agent that helps to place orders for food and drinks.",
        version = "1.0.0"
)
public class FoodAndDrinksAgent {
    private static final Logger logger = LoggerFactory.getLogger(FoodAndDrinksAgent.class);

    @AchievesGoal(description = "Order a lunch, dinner and or drinks.")
    @Action(toolGroups = {"food_and_drinks"}, description = "Order the food and beverages")
    public NomNomOrderResult orderLunch(NomNomOrderRequest orderRequest, Ai ai) throws Exception {
        logger.info("Received an order for food and beverages: {}", orderRequest);

        NomNomOrderResult response = ai.withLlmByRole(BALANCED.getModelName())
                .createObject(String.format("""
                                 You will be given an order for food and beverages.
                                 You need to receive a location, date, and order details.
                                 Use the available tools to place the order.
                                 Return a description of the order that was created or a message if it failed.

                                 # order to place
                                 %s

                                """,
                        orderRequest
                ).trim(), NomNomOrderResult.class);
        logger.info("Response generated: {}", response);
        return response;
    }

}
