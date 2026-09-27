package com.rikkeibank.transaction.exception;

public class TransferException extends RuntimeException {
    public TransferException(String message) {
        super(message);
    }
}
