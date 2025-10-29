package org.rag4j.nomnom.controller;

import org.rag4j.nomnom.products.Category;
import org.rag4j.nomnom.products.MenuService;
import org.rag4j.nomnom.products.Product;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class NomNomDocumentationController {

    private final MenuService menuService;

    public NomNomDocumentationController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping("/")
    public String index(Model model) {
        List<Category> categories = menuService.getCategories();
        List<Product> products = menuService.getProducts();
        
        // Group products by category for display
        Map<String, List<Product>> productsByCategory = products.stream()
                .collect(Collectors.groupingBy(p -> p.category().name()));
        
        model.addAttribute("categories", categories);
        model.addAttribute("products", products);
        model.addAttribute("productsByCategory", productsByCategory);
        model.addAttribute("serverName", "NomNom Food Service");
        model.addAttribute("version", "1.0.0");
        model.addAttribute("description", "AI-powered food ordering service for meetings. Order delicious food and drinks for your team!");
        model.addAttribute("totalProducts", products.size());
        
        return "index";
    }

    @GetMapping("/docs")
    public String documentation(Model model) {
        return "redirect:/";
    }
    
    @GetMapping("/menu")
    public String menu(Model model) {
        List<Category> categories = menuService.getCategories();
        List<Product> products = menuService.getProducts();
        
        // Group products by category
        Map<String, List<Product>> productsByCategory = products.stream()
                .collect(Collectors.groupingBy(p -> p.category().name()));
        
        model.addAttribute("categories", categories);
        model.addAttribute("productsByCategory", productsByCategory);
        model.addAttribute("totalProducts", products.size());
        
        return "menu";
    }
    
    @GetMapping("/category/{categoryName}")
    public String categoryProducts(@PathVariable String categoryName, Model model) {
        Category category = menuService.findCategoryByName(categoryName);
        if (category == null) {
            return "redirect:/menu";
        }
        
        List<Product> products = menuService.findProductsByCategory(categoryName);
        
        model.addAttribute("category", category);
        model.addAttribute("products", products);
        
        return "category";
    }
}
