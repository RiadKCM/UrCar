package fr.parisdauphine;

import fr.parisdauphine.config.HibernateUtil;
import fr.parisdauphine.panel.MainFrame;
import fr.parisdauphine.repository.UserRepository;
import fr.parisdauphine.service.*;
import javafx.application.Application;
import javafx.stage.Stage;
import org.hibernate.Session;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        DataInitializer.initializeData();
        CarService carService = new CarService();
        UserRepository userRepository = new UserRepository(); // Créer une instance de UserRepository
        UserService userService = new UserService(userRepository);// Passer UserRepository à UserService
        MainFrame mainFrame = new MainFrame(userService,carService);

        // Ajouter un gestionnaire pour la fermeture de l'application
        mainFrame.setOnCloseRequest(event -> {
            HibernateUtil.shutdown(); // Fermer proprement Hibernate
            System.exit(0); // Terminer l'application
        });

        // Afficher la fenêtre principale
        mainFrame.show();
    }

    public static void main(String[] args) {
        // Vérifier que Hibernate fonctionne correctement avant de lancer JavaFX
        testHibernate();

        // Lancer l'application JavaFX
        launch(args);
    }

    private static void testHibernate() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            System.out.println("Session Hibernate ouverte avec succès !");
        } catch (Exception e) {
            System.err.println("Erreur lors de l'ouverture de la session Hibernate : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
