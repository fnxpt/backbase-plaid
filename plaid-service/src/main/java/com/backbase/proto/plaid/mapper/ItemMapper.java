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
    @Mapping(target = "stateChangedDate", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "institutionId", ignore = true)
    @Mapping(target = "expiryDate", ignore = true)
    @Mapping(target = "errorMessage", ignore = true)
    @Mapping(target = "errorDisplayMessage", ignore = true)
    @Mapping(target = "errorCode", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "id", ignore = true)
    Item map(ItemPublicTokenExchangeResponse source);
}
