package com.mikro.product.exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String message) {
        super(message);
    }
    
    public InsufficientStockException(Long productId, int available, int requested) {
        super("Insufficient stock for product " + productId + ". Available: " + available + ", Requested: " + requested);
    }
}
