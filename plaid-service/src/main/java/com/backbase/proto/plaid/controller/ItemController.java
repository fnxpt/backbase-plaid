package com.backbase.proto.plaid.controller;

import com.backbase.proto.plaid.api.ItemApi;
import com.backbase.proto.plaid.mapper.LinkItemMapper;
import com.backbase.proto.plaid.model.GetItems;
import com.backbase.proto.plaid.model.Item;
import com.backbase.proto.plaid.model.LinkItem;
import com.backbase.proto.plaid.service.ItemService;
import com.plaid.client.PlaidClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This class exposes Item API through a micro service allowing the call of end points to manage items.
 */
@RestController
@Slf4j
@RequiredArgsConstructor
public class ItemController implements ItemApi {

    private final PlaidClient plaidClient;
    private final LinkItemMapper linkItemMapper;

    private final ItemService itemService;

    /**
     * Deletes an Item.
     *
     * @param itemId identifies Item to be deleted
     * @return http response, indicates the success of the operation
     */
    @Override
    public ResponseEntity<Void> deleteItem(String itemId) {
        itemService.deleteItem(itemId);
        return ResponseEntity.accepted().build();
    }

    @Override
    public ResponseEntity<GetItems> getItems() {
        List<Item> allItemsByCreator = itemService.getAllItemsByCreator();

        List<Item> items = Optional.ofNullable(allItemsByCreator).orElseThrow(() -> new IllegalArgumentException("Instition not found"));

        List<LinkItem> itemList = linkItemMapper.map(items);
        GetItems getItems = new GetItems();
        getItems.setItems(itemList);


        return ResponseEntity.ok(getItems);
    }


}
