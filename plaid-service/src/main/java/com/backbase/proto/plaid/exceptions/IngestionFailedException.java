package com.backbase.proto.plaid.exceptions;

import com.plaid.client.response.ErrorResponse;

@SuppressWarnings("java:S1948")
public class IngestionFailedException extends Exception {
    private final ErrorResponse errorResponse;

    public IngestionFailedException(ErrorResponse errorResponse) {
        this.errorResponse = errorResponse;
    }

    public IngestionFailedException(String message, Exception e) {
        super(message, e);
        errorResponse = null;

    }

    public ErrorResponse getErrorResponse() {
        return errorResponse;
    }
}
