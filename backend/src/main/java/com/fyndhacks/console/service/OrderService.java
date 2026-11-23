package com.fyndhacks.console.service;

import com.fyndhacks.console.dto.*;
import com.fyndhacks.console.entity.*;
import com.fyndhacks.console.exception.ApiExceptions.*;
import com.fyndhacks.console.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
public class OrderService {
    private final ProductRepository products;
    private final OrderRepository orders;

    public OrderService(ProductRepository products, OrderRepository orders) {
        this.products=products; this.orders=orders;
    }

    @Transactional
    public OrderResponse create(CreateOrderRequest request) {
        if (request==null || request.items()==null || request.items().isEmpty())
            throw new BadRequestException("An order must contain at least one item");

        Map<Long,Integer> quantities=new LinkedHashMap<>();
        for (CreateOrderRequest.OrderItemRequest item:request.items()) {
            if (item.productId()==null) throw new BadRequestException("Product ID is required");
            if (item.quantity()==null || item.quantity()<1)
                throw new BadRequestException("Quantities must be positive");
            if (quantities.putIfAbsent(item.productId(), item.quantity())!=null)
                throw new BadRequestException("Duplicate product IDs are not allowed");
        }

        List<Long> ids=quantities.keySet().stream().sorted().toList();
        List<Product> locked=products.findAllByIdLocked(ids);
        if (locked.size()!=ids.size())
            throw new NotFoundException("One or more products do not exist");

        for (Product product:locked) {
            int quantity=quantities.get(product.getId());
            if (product.getStockQuantity()<quantity)
                throw new ConflictException("Insufficient stock for " + product.getName());
        }

        Order order=new Order();
        order.setOrderReference(generateReference());
        order.setStatus(OrderStatus.PLACED);
        BigDecimal total=BigDecimal.ZERO;
        for (Product product:locked) {
            int quantity=quantities.get(product.getId());
            product.setStockQuantity(product.getStockQuantity()-quantity);
            BigDecimal subtotal=product.getPrice().multiply(BigDecimal.valueOf(quantity));
            total=total.add(subtotal);
            OrderItem item=new OrderItem();
            item.setProduct(product);
            item.setQuantity(quantity);
            item.setUnitPrice(product.getPrice());
            item.setSubtotal(subtotal);
            order.addItem(item);
        }
        order.setTotalAmount(total);
        return toResponse(orders.save(order));
    }

    @Transactional(readOnly=true)
    public List<OrderResponse> list(String reference, String status) {
        List<Order> result;
        OrderStatus filter=null;
        if (status!=null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            try { filter=OrderStatus.valueOf(status.toUpperCase()); }
            catch (IllegalArgumentException ex) { throw new BadRequestException("Invalid order status"); }
        }
        if (reference!=null && !reference.isBlank() && filter!=null)
            result=orders.findByOrderReferenceContainingIgnoreCaseAndStatusOrderByCreatedAtDesc(reference, filter);
        else if (reference!=null && !reference.isBlank())
            result=orders.findByOrderReferenceContainingIgnoreCaseOrderByCreatedAtDesc(reference);
        else if (filter!=null)
            result=orders.findByStatusOrderByCreatedAtDesc(filter);
        else
            result=orders.findAllByOrderByCreatedAtDesc();
        return result.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly=true)
    public OrderResponse get(Long id) {
        return toResponse(orders.findById(id).orElseThrow(() -> new NotFoundException("Order not found")));
    }

    @Transactional
    public OrderResponse updateStatus(Long id, StatusRequest request) {
        if (request==null || request.status()==null || request.status().isBlank())
            throw new BadRequestException("Status is required");
        Order order=orders.findByIdLocked(id).orElseThrow(() -> new NotFoundException("Order not found"));
        OrderStatus requested;
        try { requested=OrderStatus.valueOf(request.status().trim().toUpperCase()); }
        catch (IllegalArgumentException ex) { throw new BadRequestException("Invalid order status"); }

        if (requested==OrderStatus.CANCELLED) {
            if (order.getStatus()!=OrderStatus.PLACED && order.getStatus()!=OrderStatus.PACKED)
                throw new ConflictException("This order cannot be cancelled");
            restoreStock(order);
            order.setStatus(OrderStatus.CANCELLED);
        } else if (requested==OrderStatus.PACKED) {
            if (order.getStatus()!=OrderStatus.PLACED)
                throw new ConflictException("Only PLACED orders can be marked as PACKED");
            order.setStatus(OrderStatus.PACKED);
        } else if (requested==OrderStatus.SHIPPED) {
            if (order.getStatus()!=OrderStatus.PACKED)
                throw new ConflictException("Only PACKED orders can be marked as SHIPPED");
            order.setStatus(OrderStatus.SHIPPED);
        } else {
            throw new ConflictException("Invalid status transition");
        }
        return toResponse(orders.save(order));
    }

    private void restoreStock(Order order) {
        List<Long> ids=order.getItems().stream().map(i -> i.getProduct().getId()).sorted().toList();
        Map<Long,Product> locked=products.findAllByIdLocked(ids).stream()
                .collect(java.util.stream.Collectors.toMap(Product::getId, p->p));
        for (OrderItem item:order.getItems()) {
            Product product=locked.get(item.getProduct().getId());
            product.setStockQuantity(product.getStockQuantity()+item.getQuantity());
        }
    }

    private String generateReference() {
        return "ORD-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    private OrderResponse toResponse(Order order) {
        List<OrderResponse.OrderItemResponse> items=order.getItems().stream()
                .map(i -> new OrderResponse.OrderItemResponse(i.getId(), i.getProduct().getId(),
                        i.getProduct().getName(), i.getQuantity(), i.getUnitPrice(), i.getSubtotal()))
                .toList();
        return new OrderResponse(order.getId(), order.getOrderReference(), order.getCreatedAt(),
                order.getUpdatedAt(), order.getTotalAmount(), order.getStatus().name(), items);
    }
}
