package fr.parisdauphine.panel;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class ChoicePanel extends VBox {
    private final MainFrame mainFrame;

    public ChoicePanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        initializeUI();
    }

    private void initializeUI() {
        setAlignment(Pos.CENTER); // Centre les éléments dans le panneau principal
        setSpacing(20);           // Espacement vertical entre les composants
        setPadding(new Insets(20)); // Marges autour du panneau principal

        // Titre
        Label titleLabel = new Label("Bienvenue sur URCAR");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;"); // Style du titre
        getChildren().add(titleLabel);

        // Conteneur pour les boutons (la carte)
        VBox card = new VBox(10); // Conteneur compact avec espacement entre les boutons
        card.setPadding(new Insets(10)); // Padding interne pour une marge minimale autour des boutons
        card.setAlignment(Pos.CENTER); // Centrer les boutons dans la carte
        card.setStyle("-fx-background-color: white; -fx-border-radius: 8; -fx-background-radius: 8; "
                + "-fx-border-color: lightgray; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 5, 0, 0, 1);");

        // Réduire explicitement la largeur de la carte
        card.setPrefWidth(50); // Définit une largeur préférée pour la carte

        // Bouton "Se connecter"
        Button connexionButton = new Button("Se connecter");
        connexionButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        connexionButton.setPrefWidth(150); // Largeur fixe pour les boutons

        connexionButton.setOnAction(e -> mainFrame.navigateTo("Connexion"));

        // Bouton "S'inscrire"
        Button inscriptionButton = new Button("S'inscrire");
        inscriptionButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");
        inscriptionButton.setPrefWidth(150); // Largeur fixe pour les boutons

        inscriptionButton.setOnAction(e -> mainFrame.navigateTo("Inscription"));

        // Ajouter les boutons dans la carte
        card.getChildren().addAll(connexionButton, inscriptionButton);

        // Ajouter la carte au panneau principal
        getChildren().add(card);
    }
}
