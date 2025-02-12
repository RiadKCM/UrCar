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

    // Sauvegarde une nouvelle image
    public void save(Image image) {
        carImageRepository.save(image);
    }
}
