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
 * This class allows the retrieval and ingestion of account Item when it is available from Plaid.
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
     * Deletes an item from the Item database, Client Service, and all relevant data such as its Accounts and
     * Transactions will also be deleted from the other databases and services.
     *
     * @param itemId identifies the Item to be deleted
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
