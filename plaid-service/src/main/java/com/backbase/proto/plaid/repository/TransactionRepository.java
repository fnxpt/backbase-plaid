package com.backbase.proto.plaid.repository;

import com.backbase.proto.plaid.model.Transaction;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * This class enables the use of and access to the Transactions database where data on the Plaid Transaction is stored.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Transaction findTransactionByTransactionId(String transactionID);
    boolean existsByTransactionId(String transactionId);

    void deleteTransactionsByAccountId(String accountId);

    List<Transaction> findAllByAccountId(String accountId);


}
