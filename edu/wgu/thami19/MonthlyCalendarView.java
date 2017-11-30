package edu.wgu.thami19;

import edu.wgu.thami19.models.Customer;
import edu.wgu.thami19.models.Appointment;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author thamilton
 */
public class MonthlyCalendarView extends GridPane {
    
    private MyApp app;
    private Year currentYear;
    private Month currentMonth;
    private DayOfWeek firstDay;
    private LocalDate firstOfMonth;
    private Customer selectedCustomer;
    private List<Appointment> appointments;
    private Map<Integer, List<Appointment>> dailyAppointments;
    private AppointmentListPopup popup;
    
    public MonthlyCalendarView(Customer c, Month month, int year) {
        app = MyApp.getAppInstance();
        currentYear = Year.of(year);
        currentMonth = month;
        selectedCustomer = c;
       
        appointments = new ArrayList<>();
        dailyAppointments = new HashMap<>();
        
        setDate(month, year);
    }
    
    
    public void setDate(Month month, int year) {
        currentMonth = month;
        currentYear = Year.of(year);
        firstOfMonth= LocalDate.of(currentYear.getValue(), currentMonth, 1);
        firstDay = firstOfMonth.getDayOfWeek();
        buildUI();
    }
    
    public void setCustomer(Customer c) {
        selectedCustomer = c;
        buildUI();
    }
    
    public void buildUI() {
        int maxDays = firstOfMonth.lengthOfMonth();
        
        getChildren().clear();
        appointments.clear();
        dailyAppointments.clear();
        
        loadAppointments();
        
        add(new DayOfWeekLabel("Monday"), 0, 0);
        add(new DayOfWeekLabel("Tuesday"), 1, 0);
        add(new DayOfWeekLabel("Wednesday"), 2, 0);
        add(new DayOfWeekLabel("Thursday"), 3, 0);
        add(new DayOfWeekLabel("Friday"), 4, 0);
        add(new DayOfWeekLabel("Saturday"), 5, 0);
        add(new DayOfWeekLabel("Sunday"), 6, 0);
        
        for(int i = 1; i < firstDay.getValue(); i++) {
            add(new Label(""), i - 1, 1);
        }
        
        int row = 1;        
        int col = firstDay.getValue() - 2;
        for(int i = 1; i <= maxDays; i++) {
            col++;
            if(col == 7) {
                col = 0;
                row++;
            }            
            DaySquare day = new DaySquare(i);
            add(day, col, row);
        }
    }
    
    public void loadAppointments() {
        LocalDateTime start = LocalDateTime.of(firstOfMonth, LocalTime.MIN);
        LocalDateTime end = start.plusMonths(1);
        try {
            appointments = app.getDB().getAppointmentsForCalendar(selectedCustomer,
                    start.toInstant(app.getTimeZone()), 
                    end.toInstant(app.getTimeZone()));
        } catch(SQLException e) {
            app.alert(e.getMessage());
            return;
        }
        for(Appointment a : appointments) {
            LocalDateTime first = LocalDateTime.ofInstant(a.getStart(), app.getTimeZone());
            LocalDateTime last = LocalDateTime.ofInstant(a.getEnd(), app.getTimeZone());
            int startDate = first.getDayOfMonth();
            int endDate = last.getDayOfMonth();
            for(int date = startDate; date <= endDate; date++) {
                if(!dailyAppointments.containsKey(date))             
                    dailyAppointments.put(date, new ArrayList<>());
                dailyAppointments.get(date).add(a);
            }
        }
    }
    
    class DaySquare extends GridPane {
        public DaySquare(int date) {
            Label dateLabel = new Label(Integer.toString(date));
            add(dateLabel, 0, 0);
            Text appointmentCount = new Text();
            appointmentCount.setFont(Font.font("Tahoma", FontWeight.NORMAL, 60));
            if(dailyAppointments.containsKey(date)) {
                appointmentCount.setText(Integer.toString(
                        dailyAppointments.get(date).size()));
            }
            HBox hb = new HBox();
            hb.setAlignment(Pos.CENTER);
            hb.setMinWidth(95);
            hb.setPrefWidth(145);
            hb.setMinHeight(70);
            hb.setPrefHeight(120);
            hb.getChildren().add(appointmentCount);
            add(hb, 0, 1);
            
            setStyle("-fx-border-color: black; -fx-border-width: 0 1 1 0; " +
                    "-fx-background-color: #FFFFFF");
            setMinWidth(100);
            setPrefWidth(150);
            setMinHeight(75);
            setPrefHeight(125);
            setVgap(3);
            setPadding(new Insets(5));
            
            setOnMouseClicked(e -> showAppointments(date));
        }
        
        
        private void showAppointments(int date) {
            if(dailyAppointments.containsKey(date)) {
                popup =  new AppointmentListPopup(date);
                popup.showAndWait();
            }
        }
    }
    
    class AppointmentListPopup extends Stage {
        Scene scene;
        VBox vbox;
        DateTimeFormatter dtf;
        
        public AppointmentListPopup(int date) {
            vbox = new VBox();
            dtf = DateTimeFormatter.ofPattern("MMM d, yy hh:mm a");
            
            System.out.println("Date: " + date);
            
            if(dailyAppointments.containsKey(date)) {
                for(Appointment a : dailyAppointments.get(date)) {
                    Text title = new Text(a.getTitle());
                    title.setFont(Font.font("Tahoma", FontWeight.BOLD, 24));
                    
                    Font small = Font.font("Tahoma", 18);
                    
                    LocalDateTime start = LocalDateTime.ofInstant(a.getStart(), app.getTimeZone());
                    Text startLbl = new Text("Start:");
                    startLbl.setFont(small);
                    Text startTxt = new Text(start.format(dtf));
                    startTxt.setFont(small);
                    
                    LocalDateTime end = LocalDateTime.ofInstant(a.getEnd(), app.getTimeZone());
                    Text endLbl = new Text("End:");
                    endLbl.setFont(small);
                    Text endTxt = new Text(end.format(dtf));
                    endTxt.setFont(small);
                    
                    GridPane grid = new GridPane();
                    grid.setAlignment(Pos.CENTER);
                    grid.setHgap(10);
                    grid.setVgap(10);
                    grid.setPadding(new Insets(25));
                    grid.add(title, 0, 0, 2, 1);
                    grid.add(startLbl, 0, 1);
                    grid.add(startTxt, 1, 1);
                    grid.add(endLbl, 0, 2);
                    grid.add(endTxt, 1, 2);
                    vbox.getChildren().add(grid);
                }
            }
           
            scene = new Scene(vbox);
            setScene(scene);
            setTitle("Appointments");
            setResizable(false);
            initModality(Modality.APPLICATION_MODAL);
        }
    }
    
//    class DaySquare extends GridPane {       
//        
//        public DaySquare(int date) {
//            Label dateLabel = new Label(Integer.toString(date));
//            add(dateLabel, 0, 0);
//            if(dailyAppointments.containsKey(date)) {
//                List<Appointment> appts = dailyAppointments.get(date);
//                int max = 5;
//                if(appts.size() > max) {
//                    max = 4;
//                    add(new Label("+" + (appts.size() - max) + " more"), 0, 5);
//                } else if(appts.size() < max) {
//                    max = appts.size();
//                }
//                for(int i = 0; i < max; i++) {
//                    LocalDateTime ldt = LocalDateTime.ofInstant(appts.get(i).getStart(), app.getTimeZone());
//                    String label = ldt.getHour() + " - " + appts.get(i).toString();
//                    add(new Label(label), 0, i+1);
//                }
//            }
//            
//            setStyle("-fx-border-color: black; -fx-border-width: 0 1 1 0; " +
//                    "-fx-background-color: #FFFFFF");
//            setMinWidth(100);
//            setPrefWidth(150);
//            setVgap(3);
//            setPadding(new Insets(5));
//        }       
//    }
    
    class DayOfWeekLabel extends Label {
        public DayOfWeekLabel(String dayOfWeek) {
            super(dayOfWeek);
            
            setMinWidth(100);
            setPrefWidth(150);
            setStyle("-fx-border-color: black; -fx-border-width: 1 1 1 0; " +
                    "-fx-background-color: #FFFFFF");
        }
    }
}
