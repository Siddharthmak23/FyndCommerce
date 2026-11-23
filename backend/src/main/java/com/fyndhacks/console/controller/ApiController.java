package com.fyndhacks.console.controller;

import com.fyndhacks.console.dto.*;
import com.fyndhacks.console.entity.Product;
import com.fyndhacks.console.repository.ProductRepository;
import com.fyndhacks.console.service.OrderService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ApiController {
    private final ProductRepository products;
    private final OrderService orders;
    public ApiController(ProductRepository products, OrderService orders){this.products=products;this.orders=orders;}

    @GetMapping("/products")
    public List<ProductResponse> products() {
        return products.findAll().stream().sorted((a,b)->a.getId().compareTo(b.getId()))
                .map(p -> new ProductResponse(p.getId(),p.getName(),p.getDescription(),p.getPrice(),p.getStockQuantity())).toList();
    }

    @PostMapping("/orders")
    public ResponseEntity<OrderResponse> create(@RequestBody CreateOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orders.create(request));
    }

    @GetMapping("/orders")
    public List<OrderResponse> list(@RequestParam(required=false) String reference,
                                    @RequestParam(required=false) String status) {
        return orders.list(reference,status);
    }

    @GetMapping("/orders/{id}")
    public OrderResponse get(@PathVariable Long id) { return orders.get(id); }

    @PatchMapping("/orders/{id}/status")
    public OrderResponse status(@PathVariable Long id, @RequestBody StatusRequest request) {
        return orders.updateStatus(id,request);
    }
}
