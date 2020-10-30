package com.backbase.proto.plaid.mapper;

import com.backbase.proto.plaid.model.Webhook;
import com.backbase.proto.plaid.webhook.model.PlaidWebhook;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * This class maps the webhook so it may be used.
 */
@Mapper
public interface WebhookMapper {
    /**
     * Maps the webhook from Plaid.
     *
     * @param source webhook from Plaid
     * @return webhook to be stored in database
     */
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "completed", ignore = true)
    @Mapping(target = "dbsError", ignore = true)
    Webhook mapToDomain(PlaidWebhook source);
}
