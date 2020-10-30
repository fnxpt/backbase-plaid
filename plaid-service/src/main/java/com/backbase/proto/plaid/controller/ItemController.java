package com.backbase.proto.plaid.controller;

import com.backbase.proto.plaid.client.api.ItemApi;
import com.backbase.proto.plaid.client.model.LinkItem;
import com.backbase.proto.plaid.mapper.LinkItemMapper;
import com.backbase.proto.plaid.model.Item;
import com.backbase.proto.plaid.service.ItemService;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * This class exposes Item API through a micro service allowing the call of end points to manage items.
 */
@RestController
@Slf4j
@RequiredArgsConstructor
public class ItemController implements ItemApi {

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

    /**
     * Gets all Linked Items for logged in user
     *
     * @return list of items with ok response
     */
    @Override
    public ResponseEntity<List<LinkItem>> getItems(@NotNull @Valid String state) {
        List<Item> allItemsByCreator = itemService.getAllItemsByCreator().stream().filter(item -> item.getInstitutionId() != null).collect(Collectors.toList());

        return ResponseEntity.ok(linkItemMapper.map(allItemsByCreator));

    }


}
