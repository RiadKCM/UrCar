package fr.parisdauphine.panel;

import fr.parisdauphine.config.HibernateUtil;
import fr.parisdauphine.entity.Car;
import fr.parisdauphine.entity.Image;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.hibernate.Session;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CarAdminPanel extends VBox {
    private final MainFrame mainFrame;
    private TextField searchField;
    private VBox carListContainer;

    public CarAdminPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        initializeUI();
    }

    private void initializeUI() {
        setPadding(new Insets(20));
        setSpacing(10);
        setAlignment(Pos.TOP_CENTER);
    
        Label titleLabel = new Label("🚗 Gestion des Voitures");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
    
        Button addCarButton = new Button("➕ Ajouter une Voiture");
        addCarButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        addCarButton.setOnAction(e -> showAddCarForm());
    
        searchField = new TextField();
        searchField.setPromptText("Rechercher par marque ou modèle...");
        searchField.textProperty().addListener((observable, oldValue, newValue) -> searchCars(newValue));
    
        carListContainer = new VBox(10);
    
        // ✅ Ajout du ScrollPane pour la liste des voitures
        ScrollPane scrollPane = new ScrollPane(carListContainer);
        scrollPane.setFitToWidth(true);  // Pour que le contenu prenne toute la largeur disponible
        scrollPane.setPrefHeight(400);   // Hauteur maximale du conteneur avant de scroller
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS); // Barre de défilement verticale toujours visible (ou automatique si nécessaire)
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);  // Désactive la barre de défilement horizontale (si pas nécessaire)
    
        loadCars();
    
        getChildren().addAll(titleLabel, addCarButton, searchField, scrollPane);
    } 

    private void loadCars() {
        new Thread(() -> {
            List<Car> cars;
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                cars = session.createQuery("FROM Car", Car.class).list();
            }

            javafx.application.Platform.runLater(() -> {
                carListContainer.getChildren().clear();
                for (Car car : cars) {
                    carListContainer.getChildren().add(createCarCard(car));
                }
            });
        }).start();
    }


    private HBox createCarCard(Car car) {
        HBox carCard = new HBox(10);
        carCard.setPadding(new Insets(10));
        carCard.setStyle("-fx-border-color: black; -fx-border-radius: 5; -fx-border-width: 1;");
        carCard.setAlignment(Pos.CENTER_LEFT);

        // Image logic remains unchanged
        ImageView carImageView = new ImageView();
        if (!car.getImages().isEmpty()) {
            String imagePath = "src/main/resources" + car.getImages().get(0).getImagePath();
            File imageFile = new File(imagePath);

            carImageView.setImage(new javafx.scene.image.Image(imageFile.toURI().toString()));
            carImageView.setFitHeight(80);
            carImageView.setFitWidth(120);
            carImageView.setPreserveRatio(true);
            carImageView.setOnMouseClicked(e -> showImageCarousel(car));
        }

        // Info text with status
        Label carInfo = new Label(car.getBrand() + " " + car.getModel() + " - " + car.getPrice() + " €" + car.getdescription());

        // Display car status
        String statusText = (car.getStatus() == Car.Status.EN_VENTE) ? "EN_VENTE" : "Vendu";
        Label carStatus = new Label(statusText);
        carStatus.setStyle("-fx-font-style: italic; -fx-text-fill: gray;");

        Button editButton = new Button("✏️ Modifier");
        editButton.setOnAction(e -> showEditForm(car));

        Button deleteButton = new Button("🗑️ Supprimer");
        deleteButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");
        deleteButton.setOnAction(e -> confirmDeleteCar(car));

        // Layout for the car card
        HBox.setHgrow(carInfo, Priority.ALWAYS);
        carCard.getChildren().addAll(carImageView, carInfo, carStatus, editButton, deleteButton);
        return carCard;
    }

    private void showEditForm(Car car) {
        Stage formStage = new Stage();
        VBox form = new VBox(10);
        form.setPadding(new Insets(20));

        TextField brandField = new TextField(car.getBrand());
        TextField modelField = new TextField(car.getModel());
        TextField priceField = new TextField(car.getPrice().toString());
        TextArea descriptionField = new TextArea(car.getdescription());

        // Image container to display existing images
        VBox imageContainer = new VBox(5);
        for (Image image : car.getImages()) {
            HBox imageBox = new HBox(10);
            javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView(new javafx.scene.image.Image("file:" + image.getImagePath()));
            imageView.setFitWidth(100);
            imageView.setFitHeight(80);
            imageView.setPreserveRatio(true);

            Button deleteImageButton = new Button("🗑️ Supprimer");
            deleteImageButton.setOnAction(e -> {
                car.removeImage(image);  // Remove the image from the car
                imageContainer.getChildren().remove(imageBox); // Remove from UI
            });

            imageBox.getChildren().addAll(imageView, deleteImageButton);
            imageContainer.getChildren().add(imageBox);
        }

        // Allow new images to be added
        List<File> newImages = new ArrayList<>();
        Button addImageButton = new Button("📸 Ajouter des Images");
        addImageButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choisir des images");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
            List<File> selectedFiles = fileChooser.showOpenMultipleDialog(formStage);
            if (selectedFiles != null) {
                newImages.addAll(selectedFiles);
                for (File file : selectedFiles) {
                    javafx.scene.image.ImageView newImageView = new javafx.scene.image.ImageView(new javafx.scene.image.Image(file.toURI().toString()));
                    newImageView.setFitWidth(100);
                    newImageView.setFitHeight(80);
                    newImageView.setPreserveRatio(true);
                    imageContainer.getChildren().add(newImageView);
                }
            }
        });

        // Save the updated car
        Button saveButton = new Button("💾 Enregistrer");
        saveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        saveButton.setOnAction(e -> {
            car.setBrand(brandField.getText());
            car.setModel(modelField.getText());
            car.setPrice(Double.parseDouble(priceField.getText()));
            car.setdescription(descriptionField.getText());

            // Add new images
            for (File file : newImages) {
                car.addImage(new Image(file.getAbsolutePath(), car)); // Save the image paths
            }

            updateCar(car, newImages);  // Update car info and images in DB
            formStage.close();
        });

        form.getChildren().addAll(
                new Label("Marque :"), brandField,
                new Label("Modèle :"), modelField,
                new Label("Prix (€) :"), priceField,
                new Label("Description :"), descriptionField,
                new Label("Images actuelles :"), imageContainer,
                addImageButton,
                saveButton
        );

        formStage.setScene(new Scene(form, 400, 500));
        formStage.show();
    }


    private void updateCar(Car car, List<File> newImages) {
        new Thread(() -> {
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                session.beginTransaction();
                session.update(car);
                session.getTransaction().commit();
            }

            // ✅ Sauvegarder les nouvelles images
            saveCarImages(car, newImages);

            javafx.application.Platform.runLater(this::loadCars);
        }).start();
    }

    private void saveCarImages(Car car, List<File> images) {
        if (images.isEmpty()) return;

        String imagesDir = "src/main/resources/image/voitures/";
        new File(imagesDir).mkdirs(); // ✅ Crée le dossier s'il n'existe pas

        int imageIndex = 0; // Pour la numérotation des images

        for (File imageFile : images) {
            try {
                String extension = imageFile.getName().substring(imageFile.getName().lastIndexOf('.') + 1);
                String newFileName = (imageIndex == 0)
                        ? "image" + car.getId() + "." + extension // ✅ Première image
                        : "image" + car.getId() + "_" + imageIndex + "." + extension; // ✅ Images suivantes

                File destFile = new File(imagesDir + newFileName);
                java.nio.file.Files.copy(imageFile.toPath(), destFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);

                // ✅ Enregistrer l'image dans la base de données
                try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                    session.beginTransaction();
                    Image image = new Image("/image/voitures/" + newFileName, car); // ✅ Chemin relatif pour l'affichage
                    session.save(image);
                    session.getTransaction().commit();
                }

                car.addImage(new Image("/image/voitures/" + newFileName, car)); // ✅ Ajouter l'image à la voiture
                imageIndex++; // Incrémenter l'index pour la prochaine image
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void showAddCarForm() {
        Stage formStage = new Stage();
        VBox form = new VBox(10);
        form.setPadding(new Insets(20));

        TextField brandField = new TextField();
        brandField.setPromptText("Marque");

        TextField modelField = new TextField();
        modelField.setPromptText("Modèle");

        TextField priceField = new TextField();
        priceField.setPromptText("Prix (€)");

        TextArea descriptionField = new TextArea();
        descriptionField.setPromptText("Entrez la description de la voiture...");

        List<File> selectedImages = new ArrayList<>();
        Button uploadImageButton = new Button("📸 Ajouter des Images");
        uploadImageButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choisir des images");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
            selectedImages.addAll(fileChooser.showOpenMultipleDialog(formStage));
        });

        Button saveButton = new Button("Enregistrer");
        saveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        saveButton.setOnAction(e -> {
            if (brandField.getText().isEmpty() || modelField.getText().isEmpty() || priceField.getText().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Tous les champs doivent être remplis.");
                return;
            }

            Car newCar = new Car();
            newCar.setBrand(brandField.getText());
            newCar.setModel(modelField.getText());
            newCar.setPrice(Double.parseDouble(priceField.getText()));
            newCar.setdescription(descriptionField.getText());
            newCar.setStatus(Car.Status.EN_VENTE);

            addCar(newCar, selectedImages); // ✅ On passe les images sélectionnées
            formStage.close();
        });


        form.getChildren().addAll(new Label("Marque:"), brandField, new Label("Modèle:"), modelField,
                new Label("Prix (€):"), priceField,new Label("Description:"), descriptionField, uploadImageButton, saveButton);

        formStage.setScene(new Scene(form, 300, 350));
        formStage.show();
    }

    private void addCar(Car car, List<File> selectedImages) {
        new Thread(() -> {
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                session.beginTransaction();
                session.save(car); // On sauvegarde d'abord pour obtenir l'ID de la voiture
                session.getTransaction().commit();
            }

            // ✅ Enregistrer les images après avoir obtenu l'ID de la voiture
            saveCarImages(car, selectedImages);

            // Rafraîchir l'interface utilisateur
            javafx.application.Platform.runLater(this::loadCars);
        }).start();
    }

    private void confirmDeleteCar(Car car) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de Suppression");
        alert.setHeaderText(null);
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cette voiture ?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                deleteCar(car);
            }
        });
    }

    private void deleteCar(Car car) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            session.delete(car);
            session.getTransaction().commit();
        }
        loadCars();
    }

    public void showImageCarousel(Car car) {
        Stage carouselStage = new Stage();
        VBox carousel = new VBox(10);
        carousel.setAlignment(Pos.CENTER);
        carousel.setPadding(new Insets(20));

        List<Image> images = car.getImages();
        if (images.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Aucune Image", "Cette voiture n'a pas d'images.");
            return;
        }

        ImageView imageView = new ImageView();
        imageView.setFitHeight(300);
        imageView.setPreserveRatio(true);

        loadImage(images.get(0).getImagePath(), imageView);

        // ✅ Encapsulation de l'image dans un ScrollPane
        ScrollPane imageScrollPane = new ScrollPane(imageView);
        imageScrollPane.setFitToWidth(true);
        imageScrollPane.setPrefHeight(350); // Taille maximale avant de scroller

        Button nextButton = new Button("⏭️ Suivant");
        Button prevButton = new Button("⏮️ Précédent");
        Button backButton = new Button("⬅️ Retour");

        backButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        backButton.setOnAction(e -> carouselStage.close());

        final int[] index = {0};

        nextButton.setOnAction(e -> {
            index[0] = (index[0] + 1) % images.size();
            loadImage(images.get(index[0]).getImagePath(), imageView);
        });

        prevButton.setOnAction(e -> {
            index[0] = (index[0] - 1 + images.size()) % images.size();
            loadImage(images.get(index[0]).getImagePath(), imageView);
        });

        HBox navButtons = new HBox(10, prevButton, nextButton);
        navButtons.setAlignment(Pos.CENTER);

        carousel.getChildren().addAll(backButton, imageScrollPane, navButtons);
        carouselStage.setScene(new Scene(carousel, 400, 500)); // ✅ Augmentation de la hauteur
        carouselStage.show();
    }


    private void loadImage(String imagePath, ImageView imageView) {
        try {
            // Vérifie si le chemin est absolu
            if (imagePath.startsWith("/")) {
                imagePath = imagePath.substring(1); // Supprime le "/" initial
            }

            // ✅ Tente de charger l'image depuis le dossier de ressources (build/resources/main)
            File file = new File("build/resources/main/" + imagePath);
            if (!file.exists()) {
                // Si l'image n'est pas dans build, vérifie dans src directement (utile en développement)
                file = new File("src/main/resources/" + imagePath);
            }

            if (file.exists()) {
                imageView.setImage(new javafx.scene.image.Image(file.toURI().toString()));
            } else {
                throw new IllegalArgumentException("Fichier introuvable : " + file.getAbsolutePath());
            }
        } catch (Exception e) {
            System.out.println("Erreur de chargement : " + imagePath);
            e.printStackTrace();
            imageView.setImage(null);
        }
    }



    private void searchCars(String query) {
        carListContainer.getChildren().clear();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Car> cars = session.createQuery("FROM Car WHERE (brand LIKE :query OR model LIKE :query) AND status = 'EN_VENTE'", Car.class)
                    .setParameter("query", "%" + query + "%")
                    .list();
            for (Car car : cars) {
                carListContainer.getChildren().add(createCarCard(car));
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
