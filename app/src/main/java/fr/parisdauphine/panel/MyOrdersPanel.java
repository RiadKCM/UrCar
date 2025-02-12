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

    public void updateOrderStatus(Order updatedOrder) {
        for (int i = 0; i < ordersContainer.getChildren().size(); i++) {
            VBox orderBox = (VBox) ordersContainer.getChildren().get(i);
            Label orderIdLabel = (Label) orderBox.lookup(".orderIdLabel");
            
            if (orderIdLabel != null && orderIdLabel.getText().contains("Commande #" + updatedOrder.getId())) {
                // Trouver la commande correspondante et mettre à jour son statut
                Label statusLabel = (Label) orderBox.lookup(".statusLabel");
                if (statusLabel != null) {
                    statusLabel.setText("Statut: " + updatedOrder.getStatus());
                }
            }
        }
    }
    
    private VBox createOrderCard(Order order) {
        VBox orderBox = new VBox(10);
        orderBox.setPadding(new Insets(10));
        orderBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #ddd; -fx-border-radius: 5px; -fx-padding: 15px;");
        orderBox.setAlignment(Pos.CENTER_LEFT);
    
        Label orderIdLabel = new Label("Commande #" + order.getId());
        orderIdLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #333333;");
        orderIdLabel.setId("orderIdLabel"); // Ajouter un identifiant unique
    
        Label dateLabel = new Label("Date: " + order.getOrderDate());
        dateLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #555555;");
    
        Label statusLabel = new Label("Statut: " + order.getStatus());
        statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #007bff; -fx-font-weight: bold;");
        statusLabel.setId("statusLabel"); // Ajouter un identifiant unique
    
        // Calcul du prix total de la commande
        double totalPrice = order.getCars().stream().mapToDouble(Car::getPrice).sum();
        Label totalPriceLabel = new Label("Prix total: " + totalPrice + "€");
        totalPriceLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333;");
    
        // Liste des voitures associées à la commande
        ListView<String> carListView = new ListView<>();
        for (Car car : order.getCars()) {
            carListView.getItems().add(car.getBrand() + car.getModel() + " \n- Détails : " + car.getdescription());
        }
        carListView.setPrefHeight(80);
        carListView.setMaxWidth(300);
    
        orderBox.getChildren().addAll(orderIdLabel, dateLabel, statusLabel, totalPriceLabel, carListView);
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