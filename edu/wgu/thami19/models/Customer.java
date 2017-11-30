package edu.wgu.thami19.models;

/**
 *
 * @author thamilton
 */
public class Customer {
    
    private int id;
    private String name;
    private Address address;
    private boolean active;
    
    public Customer() {
        this(-1);
        
    }
    
    public Customer(int id) {
        this(id, "", new Address(-1), true);
    }

    public Customer(int id, String name, Address address, boolean active) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.active = active;
    }   
    
    @Override
    public String toString() {
        return name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }    
}
