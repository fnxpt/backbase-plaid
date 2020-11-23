package com.backbase.proto.plaid.service.integration;

import com.backbase.proto.plaid.PlaidApplication;
import com.backbase.proto.plaid.controller.CategoryController;
import com.backbase.proto.plaid.service.mockserver.plaid.TestMockServer;
import com.backbase.proto.plaid.service.model.Category;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = PlaidApplication.class
)
@Slf4j
public class CategoryControllerIT extends TestMockServer {

    static {
        System.setProperty("SIG_SECRET_KEY", "***REMOVED***");
    }

    @Autowired
    private CategoryController categoryController;


    @Test
    public void categoryControllerTest(){
        List<Category> expectedCategoriesParents = new ArrayList<>();
        expectedCategoriesParents.add(new Category()
                .name("Bank Fees")
                .type(Category.TypeEnum.EXPENSE)
                .uniqueName("Bank Fees")
                .id("10000000"));
        expectedCategoriesParents.add(new Category()
                .name("Cash Advance")
                .type(Category.TypeEnum.EXPENSE)
                .uniqueName("Cash Advance")
                .id("11000000"));
        expectedCategoriesParents.add(new Category()
                .name("Community")
                .type(Category.TypeEnum.EXPENSE)
                .uniqueName("Community")
                .id("12000000"));

        Assert.assertEquals("categories containing only parents was not correctly retrieved",expectedCategoriesParents,categoryController.getAllCategories(true).getBody());
    }

}
