/*package fr.parisdauphine.service;

import fr.parisdauphine.entity.Car;
import org.hibernate.Session;
import fr.parisdauphine.config.HibernateUtil;
import java.util.List;

public class CarService {
    public static List<Car> fetchAllCars() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("SELECT DISTINCT c FROM Car c LEFT JOIN FETCH c.images WHERE c.status = :status", Car.class)
                    .setParameter("status", Car.Status.EN_VENTE)  // Assurez-vous que le statut est correct
                    .list();
        }
    }
}*/

package fr.parisdauphine.service;

import fr.parisdauphine.entity.Car;
import fr.parisdauphine.repository.CarRepository;
import java.util.List;

public class CarService {

    private final CarRepository carRepository;

    public CarService() {
        this.carRepository = new CarRepository();
    }

    // Méthode pour obtenir toutes les voitures en vente
    public List<Car> getAvailableCars() {
        return carRepository.findAllCarsInSale();  // Appelle la méthode du repository pour récupérer les voitures en vente
    }

    // Méthode pour marquer une voiture comme vendue
    public void markCarAsSold(Long carId) {
        Car car = carRepository.findCarById(carId);
        if (car != null) {
            car.markAsSold();  // Appelle la méthode pour marquer la voiture comme vendue
            // Enregistre les changements dans la base de données
            carRepository.save(car);  // Exemple de méthode pour sauvegarder l'état mis à jour de la voiture
        }
    }
}
