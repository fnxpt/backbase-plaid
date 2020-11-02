package com.backbase.proto.plaid.controller;

import com.backbase.proto.plaid.client.api.LinkApi;
import com.backbase.proto.plaid.client.model.PlaidLinkRequest;
import com.backbase.proto.plaid.client.model.PlaidLinkResponse;
import com.backbase.proto.plaid.client.model.SetAccessTokenRequest;
import com.backbase.proto.plaid.mapper.ModelToPresentationMapper;
import com.backbase.proto.plaid.mapper.PresentationToModelMapper;
import com.backbase.proto.plaid.service.LinkService;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * This class exposes Link API allowing the call of link endpoints that allow a link to be initialised.
 */
@RestController
@Slf4j

public class LinkController implements LinkApi {
    /**
     * Plaid link service contains the client the webhook and all the tools required to communicate with
     * Plaid.
     */
    private final LinkService linkService;
    private final PresentationToModelMapper presentationToModelMapper = Mappers.getMapper(PresentationToModelMapper.class);
    private final ModelToPresentationMapper modelToPresentationMapper = Mappers.getMapper(ModelToPresentationMapper.class);
    /**
     * Initialises Link Service.
     *
     * @param linkService sets the link service of this class
     */
    public LinkController(LinkService linkService) {
        this.linkService = linkService;
        log.info("Plaid Controller created");
    }

    /**
     * Sends a request for a Plaid Link Token the request being parsed in and the link then stored.
     *
     * @param plaidLinkRequest  (optional) contains the request body for fetching a Plaid link
     * @return http response indicating the success of the call
     */
    @Override
    public ResponseEntity<PlaidLinkResponse> requestPlaidLink(@Valid PlaidLinkRequest plaidLinkRequest) {
        log.info("Requesting Plaid Link: {}", plaidLinkRequest);
        return ResponseEntity.ok( modelToPresentationMapper.map(linkService.createPlaidLink(presentationToModelMapper.map(plaidLinkRequest))) );
    }

    /**
     * Sends a request for the Access Token using a parsed request and returns and entity that can be stored in a
     * database.
     *
     * @param setAccessTokenRequest  (optional) contains the request body for fetching the Access Token, the body
     *                               contains the public key
     * @return http response indicating the success of the call
     */
    @Override
    public ResponseEntity<Void> setPublicAccessToken(@Valid SetAccessTokenRequest setAccessTokenRequest) {
        log.info("Set Plaid Public Token: {}", setAccessTokenRequest);
        linkService.setPublicAccessToken(presentationToModelMapper.mapAccessTokenRequest(setAccessTokenRequest));
        return ResponseEntity.accepted().build();
    }


}
