package com.mikro.product.exception;

public class ProductAccessDeniedException extends RuntimeException {
    public ProductAccessDeniedException(String message) {
        super(message);
    }
    
    public ProductAccessDeniedException(Long productId, String owner) {
        super("Access denied. Product " + productId + " belongs to user: " + owner);
    }
}
