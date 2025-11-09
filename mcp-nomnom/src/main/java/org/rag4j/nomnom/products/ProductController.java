package org.rag4j.nomnom.products;

import com.embabel.agent.api.common.autonomy.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import org.rag4j.nomnom.agent.model.ProcessedOrder;
import org.rag4j.nomnom.agent.model.UserMessage;
import org.rag4j.nomnom.products.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);
    
    private final MenuService menuService;
    private final AgentPlatform platform;
    
    public ProductController(MenuService menuService, AgentPlatform platform) {
        this.menuService = menuService;
        this.platform = platform;
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

    @PostMapping("/order")
    public ResponseEntity<ProcessedOrder> agent(@RequestBody UserMessage userMessage, Model model) {
        var agentInvocation = AgentInvocation.create(platform, ProcessedOrder.class);

        var processedOrder = agentInvocation.invoke(userMessage);

        return ResponseEntity.ok(processedOrder);
    }
    
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        logger.info("Getting all products");
        return ResponseEntity.ok(menuService.getProducts());
    }
}
