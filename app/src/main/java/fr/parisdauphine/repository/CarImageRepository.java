package fr.parisdauphine.repository;

import fr.parisdauphine.entity.Image;
import org.hibernate.Session;
import org.hibernate.Transaction;
import fr.parisdauphine.config.HibernateUtil;

import java.util.List;
import java.util.Optional;

public class CarImageRepository {

    // Vérifie si une image existe par son chemin
    public boolean existsByImagePath(String imagePath) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String query = "SELECT COUNT(ci) FROM Image ci WHERE ci.imagePath = :imagePath";
            @SuppressWarnings("deprecation")
            Long count = (Long) session.createQuery(query)
                    .setParameter("imagePath", imagePath)
                    .uniqueResult();
            return count > 0;
        }
    }

    // Sauvegarde une nouvelle image
    @SuppressWarnings("deprecation")
    public void save(Image Image) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.save(Image);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }

    // Récupère les images par id de voiture
    public List<Image> findByCarId(Long carId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String query = "FROM Image ci WHERE ci.car.id = :carId";
            return session.createQuery(query, Image.class)
                    .setParameter("carId", carId)
                    .getResultList();
        }
    }

    // Récupère une image par son id
    public Optional<Image> findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return Optional.ofNullable(session.get(Image.class, id));
        }
    }

    // Supprime une image par son id
    public void deleteById(Long id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Image Image = session.get(Image.class, id);
            if (Image != null) {
                session.delete(Image);
                transaction.commit();
            }
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }
}
