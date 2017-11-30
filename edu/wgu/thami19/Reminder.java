package edu.wgu.thami19;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.scene.control.Alert;

/**
 *
 * @author thamilton
 */
public class Reminder {
    
    public String name;
    public Timer timer;
    private MyApp app;
    public LocalDateTime trigger;
    
    public Reminder(String name, LocalDateTime trigger) {
        app = MyApp.getAppInstance();
        this.name = name;
        this.trigger = trigger;
        timer = new Timer(name, true);
        
        Duration wait = Duration.between(LocalDateTime.now(), trigger);
        
        timer.schedule(new TimerTask() {
            @Override
            public void run() {Platform.runLater(() -> show());}
        },wait.toMillis());
    }
    
    private void show() {
        String time = trigger.toLocalTime().plusMinutes(15)
                .format(DateTimeFormatter.ofPattern("hh:mm a"));
        String message = name + " will start at " + time;
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(message);
        alert.setTitle("Reminder - " + name);
        alert.showAndWait();
    }
    
}
