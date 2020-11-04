package com.backbase.proto.plaid.service;

import com.backbase.proto.plaid.service.model.Category;
import com.plaid.client.PlaidClient;
import com.plaid.client.request.CategoriesGetRequest;
import com.plaid.client.response.CategoriesGetResponse;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import retrofit2.Response;

@RequiredArgsConstructor
@Slf4j
@Service
public class CategoryService {


    private final PlaidClient plaidClient;


    public List<Category> getAllCategories(boolean parentsOnly) {
        log.info("Get All Categories (parentsOnly={})", parentsOnly);
        List<Category> result = new ArrayList<>();
        try {
            Response<CategoriesGetResponse> response = plaidClient.service().categoriesGet(new CategoriesGetRequest()).execute();
            if (response.isSuccessful()) {
                List<CategoriesGetResponse.Category> categories = Objects.requireNonNull(response.body(), "Can't be null").getCategories();

                return categories.stream().map(this::map).collect(Collectors.toList());

            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        return result;
    }
    /**
     * maps the a plaid category to an enriched category
     * sets parents and reads in the last element of each category's hierarchy as this is the unique element
     *
     * @param plaidCategory the category from plaid to be added to dbs
     * @return a category that is correctly formatted to enrich a dbs a transaction
     */
    public Category map(CategoriesGetResponse.Category plaidCategory) {
        String parentID = null;
        String subParent = null;

        log.info("Mapping Plaid Category: {} with Hierarchy: {}", plaidCategory.getCategoryId(), plaidCategory.getHierarchy().size());
        switch (plaidCategory.getHierarchy().size()) {
            case 1:
                parentID = plaidCategory.getCategoryId();
                return map(plaidCategory.getCategoryId(), plaidCategory.getHierarchy().get(0), null);
            case 2:
                subParent = plaidCategory.getCategoryId();
                return map(plaidCategory.getCategoryId(), plaidCategory.getHierarchy().get(1), parentID);
            case 3:
                return map(plaidCategory.getCategoryId(), plaidCategory.getHierarchy().get(2), subParent);
            default:
                throw new IndexOutOfBoundsException("only accepts a hierarchy of height 3");
        }

    }

    /**
     * maps the attributes of a category
     *
     * @param id       category id
     * @param name     name of category
     * @param parentId category id of the parent of this category. The element above it in the hierarchy.
     * @return Category that can be used to enrich
     */
    private Category map(String id, String name, String parentId) {

        Category category = new Category();
        category.setId(id);
        category.setName(name);
        category.setParentId(parentId);
        category.setType(Category.TypeEnum.EXPENSE);
        return category;
    }


}

