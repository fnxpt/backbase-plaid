package com.backbase.proto.plaid.mapper;

import com.backbase.proto.plaid.model.Transaction;
import com.plaid.client.response.TransactionsGetResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface TransactionsMapper {
    @Mapping(target = "id", ignore = true)
    Transaction mapToDomain(TransactionsGetResponse.Transaction source);

    com.backbase.proto.plaid.model.Transaction.PaymentMeta map(com.plaid.client.response.TransactionsGetResponse.Transaction.PaymentMeta value);
    com.backbase.proto.plaid.model.Transaction.Location map(com.plaid.client.response.TransactionsGetResponse.Transaction.Location value);
}
