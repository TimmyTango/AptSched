package edu.wgu.thami19;

import edu.wgu.thami19.models.Customer;
import edu.wgu.thami19.models.Appointment;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

/**
 *
 * @author thamilton
 */
public class WeeklyCalendarView extends GridPane {
    private MyApp app;
    private LocalDate firstDay;
    private LocalDate lastDay;
    private Customer selectedCustomer;
    private List<Appointment> appointments;
    private Map<DayOfWeek, List<Appointment>> dailyAppointments;
    
    public WeeklyCalendarView(Customer c, LocalDate day) {
        app = MyApp.getAppInstance();
        selectedCustomer = c;
        
        setWeekRange(day);
    }
    
    public void setWeekRange(LocalDate startDay) {
        firstDay = startDay.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        lastDay = startDay.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        
        buildUI();
    }
    
    public void setCustomer(Customer c) {
        selectedCustomer = c;
        buildUI();
    }
    
    public void buildUI() {
        
        loadAppointments();
        
        getChildren().clear();
        
        LocalTime hour = LocalTime.MIDNIGHT;
                
        add(new TimeBlank(), 0, 0);
        for(int i = 1; i < 48; i+=2) {
            add(new TimeLabel(hour), 0, i);
            add(new TimeBlank(), 0, i+1);
            hour = hour.plusHours(1);
        }
        
        for(int row = 1; row < 48; row++) {
            for(int col = 1; col <= 7; col++) {
                add(new AppointmentBlank(), col, row);
            }
        }
        
        add(new DayOfWeekLabel("Monday"), 1, 0);
        add(new DayOfWeekLabel("Tuesday"), 2, 0);
        add(new DayOfWeekLabel("Wednesday"), 3, 0);
        add(new DayOfWeekLabel("Thursday"), 4, 0);
        add(new DayOfWeekLabel("Friday"), 5, 0);
        add(new DayOfWeekLabel("Saturday"), 6, 0);
        add(new DayOfWeekLabel("Sunday"), 7, 0);
        
        for(Appointment a : appointments) {
            LocalDateTime starts = LocalDateTime.ofInstant(a.getStart(), app.getTimeZone());
            LocalDateTime ends = LocalDateTime.ofInstant(a.getEnd(), app.getTimeZone());
            int col = starts.getDayOfWeek().getValue();
            int firstRow = (starts.getHour() * 2) +1;
            if(starts.getMinute() != 0)
                firstRow++;
            int lastRow = (ends.getHour() * 2);
            if(ends.getMinute() != 0)
                lastRow++;
            
            // 30 minute slot
            if(firstRow == lastRow) {
                add(new TimeSlot(a.getTitle()), col, firstRow);
            } else if(firstRow == (lastRow - 1)) {
                add(new TimeSlot(a.getTitle()), col, firstRow, 1, 2);
            } else {
                int span = lastRow - firstRow + 1;
                add(new TimeSlot(a.getTitle()), col, firstRow, 1, span);
            }
        }
    }
    
    public void loadAppointments() {
        appointments = new ArrayList<>();
        dailyAppointments = new HashMap<>();
        
        Customer c = selectedCustomer;
        LocalDateTime start = LocalDateTime.of(firstDay, LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(lastDay, LocalTime.MAX);
        try {
            appointments = app.getDB().getAppointmentsForCalendar(c,
                    start.toInstant(app.getTimeZone()), 
                    end.toInstant(app.getTimeZone()));
        } catch(SQLException e) {
            app.alert(e.getMessage());
            return;
        }
        for(Appointment a : appointments) {
            LocalDateTime first = LocalDateTime.ofInstant(a.getStart(), app.getTimeZone());
            DayOfWeek dayOfWeek = first.getDayOfWeek();
            if(!dailyAppointments.containsKey(dayOfWeek))             
                dailyAppointments.put(dayOfWeek, new ArrayList<>());
            dailyAppointments.get(dayOfWeek).add(a);
        }
    }
    
    
    class DayOfWeekLabel extends Label {
        public DayOfWeekLabel(String dayOfWeek) {
            super(dayOfWeek);
            
            setMinWidth(75);
            setPrefWidth(125);
            setStyle("-fx-border-color: black; -fx-border-width: 1 1 1 0; " +
                    "-fx-background-color: #FFFFFF");
        }
    }
    
    class TimeSlot extends Label {
        public TimeSlot(String s) {
            super(s);
            
            setMinWidth(75);
            setPrefWidth(125);
            setMaxHeight(Double.MAX_VALUE);
            setStyle("-fx-background-color: #99ccff; -fx-border-color: #0066cc");
        }
    }
    
    class TimeBlank extends Label {
        public TimeBlank() {
            super("");
            
            setMinWidth(40);
            setPrefWidth(50);
            setStyle("-fx-border-color: black; -fx-border-width: 0 1 1 0; " +
                    "-fx-background-color: #FFFFFF");
        }
    }
    
    class AppointmentBlank extends Label {
        public AppointmentBlank() {
            super("");
            
            setMinWidth(75);
            setPrefWidth(125);
            setStyle("-fx-border-color: black; -fx-border-width: 0 1 1 0; " +
                    "-fx-background-color: #FFFFFF");
        }
    }
    
    class TimeLabel extends Label {
        public TimeLabel(LocalTime t) {
            super(t.format(DateTimeFormatter.ofPattern("ha")));
            
            setMinWidth(40);
            setPrefWidth(50);
            setStyle("-fx-border-color: black; -fx-border-width: 1 1 1 0; " +
                    "-fx-background-color: #FFFFFF");
        }
    }
}
