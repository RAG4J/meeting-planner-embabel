package org.rag4j.nomnom.agent.model;

import java.time.LocalDate;

public record UserMessage(String location, LocalDate deliveryDate, String message) {

}
