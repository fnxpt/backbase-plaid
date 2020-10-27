package com.backbase.proto.plaid.enrichment.provider;

import com.backbase.proto.plaid.service.CategoryService;
import com.backbase.transaction.enrichment.provider.api.CategoryLoader;
import com.backbase.transaction.enrichment.provider.domain.Category;
import com.plaid.client.PlaidClient;
import com.plaid.client.request.CategoriesGetRequest;
import com.plaid.client.response.CategoriesGetResponse;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import retrofit2.Response;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * to be moved
 * Loads categories from plaid that are to be used to enrich a transaction
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class PlaidCategoryLoader implements CategoryLoader {
    private final CategoryService categoryService;
    private final PlaidClient plaidClient;

    /**
     * ges the name of the provider of categories
     * @return plaid
     */
    @Override
    public String getName() {
        return "plaid";
    }

    /**
     * gets all categories from plaid in a format that may be used
     * @return list of plaid categories
     */
    @Override
    public List<Category> getAllCategories() {
        return getAllCategories(true);
    }

    /**
     * gets all categories from plaid and returns them in a usable format, allows the return of only parent categories,
     * the roots of hierarchies in plaid categories
     * @param parentsOnly indicates if only the root categories are wanted
     * @return list of categories to enrich a transaction
     */
    public List<Category> getAllCategories(boolean parentsOnly){
        List<Category> result = new ArrayList<>();
        try {
            Response<CategoriesGetResponse> response = plaidClient.service().categoriesGet(new CategoriesGetRequest()).execute();
            if (response.isSuccessful()) {
                List<CategoriesGetResponse.Category> categories = Objects.requireNonNull(response.body(), "Can't be null").getCategories();
                List<Category> collect = categories.stream().map(category -> categoryService.map(category)).collect(Collectors.toList());

                return collect;

            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        return result;
    }
}
