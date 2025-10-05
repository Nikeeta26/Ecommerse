package com.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionCheckResponse {
    private boolean hasSubscription;
    private Long subscriptionId;
    private String message;
    private String suggestedAction;

    public static SubscriptionCheckResponse withSubscription(Long subscriptionId) {
        return new SubscriptionCheckResponse(
            true,
            subscriptionId,
            "Active subscription found for this product",
            "PROCEED_TO_BUY"
        );
    }

    public static SubscriptionCheckResponse withoutSubscription() {
        return new SubscriptionCheckResponse(
            false,
            null,
            "No active subscription found for this product",
            "SUBSCRIBE"
        );
    }
}
