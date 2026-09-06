package com.example.demo.controller;

import com.example.demo.model.Product;
import com.example.demo.model.ProductDetail;
import com.example.demo.model.Review;
import com.example.demo.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ProductController {

    private final ProductService productService;

    // Constructor Injection
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/products";
    }

    // ── รายการสินค้า (Read) ──
    @GetMapping("/products")
    public String listProducts(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        return "products/list";
    }

    // ── ฟอร์มเพิ่มสินค้า (Create) ──
    @GetMapping("/products/add")
    public String showAddForm(Model model) {
        Product product = new Product();
        product.setDetail(new ProductDetail());
        
        Review initialReview = new Review();
        initialReview.setRating(5);
        product.getReviews().add(initialReview);

        model.addAttribute("product", product);
        return "products/add";
    }

    // ── บันทึกสินค้าใหม่ ──
    @PostMapping("/products/save")
    public String saveProduct(@ModelAttribute("product") Product product, RedirectAttributes redirectAttributes) {
        productService.saveProduct(product);
        redirectAttributes.addFlashAttribute("message", "เพิ่มสินค้าใหม่เรียบร้อยแล้ว!");
        return "redirect:/products";
    }

    // ── ฟอร์มแก้ไขสินค้า (Update) ──
    @GetMapping("/products/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product id: " + id));
        if (product.getDetail() == null) {
            product.setDetail(new ProductDetail());
        }
        model.addAttribute("product", product);
        return "products/edit";
    }

    // ── อัปเดตข้อมูลสินค้า ──
    @PostMapping("/products/update/{id}")
    public String updateProduct(@PathVariable("id") Long id,
                                @ModelAttribute("product") Product product,
                                RedirectAttributes redirectAttributes) {
        productService.updateProduct(id, product);
        redirectAttributes.addFlashAttribute("message", "อัปเดตข้อมูลสินค้าเรียบร้อยแล้ว!");
        return "redirect:/products";
    }

    // ── หน้ายืนยันการลบ ──
    @GetMapping("/products/delete/{id}")
    public String showDeleteConfirmation(@PathVariable("id") Long id, Model model) {
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product id: " + id));
        model.addAttribute("product", product);
        return "products/delete";
    }

    // ── ดำเนินการลบสินค้า (Delete) ──
    @PostMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        productService.deleteProduct(id);
        redirectAttributes.addFlashAttribute("message", "ลบสินค้าเรียบร้อยแล้ว!");
        return "redirect:/products";
    }
}
