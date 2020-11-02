package com.backbase.proto.plaid.mapper;

import com.backbase.proto.plaid.client.model.LinkItem;
import com.backbase.proto.plaid.client.model.PlaidLinkRequest;
import com.backbase.proto.plaid.model.Item;
import com.backbase.proto.plaid.model.PlaidLinkResponse;
import org.mapstruct.Mapper;

@Mapper
public interface ModelToPresentationMapper {

    com.backbase.proto.plaid.client.model.PlaidLinkResponse map(PlaidLinkResponse plaidLink);

    LinkItem mapItem(com.backbase.proto.plaid.model.LinkItem plaidLink);
}
