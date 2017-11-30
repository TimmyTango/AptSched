package edu.wgu.thami19.models;

/**
 *
 * @author thamilton
 */
public class City {
    
    private int id;
    private String city;
    private Country country;
    
    public City(int id) {
        this(id, "", new Country(-1));
    }

    public City(int id, String city, Country country) {
        this.id = id;
        this.city = city;
        this.country = country;
    }

    @Override
    public String toString() {
        return city;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }   
}
