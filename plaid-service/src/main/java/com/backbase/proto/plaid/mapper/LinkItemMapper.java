package com.backbase.proto.plaid.mapper;

import com.backbase.proto.plaid.client.model.LinkItem;
import com.backbase.proto.plaid.model.Account;
import com.backbase.proto.plaid.model.Institution;
import com.backbase.proto.plaid.model.Item;
import com.backbase.proto.plaid.repository.AccountRepository;
import com.backbase.proto.plaid.repository.InstitutionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Maps Items that have been linked to an Item that is to be displayed in the app
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LinkItemMapper {

    private final InstitutionRepository institutionRepository;
    private final AccountRepository accountRepository;

    /**
     * maps Logged users Items for endpoint
     * @param item from our repo
     * @return Item to be returned from end point
     */
    public LinkItem map(Item item){
        LinkItem itemForDisplay = new LinkItem();
        itemForDisplay.itemId(item.getItemId());
        Institution institution = institutionRepository.getByInstitutionId(item.getInstitutionId()).orElseThrow(() -> new IllegalArgumentException("Institution not found"));
        itemForDisplay.institutionName(institution.getName());
        List<String> accounts = accountRepository.findAllByItemId(item.getItemId()).stream()
                .map(Account::getName)
                .collect(Collectors.toList());
        itemForDisplay.accounts(accounts);
        itemForDisplay.experationDate(item.getExpiryDate());
        return itemForDisplay;
    }

}
