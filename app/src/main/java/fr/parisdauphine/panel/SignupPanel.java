package fr.parisdauphine.panel;

import fr.parisdauphine.entity.User;
import fr.parisdauphine.service.UserService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class SignupPanel extends StackPane {
    private final MainFrame mainFrame;
    private final UserService userService;

    public SignupPanel(MainFrame mainFrame, UserService userService) {
        this.mainFrame = mainFrame;
        this.userService = userService;
        initializeUI();
    }

    private void initializeUI() {
        Label titleLabel = new Label("Inscription");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #333;");

        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(15);
        formGrid.setAlignment(Pos.CENTER);

        TextField nameField = new TextField();
        TextField prenomField = new TextField();
        TextField emailField = new TextField();
        TextField telephoneField = new TextField();
        PasswordField passwordField = new PasswordField();

        // Afficher le mot de passe
        CheckBox showPassword = new CheckBox("Voir");
        TextField passwordTextField = new TextField();
        passwordTextField.setManaged(false);
        passwordTextField.setVisible(false);

        passwordTextField.textProperty().bindBidirectional(passwordField.textProperty());
        showPassword.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
            if (isNowSelected) {
                passwordTextField.setManaged(true);
                passwordTextField.setVisible(true);
                passwordField.setManaged(false);
                passwordField.setVisible(false);
            } else {
                passwordTextField.setManaged(false);
                passwordTextField.setVisible(false);
                passwordField.setManaged(true);
                passwordField.setVisible(true);
            }
        });

        formGrid.add(new Label("Nom:"), 0, 0);
        formGrid.add(nameField, 1, 0);
        formGrid.add(new Label("Prénom:"), 0, 1);
        formGrid.add(prenomField, 1, 1);
        formGrid.add(new Label("Email:"), 0, 2);
        formGrid.add(emailField, 1, 2);
        formGrid.add(new Label("Téléphone:"), 0, 3);
        formGrid.add(telephoneField, 1, 3);
        formGrid.add(new Label("Mot de passe:"), 0, 4);
        formGrid.add(passwordField, 1, 4);
        formGrid.add(passwordTextField, 1, 4); // Ajouter le champ texte pour afficher le mot de passe
        formGrid.add(showPassword, 1, 5); // Ajouter la case à cocher pour voir le mot de passe

        Button registerButton = new Button("S'inscrire");
        registerButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
        registerButton.setOnAction(e -> {
            User user = new User();
            user.setNom(nameField.getText());
            user.setPrenom(prenomField.getText());
            user.setEmail(emailField.getText());
            user.setMotDePasse(passwordField.getText());
            user.setTelephone(telephoneField.getText());
            user.setRole(User.Role.USER);

            String result = userService.register(user);
            if ("OK".equals(result)) {
                showAlert("Succès", "Inscription réussie !", Alert.AlertType.INFORMATION);
                mainFrame.navigateTo("Connexion");
            } else {
                showAlert("Erreur", result, Alert.AlertType.ERROR);
            }
        });

        Button backButton = new Button("Retour");
        backButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
        backButton.setOnAction(e -> mainFrame.navigateTo("Choix"));

        VBox buttonBox = new VBox(10, registerButton, backButton);
        buttonBox.setAlignment(Pos.CENTER);

        VBox formBox = new VBox(15, titleLabel, formGrid, buttonBox);
        formBox.setPadding(new Insets(20));
        formBox.setAlignment(Pos.CENTER);
        formBox.setStyle("-fx-background-color: white; -fx-border-radius: 8; -fx-background-radius: 8; "
                + "-fx-border-color: lightgray; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0, 0, 2);");
        formBox.setMaxWidth(350);

        setAlignment(Pos.CENTER);
        getChildren().add(formBox);
    }

    private void showAlert(String title, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
