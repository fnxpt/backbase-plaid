package com.backbase.proto.plaid.mapper;

import com.backbase.proto.plaid.model.Location;
import com.backbase.proto.plaid.model.PaymentMeta;
import com.backbase.proto.plaid.model.Transaction;
import com.plaid.client.response.TransactionsGetResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface PlaidToModelTransactionsMapper {
    @Mapping(target = "id", ignore = true)
    Transaction mapToDomain(TransactionsGetResponse.Transaction source);

    PaymentMeta map(com.plaid.client.response.TransactionsGetResponse.Transaction.PaymentMeta value);
    @Mapping(target = "store_number", source = "storeNumber")
    @Mapping(target = "postal_code", source = "postalCode")
    Location map(com.plaid.client.response.TransactionsGetResponse.Transaction.Location value);
}
