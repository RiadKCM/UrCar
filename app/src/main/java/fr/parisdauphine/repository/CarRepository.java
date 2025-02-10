package fr.parisdauphine.repository;

import fr.parisdauphine.entity.Car;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import fr.parisdauphine.config.HibernateUtil;
import java.util.List;

public class CarRepository {

    public List<Car> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            Query<Car> query = session.createQuery("FROM Car", Car.class);
            List<Car> cars = query.list();
            session.getTransaction().commit();
            return cars;
        }
    }

    public Car findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Car.class, id);
        }
    }

    public List<Car> findCarsByPage(int page, int itemsPerPage) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            Query<Car> query = session.createQuery("FROM Car", Car.class);
            query.setFirstResult((page - 1) * itemsPerPage);
            query.setMaxResults(itemsPerPage);
            List<Car> cars = query.list();
            session.getTransaction().commit();
            return cars;
        }
    }

    public List<Car> fetchCarsByCriteria(String criteria) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Implémenter une requête avec des critères spécifiques
            return session.createQuery("FROM Car c WHERE c.model LIKE :criteria", Car.class)
                    .setParameter("criteria", "%" + criteria + "%")
                    .list();
        }
    }

    // Méthode pour récupérer toutes les voitures en vente
    public List<Car> findAllCarsInSale() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Crée une requête pour récupérer les voitures en vente
            Query<Car> query = session.createQuery("FROM Car c WHERE c.status = :status", Car.class);
            query.setParameter("status", Car.Status.EN_VENTE);
            return query.list();  // Retourne la liste des voitures en vente
        }
    }

    // Méthode pour récupérer une voiture par son ID
    public Car findCarById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Car.class, id);  // Récupère la voiture par son ID
        }
    }

    public void save(Car car) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Démarre une transaction
            Transaction transaction = session.beginTransaction();

            // Si l'objet existe déjà (a un ID), Hibernate fera une mise à jour automatiquement
            // Sinon, il créera une nouvelle entrée dans la base de données
            session.saveOrUpdate(car);

            // Commit de la transaction
            transaction.commit();
        }
    }
}
