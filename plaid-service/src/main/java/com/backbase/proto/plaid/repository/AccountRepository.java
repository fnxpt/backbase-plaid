package com.backbase.proto.plaid.repository;

import com.backbase.proto.plaid.model.Account;
import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * used to access account database, to manipulate and use the data
 */
@Repository
public interface AccountRepository extends CrudRepository<Account, Long> {

    Account findByAccountId(String accountId);

    boolean existsByAccountId(String accountId);

    List<Account> findAllByItemId(String itemId);
}
