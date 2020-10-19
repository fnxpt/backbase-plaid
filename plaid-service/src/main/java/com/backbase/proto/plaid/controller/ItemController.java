package com.backbase.proto.plaid.controller;

import com.backbase.proto.plaid.api.ItemApi;
import com.backbase.proto.plaid.service.ItemService;
import com.plaid.client.PlaidClient;
import com.plaid.client.request.ItemRemoveRequest;
import java.awt.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 */
@RestController
@Slf4j
@RequiredArgsConstructor
public class ItemController implements ItemApi {

    private final PlaidClient plaidClient;

    private final ItemService itemService;

    /**
     *
     * @param itemId
     * @return
     */
    @Override
    public ResponseEntity<Void> deleteItem(String itemId) {
        itemService.deleteItem(itemId);
        return ResponseEntity.accepted().build();
    }
}
