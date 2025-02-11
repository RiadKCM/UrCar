package fr.parisdauphine.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "car")
public class Car {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String brand;

    @Column(nullable = false)
    private String model;

    @Column(nullable = false)
    private Double price;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description; 

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status; // ✅ Nouveau champ pour le statut (EN_VENTE, VENDU)

    @OneToMany(mappedBy = "car", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images = new ArrayList<>(); // ✅ Initialisation pour éviter NullPointerException

    public enum Status {
        EN_VENTE, VENDU
    }

    // ✅ Constructeurs
    public Car() {
        this.status = Status.EN_VENTE; // Par défaut, une nouvelle voiture est en vente
    }

    public Car(String brand, String model, Double price, String description, List<Image> images) {
        this.brand = brand;
        this.model = model;
        this.price = price;
        this.description=description;
        this.images = images != null ? images : new ArrayList<>();
        this.status = Status.EN_VENTE; // Statut par défaut
    }

    // ✅ Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }


    public List<Image> getImages() { return images; }
    public void setImages(List<Image> images) {
        this.images = images != null ? images : new ArrayList<>();
        if (this.images != null) {
            this.images.forEach(image -> image.setCar(this)); // ✅ Assure la cohérence des relations
        }
    }

    public String getdescription() { return description; }
    public void setdescription(String description) { this.description = description; }

    // ✅ Méthodes pour gérer les images associées
    public void addImage(Image image) {
        if (image != null) {
            images.add(image);
            image.setCar(this); // Lier l'image à la voiture
        }
    }

    public void removeImage(Image image) {
        if (image != null && images.contains(image)) {
            images.remove(image);
            image.setCar(null); // Rompre la relation entre l'image et la voiture
        }
    }

    // ✅ Méthode pour marquer une voiture comme vendue
    public void markAsSold() {
        if (this.status == Status.VENDU) {
            throw new IllegalStateException("Cette voiture est déjà vendue.");
        }
        this.status = Status.VENDU;
    }

    // ✅ Méthode pour vérifier si la voiture est en vente
    public boolean isAvailableForSale() {
        return this.status == Status.EN_VENTE;
    }

    // ✅ Méthode utilitaire pour afficher les informations de la voiture
    @Override
    public String toString() {
        return "Car{" +
                "id=" + id +
                ", brand='" + brand + '\'' +
                ", model='" + model + '\'' +
                ", price=" + price +
                ", status=" + status +
                '}';
    }
}