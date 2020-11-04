package com.backbase.proto.plaid.repository;

import com.backbase.proto.plaid.model.Webhook;
import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * This class enables the use of and access to the webhook database where data on the Plaid webhook is stored
 */
@Repository
public interface WebhookRepository extends CrudRepository<Webhook, Long> {


    List<Webhook> findAllByCompleted(boolean completed);
}
