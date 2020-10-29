package com.backbase.proto.plaid.service.integration;

import com.backbase.proto.plaid.PlaidApplication;
import lombok.extern.slf4j.Slf4j;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


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

//    @Autowired
//    private PlaidCategoryLoader plaidCategoryLoader;
//
//    @Test
//    public void testIngestAccounts() {
//
//        List<Category> allCategories = plaidCategoryLoader.getAllCategories();
//        log.info("allCategories: {}", allCategories);
//    }

}