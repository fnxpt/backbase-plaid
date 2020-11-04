package com.backbase.proto.plaid.controller;

import com.backbase.proto.plaid.configuration.PlaidConfiguration;
import com.backbase.stream.dbs.account.outbound.api.ArrangementDetailsApi;
import com.backbase.stream.dbs.account.outbound.model.ArrangementDetails;
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
public class ArrangementDetailsController implements ArrangementDetailsApi {


    @Override
    public ResponseEntity<ArrangementDetails> getArrangementDetails(@Valid @NotNull String arrangementId) {
        return null;
    }
}
