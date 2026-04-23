package com.dev.transcribeflow.subscription;

import com.dev.transcribeflow.core.exception.SubscriptionNotFoundException;
import com.dev.transcribeflow.core.utils.MessageUtils;
import com.dev.transcribeflow.usage.UsageLogEntity;
import com.dev.transcribeflow.usage.UsageLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuotaService {

    private final StringRedisTemplate redisTemplate;
    private final SubscriptionRepository subscriptionRepository;
    private final UsageLogRepository usageLogRepository;
    private final MessageUtils messageUtils;

    private static final String QUOTA_KEY_PREFIX = "quota:user:";

    public void prepareQuotaInCache(UUID userId){
        String key = QUOTA_KEY_PREFIX + userId;

        if(Boolean.FALSE.equals(redisTemplate.hasKey(key))) {
            Integer balance = subscriptionRepository.findByUserId(userId)
                    .map(SubscriptionEntity::getRemainingSeconds)
                    .orElseThrow(() -> new SubscriptionNotFoundException(
                            messageUtils.getMessage("subscription.error.not_found", userId)));

            redisTemplate.opsForValue().set(key, String.valueOf(balance));

            log.info("Quota loaded into Redis for user {}: {} seconds", userId, balance);
        }
    }

    public boolean consumeSecond(UUID userId){
        String key = QUOTA_KEY_PREFIX + userId;

        Long remaining = redisTemplate.opsForValue().decrement(key);

        if (remaining != null && remaining < 0) {
            redisTemplate.opsForValue().set(key, "0");
            return false;
        }

        return true;
    }

    @Transactional
    public void syncQuotaToDatabase(UUID userId, String source){
        String key = QUOTA_KEY_PREFIX + userId;

        String redisValue = redisTemplate.opsForValue().get(key);
        if(redisValue == null){
            return;
        }

        int newBalance = Integer.parseInt(redisValue);

        SubscriptionEntity subscription = subscriptionRepository.findByUserId(userId)
                .orElseThrow(() -> new SubscriptionNotFoundException(
                        messageUtils.getMessage("subscription.error.not_found", userId)));

        int consumed = subscription.getRemainingSeconds() - newBalance;

        if(consumed > 0){
            subscription.setRemainingSeconds(newBalance);
            subscriptionRepository.save(subscription);

            UsageLogEntity logEntity = UsageLogEntity.builder()
                    .user(subscription.getUser())
                    .secondsProcessed(consumed)
                    .timestamp(LocalDateTime.now())
                    .sourceUrl(source)
                    .build();

            log.info("Synchronized: User {} consumed {}s. New balance: {}s", userId, consumed, newBalance);
        }

        redisTemplate.delete(key);
    }
}
