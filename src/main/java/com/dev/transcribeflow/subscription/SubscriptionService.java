package com.dev.transcribeflow.subscription;

import com.dev.transcribeflow.core.exception.*;
import com.dev.transcribeflow.core.utils.MessageUtils;
import com.dev.transcribeflow.user.UserEntity;
import com.dev.transcribeflow.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final MessageUtils messageUtils;

    @Transactional
    public void createDefaultSubscription(UUID userId) {

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(
                        messageUtils.getMessage("subscription.error.user_not_found", userId)));

        subscriptionRepository.findByUserId(userId).ifPresent(s -> {
            throw new SubscriptionAlreadyExistsException(
                    messageUtils.getMessage("subscription.error.already_exists"));
        });

        SubscriptionEntity subscription = SubscriptionEntity.builder()
                .planType(PlanType.FREE)
                .status(SubscriptionStatus.ACTIVE)
                .remainingSeconds(PlanType.FREE.getMonthlySecondsLimit())
                .user(user)
                .build();

        subscriptionRepository.save(subscription);
        log.info("Default FREE subscription created for user: {}", userId);
    }

    public SubscriptionEntity getActiveSubscription(UUID userId) {

        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(
                    messageUtils.getMessage("subscription.error.user_not_found", userId));
        }

        return subscriptionRepository.findByUserId(userId)
                .filter(s -> s.getStatus() == SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new SubscriptionNotFoundException(
                        messageUtils.getMessage("subscription.error.not_found", userId)));
    }

    @Transactional
    public SubscriptionEntity updatePlan(UUID userId, PlanType newPlan) {
        if (newPlan == null) {
            throw new SubscriptionInvalidPayloadException(
                    messageUtils.getMessage("subscription.error.invalid_payload"));
        }

        SubscriptionEntity subscription = getActiveSubscription(userId);
        subscription.setPlanType(newPlan);
        subscription.setRemainingSeconds(newPlan.getMonthlySecondsLimit());
        return subscriptionRepository.save(subscription);
    }

    @Transactional
    public SubscriptionEntity cancelSubscription(UUID userId) {
        SubscriptionEntity subscription = getActiveSubscription(userId);

        if (subscription.getStatus() == SubscriptionStatus.CANCELED) {
            throw new SubscriptionStateConflictException(
                    messageUtils.getMessage(("subscription.error.already_canceled")));
        }

        subscription.setStatus(SubscriptionStatus.CANCELED);
        log.info("Subscription canceled for user: {}", userId);
        return subscriptionRepository.save(subscription);
    }

}
