package com.example.demo.service;

import com.example.demo.model.Product;
import com.example.demo.model.Review;
import com.example.demo.repository.ProductRepository;
import com.example.demo.strategy.DiscountContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    // Constructor Injection (DIP: depends on ProductRepository abstraction)
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getAllProducts() {
        List<Product> products = productRepository.findAll();
        products.forEach(this::calculateDiscountedPrice);
        return products;
    }

    public Optional<Product> getProductById(Long id) {
        Optional<Product> product = productRepository.findById(id);
        product.ifPresent(this::calculateDiscountedPrice);
        return product;
    }

    @Transactional
    public Product saveProduct(Product product) {
        // ── จัดการความสัมพันธ์ 1:1 ──
        if (product.getDetail() != null) {
            product.getDetail().setProduct(product);
        }

        // ── จัดการความสัมพันธ์ 1:N ──
        if (product.getReviews() != null) {
            for (Review review : product.getReviews()) {
                if (review.getReviewer() != null && !review.getReviewer().trim().isEmpty()) {
                    review.setProduct(product);
                    if (review.getReviewDate() == null) {
                        review.setReviewDate(LocalDate.now());
                    }
                }
            }
            // ลบ review เปล่าที่ไม่มีชื่อผู้รีวิวออก (กรณีผู้ใช้ไม่ได้กรอก)
            product.getReviews().removeIf(r -> r.getReviewer() == null || r.getReviewer().trim().isEmpty());
        }

        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(Long id, Product updatedProduct) {
        return productRepository.findById(id).map(existing -> {
            existing.setName(updatedProduct.getName());
            existing.setCategory(updatedProduct.getCategory());
            existing.setBrand(updatedProduct.getBrand());
            existing.setStock(updatedProduct.getStock());
            existing.setPrice(updatedProduct.getPrice());
            existing.setDiscountType(updatedProduct.getDiscountType());

            // ── อัปเดตข้อมูล 1:1 ProductDetail ──
            if (updatedProduct.getDetail() != null) {
                if (existing.getDetail() == null) {
                    existing.setDetail(updatedProduct.getDetail());
                } else {
                    existing.getDetail().setDescription(updatedProduct.getDetail().getDescription());
                    existing.getDetail().setWarranty(updatedProduct.getDetail().getWarranty());
                    existing.getDetail().setWeight(updatedProduct.getDetail().getWeight());
                    existing.getDetail().setDimensions(updatedProduct.getDetail().getDimensions());
                    existing.getDetail().setManufacturedCountry(updatedProduct.getDetail().getManufacturedCountry());
                }
                existing.getDetail().setProduct(existing);
            }

            return productRepository.save(existing);
        }).orElseThrow(() -> new IllegalArgumentException("ไม่พบสินค้ารหัส: " + id));
    }

    @Transactional
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    // คำนวณราคาหลังหักส่วนลดด้วย Strategy Pattern
    public void calculateDiscountedPrice(Product product) {
        if (product != null && product.getPrice() != null) {
            DiscountContext context = new DiscountContext(
                DiscountContext.getStrategyByType(product.getDiscountType())
            );
            product.setDiscountedPrice(context.executeDiscount(product.getPrice()));
        }
    }
}
