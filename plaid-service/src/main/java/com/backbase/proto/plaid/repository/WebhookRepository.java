package com.backbase.proto.plaid.repository;

import com.backbase.proto.plaid.model.Webhook;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Enables the use of and access to the webhook database where data on the plaid webhook is stored
 */
@Repository
public interface WebhookRepository extends CrudRepository<Webhook, Long> {


}
