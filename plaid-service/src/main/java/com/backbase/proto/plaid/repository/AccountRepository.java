package com.backbase.proto.plaid.repository;

import com.backbase.proto.plaid.model.Account;
import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * This class is used to access account database, to manipulate and use the data.
 */
@Repository
public interface AccountRepository extends CrudRepository<Account, Long> {

    /**
     * Finds an account stored in account table by its ID.
     *
     * @param accountId identifies the account
     * @return the account object that has the ID parsed in, if its not present it will return null
     */

    Account findByAccountId(String accountId);

    /**
     * Checks if an account exists in the database account, it's presence is search for using the account ID.
     *
     * @param accountId identifies the account being searched for
     * @return true if its is found in the database and false if not
     */
    boolean existsByAccountId(String accountId);

    /**
     * Finds all the accounts that belong to the same specified Item.
     *
     * @param itemId identifies the Item that that the accounts being searched for belong to
     * @return a list of accounts that belong to that Item
     */
    List<Account> findAllByItemId(String itemId);



    void deleteAccountsByItemId(String itemId);
}
