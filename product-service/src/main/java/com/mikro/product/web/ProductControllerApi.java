package com.mikro.product.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

public interface ProductControllerApi {

    @PostMapping
    ResponseEntity<?> create(@Valid @RequestBody ProductRequest req);

    @GetMapping("/{id}")
    ResponseEntity<?> get(@PathVariable Long id);

    @GetMapping
    ResponseEntity<?> list(@RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "10") int size);

    @PutMapping("/{id}")
    ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody ProductRequest req);

    @DeleteMapping("/{id}")
    ResponseEntity<?> delete(@PathVariable Long id);

    @PostMapping("/{id}/reduce-stock")
    ResponseEntity<?> reduceStock(@PathVariable Long id, @RequestBody StockReductionRequest request);

    record ProductRequest(
        @NotBlank String name,
        @Min(0) int stock,
        @Min(0) BigDecimal price
    ) {}
    
    record StockReductionRequest(@Min(1) int quantity) {}
}


