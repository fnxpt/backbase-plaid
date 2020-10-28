package com.backbase.proto.plaid.service;

import com.backbase.proto.plaid.model.Item;
import com.backbase.proto.plaid.repository.ItemRepository;
import com.plaid.client.response.ErrorResponse;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ErrorHandler {

    private final ItemRepository itemRepository;

    public void handleErrorResponse(ErrorResponse errorResponse, Item item) {

        if("ITEM_LOGIN_REQUIRED".equals(errorResponse.getErrorCode())) {
            item.setExpiryDate(LocalDate.now());
        }
        item.setErrorCode(errorResponse.getErrorCode());
        item.setState(errorResponse.getErrorCode());
        item.setErrorDisplayMessage(errorResponse.getDisplayMessage());
        item.setErrorMessage(errorResponse.getErrorMessage());
        itemRepository.save(item);
    }


}
