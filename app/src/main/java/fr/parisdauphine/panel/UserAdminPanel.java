package fr.parisdauphine.panel;

import fr.parisdauphine.config.HibernateUtil;
import fr.parisdauphine.entity.User;
import fr.parisdauphine.entity.Review;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.hibernate.Session;

import java.util.List;

public class UserAdminPanel extends VBox {
    private final MainFrame mainFrame;
    private VBox userListContainer;
    private VBox reviewListContainer;

    public UserAdminPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        initializeUI();
    }

    private void initializeUI() {
        setPadding(new Insets(20));
        setSpacing(10);
        setAlignment(Pos.TOP_CENTER);

        Label titleLabel = new Label("👑 Gestion des Utilisateurs");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Button addUserButton = new Button("➕ Ajouter un Utilisateur");
        addUserButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        addUserButton.setOnAction(e -> showAddUserForm());

        Button manageReviewsButton = new Button("Gérer les Avis");
        manageReviewsButton.setStyle("-fx-background-color: #FF5722; -fx-text-fill: white;");
        manageReviewsButton.setOnAction(e ->  mainFrame.navigateTo("Gérer les Avis"));

        Button manageInvoiceButton = new Button("Gérer les Factures");
        manageInvoiceButton.setStyle("-fx-background-color: #FF5722; -fx-text-fill: white;");
        manageInvoiceButton.setOnAction(e ->  mainFrame.navigateTo("Gérer les Factures"));

        userListContainer = new VBox(10);
        reviewListContainer = new VBox(10);

        ScrollPane userScrollPane = new ScrollPane(userListContainer);
        userScrollPane.setFitToWidth(true);
        userScrollPane.setPrefHeight(300);

        loadUsers();
        getChildren().addAll(
                titleLabel, 
                addUserButton, 
                manageReviewsButton,
                manageInvoiceButton,
                userScrollPane
        );
    }

    private void showManageReviews() {
        // Création d'une fenêtre secondaire (Stage) pour la gestion des avis
        Stage reviewStage = new Stage();
        VBox reviewForm = new VBox(10);
        reviewForm.setPadding(new Insets(20));
    
        Label reviewLabel = new Label("Gérer les Avis");
        reviewLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
    
        // Ajouter un bouton pour revenir à la page principale
        Button backButton = new Button("Retour");
        backButton.setStyle("-fx-background-color: #FFC107; -fx-text-fill: white;");
        backButton.setOnAction(e -> reviewStage.close());
    
    }
    
    private void loadUsers() {
        new Thread(() -> {
            List<User> users;
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                users = session.createQuery("FROM User", User.class).list();
            }

            javafx.application.Platform.runLater(() -> {
                userListContainer.getChildren().clear();
                for (User user : users) {
                    userListContainer.getChildren().add(createUserCard(user));
                }
            });
        }).start();
    }

    private HBox createUserCard(User user) {
        HBox userCard = new HBox(10);
        userCard.setPadding(new Insets(10));
        userCard.setStyle("-fx-border-color: black; -fx-border-radius: 5; -fx-border-width: 1;");
        userCard.setAlignment(Pos.CENTER_LEFT);
    
        Label userInfo = new Label(user.getNom() + " " + user.getPrenom() + " - " + user.getEmail() + " - Statut: " + user.getRole());
    
        Button editButton = new Button("\u270F Modifier");
        editButton.setOnAction(e -> showEditUserForm(user));
    
        Button deleteButton = new Button("🗑️ Supprimer");
        deleteButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");
        deleteButton.setOnAction(e -> confirmDeleteUser(user));
    
        userCard.getChildren().addAll(userInfo, editButton, deleteButton);
        return userCard;
    }

    private void showEditUserForm(User user) {
        Stage formStage = new Stage();
        VBox form = new VBox(10);
        form.setPadding(new Insets(20));
    
        TextField nomField = new TextField(user.getNom());
        TextField prenomField = new TextField(user.getPrenom());
        TextField emailField = new TextField(user.getEmail());
        TextField telephoneField = new TextField(user.getTelephone());
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Laissez vide pour ne pas changer");
    
        ComboBox<User.Role> statusComboBox = new ComboBox<>();
        statusComboBox.getItems().setAll(User.Role.values());
        statusComboBox.setValue(user.getRole());

        Label statusLabel = new Label("Statut :");

        Button saveButton = new Button("💾 Enregistrer");
        saveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        saveButton.setOnAction(e -> {
            user.setNom(nomField.getText());
            user.setPrenom(prenomField.getText());
            user.setEmail(emailField.getText());
            user.setTelephone(telephoneField.getText());
            if (!passwordField.getText().isEmpty()) {
                user.setMotDePasse(passwordField.getText());
            }
            user.setRole(statusComboBox.getValue());
            updateUser(user);
            formStage.close();
        });
    
        form.getChildren().addAll(
                new Label("Nom :"), nomField,
                new Label("Prénom :"), prenomField,
                new Label("Email :"), emailField,
                new Label("Téléphone :"), telephoneField,
                new Label("Mot de passe :"), passwordField,
                statusLabel, statusComboBox,
                saveButton
        );
    
        formStage.setScene(new Scene(form, 300, 300));
        formStage.show();
    }

    private void confirmDeleteUser(User user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de Suppression");
        alert.setHeaderText(null);
        alert.setContentText("Êtes-vous sûr de vouloir supprimer " + user.getNom() + " ?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                deleteUser(user);
            }
        });
    }

    private void updateUser(User user) {
        new Thread(() -> {
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                session.beginTransaction();
                session.update(user);
                session.getTransaction().commit();
            }
            javafx.application.Platform.runLater(this::loadUsers);
        }).start();
    }

    private void deleteUser(User user) {
        new Thread(() -> {
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                session.beginTransaction();
                session.delete(user);
                session.getTransaction().commit();
            }
            javafx.application.Platform.runLater(this::loadUsers);
        }).start();
    }

    private void showAddUserForm() {
        Stage formStage = new Stage();
        VBox form = new VBox(10);
        form.setPadding(new Insets(20));
    
        TextField nameField = new TextField();
        nameField.setPromptText("Nom");
        
        TextField prenomField = new TextField();
        prenomField.setPromptText("Prénom");
        
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        
        TextField telephoneField = new TextField();
        telephoneField.setPromptText("Téléphone");
        
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Mot de passe");
        
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
    
        Button saveButton = new Button("Enregistrer");
        saveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        saveButton.setOnAction(e -> {
            if (nameField.getText().isEmpty() || prenomField.getText().isEmpty() ||
                emailField.getText().isEmpty() || telephoneField.getText().isEmpty() ||
                passwordField.getText().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Tous les champs doivent être remplis.");
                return;
            }
    
            User newUser = new User();
            newUser.setNom(nameField.getText());
            newUser.setPrenom(prenomField.getText());
            newUser.setEmail(emailField.getText());
            newUser.setTelephone(telephoneField.getText());
            newUser.setMotDePasse(passwordField.getText());
            newUser.setRole(User.Role.USER);
    
            addUser(newUser);
            formStage.close();
        });
    
        VBox formBox = new VBox(10,
                new Label("Nom:"), nameField,
                new Label("Prénom:"), prenomField,
                new Label("Email:"), emailField,
                new Label("Téléphone:"), telephoneField,
                new Label("Mot de passe:"), passwordField, passwordTextField, showPassword,
                saveButton
        );
        formBox.setAlignment(Pos.CENTER);
        ScrollPane scrollPane = new ScrollPane(formBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(300);

        formStage.setScene(new Scene(scrollPane, 300, 300));
        formStage.setScene(new Scene(formBox, 300, 350));
        formStage.show();
    }

    private void addUser(User user) {
        new Thread(() -> {
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                session.beginTransaction();
                session.save(user);
                session.getTransaction().commit();
            }
            javafx.application.Platform.runLater(this::loadUsers);
        }).start();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}