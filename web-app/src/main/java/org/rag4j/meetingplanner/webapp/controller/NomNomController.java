package org.rag4j.meetingplanner.webapp.controller;

import org.rag4j.meetingplanner.agent.nomnom.model.NomNomOrderConfirmation;
import org.rag4j.meetingplanner.agent.nomnom.model.NomNomOrderRequest;
import org.rag4j.meetingplanner.webapp.nomnom.NomNomAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDate;

@Controller
public class NomNomController {
    private static final Logger logger = LoggerFactory.getLogger(NomNomController.class);

    private final NomNomAgent nomNomAgent;

    public NomNomController(NomNomAgent nomNomAgent) {
        this.nomNomAgent = nomNomAgent;
    }

    @GetMapping("/nomnom")
    public String nomNom(Model model) {
        model.addAttribute("title", "NomNom Food Orders");
        model.addAttribute("orderRequest", new NomNomOrderRequest("", LocalDate.now().plusDays(1), ""));
        return "nomnom";
    }

    @PostMapping("/nomnom")
    public String submitOrder(@ModelAttribute NomNomOrderRequest orderRequest, Model model) {
        model.addAttribute("title", "NomNom Food Orders");
        model.addAttribute("orderRequest", orderRequest);

        try {
            logger.info("Submitting NomNom order request for location {}", orderRequest.location());
            NomNomAgent.OrderConfirmation response = nomNomAgent.placeOrder(orderRequest.location(), orderRequest.deliveryDate(), orderRequest.message());

            model.addAttribute("success", response.responseMessage());
            if (response.askForConfirmation()) {
                model.addAttribute("orderConfirmation", new NomNomOrderConfirmation(""));
                return "nomnomconfirm";
            } else {
                model.addAttribute("orderRequest", orderRequest);
            }
        } catch (Exception ex) {
            logger.error("Failed to process NomNom order request", ex);
            model.addAttribute("error", "Unable to process the NomNom order request: " + ex.getMessage());
        }

        return "nomnom";
    }

    @PostMapping("/nomnom/confirm")
    public String submitOrderConfirmation(@ModelAttribute NomNomOrderConfirmation orderConfirmation, Model model) {
        model.addAttribute("title", "NomNom Food Orders");
        model.addAttribute("orderRequest", new NomNomOrderRequest("", LocalDate.now().plusDays(1), ""));

        try {
            logger.info("Submitting NomNom order confirmation: {}", orderConfirmation.message());
            NomNomAgent.OrderConfirmation response = nomNomAgent.confirmOrder(orderConfirmation.message());

            model.addAttribute("success", response.responseMessage());

        } catch (Exception ex) {
            logger.error("Failed to process NomNom order confirmation", ex);
            model.addAttribute("error", "Unable to process the NomNom order confirmation: " + ex.getMessage());
        }

        return "nomnom";
    }
}
