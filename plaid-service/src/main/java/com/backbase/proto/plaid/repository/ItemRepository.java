package com.backbase.proto.plaid.repository;

import com.backbase.proto.plaid.model.Item;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Enables the use of and access to the item database where data on the banking item is stored
 */
@Repository
public interface ItemRepository extends CrudRepository<Item, Long> {

    boolean existsByItemId(String itemId);

    Optional<Item> findByItemId(String itemId);
}
