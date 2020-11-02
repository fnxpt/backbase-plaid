package com.backbase.proto.plaid.service;

import com.backbase.buildingblocks.backend.security.auth.config.SecurityContextUtil;
import com.backbase.buildingblocks.jwt.internal.token.InternalJwt;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.buildingblocks.presentation.errors.UnauthorizedException;
import com.backbase.dbs.transaction.presentation.service.api.TransactionsApi;
import com.backbase.dbs.transaction.presentation.service.model.TransactionsDeleteRequestBody;
import com.backbase.proto.plaid.mapper.LinkItemMapper;
import com.backbase.proto.plaid.model.Account;
import com.backbase.proto.plaid.model.Item;
import com.backbase.proto.plaid.model.LinkItem;
import com.backbase.proto.plaid.repository.ItemRepository;
import com.backbase.stream.configuration.TransactionServiceConfiguration;
import com.backbase.stream.product.service.ArrangementService;
import com.plaid.client.PlaidClient;
import com.plaid.client.request.ItemRemoveRequest;
import com.plaid.client.response.ErrorResponse;
import com.plaid.client.response.ItemRemoveResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
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

    private final AccountService accountService;

    private final PlaidClient plaidClient;

    private final ArrangementService streamArrangementService;

    private final TransactionsApi transactionsApi;

    private final SecurityContextUtil securityContextUtil;

    private final LinkItemMapper linkItemMapper;

    /**
     * Deletes an item from the Item database, Client Service, and all relevant data such as its Accounts and
     * Transactions will also be deleted from the other databases and services.
     *
     * @param itemId identifies the Item to be deleted
     */
    public void deleteItem(String itemId) {

        log.info("Unlinking item: {}", itemId);
        Item item = itemRepository.findByItemId(itemId).orElseThrow(() -> new BadRequestException("Item not found"));
        String loggedInUserId = getLoggedInUserId();

        if (!item.getCreatedBy().equals(loggedInUserId)) {
            throw new UnauthorizedException("access not granted");
        }
        Response<ItemRemoveResponse> response = null;
        try {
            response = plaidClient.service().itemRemove(new ItemRemoveRequest(item.getAccessToken())).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (response != null && response.isSuccessful()) {
            deleteItemFromDBS(itemId);
            accountService.deleteAccountByItemId(item);

//            itemRepository.delete(item);
        } else {
            ErrorResponse errorResponse = plaidClient.parseError(response);
            log.error("Plaid error: {}", errorResponse.getErrorMessage());
        }
    }


    public void deleteItemFromDBS(String itemId) {
        // Delete accounts from DBS
        List<Account> accounts = accountService.findAllByItemId(itemId);
        accounts.forEach(account -> streamArrangementService.deleteArrangementByExternalId(account.getAccountId())
            .onErrorResume(WebClientResponseException.NotFound.class, e -> {
                log.info("Arrangement already deleted");
                return Mono.empty();
            })
            .block());
        List<TransactionsDeleteRequestBody> transactionDeleteRequests = accounts.stream()
            .map(account -> new TransactionsDeleteRequestBody().arrangementId(account.getAccountId()))
            .collect(Collectors.toList());
        transactionsApi.postDelete(transactionDeleteRequests)
            .onErrorResume(WebClientResponseException.NotFound.class, e -> {
                log.info("Transactions already deleted");
                return Mono.empty();
            })
            .block();
    }


    /**
     * Gets all items in the repo, used for resetting DBS and ingesting them
     *
     * @return all Items stored
     */
    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    public List<LinkItem> getAllItemsByCreator() {
        String loggedInUserId = getLoggedInUserId();
        return getItemsByUserId(loggedInUserId);
    }

    public List<LinkItem> getAllItemsByCreator(String state) {
        String loggedInUserId = getLoggedInUserId();
        return getItemsByUserId(state, loggedInUserId);
    }

    public List<LinkItem> getItemsByUserId(String loggedInUserId) {
        log.info("Get all items for: {}", loggedInUserId);
        return itemRepository.findAllByCreatedBy(loggedInUserId).stream()
                .map(linkItemMapper::map)
                .collect(Collectors.toList());
    }

    public List<LinkItem> getItemsByUserId(String state, String loggedInUserId) {
        log.info("Get all items for: {}", loggedInUserId);
        return itemRepository.findAllByCreatedBy(loggedInUserId).stream()
                .map(linkItemMapper::map)
                .collect(Collectors.toList());
    }




    private InternalJwt getInternalJwt() {
        return securityContextUtil.getOriginatingUserJwt().orElseThrow(() -> new IllegalStateException("Cannnot get internal JWT"));
    }

    private String getLoggedInUserId(InternalJwt internalJwt) {
        return internalJwt.getClaimsSet().getSubject().orElseThrow(() -> new IllegalStateException("Cannot get subject"));
    }

    private String getLoggedInUserId() {
        return getLoggedInUserId(getInternalJwt());
    }

    public boolean isValidItem(String itemId) {
        return itemRepository.existsByItemId(itemId);
    }

    public Item getValidItem(String itemId) {
        Item item = itemRepository.findByItemId(itemId).orElseThrow(() -> new BadRequestException("Item does not exist"));
        if (item.getExpiryDate() == null) {
            return item;
        } else {
            throw new BadRequestException("Item is expired");
        }
    }

    public void setPendingExpiration(Item item) {
        item.setExpiryDate(LocalDate.now().plusDays(7));
        itemRepository.save(item);
    }
}
