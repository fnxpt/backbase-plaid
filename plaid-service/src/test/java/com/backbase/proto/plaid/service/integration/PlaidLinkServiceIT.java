package com.backbase.proto.plaid.service.integration;

import com.backbase.proto.plaid.PlaidApplication;
import com.backbase.proto.plaid.service.PlaidLinkService;
import com.backbase.proto.plaid.service.PlaidTransactionsService;
import com.plaid.client.response.AccountsBalanceGetResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.tools.agent.ReactorDebugAgent;


@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = PlaidApplication.class
)
@Slf4j
public class PlaidLinkServiceIT {

    static {
        System.setProperty("SIG_SECRET_KEY", "***REMOVED***");
    }

    @Autowired
    private PlaidLinkService plaidLinkService;
    @Autowired
    private PlaidTransactionsService plaidTransactionsService;

    @Test
    public void testGetTransactions(){
        plaidTransactionsService.ingestTransactions("access-testing");
    }

    @Test
    public void testIngestAccounts() {
        plaidLinkService.ingestPlaidAccounts(
            "access-testing",
            "lesley.knope",
            "8a808094748c4ca701749668ea030012");
    }

}