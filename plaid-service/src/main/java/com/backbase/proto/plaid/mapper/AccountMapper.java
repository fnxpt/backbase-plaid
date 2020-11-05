package com.backbase.proto.plaid.mapper;

import com.backbase.proto.plaid.model.Account;
import com.backbase.proto.plaid.model.Institution;
import com.backbase.stream.legalentity.model.AvailableBalance;
import com.backbase.stream.legalentity.model.BookedBalance;
import com.backbase.stream.legalentity.model.CreditLimit;
import com.backbase.stream.legalentity.model.Product;
import com.plaid.client.response.ItemStatus;
import java.math.BigDecimal;
import java.util.Objects;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.ReportingPolicy;

import static com.backbase.proto.plaid.utils.ProductTypeUtils.mapSubTypeId;

/**
 * This class maps accounts from Plaid to backbase DBS, using map struct to map to stream
 * so they may then be used, processed by DBS.
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR,
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    nullValueMappingStrategy = NullValueMappingStrategy.RETURN_NULL)
public interface AccountMapper {
    /**
     * Maps the Item ID from Plaid to account.
     *
     * @param source Plaid account
     * @param itemId Identifies the Item the account belongs to
     * @return Account with the Item ID that was parsed in
     */
    @Mapping(target = "id", ignore = true)
    Account mapToDomain(com.plaid.client.response.Account source, String itemId);

}
