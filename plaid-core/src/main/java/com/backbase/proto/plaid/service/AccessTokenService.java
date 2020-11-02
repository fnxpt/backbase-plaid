package com.backbase.proto.plaid.service;

import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.proto.plaid.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccessTokenService {

    private final ItemRepository itemRepository;

    /**
     * Gets the Access Token of an Item from the Item database.
     *
     * @param itemId identifies the Item that the Access Token belongs to
     * @return the Access Token of the Item, if the Item is not present in the data base an exception is thrown
     * @throws BadRequestException When Item is not found
     */
    public String getAccessToken(String itemId) {
        return itemRepository.findByItemId(itemId).orElseThrow(() -> new BadRequestException("Item not found")).getAccessToken();
    }


}