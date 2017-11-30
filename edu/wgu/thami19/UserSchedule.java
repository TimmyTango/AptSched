package edu.wgu.thami19;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author thamilton
 */
public class UserSchedule {
    
    public String customer, title;
    public LocalDateTime start, end;

    public UserSchedule(String customer, String title, LocalDateTime start, LocalDateTime end) {
        this.customer = customer;
        this.title = title;
        this.start = start;
        this.end = end;
    }
    
    @Override
    public String toString() {
        String s = customer + " - " + title;
        s += "\n\tStarts: " + start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a"));
        s += "\n\tEnds: " + end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a"));
        return s;
    }
}
