package org.rag4j.nomnom.agent;

public enum LlmModel {
    BEST("best"),
    BALANCED("balanced");

    private final String modelName;

    LlmModel(String modelName) {
        this.modelName = modelName;
    }

    public String getModelName() {
        return modelName;
    }

    @Override
    public String toString() {
        return modelName;
    }
}
