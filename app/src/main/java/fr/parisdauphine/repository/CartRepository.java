package fr.parisdauphine.repository;

import fr.parisdauphine.entity.Car;
import fr.parisdauphine.entity.Cart;
import fr.parisdauphine.entity.User;
import fr.parisdauphine.config.HibernateUtil;
import org.hibernate.Session;

public class CartRepository {

    public void addCarToCart(User user, Car car) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            Cart cart = session.createQuery("FROM Cart WHERE user = :user", Cart.class)
                    .setParameter("user", user)
                    .uniqueResult();
            if (cart == null) {
                cart = new Cart();
                cart.setUser(user);
                session.persist(cart);  // Sauvegarde du nouveau panier
            }
            cart.addCar(car);
            session.merge(cart);
            session.getTransaction().commit();
        }
    }

    public void removeCarFromCart(User user, Car car) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            Cart cart = session.createQuery("FROM Cart WHERE user = :user", Cart.class)
                    .setParameter("user", user)
                    .uniqueResult();
            if (cart != null) {
                cart.removeCar(car);
                session.merge(cart);
            }
            session.getTransaction().commit();
        }
    }

    public Cart findCartByUser(User user) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Cart c WHERE c.user = :user", Cart.class)
                    .setParameter("user", user)
                    .uniqueResult();
        }
    }

    public void save(Cart cart) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            session.persist(cart);  // Sauvegarde du panier
            session.getTransaction().commit();
        }
    }

    public void update(Cart cart) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            session.merge(cart);  // Mise à jour du panier
            session.getTransaction().commit();
        }
    }
}
