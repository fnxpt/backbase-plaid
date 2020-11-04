package com.backbase.proto.plaid.controller;

import com.backbase.proto.plaid.configuration.PlaidConfiguration;
import com.backbase.proto.plaid.service.AccountService;
import com.backbase.stream.dbs.account.outbound.api.BalancesApi;
import com.backbase.stream.dbs.account.outbound.model.BalanceItemItem;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@Import(PlaidConfiguration.class)
public class BalanceController implements BalancesApi {

    private final AccountService accountService;

    @Override
    public ResponseEntity<List<BalanceItemItem>> getBalance(@Valid @NotNull String arrangementIds) {
        return ResponseEntity.ok(accountService.getAccountBalance(Arrays.asList(arrangementIds.split(",")))
                .stream()
                .map(account -> new BalanceItemItem()
                        .arrangementId(account.getAccountId())
                        .availableBalance(BigDecimal.valueOf(account.getBalances().getCurrent())))
                .collect(Collectors.toList()));
    }
}
