package com.backbase.proto.plaid.exceptions;

import com.plaid.client.response.ErrorResponse;

public class AccountBalanceException extends RuntimeException {
    private final ErrorResponse errorResponse;

    public AccountBalanceException(ErrorResponse errorResponse) {
        this.errorResponse = errorResponse;
    }

    public AccountBalanceException(String message, Exception e) {
        super(message, e);
        errorResponse = null;

    }

    public ErrorResponse getErrorResponse() {
        return errorResponse;
    }
}
