package edu.wgu.thami19;

import edu.wgu.thami19.models.Customer;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 *
 * @author thamilton
 */
public class Reports {
    
    MyApp app;
    
    public Reports() {
        app = MyApp.getAppInstance();
    }
    
    public void appointmentCountPerMonth(String fname) {
        try (PrintWriter report = createReport(fname)) {
            
            Map<String, List<AppointmentCount>> counts = app.getDB().getAppointmentsReport();
                    
            for(String month : counts.keySet()) {
                report.println(month);
                report.println("-----------");
                for(AppointmentCount ac : counts.get(month)) {
                    report.println(ac.customer + ": " + ac.count);
                }
                report.println("-----------");
            }     
            
        } catch(Exception e) {
            app.alert("Could not write to report:\n" + e.getMessage());
        }
    }
    
    
    
    public void userSchedule(String fname) {
        try (PrintWriter report = createReport(fname)) {
            
            Map<String, List<UserSchedule>> counts = app.getDB().getUserSchedule();
                    
            for(String user : counts.keySet()) {
                report.println(user);
                report.println("-----------");
                for(UserSchedule sched : counts.get(user)) {
                    report.println(sched);
                }
                report.println("-----------");
            }     
            
        } catch(Exception e) {
            app.alert("Could not write to report:\n" + e.getMessage());
        }
    }
    
    public void customerList(String fname) {
        try (PrintWriter report = createReport(fname)) {
            
            Customer[] custs = app.getDB().getCustomers();
            
            for(Customer c : custs) {
                report.println("-----------");
                report.println(c.getName());
                report.println("\tId: " + c.getId());
                report.println("\tActive: " + c.isActive());
            }
            
        } catch(Exception e) {
            app.alert("Could not write to report:\n" + e.getMessage());
        }
    }
    
    
    private  PrintWriter createReport(String fname) {
        try {
            File file = new File(fname);
            if(!file.exists())
                file.createNewFile();
            
            
            FileWriter fw = null;
            BufferedWriter bw = null;
            PrintWriter report = null;
            
            try {
                fw = new FileWriter(file, false);
                bw = new BufferedWriter(fw);
                report = new PrintWriter(bw);            
                LocalDateTime now = LocalDateTime.now();
                String timestamp = now.format(DateTimeFormatter.ofPattern(
                        "yyyy-MM-dd hh:mm:dd a"));
                report.println("Report generated on " +timestamp + " local time");
                return report;
            } catch(IOException e) {
                app.alert("Could not write to report:\n"+e.getMessage());
            }
        } catch(IOException e) {
            app.alert("Could not create report: " + e.getMessage());
        }   
        return null;
    }
    
}