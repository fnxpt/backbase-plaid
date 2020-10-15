package com.backbase.proto.plaid.repository;

import com.backbase.proto.plaid.entity.Item;
import org.springframework.data.repository.CrudRepository;

public interface PlaidItemRepository extends CrudRepository<Item, Long> {


}
