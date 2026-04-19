package com.dev.transcribeflow.subscription;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<SubscriptionEntity, UUID> {

    Optional<SubscriptionEntity> findByUserId(UUID userId);
}
