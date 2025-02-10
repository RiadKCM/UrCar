package fr.parisdauphine.repository;

import fr.parisdauphine.entity.User;
import org.hibernate.Session;
import org.hibernate.Transaction;
import fr.parisdauphine.config.HibernateUtil;

import java.util.Optional;

public class UserRepository {
    public boolean existsByEmail(String email) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String query = "SELECT COUNT(u) FROM User u WHERE u.email = :email";
            @SuppressWarnings("deprecation")
            Long count = (Long) session.createQuery(query)
                                        .setParameter("email", email)
                                        .uniqueResult();
            return count > 0;
        }
    }

    @SuppressWarnings("deprecation")
    public void save(User user) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.save(user);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }
    public boolean existsByPhoneNumber(String phoneNumber) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String query = "SELECT COUNT(u) FROM User u WHERE u.telephone = :telephone";
            Long count = session.createQuery(query, Long.class) // Utilisation de createQuery avec un type spécifique
                                .setParameter("telephone", phoneNumber)
                                .getSingleResult();
            return count > 0;
        }
    }
        
    public Optional<User> findByEmail(String email) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String query = "FROM User u WHERE u.email = :email";
            User user = session.createQuery(query, User.class)
                                .setParameter("email", email)
                                .uniqueResult();
            return Optional.ofNullable(user);
        }
    }
}
