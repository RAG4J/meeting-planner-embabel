package org.rag4j.meetingplanner.agent.nomnom.model;

import java.time.LocalDate;

public record NomNomOrderRequest(String location, LocalDate deliveryDate, String message) {
}
