package edu.wgu.thami19.models;

/**
 *
 * @author thamilton
 */
public class Address {
    
    private int id;
    private String address;
    private String address2;
    private City city;
    private String postalCode;
    private String phone;    

    public Address(int id) {
        this(id, "", "", new City(-1), "", "");
    }   

    public Address(int id, String address, String address2, City city, String postalCode, String phone) {
        this.id = id;
        this.address = address;
        this.address2 = address2;
        this.city = city;
        this.postalCode = postalCode;
        this.phone = phone;
    }
    
    @Override
    public String toString() {
        return address + "\n" + address2 + "\n" + city + " " + postalCode;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
