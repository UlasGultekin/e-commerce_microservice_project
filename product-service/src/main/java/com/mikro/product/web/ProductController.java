package com.mikro.product.web;

import com.mikro.product.domain.Product;
import com.mikro.product.domain.ProductRepository;
import com.mikro.product.exception.ProductAccessDeniedException;
import com.mikro.product.exception.ProductNotFoundException;
import com.mikro.product.exception.InsufficientStockException;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController implements ProductControllerApi {
    private final ProductRepository repository;

    public ProductController(ProductRepository repository) {
        this.repository = repository;
    }

    @Override
    public ResponseEntity<?> create(ProductRequest req) {
        Product p = Product.builder()
            .name(req.name())
            .stock(req.stock())
            .price(req.price())
            .ownerUsername(org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication() != null ?
                String.valueOf(org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal()) :
                "unknown")
            .build();
        return ResponseEntity.ok(repository.save(p));
    }

    @Override
    public ResponseEntity<?> get(Long id) {
        Product product = repository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException(id));
        return ResponseEntity.ok(product);
    }

    @Override
    public ResponseEntity<?> list(int page, int size) {
        return ResponseEntity.ok(repository.findAll(PageRequest.of(page, size)));
    }

    @Override
    public ResponseEntity<?> update(Long id, ProductRequest req) {
        String currentUser = String.valueOf(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        Product product = repository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException(id));
            
        if (!product.getOwnerUsername().equals(currentUser)) {
            throw new ProductAccessDeniedException(id, product.getOwnerUsername());
        }
        
        product.setName(req.name());
        product.setPrice(req.price());
        product.setStock(req.stock());
        return ResponseEntity.ok(repository.save(product));
    }

    @Override
    public ResponseEntity<?> delete(Long id) {
        String currentUser = String.valueOf(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        Product product = repository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException(id));
            
        if (!product.getOwnerUsername().equals(currentUser)) {
            throw new ProductAccessDeniedException(id, product.getOwnerUsername());
        }
        
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @Transactional
    public ResponseEntity<?> reduceStock(Long id, StockReductionRequest request) {
        Product product = repository.findByIdWithLock(id)
            .orElseThrow(() -> new ProductNotFoundException(id));
            
        if (product.getStock() < request.quantity()) {
            throw new InsufficientStockException(id, product.getStock(), request.quantity());
        }
        
        int updatedRows = repository.reduceStock(id, request.quantity());
        if (updatedRows == 0) {
            throw new InsufficientStockException("Concurrent stock reduction occurred, please try again");
        }
        
        // Refresh entity to get updated stock
        Product updatedProduct = repository.findById(id).orElseThrow();
        return ResponseEntity.ok(updatedProduct);
    }

}


