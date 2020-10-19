package com.backbase.proto.plaid.controller;

import com.backbase.proto.plaid.api.ItemApi;
import com.backbase.proto.plaid.service.ItemService;
import com.plaid.client.PlaidClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * ItemController:
 * Uses item api through a micro service to call end points to manage items
 */
@RestController
@Slf4j
@RequiredArgsConstructor
public class ItemController implements ItemApi {

    private final PlaidClient plaidClient;

    private final ItemService itemService;

    /**
     * Deletes an Item
     *
     * @param itemId identifies item to be deleted
     * @return http response, indicates the success of the operation
     */
    @Override
    public ResponseEntity<Void> deleteItem(String itemId) {
        itemService.deleteItem(itemId);
        return ResponseEntity.accepted().build();
    }
}
