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

@RequiredArgsConstructor
@Slf4j
@Service
public class PlaidCategoryLoader implements CategoryLoader {
    private final CategoryService categoryService;
    private final PlaidClient plaidClient;
    @Override
    public String getName() {
        return "plaid";
    }

    @Override
    public List<Category> getAllCategories() {
        return getAllCategories(true);
    }

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
