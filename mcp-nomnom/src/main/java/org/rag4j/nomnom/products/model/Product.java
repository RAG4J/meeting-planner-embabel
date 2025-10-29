package org.rag4j.nomnom.products.model;

public record Product(String id, String name, String description, double price, Category category) {
}
