package fr.parisdauphine.panel;

import fr.parisdauphine.entity.Car;
import fr.parisdauphine.entity.Cart;
import fr.parisdauphine.entity.User;
import fr.parisdauphine.service.CartService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.List;

public class CartPanel extends VBox {
    private final ListView<String> cartListView = new ListView<>();
    private final MainFrame mainFrame;
    private final Label totalLabel = new Label("Total: 0€");
    private final CartService cartService;

    public CartPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.cartService = new CartService();
        initializeUI();
    }

    private void initializeUI() {
        setSpacing(20);
        setPadding(new Insets(10));
        setAlignment(Pos.CENTER);

        VBox cardContainer = new VBox(20);
        cardContainer.setPadding(new Insets(20));
        cardContainer.setStyle("-fx-border-color: lightgrey; -fx-border-width: 2px; -fx-border-radius: 10px; -fx-background-color: white;");
        cardContainer.setAlignment(Pos.CENTER);

        Text title = new Text("Votre Panier");
        title.setFont(Font.font("Arial", 24));
        cardContainer.getChildren().add(title);

        cartListView.setPlaceholder(new Label("Votre panier est vide."));
        cartListView.setStyle("-fx-font-size: 14px;");
        VBox leftColumn = new VBox(10, new Label("Voitures dans le panier:"), cartListView);
        leftColumn.setPadding(new Insets(10));
        leftColumn.setAlignment(Pos.CENTER);

        Button removeButton = new Button("❌ Supprimer l'article");
        removeButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");
        removeButton.setOnAction(e -> removeSelectedItem());
        leftColumn.getChildren().add(removeButton);

        VBox rightColumn = new VBox(10, new HBox(10, new Label("Total : "), totalLabel));
        rightColumn.setPadding(new Insets(10));
        rightColumn.setAlignment(Pos.CENTER);

        Button confirmButton = new Button("✔️ Confirmer l'achat");
        confirmButton.setStyle("-fx-background-color: green; -fx-text-fill: white;");
        confirmButton.setOnAction(e -> handlePurchase());
        rightColumn.getChildren().add(confirmButton);

        HBox mainContainer = new HBox(20, leftColumn, rightColumn);
        mainContainer.setAlignment(Pos.CENTER);

        cardContainer.getChildren().add(mainContainer);
        getChildren().add(cardContainer);

        loadCart();
    }

    private void loadCart() {
        cartListView.getItems().clear();
        List<Car> cars = cartService.getCarsInCart(mainFrame.getCurrentUser());
        for (Car car : cars) {
            cartListView.getItems().add(car.getBrand() + " " + car.getModel() + " - " + car.getPrice() + "€");
        }
        updateTotal();
    }


    private void removeSelectedItem() {
        String selectedItem = cartListView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            String[] parts = selectedItem.split(" - ");
            if (parts.length > 1) {
                String carBrandModel = parts[0].trim();
                cartService.removeCarFromCart(mainFrame.getCurrentUser(), carBrandModel);
                loadCart();
                showAlert(Alert.AlertType.INFORMATION, "Article supprimé", "L'article a été retiré du panier.");
            }
        }
    }



    private void handlePurchase() {
        try {
            cartService.placeOrder(mainFrame.getCurrentUser());
            cartListView.getItems().clear();
            updateTotal();
            showAlert(Alert.AlertType.INFORMATION, "Achat confirmé", "Merci pour votre achat !");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue lors de l'achat.");
        }
    }

    private void updateTotal() {
        double total = cartService.getCartTotal(mainFrame.getCurrentUser());
        totalLabel.setText("Total: " + total + "€");
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}


/*package fr.parisdauphine.panel;

import fr.parisdauphine.config.HibernateUtil;
import fr.parisdauphine.entity.Cart;
import fr.parisdauphine.entity.Order;
import fr.parisdauphine.entity.Car;
import fr.parisdauphine.entity.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.hibernate.Session;
import org.hibernate.Transaction;
import java.util.List;

public class CartPanel extends VBox {
    private final ListView<String> cartListView = new ListView<>();
    private final MainFrame mainFrame;
    private final Label totalLabel = new Label("Total: 0€");

    public CartPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        initializeUI();
    }

    private void initializeUI() {
        setSpacing(20);
        setPadding(new Insets(10));
        setAlignment(Pos.CENTER);

        VBox cardContainer = new VBox(20);
        cardContainer.setPadding(new Insets(20));
        cardContainer.setStyle("-fx-border-color: lightgrey; -fx-border-width: 2px; -fx-border-radius: 10px; -fx-background-color: white;");
        cardContainer.setAlignment(Pos.CENTER);

        Text title = new Text("Votre Panier");
        title.setFont(Font.font("Arial", 24));
        cardContainer.getChildren().add(title);

        cartListView.setPlaceholder(new Label("Votre panier est vide."));
        cartListView.setStyle("-fx-font-size: 14px;");
        VBox leftColumn = new VBox(10, new Label("Voitures dans le panier:"), cartListView);
        leftColumn.setPadding(new Insets(10));
        leftColumn.setAlignment(Pos.CENTER);

        Button removeButton = new Button("❌ Supprimer l'article");
        removeButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");
        removeButton.setOnAction(e -> removeSelectedItem());
        leftColumn.getChildren().add(removeButton);

        VBox rightColumn = new VBox(10, createPaymentForm(), new HBox(10, new Label("Total : "), totalLabel));
        rightColumn.setPadding(new Insets(10));
        rightColumn.setAlignment(Pos.CENTER);

        Button confirmButton = new Button("✔️ Confirmer l'achat");
        confirmButton.setStyle("-fx-background-color: green; -fx-text-fill: white;");
        confirmButton.setOnAction(e -> handlePurchase());
        rightColumn.getChildren().add(confirmButton);

        HBox mainContainer = new HBox(20, leftColumn, rightColumn);
        mainContainer.setAlignment(Pos.CENTER);

        cardContainer.getChildren().add(mainContainer);
        getChildren().add(cardContainer);

        loadCartFromDatabase();
    }

    private void loadCartFromDatabase() {
        cartListView.getItems().clear();
        User currentUser = mainFrame.getCurrentUser();
        if (currentUser != null) {
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                Cart cart = session.createQuery("FROM Cart c WHERE c.user = :user", Cart.class)
                        .setParameter("user", currentUser)
                        .uniqueResult();
                if (cart != null) {
                    for (Car car : cart.getCars()) {
                        cartListView.getItems().add(car.getBrand() + " " + car.getModel() + " - " + car.getPrice() + "€");
                    }
                }
                updateTotal();
            }
        }
    }

    private void removeSelectedItem() {
        String selectedItem = cartListView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            String[] parts = selectedItem.split(" - ");
            if (parts.length > 1) { 
                String carBrandModel = parts[0].trim();
                User currentUser = mainFrame.getCurrentUser();
                if (currentUser != null) {
                    try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                        Transaction tx = session.beginTransaction();
    
                        // Récupérer la voiture basée sur son modèle et sa marque
                        Car carToRemove = session.createQuery(
                            "FROM Car c WHERE CONCAT(c.brand, ' ', c.model) = :brandModel", Car.class)
                            .setParameter("brandModel", carBrandModel)
                            .uniqueResult();
    
                        if (carToRemove != null) {
                            Cart cart = session.createQuery(
                                "FROM Cart c WHERE c.user = :user", Cart.class)
                                .setParameter("user", currentUser)
                                .uniqueResult();
    
                            if (cart != null && cart.getCars().contains(carToRemove)) {
                                cart.getCars().remove(carToRemove);
                                session.persist(cart);
                                tx.commit();
    
                                cartListView.getItems().remove(selectedItem);
                                updateTotal();
                                showAlert(Alert.AlertType.INFORMATION, "Article supprimé", "L'article a été retiré du panier.");
                            } else {
                                showAlert(Alert.AlertType.ERROR, "Erreur", "L'article n'est pas dans votre panier.");
                                tx.rollback();
                            }
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de trouver l'article.");
                        }
                    }
                }
            }
        }
    }    

    private void handlePurchase() {
        if (!cartListView.getItems().isEmpty()) {
            User currentUser = mainFrame.getCurrentUser();
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                Transaction tx = session.beginTransaction();
    
                // Récupérer le panier de l'utilisateur
                Cart cart = session.createQuery("FROM Cart c WHERE c.user = :user", Cart.class)
                        .setParameter("user", currentUser)
                        .uniqueResult();
    
                if (cart != null) {
                    // Créer une nouvelle commande avec les informations du panier
                    Order order = new Order(currentUser, cart.getCars());
                    order.setStatus(Order.Status.PAYE); // Modifier le statut à "PAYE"
                    session.persist(order); // Insérer la commande dans la base de données
    
                    // Marquer les voitures comme vendues
                    for (Car car : cart.getCars()) {
                        car.setStatus(Car.Status.VENDU);
                        session.persist(car); // Mettre à jour les voitures dans la base de données
                    }
    
                    // Vider le panier de l'utilisateur
                    cart.getCars().clear();
                    session.persist(cart);
    
                    // Commit de la transaction
                    tx.commit();
                }
    
                // Mettre à jour l'interface utilisateur
                cartListView.getItems().clear();
                updateTotal();
                showAlert(Alert.AlertType.INFORMATION, "Achat confirmé", "Merci pour votre achat !");
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue lors de l'achat.");
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Panier vide", "Votre panier est vide.");
        }
    }       

    private void updateTotal() {
        User currentUser = mainFrame.getCurrentUser();
        if (currentUser != null) {
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                Double total = session.createQuery(
                        "SELECT SUM(c.price) FROM Cart cart JOIN cart.cars c WHERE cart.user = :user",
                        Double.class
                ).setParameter("user", currentUser).uniqueResult();

                totalLabel.setText("Total: " + (total != null ? total : 0) + "€");
            }
        }
    }

    private VBox createPaymentForm() {
        VBox form = new VBox(10);
        form.setAlignment(Pos.CENTER);
        form.getChildren().add(new Label("Informations de paiement :"));

        TextField nameField = new TextField();
        nameField.setPromptText("Nom complet");
        form.getChildren().add(new HBox(10, new Label("Nom complet:"), nameField));

        TextField cardNumberField = new TextField();
        cardNumberField.setPromptText("Numéro de carte");
        form.getChildren().add(new HBox(10, new Label("Numéro de carte:"), cardNumberField));

        PasswordField bicField = new PasswordField();
        bicField.setPromptText("Code secret");
        form.getChildren().add(new HBox(10, new Label("Code Secret:"), bicField));

        TextField expiryDateField = new TextField();
        expiryDateField.setPromptText("MM/YY");
        form.getChildren().add(new HBox(10, new Label("Date d'expiration (MM/YY):"), expiryDateField));

        return form;
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}*/
