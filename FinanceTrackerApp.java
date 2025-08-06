import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javafx.stage.FileChooser;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FinanceTrackerApp extends Application {
    private double currentBalance = 0.0;
    private Label balanceLabel;
    ListView<String> transactionHistory;
    private Label message;
    private final String url = "jdbc:sqlite:/Users/sebastiancourdy/Desktop/Personal Projects/Finance Tracker/finance_tracker.db";   
    private Stage primaryStage;
    private String loggedInUsername;

    public static void main(String[] args) {
        launch(args);
    }

        public void start(Stage primaryStage) {
            this.primaryStage = primaryStage;

            message = new Label("");
            Font messageFont = Font.font(20);
            message.setFont(messageFont);

            initializeDB();
            showLoginScreen(primaryStage);
        }
        
        private void startMainScene(Stage primaryStage) {
            VBox vBox = new VBox();
            vBox.setSpacing(10);
            vBox.setAlignment(Pos.CENTER);
            vBox.setPadding(new Insets(20));

            primaryStage.setTitle("Personal Finance Tracker");

            Label labelTitle = new Label("Personal Finance Tracker");
            Font labelTitleFont = Font.font("Times New Roman", FontWeight.BOLD, 50);
            labelTitle.setFont(labelTitleFont);
            StackPane labelTitlePane = new StackPane(labelTitle);
            labelTitlePane.setAlignment(Pos.CENTER);

            balanceLabel = new Label("Balance = $" + currentBalance);
            Font balanceLabelFont = Font.font("Times New Roman", 35);
            balanceLabel.setFont(balanceLabelFont);
            HBox balanceLeft = new HBox();
            balanceLeft.setAlignment(Pos.CENTER_LEFT);
            balanceLeft.setPrefWidth(1000);
            balanceLeft.getChildren().add(balanceLabel);

            TextField addDepositTF = new TextField();
            addDepositTF.setPromptText("ex. 100");
            addDepositTF.setMaxWidth(200);
            Button addDepositB = new Button("Add Deposit");
            addDepositB.setMaxWidth(120);
            addDepositB.setOnAction(e -> {
            
            String depositInputS = addDepositTF.getText();

                if (depositInputS.isEmpty()) {
                    message.setText("Input cannot be empty");
                    } else {
                        try {
                            double depositInputD = Double.parseDouble(depositInputS);

                            if (depositInputD <= 0) {
                                message.setText("Deposit must be greater than 0");
                            } else {
                                currentBalance += depositInputD;
                                balanceLabel.setText(String.format("Balance = $%.2f", currentBalance));
                                transactionHistory.getItems().add("Deposited: $" + String.format("%.2f", depositInputD));
                                message.setText("");
                                addDepositTF.clear();

                                try (Connection connection = DriverManager.getConnection(url);
                                     PreparedStatement statement = connection.prepareStatement(
                                "INSERT INTO financeTrackerTable (username, typeOfTransaction, amount, dateAndTime) VALUES (?, 'Deposited', ?, datetime('now'))")) {
                                    statement.setString(1, loggedInUsername);
                                    statement.setDouble(2, depositInputD);
                                    statement.executeUpdate();

                                } catch (Exception ex) {
                                    message.setText("Error saving transaction");
                                    ex.printStackTrace();
                                }
                            }
                        } catch (NumberFormatException ex) {
                            message.setText("Please enter a number");
                    }
                }
            });

            HBox depositHBox = new HBox(10, addDepositTF, addDepositB);
            depositHBox.setAlignment(Pos.CENTER);

            TextField addWithdrawlTF = new TextField();
            addWithdrawlTF.setPromptText("ex. 100");
            addWithdrawlTF.setMaxWidth(200);
            Button addWithdrawlB = new Button("Add Withdrawl");
            addWithdrawlB.setMaxWidth(120);
            addWithdrawlB.setOnAction(e -> {
                String withdrawInputS = addWithdrawlTF.getText();

                if (withdrawInputS.isEmpty()) {
                        message.setText("Input cannot be empty");
                    } else {
                        try {
                            double withdrawInputD = Double.parseDouble(withdrawInputS);

                            if (withdrawInputD <= 0) {
                                message.setText("Withdrawl must be greater than 0");
                            } else if (withdrawInputD > currentBalance) {
                                message.setText("Insufficient Funds");
                                addWithdrawlTF.clear();
                                } else {
                                    currentBalance -= withdrawInputD;
                                    balanceLabel.setText(String.format("Balance = $%.2f", currentBalance));
                                    transactionHistory.getItems().add("Withdrew: $" + String.format("%.2f", withdrawInputD));
                                    message.setText("");
                                    addWithdrawlTF.clear();

                                    try (Connection connection = DriverManager.getConnection(url);
                                     PreparedStatement statement = connection.prepareStatement(
                                "INSERT INTO financeTrackerTable (username, typeOfTransaction, amount, dateAndTime) VALUES (?, 'Withdrew', ?, datetime('now'))")) {
                                    statement.setString(1, loggedInUsername);
                                    statement.setDouble(2, withdrawInputD);
                                    statement.executeUpdate();

                                } catch (Exception ex) {
                                    message.setText("Error saving transaction");
                                    ex.printStackTrace();
                                }
                                }
                            } catch (NumberFormatException ex) {
                            message.setText("Please enter a number");
                        }
                    }
                });

                HBox withdrawHBox = new HBox(10, addWithdrawlTF, addWithdrawlB);
                withdrawHBox.setAlignment(Pos.CENTER);

            Button clearBalance = new Button("Clear Balance");
            clearBalance.setOnAction(e -> {
                currentBalance = 0;
                balanceLabel.setText(String.format("Balance = $%.2f", currentBalance));
                transactionHistory.getItems().clear();
                message.setText("");
            });
            HBox clearBalanceLeft = new HBox();
            clearBalanceLeft.setAlignment(Pos.CENTER_LEFT);
            clearBalanceLeft.setPrefWidth(1000);
            clearBalanceLeft.getChildren().add(clearBalance);

            Button reloadTransactionB = new Button("Reload Transactions");
            HBox reloadTransaction = new HBox();
            reloadTransaction.setAlignment(Pos.CENTER_RIGHT);
            reloadTransaction.setPrefWidth(1000);
            reloadTransaction.getChildren().add(reloadTransactionB);
            reloadTransactionB.setOnAction(e -> {
                currentBalance = 0;
                transactionHistory.getItems().clear();
                message.setText("");
                balanceLabel.setText(String.format("Balance = $%.2f", currentBalance));
                loadFromDB();
            });

            Button clearDataB = new Button("Clear All Data");
            HBox clearData = new HBox();
            clearData.setAlignment(Pos.CENTER_LEFT);
            clearData.setPrefWidth(1000);
            clearData.getChildren().add(clearDataB);
            clearDataB.setOnAction(e -> {
                currentBalance = 0;
                balanceLabel.setText(String.format("Balance = $%.2f", currentBalance));
                transactionHistory.getItems().clear();
                message.setText("");

                try (Connection connection = DriverManager.getConnection(url);
                     PreparedStatement statement = connection.prepareStatement("DELETE FROM financeTrackerTable WHERE username = ?")) {
                    statement.setString(1, loggedInUsername);
                    statement.executeUpdate();

                    } catch (Exception exc) {
                        exc.printStackTrace();
                        message.setText("Error Deleting Data");
                    }              
            });


            Button clearTransactionHistory = new Button("Clear transaction History");
            clearTransactionHistory.setOnAction(e -> {
                transactionHistory.getItems().clear();
                message.setText("");
            });

            Button exportToCSVB = new Button("Export Data");
            HBox exportToCSV = new HBox();
            exportToCSV.setAlignment(Pos.CENTER);
            exportToCSV.setPrefWidth(1000);
            exportToCSV.getChildren().add(exportToCSVB);
            exportToCSVB.setOnAction(e -> exportDataToCSV(primaryStage));

            Button exitButton = new Button("Exit Application");
            exitButton.setOnAction(e -> {
                Platform.exit();
            });
            HBox exitRight = new HBox();
            exitRight.setAlignment(Pos.CENTER_RIGHT);
            exitRight.setPrefWidth(1000);
            exitRight.getChildren().add(exitButton);

            transactionHistory = new ListView<>();
            transactionHistory.setPrefHeight(300);
            transactionHistory.setPrefWidth(800);

            HBox messageLeft = new HBox();
            messageLeft.setAlignment(Pos.CENTER_LEFT);
            messageLeft.setPrefWidth(1000);
            messageLeft.getChildren().add(message);
   
            vBox.getChildren().addAll(labelTitlePane, balanceLeft, reloadTransaction, clearBalanceLeft, messageLeft, depositHBox, withdrawHBox, transactionHistory, clearTransactionHistory, exportToCSV, clearData, exitRight);

            vBox.setStyle("-fx-background-color: #1b858bff;");
            
            labelTitle.setStyle("-fx-text-fill: #ffffffff;");
            balanceLabel.setStyle("-fx-text-fill: #ffffffff;");
            message.setStyle("-fx-text-fill: #cb2715ff; -fx-font-weight: bold;");

            Scene scene = new Scene(vBox, 1000, 500);
            scene.setFill(Color.AQUA);
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();
            primaryStage.show();

        loadFromDB();
        }

    private void initializeDB() {
    try (Connection connection = DriverManager.getConnection(url);
         Statement statement = connection.createStatement()) {

        String userTableSQL = "CREATE TABLE IF NOT EXISTS userTable (" +
                    "username TEXT PRIMARY KEY, " +
                    "password TEXT NOT NULL)";
        statement.executeUpdate(userTableSQL);

        String transactionTableSQL = "CREATE TABLE IF NOT EXISTS financeTrackerTable (" +
                     "transactionNumber INTEGER PRIMARY KEY AUTOINCREMENT, " +
                     "username TEXT NOT NULL, " +
                     "typeOfTransaction TEXT NOT NULL, " +
                     "amount REAL NOT NULL, " +
                     "dateAndTime TEXT NOT NULL, " +
                     "FOREIGN KEY (username) REFERENCES userTable(username))";
        statement.executeUpdate(transactionTableSQL);
            System.out.println("Tables created successfully: userTable, financeTrackerTable");
    } catch (Exception e) {
        e.printStackTrace();
        message.setText("Error initializing DB");
    }
}


    private void loadFromDB() { 
        if (loggedInUsername == null) {
             message.setText("No user logged in"); 
             return;
        } try {
            Connection connection = DriverManager.getConnection(url);

            PreparedStatement statement = connection.prepareStatement("SELECT * FROM financeTrackerTable WHERE username = ?");
            statement.setString(1, loggedInUsername);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String type = resultSet.getString("typeOfTransaction");
                double amount = resultSet.getDouble("amount");
                String dateAndTime = resultSet.getString("dateAndTime");

                if (type.equalsIgnoreCase("Deposited")) {
                    currentBalance += amount;
                } else if (type.equalsIgnoreCase("Withdrew")) {
                    currentBalance -= amount;
                }

                String result = String.format("%s: $%.2f (%s)", type, amount, dateAndTime);
                transactionHistory.getItems().add(result);
            }
        
            balanceLabel.setText(String.format("Balance = $%.2f", currentBalance));

            resultSet.close();
            statement.close();
            connection.close();

        } catch (Exception e) {
            e.printStackTrace();
            message.setText("Error loading from DB");   
        }
    }

    private void showLoginScreen(Stage primaryStage) {
    VBox loginVBox = new VBox(10);
    loginVBox.setAlignment(Pos.CENTER);
    loginVBox.setPadding(new Insets(20));
    loginVBox.setStyle("-fx-background-color: #1b858bff;");

    Label titleLabel = new Label("Login to Finance Tracker");
    titleLabel.setFont(Font.font("Times New Roman", FontWeight.BOLD, 30));
    titleLabel.setStyle("-fx-text-fill: #ffffffff;");

    TextField usernameField = new TextField();
    usernameField.setPromptText("Username");
    usernameField.setMaxWidth(200);

    PasswordField passwordField = new PasswordField();
    passwordField.setPromptText("Password");
    passwordField.setMaxWidth(200);

    Label errorLabel = new Label();
    errorLabel.setStyle("-fx-text-fill: #cb2715ff; -fx-font-weight: bold;");
    errorLabel.setFont(Font.font(16));

    Button loginButton = new Button("Login");
    loginButton.setMaxWidth(120);

    Button registerButton = new Button("Register");
    registerButton.setMaxWidth(120);

    HBox buttonHBox = new HBox(10, loginButton, registerButton);
    buttonHBox.setAlignment(Pos.CENTER);

    loginVBox.getChildren().addAll(titleLabel, usernameField, passwordField, errorLabel, buttonHBox);

    Scene loginScene = new Scene(loginVBox, 400, 300);
    primaryStage.setScene(loginScene);
    primaryStage.setTitle("Login - Personal Finance Tracker");
    primaryStage.centerOnScreen();
    primaryStage.show();

    loginButton.setOnAction(e -> {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Username and password cannot be empty");
            return;
        }

        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement statement = connection.prepareStatement("SELECT password FROM userTable WHERE username = ?")) {
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String storedPassword = resultSet.getString("password");
                String hashedInputPassword = hashPassword(password);

                if (storedPassword.equals(hashedInputPassword)) {
                    errorLabel.setText("");
                    loggedInUsername = username;
                    startMainScene(primaryStage);
                } else {
                    errorLabel.setText("Invalid username or password");
                }
            } else {
                errorLabel.setText("Invalid username or password");
            }

            resultSet.close();

        } catch (Exception ex) {
            ex.printStackTrace();
            errorLabel.setText("Error accessing database");
        }
    });

    registerButton.setOnAction(e -> {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Username and password cannot be empty");
            return;
        }

        try (Connection connection = DriverManager.getConnection(url);
            PreparedStatement checkStatement = connection.prepareStatement("SELECT username FROM userTable WHERE username = ?");
            PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO userTable (username, password) VALUES (?, ?)")) {
            checkStatement.setString(1, username);
            ResultSet resultSet = checkStatement.executeQuery();

            if (resultSet.next()) {
                errorLabel.setText("Username already exists");
                resultSet.close();
                return;
            }
            resultSet.close();

            String hashedPassword = hashPassword(password);

            insertStatement.setString(1, username);
            insertStatement.setString(2, hashedPassword);
            insertStatement.executeUpdate();

            errorLabel.setText("Registration successful! Please login.");

            usernameField.clear();
            passwordField.clear();

        } catch (Exception ex) {
            ex.printStackTrace();
            errorLabel.setText("Error during registration");
        }
    });
}

    private String hashPassword(String password) {
    try {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(password.getBytes());
        StringBuilder hexString = new StringBuilder();

        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);

            if (hex.length() == 1) { 
                hexString.append('0');
            }

            hexString.append(hex);
        }

        return hexString.toString();

    } catch (NoSuchAlgorithmException e) {
        e.printStackTrace();
        return password;
    }
}

    private void exportDataToCSV(Stage primaryStage) {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Save Finance Data");
    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv"));
    File selectedFile = fileChooser.showSaveDialog(primaryStage);

    if (selectedFile != null) {
        try (FileWriter file = new FileWriter(selectedFile);
             Connection connection = DriverManager.getConnection(url);
            
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM financeTrackerTable WHERE username = ?")) {
            statement.setString(1, loggedInUsername);
            ResultSet resultSet = statement.executeQuery();

            file.write("Transaction type, Amount, Date and Time\n");

            while (resultSet.next()) {
                String type = resultSet.getString("typeOfTransaction");
                double amount = resultSet.getDouble("amount");
                String dateAndTime = resultSet.getString("dateAndTime");
                String line = String.format("%s, %.2f, %s\n", type, amount, dateAndTime);
                file.write(line);
            }

            message.setText("Data exported successfully!");

        } catch (IOException ex) {
            ex.printStackTrace();
            message.setText("Error writing to file.");
        } catch (Exception exc) {
            exc.printStackTrace();
            message.setText("Error accessing database.");
        }
    } else {
        message.setText("Export canceled.");
    }
}

}
