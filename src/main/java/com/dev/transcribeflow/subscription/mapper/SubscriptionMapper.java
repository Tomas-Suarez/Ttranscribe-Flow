package com.dev.transcribeflow.subscription.mapper;

import com.dev.transcribeflow.subscription.SubscriptionEntity;
import com.dev.transcribeflow.subscription.dto.response.SubscriptionResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SubscriptionMapper {

    @Mapping(target = "message", source = "message")
    SubscriptionResponseDTO toResponse(SubscriptionEntity entity, String message);
}
