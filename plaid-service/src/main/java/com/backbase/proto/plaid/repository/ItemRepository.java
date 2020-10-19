package com.backbase.proto.plaid.repository;

import com.backbase.proto.plaid.model.Item;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * ItemRepository:
 * Enables the use of and access to the item database where data on the banking item is stored
 */
@Repository
public interface ItemRepository extends CrudRepository<Item, Long> {
    /**
     * Checks if an item exists by searching for it in the item database using it's id
     *
     * @param itemId id of the item being searched for
     * @return true if the item exists in the database false if it doesn't
     */
    boolean existsByItemId(String itemId);

    /**
     * Finds an item in the item table using its ID
     *
     * @param itemId identifies the item being searched for
     * @return the item object with the matching ID if it is present if not nothing is returned
     */
    Optional<Item> findByItemId(String itemId);
}
