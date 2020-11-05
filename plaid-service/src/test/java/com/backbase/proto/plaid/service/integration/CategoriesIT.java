package com.backbase.proto.plaid.service.integration;

import com.backbase.proto.plaid.PlaidApplication;
import com.backbase.proto.plaid.service.CategoryService;
import com.backbase.proto.plaid.service.model.Category;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
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
public class CategoriesIT {

    @Autowired
    private CategoryService categoryService;

    static {
        System.setProperty("SIG_SECRET_KEY", "***REMOVED***");
    }


    @Test
    public void testCategoriesParentsOnly() {
        List<Category> categories = categoryService.getAllCategories(true);
        log.info("categories: {}", categories);

    }

    @Test
    public void testCategories() {
        List<Category> categories = categoryService.getAllCategories(false);
        log.info("categories: {}", categories);
    }


    @Test
    public void testUndoubleNames() {
        List<Category> categories = categoryService.getAllCategories(false);

        ArrayList<Category> collect = categories.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(Category::getName))), ArrayList::new));

        log.info("categories: {}", categories);
    }

}