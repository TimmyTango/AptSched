package edu.wgu.thami19;

import edu.wgu.thami19.models.Customer;
import edu.wgu.thami19.models.Appointment;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Border;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class AppointmentView extends GridPane {
    
    private MyApp app;
    private Appointment apt;
    private Text sceneTitle, error;
    private Label titleLabel, locationLabel, contactLabel, urlLabel, startLabel,
            endLabel, descriptionLabel, customerLabel;
    private Hyperlink customerLink;
    private ChoiceBox<Customer> customerChoice;
    private TextField title, location, contact, url;
    private DatePicker startDate;
    private ChoiceBox<String> startTime, endTime;
    private TextArea description;
    private Button confirm, cancel, edit;
    private CheckBox reminder;
    
    public AppointmentView() {
        this(new Appointment());
    }
    
    public AppointmentView(Appointment apt) {
        this.app = MyApp.getAppInstance();
        this.apt = apt;
        
        if(apt.getId() < 0) {
            createUI();
            enableEdit();
        } else {
            try {
                Customer c = app.getDB().getCustomer(apt.getCustomer().getId());
                apt.setCustomer(c);
            } catch (SQLException e) {
                app.alert(e.getMessage());
            }
            createUI();
        }
    }
    
    public void rebuildUI() {
        if(app.getMainStage().isLocked()) {
            app.info("Please confirm or cancel edit for appointment.");
        } else {
            createUI();
        }
    }
    
    
    private void createUI() {        
        setHgap(10);
        setVgap(10);
        setPadding(new Insets(0, 25, 25, 25));
        
        getChildren().clear();
        
        sceneTitle = new Text("Appointment");
        sceneTitle.setFont(Font.font("Arial", FontWeight.NORMAL, 20));
        error = new Text();
        error.setFill(Color.FIREBRICK);
        titleLabel = new Label("Title");
        locationLabel = new Label("Location");
        contactLabel = new Label("Contact");
        urlLabel = new Label("URL");
        startLabel = new Label("Starts at");
        endLabel = new Label("Ends at");
        descriptionLabel = new Label("Description");
        customerLabel = new Label("Customer");
        
        customerLink = new Hyperlink(apt.getCustomer().getName());
        customerLink.setBorder(Border.EMPTY);
        customerLink.setOnAction(e -> goToCustomer(e));
        
        customerChoice = new ChoiceBox<>();
                
        title = new TextField(apt.getTitle());
        title.setEditable(false);
        location = new TextField(apt.getLocation());
        location.setEditable(false);
        contact = new TextField(apt.getContact());
        contact.setEditable(false);
        url = new TextField(apt.getUrl());
        url.setEditable(false);
        
        LocalDateTime start = LocalDateTime.ofInstant(apt.getStart(), app.getTimeZone());
        LocalDateTime end = LocalDateTime.ofInstant(apt.getEnd(), app.getTimeZone());
        
        startDate = new DatePicker(start.toLocalDate());
        startDate.setDisable(true);
        startDate.setMaxWidth(125);
        
        startDate.setOnAction(e -> {
            if(startDate.getValue().isBefore(LocalDate.now())) {
                error.setFill(Color.ORANGE);
                error.setText("Start date is in the past");
            } else {
                error.setText("");
            }
        });
        
        ObservableList<String> times = FXCollections.observableArrayList(app.generateHalfHour());
        
        startTime = new ChoiceBox<>(times);
        startTime.setDisable(true);
        selectTime(startTime, start);
        endTime = new ChoiceBox<>(times);
        endTime.setDisable(true);
        selectTime(endTime, end);
        
        description = new TextArea(apt.getDescription());
        description.setMaxWidth(300);
        description.setEditable(false);
        
        confirm = new Button("Ok");
        confirm.setMinWidth(75);
        confirm.setVisible(false);
        cancel = new Button("Cancel");
        cancel.setMinWidth(75);
        cancel.setVisible(false);
        edit = new Button("Edit");
        edit.setMinWidth(75);
        
        confirm.setOnAction(e -> confirmAction());
        cancel.setOnAction(e -> cancelAction());
        edit.setOnAction(e -> enableEdit());    
        
        
        HBox hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
        hbBtn.getChildren().addAll(edit, confirm, cancel);
        
        HBox hbStart = new HBox(5);
        hbStart.setAlignment(Pos.CENTER_LEFT);
        hbStart.getChildren().addAll(startDate, startTime);
        
        HBox hbEnd = new HBox(5);
        hbEnd.setAlignment(Pos.CENTER_LEFT);
        hbEnd.getChildren().addAll(endTime);
        
        reminder = new CheckBox("15 minute reminder");
        reminder.setDisable(true);
        try {
            reminder.setSelected(app.getDB().isReminderSet(apt));
        } catch(SQLException e) {
            // safe to ignore
            System.err.println(e.getMessage());
        }
        
        int row = 0;
        add(sceneTitle, 0, row++, 2, 1);
        add(error, 0, row++, 2, 1);
        add(customerLabel, 0, row);
        add(customerLink, 1, row++);
        add(titleLabel, 0, row);
        add(title, 1, row++);
        add(locationLabel, 0, row);
        add(location, 1, row++);
        add(contactLabel, 0, row);
        add(contact, 1, row++);
        add(urlLabel, 0, row);
        add(url, 1, row++);
        add(startLabel, 0, row);
        add(hbStart, 1, row++);
        add(endLabel, 0, row);
        add(hbEnd, 1, row++);
        add(descriptionLabel, 0, row++);
        add(description, 0, row++, 3, 1);
        add(hbBtn, 1, row++); 
        add(reminder, 0, row++, 2, 1);
    }
    
    private void enableEdit() {
        app.getMainStage().lockEdit();
        sceneTitle.setText("*Appointment*");
        getChildren().remove(customerLink);
        try {
            ObservableList<Customer> customers = FXCollections.observableArrayList(
                    app.getDB().getCustomers());
            customerChoice.setItems(customers);
            for(Customer c : customers) {
                if(c.getId() == apt.getCustomer().getId()) {
                    customerChoice.getSelectionModel().select(c);
                    break;
                }
            }            
        } catch(SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
        customerChoice.setMaxWidth(Double.MAX_VALUE);
//        customerChoice.getSelectionModel().select(apt.getCustomer());
        add(customerChoice, 1, 2);
        title.setEditable(true);
        location.setEditable(true);
        contact.setEditable(true);
        url.setEditable(true);
        startDate.setDisable(false);
        startTime.setDisable(false);
        endTime.setDisable(false);
        description.setEditable(true);
        confirm.setVisible(true);
        cancel.setVisible(true);
        edit.setVisible(false);        
        reminder.setDisable(false);
    }
    
    private void confirmAction() {
        try {
            validateInput();
        } catch(AppointmentException e) {
            return;
        }
        
        LocalDateTime start = LocalDateTime.of(startDate.getValue(),
                app.convertTime(startTime.getValue()));
        
        LocalDateTime end = LocalDateTime.of(startDate.getValue(),
                app.convertTime(endTime.getValue()));
               
        Appointment updApt = new Appointment(apt.getId(), customerChoice.getValue(),
                title.getText(), description.getText(), location.getText(), contact.getText(),
                url.getText(), start.toInstant(app.getTimeZone()), end.toInstant(app.getTimeZone()));
        
        
        
        try {
            if(apt.getId() < 0) {
                app.getDB().insertAppointment(updApt);
            } else {
                app.getDB().updateAppointment(updApt);
            }
            
            if(reminder.isSelected())
                app.getDB().insertReminder(updApt);
            else
                app.getDB().removeReminder(updApt);
            
        } catch(SQLException e) {
            app.alert(e.getMessage());
            return;
        }
        
        app.getMainStage().freeEdit(true);
        createUI();
    }
    
    private void cancelAction() {
        app.getMainStage().freeEdit(false);
        createUI();
    }
    
    private void validateInput() throws AppointmentException {
        
        error.setFill(Color.FIREBRICK);
        error.setText("");
        
        if(customerChoice.getSelectionModel().isEmpty()) {
            throw new AppointmentException("You must select a customer");
        }
        
        if(title.getText().trim().length() < 1) {
            throw new OutOfBusinessHoursException("Appointment must have title");
        }
        
        LocalTime start = app.convertTime(startTime.getValue());
        LocalTime end = app.convertTime(endTime.getValue());
        if(start.isAfter(end) || start.equals(end)) {
            throw new OutOfBusinessHoursException("End time must be after start time");
        }
        
        if(startDate.getValue().getDayOfWeek() == DayOfWeek.SATURDAY
                || startDate.getValue().getDayOfWeek() == DayOfWeek.SUNDAY) {
            throw new OutOfBusinessHoursException("Business hours are Mon-Fri 8am-5pm");
        }
        
        LocalTime openHour = LocalTime.of(8, 0);
        LocalTime closeHour = LocalTime.of(17, 0);
        
        if(start.isBefore(openHour) || start.isAfter(closeHour) || end.isAfter(closeHour))
            throw new OutOfBusinessHoursException("Business hours are Mon-Fri 8am-5pm");
        
        try {
            LocalDateTime startDT = LocalDateTime.of(startDate.getValue(),
                app.convertTime(startTime.getValue()));
        
            LocalDateTime endDT = LocalDateTime.of(startDate.getValue(),
                    app.convertTime(endTime.getValue()));
            if(app.getDB().isAppointmentOverlapping(apt, startDT.toInstant(app.getTimeZone()), endDT.toInstant(app.getTimeZone())))
                throw new OverlappingAppointmentException("Appointment is overlapping with another appointment");
        } catch(SQLException e) {
            throw new AppointmentException(e.getMessage());
        }
    }

    private void goToCustomer(ActionEvent e) {
        app.getMainStage().showCustomerPanel(apt.getCustomer());
    }
    
    private void selectTime(ChoiceBox timeBox, LocalDateTime time) {
        String timeStr = app.convertTimeString(time.toLocalTime());
        
        timeBox.getSelectionModel().select(timeStr);
    }
    
    class AppointmentException extends Exception {
        public AppointmentException(String message) {
            super();
            error.setText(message);
        }
    }
    
    class OutOfBusinessHoursException extends AppointmentException {
        public OutOfBusinessHoursException(String message) {
            super(message);
        }
    }
    
    class OverlappingAppointmentException extends AppointmentException {
        public OverlappingAppointmentException(String message) {
            super(message);
        }
    }
}
