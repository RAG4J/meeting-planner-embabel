package org.rag4j.meetingplanner.agent.model.nomnom;

import java.time.LocalDate;

public record NomNomOrderRequest(String location, LocalDate deliveryDate, String message) {
}
