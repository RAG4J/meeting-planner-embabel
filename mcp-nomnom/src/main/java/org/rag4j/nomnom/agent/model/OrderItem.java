package org.rag4j.nomnom.agent.model;

import org.rag4j.nomnom.products.model.Product;

public record OrderItem(Product product, int quantity) {

}
