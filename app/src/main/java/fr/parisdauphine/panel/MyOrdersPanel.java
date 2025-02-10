package fr.parisdauphine.panel;

import fr.parisdauphine.entity.Order;
import fr.parisdauphine.entity.Car;
import fr.parisdauphine.entity.User;
import fr.parisdauphine.service.OrderService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import java.util.List;

public class MyOrdersPanel extends VBox {
    private final MainFrame mainFrame;
    private final VBox ordersContainer;
    private final OrderService orderService;

    public MyOrdersPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.orderService = new OrderService();
        setPadding(new Insets(20));
        setSpacing(15);
        setAlignment(Pos.CENTER);
        setStyle("-fx-background-color: #ffffff; -fx-border-radius: 10px; -fx-padding: 15px;");

        // Titre
        Text title = new Text("Mes Commandes");
        title.setFont(Font.font("Arial", 24));
        title.setStyle("-fx-fill: #333333; -fx-font-weight: bold;");
        getChildren().add(title);

        // Conteneur des commandes avec ScrollPane
        ordersContainer = new VBox(10);
        ordersContainer.setPadding(new Insets(10));
        ordersContainer.setStyle("-fx-background-color: #f8f9fa; -fx-border-radius: 10px; -fx-padding: 10px;");

        ScrollPane scrollPane = new ScrollPane(ordersContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(300);
        scrollPane.setStyle("-fx-background: transparent; -fx-border-color: gray; -fx-border-radius: 5px;");
        getChildren().add(scrollPane);

        // Chargement des commandes dès le début
        loadOrders();
    }

    private void loadOrders() {
        ordersContainer.getChildren().clear();
        User currentUser = mainFrame.getCurrentUser();
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Vous devez être connecté pour voir vos commandes.");
            return;
        }

        List<Order> orders = orderService.getOrdersByUser(currentUser);
        if (orders.isEmpty()) {
            Label noOrdersLabel = new Label("Aucune commande trouvée.");
            noOrdersLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: gray;");
            ordersContainer.getChildren().add(noOrdersLabel);
        } else {
            orders.forEach(order -> ordersContainer.getChildren().add(createOrderCard(order)));
        }
    }

    private VBox createOrderCard(Order order) {
        VBox orderBox = new VBox(10);
        orderBox.setPadding(new Insets(10));
        orderBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #ddd; -fx-border-radius: 5px; -fx-padding: 15px;");
        orderBox.setAlignment(Pos.CENTER_LEFT);

        Label orderIdLabel = new Label("Commande #" + order.getId());
        orderIdLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #333333;");

        Label dateLabel = new Label("Date: " + order.getOrderDate());
        dateLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #555555;");

        Label statusLabel = new Label("Statut: " + order.getStatus());
        statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #007bff; -fx-font-weight: bold;");

        ListView<String> carListView = new ListView<>();
        for (Car car : order.getCars()) {
            carListView.getItems().add(car.getModel() + " - " + car.getPrice() + "€");
        }
        carListView.setPrefHeight(80);
        carListView.setMaxWidth(300);

        orderBox.getChildren().addAll(orderIdLabel, dateLabel, statusLabel, carListView);
        return orderBox;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}


/*package fr.parisdauphine.panel;

import fr.parisdauphine.config.HibernateUtil;
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
import java.util.List;

public class MyOrdersPanel extends VBox {
    private final MainFrame mainFrame;
    private final VBox ordersContainer;

    public MyOrdersPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setPadding(new Insets(20));
        setSpacing(15);
        setAlignment(Pos.CENTER);
        setStyle("-fx-background-color: #ffffff; -fx-border-radius: 10px; -fx-padding: 15px;");

        // Titre
        Text title = new Text("Mes Commandes");
        title.setFont(Font.font("Arial", 24));
        title.setStyle("-fx-fill: #333333; -fx-font-weight: bold;");
        getChildren().add(title);

        // Conteneur des commandes avec ScrollPane
        ordersContainer = new VBox(10);
        ordersContainer.setPadding(new Insets(10));
        ordersContainer.setStyle("-fx-background-color: #f8f9fa; -fx-border-radius: 10px; -fx-padding: 10px;");

        ScrollPane scrollPane = new ScrollPane(ordersContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(300);
        scrollPane.setStyle("-fx-background: transparent; -fx-border-color: gray; -fx-border-radius: 5px;");
        getChildren().add(scrollPane);

        // Chargement des commandes dès le début
        loadOrders();
    }

    // Chargement des commandes de l'utilisateur
    private void loadOrders() {
        ordersContainer.getChildren().clear();
        User currentUser = mainFrame.getCurrentUser();
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Vous devez être connecté pour voir vos commandes.");
            return;
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Order> orders = session.createQuery("FROM Order o WHERE o.user.id = :userId ORDER BY o.orderDate DESC", Order.class)
                    .setParameter("userId", currentUser.getId())
                    .list();
            if (orders.isEmpty()) {
                Label noOrdersLabel = new Label("Aucune commande trouvée.");
                noOrdersLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: gray;");
                ordersContainer.getChildren().add(noOrdersLabel);
            } else {
                orders.forEach(order -> ordersContainer.getChildren().add(createOrderCard(order)));
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les commandes.");
        }
    }

    // Création d'une carte d'affichage pour une commande
    private VBox createOrderCard(Order order) {
        VBox orderBox = new VBox(10);
        orderBox.setPadding(new Insets(10));
        orderBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #ddd; -fx-border-radius: 5px; -fx-padding: 15px;");
        orderBox.setAlignment(Pos.CENTER_LEFT);

        Label orderIdLabel = new Label("Commande #" + order.getId());
        orderIdLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #333333;");

        Label dateLabel = new Label("Date: " + order.getOrderDate());
        dateLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #555555;");

        Label statusLabel = new Label("Statut: " + order.getStatus());
        statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #007bff; -fx-font-weight: bold;");

        // Liste des voitures associées à la commande
        ListView<String> carListView = new ListView<>();
        for (Car car : order.getCars()) {
            carListView.getItems().add(car.getModel() + " - " + car.getPrice() + "€");
        }
        carListView.setPrefHeight(80);
        carListView.setMaxWidth(300);

        orderBox.getChildren().addAll(orderIdLabel, dateLabel, statusLabel, carListView);
        return orderBox;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}*/