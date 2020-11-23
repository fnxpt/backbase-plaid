package com.backbase.proto.plaid.service.integration;

import com.backbase.buildingblocks.jwt.internal.authentication.InternalJwtAuthentication;
import com.backbase.buildingblocks.jwt.internal.token.InternalJwt;
import com.backbase.buildingblocks.jwt.internal.token.InternalJwtClaimsSet;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.proto.plaid.PlaidApplication;
import com.backbase.proto.plaid.client.model.*;
import com.backbase.proto.plaid.controller.LinkController;
import com.backbase.proto.plaid.model.Item;
import com.backbase.proto.plaid.model.Webhook;
import com.backbase.proto.plaid.repository.AccountRepository;
import com.backbase.proto.plaid.repository.ItemRepository;
import com.backbase.proto.plaid.repository.WebhookRepository;
import com.backbase.proto.plaid.service.mockserver.plaid.TestMockServer;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = PlaidApplication.class
)
@Slf4j

public class LinkControllerIT extends TestMockServer {

    static {
        System.setProperty("SIG_SECRET_KEY", "***REMOVED***");
    }

    @Autowired
    private LinkController linkController;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Before
    public void setup() throws IOException {
        //AUTHENTICATION
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "lesley.knope");
        claims.put("leid", "8q73649283472");
        InternalJwtClaimsSet internalJwtClaimsSet = new InternalJwtClaimsSet(claims);
        InternalJwt internalJwt = new InternalJwt("", internalJwtClaimsSet);
        SecurityContextHolder.getContext().setAuthentication(new InternalJwtAuthentication(internalJwt));
    }

    @Test
    public void getLinkTokenTest(){
        PlaidLinkRequest plaidLinkRequest = new PlaidLinkRequest()
                .name("BackbaseTest")
                .language("en");
        PlaidLinkResponse expectedResponse = new PlaidLinkResponse().token("link-sandbox-b61203e9-2455-4fba-9cea-a438812938bb");
        Assert.assertEquals("link token was not correctly retireved", expectedResponse, linkController.requestPlaidLink(plaidLinkRequest).getBody());
    }

    @Test
    public void getAccessTokenTest(){
        SetAccessTokenRequest setAccessTokenRequest = new SetAccessTokenRequest()
                .publicToken("public-token-1gywu6twqej")
                .metadata(new Metadata().institution(new PlaidInstitution().institutionId("ins_3")));
        linkController.setPublicAccessToken(setAccessTokenRequest);
        Item actual = itemRepository.findByItemId("WGYJu6gjhA6r6ygSGYI6556456gvgha").orElseThrow(()-> new BadRequestException("Item not saved"));
        Assert.assertEquals("not linked","access-testing",actual.getAccessToken());

        Assert.assertTrue("not linked accounts ",accountRepository.existsByAccountId("DZpP9JqjRrSNnpVZArAyslbwnvQq3Btv8m9rA"));

    }
}
