package fr.parisdauphine.panel;

import fr.parisdauphine.entity.Car;
import fr.parisdauphine.entity.Favorite;
import fr.parisdauphine.entity.User;
import fr.parisdauphine.exception.CarAlreadyInCartException;
import fr.parisdauphine.service.CartService;
import fr.parisdauphine.service.FavoriteService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.List;

public class FavoritePanel extends VBox {
    private final MainFrame mainFrame;
    private final FavoriteService favoriteService;
    private final CartService cartService;

    public FavoritePanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.favoriteService = new FavoriteService();
        this.cartService = new CartService();
        initializeUI();
    }

    private void initializeUI() {
        setPadding(new Insets(20));
        setSpacing(15);
        setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Vos voitures favorites");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        getChildren().add(titleLabel);

        ScrollPane scrollPane = new ScrollPane();
        VBox favoritesContainer = new VBox(10);
        favoritesContainer.setPadding(new Insets(10));
        scrollPane.setContent(favoritesContainer);
        scrollPane.setFitToWidth(true);
        getChildren().add(scrollPane);

        User currentUser = mainFrame.getCurrentUser();
        if (currentUser != null) {
            List<Favorite> favorites = favoriteService.getFavorites(currentUser);
            if (favorites.isEmpty()) {
                favoritesContainer.getChildren().add(new Label("Vous n'avez pas encore de favoris."));
            } else {
                favorites.forEach(fav -> favoritesContainer.getChildren().add(createFavoriteCard(fav, favoritesContainer)));
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Non connecté", "Veuillez vous connecter pour voir vos favoris.");
        }
    }

    private HBox createFavoriteCard(Favorite favorite, VBox container) {
        Car car = favorite.getCar();

        HBox carCard = new HBox(10);
        carCard.setPadding(new Insets(10));
        carCard.setStyle("-fx-border-color: black; -fx-border-radius: 5; -fx-border-width: 1;");
        carCard.setAlignment(Pos.CENTER_LEFT);

        ImageView carImageView = new ImageView();
        carImageView.setFitWidth(150);
        carImageView.setFitHeight(100);
        carImageView.setPreserveRatio(true);

        if (!car.getImages().isEmpty()) {
            String imagePath = "src/main/resources" + car.getImages().get(0).getImagePath();
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                carImageView.setImage(new Image(imageFile.toURI().toString()));
            }
            carImageView.setOnMouseClicked(e -> showImageDetail(car));
        }

        VBox carInfo = new VBox(5);
        carInfo.getChildren().addAll(
                new Label(car.getBrand() + " " + car.getModel()),
                new Label("Prix : " + car.getPrice() + " €"),
                new Label("Détails : " + car.getdescription()),
                createButtons(favorite, carCard, container)
        );
        carCard.getChildren().addAll(carImageView, carInfo);
        return carCard;
    }

    private HBox createButtons(Favorite favorite, HBox carCard, VBox container) {
        Button removeButton = new Button("❌ Retirer");
        removeButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");
        removeButton.setOnAction(e -> {
            favoriteService.removeFromFavorites(favorite.getUser(), favorite.getCar());
            container.getChildren().remove(carCard);
            showAlert(Alert.AlertType.INFORMATION, "Favori supprimé", "La voiture a été retirée de vos favoris.");
        });

        Button addToCartButton = new Button("🛒 Ajouter au panier");
        addToCartButton.setStyle("-fx-background-color: green; -fx-text-fill: white;");
        addToCartButton.setOnAction(e -> {
            try {
                cartService.addToCart(favorite.getUser(), favorite.getCar());
                showAlert(Alert.AlertType.INFORMATION, "Ajouté au panier",
                        favorite.getCar().getBrand() + " " + favorite.getCar().getModel() + " a été ajouté au panier.");
            } catch (CarAlreadyInCartException ex) {
                showAlert(Alert.AlertType.WARNING, "Déjà ajouté", ex.getMessage());
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue : " + ex.getMessage());
            }
        });

        return new HBox(10, removeButton, addToCartButton);
    }

    private void showImageDetail(Car car) {
        Dialog<Void> imageDialog = new Dialog<>();
        imageDialog.setTitle("Détails de l'image");
        imageDialog.setHeaderText(car.getBrand() + " " + car.getModel());
    
        VBox dialogContent = new VBox();
        dialogContent.setAlignment(Pos.CENTER);
        dialogContent.setPadding(new Insets(10));
    
        // Créer l'ImageView et l'encapsuler dans un ScrollPane pour activer le défilement
        ImageView imageView = new ImageView();
        imageView.setPreserveRatio(true);
    
        List<String> imagePaths = car.getImages().stream()
                .map(img -> "src/main/resources" + img.getImagePath())
                .toList();
    
        if (imagePaths.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Aucune image", "Aucune image disponible pour cette voiture.");
            return;
        }
    
        final int[] currentIndex = {0};
        updateImageView(imageView, imagePaths.get(currentIndex[0]));
    
        // Créer un ScrollPane autour de l'ImageView pour permettre le défilement
        ScrollPane imageScrollPane = new ScrollPane();
        imageScrollPane.setContent(imageView);
        imageScrollPane.setFitToHeight(true);  // Ajuste la hauteur à la taille du ScrollPane
        imageScrollPane.setFitToWidth(true);   // Ajuste la largeur à la taille du ScrollPane
        imageScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); // Barres de défilement verticales apparaissent quand nécessaire
    
        // Mettre une taille initiale de l'image en fonction de la taille du dialogue
        updateImageSize(imageView, imageDialog);
    
        Button prevButton = new Button("⬅ Précédent");
        Button nextButton = new Button("Suivant ➡");
    
        prevButton.setOnAction(e -> {
            if (currentIndex[0] > 0) {
                currentIndex[0]--;
                updateImageView(imageView, imagePaths.get(currentIndex[0]));
            }
            updateButtonState(prevButton, nextButton, currentIndex[0], imagePaths.size());
        });
    
        nextButton.setOnAction(e -> {
            if (currentIndex[0] < imagePaths.size() - 1) {
                currentIndex[0]++;
                updateImageView(imageView, imagePaths.get(currentIndex[0]));
            }
            updateButtonState(prevButton, nextButton, currentIndex[0], imagePaths.size());
        });
    
        updateButtonState(prevButton, nextButton, currentIndex[0], imagePaths.size());
    
        HBox buttonContainer = new HBox(10, prevButton, nextButton);
        buttonContainer.setAlignment(Pos.CENTER);
    
        dialogContent.getChildren().addAll(imageScrollPane, buttonContainer);
        imageDialog.getDialogPane().setContent(dialogContent);
        imageDialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        imageDialog.showAndWait();
    }
    
    private void updateImageView(ImageView imageView, String imagePath) {
        File imageFile = new File(imagePath);
        if (imageFile.exists()) {
            imageView.setImage(new Image(imageFile.toURI().toString()));
        }
    }
    
    private void updateImageSize(ImageView imageView, Dialog<Void> imageDialog) {
        // Redimensionner l'image pour qu'elle s'adapte à la taille de la fenêtre du dialogue
        imageDialog.widthProperty().addListener((obs, oldVal, newVal) -> {
            double width = newVal.doubleValue() - 40;  // 40px pour les marges
            double height = width * 0.75;  // Calculer la hauteur en fonction du ratio d'image
            imageView.setFitWidth(width);
            imageView.setFitHeight(height);
        });
    
        imageDialog.heightProperty().addListener((obs, oldVal, newVal) -> {
            double height = newVal.doubleValue() - 100;  // Ajustement avec une marge pour les boutons
            double width = height * 1.33;  // Calculer la largeur en fonction du ratio d'image
            imageView.setFitWidth(width);
            imageView.setFitHeight(height);
        });
    }
    
    private void updateButtonState(Button prevButton, Button nextButton, int currentIndex, int totalImages) {
        prevButton.setDisable(currentIndex == 0);
        nextButton.setDisable(currentIndex == totalImages - 1);
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
