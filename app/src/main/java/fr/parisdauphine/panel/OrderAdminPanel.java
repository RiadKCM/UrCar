package fr.parisdauphine.panel;

import fr.parisdauphine.config.HibernateUtil;
import fr.parisdauphine.entity.Order;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.hibernate.Session;
import org.hibernate.Transaction;
import java.util.List;

public class OrderAdminPanel extends VBox {
    private final MainFrame mainFrame;
    private VBox orderListContainer;

    public OrderAdminPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        initializeUI();
    }

    private void initializeUI() {
        setPadding(new Insets(20));
        setSpacing(10);
        setAlignment(Pos.TOP_CENTER);

        Label titleLabel = new Label("\uD83D\uDCE6 Gestion des Commandes");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Button refreshButton = new Button("Rafraîchir");
        refreshButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white;");
        refreshButton.setOnAction(e -> loadOrders());

        orderListContainer = new VBox(10);
        ScrollPane orderScrollPane = new ScrollPane(orderListContainer);
        orderScrollPane.setFitToWidth(true);
        orderScrollPane.setPrefHeight(300);

        loadOrders();
        getChildren().addAll(titleLabel, refreshButton, orderScrollPane);
    }

    private void loadOrders() {
        new Thread(() -> {
            List<Order> orders;
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                orders = session.createQuery("FROM Order", Order.class).list();
            }

            Platform.runLater(() -> {
                orderListContainer.getChildren().clear();
                for (Order order : orders) {
                    orderListContainer.getChildren().add(createOrderCard(order));
                }
            });
        }).start();
    }

    private HBox createOrderCard(Order order) {
        HBox orderCard = new HBox(10);
        orderCard.setPadding(new Insets(10));
        orderCard.setStyle("-fx-border-color: black; -fx-border-radius: 5; -fx-border-width: 1;");
        orderCard.setAlignment(Pos.CENTER_LEFT);

        Label orderInfo = new Label("Commande #" + order.getId() + " - Statut: " + order.getStatus());

        ComboBox<String> statusDropdown = new ComboBox<>();
        statusDropdown.getItems().addAll( "En cours", "Expédiée", "Livrée", "Annulée");
        statusDropdown.setValue(order.getStatus().toString());

        Button updateButton = new Button("Modifier");
        updateButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        updateButton.setOnAction(e -> updateOrderStatus(order, statusDropdown.getValue()));

        orderCard.getChildren().addAll(orderInfo, statusDropdown, updateButton);
        return orderCard;
    }

    private void updateOrderStatus(Order order, String newStatus) {
        new Thread(() -> {
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                Transaction transaction = session.beginTransaction();
                System.out.println("Transaction démarrée");
                
                // Mapping manuel pour éviter les erreurs d'encodage
                Order.Status mappedStatus = mapStatus(newStatus);
                if (mappedStatus != null) {
                    order.setStatus(mappedStatus);
                    session.update(order);
                    System.out.println("Commande mise à jour: " + order.getId() + " - Nouveau statut: " + mappedStatus);
                    transaction.commit();
                    System.out.println("Transaction commitée");
    
                    // Mettre à jour directement le statut dans MyOrdersPanel
                    Platform.runLater(() -> {
                        mainFrame.updateMyOrdersPanel(order);  // Appel à la méthode qui met à jour le statut dans le panel utilisateur
                    });
                } else {
                    System.err.println("Statut inconnu : " + newStatus);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }          

    private Order.Status mapStatus(String status) {
        switch (status.toLowerCase()) {
            case "en cours": return Order.Status.EN_COURS;
            case "expédiée": return Order.Status.EXPEDIEE; 
            case "livrée": return Order.Status.LIVREE;
            case "annulée": return Order.Status.ANNULEE;
            default: return null;
        }
    }
    
}
