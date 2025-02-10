package fr.parisdauphine.panel;

import fr.parisdauphine.config.HibernateUtil;
import fr.parisdauphine.entity.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.hibernate.Session;
import org.hibernate.query.Query;

public class AccountPanel extends BorderPane {
    private final MainFrame mainFrame;

    public AccountPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        initializeUI();
    }

    private void initializeUI() {
        // Récupérer l'utilisateur connecté
        User currentUser = getUserFromDatabase();

        // Vérifier si un utilisateur est connecté
        if (currentUser == null) {
            Label errorLabel = new Label("Aucun utilisateur connecté.");
            errorLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: red; -fx-font-weight: bold;");
            setCenter(errorLabel);
            BorderPane.setAlignment(errorLabel, Pos.CENTER);
            return;
        }

        // Titre
        Label titleLabel = new Label("Mon Compte");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        setTop(titleLabel);
        BorderPane.setAlignment(titleLabel, Pos.CENTER); // Aligner le titre au centre
        BorderPane.setMargin(titleLabel, new Insets(10, 0, 20, 0)); // Marges autour du titre

        // Conteneur principal
        VBox accountContainer = new VBox(20);
        accountContainer.setAlignment(Pos.CENTER);
        accountContainer.setPadding(new Insets(30));
        accountContainer.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: lightgray; -fx-border-radius: 8; -fx-background-radius: 8;");

        // Grille pour afficher les informations utilisateur
        GridPane userInfoGrid = new GridPane();
        userInfoGrid.setHgap(20);
        userInfoGrid.setVgap(10);
        userInfoGrid.setAlignment(Pos.CENTER);

        // Styles communs
        String labelStyle = "-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333;";
        String valueStyle = "-fx-font-size: 16px; -fx-text-fill: #555;";

        // Nom
        Label nameLabel = new Label("Nom :");
        nameLabel.setStyle(labelStyle);
        Label nameValue = new Label(currentUser.getNom());
        nameValue.setStyle(valueStyle);
        Button nameEditButton = createEditButton(nameValue, "Nom", currentUser);

        // Prénom
        Label prenomLabel = new Label("Prénom :");
        prenomLabel.setStyle(labelStyle);
        Label prenomValue = new Label(currentUser.getPrenom());
        prenomValue.setStyle(valueStyle);
        Button prenomEditButton = createEditButton(prenomValue, "Prénom", currentUser);

        // Email
        Label emailLabel = new Label("Adresse e-mail :");
        emailLabel.setStyle(labelStyle);
        Label emailValue = new Label(currentUser.getEmail());
        emailValue.setStyle(valueStyle);
        Button emailEditButton = createEditButton(emailValue, "Email", currentUser);

        // Téléphone
        Label phoneLabel = new Label("Numéro de téléphone :");
        phoneLabel.setStyle(labelStyle);
        Label phoneValue = new Label(currentUser.getTelephone());
        phoneValue.setStyle(valueStyle);
        Button phoneEditButton = createEditButton(phoneValue, "Téléphone", currentUser);

        // Ajouter les informations à la grille
        userInfoGrid.add(nameLabel, 0, 0);
        userInfoGrid.add(nameValue, 1, 0);
        userInfoGrid.add(nameEditButton, 2, 0);

        userInfoGrid.add(prenomLabel, 0, 1);
        userInfoGrid.add(prenomValue, 1, 1);
        userInfoGrid.add(prenomEditButton, 2, 1);

        userInfoGrid.add(emailLabel, 0, 2);
        userInfoGrid.add(emailValue, 1, 2);
        userInfoGrid.add(emailEditButton, 2, 2);

        userInfoGrid.add(phoneLabel, 0, 3);
        userInfoGrid.add(phoneValue, 1, 3);
        userInfoGrid.add(phoneEditButton, 2, 3);

        // Ajouter le conteneur principal au panneau
        accountContainer.getChildren().add(userInfoGrid);
        setCenter(accountContainer);
    }

    private User getUserFromDatabase() {
        // Récupérer l'utilisateur connecté depuis la session en cours
        User currentUser = mainFrame.getCurrentUser();

        if (currentUser == null) {
            return null; // Aucun utilisateur connecté
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Utilisation d'une requête HQL pour récupérer l'utilisateur par son email
            String hql = "FROM User u WHERE u.email = :email";
            Query<User> query = session.createQuery(hql, User.class);
            query.setParameter("email", currentUser.getEmail()); // Utilisation de l'email pour rechercher l'utilisateur
            User result = query.uniqueResult(); // Obtenir l'utilisateur unique basé sur l'email

            return result; // Retourner l'utilisateur récupéré
        } catch (Exception e) {
            e.printStackTrace();
            return null; // En cas d'erreur, retourner null
        }
    }

    private Button createEditButton(Label label, String field, User user) {
        Button editButton = new Button("Modifier");
        editButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        editButton.setOnAction(e -> openEditDialog(label, field, user));
        return editButton;
    }

    private void openEditDialog(Label label, String field, User user) {
        // Créer un TextField pour permettre la modification
        TextField editField = new TextField(label.getText());

        // Créer un dialog pour la modification
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Modifier " + field);
        dialog.setHeaderText("Modifier le " + field);

        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Ajouter le champ de texte au dialogue
        dialog.getDialogPane().setContent(editField);

        // Lorsque l'utilisateur clique sur "Enregistrer"
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String newValue = editField.getText();
                if (newValue != null && !newValue.trim().isEmpty()) {
                    label.setText(newValue); // Mise à jour de l'étiquette avec la nouvelle valeur
                    showConfirmationDialog(user, field, newValue);
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void showConfirmationDialog(User user, String field, String newValue) {
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Confirmation");
        confirmationAlert.setHeaderText("Êtes-vous sûr de vouloir enregistrer les modifications ?");
        confirmationAlert.setContentText("Le " + field + " sera mis à jour avec la valeur suivante : " + newValue);

        confirmationAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Mettre à jour la base de données
                updateUserFieldInDatabase(user, field, newValue);
            }
        });
    }

    private void updateUserFieldInDatabase(User user, String field, String newValue) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();

            // Mettre à jour le champ de l'utilisateur
            switch (field) {
                case "Nom":
                    user.setNom(newValue);
                    break;
                case "Prénom":
                    user.setPrenom(newValue);
                    break;
                case "Email":
                    user.setEmail(newValue);
                    break;
                case "Téléphone":
                    user.setTelephone(newValue);
                    break;
            }

            session.update(user); // Mettre à jour l'utilisateur dans la base de données
            session.getTransaction().commit();

            showAlert(Alert.AlertType.INFORMATION, "Mise à jour réussie", "Les informations de votre compte ont été mises à jour avec succès.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue lors de la mise à jour.");
        }
    }

    // Méthode pour afficher une alerte
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
