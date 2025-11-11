package org.rag4j.meetingplanner.agent.config;

public enum LlmModel {
    BEST("best"),
    BALANCED("balanced"),
    FAST("fast");

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
