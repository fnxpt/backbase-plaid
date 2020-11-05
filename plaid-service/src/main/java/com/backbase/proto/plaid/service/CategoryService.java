package com.backbase.proto.plaid.service;

import com.backbase.proto.plaid.service.model.Category;
import com.plaid.client.PlaidClient;
import com.plaid.client.request.CategoriesGetRequest;
import com.plaid.client.response.CategoriesGetResponse;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
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
                Map<String, Category> parents = new HashMap<>();
                Map<String, Category> subParents = new HashMap<>();

                List<Category> allCategories = categories.stream().map(
                    category -> {
                        return map(category, parents, subParents);
                    }

                ).collect(Collectors.toList());


                if (parentsOnly) {
                    return allCategories.stream().filter(category -> category.getParentId() == null).collect(Collectors.toList());
                } else {
                    return allCategories;
                }

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
    public Category map(CategoriesGetResponse.Category plaidCategory, Map<String, Category> parents, Map<String, Category> subParents) {

        log.info("Mapping Plaid Category: {} with Hierarchy: {}", plaidCategory.getCategoryId(), plaidCategory.getHierarchy().size());
        switch (plaidCategory.getHierarchy().size()) {
            case 1:
                return mapParent(plaidCategory, parents);
            case 2:
                return mapSubParent(plaidCategory, parents, subParents);
            case 3:
                return mapLeaf(plaidCategory, parents, subParents);
            default:
                throw new IndexOutOfBoundsException("only accepts a hierarchy of height 3");
        }

    }

    private Category mapLeaf(CategoriesGetResponse.Category plaidCategory, Map<String, Category> parents, Map<String, Category> subParents) {
        Category subParent = subParents.get(plaidCategory.getHierarchy().get(1));
        return map(plaidCategory.getCategoryId(), plaidCategory.getHierarchy().get(2), subParent.getId());
    }

    @NotNull
    private Category mapSubParent(CategoriesGetResponse.Category plaidCategory, Map<String, Category> parents, Map<String, Category> subParents) {
        Category parent = parents.get(plaidCategory.getHierarchy().get(0));
        Category subParent = map(plaidCategory.getCategoryId(), plaidCategory.getHierarchy().get(1), parent.getId());
        subParents.put(subParent.getName(), subParent);
        return subParent;
    }

    @NotNull
    private Category mapParent(CategoriesGetResponse.Category plaidCategory, Map<String, Category> parents) {
        Category category = map(plaidCategory.getCategoryId(), plaidCategory.getHierarchy().get(0), null);
        parents.put(category.getName(), category);
        return category;
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

