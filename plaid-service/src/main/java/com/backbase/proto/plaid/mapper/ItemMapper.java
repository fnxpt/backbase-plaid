package com.backbase.proto.plaid.mapper;

import com.backbase.proto.plaid.model.Item;
import com.plaid.client.response.ItemPublicTokenExchangeResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Maps the item data
 */
@Mapper
public interface ItemMapper {
    /**
     * maps the data of the item identified in the token exchange response
     * @param source the access token and item id that were exchanged for a public token
     * @return the item that is identified by the item id in the response parsed in
     */
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "id", ignore = true)
    Item map(ItemPublicTokenExchangeResponse source);
}
