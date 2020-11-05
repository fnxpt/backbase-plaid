package com.backbase.proto.plaid.repository;

import com.backbase.proto.plaid.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import org.springframework.transaction.annotation.Transactional;

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

    Page<Transaction> findAllByItemIdAndIngested(String itemId, boolean ingested, Pageable page);


    @Modifying
    @Transactional
    @Query("update Transaction set ingested = :ingested where transactionId in (:transactionIds)")
    int updateIngested(@Param("ingested") boolean ingested, @Param("transactionIds") List<String> transactionIds);



}
