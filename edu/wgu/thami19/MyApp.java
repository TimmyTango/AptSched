/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.wgu.thami19;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

/**
 *
 * @author thamilton
 */
public class MyApp extends Application {
    
    private Database db;
    private Stage primaryStage;
    private String user;
    private ZoneOffset timeZone;
    private List<Reminder> reminders;
    private Timer reminderReload;
    private Reports reports;
    private static MyApp app = null; 
    private static final boolean DEBUG = false;
    
    @Override
    public void start(Stage pStage) {        
        
        Properties prop = new Properties();
        InputStream input = null;
        
        try {
            input = getClass().getClassLoader().getResourceAsStream("config.properties");
            prop.load(input);
            
            String dbAddress = prop.getProperty("dbaddress");
            String dbName = prop.getProperty("dbname");
            String dbUser = prop.getProperty("dbuser");
            String dbPass = prop.getProperty("dbpass");
            
            db = new Database(dbAddress, dbName, dbUser, dbPass);
        } catch(Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(e.getMessage());
            alert.showAndWait();
            Platform.exit();
        } finally {
            if(input!=null) {
                try {
                    input.close();
                } catch(IOException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText(e.getMessage());
                    alert.showAndWait();
                    Platform.exit();
                }
            }
        }
        
        //timeZone = ZoneOffset.UTC;
        timeZone = ZoneOffset.systemDefault().getRules().getOffset(Instant.now());
        reports = new Reports();
        
        if(DEBUG) {
            user = "thamilton";
            showMainScreen();
            reports.appointmentCountPerMonth("report1.txt");
            reports.userSchedule("report2.txt");
            reports.customerList("report3.txt");
        } else {
            primaryStage = new LogInStage();
            primaryStage.show();
        }
    }
    
    public Reports getReports() {
        return reports;
    }
    
    public void showMainScreen() {
        
        primaryStage = new MainStage();
        primaryStage.show();
        if(DEBUG) {
            LocalDateTime now = LocalDateTime.now().plusSeconds(10);
            Reminder r = new Reminder("Test Reminder", now);
        } else {
            loadReminders();
        }   
    }
    
    public void loadReminders() {
        try {
            reminders = db.getReminders();
        } catch(SQLException e) {
            alert(e.getMessage());
        }
        reminderReload = new Timer();
        reminderReload.schedule(new TimerTask() {
            @Override
            public void run() {
                loadReminders();
            }
        }, Duration.ofHours(1).toMillis());
    }
    
    public Database getDB() {
        return db;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
    
    public Stage getPrimaryStage() {
        return primaryStage;
    }
    
    public MainStage getMainStage() {
        return (MainStage)primaryStage;
    }
    
    public ZoneOffset getTimeZone() {
        return timeZone;
    }
    
    public void toggleTimeZone() {
        if(timeZone.equals(ZoneOffset.UTC)) {
            timeZone = ZoneOffset.systemDefault().getRules().getOffset(Instant.now());
        } else {
            timeZone = ZoneOffset.UTC;
        }
    }
    
//    public void setTimeZone(ZoneOffset zone) {
//        timeZone = zone;
//    }
    
    public LocalTime convertTime(String timeStr) {
        String pattern = "hh:mm a";
        DateTimeFormatter timef = DateTimeFormatter.ofPattern(pattern);
        
        return LocalTime.parse(timeStr, timef);
    }
    
    public String convertTimeString(LocalTime time) {
        String pattern = "hh:mm a";
        DateTimeFormatter timef = DateTimeFormatter.ofPattern(pattern);
        
        return time.format(timef);
    }
    
    public List<String> generateHalfHour() {
        List<String> list = new ArrayList<>();
        String pattern = "hh:mm a";
        DateTimeFormatter timef = DateTimeFormatter.ofPattern(pattern);
        
        for(int i = 0; i < 48; i++) {
            LocalTime time = LocalTime.MIDNIGHT;
            time = time.plusMinutes(i * 30);
            list.add(time.format(timef));
        }
        
        return list;
    }
    
    public void alert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public void info(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public static void main(String[] args) {
        if(app == null)
            app = new MyApp();
        app.launch(args);
    }
    
    public static MyApp getAppInstance() {
        return app;
    }
}
