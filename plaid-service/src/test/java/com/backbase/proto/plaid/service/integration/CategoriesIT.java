package com.backbase.proto.plaid.service.integration;

import com.backbase.proto.plaid.PlaidApplication;
import com.backbase.proto.plaid.enrichment.provider.PlaidCategoryLoader;
import com.backbase.proto.plaid.service.AccountService;
import com.backbase.proto.plaid.service.CategoryService;
import com.backbase.transaction.enrichment.provider.domain.Category;
import lombok.extern.slf4j.Slf4j;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;


@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = PlaidApplication.class
)
@Slf4j
@Ignore
public class CategoriesIT {

    static {
        System.setProperty("SIG_SECRET_KEY", "***REMOVED***");
    }

    @Autowired
    private PlaidCategoryLoader plaidCategoryLoader;

    @Test
    public void testIngestAccounts() {

        List<Category> allCategories = plaidCategoryLoader.getAllCategories();
        log.info("allCategories: {}", allCategories);
    }

}