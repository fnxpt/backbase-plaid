package com.backbase.proto.plaid.mapper;

import com.backbase.proto.plaid.model.PlaidWebhook;
import com.backbase.proto.plaid.model.Webhook;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface WebhookMapper {

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "completed", ignore = true)
    @Mapping(target = "dbsError", ignore = true)
    Webhook mapToDomain(PlaidWebhook source);
}
