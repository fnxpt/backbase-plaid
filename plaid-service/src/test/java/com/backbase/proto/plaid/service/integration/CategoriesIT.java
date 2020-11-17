package com.backbase.proto.plaid.service.integration;

import com.backbase.proto.plaid.PlaidApplication;
import com.backbase.proto.plaid.service.CategoryService;
import com.backbase.proto.plaid.service.mockserver.plaid.TestMockServer;
import com.backbase.proto.plaid.service.model.Category;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Categories;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = PlaidApplication.class
)
@Slf4j
public class CategoriesIT extends TestMockServer {

    @Autowired
    private CategoryService categoryService;

    static {
        System.setProperty("SIG_SECRET_KEY", "***REMOVED***");
    }


    @Test
    public void testCategoriesParentsOnly() {
        List<Category> categories = categoryService.getAllCategories(true);
        log.info("categories: {}", categories);

        Category category1 = new Category().id("10000000").name("Bank Fees").type(Category.TypeEnum.EXPENSE);
        Category category2 = new Category().id("11000000").name("Cash Advance").type(Category.TypeEnum.EXPENSE);
        Category category3 = new Category().id("12000000").name("Community").type(Category.TypeEnum.EXPENSE);
        List<Category> categoriesExpected = new ArrayList<>();
        categoriesExpected.add(category1);
        categoriesExpected.add(category2);
        categoriesExpected.add(category3);

        Assert.assertEquals("doesn't match expected number of categories", 3,categories.size());
        Assert.assertEquals("doesn't match expected categories",categoriesExpected,categories);

    }

    @Test
    public void testCategories() {
        List<Category> categories = categoryService.getAllCategories(false);
        log.info("categories: {}", categories);
        Assert.assertEquals("doesn't match expected number of categories",55,categories.size());
    }


    @Test
    public void testUndoubleNames() {
        List<Category> categories = categoryService.getAllCategories(false);

        ArrayList<Category> collect = categories.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(Category::getName))), ArrayList::new));
        log.info("categories: {}", categories);

        Assert.assertNotEquals("Duplicates not handeled", collect.size(),categories.size());


    }

}