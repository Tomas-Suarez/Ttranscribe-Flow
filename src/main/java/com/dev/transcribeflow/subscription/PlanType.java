package com.dev.transcribeflow.subscription;

import lombok.Getter;

@Getter
public enum PlanType {
    FREE("Free", 600),
    PREMIUM("Premium", 36000),
    UNLIMITED("Unlimited", -1);

    private final String displayName;
    private final int monthlySecondsLimit;

    PlanType(String displayName, int monthlySecondsLimit) {
        this.displayName = displayName;
        this.monthlySecondsLimit = monthlySecondsLimit;
    }
}
