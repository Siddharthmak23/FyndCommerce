package com.fyndhacks.console.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
public class OrderItem {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="order_id", nullable=false) private Order order;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="product_id", nullable=false) private Product product;
    @Column(nullable=false) private Integer quantity;
    @Column(nullable=false, precision=12, scale=2) private BigDecimal unitPrice;
    @Column(nullable=false, precision=12, scale=2) private BigDecimal subtotal;

    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public Order getOrder(){return order;} public void setOrder(Order v){order=v;}
    public Product getProduct(){return product;} public void setProduct(Product v){product=v;}
    public Integer getQuantity(){return quantity;} public void setQuantity(Integer v){quantity=v;}
    public BigDecimal getUnitPrice(){return unitPrice;} public void setUnitPrice(BigDecimal v){unitPrice=v;}
    public BigDecimal getSubtotal(){return subtotal;} public void setSubtotal(BigDecimal v){subtotal=v;}
}
