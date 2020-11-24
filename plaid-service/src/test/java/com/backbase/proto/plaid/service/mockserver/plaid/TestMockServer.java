package com.backbase.proto.plaid.service.mockserver.plaid;

import lombok.extern.slf4j.Slf4j;
import org.junit.*;

import org.mockserver.configuration.ConfigurationProperties;
import org.mockserver.integration.ClientAndServer;


import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@Slf4j
public class TestMockServer {

    private static ClientAndServer plaidMockServer;



    @BeforeClass
    public static void setUp() {

        String initializationJsonPath = "src/test/java/com/backbase/proto/plaid/service/mockserver/plaid/serverResponses.json";
        ConfigurationProperties.initializationJsonPath(initializationJsonPath);
        plaidMockServer = startClientAndServer(9090);


    }


    @AfterClass
    public static void stopServer() {
        plaidMockServer.stop();
    }

}

