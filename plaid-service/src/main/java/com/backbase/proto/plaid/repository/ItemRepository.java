package com.backbase.proto.plaid.repository;

import com.backbase.proto.plaid.model.Item;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends CrudRepository<Item, Long> {

    Optional<Item> findByItemId(String itemId);
}
