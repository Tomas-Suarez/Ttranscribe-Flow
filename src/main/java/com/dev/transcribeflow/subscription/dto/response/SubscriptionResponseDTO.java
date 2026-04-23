package com.dev.transcribeflow.subscription.dto.response;

import com.dev.transcribeflow.subscription.PlanType;
import com.dev.transcribeflow.subscription.SubscriptionStatus;

public record SubscriptionResponseDTO(
        PlanType planType,
        SubscriptionStatus status,
        String message
) {
}
