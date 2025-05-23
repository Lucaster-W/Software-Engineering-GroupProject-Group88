import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.awt.*;
import java.net.URI;
import javafx.application.Platform;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class FinancialAnalysis extends Application {

    private final String lineColor = "#855FAF";
    private final String barColor = "#855FAF";
    private final String[] pieColors = {"#855FAF", "#CEA3ED", "#7D4B79", "#F05865", "#36344C"};
    private final String backgroundColor = "#FFD4EC54";

    private List<Transaction> transactions = new ArrayList<>();

    @Override
    public void start(Stage primaryStage) {
        // Load data from CSV first
        loadTransactionData();

        BorderPane root = new BorderPane();
        VBox mainContainer = new VBox();
        mainContainer.setPadding(new Insets(20));
        mainContainer.setSpacing(20);
        mainContainer.setStyle("-fx-background-color: " + backgroundColor + ";");

        Text title = new Text("Financial Analysis Dashboard");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        title.setFill(Color.web("#855FAF"));
        title.setOpacity(0);
        title.setScaleX(0.5);
        title.setScaleY(0.5);

        ParallelTransition titleAnimation = new ParallelTransition(
                new FadeTransition(Duration.seconds(1), title),
                new ScaleTransition(Duration.seconds(1), title)
        );
        ((FadeTransition)titleAnimation.getChildren().get(0)).setToValue(1);
        ((ScaleTransition)titleAnimation.getChildren().get(1)).setToX(1);
        ((ScaleTransition)titleAnimation.getChildren().get(1)).setToY(1);

        // Build charts with actual data
        LineChart<Number, Number> spendingTrendChart = buildSpendingTrendChart();
        BarChart<String, Number> categorySpendingChart = buildCategorySpendingChart();
        PieChart paymentMethodChart = buildCategoryChart();

        Label trendTitle = new Label("Monthly Spending Trend");
        trendTitle.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        trendTitle.setTextFill(Color.web("#855FAF"));

        Label categoryTitle = new Label("Spending by Category");
        categoryTitle.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        categoryTitle.setTextFill(Color.web("#855FAF"));

        Label methodTitle = new Label("Payment Methods");
        methodTitle.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        methodTitle.setTextFill(Color.web("#855FAF"));

        WebView webView = new WebView();
        webView.getEngine().loadContent(generateFinancialAnalysis());
        webView.setPrefHeight(400);  // 设置合适的高度

        Button aiButton = new Button("Ask More");
        aiButton.setStyle("-fx-background-color: " + lineColor + "; -fx-text-fill: white; -fx-font-size: 16px;");
        aiButton.setOnAction(e -> {
            try {
                Desktop.getDesktop().browse(new URI("https://chat.deepseek.com/"));
            } catch (Exception ex) {
                System.out.println("Fail to open the web: " + ex.getMessage());
            }
        });

        ScaleTransition scaleBtn = new ScaleTransition(Duration.millis(200), aiButton);
        aiButton.setOnMouseEntered(e -> {
            scaleBtn.setToX(1.05);
            scaleBtn.setToY(1.05);
            scaleBtn.play();
        });
        aiButton.setOnMouseExited(e -> {
            scaleBtn.setToX(1.0);
            scaleBtn.setToY(1.0);
            scaleBtn.play();
        });

        VBox contentContainer = new VBox(30);
        contentContainer.setPadding(new Insets(20));
        contentContainer.setAlignment(Pos.TOP_CENTER);
        contentContainer.setStyle("-fx-background-color: " + backgroundColor + ";");
        contentContainer.setOpacity(0);

        contentContainer.getChildren().addAll(title, trendTitle, spendingTrendChart,
                categoryTitle, categorySpendingChart, methodTitle, paymentMethodChart,
                webView, aiButton);  // 注意这里用webView替换了原来的analysis

        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.8), contentContainer);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        SequentialTransition sequentialTransition = new SequentialTransition();
        for (Node node : contentContainer.getChildren()) {
            if (node instanceof Chart || node instanceof Label || node instanceof Text) {
                node.setOpacity(0);
                node.setTranslateY(20);

                ParallelTransition pt = new ParallelTransition(
                        new FadeTransition(Duration.seconds(0.5), node),
                        new TranslateTransition(Duration.seconds(0.5), node)
                );
                ((FadeTransition)pt.getChildren().get(0)).setFromValue(0);
                ((FadeTransition)pt.getChildren().get(0)).setToValue(1);
                ((TranslateTransition)pt.getChildren().get(1)).setFromY(20);
                ((TranslateTransition)pt.getChildren().get(1)).setToY(0);

                sequentialTransition.getChildren().add(pt);
            }
        }

        ParallelTransition allAnimations = new ParallelTransition(fadeIn, sequentialTransition, titleAnimation);
        allAnimations.play();

        ScrollPane scrollPane = new ScrollPane(contentContainer);
        scrollPane.setFitToWidth(true);

        mainContainer.getChildren().add(scrollPane);
        root.setCenter(mainContainer);

        // Bottom Navigation Bar
        HBox bottomNavigationBar = new HBox();
        bottomNavigationBar.setSpacing(0);
        bottomNavigationBar.setAlignment(Pos.CENTER);
        bottomNavigationBar.setPrefHeight(80);
        bottomNavigationBar.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-width: 1 0 0 0;");

        Button homeButton = createNavButtonWithEmoji("Home", "🏠");
        Button discoverButton = createNavButtonWithEmoji("Discover", "🔍");
        Button settingsButton = createNavButtonWithEmoji("Settings", "⚙");

        homeButton.setOnAction(e -> {
            try { new Nutllet().start(new Stage()); primaryStage.close(); } catch (Exception ex) { ex.printStackTrace(); }
        });
        discoverButton.setOnAction(e -> {
            try { new Discover().start(new Stage()); primaryStage.close(); } catch (Exception ex) { ex.printStackTrace(); }
        });
        settingsButton.setOnAction(e -> {
            try { new Settings().start(new Stage()); primaryStage.close(); } catch (Exception ex) { ex.printStackTrace(); }
        });

        bottomNavigationBar.getChildren().addAll(homeButton, discoverButton, settingsButton);
        root.setBottom(bottomNavigationBar);

        Scene scene = new Scene(root, 1366, 768);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Financial Analysis");
        primaryStage.show();

        Platform.runLater(() -> {
            scrollPane.setVvalue(0);
            scrollPane.layout();
        });
    }

    private void loadTransactionData() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        try (BufferedReader reader = new BufferedReader(new FileReader("deals.csv"))) {
            String line;
            boolean headerSkipped = false;

            while ((line = reader.readLine()) != null) {
                if (!headerSkipped) {
                    if (line.startsWith("交易时间")) {
                        headerSkipped = true;
                    }
                    continue;
                }

                if (line.trim().isEmpty()) continue;

                // Parse CSV line with quotes
                String[] parts = parseCsvLine(line);
                if (parts.length >= 7) {
                    try {
                        LocalDate date = LocalDate.parse(parts[0].substring(0, 10), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                        String type = parts[1];
                        String counterpart = parts[2];
                        String product = parts[3];
                        String direction = parts[4];
                        double amount = Double.parseDouble(parts[5].replace("¥", "").replace(",", ""));
                        String paymentMethod = parts[6];
                        String status = parts.length > 7 ? parts[7] : "";

                        transactions.add(new Transaction(date, type, counterpart, product, direction, amount, paymentMethod, status));
                    } catch (Exception e) {
                        System.err.println("Error parsing line: " + line);
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private String[] parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder sb = new StringBuilder();

        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                values.add(sb.toString());
                sb = new StringBuilder();
            } else {
                sb.append(c);
            }
        }
        values.add(sb.toString());
        return values.toArray(new String[0]);
    }

    private LineChart<Number, Number> buildSpendingTrendChart() {
        NumberAxis xAxis = new NumberAxis(9, 19, 1);
        xAxis.setLabel("Date");
        xAxis.setTickLabelFill(Color.web("#855FAF"));

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Amount (¥)");
        yAxis.setTickLabelFill(Color.web("#855FAF"));

        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Daily Spending Trend - APRIL (4.10-4.19)");
        chart.setLegendVisible(false);
        chart.setPrefWidth(1000);
        chart.setAlternativeRowFillVisible(false);
        chart.setAlternativeColumnFillVisible(false);

        // Group transactions by day of month
        Map<Integer, Double> dailySpending = new TreeMap<>();
        for (Transaction t : transactions) {
            if ("支出".equals(t.direction)) {
                int day = t.date.getDayOfMonth();
                if (t.date.isBefore(LocalDate.of(2025, 4, 9)) || t.date.isAfter(LocalDate.of(2025, 4, 19))) continue;
                dailySpending.put(day, dailySpending.getOrDefault(day, 0.0) + t.amount);
            }
        }

        XYChart.Series<Number, Number> dataSeries = new XYChart.Series<>();
        for (Map.Entry<Integer, Double> entry : dailySpending.entrySet()) {
            XYChart.Data<Number, Number> data = new XYChart.Data<>(entry.getKey(), entry.getValue());
            dataSeries.getData().add(data);
            Tooltip tooltip = new Tooltip("Day: " + entry.getKey() + "\nAmount: ¥" + String.format("%.2f", entry.getValue()));
            Tooltip.install(data.getNode(), tooltip);
        }
        chart.getData().add(dataSeries);

        chart.applyCss();
        chart.layout();

        for (XYChart.Data<Number, Number> data : dataSeries.getData()) {
            Node node = data.getNode();
            if (node != null) {
                node.setStyle("-fx-background-color: " + lineColor + ", white;");
            }
        }

        Node line = chart.lookup(".chart-series-line");
        if (line != null) {
            line.setStyle("-fx-stroke: " + lineColor + "; -fx-stroke-width: 2px;");
        }

        return chart;
    }


    private BarChart<String, Number> buildCategorySpendingChart() {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Category");
        xAxis.setTickLabelFill(Color.web("#855FAF"));

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Amount (¥)");
        yAxis.setTickLabelFill(Color.web("#855FAF"));

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle("Spending by Category");
        chart.setStyle("-fx-background-color: transparent; -fx-plot-background-color: " + backgroundColor + ";");
        chart.setLegendVisible(false);
        chart.setCategoryGap(20);
        chart.setBarGap(5);
        chart.setPrefWidth(1000);
        chart.setAlternativeRowFillVisible(false);
        chart.setAlternativeColumnFillVisible(false);

        // Group transactions by category (counterpart)
        Map<String, Double> categorySpending = new HashMap<>();
        for (Transaction t : transactions) {
            if ("支出".equals(t.direction)) {
                String category = t.counterpart;
                categorySpending.put(category, categorySpending.getOrDefault(category, 0.0) + t.amount);
            }
        }

        // Sort by amount descending
        List<Map.Entry<String, Double>> sortedCategories = new ArrayList<>(categorySpending.entrySet());
        sortedCategories.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

        // Take top 10 categories
        int limit = Math.min(10, sortedCategories.size());
        XYChart.Series<String, Number> dataSeries = new XYChart.Series<>();
        for (int i = 0; i < limit; i++) {
            Map.Entry<String, Double> entry = sortedCategories.get(i);
            XYChart.Data<String, Number> data = new XYChart.Data<>(entry.getKey(), entry.getValue());
            dataSeries.getData().add(data);
            Tooltip tooltip = new Tooltip("Category: " + entry.getKey() + "\nAmount: ¥" + String.format("%.2f", entry.getValue()));
            Tooltip.install(data.getNode(), tooltip);
        }
        chart.getData().add(dataSeries);

        chart.applyCss();
        chart.layout();

        for (XYChart.Data<String, Number> data : dataSeries.getData()) {
            Node node = data.getNode();
            if (node != null) {
                node.setStyle("-fx-bar-fill: " + barColor + ";");
            }
        }

        return chart;
    }



    private PieChart buildCategoryChart() {
        Map<String, Double> categoryTotals = new HashMap<>();
        for (Transaction t : transactions) {
            String category = categorizeExpense(t);
            categoryTotals.put(category, categoryTotals.getOrDefault(category, 0.0) + t.amount);
        }

        PieChart chart = new PieChart();
        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            chart.getData().add(new PieChart.Data(entry.getKey(), entry.getValue()));
        }
        chart.setTitle("Spending by Category");
        return chart;
    }

    private String categorizeExpense(Transaction t) {
        String counterpart = t.counterpart.toLowerCase();
        String product = t.product.toLowerCase();

        if (counterpart.contains("美团") || product.contains("餐") || counterpart.contains("食堂") || product.contains("茶")) return "Food";
        if (counterpart.contains("滴滴") || counterpart.contains("加油站") || counterpart.contains("石油") || product.contains("交通")) return "Transport";
        if (counterpart.contains("电影院") || product.contains("游戏") || counterpart.contains("休息") || counterpart.contains("Apple")) return "Entertainment";
        if (counterpart.contains("超市") || product.contains("日用品") || counterpart.contains("叮咚") || counterpart.contains("京东到家")) return "Living";
        if (product.contains("会员")) return "Subscription";
        if (product.contains("转账") || product.contains("红包")) return "Social";
        if (product.contains("银行") || product.contains("理财")) return "Finance";
        if (product.contains("医疗") || product.contains("药品")) return "Health";
        if (product.contains("教育") || product.contains("学费")) return "Education";
        if (product.contains("租金") || product.contains("房租")) return "Housing";
        return "Other";
    }


    private String generateFinancialAnalysis() {
        double totalSpending = transactions.stream()
                .filter(t -> "支出".equals(t.direction))
                .mapToDouble(t -> t.amount)
                .sum();

        double totalIncome = transactions.stream()
                .filter(t -> "收入".equals(t.direction))
                .mapToDouble(t -> t.amount)
                .sum();

        Optional<Transaction> largestExpense = transactions.stream()
                .filter(t -> "支出".equals(t.direction))
                .max(Comparator.comparingDouble(t -> t.amount));

        // 创建HTML格式的分析报告
        StringBuilder html = new StringBuilder();
        html.append("""
        <html>
        <head>
            <style>
                body {
                    font-family: 'Arial', sans-serif;
                    background-color: #FFD4EC54;
                    color: #855FAF;
                    padding: 20px;
                    line-height: 1.6;
                }
                h1 { color: #855FAF; border-bottom: 2px solid #CEA3ED; padding-bottom: 10px; }
                h2 { color: #6a3093; margin-top: 20px; }
                ul { padding-left: 20px; }
                li { margin-bottom: 8px; }
                strong { color: #d14; }
                .highlight { background-color: #F0E6FF; padding: 2px 5px; border-radius: 3px; }
            </style>
        </head>
        <body>
        <h1>Financial Analysis</h1>
        
        <h2>Summary</h2>
        <ul>
            <li><strong>Total Income</strong>: ¥%.2f</li>
            <li><strong>Total Spending</strong>: ¥%.2f</li>
            <li><strong>Net Balance</strong>: <span class="highlight">¥%.2f</span></li>
        </ul>
        """.formatted(totalIncome, totalSpending, (totalIncome - totalSpending)));

        // 添加最大支出
        if (largestExpense.isPresent()) {
            Transaction t = largestExpense.get();
            html.append("""
            <h2>Spending Highlights</h2>
            <ul>
                <li><strong>Largest Expense</strong>: ¥%.2f
                    <ul>
                        <li><em>Where</em>: %s</li>
                        <li><em>When</em>: %s</li>
                        <li><em>Category</em>: %s</li>
                    </ul>
                </li>
            </ul>
            """.formatted(t.amount, t.counterpart, t.date.toString(), categorizeExpense(t)));
        }

        // 添加其他分析
        html.append("""
        <h2>Trends</h2>
        <ul>
            <li><strong>Most Active Day</strong>: %s</li>
            <li><strong>Top Category</strong>: %s</li>
            <li><strong>Primary Payment Method</strong>: %s</li>
        </ul>
        """.formatted(getMostSpendingDay(), getTopSpendingCategory(), getPrimaryPaymentMethod()));

        // 添加分类明细
        html.append("<h2>Category Breakdown</h2><ul>");
        Map<String, Double> categoryTotals = new HashMap<>();
        for (Transaction t : transactions) {
            if ("支出".equals(t.direction)) {
                String category = categorizeExpense(t);
                categoryTotals.put(category, categoryTotals.getOrDefault(category, 0.0) + t.amount);
            }
        }

        categoryTotals.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .forEach(entry -> {
                    double percentage = (entry.getValue() / totalSpending) * 100;
                    html.append(String.format("<li><strong>%s</strong>: ¥%.2f (%.1f%%)</li>",
                            entry.getKey(), entry.getValue(), percentage));
                });
        html.append("</ul>");

        // 添加建议
        html.append("""
        <h2>Recommendations</h2>
        <ul>
            <li>Consider setting budgets for top spending categories</li>
            <li>Review recurring subscriptions and memberships</li>
            <li>Analyze if largest expenses were necessary purchases</li>
        </ul>
        </body>
        </html>
        """);

        return html.toString();
    }


    private String getMostSpendingDay() {
        Map<Integer, Double> dailySpending = new HashMap<>();
        for (Transaction t : transactions) {
            if ("支出".equals(t.direction)) {
                int day = t.date.getDayOfMonth();
                dailySpending.put(day, dailySpending.getOrDefault(day, 0.0) + t.amount);
            }
        }

        return dailySpending.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(e -> "day " + e.getKey())
                .orElse("unknown day");
    }

    private String getTopSpendingCategory() {
        Map<String, Double> categorySpending = new HashMap<>();
        for (Transaction t : transactions) {
            if ("支出".equals(t.direction)) {
                categorySpending.put(t.counterpart, categorySpending.getOrDefault(t.counterpart, 0.0) + t.amount);
            }
        }

        return categorySpending.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("unknown");
    }

    private String getPrimaryPaymentMethod() {
        Map<String, Double> methodSpending = new HashMap<>();
        for (Transaction t : transactions) {
            if ("支出".equals(t.direction)) {
                methodSpending.put(t.paymentMethod, methodSpending.getOrDefault(t.paymentMethod, 0.0) + t.amount);
            }
        }

        return methodSpending.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("unknown");
    }

    private Button createNavButtonWithEmoji(String labelText, String emojiSymbol) {
        VBox buttonContent = new VBox();
        buttonContent.setAlignment(Pos.CENTER);
        buttonContent.setSpacing(2);

        Label emojiLabel = new Label(emojiSymbol);
        emojiLabel.setStyle("-fx-font-size: 16px;");

        Label textLabel = new Label(labelText);
        textLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

        buttonContent.getChildren().addAll(emojiLabel, textLabel);

        Button navigationButton = new Button();
        navigationButton.setPrefWidth(456);
        navigationButton.setPrefHeight(80);
        navigationButton.setGraphic(buttonContent);
        navigationButton.setStyle("-fx-background-color: white; -fx-border-color: transparent;");

        ScaleTransition scaleNavBtn = new ScaleTransition(Duration.millis(150), navigationButton);
        navigationButton.setOnMouseEntered(e -> {
            scaleNavBtn.setToX(1.03);
            scaleNavBtn.setToY(1.03);
            scaleNavBtn.play();

            emojiLabel.setStyle("-fx-font-size: 18px;");
            textLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #855FAF;");
        });

        navigationButton.setOnMouseExited(e -> {
            scaleNavBtn.setToX(1.0);
            scaleNavBtn.setToY(1.0);
            scaleNavBtn.play();

            emojiLabel.setStyle("-fx-font-size: 16px;");
            textLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
        });

        return navigationButton;
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static class Transaction {
        LocalDate date;
        String type;
        String counterpart;
        String product;
        String direction;
        double amount;
        String paymentMethod;
        String status;

        public Transaction(LocalDate date, String type, String counterpart, String product,
                           String direction, double amount, String paymentMethod, String status) {
            this.date = date;
            this.type = type;
            this.counterpart = counterpart;
            this.product = product;
            this.direction = direction;
            this.amount = amount;
            this.paymentMethod = paymentMethod;
            this.status = status;
        }
    }
}