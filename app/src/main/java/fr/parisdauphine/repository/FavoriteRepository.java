package fr.parisdauphine.repository;

import fr.parisdauphine.entity.Car;
import fr.parisdauphine.entity.Favorite;
import fr.parisdauphine.entity.User;
import fr.parisdauphine.config.HibernateUtil;
import org.hibernate.Session;

import java.util.List;

public class FavoriteRepository {

    public void addCarToFavorites(User user, Car car) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            Favorite favorite = new Favorite(user, car);
            session.save(favorite);
            session.getTransaction().commit();
        }
    }

    public void removeCarFromFavorites(User user, Car car) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            Favorite favorite = session.createQuery("FROM Favorite WHERE user = :user AND car = :car", Favorite.class)
                    .setParameter("user", user)
                    .setParameter("car", car)
                    .uniqueResult();
            if (favorite != null) {
                session.delete(favorite);
            }
            session.getTransaction().commit();
        }
    }

    public Favorite findFavorite(User user, Car car) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Favorite f WHERE f.user = :user AND f.car = :car", Favorite.class)
                    .setParameter("user", user)
                    .setParameter("car", car)
                    .uniqueResult();
        }
    }

    public void save(Favorite favorite) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            session.save(favorite);
            session.getTransaction().commit();
        }
    }

    public List<Favorite> getFavorites(User user) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Favorite f WHERE f.user = :user", Favorite.class)
                    .setParameter("user", user)
                    .list();
        }
    }
}
