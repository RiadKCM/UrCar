package fr.parisdauphine.repository;

import fr.parisdauphine.entity.Order;
import fr.parisdauphine.config.HibernateUtil;
import org.hibernate.Session;

public class OrderRepository {

    public void save(Order order) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            session.persist(order);  // Sauvegarde de la commande
            session.getTransaction().commit();
        }
    }
}
