package tn.esprit.controllers.Admin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import tn.esprit.controllers.auth.UserSession;
import tn.esprit.entities.User;
import tn.esprit.services.AnnonceService;
import tn.esprit.services.EvenementService;
import tn.esprit.services.ForumService;
import tn.esprit.services.UserService;
import tn.esprit.services.group.GroupService;
import tn.esprit.services.paiementService;
import tn.esprit.services.project.ProjectService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class AdminDashboardController {
    @FXML
    private VBox sidebarContainer;
    @FXML
    private Text welcomeText;
    @FXML
    private Text userCountText;
    @FXML
    private Text newUsersText;
    @FXML
    private Text eventCountText;
    @FXML
    private Text upcomingEventsText;
    @FXML
    private Text postCountText;
    @FXML
    private Text todayPostsText;
    @FXML
    private Text paymentCountText;
    @FXML
    private Text monthRevenueText;

    @FXML
    private BarChart<String, Number> eventsByMonthChart;
    @FXML
    private PieChart annoncesByTypeChart;
    @FXML
    private LineChart<String, Number> forumActivityChart;
    @FXML
    private BarChart<String, Number> groupsByProjectsChart;
    @FXML
    private PieChart paymentDistributionChart;
    @FXML
    private LineChart<String, Number> monthlyRevenueChart;

    private User currentUser;
    private final UserService userService = new UserService();
    private final EvenementService evenementService = new EvenementService();
    private final AnnonceService annonceService = new AnnonceService();
    private final ForumService forumService = new ForumService();
    private final GroupService groupService = GroupService.getInstance();
    private final ProjectService projectService = ProjectService.getInstance();
    private final paiementService paiementService = new paiementService();

    private final Random random = new Random(); // For demo data where real data is not available

    @FXML
    public void initialize() {
        // Verify user session first
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser == null) {
            redirectToLogin();
            return;
        }
        initializeWithUser(currentUser);
    }

    private void redirectToLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/interfaces/auth/login.fxml"));
            Stage stage = (Stage) sidebarContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initializeWithUser(User user) {
        this.currentUser = user;
        System.out.println("Initializing with user: " + (user != null ? user.getName() : "null"));

        // Set welcome message with user's name
        if (user != null) {
            welcomeText.setText("Welcome, " + user.getName() + "!");
        }

        loadSidebar();
        loadStatistics();
    }

    private void loadSidebar() {
        System.out.println("Attempting to load sidebar...");

        try {
            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/Admin/Sidebar.fxml"));
            System.out.println("FXML loader created, resource: " + loader.getLocation());

            Parent sidebar = loader.load();
            System.out.println("Sidebar FXML loaded successfully");

            // Get the controller and set up dependencies
            SidebarController sidebarController = loader.getController();
            System.out.println("Sidebar controller obtained");

            if (currentUser != null) {
                sidebarController.setCurrentUser(currentUser);
                System.out.println("Current user set on sidebar controller");
            }

            // Clear and add the sidebar
            sidebarContainer.getChildren().clear();
            sidebarContainer.getChildren().add(sidebar);
            System.out.println("Sidebar added to container");

        } catch (IOException e) {
            System.err.println("CRITICAL ERROR LOADING SIDEBAR FXML:");
            e.printStackTrace();
            showErrorAlert("Failed to load sidebar", "Could not load the sidebar navigation. Please check the resource path.\nError: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("UNEXPECTED ERROR LOADING SIDEBAR:");
            e.printStackTrace();
            showErrorAlert("Unexpected error", "An unexpected error occurred while loading the sidebar.\nError: " + e.getMessage());
        }
    }

    private void loadStatistics() {
        try {
            // Load summary statistics
            loadSummaryStats();

            // Load chart data
            loadEventChart();
            loadAnnonceChart();
            loadForumChart();
            loadGroupChart();
            loadPaymentCharts();

        } catch (Exception e) {
            System.err.println("Error loading statistics: " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Statistics Error", "Failed to load some statistics. Please try refreshing.");
        }
    }

    private void loadSummaryStats() {
        try {
            // Users stats
            int userCount = userService.getTotalUserCount();
            userCountText.setText(String.valueOf(userCount));
            newUsersText.setText("New this week: " + userService.getNewUsersThisWeek());

            // Events stats
            int eventCount = evenementService.getEventCount();
            eventCountText.setText(String.valueOf(eventCount));
            upcomingEventsText.setText("Upcoming: " + evenementService.getUpcomingEventCount());

            // Forum posts stats
            int postCount = forumService.getTotalPostCount();
            postCountText.setText(String.valueOf(postCount));
            todayPostsText.setText("Today: " + forumService.getTodayPostCount());

            // Payment stats
            int paymentCount = paiementService.getTotalPaymentCount();
            paymentCountText.setText(String.valueOf(paymentCount));

            double monthRevenue = paiementService.getCurrentMonthRevenue();
            monthRevenueText.setText("This month: $" + String.format("%.2f", monthRevenue));

        } catch (Exception e) {
            System.err.println("Error loading summary stats: " + e.getMessage());
            // Use demo data if real data can't be loaded
            userCountText.setText(String.valueOf(100 + random.nextInt(900)));
            newUsersText.setText("New this week: " + (5 + random.nextInt(20)));

            eventCountText.setText(String.valueOf(10 + random.nextInt(90)));
            upcomingEventsText.setText("Upcoming: " + (2 + random.nextInt(10)));

            postCountText.setText(String.valueOf(200 + random.nextInt(800)));
            todayPostsText.setText("Today: " + (3 + random.nextInt(15)));

            paymentCountText.setText(String.valueOf(50 + random.nextInt(200)));
            monthRevenueText.setText("This month: $" + String.format("%.2f", 1000.0 + random.nextDouble() * 5000));
        }
    }

    private void loadEventChart() {
        try {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Events");

            // Try to get real data
            Map<YearMonth, Integer> eventsByMonth = evenementService.getEventCountByMonth();

            if (eventsByMonth != null && !eventsByMonth.isEmpty()) {
                for (Map.Entry<YearMonth, Integer> entry : eventsByMonth.entrySet()) {
                    series.getData().add(new XYChart.Data<>(
                            entry.getKey().format(DateTimeFormatter.ofPattern("MMM yyyy")),
                            entry.getValue()));
                }
            } else {
                // Demo data if real data can't be loaded
                LocalDate now = LocalDate.now();
                for (int i = 6; i >= 0; i--) {
                    YearMonth month = YearMonth.from(now.minusMonths(i));
                    series.getData().add(new XYChart.Data<>(
                            month.format(DateTimeFormatter.ofPattern("MMM yyyy")),
                            random.nextInt(15) + 1));
                }
            }

            eventsByMonthChart.getData().add(series);

        } catch (Exception e) {
            System.err.println("Error loading event chart: " + e.getMessage());
            loadDemoEventChart();
        }
    }

    private void loadAnnonceChart() {
        try {
            // Try to get real data
            Map<String, Integer> annoncesByType = annonceService.getAnnonceCountByEventType();

            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

            if (annoncesByType != null && !annoncesByType.isEmpty()) {
                for (Map.Entry<String, Integer> entry : annoncesByType.entrySet()) {
                    pieChartData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
                }
            } else {
                // Demo data if real data can't be loaded
                pieChartData.add(new PieChart.Data("Workshop", random.nextInt(20) + 5));
                pieChartData.add(new PieChart.Data("Conference", random.nextInt(15) + 10));
                pieChartData.add(new PieChart.Data("Meetup", random.nextInt(25) + 5));
                pieChartData.add(new PieChart.Data("Webinar", random.nextInt(10) + 5));
            }

            annoncesByTypeChart.setData(pieChartData);

        } catch (Exception e) {
            System.err.println("Error loading annonce chart: " + e.getMessage());
            loadDemoAnnonceChart();
        }
    }

    private void loadForumChart() {
        try {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Posts");

            // Try to get real data
            Map<LocalDate, Integer> postsByDate = forumService.getPostCountByDate();

            if (postsByDate != null && !postsByDate.isEmpty()) {
                for (Map.Entry<LocalDate, Integer> entry : postsByDate.entrySet()) {
                    series.getData().add(new XYChart.Data<>(
                            entry.getKey().format(DateTimeFormatter.ofPattern("dd MMM")),
                            entry.getValue()));
                }
            } else {
                // Demo data if real data can't be loaded
                LocalDate now = LocalDate.now();
                for (int i = 14; i >= 0; i--) {
                    LocalDate date = now.minusDays(i);
                    series.getData().add(new XYChart.Data<>(
                            date.format(DateTimeFormatter.ofPattern("dd MMM")),
                            random.nextInt(30) + 1));
                }
            }

            forumActivityChart.getData().add(series);

        } catch (Exception e) {
            System.err.println("Error loading forum chart: " + e.getMessage());
            loadDemoForumChart();
        }
    }

    private void loadGroupChart() {
        try {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Projects");

            // Try to get real data
            Map<String, Integer> groupProjects = groupService.getGroupProjectCounts();

            if (groupProjects != null && !groupProjects.isEmpty()) {
                // Sort by project count and take top 5
                groupProjects.entrySet().stream()
                        .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                        .limit(5)
                        .forEach(entry ->
                                series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue())));
            } else {
                // Demo data if real data can't be loaded
                series.getData().add(new XYChart.Data<>("Team Alpha", random.nextInt(10) + 5));
                series.getData().add(new XYChart.Data<>("Innovators", random.nextInt(8) + 3));
                series.getData().add(new XYChart.Data<>("Tech Wizards", random.nextInt(7) + 2));
                series.getData().add(new XYChart.Data<>("Code Masters", random.nextInt(6) + 2));
                series.getData().add(new XYChart.Data<>("Dev Heroes", random.nextInt(5) + 1));
            }

            groupsByProjectsChart.getData().add(series);

        } catch (Exception e) {
            System.err.println("Error loading group chart: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void loadPaymentCharts() {
        try {
            // Load pie chart for payment distribution
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

            // Try to get real data
            Map<String, Double> paymentsByOffer = paiementService.getPaymentAmountsByOffer();

            if (paymentsByOffer != null && !paymentsByOffer.isEmpty()) {
                for (Map.Entry<String, Double> entry : paymentsByOffer.entrySet()) {
                    pieChartData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
                }
            } else {
                // Demo data if real data can't be loaded
                pieChartData.add(new PieChart.Data("Premium", random.nextDouble() * 3000 + 1000));
                pieChartData.add(new PieChart.Data("Standard", random.nextDouble() * 2000 + 800));
                pieChartData.add(new PieChart.Data("Basic", random.nextDouble() * 1500 + 500));
            }

            paymentDistributionChart.setData(pieChartData);

            // Load line chart for monthly revenue
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Revenue");

            // Try to get real data
            Map<YearMonth, Double> revenueByMonth = paiementService.getRevenueByMonth();

            if (revenueByMonth != null && !revenueByMonth.isEmpty()) {
                for (Map.Entry<YearMonth, Double> entry : revenueByMonth.entrySet()) {
                    series.getData().add(new XYChart.Data<>(
                            entry.getKey().format(DateTimeFormatter.ofPattern("MMM yyyy")),
                            entry.getValue()));
                }
            } else {
                // Demo data if real data can't be loaded
                LocalDate now = LocalDate.now();
                for (int i = 6; i >= 0; i--) {
                    YearMonth month = YearMonth.from(now.minusMonths(i));
                    series.getData().add(new XYChart.Data<>(
                            month.format(DateTimeFormatter.ofPattern("MMM yyyy")),
                            random.nextDouble() * 5000 + 1000));
                }
            }

            monthlyRevenueChart.getData().add(series);

        } catch (Exception e) {
            System.err.println("Error loading payment charts: " + e.getMessage());
            loadDemoPaymentCharts();
        }
    }

    // Demo data methods in case real services aren't implemented yet
    private void loadDemoEventChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Events");

        LocalDate now = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            YearMonth month = YearMonth.from(now.minusMonths(i));
            series.getData().add(new XYChart.Data<>(
                    month.format(DateTimeFormatter.ofPattern("MMM yyyy")),
                    random.nextInt(15) + 1));
        }

        eventsByMonthChart.getData().clear();
        eventsByMonthChart.getData().add(series);
    }

    private void loadDemoAnnonceChart() {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Workshop", random.nextInt(20) + 5),
                new PieChart.Data("Conference", random.nextInt(15) + 10),
                new PieChart.Data("Meetup", random.nextInt(25) + 5),
                new PieChart.Data("Webinar", random.nextInt(10) + 5)
        );

        annoncesByTypeChart.setData(pieChartData);
    }

    private void loadDemoForumChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Posts");

        LocalDate now = LocalDate.now();
        for (int i = 14; i >= 0; i--) {
            LocalDate date = now.minusDays(i);
            series.getData().add(new XYChart.Data<>(
                    date.format(DateTimeFormatter.ofPattern("dd MMM")),
                    random.nextInt(30) + 1));
        }

        forumActivityChart.getData().clear();
        forumActivityChart.getData().add(series);
    }

    private void loadDemoGroupChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Projects");

        series.getData().add(new XYChart.Data<>("Team Alpha", random.nextInt(10) + 5));
        series.getData().add(new XYChart.Data<>("Innovators", random.nextInt(8) + 3));
        series.getData().add(new XYChart.Data<>("Tech Wizards", random.nextInt(7) + 2));
        series.getData().add(new XYChart.Data<>("Code Masters", random.nextInt(6) + 2));
        series.getData().add(new XYChart.Data<>("Dev Squad", random.nextInt(5) + 1));

        groupsByProjectsChart.getData().clear();
        groupsByProjectsChart.getData().add(series);
    }

    private void loadDemoPaymentCharts() {
        // Pie chart demo data
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Premium", random.nextDouble() * 3000 + 1000),
                new PieChart.Data("Standard", random.nextDouble() * 2000 + 800),
                new PieChart.Data("Basic", random.nextDouble() * 1500 + 500)
        );

        paymentDistributionChart.setData(pieChartData);

        // Line chart demo data
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Revenue");

        LocalDate now = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            YearMonth month = YearMonth.from(now.minusMonths(i));
            series.getData().add(new XYChart.Data<>(
                    month.format(DateTimeFormatter.ofPattern("MMM yyyy")),
                    random.nextDouble() * 5000 + 1000));
        }

        monthlyRevenueChart.getData().clear();
        monthlyRevenueChart.getData().add(series);
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}