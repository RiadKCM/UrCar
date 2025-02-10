 package fr.parisdauphine.panel;

import fr.parisdauphine.config.HibernateUtil;
import fr.parisdauphine.entity.Review;
import fr.parisdauphine.entity.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.hibernate.Session;

import java.util.List;

public class ReviewPanel extends VBox {
    private final MainFrame mainFrame;
    private final VBox reviewContainer;

    public ReviewPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setPadding(new Insets(20));
        setSpacing(15);
        setAlignment(Pos.CENTER);
        setStyle("-fx-background-color: #ffffff; -fx-border-radius: 10px; -fx-padding: 15px;");

        // Titre
        Text title = new Text("Avis des clients");
        title.setFont(Font.font("Arial", 24));
        title.setStyle("-fx-fill: #333333; -fx-font-weight: bold;");
        getChildren().add(title);

        // Conteneur des avis avec ScrollPane
        reviewContainer = new VBox(10);
        reviewContainer.setPadding(new Insets(10));
        reviewContainer.setStyle("-fx-background-color: #f8f9fa; -fx-border-radius: 10px; -fx-padding: 10px;");

        ScrollPane scrollPane = new ScrollPane(reviewContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(300);
        scrollPane.setStyle("-fx-background: transparent; -fx-border-color: gray; -fx-border-radius: 5px;");
        getChildren().add(scrollPane);

        // Chargement des avis dès le début
        loadReviews();
        refreshReviews();

        // Bouton pour ajouter un avis
        Button addReviewButton = new Button("📝 Ajouter un avis");
        addReviewButton.setFont(Font.font("Arial", 14));
        addReviewButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-radius: 5px;");
        addReviewButton.setOnAction(e -> handleAddReview());
        getChildren().add(addReviewButton);
    }

    // Chargement des avis
    private void loadReviews() {
        reviewContainer.getChildren().clear();

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Review> reviews = session.createQuery("FROM Review r ORDER BY r.id DESC", Review.class).list();

            if (reviews.isEmpty()) {
                Label noReviewsLabel = new Label("Aucun avis pour le moment.");
                noReviewsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: gray;");
                reviewContainer.getChildren().add(noReviewsLabel);
            } else {
                reviews.forEach(review -> reviewContainer.getChildren().add(createReviewCard(review)));
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les avis.");
        }
    }

    public void refreshReviews() {
        loadReviews();
    }

    // Création d'une carte d'affichage d'avis
    private HBox createReviewCard(Review review) {
        HBox reviewBox = new HBox(15);
        reviewBox.setPadding(new Insets(10));
        reviewBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #ddd; -fx-border-radius: 5px; -fx-padding: 15px;");
        reviewBox.setAlignment(Pos.CENTER_LEFT);

        String stars = "★".repeat(review.getRating());
        Label starsLabel = new Label(stars);
        starsLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #f39c12; -fx-font-weight: bold;");

        VBox reviewInfo = new VBox(5);
        Label userLabel = new Label(review.getUser().getPrenom() + " " + review.getUser().getNom());
        userLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #333333;");

        Label commentLabel = new Label(review.getComment());
        commentLabel.setWrapText(true);
        commentLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #555555;");

        reviewInfo.getChildren().addAll(userLabel, starsLabel, commentLabel);
        reviewBox.getChildren().add(reviewInfo);

        return reviewBox;
    }

    // Boîte de dialogue pour ajouter un avis
    private void handleAddReview() {
        User currentUser = mainFrame.getCurrentUser();
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Vous devez être connecté pour ajouter un avis.");
            return;
        }

        ChoiceDialog<Integer> starDialog = new ChoiceDialog<>(5, 1, 2, 3, 4, 5);
        starDialog.setTitle("Nombre d'étoiles");
        starDialog.setHeaderText(null);
        starDialog.setContentText("Combien d'étoiles attribuez-vous ?");
        Integer starCount = starDialog.showAndWait().orElse(null);

        if (starCount == null) return;

        TextInputDialog reviewDialog = new TextInputDialog();
        reviewDialog.setTitle("Nouvel Avis");
        reviewDialog.setHeaderText(null);
        reviewDialog.setContentText("Entrez votre avis :");
        String newReviewText = reviewDialog.showAndWait().orElse(null);

        if (newReviewText != null && !newReviewText.trim().isEmpty()) {
            saveReview(currentUser, newReviewText, starCount);
        }
    }

    private void saveReview(User user, String comment, int rating) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            Review review = new Review();
            review.setUser(user);
            review.setComment(comment);
            review.setRating(rating);
            session.persist(review);
            session.getTransaction().commit();
            showAlert(Alert.AlertType.INFORMATION, "Ajouté", "Votre avis a été enregistré !");
            loadReviews();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ajouter l'avis.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}