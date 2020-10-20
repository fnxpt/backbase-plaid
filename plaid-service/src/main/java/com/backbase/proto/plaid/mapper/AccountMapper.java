package com.backbase.proto.plaid.mapper;

import com.backbase.proto.plaid.model.Account;
import com.backbase.proto.plaid.model.Institution;
import com.backbase.stream.legalentity.model.AvailableBalance;
import com.backbase.stream.legalentity.model.BookedBalance;
import com.backbase.stream.legalentity.model.CreditLimit;
import com.backbase.stream.legalentity.model.Product;
import com.plaid.client.response.ItemStatus;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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

    /**
     * Maps the account data parsed in from Plaid to product in Backbase using map struct pairing names and values.
     *
     * @param accessToken this is added to additions so it may be stored with product and used later to request data
     * @param item this is used to get the Item ID for so the item which belongs to this account may be indicated
     * @param institution this is the name of the institution that the account belongs to
     * @param account this is the account that was requested from Plaid and contains the data to be mapped to Backbase
     * @return the Backbase product, containing all account data retrieved from Plaid
     */
    default Product mapToStream(String accessToken, ItemStatus item, Institution institution, com.plaid.client.response.Account account) {
        Map<String, Object> additions = new HashMap<>();
        additions.put("plaidAccessToken", accessToken);
        additions.put("plaidItemId", item.getItemId());
        additions.put("plaidInstitutionId", institution.getInstitutionId());
        additions.put("plaidAccountOfficialName", account.getOfficialName());
        additions.put("institutionName", institution.getName());
        additions.put("institutionLogo", institution.getLogo());

        Product product = new Product();
        product.setExternalId(account.getAccountId());
        product.setName(account.getName());
        product.setBankAlias(account.getName());
        product.setAdditions(additions);
        String productTypeExternalId = mapSubTypeId(account.getSubtype());
        product.setProductTypeExternalId(productTypeExternalId);
        product.setBBAN(account.getMask());

        com.plaid.client.response.Account.Balances balances = account.getBalances();
        if (balances != null) {
            product.setCurrency(balances.getIsoCurrencyCode());
            if (Objects.nonNull(balances.getAvailable())) {
                product.setAvailableBalance(new AvailableBalance()
                    .amount(BigDecimal.valueOf(balances.getAvailable()))
                    .currencyCode(balances.getIsoCurrencyCode()));
            }
            if (Objects.nonNull(balances.getCurrent())) {
                product.setBookedBalance(new BookedBalance()
                    .amount(BigDecimal.valueOf(balances.getCurrent()))
                    .currencyCode(balances.getIsoCurrencyCode()));
            }
            if (Objects.nonNull(balances.getLimit())) {
                product.setCreditLimit(new CreditLimit()
                    .amount(BigDecimal.valueOf(balances.getLimit()))
                    .currencyCode(balances.getIsoCurrencyCode()));
            }
        }
        return product;
    }



}
