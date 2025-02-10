/*package fr.parisdauphine.panel;

import fr.parisdauphine.config.HibernateUtil;
import fr.parisdauphine.entity.Car;
import fr.parisdauphine.entity.Cart;
import fr.parisdauphine.entity.Favorite;
import fr.parisdauphine.entity.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.hibernate.Session;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FavoritePanel extends VBox {

    private final MainFrame mainFrame;

    public FavoritePanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        initializeUI();
    }

    private void initializeUI() {
        setPadding(new Insets(20));
        setSpacing(15);
        setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Vos voitures favorites");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        getChildren().add(titleLabel);

        // ✅ ScrollPane pour gérer la scroll bar
        ScrollPane scrollPane = new ScrollPane();
        VBox favoritesContainer = new VBox(10);  // Conteneur pour les favoris
        favoritesContainer.setPadding(new Insets(10));
        scrollPane.setContent(favoritesContainer);
        scrollPane.setFitToWidth(true); // Ajuste la largeur du ScrollPane

        getChildren().add(scrollPane); // Ajoute le ScrollPane à la VBox principale

        // ✅ Récupérer les favoris de l'utilisateur connecté
        User currentUser = mainFrame.getCurrentUser();
        if (currentUser != null) {
            List<Favorite> favorites = fetchFavorites(currentUser);
            if (favorites.isEmpty()) {
                Label noFavoritesLabel = new Label("Vous n'avez pas encore de favoris.");
                favoritesContainer.getChildren().add(noFavoritesLabel);
            } else {
                favorites.forEach(fav -> favoritesContainer.getChildren().add(createFavoriteCard(fav)));
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Non connecté", "Veuillez vous connecter pour voir vos favoris.");
        }
    }

    private List<Favorite> fetchFavorites(User user) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Favorite> favorites = session.createQuery(
                            "FROM Favorite f WHERE f.user = :user AND f.car.status = :status", Favorite.class)
                    .setParameter("user", user)
                    .setParameter("status", Car.Status.EN_VENTE)  // Utiliser l'énumération
                    .list();

            System.out.println("Favoris trouvés : " + favorites.size());
            for (Favorite fav : favorites) {
                System.out.println("Favori : " + fav.getCar().getBrand() + " " + fav.getCar().getModel());
            }

            return favorites;
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }


    // ✅ Affichage d'une carte de favori avec image + bouton "Ajouter au panier"
    private HBox createFavoriteCard(Favorite favorite) {
        Car car = favorite.getCar();

        HBox carCard = new HBox(10);
        carCard.setPadding(new Insets(10));
        carCard.setStyle("-fx-border-color: black; -fx-border-radius: 5; -fx-border-width: 1;");
        carCard.setAlignment(Pos.CENTER_LEFT);

        // ✅ Image de la voiture
        ImageView carImageView = new ImageView();
        carImageView.setFitWidth(150);
        carImageView.setFitHeight(100);
        carImageView.setPreserveRatio(true);

        if (!car.getImages().isEmpty()) {
            String imagePath = "src/main/resources" + car.getImages().get(0).getImagePath();
            File imageFile = new File(imagePath);

            if (imageFile.exists()) {
                carImageView.setImage(new Image(imageFile.toURI().toString()));
            } else {
                carImageView.setStyle("-fx-border-color: gray; -fx-border-style: dashed;");
            }

            // ✅ Clic pour agrandir l'image
            carImageView.setOnMouseClicked(e -> showImageDetail(car));
        }

        // ✅ Informations sur la voiture
        VBox carInfo = new VBox(5);
        Label brandLabel = new Label(car.getBrand() + " " + car.getModel());
        brandLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Label priceLabel = new Label("Prix : " + car.getPrice() + " €");

        // ✅ Bouton pour retirer des favoris
        Button removeButton = new Button("❌ Retirer");
        removeButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");
        removeButton.setOnAction(e -> {
            removeFavorite(favorite);
            ((VBox) carCard.getParent()).getChildren().remove(carCard);  // Retirer de l'affichage
        });

        // ✅ Bouton "Ajouter au Panier" avec icône 🛒
        Button addToCartButton = new Button("🛒 Ajouter");
        addToCartButton.setStyle("-fx-background-color: green; -fx-text-fill: white;");
        addToCartButton.setOnAction(e -> addToCart(car));


        HBox buttonBox = new HBox(10, removeButton, addToCartButton);
        carInfo.getChildren().addAll(brandLabel, priceLabel, buttonBox);
        carCard.getChildren().addAll(carImageView, carInfo);

        return carCard;
    }

    // ✅ Méthode pour ajouter au panier
    private void addToCart(Car car) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();

            User currentUser = mainFrame.getCurrentUser();
            if (currentUser == null) {
                showAlert(Alert.AlertType.WARNING, "Non connecté", "Veuillez vous connecter pour ajouter au panier.");
                return;
            }

            // Vérifie si la voiture est déjà dans le panier
            Cart cartItem = session.createQuery(
                            "FROM Cart c WHERE c.user = :user", Cart.class)
                    .setParameter("user", currentUser)
                    .uniqueResult();

            if (cartItem == null) {
                cartItem = new Cart();
                cartItem.setUser(currentUser);
                cartItem.setCars(new ArrayList<>()); // Initialiser la liste
                session.save(cartItem);
            }

            if (!cartItem.getCars().contains(car)) {
                cartItem.getCars().add(car);
                session.update(cartItem);
                session.getTransaction().commit();
                showAlert(Alert.AlertType.INFORMATION, "Ajouté au panier", car.getBrand() + " " + car.getModel() + " a été ajouté au panier.");
            } else {
                showAlert(Alert.AlertType.WARNING, "Déjà dans le panier", "Cette voiture est déjà dans votre panier !");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ajouter au panier.");
        }
    }


    // ✅ Suppression d'un favori
    private void removeFavorite(Favorite favorite) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            session.remove(favorite);
            session.getTransaction().commit();
            showAlert(Alert.AlertType.INFORMATION, "Favori supprimé", "La voiture a été retirée de vos favoris.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de retirer des favoris.");
        }
    }

    // ✅ Affichage en plein écran de l'image
    private void showImageDetail(Car car) {
        Alert imageAlert = new Alert(Alert.AlertType.INFORMATION);
        imageAlert.setTitle("Détails de l'image");
        imageAlert.setHeaderText(car.getBrand() + " " + car.getModel());

        ImageView imageView = new ImageView();
        if (!car.getImages().isEmpty()) {
            String imagePath = "src/main/resources" + car.getImages().get(0).getImagePath();
            File imageFile = new File(imagePath);

            if (imageFile.exists()) {
                imageView.setImage(new Image(imageFile.toURI().toString()));
            } else {
                imageView.setStyle("-fx-border-color: gray; -fx-border-style: dashed;");
            }
        }

        imageView.setFitWidth(400);  // Agrandir l'image
        imageView.setPreserveRatio(true);

        VBox dialogContent = new VBox(imageView);
        dialogContent.setAlignment(Pos.CENTER);
        dialogContent.setPadding(new Insets(10));

        imageAlert.getDialogPane().setContent(dialogContent);
        imageAlert.showAndWait();
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}*/

/*package fr.parisdauphine.panel;

import fr.parisdauphine.entity.Car;
import fr.parisdauphine.entity.Favorite;
import fr.parisdauphine.entity.User;
import fr.parisdauphine.service.FavoriteService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
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

    public FavoritePanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.favoriteService = new FavoriteService();
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

        return new HBox(10, removeButton);
    }

    private void showImageDetail(Car car) {
        Alert imageAlert = new Alert(Alert.AlertType.INFORMATION);
        imageAlert.setTitle("Détails de l'image");
        imageAlert.setHeaderText(car.getBrand() + " " + car.getModel());

        ImageView imageView = new ImageView();
        if (!car.getImages().isEmpty()) {
            String imagePath = "src/main/resources" + car.getImages().get(0).getImagePath();
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                imageView.setImage(new Image(imageFile.toURI().toString()));
            }
        }
        imageView.setFitWidth(400);
        imageView.setPreserveRatio(true);

        VBox dialogContent = new VBox(imageView);
        dialogContent.setAlignment(Pos.CENTER);
        dialogContent.setPadding(new Insets(10));
        imageAlert.getDialogPane().setContent(dialogContent);
        imageAlert.showAndWait();
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}*/

package fr.parisdauphine.panel;

import fr.parisdauphine.entity.Car;
import fr.parisdauphine.entity.Favorite;
import fr.parisdauphine.entity.User;
import fr.parisdauphine.service.CartService;
import fr.parisdauphine.service.FavoriteService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
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
            cartService.addToCart(favorite.getUser(), favorite.getCar());
            showAlert(Alert.AlertType.INFORMATION, "Ajouté au panier", favorite.getCar().getBrand() + " " + favorite.getCar().getModel() + " a été ajouté au panier.");
        });

        return new HBox(10, removeButton, addToCartButton);
    }

    private void showImageDetail(Car car) {
        Alert imageAlert = new Alert(Alert.AlertType.INFORMATION);
        imageAlert.setTitle("Détails de l'image");
        imageAlert.setHeaderText(car.getBrand() + " " + car.getModel());

        ImageView imageView = new ImageView();
        if (!car.getImages().isEmpty()) {
            String imagePath = "src/main/resources" + car.getImages().get(0).getImagePath();
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                imageView.setImage(new Image(imageFile.toURI().toString()));
            }
        }
        imageView.setFitWidth(400);
        imageView.setPreserveRatio(true);

        VBox dialogContent = new VBox(imageView);
        dialogContent.setAlignment(Pos.CENTER);
        dialogContent.setPadding(new Insets(10));
        imageAlert.getDialogPane().setContent(dialogContent);
        imageAlert.showAndWait();
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
