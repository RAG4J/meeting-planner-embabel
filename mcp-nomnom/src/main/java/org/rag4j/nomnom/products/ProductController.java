package org.rag4j.nomnom.products;

import org.rag4j.nomnom.products.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);
    
    private final MenuService menuService;
    
    public ProductController(MenuService menuService) {
        this.menuService = menuService;
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam String query) {
        logger.info("Searching for products with query: {}", query);
        
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        
        Product product = menuService.findBestMatchingProduct(query);
        
        if (product == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        
        // Return single product as list for consistency
        return ResponseEntity.ok(List.of(product));
    }
    
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        logger.info("Getting all products");
        return ResponseEntity.ok(menuService.getProducts());
    }
}
