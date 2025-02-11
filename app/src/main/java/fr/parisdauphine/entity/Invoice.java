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

    private Double Amount;

    public Invoice() {}

    public Invoice(Order order, Double Amount) {
        this.order = order;
        this.invoiceDate = LocalDateTime.now();
        this.Amount = Amount;
    }

    public Long getId() { return id; }

    public Order getOrder() { return order; }

    public LocalDateTime getInvoiceDate() { return invoiceDate; }

    public Double getAmount() { return Amount; }

    public void setOrder(Order order) { this.order = order; }

    public void setInvoiceDate(LocalDateTime invoiceDate) { this.invoiceDate = invoiceDate; }

    public void setAmount(Double Amount) { this.Amount = Amount; }
}
