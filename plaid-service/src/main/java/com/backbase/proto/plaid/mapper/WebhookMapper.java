package com.backbase.proto.plaid.mapper;

import com.backbase.proto.plaid.model.PlaidWebhook;
import com.backbase.proto.plaid.model.Webhook;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * WebhookMapper:
 * Maps the webhook so it may be used
 */
@Mapper
public interface WebhookMapper {
    /**
     * Maps the webhook from plaid
     *
     * @param source webhook from plaid
     * @return webhook to be stored in database
     */
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "completed", ignore = true)
    @Mapping(target = "dbsError", ignore = true)
    Webhook mapToDomain(PlaidWebhook source);
}
