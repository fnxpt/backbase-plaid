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
//        new MockServerClient("localhost",1080).when(
//                request()
//        ).respond(
//                response().withBody("heyp")
//        );

        String initializationJsonPath = "src/test/java/com/backbase/proto/plaid/service/mockserver/plaid/serverResponses.json";
        ConfigurationProperties.initializationJsonPath(initializationJsonPath);
        plaidMockServer= startClientAndServer(9090);



    }
//    @Test
//    public void testMock() throws IOException {
//        Expectation[] expectations = mockServer.retrieveActiveExpectations(request().withMethod("GET").withPath("/transactions/get").withBody("\"client_id\": \"***REMOVED***\",\n" +
//                "        \"secret\": \"***REMOVED***\",\n" +
//                "        \"access_token\": \"access-testing\",\n" +
//                "        \"start_date\": \"2017-01-01\",\n" +
//                "        \"end_date\": \"2019-05-10\""));
//        log.info("exceptions: {}",expectations);
//        HttpRequest httpRequest = request("/transactions/get");
//        HttpResponse response = response();
//        log.info("request {}, response{}", httpRequest, response);
////        URL url = new URL("http://localhost:1080/mockserver");
////        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//
//
//
//
//
//
//
//
//
//
//
//
//
//    }


    @AfterClass
    public static void stopServer() {
        plaidMockServer.stop();
    }

}

