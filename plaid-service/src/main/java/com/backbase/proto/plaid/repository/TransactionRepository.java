package com.backbase.proto.plaid.repository;

import com.backbase.proto.plaid.model.Transaction;
import io.netty.channel.oio.AbstractOioChannel;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * This class enables the use of and access to the Transactions database where data on the Plaid Transaction is stored.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Transaction findByTransactionId(Transaction s );

    boolean existsByTransactionId(String transactionId);

    void deleteTransactionsByAccountId(String accountId);

    List<Transaction> findAllByAccountId(String accountId);

    Page<Transaction> findAllByItemId(String itemId, Pageable page);



}
