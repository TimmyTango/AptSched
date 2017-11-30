package edu.wgu.thami19;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public final class LogInStage extends Stage {
    
    ResourceBundle rb;
    Text sceneTitle, actionTarget;
    Label userName, password, language;
    TextField userTextField;
    PasswordField passwordField;
    Button btn;
    HBox hbBtn;
    ChoiceBox languageChoice;
    GridPane grid;
    Scene scene;
    MyApp app;
    
    public LogInStage() {
        
        grid = new GridPane();
        
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25));
        grid.setMinSize(320, 200);
        grid.setMaxSize(640, 400);
        
        app = MyApp.getAppInstance();
                
        sceneTitle = new Text();
        sceneTitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        userName = new Label();
        userTextField = new TextField();
        password = new Label();
        passwordField = new PasswordField();
        language = new Label();
        languageChoice = new ChoiceBox(FXCollections.observableArrayList(
                "English", "Español"));
        languageChoice.setMaxWidth(Double.MAX_VALUE);
        btn = new Button();
        hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
        hbBtn.getChildren().add(btn);
        actionTarget = new Text();
                
        if(Locale.getDefault().getLanguage().equals(new Locale("es").getLanguage())) {
            languageChoice.getSelectionModel().selectLast();
            generateLabels(new Locale("es"));
        } else {
            languageChoice.getSelectionModel().selectFirst();
            generateLabels(new Locale("en"));
        }
        
        grid.add(sceneTitle, 0, 0, 2, 1);        
        grid.add(userName, 0, 2);        
        grid.add(userTextField, 1, 2);        
        grid.add(password, 0, 3);        
        grid.add(passwordField, 1, 3);        
        grid.add(language, 0, 4);
        grid.add(languageChoice, 1, 4);        
        grid.add(hbBtn, 1, 5);
        grid.add(actionTarget, 0, 1, 2, 1);
        
        btn.setOnAction(event -> loginAction());
        
        languageChoice.getSelectionModel().selectedIndexProperty().addListener(
            (ObservableValue<? extends Number> observable, Number oldIndex,
                    Number newIndex) -> {
                if(newIndex.equals(0))
                    generateLabels(new Locale("en", "US"));
                else
                    generateLabels(new Locale("es", "MX"));
            }
        );
        
        scene = new Scene(grid);
        
        setTitle("Log In");
        setScene(scene);
        setResizable(false);
    }
    
    private void generateLabels(Locale locale) {
        rb = ResourceBundle.getBundle("Sched", locale);
        sceneTitle.setText(rb.getString("welcome"));
        userName.setText(rb.getString("username"));
        password.setText(rb.getString("password"));
        language.setText(rb.getString("language"));
        btn.setText(rb.getString("login"));
        actionTarget.setText("");
    }
    
    private void loginAction() {
        try {
            app.getDB().checkLogin(userTextField.getText(),
                    passwordField.getText());
        } catch(SQLException e) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setContentText(e.getMessage());
            alert.showAndWait();
            return;
        } catch(InvalidLoginException e) {
            actionTarget.setFill(Color.FIREBRICK);
            actionTarget.setText(rb.getString("badmsg"));
            return;
        }
        
        addToLog(userTextField.getText());
        
        hide();
        app.setUser(userTextField.getText());
        app.showMainScreen();
    }
    
    private void addToLog(String user) {
        try {
            File file = new File("log.txt");
            if(!file.exists())
                file.createNewFile();
            
            try (
                FileWriter fw = new FileWriter(file, true);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter log = new PrintWriter(bw);) {
            
            LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
            String timestamp = now.format(DateTimeFormatter.ofPattern(
                    "yyyy-MM-dd hh:mm:dd a"));
            log.println("Login: " + user + " ["+timestamp+"]");
        } catch(IOException e) {
            app.alert("Could not write to log file:\n"+e.getMessage());
        }    
        } catch(IOException e) {
            app.alert("Could not create log file: " + e.getMessage());
        }   
    }
}