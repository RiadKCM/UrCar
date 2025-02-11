package fr.parisdauphine.panel;

import fr.parisdauphine.entity.Car;
import fr.parisdauphine.entity.Cart;
import fr.parisdauphine.entity.Invoice;
import fr.parisdauphine.entity.Order;
import fr.parisdauphine.entity.User;
import fr.parisdauphine.repository.InvoiceRepository;
import fr.parisdauphine.service.CartService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.util.List;

public class CartPanel extends VBox {
    private final ListView<String> cartListView = new ListView<>();
    private final MainFrame mainFrame;
    private final Label totalLabel = new Label(" 0€");
    private final Label carsCountLabel = new Label("Nombre de voitures: 0");
    private final CartService cartService;

    public CartPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.cartService = new CartService();
        initializeUI();
    }

    private void initializeUI() {
        setSpacing(20);
        setPadding(new Insets(10));
        setAlignment(Pos.CENTER);

        VBox cardContainer = new VBox(20);
        cardContainer.setPadding(new Insets(20));
        cardContainer.setStyle("-fx-border-color: lightgrey; -fx-border-width: 2px; -fx-border-radius: 10px; -fx-background-color: white;");
        cardContainer.setAlignment(Pos.CENTER);

        Text title = new Text("Votre Panier");
        title.setFont(Font.font("Arial", 24));
        cardContainer.getChildren().add(title);

        cartListView.setPlaceholder(new Label("Votre panier est vide."));
        cartListView.setStyle("-fx-font-size: 14px;");
        VBox leftColumn = new VBox(10, new Label("Voitures dans le panier:"), carsCountLabel, cartListView);
        leftColumn.setPadding(new Insets(10));
        leftColumn.setAlignment(Pos.CENTER);

        Button removeButton = new Button("❌ Supprimer l'article");
        removeButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");
        removeButton.setOnAction(e -> removeSelectedItem());
        leftColumn.getChildren().add(removeButton);

        VBox rightColumn = new VBox(10, createPaymentForm(), new HBox(10, new Label(""), totalLabel));
        rightColumn.setPadding(new Insets(10));
        rightColumn.setAlignment(Pos.CENTER);
        Button confirmButton = new Button("✔️ Confirmer l'achat");
        confirmButton.setStyle("-fx-background-color: green; -fx-text-fill: white;");
        confirmButton.setOnAction(e -> handlePurchase());
        rightColumn.getChildren().add(confirmButton);
        HBox mainContainer = new HBox(20, leftColumn, rightColumn);
        mainContainer.setAlignment(Pos.CENTER);
        cardContainer.getChildren().add(mainContainer);
        getChildren().add(cardContainer);
        loadCart();
    }

    private void loadCart() {
        cartListView.getItems().clear();
        List<Car> cars = cartService.getCarsInCart(mainFrame.getCurrentUser());
        for (Car car : cars) {
            cartListView.getItems().add(car.getBrand() + " " + car.getModel() + " - " + car.getPrice() + "€");
        }
        updateTotal();
        updateCarsCount(cars.size());
    }

    private void updateCarsCount(int count) {
        carsCountLabel.setText("Nombre de voitures: " + count);
    }

    private void generateInvoicePDF(Invoice invoice) {
        String fileName = "facture_" + invoice.getOrder().getUser().getNom() + ".pdf";
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
                contentStream.newLineAtOffset(50, 750);
                contentStream.showText("Facture #" + (invoice.getId() != null ? invoice.getId() : "N/A"));
                contentStream.newLineAtOffset(0, -30);
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.showText("Date : " + invoice.getInvoiceDate());
                contentStream.newLineAtOffset(0, -20);
                contentStream.showText("Client : " + invoice.getOrder().getUser().getNom() + " " + invoice.getOrder().getUser().getPrenom());
                contentStream.newLineAtOffset(0, -20);
                contentStream.showText("Email : " + invoice.getOrder().getUser().getEmail());
                contentStream.newLineAtOffset(0, -20);
                contentStream.showText("Total : " + invoice.getOrder().getCars().stream().mapToDouble(Car::getPrice).sum() + "€");
                contentStream.newLineAtOffset(0, -30);
    
                // Ajout des détails des voitures achetées
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
                contentStream.showText("Détails des voitures :");
                contentStream.newLineAtOffset(0, -20);
                contentStream.setFont(PDType1Font.HELVETICA, 12);
    
                if (invoice.getOrder().getCars() != null && !invoice.getOrder().getCars().isEmpty()) {
                    for (Car car : invoice.getOrder().getCars()) {
                        contentStream.showText("- " + car.getBrand() + " " + car.getModel() + " | " + car.getPrice() + "€");
                        contentStream.newLineAtOffset(0, -15);
                        contentStream.showText("  Description : " + car.getdescription());
                        contentStream.newLineAtOffset(0, -15);
                    }
                } else {
                    contentStream.showText("Aucune voiture enregistrée.");
                    contentStream.newLineAtOffset(0, -15);
                }
                contentStream.endText();
            }
            document.save(fileName);
            showAlert(Alert.AlertType.INFORMATION, "Facture générée", "La facture a été sauvegardée sous : " + fileName);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la génération de la facture.");
        }
    }    
    
    private void removeSelectedItem() {
        String selectedItem = cartListView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            String[] parts = selectedItem.split(" - ");
            if (parts.length > 1) {
                String carBrandModel = parts[0].trim();
                cartService.removeCarFromCart(mainFrame.getCurrentUser(), carBrandModel);
                loadCart();
                showAlert(Alert.AlertType.INFORMATION, "Article supprimé", "L'article a été retiré du panier.");
            }
        }
    }

    private VBox createPaymentForm() {
        VBox form = new VBox(10);
        form.setAlignment(Pos.CENTER);
        form.getChildren().add(new Label("Informations de paiement :"));

        TextField nameField = new TextField();
        nameField.setPromptText("Nom complet");
        form.getChildren().add(new HBox(10, new Label("Nom complet:"), nameField));

        TextField cardNumberField = new TextField();
        cardNumberField.setPromptText("Numéro de carte");
        form.getChildren().add(new HBox(10, new Label("Numéro de carte:"), cardNumberField));

        PasswordField bicField = new PasswordField();
        bicField.setPromptText("Code secret");
        form.getChildren().add(new HBox(10, new Label("Code Secret:"), bicField));

        TextField expiryDateField = new TextField();
        expiryDateField.setPromptText("MM/YY");
        form.getChildren().add(new HBox(10, new Label("Date d'expiration (MM/YY):"), expiryDateField));

        return form;
    }

    private void handlePurchase() {
        try {
            // Passer la commande et récupérer l'objet Order
            Order order = cartService.placeOrder(mainFrame.getCurrentUser());
    
            loadCart();
            cartListView.getItems().clear();
            updateTotal();
            
            double Amount = order.getCars().stream().mapToDouble(Car::getPrice).sum();

            // Générer la facture immédiatement après la validation de l'achat
            // 1. Créer l'instance de Invoice
            Invoice invoice = new Invoice(order, Amount);

            // 2. Enregistrer dans la base de données (l'ID sera auto-généré)
            InvoiceRepository invoiceRepository = new InvoiceRepository();
            invoiceRepository.save(invoice); 
            // 3. Générer la facture PDF avec l'ID mis à jour
            generateInvoicePDF(invoice);
    
            showAlert(Alert.AlertType.INFORMATION, "Achat confirmé", "Merci pour votre achat ! La facture a été générée.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue lors de l'achat.");
        }
    }    

    private void updateTotal() {
        double total = cartService.getCartTotal(mainFrame.getCurrentUser());
        totalLabel.setText("Total: " + total + "€");
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
