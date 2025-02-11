package fr.parisdauphine.repository;

import fr.parisdauphine.entity.Order;
import fr.parisdauphine.config.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

public class OrderRepository {
    private final SessionFactory sessionFactory;

    public OrderRepository() {
        this.sessionFactory = HibernateUtil.getSessionFactory();
    }

    public void save(Order order) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            System.out.println("deded");
            session.save(order);
            System.out.println("hhhhhhh");
            session.getTransaction().commit();
        }
    }

    public SessionFactory getSessionFactory() {  // ✅ Ajouter cette méthode
        return sessionFactory;
    }
}
