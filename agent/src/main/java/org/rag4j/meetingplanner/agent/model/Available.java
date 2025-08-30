package org.rag4j.meetingplanner.agent.model;

/**
 * Represents the availability status of a person
 */
public record Available(Person person, boolean available) {
}
