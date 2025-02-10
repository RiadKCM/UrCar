package fr.parisdauphine.service;

import fr.parisdauphine.entity.Car;
import fr.parisdauphine.entity.Favorite;
import fr.parisdauphine.entity.User;
import fr.parisdauphine.repository.FavoriteRepository;

import java.util.List;


public class FavoriteService {

    private FavoriteRepository favoriteRepository;

    public FavoriteService() {
        this.favoriteRepository = new FavoriteRepository();
    }

    public void addToFavorites(User user, Car car) {
        Favorite existingFavorite = favoriteRepository.findFavorite(user, car);

        if (existingFavorite == null) {
            favoriteRepository.addCarToFavorites(user, car);  // Appel à la méthode du repository
        }
    }

    public List<Favorite> getFavorites(User user) {
        return favoriteRepository.getFavorites(user);
    }

    public void removeFromFavorites(User user, Car car) {
        favoriteRepository.removeCarFromFavorites(user, car);
    }
}
