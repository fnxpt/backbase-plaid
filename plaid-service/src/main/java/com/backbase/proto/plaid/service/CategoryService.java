package com.backbase.proto.plaid.service;

import com.backbase.transaction.enrichment.provider.domain.Category.Builder;
import com.backbase.transaction.enrichment.provider.domain.Category;

import com.backbase.transaction.enrichment.provider.domain.CategoryType;
import com.plaid.client.PlaidClient;
import com.plaid.client.request.CategoriesGetRequest;
import com.plaid.client.response.CategoriesGetResponse;
import com.plaid.client.response.TransactionsGetResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import retrofit2.Response;

import javax.websocket.server.ServerEndpoint;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class CategoryService {

    private static String parentID;
    private static String subParent;


    public Category map(CategoriesGetResponse.Category plaidCategory) {

        switch (plaidCategory.getHierarchy().size()){
            case 1:
                parentID= plaidCategory.getCategoryId();
                return map(plaidCategory.getCategoryId(), plaidCategory.getHierarchy().get(0), null);
            case 2:
                subParent= plaidCategory.getCategoryId();
                return map(plaidCategory.getCategoryId(), plaidCategory.getHierarchy().get(1), parentID);
            case 3:
                return map(plaidCategory.getCategoryId(), plaidCategory.getHierarchy().get(2), subParent);
        }

        return null;
    }

    private Category map(String id, String name, String parentId){
        Builder category = Category.builder();
        category.id(id);
        category.name(name);
        if(parentId!=null)
            category.parentId(parentId);
        category.type(CategoryType.EXPENSE);
        return category.build();
    }




}
