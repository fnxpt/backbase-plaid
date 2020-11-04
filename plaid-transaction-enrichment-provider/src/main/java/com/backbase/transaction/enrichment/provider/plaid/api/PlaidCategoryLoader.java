package com.backbase.transaction.enrichment.provider.plaid.api;

import com.backbase.proto.plaid.service.api.CategoriesApi;
import com.backbase.transaction.enrichment.provider.api.CategoryLoader;
import com.backbase.transaction.enrichment.provider.domain.Category;
import com.backbase.transaction.enrichment.provider.domain.CategoryType;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;

/**
 * to be moved
 * Loads categories from plaid that are to be used to enrich a transaction
 */
@Slf4j
@RequiredArgsConstructor
public class PlaidCategoryLoader implements CategoryLoader {


    private final CategoriesApi categoriesApi;

    /**
     * ges the name of the provider of categories
     *
     * @return plaid
     */
    @Override
    public String getName() {
        return "plaid";
    }

    /**
     * gets all categories from plaid in a format that may be used
     *
     * @return list of plaid categories
     */
    @Override
    public List<Category> getAllCategories() {
        return getAllCategories(true);
    }

    /**
     * gets all categories from plaid and returns them in a usable format, allows the return of only parent categories,
     * the roots of hierarchies in plaid categories
     *
     * @param parentsOnly indicates if only the root categories are wanted
     * @return list of categories to enrich a transaction
     */
    public List<Category> getAllCategories(boolean parentsOnly) {
        log.info("Get All Categories (parentsOnly={})", parentsOnly);

        List<com.backbase.proto.plaid.service.model.Category> allCategories = categoriesApi.getAllCategories(parentsOnly);
        return mapCategories(allCategories);

    }

    private List<Category> mapCategories(List<com.backbase.proto.plaid.service.model.Category> allCategories) {
        return allCategories.stream().map(this::mapCategory).collect(Collectors.toList());
    }

    private  Category mapCategory(com.backbase.proto.plaid.service.model.Category category) {
        return Category.builder()
            .id(Objects.requireNonNull(category.getId()))
            .name(Objects.requireNonNull(category.getName()))
            .parentId(Objects.requireNonNull(category.getParentId()))
            .type(CategoryType.valueOf(Objects.requireNonNull(category.getType()).getValue()))
            .build();
    }
}
