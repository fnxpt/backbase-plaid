package com.backbase.proto.plaid.provider;

import com.backbase.transaction.enrichment.provider.domain.Category;
import com.backbase.transaction.enrichment.provider.domain.Category.Builder;
import com.backbase.transaction.enrichment.provider.domain.CategoryType;
import com.plaid.client.response.CategoriesGetResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * This class maps the category response from plaid to DBS for use in transaction enrichment
 */
@RequiredArgsConstructor
public class CategoryService {
    /**
     * Keep track of the current point in the hierarchy of categories being read in
     */
    private String parentID;
    private String subParent;

    /**
     * maps the a plaid category to an enriched category
     * sets parents and reads in the last element of each category's hierarchy as this is the unique element
     * @param plaidCategory the category from plaid to be added to dbs
     * @return a category that is correctly formatted to enrich a dbs a transaction
     */
    public Category map(CategoriesGetResponse.Category plaidCategory) {

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
     * @param id category id
     * @param name name of category
     * @param parentId category id of the parent of this category. The element above it in the hierarchy.
     * @return Category that can be used to enrich
     */
    private Category map(String id, String name, String parentId) {
        Builder category = Category.builder();
        category.id(id);
        category.name(name);
        if (parentId != null)
            category.parentId(parentId);
        category.type(CategoryType.EXPENSE);
        return category.build();
    }


}
