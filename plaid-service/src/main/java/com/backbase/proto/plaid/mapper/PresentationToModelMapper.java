package com.backbase.proto.plaid.mapper;

import com.backbase.proto.plaid.client.model.PlaidLinkRequest;
import com.backbase.proto.plaid.model.SetAccessTokenRequest;
import org.mapstruct.Mapper;

@Mapper
public interface PresentationToModelMapper {

    com.backbase.proto.plaid.model.PlaidLinkRequest  map(PlaidLinkRequest plaidLinkRequest);

    SetAccessTokenRequest mapAccessTokenRequest(com.backbase.proto.plaid.client.model.SetAccessTokenRequest setAccessTokenRequest);
}
