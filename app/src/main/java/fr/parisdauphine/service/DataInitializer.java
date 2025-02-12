package fr.parisdauphine.service;

import fr.parisdauphine.entity.Car;
import fr.parisdauphine.entity.Image;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DataInitializer {
    public static void initializeData() {
        System.out.println("🚗 Initialisation des données...");

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("urcar-pu");
        EntityManager em = emf.createEntityManager();

        try {
            long carCount = (long) em.createQuery("SELECT COUNT(c) FROM Car c").getSingleResult();
            System.out.println("📊 Nombre de voitures existantes : " + carCount);
            if (carCount > 0) {
                return;  //  Si les voitures existent déjà, on ne fait rien
            }

            // 1️⃣ Insérer les voitures
            List<Car> cars = Arrays.asList(
                new Car("Toyota", "Corolla", 20000.0, "Berline blanche, 50 000 km, très bien entretenue.", new ArrayList<>()),
                new Car("Toyota", "Camry", 25000.0, "Camry noire, 40 000 km, conduite souple et économique.", new ArrayList<>()),
                new Car("BMW", "3 Series", 40000.0, "BMW gris métallisé, 30 000 km, intérieur cuir premium.", new ArrayList<>()),
                new Car("Mercedes", "C-Class", 42000.0, "Mercedes élégante en bleu nuit, seulement 20 000 km.", new ArrayList<>()),
                new Car("Ferrari", "Roma", 220000.0, "Supercar rouge vif, 5 000 km, sensations fortes garanties !", new ArrayList<>()),
                new Car("Audi", "A4", 35000.0, "Berline allemande noire, 60 000 km, full options.", new ArrayList<>()),
                new Car("Ford", "Mustang", 55000.0, "Mustang rouge, 25 000 km, moteur V8 rugissant !", new ArrayList<>()),
                new Car("Porsche", "911", 150000.0, "Porsche blanche, 15 000 km, performances de rêve.", new ArrayList<>()),
                new Car("Tesla", "Model S", 90000.0, "Tesla grise, 35 000 km, autonomie impressionnante.", new ArrayList<>()),
                new Car("Peugeot", "208", 18000.0, "Citadine jaune, 70 000 km, faible consommation.", new ArrayList<>()),
                new Car("Renault", "Clio", 16000.0, "Clio rouge, 65 000 km, parfaite pour la ville.", new ArrayList<>()),
                new Car("Volkswagen", "Golf", 27000.0, "Golf bleue, 45 000 km, équilibre parfait entre confort et puissance.", new ArrayList<>()),
                new Car("Nissan", "Qashqai", 32000.0, "SUV familial gris, 50 000 km, très spacieux.", new ArrayList<>()),
                new Car("Jeep", "Wrangler", 60000.0, "4x4 tout-terrain noir, 30 000 km, prêt pour l'aventure !", new ArrayList<>()),
                new Car("Hyundai", "Tucson", 28000.0, "SUV blanc, 55 000 km, très bien équipé.", new ArrayList<>()),
                new Car("Fiat", "500", 14000.0, "Mini citadine rose, 80 000 km, parfaite pour la ville.", new ArrayList<>()),
                new Car("Citroën", "C3", 17000.0, "Citroën bleu ciel, 60 000 km, très économique.", new ArrayList<>()),
                new Car("Mazda", "CX-5", 35000.0, "SUV Mazda rouge, 40 000 km, conduite dynamique.", new ArrayList<>()),
                new Car("Opel", "Corsa", 15000.0, "Compacte blanche, 75 000 km, idéale pour jeunes conducteurs.", new ArrayList<>()),
                new Car("Lexus", "RX", 50000.0, "SUV hybride luxe gris, 25 000 km, ultra confortable.", new ArrayList<>())
        );

            em.getTransaction().begin();
            for (Car car : cars) {
                em.persist(car);
            }
            em.getTransaction().commit();
            System.out.println("✅ Voitures insérées avec succès !");

            // 2️⃣ Récupérer les voitures après insertion pour s'assurer que les IDs sont bien définis
            List<Car> persistedCars = em.createQuery("SELECT c FROM Car c", Car.class).getResultList();

            // 3️⃣ Insérer les images associées aux voitures
            em.getTransaction().begin();
            for (Car car : persistedCars) {
                List<Image> images = generateCarImages(car);
                car.getImages().addAll(images); // Ajoute les images à la voiture
                for (Image image : images) {
                    em.persist(image); // Persiste chaque image en base
                    System.out.println("🖼️ Image ajoutée : " + image.getImagePath());
                }
                em.merge(car); // Met à jour la voiture pour inclure les images
            }
            em.getTransaction().commit();
            System.out.println("✅ Images insérées avec succès !");
            
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            em.close();
            emf.close();
        }
    }

    // 🔄 Génère une liste d'images pour une voiture donnée
    private static List<Image> generateCarImages(Car car) {
        List<Image> images = new ArrayList<>();
        int nombreImages = (car.getId() % 2 == 0) ? 2 : 1;  // Alterne entre 1 et 2 images

        for (int i = 1; i <= nombreImages; i++) {
            String imagePath = "/image/voitures/voiture" + car.getId() + "_" + i + ".jpg";
            images.add(new Image(imagePath, car));
        }
        return images;
    }
}
