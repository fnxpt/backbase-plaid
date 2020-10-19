package com.backbase.proto.plaid.mapper;

import com.backbase.proto.plaid.model.Item;
import com.plaid.client.response.ItemPublicTokenExchangeResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * This class maps the Item data.
 */
@Mapper
public interface ItemMapper {
    /**
     * Maps the data of the Item identified in the token exchange response.
     *
     * @param source the Access Token and Item ID that were exchanged for a Public Token
     * @return the Item that is identified by the Item ID in the response parsed in
     */
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "id", ignore = true)
    Item map(ItemPublicTokenExchangeResponse source);
}
