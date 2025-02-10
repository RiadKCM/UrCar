package fr.parisdauphine.panel;

import fr.parisdauphine.service.UserService;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class LoginPanel extends StackPane {
    private final MainFrame mainFrame;
    private final UserService userService;

    public LoginPanel(MainFrame mainFrame, UserService userService) {
        this.mainFrame = mainFrame;
        this.userService = userService;
        initializeUI();
    }

    private void initializeUI() {
        // Titre du formulaire
        Label titleLabel = new Label("Connexion");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #333;");

        // Grille pour les champs de saisie
        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(15);
        formGrid.setAlignment(Pos.CENTER); // Centre les champs dans la grille

        // Labels et champs
        TextField emailField = new TextField();
        emailField.setPromptText("Entrez votre email");
        emailField.setStyle("-fx-font-size: 14px;");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Entrez votre mot de passe");
        passwordField.setStyle("-fx-font-size: 14px;");

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

        // Ajouter les composants à la grille
        formGrid.add(new Label("Email:"), 0, 0);
        formGrid.add(emailField, 1, 0);
        formGrid.add(new Label("Mot de passe:"), 0, 1);
        formGrid.add(passwordField, 1, 1);
        formGrid.add(passwordTextField, 1, 1); // Ajout du TextField pour afficher le mot de passe
        formGrid.add(showPassword, 1, 2); // Ajout du CheckBox pour activer/désactiver l'affichage du mot de passe

        // Bouton de connexion
        Button loginButton = new Button("Se connecter");
        loginButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white;");
        loginButton.setOnAction(e -> {
            userService.login(emailField.getText(), passwordField.getText())
                    .ifPresentOrElse(
                            user -> {
                                mainFrame.setCurrentUser(user);
                                showAlert("Succès", "Connexion réussie !", Alert.AlertType.INFORMATION);
                                mainFrame.navigateTo("Accueil");
                            },
                            () -> showAlert("Erreur", "Email ou mot de passe incorrect.", Alert.AlertType.ERROR)
                    );
        });

        // Bouton Retour
        Button backButton = new Button("Retour");
        backButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
        backButton.setOnAction(e -> mainFrame.navigateTo("Choix"));

        // Conteneur pour les boutons
        VBox buttonBox = new VBox(10, loginButton, backButton);
        buttonBox.setAlignment(Pos.CENTER);

        // Conteneur principal (Carte)
        VBox formBox = new VBox(15, titleLabel, formGrid, buttonBox);
        formBox.setPadding(new javafx.geometry.Insets(20));
        formBox.setStyle("-fx-background-color: white; -fx-border-radius: 8; -fx-background-radius: 8; "
                + "-fx-border-color: lightgray; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0, 0, 2);");
        formBox.setMaxWidth(300); // Limiter la largeur pour un look compact
        formBox.setMinWidth(250);

        // Centrer la carte exactement au centre de la fenêtre
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
