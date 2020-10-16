package com.backbase.proto.plaid.mapper;

import com.backbase.proto.plaid.model.Item;
import com.plaid.client.response.ItemPublicTokenExchangeResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface ItemMapper {

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "id", ignore = true)
    Item map(ItemPublicTokenExchangeResponse source);
}
