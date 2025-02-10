package fr.parisdauphine.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "order_id")
    private Order order;

    private LocalDateTime invoiceDate;

    private Double totalAmount;

    public Invoice() {}

    public Invoice(Order order, Double totalAmount) {
        this.order = order;
        this.invoiceDate = LocalDateTime.now();
        this.totalAmount = totalAmount;
    }

    public Long getId() { return id; }

    public Order getOrder() { return order; }

    public LocalDateTime getInvoiceDate() { return invoiceDate; }

    public Double getTotalAmount() { return totalAmount; }

    public void setOrder(Order order) { this.order = order; }

    public void setInvoiceDate(LocalDateTime invoiceDate) { this.invoiceDate = invoiceDate; }

    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
}
