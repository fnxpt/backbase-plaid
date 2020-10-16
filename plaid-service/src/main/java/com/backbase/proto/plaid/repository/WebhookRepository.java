package com.backbase.proto.plaid.repository;

import com.backbase.proto.plaid.model.Webhook;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WebhookRepository extends CrudRepository<Webhook, Long> {


}
