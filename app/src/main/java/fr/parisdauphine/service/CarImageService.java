package fr.parisdauphine.service;

import fr.parisdauphine.entity.Image;
import fr.parisdauphine.repository.CarImageRepository;

import java.util.List;
import java.util.Optional;

public class CarImageService {

    private final CarImageRepository carImageRepository;

    public CarImageService() {
        this.carImageRepository = new CarImageRepository();
    }

    // Vérifie si une image existe par son chemin
    public boolean existsByImagePath(String imagePath) {
        return carImageRepository.existsByImagePath(imagePath);
    }

    // Sauvegarde une nouvelle image
    public void save(Image image) {
        carImageRepository.save(image);
    }

    // Récupère toutes les images pour une voiture spécifique
    public List<Image> getImagesByCarId(Long carId) {
        return carImageRepository.findByCarId(carId);
    }

    // Récupère une image par son id
    public Optional<Image> getImageById(Long id) {
        return carImageRepository.findById(id);
    }

    // Supprime une image par son id
    public void deleteImageById(Long id) {
        carImageRepository.deleteById(id);
    }
}
