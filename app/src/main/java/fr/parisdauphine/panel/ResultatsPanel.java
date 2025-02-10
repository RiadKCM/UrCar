/*package fr.parisdauphine.panel;

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
import fr.parisdauphine.config.HibernateUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ResultatsPanel extends VBox {

    private final MainFrame mainFrame;
    private List<Car> carList;
    private static final int ITEMS_PER_PAGE = 20; 
    private int currentPage = 1;

    public ResultatsPanel(List<Car> results, MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        // ✅ Si la liste est vide, on charge toutes les voitures depuis la base
        if (results == null || results.isEmpty()) {
            this.carList = fetchAllCars();
        } else {
            this.carList = results;
        }

        initializeUI();
    }

    private void initializeUI() {
        setPadding(new Insets(20));
        setSpacing(15);
        setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Résultats de la recherche");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        ScrollPane scrollPane = new ScrollPane();
        VBox resultsContainer = new VBox(10);
        scrollPane.setContent(resultsContainer);
        scrollPane.setFitToWidth(true);

        Button backButton = new Button("Retour");
        backButton.setOnAction(e -> mainFrame.navigateTo("Accueil"));

        getChildren().addAll(titleLabel, scrollPane, createPaginationControls(), backButton);
        updateResults(resultsContainer);
    }

    private void updateResults(VBox resultsContainer) {
        resultsContainer.getChildren().clear();

        int fromIndex = (currentPage - 1) * ITEMS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, carList.size());

        List<Car> currentPageItems = carList.subList(fromIndex, toIndex);

        if (currentPageItems.isEmpty()) {
            Label noResults = new Label("Aucun véhicule ne correspond à votre recherche.");
            resultsContainer.getChildren().add(noResults);
        } else {
            for (Car car : currentPageItems) {
                HBox carCard = createCarCard(car);
                resultsContainer.getChildren().add(carCard);
            }
        }
    }

    private HBox createCarCard(Car car) {
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
            } else {
                carImageView.setStyle("-fx-border-color: gray; -fx-border-style: dashed;");
            }

            // ✅ Clic pour afficher le carrousel
            carImageView.setOnMouseClicked(e -> {
                CarAdminPanel panel = new CarAdminPanel(mainFrame);
                panel.showImageCarousel(car);
            });
        }

        VBox carInfo = new VBox(5);
        Label brandLabel = new Label(car.getBrand() + " " + car.getModel());
        brandLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Label priceLabel = new Label("Prix : " + car.getPrice() + " €");

        // ✅ Bouton "Ajouter au Panier" avec icône 🛒
        Button addToCartButton = new Button("🛒 Ajouter au panier");
        addToCartButton.setStyle("-fx-background-color: green; -fx-text-fill: white;");
        addToCartButton.setOnAction(e -> addToCart(car));

        // ❤️ Bouton Like
        Button likeButton = new Button("❤️");
        likeButton.setStyle("-fx-background-color: transparent; -fx-font-size: 16px;");
        likeButton.setOnAction(e -> addToFavorites(car));

        HBox buttonBox = new HBox(10, likeButton, addToCartButton);
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
                session.persist(cartItem);
            }

            if (!cartItem.getCars().contains(car)) {
                cartItem.getCars().add(car);
                session.merge(cartItem);
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

    private void addToFavorites(Car car) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();

            // Supposons que l'utilisateur connecté est "user"
            User user = mainFrame.getCurrentUser(); // ✅ Récupérer l'utilisateur connecté

            // Vérifie si la voiture est déjà dans les favoris
            Favorite existingFavorite = session.createQuery(
                            "FROM Favorite f WHERE f.user = :user AND f.car = :car", Favorite.class)
                    .setParameter("user", user)
                    .setParameter("car", car)
                    .uniqueResult();

            if (existingFavorite == null) {
                // ✅ Ajouter le favori
                Favorite favorite = new Favorite();
                favorite.setUser(user);
                favorite.setCar(car);
                session.save(favorite);
                session.getTransaction().commit();
                showAlert(Alert.AlertType.INFORMATION, "Favori ajouté", "La voiture a été ajoutée à vos favoris !");
            } else {
                showAlert(Alert.AlertType.WARNING, "Déjà en favori", "Cette voiture est déjà dans vos favoris !");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ajouter aux favoris.");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private HBox createPaginationControls() {
        HBox paginationControls = new HBox(10);
        paginationControls.setAlignment(Pos.CENTER);

        int totalPages = (int) Math.ceil((double) carList.size() / ITEMS_PER_PAGE);

        // Bouton "Précédent"
        Button previousButton = new Button("⟨ Précédent");
        previousButton.setDisable(currentPage == 1);
        previousButton.setOnAction(e -> {
            if (currentPage > 1) {
                currentPage--;
                updateResults((VBox) ((ScrollPane) getChildren().get(1)).getContent());
                refreshPaginationControls();
            }
        });
        paginationControls.getChildren().add(previousButton);

        // Création des boutons de pages
        for (int i = 1; i <= totalPages; i++) {
            Button pageButton = new Button(String.valueOf(i));
            int pageNumber = i;

            // Désactiver uniquement le bouton de la page actuelle
            pageButton.setDisable(pageNumber == currentPage);

            pageButton.setOnAction(e -> {
                currentPage = pageNumber;
                updateResults((VBox) ((ScrollPane) getChildren().get(1)).getContent());
                refreshPaginationControls();
            });

            paginationControls.getChildren().add(pageButton);
        }

        // Bouton "Suivant"
        Button nextButton = new Button("Suivant ⟩");
        nextButton.setDisable(currentPage == totalPages);
        nextButton.setOnAction(e -> {
            if (currentPage < totalPages) {
                currentPage++;
                updateResults((VBox) ((ScrollPane) getChildren().get(1)).getContent());
                refreshPaginationControls();
            }
        });
        paginationControls.getChildren().add(nextButton);

        return paginationControls;
    }

    private void refreshPaginationControls() {
        // On supprime l'ancien conteneur de pagination et on en ajoute un nouveau
        getChildren().removeIf(node -> node instanceof HBox && ((HBox) node).getChildren().stream().anyMatch(n -> n instanceof Button));
        getChildren().add(getChildren().size() - 1, createPaginationControls());
    }
}*/

package fr.parisdauphine.panel;

import fr.parisdauphine.entity.Car;
import fr.parisdauphine.entity.User;
import fr.parisdauphine.service.CarService;
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

public class ResultatsPanel extends VBox {

    private final MainFrame mainFrame;
    private List<Car> carList;
    private static final int ITEMS_PER_PAGE = 20;
    private int currentPage = 1;
    private Button nextButton;
    private Button previousButton;


    private final CarService carService = new CarService();
    private final CartService cartService = new CartService();
    private final FavoriteService favoriteService = new FavoriteService();

    public ResultatsPanel(List<Car> results, MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        if (results == null || results.isEmpty()) {
            this.carList = carService.getAvailableCars();
        } else {
            this.carList = results;
        }

        initializeUI();
    }

    private void initializeUI() {
        setPadding(new Insets(20));
        setSpacing(15);
        setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Résultats de la recherche");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        ScrollPane scrollPane = new ScrollPane();
        VBox resultsContainer = new VBox(10);
        scrollPane.setContent(resultsContainer);
        scrollPane.setFitToWidth(true);

        Button backButton = new Button("Retour");
        backButton.setOnAction(e -> mainFrame.navigateTo("Accueil"));

        getChildren().addAll(titleLabel, scrollPane, createPaginationControls(), backButton);
        updateResults(resultsContainer);
    }

    private void updateResults(VBox resultsContainer) {
        resultsContainer.getChildren().clear();

        int fromIndex = (currentPage - 1) * ITEMS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, carList.size());

        List<Car> currentPageItems = carList.subList(fromIndex, toIndex);

        if (currentPageItems.isEmpty()) {
            Label noResults = new Label("Aucun véhicule ne correspond à votre recherche.");
            resultsContainer.getChildren().add(noResults);
        } else {
            for (Car car : currentPageItems) {
                HBox carCard = createCarCard(car);
                resultsContainer.getChildren().add(carCard);
            }
        }
    }

    private HBox createCarCard(Car car) {
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
            } else {
                carImageView.setStyle("-fx-border-color: gray; -fx-border-style: dashed;");
            }
        }

        VBox carInfo = new VBox(5);
        Label brandLabel = new Label(car.getBrand() + " " + car.getModel());
        brandLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Label priceLabel = new Label("Prix : " + car.getPrice() + " €");

        Button addToCartButton = new Button("🛒 Ajouter au panier");
        addToCartButton.setStyle("-fx-background-color: green; -fx-text-fill: white;");
        addToCartButton.setOnAction(e -> addToCart(car));

        Button likeButton = new Button("❤️");
        likeButton.setStyle("-fx-background-color: transparent; -fx-font-size: 16px;");
        likeButton.setOnAction(e -> addToFavorites(car));
        carImageView.setOnMouseClicked(e -> showImageDetail(car));
        HBox buttonBox = new HBox(10, likeButton, addToCartButton);
        carInfo.getChildren().addAll(brandLabel, priceLabel, buttonBox);
        carCard.getChildren().addAll(carImageView, carInfo);
        return carCard;
    }

    private void addToCart(Car car) {
        User currentUser = mainFrame.getCurrentUser();
        if (currentUser == null) {
            showAlert(Alert.AlertType.WARNING, "Non connecté", "Veuillez vous connecter pour ajouter au panier.");
            return;
        }

        cartService.addToCart(currentUser, car);
        showAlert(Alert.AlertType.INFORMATION, "Ajouté au panier", car.getBrand() + " " + car.getModel() + " a été ajouté au panier.");
    }

    private void addToFavorites(Car car) {
        User currentUser = mainFrame.getCurrentUser();
        if (currentUser == null) {
            showAlert(Alert.AlertType.WARNING, "Non connecté", "Veuillez vous connecter pour ajouter aux favoris.");
            return;
        }

        favoriteService.addToFavorites(currentUser, car);
        showAlert(Alert.AlertType.INFORMATION, "Favori ajouté", "La voiture a été ajoutée à vos favoris !");
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private HBox createPaginationControls() {
        HBox paginationControls = new HBox(10);
        paginationControls.setAlignment(Pos.CENTER);
    
        int totalPages = (int) Math.ceil((double) carList.size() / ITEMS_PER_PAGE);
    
        previousButton = new Button("⟨ Précédent");
        previousButton.setDisable(currentPage == 1);
        previousButton.setOnAction(e -> {
            if (currentPage > 1) {
                currentPage--;
                updateResults((VBox) ((ScrollPane) getChildren().get(1)).getContent());
                refreshPaginationControls();
            }
        });
        paginationControls.getChildren().add(previousButton);
    
        for (int i = 1; i <= totalPages; i++) {
            Button pageButton = new Button(String.valueOf(i));
            int pageNumber = i;
            pageButton.setDisable(pageNumber == currentPage);
            pageButton.setOnAction(e -> {
                currentPage = pageNumber;
                updateResults((VBox) ((ScrollPane) getChildren().get(1)).getContent());
                refreshPaginationControls();
            });
    
            paginationControls.getChildren().add(pageButton);
        }
    
        nextButton = new Button("Suivant ⟩");
        nextButton.setDisable(currentPage == totalPages);
        nextButton.setOnAction(e -> {
            if (currentPage < totalPages) {
                currentPage++;
                updateResults((VBox) ((ScrollPane) getChildren().get(1)).getContent());
                refreshPaginationControls();
            }
        });
    
        paginationControls.getChildren().add(nextButton);
    
        return paginationControls;
    }
    
    private void showImageDetail(Car car) {
        List<String> imagePaths = car.getImages().stream()
                .map(img -> "src/main/resources" + img.getImagePath())
                .toList();
    
        if (imagePaths.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Aucune image", "Aucune image disponible pour cette voiture.");
            return;
        }
    
        Dialog<Void> imageDialog = new Dialog<>();
        imageDialog.setTitle("Détails des images");
        imageDialog.setHeaderText(car.getBrand() + " " + car.getModel());
    
        VBox dialogContent = new VBox(10);
        dialogContent.setAlignment(Pos.CENTER);
        dialogContent.setPadding(new Insets(10));
    
        ImageView imageView = new ImageView();
        imageView.setFitWidth(500);
        imageView.setFitHeight(300);
        imageView.setPreserveRatio(true);
    
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(imageView);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setPannable(true); // Permet de scroller avec la souris
    
        final int[] currentIndex = {0};
        updateImageView(imageView, imagePaths.get(currentIndex[0]));
    
        Button prevButton = new Button("⬅ Précédent");
        prevButton.setDisable(currentIndex[0] == 0);
        prevButton.setOnAction(e -> {
            if (currentIndex[0] > 0) {
                currentIndex[0]--;
                updateImageView(imageView, imagePaths.get(currentIndex[0]));
                nextButton.setDisable(false);
            }
            if (currentIndex[0] == 0) {
                prevButton.setDisable(true);
            }
        });
    
        Button nextButton = new Button("Suivant ➡");
        nextButton.setDisable(currentIndex[0] == imagePaths.size() - 1);
        nextButton.setOnAction(e -> {
            if (currentIndex[0] < imagePaths.size() - 1) {
                currentIndex[0]++;
                updateImageView(imageView, imagePaths.get(currentIndex[0]));
                prevButton.setDisable(false);
            }
            if (currentIndex[0] == imagePaths.size() - 1) {
                nextButton.setDisable(true);
            }
        });
    
        HBox buttonBox = new HBox(10, prevButton, nextButton);
        buttonBox.setAlignment(Pos.CENTER);
    
        dialogContent.getChildren().addAll(scrollPane, buttonBox);
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

    private void refreshPaginationControls() {
        int totalPages = (int) Math.ceil((double) carList.size() / ITEMS_PER_PAGE);
    
        previousButton.setDisable(currentPage == 1);
        nextButton.setDisable(currentPage == totalPages);
    }
}
