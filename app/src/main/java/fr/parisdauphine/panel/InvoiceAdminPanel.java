package fr.parisdauphine.panel;

import fr.parisdauphine.config.HibernateUtil;
import fr.parisdauphine.entity.Invoice;
import fr.parisdauphine.entity.Order;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.hibernate.Session;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class InvoiceAdminPanel extends VBox {

    private final MainFrame mainFrame;
    private VBox invoiceListContainer;

    public InvoiceAdminPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        initializeUI();
    }

    private void initializeUI() {
        setPadding(new Insets(20));
        setSpacing(10);
        setAlignment(Pos.TOP_CENTER);

        Label titleLabel = new Label("👑 Gestion des Factures");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        invoiceListContainer = new VBox(10);

        ScrollPane invoiceScrollPane = new ScrollPane(invoiceListContainer);
        invoiceScrollPane.setFitToWidth(true);
        invoiceScrollPane.setPrefHeight(300);

        loadInvoices();
        getChildren().addAll(
                titleLabel,
                invoiceScrollPane
        );
    }

    private void loadInvoices() {
        new Thread(() -> {
            List<Invoice> invoices;
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                // Utilisation de JOIN FETCH pour charger les entités User et Order de manière eager
                invoices = session.createQuery(
                                "SELECT i FROM Invoice i JOIN FETCH i.order o JOIN FETCH o.user", Invoice.class)
                        .list();
            }

            javafx.application.Platform.runLater(() -> {
                invoiceListContainer.getChildren().clear();
                for (Invoice invoice : invoices) {
                    invoiceListContainer.getChildren().add(createInvoiceCard(invoice));
                }
            });
        }).start();
    }

    private HBox createInvoiceCard(Invoice invoice) {
        HBox invoiceCard = new HBox(10);
        invoiceCard.setPadding(new Insets(10));
        invoiceCard.setStyle("-fx-border-color: black; -fx-border-radius: 5; -fx-border-width: 1;");
        invoiceCard.setAlignment(Pos.CENTER_LEFT);

        Label invoiceInfo = new Label("Facture #" + invoice.getId() + " - Montant: " + invoice.getAmount() + "€ - Date: " + invoice.getInvoiceDate());

        Button downloadButton = new Button("⬇️ Télécharger");
        downloadButton.setOnAction(e -> downloadInvoice(invoice));
        Button deleteButton = new Button("🗑️ Supprimer");
        deleteButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");
        deleteButton.setOnAction(e -> confirmDeleteInvoice(invoice));

        invoiceCard.getChildren().addAll(invoiceInfo, downloadButton, deleteButton);
        return invoiceCard;
    }

    private void downloadInvoice(Invoice invoice) {
        // Vérifier si le fichier existe avant d'afficher le FileChooser
        String filePath = generateInvoiceFilePath(invoice);
        File file = new File(filePath);

        if (file.exists()) {
            // Lancer un FileChooser pour télécharger la facture en local
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            fileChooser.setInitialFileName(file.getName());
            File destinationFile = fileChooser.showSaveDialog(null);

            if (destinationFile != null) {
                try {
                    // Copier le fichier à l'emplacement sélectionné
                    Files.copy(file.toPath(), destinationFile.toPath());
                    showAlert(Alert.AlertType.INFORMATION, "Téléchargement réussi", "La facture a été téléchargée sous : " + destinationFile.getAbsolutePath());
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du téléchargement de la facture.");
                }
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Fichier non trouvé", "La facture n'a pas été générée ou a été supprimée.");
        }
    }

    private String generateInvoiceFilePath(Invoice invoice) {
        // Générer le chemin et le nom du fichier PDF
        String directoryPath = "src/main/resources/factures/";
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        String userName = invoice.getOrder().getUser().getNom();  // Accès à l'utilisateur sans problème de session
        String fileName = "facture_" + userName + "_"
                + invoice.getOrder().getUser().getPrenom() + "_" + invoice.getId() + ".pdf";
        return new File(directory, fileName).getAbsolutePath();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void confirmDeleteInvoice(Invoice invoice) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de Suppression");
        alert.setHeaderText(null);
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cette facture ?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                deleteInvoice(invoice);
            }
        });
    }

    private void deleteInvoice(Invoice invoice) {
        new Thread(() -> {
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                // Supprimer le fichier PDF associé à la facture
                String filePath = generateInvoiceFilePath(invoice);
                File file = new File(filePath);
                if (file.exists()) {
                    if (file.delete()) {
                        System.out.println("Fichier supprimé : " + file.getAbsolutePath());
                    } else {
                        System.err.println("Erreur lors de la suppression du fichier : " + file.getAbsolutePath());
                    }
                }

                // Supprimer la facture de la base de données
                session.beginTransaction();
                session.delete(invoice);
                session.getTransaction().commit();
            } catch (Exception e) {
                e.printStackTrace();
            }

            javafx.application.Platform.runLater(this::loadInvoices);
        }).start();
    }

    private List<Order> getOrders() {
        // Cette méthode doit récupérer les commandes dans la base de données
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Order", Order.class).list();
        }
    }
}
