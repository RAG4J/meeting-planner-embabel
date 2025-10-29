package org.rag4j.nomnom.products;

import com.embabel.agent.api.common.Ai;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.rag4j.nomnom.agent.LlmModel.BALANCED;

@Service
public class MenuService {
    private final static Logger logger = LoggerFactory.getLogger(MenuService.class);

    private final List<Category> categories = List.of(
        new Category("Snacks", "Tasty bites to keep you going."),
        new Category("Drinks", "Refreshing beverages to quench your thirst."),
        new Category("Diner", "Hearty meals to satisfy your hunger."),
        new Category("Lunch", "Delicious midday meals.")
    );

    private final List<Product> products = List.of(
        new Product("1", "Chips", "Crispy potato chips.", 2.5, categories.get(0)),
        new Product("2", "Cookie", "A tasty cookie", 1.0, categories.get(0)),
        new Product("2", "Soda", "Carbonated soft drink.", 2.5, categories.get(1)),
        new Product("2", "Coffee", "Keeps the engine running.", 1.5, categories.get(1)),
        new Product("2", "Tea", "Carbonated soft drink.", 1.5, categories.get(1)),
        new Product("2", "Juice", "Carbonated soft drink.", 3.0, categories.get(1)),
        new Product("3", "Burger", "Juicy beef burger with cheese.", 15.0, categories.get(2)),
        new Product("3", "Pizza", "Freshly ordered near you.", 12.5, categories.get(2)),
        new Product("3", "Salad", "For the smaller appetite.", 9.5, categories.get(2)),
        new Product("3", "Sushi", "Fashionable food, all fresh.", 23.0, categories.get(2)),
        new Product("4", "Sandwich", "Have a Dutch lunch.", 5.0, categories.get(3)),
        new Product("4", "Wrap", "A hip lunch bite.", 5.5, categories.get(3)),
        new Product("4", "Soup", "For those colder days.", 3.5, categories.get(3))
    );

    private final Ai ai;
    public MenuService(Ai ai) {
        logger.info("Init Menu Service");
        this.ai = ai;
    }

    public Category findCategoryByName(String name) {
        return categories.stream()
                .filter(category -> category.name().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public Product findProductByName(String name) {
        return products.stream()
                .filter(product -> product.name().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    @Tool(description = "Finds the best matching product for a provided product name.")
    public Product findBestMatchingProduct(String providedName) {
        logger.info("Finding best matching product for provided product name: {}", providedName);

        Product foundProduct = findProductByName(providedName);

        if (foundProduct == null) {
            logger.info("No Exact match found for product name: {}", providedName);

            String prompt = String.format("""
                    You are given a list of product names and their details.
                    Return only the name of the product that is most similar to the provided product.
                    Work with the name but also think about what the requested product is and what would best match.
                    Always return only the name of the product, nothing else.
                    If no product matches well, return 'not-found'.
                    
                    Requested product: %s
                  
                    Available products:
                    %s
                    """, providedName, productsListAsString());

            String productName = ai.withLlmByRole(BALANCED.getModelName())
                    .generateText(prompt);

            logger.info("Best matching product name found: {}", productName);

            foundProduct = findProductByName(productName.trim());

            if (foundProduct == null) {
                logger.info("No matching product found for name: {}", productName);
            }
        } else {
            logger.info("Exact match found for product name: {}", providedName);
        }

        return foundProduct;
    }

    public List<Product> findProductsByCategory(String categoryName) {
        return products.stream()
                .filter(product -> product.category().name().equalsIgnoreCase(categoryName))
                .toList();
    }

    public List<Category> getCategories() {
        return categories.stream().toList();
    }

    public List<Product> getProducts() {
        return products.stream().toList();
    }

    private String productsListAsString() {
        List<String> productNames = products.stream().map(Product::name).toList();
        return String.join(", ", productNames);
    }

}
