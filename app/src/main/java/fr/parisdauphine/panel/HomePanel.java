package fr.parisdauphine.panel;

import fr.parisdauphine.entity.Car;
import fr.parisdauphine.config.HibernateUtil;
import fr.parisdauphine.service.CarService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.hibernate.Session;

import java.util.*;
import java.util.stream.Collectors;


public class HomePanel extends VBox {

    private List<CheckBox> brandCheckBoxes = new ArrayList<>(); // Checkboxes pour les marques
    private List<CheckBox> modelCheckBoxes = new ArrayList<>(); // Checkboxes pour les modèles
    private final MainFrame mainFrame;
    private final CarService carService;// Ajout de mainFrame

    public HomePanel(MainFrame mainFrame, CarService carService) {
        this.mainFrame = mainFrame;
        this.carService = new CarService();
        initializeUI();
    }

    private void initializeUI() {
        setPadding(new Insets(20));
        setSpacing(20);
        setAlignment(Pos.CENTER);

        // Description
        Label descriptionLabel = new Label(
                "URCAR est une entreprise spécialisée dans la vente de voitures.\n"
                        + "Découvrez nos offres exclusives et trouvez la voiture de vos rêves !");
        descriptionLabel.setStyle("-fx-font-style: italic; -fx-font-size: 14px; -fx-text-alignment: center;");
        descriptionLabel.setWrapText(true);

        // Formulaire de recherche
        VBox searchBox = new VBox(10);
        searchBox.setPadding(new Insets(15));
        searchBox.setStyle("-fx-border-color: black; -fx-border-radius: 5; -fx-border-width: 1;");
        searchBox.setPrefWidth(300);

        Label searchTitle = new Label("Rechercher une voiture");
        searchTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        Label brandLabel = new Label("Marques (les plus recherchées) :");
        TextField searchBrandField = new TextField();
        searchBrandField.setPromptText("Rechercher une marque");

        ScrollPane brandScrollPane = new ScrollPane();
        VBox brandContainer = new VBox(5); // Conteneur des CheckBoxes pour les marques
        brandScrollPane.setContent(brandContainer);
        brandScrollPane.setFitToWidth(true);
        brandScrollPane.setPrefHeight(150);

        Label modelLabel = new Label("Modèles (les plus recherchés) :");
        TextField searchModelField = new TextField();
        searchModelField.setPromptText("Rechercher un modèle");

        ScrollPane modelScrollPane = new ScrollPane();
        VBox modelContainer = new VBox(5); // Conteneur des CheckBoxes pour les modèles
        modelScrollPane.setContent(modelContainer);
        modelScrollPane.setFitToWidth(true);
        modelScrollPane.setPrefHeight(150);

        Label priceLabel = new Label("Prix (€) :");
        HBox priceFields = new HBox(10);
        TextField minPriceField = new TextField();
        minPriceField.setPromptText("Min (facultatif)");
        TextField maxPriceField = new TextField();
        maxPriceField.setPromptText("Max (facultatif)");
        priceFields.getChildren().addAll(minPriceField, maxPriceField);

        Button searchButton = new Button("Rechercher");
        searchButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");

        // Charger les données depuis la base de données
        Map<String, List<String>> carData = fetchCarData();
        if (carData.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Aucune donnée de voiture trouvée dans la base de données.");
        } else {
            // Ajouter les marques dans le conteneur de CheckBoxes
            carData.keySet().forEach(brand -> {
                CheckBox brandCheckBox = new CheckBox(brand);
                brandCheckBoxes.add(brandCheckBox);
                brandContainer.getChildren().add(brandCheckBox);
            });

            // Filtrer les marques en fonction de la recherche
            searchBrandField.textProperty().addListener((observable, oldValue, newValue) -> {
                String filter = newValue.toLowerCase();
                brandContainer.getChildren().clear();
                brandCheckBoxes.stream()
                        .filter(cb -> cb.getText().toLowerCase().contains(filter))
                        .forEach(brandContainer.getChildren()::add);
            });

            // Ajouter les modèles associés aux marques sélectionnées
            brandCheckBoxes.forEach(brandCheckBox -> brandCheckBox.setOnAction(e -> {
                List<String> selectedBrands = brandCheckBoxes.stream()
                        .filter(CheckBox::isSelected)
                        .map(CheckBox::getText)
                        .collect(Collectors.toList());

                modelCheckBoxes.clear();
                modelContainer.getChildren().clear();
                if (!selectedBrands.isEmpty()) {
                    selectedBrands.stream()
                            .flatMap(brand -> carData.getOrDefault(brand, Collections.emptyList()).stream())
                            .distinct()
                            .forEach(model -> {
                                CheckBox modelCheckBox = new CheckBox(model);
                                modelCheckBoxes.add(modelCheckBox);
                                modelContainer.getChildren().add(modelCheckBox);
                            });
                }
            }));

            // Filtrer les modèles en fonction de la recherche
            searchModelField.textProperty().addListener((observable, oldValue, newValue) -> {
                String filter = newValue.toLowerCase();
                modelContainer.getChildren().clear();
                modelCheckBoxes.stream()
                        .filter(cb -> cb.getText().toLowerCase().contains(filter))
                        .forEach(modelContainer.getChildren()::add);
            });
        }

        searchButton.setOnAction(e -> {
            List<String> selectedBrands = brandCheckBoxes.stream()
                    .filter(CheckBox::isSelected)
                    .map(CheckBox::getText)
                    .collect(Collectors.toList());

            List<String> selectedModels = modelCheckBoxes.stream()
                    .filter(CheckBox::isSelected)
                    .map(CheckBox::getText)
                    .collect(Collectors.toList());

            String minPriceText = minPriceField.getText();
            String maxPriceText = maxPriceField.getText();

            try {
                // Fetch all cars if no filters are applied
                List<Car> results = carService.getAvailableCars();

                // Filter results based on user selections
                if (!selectedBrands.isEmpty()) {
                    results = results.stream().filter(car -> selectedBrands.contains(car.getBrand())).collect(Collectors.toList());
                }
                if (!selectedModels.isEmpty()) {
                    results = results.stream().filter(car -> selectedModels.contains(car.getModel())).collect(Collectors.toList());
                }
                if (!minPriceText.isEmpty()) {
                    int minPrice = Integer.parseInt(minPriceText);
                    results = results.stream().filter(car -> car.getPrice() >= minPrice).collect(Collectors.toList());
                }
                if (!maxPriceText.isEmpty()) {
                    int maxPrice = Integer.parseInt(maxPriceText);
                    results = results.stream().filter(car -> car.getPrice() <= maxPrice).collect(Collectors.toList());
                }

                // Create the results panel and navigate to it
                ResultatsPanel resultatsPanel = new ResultatsPanel(results, mainFrame);
                mainFrame.getPanels().put("Resultats", resultatsPanel);
                mainFrame.navigateTo("Resultats");

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });


        searchBox.getChildren().addAll(searchTitle, searchBrandField, brandLabel, brandScrollPane,
                modelLabel, searchModelField, modelScrollPane, priceLabel, priceFields, searchButton);

        // Image de voiture
        ImageView carImageView = new ImageView();
        try {
            Image carImage = new Image(getClass().getResource("/image/Accueil.png").toExternalForm());
            carImageView.setImage(carImage);
            carImageView.setFitWidth(300);
            carImageView.setFitHeight(150);
            carImageView.setPreserveRatio(true);
        } catch (Exception e) {
            carImageView.setFitWidth(300);
            carImageView.setFitHeight(150);
            carImageView.setPreserveRatio(true);
            carImageView.setStyle("-fx-border-color: gray; -fx-border-style: dashed;");
        }

        // Layout principal
        HBox mainContent = new HBox(20, searchBox, carImageView);
        mainContent.setPadding(new Insets(20));
        mainContent.setAlignment(Pos.CENTER);

        // Ajouter les composants à la VBox principale
        getChildren().addAll(descriptionLabel, mainContent);
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private Map<String, List<String>> fetchCarData() {
        Map<String, List<String>> carData = new HashMap<>();

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Filtrer les voitures ayant le statut EN_VENTE
            List<Car> cars = session.createQuery("FROM Car c WHERE c.status = :status", Car.class)
                    .setParameter("status", Car.Status.EN_VENTE)  // Filtrer par le statut EN_VENTE
                    .list();

            // Grouper par marque et modèle
            carData = cars.stream().collect(Collectors.groupingBy(
                    Car::getBrand, // Grouper par marque
                    Collectors.mapping(Car::getModel, Collectors.toList()) // Extraire les modèles
            ));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return carData;
    }

}