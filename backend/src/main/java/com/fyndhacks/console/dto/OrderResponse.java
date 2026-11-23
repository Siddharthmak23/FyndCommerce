package com.fyndhacks.console.dto;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(Long id, String orderReference, Instant createdAt, Instant updatedAt,
                            BigDecimal totalAmount, String status, List<OrderItemResponse> items) {
    public record OrderItemResponse(Long id, Long productId, String productName, Integer quantity,
                                    BigDecimal unitPrice, BigDecimal subtotal) {}
}
