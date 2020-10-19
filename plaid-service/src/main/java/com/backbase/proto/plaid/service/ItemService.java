package com.backbase.proto.plaid.service;

import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.dbs.transaction.presentation.service.api.TransactionsApi;
import com.backbase.dbs.transaction.presentation.service.model.TransactionsDeleteRequestBody;
import com.backbase.proto.plaid.model.Account;
import com.backbase.proto.plaid.model.Item;
import com.backbase.proto.plaid.repository.AccountRepository;
import com.backbase.proto.plaid.repository.ItemRepository;
import com.backbase.stream.configuration.TransactionServiceConfiguration;
import com.backbase.stream.product.service.ArrangementService;
import com.plaid.client.PlaidClient;
import com.plaid.client.request.ItemRemoveRequest;
import com.plaid.client.response.ItemRemoveResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Service;
import retrofit2.Response;

/**
 * ItemService:
 * This allows the retrieval and ingestion of account Item when it is available from plaid
 */
@Service
@RequiredArgsConstructor
@Import(TransactionServiceConfiguration.class)
@Slf4j
public class ItemService {

    private final ItemRepository itemRepository;

    private final AccountRepository accountRepository;

    private final PlaidClient plaidClient;

    private final ArrangementService arrangementService;

    private final TransactionsApi transactionsApi;

    /**
     * Deletes an item from the item database, client service, and all relevant data such as its accounts and
     * transactions will also be deleted from the other databases and services
     *
     * @param itemId identifies the item to e deleted
     */

    public void deleteItem(String itemId) {

        Item item = itemRepository.findByItemId(itemId).orElseThrow(()-> new BadRequestException("Item not found"));
        Response<ItemRemoveResponse> response = null;
        try {
            response = plaidClient.service().itemRemove(new ItemRemoveRequest(item.getAccessToken())).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(response != null && response.isSuccessful()) {
            // Delete accounts from DBS
            List<Account> accounts = accountRepository.findAllByItemId(itemId);
            accounts.forEach(account -> {
                arrangementService.deleteArrangementByExternalId(account.getAccountId()).block();
            });
            List<TransactionsDeleteRequestBody> transactionDeleteRequests = accounts.stream()
                .map(account -> new TransactionsDeleteRequestBody().arrangementId(account.getAccountId()))
                .collect(Collectors.toList());
            transactionsApi.postDelete(transactionDeleteRequests).block();
        }
        itemRepository.delete(item);



    }

    /**
     * Gets the access token of an item from the item database
     *
     * @param itemId identifies the item that the access token belongs to
     * @return the access token of the item, if the item is not present in the data base an exception is thrown
     */
    public String getAccessToken(String itemId) {
        return itemRepository.findByItemId(itemId).orElseThrow(() -> new BadRequestException("Item not found")).getAccessToken();
    }
}
