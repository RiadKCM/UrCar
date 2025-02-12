package fr.parisdauphine.panel;

import fr.parisdauphine.config.HibernateUtil;
import fr.parisdauphine.entity.Car;
import fr.parisdauphine.entity.Order;
import fr.parisdauphine.entity.User;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.hibernate.Session;

import java.text.DecimalFormat;
import java.util.List;

public class DashboardPanel extends VBox {
    private final MainFrame mainFrame;
    private Label totalUsersLabel;
    private Label totalSalesLabel;
    private Label totalCarsLabel;
    private Label carsInStockLabel;
    private PieChart orderStatusChart;
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,##0.00 €");

    public DashboardPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        initializeUI();
    }

    private void initializeUI() {
        setPadding(new Insets(20));
        setSpacing(15);
        setAlignment(Pos.TOP_CENTER);
        setStyle("-fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-border-radius: 5;");

        Label titleLabel = new Label("📊 Tableau de Bord");
        titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        totalUsersLabel = createStyledLabel("Utilisateurs: Chargement...");
        totalSalesLabel = createStyledLabel("Total ventes: Chargement...");
        totalCarsLabel = createStyledLabel("Voitures vendues: Chargement...");
        carsInStockLabel = createStyledLabel("Voitures en stock: Chargement...");

        orderStatusChart = new PieChart();
        orderStatusChart.setTitle("Répartition des Statuts de Commande");
        orderStatusChart.setStyle("-fx-background-color: transparent;");

        loadDashboardData();
        getChildren().addAll(titleLabel, totalUsersLabel, totalSalesLabel, totalCarsLabel, carsInStockLabel, orderStatusChart);
    }

    private Label createStyledLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333333;");
        return label;
    }

    private void loadDashboardData() {
        new Thread(() -> {
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                // Récupération des données
                Long totalUsers = (Long) session.createQuery("SELECT COUNT(u) FROM User u").uniqueResult();
                Long totalCars = (Long) session.createQuery("SELECT COUNT(c) FROM Car c").uniqueResult();
                Long carsInStock = (Long) session.createQuery("SELECT COUNT(c) FROM Car c WHERE c.status = 'EN_VENTE'").uniqueResult();
                Double totalSales = (Double) session.createQuery("SELECT SUM(o.totalPrice) FROM Car c WHERE c.status = 'VENDU'").uniqueResult();
                List<Object[]> orderStats = session.createQuery("SELECT o.status, COUNT(o) FROM Order o GROUP BY o.status").list();

                // Sécurisation des valeurs nulles
                long finalTotalUsers = (totalUsers != null) ? totalUsers : 0;
                long finalTotalCars = (totalCars != null) ? totalCars : 0;
                long finalCarsInStock = (carsInStock != null) ? carsInStock : 0;
                double finalTotalSales = (totalSales != null) ? totalSales : 0.0;

                Platform.runLater(() -> {
                    totalUsersLabel.setText("👤 Utilisateurs: " + finalTotalUsers);
                    totalSalesLabel.setText("💰 Total ventes: " + DECIMAL_FORMAT.format(finalTotalSales));
                    totalCarsLabel.setText("🚗 Voitures vendues: " + (finalTotalCars - finalCarsInStock));
                    carsInStockLabel.setText("📦 Voitures en stock: " + finalCarsInStock);

                    orderStatusChart.getData().clear();
                    if (orderStats.isEmpty()) {
                        orderStatusChart.getData().add(new PieChart.Data("Aucune commande", 1));
                    } else {
                        for (Object[] stat : orderStats) {
                            String status = (String) stat[0];
                            long count = (long) stat[1];
                            orderStatusChart.getData().add(new PieChart.Data(status, count));
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    totalUsersLabel.setText("❌ Erreur lors du chargement des utilisateurs.");
                    totalSalesLabel.setText("❌ Erreur lors du chargement des ventes.");
                    totalCarsLabel.setText("❌ Erreur lors du chargement des voitures.");
                    carsInStockLabel.setText("❌ Erreur lors du chargement du stock.");
                });
            }
        }).start();
    }
}
