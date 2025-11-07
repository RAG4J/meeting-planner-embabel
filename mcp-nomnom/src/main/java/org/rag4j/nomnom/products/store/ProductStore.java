package org.rag4j.nomnom.products.store;

import jakarta.annotation.PostConstruct;
import org.rag4j.nomnom.products.model.Category;
import org.rag4j.nomnom.products.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Repository;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static org.rag4j.nomnom.products.model.Category.*;

@Repository
public class ProductStore {
    private static final Logger logger = LoggerFactory.getLogger(ProductStore.class);
    private final VectorStore vectorStore;

    public ProductStore(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public Product findProductByName(String name) {
        logger.info("Searching for product by name: {}", name);
        SearchRequest request = SearchRequest.builder()
                .query(name)
                .build();
        List<Document> documents = vectorStore.similaritySearch(request);
        if (documents.isEmpty()) {
            return null;
        }
        Map<String, Object> metadata = documents.getFirst().getMetadata();

        String categoryName = (String) metadata.get("category");
        Category category = Category.fromName(categoryName);

        Product foundProduct = new Product(
                (String) metadata.get("id"),
                (String) metadata.get("name"),
                (String) metadata.get("description"),
                (Double) metadata.get("price"),
                category
        );

        logger.info("Found product: {} for query {}", foundProduct.name(), name);

        return foundProduct;
    }

    public List<Product> getAllProducts() {
        logger.info("Retrieving all products from vector store.");
        return List.of(
                new Product("1", "Chips", "Crispy potato chips.", 2.5, SNACKS),
                new Product("2", "Cookie", "A tasty cookie", 1.0, SNACKS),
                new Product("2", "Soda", "Carbonated soft drink.", 2.5, DRINKS),
                new Product("2", "Coffee", "Keeps the engine running.", 1.5, DRINKS),
                new Product("2", "Tea", "Carbonated soft drink.", 1.5, DRINKS),
                new Product("2", "Juice", "Carbonated soft drink.", 3.0, DRINKS),
                new Product("3", "Burger", "Juicy beef burger with cheese.", 15.0, DINER),
                new Product("3", "Pizza", "Freshly ordered near you.", 12.5, DINER),
                new Product("3", "Salad", "For the smaller appetite.", 9.5, DINER),
                new Product("3", "Sushi", "Fashionable food, all fresh.", 23.0, DINER),
                new Product("4", "Sandwich", "Have a Dutch lunch.", 5.0, LUNCH),
                new Product("4", "Wrap", "A hip lunch bite.", 5.5, LUNCH),
                new Product("4", "Soup", "For those colder days.", 3.5, LUNCH)
        );
    }

    public List<Product> findProductsByCategory(String categoryName) {
        logger.info("Finding products by category: {}", categoryName);
        return this.getAllProducts().stream()
                .filter(product -> product.category().equals(Category.fromName(categoryName)))
                .toList();
    }

    @PostConstruct
    public void initializeStore() {
        Path filePath = Paths.get(System.getProperty("user.dir"), "data","vectorstore.ser");
        if (filePath.toFile().exists()) {
            logger.info("Loading vector store: {}", filePath);
            if (vectorStore instanceof SimpleVectorStore simpleVectorStore) {
                simpleVectorStore.load(filePath.toFile());
            }
        } else {
            logger.info("Initializing vector store with sample products.");
            List<Product> products = this.getAllProducts();
            List<Document> documents = products.stream().map(product -> Document.builder()
                    .text(product.name())
                    .metadata(Map.of(
                            "id", product.id(),
                            "name", product.name(),
                            "description", product.description(),
                            "price", product.price(),
                            "category", product.category().name()
                    ))
                    .build()).toList();
            vectorStore.add(documents);

            if (vectorStore instanceof SimpleVectorStore simpleVectorStore) {
                simpleVectorStore.save(filePath.toFile());
            }
        }
    }
}
