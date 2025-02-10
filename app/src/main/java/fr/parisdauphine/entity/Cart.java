package fr.parisdauphine.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "cart")
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToMany
    @JoinTable(
            name = "cart_cars",
            joinColumns = @JoinColumn(name = "cart_id"),
            inverseJoinColumns = @JoinColumn(name = "car_id")
    )
    private List<Car> cars; // ✅ Remplacement de "stocks" par "cars"

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Car> getCars() {
        return cars;
    }

    public void setCars(List<Car> cars) {
        this.cars = cars;
    }

    // ✅ Méthode pour ajouter une voiture au panier
    public void addCar(Car car) {
        if (car.getStatus() == Car.Status.EN_VENTE) { // Vérifie si la voiture est disponible à la vente
            this.cars.add(car);
        } else {
            throw new IllegalStateException("Cette voiture n'est plus en vente.");
        }
    }

    // ✅ Méthode pour retirer une voiture du panier
    public void removeCar(Car car) {
        this.cars.remove(car);
    }
}
