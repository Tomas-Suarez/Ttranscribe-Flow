package com.dev.transcribeflow.usage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UsageLogRepository extends JpaRepository<UsageLogEntity, UUID> {
}
