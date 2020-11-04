package com.backbase.proto.plaid.controller;

import com.backbase.proto.plaid.service.CategoryService;
import com.backbase.proto.plaid.service.api.CategoriesApi;
import com.backbase.proto.plaid.service.model.Category;
import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * This class sets up and builds a Plaid Webhook, this webhook notifies DBS when data is available for retrieval.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class CategoryController implements CategoriesApi {

    private final CategoryService categoryService;

    @Override
    public ResponseEntity<List<Category>> getAllCategories(@Valid Boolean parentsOnly) {
        if(parentsOnly == null) {
            parentsOnly = false;
        }
        return ResponseEntity.ok(categoryService.getAllCategories(parentsOnly));
    }
}
