package fr.parisdauphine.repository;

import fr.parisdauphine.entity.Car;
import fr.parisdauphine.entity.Cart;
import fr.parisdauphine.entity.User;
import fr.parisdauphine.config.HibernateUtil;
import fr.parisdauphine.exception.CarAlreadyInCartException;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.ArrayList;
import java.util.List;

public class CartRepository {

    private final SessionFactory sessionFactory;

    public CartRepository() {
        this.sessionFactory = HibernateUtil.getSessionFactory();
    }

    // Ajouter une voiture au panier
    public void addCarToCart(User user, Car car) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();

            // Récupération du panier existant
            Cart cart = session.createQuery("FROM Cart c WHERE c.user = :user", Cart.class)
                    .setParameter("user", user)
                    .uniqueResult();

            if (cart == null) {
                cart = new Cart();
                cart.setUser(user);
                cart.setCars(new ArrayList<>()); // Initialisation de la liste pour éviter NullPointerException
                session.persist(cart);
            }

            Long count = session.createQuery(
                            "SELECT COUNT(c) FROM Cart cart JOIN cart.cars c WHERE cart.id = :cartId AND c.id = :carId", Long.class)
                    .setParameter("cartId", cart.getId())
                    .setParameter("carId", car.getId())
                    .uniqueResult();

            if (count != null && count > 0) {
                throw new CarAlreadyInCartException("La voiture " + car.getBrand() + " " + car.getModel() + " est déjà dans le panier.");
            } else {
                cart.addCar(car);
                session.merge(cart);
            }

            transaction.commit();
        } catch (CarAlreadyInCartException e) {
            throw e;
        }catch (Exception e) {
            e.printStackTrace();
        }
    }


    // Supprimer une voiture du panier
    public void removeCarFromCart(User user, Car car) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();

            // Récupérer le panier de l'utilisateur
            Cart cart = session.createQuery("FROM Cart c WHERE c.user = :user", Cart.class)
                    .setParameter("user", user)
                    .uniqueResult();

            if (cart != null) {
                // Supprimer la voiture du panier
                cart.removeCar(car);
                session.merge(cart);
            }

            transaction.commit();  // Commencer la transaction
        }
    }

    // Sauvegarder un panier
    public void save(Cart cart) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();

            // Sauvegarder le panier
            session.persist(cart);

            transaction.commit();  // Commencer la transaction
        }
    }

    // Mettre à jour un panier
    public void update(Cart cart) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();

            // Mettre à jour le panier
            session.merge(cart);

            transaction.commit();  // Commencer la transaction
        }
    }

    // Récupérer toutes les voitures dans le panier de l'utilisateur
    public List<Car> findCarsInCart(User user) {
        try (Session session = sessionFactory.openSession()) {
            // Requête HQL pour récupérer toutes les voitures du panier de l'utilisateur
            String hql = "SELECT car FROM Cart cart JOIN cart.cars car WHERE cart.user = :user";
            Query<Car> query = session.createQuery(hql, Car.class);
            query.setParameter("user", user);
            return query.getResultList();  // Retourner la liste des voitures
        }
    }

    public Cart findCartByUser(User user) {
        try (Session session = sessionFactory.openSession()) {
            Cart cart = session.createQuery("FROM Cart c WHERE c.user = :user", Cart.class)
                    .setParameter("user", user)
                    .uniqueResult();

            if (cart != null) {
                Hibernate.initialize(cart.getCars()); // 🔥 Force le chargement
            }

            return cart;
        }
    }
}
