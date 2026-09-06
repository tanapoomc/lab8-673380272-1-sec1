package com.example.demo.strategy;

public class DiscountContext {

    private DiscountStrategy strategy;

    public DiscountContext(DiscountStrategy strategy) {
        this.strategy = strategy;
    }

    public void setStrategy(DiscountStrategy strategy) {
        this.strategy = strategy;
    }

    public double executeDiscount(double originalPrice) {
        if (strategy == null) {
            return originalPrice;
        }
        return strategy.applyDiscount(originalPrice);
    }

    public static DiscountStrategy getStrategyByType(String discountType) {
        if (discountType == null) {
            return new NoDiscountStrategy();
        }
        return switch (discountType.toUpperCase()) {
            case "MEMBER" -> new MemberDiscountStrategy();
            case "SEASONAL" -> new SeasonalSaleStrategy();
            default -> new NoDiscountStrategy();
        };
    }
}
