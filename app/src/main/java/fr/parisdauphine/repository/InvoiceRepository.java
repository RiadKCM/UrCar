package fr.parisdauphine.repository;

import fr.parisdauphine.entity.Invoice;
import fr.parisdauphine.config.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

public class InvoiceRepository {
    private final SessionFactory sessionFactory;

    public InvoiceRepository() {
        this.sessionFactory = HibernateUtil.getSessionFactory();
    }

    // Méthode pour sauvegarder une facture
    public void save(Invoice invoice) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.save(invoice);  // Sauvegarde l'objet Invoice dans la base de données
            session.getTransaction().commit();  // Commit de la transaction pour persister l'objet
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Méthode pour récupérer une facture par son ID
    public Invoice getById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(Invoice.class, id);  // Retourne l'Invoice correspondant à l'ID
        }
    }

    // Méthode pour mettre à jour une facture existante
    public void update(Invoice invoice) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.update(invoice);  
            session.getTransaction().commit();
        }
    }

    // Méthode pour supprimer une facture
    public void delete(Invoice invoice) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.delete(invoice);  // Supprime l'Invoice de la base de données
            session.getTransaction().commit();
        }
    }

    // Getter pour la SessionFactory, si nécessaire
    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }
}
