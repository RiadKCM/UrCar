package fr.parisdauphine.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(
        name = "order_cars",
        joinColumns = @JoinColumn(name = "order_id"),
        inverseJoinColumns = @JoinColumn(name = "car_id")
    )
    private List<Car> cars = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    public enum Status {
        EN_COURS, LIVRE, ANNULE, PAYE
    }

    // Constructeurs
    public Order() {
        this.orderDate = LocalDateTime.now();
        this.status = Status.EN_COURS;
    }

    public Order(User user, List<Car> cars) {
        this.user = user;
        this.cars = cars != null ? cars : new ArrayList<>();
        this.orderDate = LocalDateTime.now();
        this.status = Status.EN_COURS;
    }

    public Order(User user, List<Car> cars, Status status) {
        this.user = user;
        this.cars = cars != null ? cars : new ArrayList<>();
        this.orderDate = LocalDateTime.now();
        this.status = status;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public LocalDateTime getdate() { return orderDate; }
    public void setdate(LocalDateTime orderDate) { this.orderDate = orderDate; }

    public List<Car> getCars() { return cars; }
    public void setCars(List<Car> cars) { this.cars = cars; }

    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    

    // Méthodes pour gérer la liste des voitures
    public void addCar(Car car) {
        this.cars.add(car);
    }

    public void removeCar(Car car) {
        this.cars.remove(car);
    }
}
