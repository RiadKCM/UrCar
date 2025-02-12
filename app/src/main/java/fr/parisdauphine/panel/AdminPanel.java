package fr.parisdauphine.panel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class AdminPanel extends VBox {
    private final MainFrame mainFrame;
    public AdminPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        setSpacing(20);
        setPadding(new Insets(20));
        setAlignment(Pos.CENTER);
        setStyle("-fx-background-color: #f4f4f4;");

        Label titleLabel = new Label("👑 Espace Administrateur");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Button manageUsersButton = new Button("Gérer les Utilisateurs");
        manageUsersButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        manageUsersButton.setOnAction(e -> mainFrame.navigateTo("Gestion des utilisateurs"));

        Button viewSalesButton = new Button("Consulter les Ventes");
        viewSalesButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        viewSalesButton.setOnAction(e -> showAlert("Consultation des ventes"));

        Button dashboardButton = new Button("Tableau de Bord");
        dashboardButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white;");
        dashboardButton.setOnAction(e -> showAlert("Tableau de bord admin"));

        Button manageCarsButton = new Button("Gérer les Voitures");
        manageCarsButton.setStyle("-fx-background-color: #FF5722; -fx-text-fill: white;");
        manageCarsButton.setOnAction(e -> mainFrame.navigateTo("GestionVoitures"));

        getChildren().addAll(titleLabel, manageUsersButton, viewSalesButton, dashboardButton, manageCarsButton);
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Admin Panel");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

