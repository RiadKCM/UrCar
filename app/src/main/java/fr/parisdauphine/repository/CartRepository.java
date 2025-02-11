package fr.parisdauphine.repository;

import fr.parisdauphine.entity.Car;
import fr.parisdauphine.entity.Cart;
import fr.parisdauphine.entity.User;
import fr.parisdauphine.config.HibernateUtil;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

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
    
            Cart cart = session.createQuery("FROM Cart c WHERE c.user = :user", Cart.class)
                    .setParameter("user", user)
                    .uniqueResult();
    
            if (cart == null) {
                cart = new Cart();
                cart.setUser(user);
                session.persist(cart);
            }
    
            if (!cart.getCars().contains(car)) {  // Vérifie si la voiture n'est pas déjà présente
                cart.addCar(car);
                session.merge(cart);
            } else {
                System.out.println("Cette voiture est déjà dans le panier !");
            }
    
            transaction.commit();
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
