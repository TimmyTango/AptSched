package edu.wgu.thami19.models;

import java.time.Instant;

/**
 *
 * @author thamilton
 */
public class Appointment {
    
    private int id;
    private Customer customer;
    private String title;
    private String description;
    private String location;
    private String contact;
    private String url;
    private Instant start;
    private Instant end;
    
    public Appointment() {
        this(-1, new Customer(), "", "", "", "", "", Instant.now(), Instant.now());
    }

    public Appointment(int id, Customer customer, String title, String description, String location, String contact, String url, Instant start, Instant end) {
        this.id = id;
        this.customer = customer;
        this.title = title;
        this.description = description;
        this.location = location;
        this.contact = contact;
        this.url = url;
        this.start = start;
        this.end = end;
    }
    
    @Override
    public String toString() {
        return title;
    }

    public Instant getEnd() {
        return end;
    }

    public void setEnd(Instant end) {
        this.end = end;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Instant getStart() {
        return start;
    }

    public void setStart(Instant start) {
        this.start = start;
    }
    
    
}
