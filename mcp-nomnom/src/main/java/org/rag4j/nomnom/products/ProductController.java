package org.rag4j.nomnom.products;

import com.embabel.agent.api.common.autonomy.AgentInvocation;
import com.embabel.agent.core.Agent;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.core.AgentProcess;
import com.embabel.agent.core.ProcessOptions;
import com.embabel.agent.core.hitl.FormBindingRequest;
import com.embabel.agent.core.hitl.FormResponse;
import com.embabel.ux.form.FormSubmission;
import org.rag4j.nomnom.agent.HandleOrderAgent;
import org.rag4j.nomnom.agent.model.ProcessedOrder;
import org.rag4j.nomnom.agent.model.UserMessage;
import org.rag4j.nomnom.orders.OrderService;
import org.rag4j.nomnom.products.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

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
    public ResponseEntity<ProcessedOrder> agent(@RequestBody UserMessage userMessage) {

//        List<Agent> agents = platform.agents();
//        logger.info("Available agents: {}", agents);
//
//        Agent agent = agents.getFirst();
//
//        var process = platform.createAgentProcess(agent, ProcessOptions.builder().build(), Map.of("input",
//                userMessage));
//        var completedProcess = process.run();
//
//        var processId = completedProcess.getId();
//
//        var lastResult = completedProcess.lastResult();
//
//        if (lastResult instanceof FormBindingRequest) {
//            var r = (FormBindingRequest) lastResult;
//            r.getPayload()
//        }
//
//        FormResponse formResponse = new FormResponse(
//                processId,
//                processId,
//                new FormSubmission()
//        );

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
