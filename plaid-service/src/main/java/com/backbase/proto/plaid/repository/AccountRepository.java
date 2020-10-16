package com.backbase.proto.plaid.repository;

import com.backbase.proto.plaid.model.Account;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends CrudRepository<Account, Long> {


}
