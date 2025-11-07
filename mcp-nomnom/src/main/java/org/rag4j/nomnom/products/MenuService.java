package org.rag4j.nomnom.products;

import org.rag4j.nomnom.products.model.Category;
import org.rag4j.nomnom.products.model.Product;
import org.rag4j.nomnom.products.store.ProductStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MenuService {
    private final static Logger logger = LoggerFactory.getLogger(MenuService.class);

    private final ProductStore productStore;

    public MenuService(ProductStore productStore) {
        logger.info("Init Menu Service");
        this.productStore = productStore;
    }

    public Category findCategoryByName(String name) {
        return Category.fromName(name);
    }

    @Tool(description = "Finds the best matching product for a provided product name.")
    public Product findBestMatchingProduct(String providedName) {
        logger.info("Finding best matching product for provided product name: {}", providedName);

        Product foundProduct = productStore.findProductByName(providedName);

        if (foundProduct == null) {
            logger.info("No Exact match found for product name: {}", providedName);
        }

        return foundProduct;
    }

    public List<Category> getCategories() {
        return List.of(Category.values());
    }

    public List<Product> getProducts() {
        return productStore.getAllProducts();
    }

    public List<Product> findProductsByCategory(String categoryName) {
        return productStore.findProductsByCategory(categoryName);
    }
}
