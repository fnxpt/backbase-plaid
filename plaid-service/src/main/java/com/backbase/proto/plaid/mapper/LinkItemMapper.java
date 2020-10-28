package com.backbase.proto.plaid.mapper;

import com.backbase.proto.plaid.model.Account;
import com.backbase.proto.plaid.model.Institution;
import com.backbase.proto.plaid.model.Item;
import com.backbase.proto.plaid.model.LinkItem;
import com.backbase.proto.plaid.repository.AccountRepository;
import com.backbase.proto.plaid.repository.InstitutionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
/**
 * Maps Items that have been linked to an Item that is to be displayed in the app
 */
public class LinkItemMapper {

    private final InstitutionRepository institutionRepository;
    private final AccountRepository accountRepository;

    /**
     * maps Logged users Items for endpoint
     * @param item from our repo
     * @return Item to be returned from end point
     */
    private LinkItem map(Item item){
        LinkItem itemForDisplay = new LinkItem();
        itemForDisplay.itemId(item.getItemId());
        Institution institution = institutionRepository.getByInstitutionId(item.getInstitutionId()).orElseThrow(() -> new IllegalArgumentException("Instition not found"));
        itemForDisplay.institutionName(institution.getName());
        List<String> accounts = accountRepository.findAllByItemId(item.getItemId()).stream().map(Account::getName).collect(Collectors.toList());
        itemForDisplay.accounts(accounts);
        itemForDisplay.experationDate(item.getExpiryDate());

        return itemForDisplay;
    }

    /**
     * Maps a list of Items from our repo to a list to be sent from an end point
     * @param items from repo
     * @return items to be displayed in app
     */
    public List<LinkItem> map(List<Item> items){
        return items.stream().map(this::map).collect(Collectors.toList());
    }
}
