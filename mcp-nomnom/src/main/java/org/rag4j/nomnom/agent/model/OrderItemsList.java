package org.rag4j.nomnom.agent.model;

public record OrderItemsList(OrderItem[] items, String note) {

    public String printOrderItems() {
        StringBuilder sb = new StringBuilder();
        for (OrderItem item : items()) {
            sb.append("- ").append(item.quantity()).append(" x ").append(item.product().name()).append("\n");
        }
        return sb.toString();
    }

}
