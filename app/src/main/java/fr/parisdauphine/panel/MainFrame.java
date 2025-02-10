package fr.parisdauphine.panel;

import java.util.*;

import fr.parisdauphine.service.CarService;
import fr.parisdauphine.service.UserService;
import org.hibernate.Session;
import fr.parisdauphine.config.HibernateUtil;
import fr.parisdauphine.entity.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class MainFrame extends Stage {
    private User currentUser;
    private Label panierBadge;
    private final Map<String, VBox> panels = new HashMap<>();
    private final BorderPane rootLayout = new BorderPane();
    private String activeTab = "Choix";
    private final UserService userService;
    private final CarService carService;

    private HBox navBar;
    private Label userLabel;
    private Button logoutButton;

    public MainFrame(UserService userService, CarService carService) {
        this.userService = userService;
        this.carService = carService;
        this.currentUser = null;

        // Initialiser userLabel
        userLabel = new Label();
        userLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold;");
        userLabel.setText(""); // Texte par défaut vide

        // Bouton de déconnexion
        logoutButton = new Button("Se Déconnecter");
        logoutButton.setStyle("-fx-background-color: #c83232; -fx-text-fill: white;");
        logoutButton.setVisible(false); // Caché par défaut

        logoutButton.setOnAction(e -> {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Déconnexion");
            confirmAlert.setHeaderText(null);
            confirmAlert.setContentText("Êtes-vous sûr de vouloir vous déconnecter ?");
            confirmAlert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    logout();
                }
            });
        });

        initializeUI();
    }


    private void initializeUI() {
        setTitle("Ur Car - Application de Vente de Voitures");

        rootLayout.setTop(createHeader());
        rootLayout.setCenter(createContentPanel());
        rootLayout.setBottom(createFooter());

        // Par défaut, afficher l'écran de choix
        navigateTo("Choix");

        // Création de la scène
        Scene scene = new Scene(rootLayout, 900, 600);

        // Application des styles CSS
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

        setScene(scene);
        show();
    }

    public User getCurrentUser() {
        return currentUser;
    }

    private BorderPane createHeader() {
        BorderPane header = new BorderPane();
        header.setPadding(new Insets(10)); // Ajouter des marges internes
        header.setStyle("-fx-background-color: #1e1e1e;"); // Fond sombre

        // Barre de navigation (à gauche)
        navBar = new HBox();
        navBar.setSpacing(15);
        navBar.setPadding(new Insets(0, 10, 0, 10));
        navBar.setAlignment(Pos.CENTER_LEFT);

        // Logo et texte URCAR (au centre)
        HBox logoBox = new HBox();
        logoBox.setAlignment(Pos.CENTER); // Centrer verticalement
        logoBox.setSpacing(10);

        ImageView logoImage = null;
        try {
            Image logo = new Image(getClass().getResourceAsStream("/image/Logo.png"));
            logoImage = new ImageView(logo);
            logoImage.setFitHeight(30);
            logoImage.setPreserveRatio(true);
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image : " + e.getMessage());
        }

        Label logoLabel = new Label("URCAR");
        logoLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");

        if (logoImage != null) {
            logoBox.getChildren().addAll(logoImage, logoLabel);
        } else {
            logoBox.getChildren().add(logoLabel);
        }

        // Ajouter userLabel dans le header à droite
        HBox rightBox = new HBox();
        rightBox.setAlignment(Pos.CENTER_RIGHT);
        rightBox.setSpacing(10);
        rightBox.getChildren().add(userLabel);

        header.setLeft(navBar);
        header.setCenter(logoBox);
        header.setRight(rightBox);

        return header;
    }

    private void updateHeaderButtons() {
        navBar.getChildren().clear(); // Vider la barre de navigation

        if (currentUser != null) {
            // Onglets de base
            List<String> tabs = new ArrayList<>(Arrays.asList("Accueil", "Favoris", "Panier", "Avis"));

            // Création des boutons de navigation
            for (String tab : tabs) {
                Button button = createNavButton(tab);
                navBar.getChildren().add(button);
            }

            logoutButton.setVisible(true); // Afficher le bouton de déconnexion
        } else {
            logoutButton.setVisible(false); // Cacher si pas connecté
        }
    }


    private VBox createContentPanel() {
        VBox contentPanel = new VBox();
        contentPanel.setSpacing(10);

        // Ajouter des panneaux standards à la map
        panels.put("Choix", new ChoicePanel(this));
        panels.put("Connexion", new VBox(new LoginPanel(this,userService)));
        panels.put("Inscription", new VBox(new SignupPanel(this,userService)));
        panels.put("Accueil", new VBox());
        panels.put("Favoris", new VBox());
        panels.put("Panier", new VBox());
        panels.put("Avis", new ReviewPanel(this));
        panels.put("Compte", new VBox());
        panels.put("Mes Commandes", new VBox());
        panels.put("Resultats", new VBox());
        panels.put("GestionVoitures", new VBox(new CarAdminPanel(this)));

        return contentPanel;
    }


    private VBox createFooter() {
        VBox footer = new VBox(5); // Espacement réduit entre les éléments
        footer.setPadding(new Insets(10)); // Marges internes réduites
        footer.setStyle("-fx-background-color: #323232; -fx-alignment: center;"); // Couleur de fond et alignement

        // Labels avec style mis à jour
        Label phoneLabel = new Label("📞 +33 766349365");
        Label emailLabel = new Label("📧 contact@urcar.com");
        Label addressLabel = new Label("📍 Place du Maréchal de Lattre de Tassigny, 75116 Paris");

        // Appliquer un style compact à chaque label
        for (Label label : new Label[]{phoneLabel, emailLabel, addressLabel}) {
            label.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px;"); // Taille réduite et texte en gras
        }

        footer.getChildren().addAll(phoneLabel, emailLabel, addressLabel);
        footer.setAlignment(Pos.CENTER); // Alignement centré
        footer.setMaxHeight(50); // Limite la hauteur totale du footer
        return footer;
    }


    private void updateNavButtonStyle(Button button, String text) {
        if (activeTab.equals(text)) {
            button.setStyle("-fx-background-color: #6495ED; -fx-text-fill: white; -fx-font-weight: bold;"); // Couleur bleue pour l'onglet actif
        } else {
            button.setStyle("-fx-background-color: #323232; -fx-text-fill: white;"); // Couleur par défaut pour les onglets inactifs
        }
    }


    private Button createNavButton(String text) {
        Button button = new Button();

        // Ajoutez une classe CSS personnalisée et du texte correspondant
        if (text.equals("Accueil")) {
            button.getStyleClass().add("nav-button");
            button.getStyleClass().add("home");
            button.setText("🏠 Home");
        } else if (text.equals("Panier")) {
            button.getStyleClass().add("nav-button");
            button.getStyleClass().add("cart");
            button.setText("🛒");
        } else {
            button.getStyleClass().add("nav-button");
            button.setText(text);
        }

        updateNavButtonStyle(button, text); // Applique le style initial

        button.setOnAction(e -> {
            activeTab = text; // Met à jour l'onglet actif
            updateHeaderButtons(); // Met à jour les styles des boutons
            navigateTo(text); // Navigue vers l'onglet
        });

        return button;
    }

    public void navigateTo(String panelName) {
        VBox panel = panels.get(panelName);

        if ("Favoris".equals(panelName)) {
            // ✅ Vérifie la connexion avant de charger les favoris
            panel.getChildren().clear();  // Vide l'ancien contenu
            panel.getChildren().add(new FavoritePanel(this));  // ✅ Charge les favoris dynamiquement
        }

        if ("Panier".equals(panelName)) {
            // ✅ Vérifie la connexion avant de charger les favoris
            panel.getChildren().clear();  // Vide l'ancien contenu
            panel.getChildren().add(new CartPanel(this));  // ✅ Charge les paniers dynamiquement
        }

        if ("Compte".equals(panelName)) {
            // ✅ Vérifie la connexion avant de charger les favoris
            panel.getChildren().clear();  // Vide l'ancien contenu
            panel.getChildren().add(new AccountPanel(this));  // ✅ Charge les paniers dynamiquement
        }

        if ("Mes Commandes".equals(panelName)) {
            // ✅ Vérifie la connexion avant de charger les favoris
            panel.getChildren().clear();  // Vide l'ancien contenu
            panel.getChildren().add(new MyOrdersPanel(this));  // ✅ Charge les paniers dynamiquement
        }

        if ("Accueil".equals(panelName)) {
            // ✅ Vérifie la connexion avant de charger les favoris
            panel.getChildren().clear();  // Vide l'ancien contenu
            panel.getChildren().add(new HomePanel(this,carService));  // ✅ Charge les paniers dynamiquement
        }

        if ("Commandes".equals(panelName)) {
            panel.getChildren().clear();
            panel.getChildren().add(new MyOrdersPanel(this));
        }

        if (panel != null) {
            rootLayout.setCenter(panel);
            activeTab = panelName; // Met à jour l'onglet actif
            updateHeaderForActivePanel(); // Met à jour le header dynamiquement
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Panneau non trouvé : " + panelName);
            alert.showAndWait();
        }
    }

    private void updateHeaderForActivePanel() {
        BorderPane header = new BorderPane();
        header.setPadding(new Insets(10));
        header.setStyle("-fx-background-color: #1e1e1e;");

        // Logo et texte URCAR (toujours centré)
        HBox logoBox = new HBox();
        logoBox.setAlignment(Pos.CENTER); // Centrer verticalement et horizontalement
        logoBox.setSpacing(10);

        ImageView logoImage = null;
        try {
            Image logo = new Image(getClass().getResourceAsStream("/image/Logo.png"));
            logoImage = new ImageView(logo);
            logoImage.setFitHeight(30);
            logoImage.setPreserveRatio(true);
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image : " + e.getMessage());
        }

        Label logoLabel = new Label("URCAR");
        logoLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");

        if (logoImage != null) {
            logoBox.getChildren().addAll(logoImage, logoLabel);
        } else {
            logoBox.getChildren().add(logoLabel);
        }

        header.setCenter(logoBox); // Le logo est toujours centré

        // Si on est sur les panneaux "Choix", "Connexion" ou "Inscription"
        if ("Choix".equals(activeTab) || "Connexion".equals(activeTab) || "Inscription".equals(activeTab)) {
            header.setLeft(null);
            header.setRight(null);
        } else {
            // Barre de navigation (à gauche)
            navBar = new HBox();
            navBar.setSpacing(15);
            navBar.setPadding(new Insets(0, 10, 0, 10));
            navBar.setAlignment(Pos.CENTER_LEFT);

            String[] tabs = {"Accueil", "Favoris", "Avis"}; // Onglets pour navBar
            for (String tab : tabs) {
                Button button = createNavButton(tab);
                button.getStyleClass().add("nav-button");
                navBar.getChildren().add(button);
            }
            header.setLeft(navBar); // Ajouter les boutons de navigation à gauche

            // Menu Burger, "Bonjour", bouton Panier (à droite)
            HBox rightBox = new HBox(10); // Espacement entre les éléments
            rightBox.setAlignment(Pos.CENTER_RIGHT); // Aligner à droite

            userLabel.setText("Bonjour " + (currentUser != null ? currentUser.getPrenom() : "") + " !");
            userLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold;");

            // Bouton Panier
            Button panierButton = new Button("🛒 Panier");
            panierBadge = new Label("0"); // Initialisation du badge
            panierBadge.setStyle("-fx-background-color: red; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 2 6; -fx-background-radius: 10;");
            panierBadge.setVisible(false); // Caché si le panier est vide

            StackPane panierButtonWithBadge = new StackPane(panierButton, panierBadge);
            StackPane.setAlignment(panierBadge, Pos.TOP_RIGHT);

            panierButton.setOnAction(e -> navigateTo("Panier"));
            
            // Menu Burger
            MenuButton menuBurger = new MenuButton("☰");
            menuBurger.getStyleClass().add("menu-burger");

            MenuItem profileMenuItem = new MenuItem("Compte (Profil)");
            profileMenuItem.setOnAction(e -> navigateTo("Compte"));

            MenuItem orderMenuItem = new MenuItem("Mes Commandes");
            orderMenuItem.setOnAction(e -> navigateTo("Mes Commandes"));

            // ✅ MenuItem pour accéder à la section Admin (visible uniquement si l'utilisateur est ADMIN)
            MenuItem adminMenuItem = new MenuItem("👑 Espace Admin");
            if (currentUser != null && currentUser.getRole() == User.Role.ADMIN) {
                adminMenuItem.setOnAction(e -> navigateTo("Admin"));

                // ✅ Ajoute le panneau Admin s'il n'existe pas déjà
                if (!panels.containsKey("Admin")) {
                    panels.put("Admin", new VBox(new AdminPanel(this)));
                }

                // ✅ Ajouter l'élément Admin uniquement si l'utilisateur est ADMIN
                menuBurger.getItems().addAll(profileMenuItem,orderMenuItem ,adminMenuItem);
            } else {
                // ✅ Si l'utilisateur n'est PAS un admin, ne pas afficher l'élément Admin
                menuBurger.getItems().addAll(profileMenuItem,orderMenuItem);
            }

            MenuItem logoutMenuItem = new MenuItem("Se Déconnecter");
            logoutMenuItem.setOnAction(e -> {
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("Déconnexion");
                confirmAlert.setHeaderText(null);
                confirmAlert.setContentText("Êtes-vous sûr de vouloir vous déconnecter ?");
                confirmAlert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        logout();
                    }
                });
            });
            // ✅ Ajouter l'élément de déconnexion à la fin, dans tous les cas
            menuBurger.getItems().add(logoutMenuItem);

            // ✅ Ajouter les éléments à la section droite
            rightBox.getChildren().addAll(userLabel, panierButtonWithBadge, menuBurger);
            header.setRight(rightBox); // Ajouter le contenu à droite
        }
        rootLayout.setTop(header); // Mettre à jour le header
    }

    public void updatePanierBadge(int itemCount) {
        if (panierBadge != null) {
            panierBadge.setText(String.valueOf(itemCount));
            panierBadge.setVisible(itemCount > 0); // Afficher uniquement si le panier n'est pas vide
        }
    }

    public void setCurrentUser(User user) {
        if (user != null) {
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                User fullUser = session.get(User.class, user.getId());
                this.currentUser = fullUser;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            this.currentUser = null;
        }

        updateUserLabel();        // Met à jour le texte de userLabel
        updateHeaderButtons();    // Met à jour les boutons de navigation
        navigateTo("Accueil");    // Redirige vers Accueil par défaut

        //testRole();
    }

    private void updateUserLabel() {
        if (userLabel != null) {
            if (currentUser != null) {
                userLabel.setText("Bonjour " + currentUser.getPrenom() + " !");
            } else {
                userLabel.setText("");
            }
        }
    }

    private void logout() {
        currentUser = null; // Réinitialiser l'utilisateur actuel
        userLabel.setText(""); // Effacer le message de bienvenue
        activeTab = "Choix"; // Réinitialiser l'onglet actif
        logoutButton.setVisible(false); // Cacher le bouton "Se Déconnecter"
        updateHeaderButtons(); // Mettre à jour les boutons du header
        enableLoginPanels(); // Réactiver les panneaux "Connexion" et "Inscription"
        navigateTo("Choix"); // Naviguer vers l'écran de choix
    }

    private void enableLoginPanels() {
        panels.put("Connexion", new VBox(new LoginPanel(this,userService)));
        panels.put("Inscription",new VBox(new SignupPanel(this,userService)));
    }

    public Map<String, VBox> getPanels() {
        return panels;
    }

}
