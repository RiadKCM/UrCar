package fr.parisdauphine.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "car_image")
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String imagePath;

    @ManyToOne
    @JoinColumn(name = "car_id") // Clé étrangère
    private Car car;

    public Image() {}

    public Image(String imagePath, Car car) {
        this.imagePath = imagePath;
        this.car = car;
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public Car getCar() { return car; }
    public void setCar(Car car) { this.car = car; }
}
