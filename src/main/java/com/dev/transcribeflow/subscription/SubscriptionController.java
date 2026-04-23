package com.dev.transcribeflow.subscription;

import com.dev.transcribeflow.core.security.principal.SecurityUser;
import com.dev.transcribeflow.core.utils.MessageUtils;
import com.dev.transcribeflow.subscription.dto.response.SubscriptionResponseDTO;
import com.dev.transcribeflow.subscription.mapper.SubscriptionMapper;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/subscription")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final SubscriptionMapper subscriptionMapper;
    private final MessageUtils messageUtils;

    @GetMapping("/my-plan")
    public ResponseEntity<SubscriptionResponseDTO> getMyPlan(@AuthenticationPrincipal SecurityUser securityUser) {
        SubscriptionEntity entity = subscriptionService.getActiveSubscription(securityUser.getUserId());

        return ResponseEntity.ok(
                subscriptionMapper.toResponse(entity, messageUtils.getMessage("subscription.success.retrieved"))
        );
    }

    @PutMapping("/upgrade")
    public ResponseEntity<SubscriptionResponseDTO> upgradePlan(@AuthenticationPrincipal SecurityUser securityUser,
                                                               @RequestParam PlanType newPlan){
        SubscriptionEntity updated = subscriptionService.updatePlan(securityUser.getUserId(), newPlan);

        return ResponseEntity.ok(
                subscriptionMapper.toResponse(updated, messageUtils.getMessage("subscription.success.updated"))
        );
    }

    @PostMapping("/cancel")
    public ResponseEntity<SubscriptionResponseDTO> cancelSubscription(@AuthenticationPrincipal SecurityUser securityUser){
        SubscriptionEntity canceled = subscriptionService.cancelSubscription(securityUser.getUserId());

        return ResponseEntity.ok(
                subscriptionMapper.toResponse(canceled, messageUtils.getMessage("subscription.success.canceled"))
        );
    }
}
