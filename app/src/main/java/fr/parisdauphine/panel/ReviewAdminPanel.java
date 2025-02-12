package fr.parisdauphine.panel;

import fr.parisdauphine.config.HibernateUtil;
import fr.parisdauphine.entity.Review;
import fr.parisdauphine.entity.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.hibernate.Session;
import java.util.List;

public class ReviewAdminPanel extends VBox {
    private final MainFrame mainFrame;
    private VBox userListContainer;
    private VBox reviewListContainer;

    public ReviewAdminPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        initializeUI();
    }

    private void initializeUI() {
        setPadding(new Insets(20));
        setSpacing(10);
        setAlignment(Pos.TOP_CENTER);

        Label titleLabel = new Label("\ud83d\udc51 Gestion des Utilisateurs");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Button manageReviewsButton = new Button("Gérer les Avis");
        manageReviewsButton.setStyle("-fx-background-color: #FF5722; -fx-text-fill: white;");
        manageReviewsButton.setOnAction(e -> showManageReviews());

        reviewListContainer = new VBox(10);
        ScrollPane reviewScrollPane = new ScrollPane(reviewListContainer);
        reviewScrollPane.setFitToWidth(true);
        reviewScrollPane.setPrefHeight(300);

        loadReviews();
        getChildren().addAll(titleLabel, manageReviewsButton, reviewScrollPane);
    }

    private void showManageReviews() {
        Stage reviewStage = new Stage();
        VBox reviewForm = new VBox(10);
        reviewForm.setPadding(new Insets(20));
    
        Label reviewLabel = new Label("Gérer les Avis");
        reviewLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
    
        Button backButton = new Button("Retour");
        backButton.setStyle("-fx-background-color: #FFC107; -fx-text-fill: white;");
        backButton.setOnAction(e -> reviewStage.close());
    
        reviewListContainer = new VBox(10);
        ScrollPane reviewScrollPane = new ScrollPane(reviewListContainer);
        reviewScrollPane.setFitToWidth(true);
        reviewScrollPane.setPrefHeight(300);
    
        loadReviews(); // Ajout ici pour recharger les avis à chaque ouverture
    
        reviewForm.getChildren().addAll(reviewLabel, reviewScrollPane, backButton);
        reviewStage.setScene(new Scene(reviewForm, 400, 400));
        reviewStage.show();
    }
    

    private void loadReviews() {
        new Thread(() -> {
            List<Review> reviews;
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                reviews = session.createQuery("FROM Review", Review.class).list();
            }

            javafx.application.Platform.runLater(() -> {
                reviewListContainer.getChildren().clear();
                for (Review review : reviews) {
                    reviewListContainer.getChildren().add(createReviewCard(review));
                }
            });
        }).start();
    }

    private HBox createReviewCard(Review review) {
        HBox reviewCard = new HBox(10);
        reviewCard.setPadding(new Insets(10));
        reviewCard.setStyle("-fx-border-color: black; -fx-border-radius: 5; -fx-border-width: 1;");
        reviewCard.setAlignment(Pos.CENTER_LEFT);

        Label reviewInfo = new Label("Avis de " + review.getUser().getNom() + ": " + review.getComment());

        Button deleteButton = new Button("Supprimer");
        deleteButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");
        deleteButton.setOnAction(e -> confirmDeleteReview(review));

        reviewCard.getChildren().addAll(reviewInfo, deleteButton);
        return reviewCard;
    }

    private void confirmDeleteReview(Review review) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de Suppression");
        alert.setHeaderText(null);
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cet avis ?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                deleteReview(review);
            }
        });
    }

    private void deleteReview(Review review) {
        new Thread(() -> {
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                session.beginTransaction();
                session.delete(review);
                session.getTransaction().commit();
            }
            javafx.application.Platform.runLater(() -> {
                loadReviews();
                ((ReviewPanel) mainFrame.getReviewPanel()).refreshReviews(); // Mise à jour immédiate du ReviewPanel
            });
        }).start();
    }
}
