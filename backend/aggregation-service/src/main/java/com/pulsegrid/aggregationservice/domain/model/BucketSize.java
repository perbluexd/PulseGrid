package com.pulsegrid.aggregationservice.domain.model;

public enum BucketSize {
    ONE_MINUTE("1 minute"),
    FIVE_MINUTES("5 minutes"),
    ONE_HOUR("1 hour"),
    ONE_DAY("1 day");

    private final String interval;

    BucketSize(String interval) {
        this.interval = interval;
    }

    public String interval() {
        return interval;
    }
}
