package com.backbase.proto.plaid.mapper;

import com.backbase.proto.plaid.model.Account;
import com.backbase.proto.plaid.model.Institution;
import com.backbase.stream.legalentity.model.AvailableBalance;
import com.backbase.stream.legalentity.model.BookedBalance;
import com.backbase.stream.legalentity.model.CreditLimit;
import com.backbase.stream.legalentity.model.Product;
import com.plaid.client.response.ItemStatus;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface AccountMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "itemId", ignore = true)
    Account mapToDomain(com.plaid.client.response.Account source);


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

    default String mapSubTypeId(String subtype) {
        return "external-" + subtype.replace(" ", "-");
    }

    default String mapProductType(String type, String s) {
        return s + type;
    }

}
