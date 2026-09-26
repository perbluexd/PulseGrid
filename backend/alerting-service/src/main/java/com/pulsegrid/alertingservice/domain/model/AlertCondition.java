package com.pulsegrid.alertingservice.domain.model;

public enum AlertCondition {
    GREATER_THAN,
    LESS_THAN,
    EQUALS;

    public boolean isMetBy(double value, double threshold) {
        return switch (this) {
            case GREATER_THAN -> value > threshold;
            case LESS_THAN -> value < threshold;
            case EQUALS -> Double.compare(value, threshold) == 0;
        };
    }
}
