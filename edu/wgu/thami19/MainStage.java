package edu.wgu.thami19;

import edu.wgu.thami19.models.Customer;
import edu.wgu.thami19.models.Appointment;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainStage extends Stage {
    
    BorderPane bPane;
    Scene scene;
    MyApp app;
    CustomerTreeView ctv;
    Customer selectedCustomer;
    MonthlyCalendarView monthlyView;
    WeeklyCalendarView weeklyView;
    ScrollPane scroll;
    LocalDate viewDate, firstDay, lastDay;
    private boolean editMode;
    private boolean showUtcTime;
    private boolean showMonthlyView;
    
    public MainStage() {
        
        this.app = MyApp.getAppInstance();
        
        bPane = new BorderPane();        
        scene = new Scene(bPane);
        ctv = new CustomerTreeView();        
        selectedCustomer = new Customer();
        
        LocalDate today = LocalDate.now();
//        viewDate = LocalDate.of(today.getYear(), today.getMonth(), 1);
        viewDate = today;        
        firstDay = viewDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        lastDay = viewDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        monthlyView = new MonthlyCalendarView(selectedCustomer, viewDate.getMonth(), viewDate.getYear());
        scroll = new ScrollPane();
        weeklyView = new WeeklyCalendarView(selectedCustomer, today);
        editMode = false;
        showUtcTime = false;
        showMonthlyView = false;
        
        scroll.setContent(weeklyView);
        
        
        bPane.setTop(buildTopButtons());        
        bPane.setCenter(scroll);
        bPane.setLeft(ctv);
        
        setTitle("I'm Booked");
        setScene(scene);
        setResizable(true);
        setMaximized(true);
    }
    
    public void showCustomerPanel(Customer c) {
        if(!editMode)
            bPane.setRight(new CustomerDetailsView(c));
    }
    
    public void showAppointmentPanel(Appointment a) {
        if(!editMode)
            bPane.setRight(new AppointmentView(a));
    }
    
    public void lockEdit() {
        editMode = true;
    }
    
    public void freeEdit(boolean needRefresh) {
        editMode = false;
        if(needRefresh) {
            ctv.rebuildTree();
            monthlyView.buildUI();
            weeklyView.buildUI();
            bPane.setRight(null);
        }
    }
    
    public boolean isLocked() {
        return editMode;
    }
    
    public Scene getMyScene() {
        return scene;
    }
    
    private void toggleCalendarView() {
        showMonthlyView = !showMonthlyView;
        if(showMonthlyView)
            bPane.setCenter(monthlyView);
        else
            bPane.setCenter(weeklyView);
        bPane.setTop(buildTopButtons());
    }
       
    
    private void changeViewDate(LocalDate newDate) {
        weeklyView.setWeekRange(newDate);
        monthlyView.setDate(newDate.getMonth(), newDate.getYear());
    }
    
    private void changeCustomerView(Customer c) {
        selectedCustomer = c;
        weeklyView.setCustomer(c);
        monthlyView.setCustomer(c);
    }
    
    private HBox buildTopButtons() {
        Image newCustImg = new Image(getClass().getResourceAsStream("contact-new.png"));
        Hyperlink newCustHl = new Hyperlink("New Customer", new ImageView(newCustImg));
        newCustHl.setContentDisplay(ContentDisplay.TOP);
        newCustHl.setBorder(Border.EMPTY);
        newCustHl.setOnAction(e -> newCustomer(e));
        
        Image newAptImg = new Image(getClass().getResourceAsStream("office-calendar.png"));
        Hyperlink newAptHl = new Hyperlink("New Appointment", new ImageView(newAptImg));
        newAptHl.setContentDisplay(ContentDisplay.TOP);
        newAptHl.setBorder(Border.EMPTY);
        newAptHl.setOnAction(e -> newAppointment(e));
        
        Image timeZoneImg = new Image(getClass().getResourceAsStream("appointment-new.png"));
        Hyperlink timeZoneHl = new Hyperlink("Show UTC Time", new ImageView(timeZoneImg));
        timeZoneHl.setContentDisplay(ContentDisplay.TOP);
        timeZoneHl.setBorder(Border.EMPTY);
        timeZoneHl.setOnAction(e -> toggleTimeZone(e)); 
        if(showUtcTime)
            timeZoneHl.setText("Show Local Time");
        
        Image reportImg = new Image(getClass().getResourceAsStream("edit-find.png"));
        Hyperlink apptTypeHl = new Hyperlink("Report: Appt. Count", new ImageView(reportImg));
        apptTypeHl.setContentDisplay(ContentDisplay.TOP);
        apptTypeHl.setBorder(Border.EMPTY);
        apptTypeHl.setOnAction(e -> app.getReports().appointmentCountPerMonth("Appointments Count.txt"));
        
        Hyperlink userSchedule = new Hyperlink("Report: User Schedule", new ImageView(reportImg));
        userSchedule.setContentDisplay(ContentDisplay.TOP);
        userSchedule.setBorder(Border.EMPTY);
        userSchedule.setOnAction(e -> app.getReports().userSchedule("User Schedule.txt"));
        
        
        Hyperlink customerList = new Hyperlink("Report: Customer List", new ImageView(reportImg));
        customerList.setContentDisplay(ContentDisplay.TOP);
        customerList.setBorder(Border.EMPTY);
        customerList.setOnAction(e -> app.getReports().appointmentCountPerMonth("Customer List.txt"));
        
        
        
        Button toggleCalendar = new Button("Toggle calendar view");
        toggleCalendar.setOnAction(e -> toggleCalendarView());
        
        ChoiceBox<Customer> customerChoice = new ChoiceBox<>();
        try {
            ObservableList<Customer> customers = FXCollections.observableArrayList(
                    app.getDB().getCustomers());
            customerChoice.setItems(customers);
            for(Customer c : customers) {
                if(c.getId() == selectedCustomer.getId()) {
                    customerChoice.getSelectionModel().select(c);
                    break;
                }
            }     
        } catch(SQLException e) {
            app.alert(e.getMessage());
        }
        
        customerChoice.setOnAction(e -> changeCustomerView(customerChoice.getValue()));
                
        HBox datePickerBox = new HBox(5);
        datePickerBox.setAlignment(Pos.CENTER_LEFT);
        
        if(showMonthlyView) {
            ChoiceBox<Month> months = new ChoiceBox<>(
                    FXCollections.observableArrayList(Month.values()));
            ChoiceBox<Integer> years = new ChoiceBox<>(
                    FXCollections.observableArrayList(2016, 2017, 2018));
            datePickerBox.getChildren().addAll(months, years);
            months.setOnAction(e -> {
                viewDate = LocalDate.of(years.getValue(), months.getValue(), 1);
                changeViewDate(viewDate);
            });
            years.setOnAction(e -> {
                viewDate = LocalDate.of(years.getValue(), months.getValue(), 1);
                changeViewDate(viewDate);
            });
            months.getSelectionModel().select(viewDate.getMonth());
            years.getSelectionModel().select((Integer)viewDate.getYear());
        } else {
            Button prev = new Button("<");
            Button next = new Button(">");
            VBox dateRange = new VBox(5);
            firstDay = viewDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            lastDay = viewDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
            Label start = new Label(firstDay.format(DateTimeFormatter.ofPattern("MM/dd yy")));
            Label end = new Label(lastDay.format(DateTimeFormatter.ofPattern("MM/dd yy")));
            dateRange.getChildren().addAll(start, end);
            dateRange.setAlignment(Pos.CENTER);
            datePickerBox.getChildren().addAll(prev, dateRange, next);
            
            prev.setOnAction(e -> {
                viewDate = viewDate.minusDays(7);
                firstDay = viewDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                lastDay = viewDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
                start.setText(firstDay.format(DateTimeFormatter.ofPattern("MM/dd yy")));
                end.setText(lastDay.format(DateTimeFormatter.ofPattern("MM/dd yy")));
                changeViewDate(viewDate);
            });
            
            next.setOnAction(e -> {
                viewDate = viewDate.plusDays(7);
                firstDay = viewDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                lastDay = viewDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
                start.setText(firstDay.format(DateTimeFormatter.ofPattern("MM/dd yy")));
                end.setText(lastDay.format(DateTimeFormatter.ofPattern("MM/dd yy")));
                changeViewDate(viewDate);
            });
        }
        
        HBox hb = new HBox(10);
        hb.setPadding(new Insets(10));
        hb.setAlignment(Pos.CENTER_LEFT);
        hb.getChildren().addAll(newCustHl, newAptHl, timeZoneHl, apptTypeHl,
                userSchedule, customerList, toggleCalendar,
                datePickerBox, customerChoice);
        
        return hb;
    }
    
    private void newCustomer(ActionEvent e) {
        ((Hyperlink)e.getSource()).setVisited(false);
        if(!editMode)
            bPane.setRight(new CustomerDetailsView());
    }
    
    private void newAppointment(ActionEvent e) {
        ((Hyperlink)e.getSource()).setVisited(false);
        if(!editMode)
            bPane.setRight(new AppointmentView());
    }
    
    private void toggleTimeZone(ActionEvent e) {
        Hyperlink link = (Hyperlink)e.getSource();
        link.setVisited(false);
        app.toggleTimeZone();
        showUtcTime = !showUtcTime;
        if(showUtcTime)
            link.setText("Show Local Time");
        else
            link.setText("Show UTC Time");
        
        if(bPane.getRight() != null && 
                bPane.getRight().getClass().equals(AppointmentView.class)) {
            AppointmentView av = (AppointmentView)bPane.getRight();
            av.rebuildUI();
        }
        
        monthlyView.buildUI();
            weeklyView.buildUI();
    }
    
}
