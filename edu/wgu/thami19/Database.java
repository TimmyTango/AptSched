package edu.wgu.thami19;

import edu.wgu.thami19.models.Customer;
import edu.wgu.thami19.models.Country;
import edu.wgu.thami19.models.City;
import edu.wgu.thami19.models.Appointment;
import edu.wgu.thami19.models.Address;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 *
 * @author thamilton
 */
public class Database {
    
    private MyApp app;
    private Connection conn;
    private PreparedStatement loginStmt, nextId;
    private PreparedStatement custDetailStmt;
    private PreparedStatement updateCustStmt, updateAddrStmt, updateCityStmt, updateCountryStmt;
    private PreparedStatement insCust, insAddr, insCity, insCountry;
    private Map<String, PreparedStatement> appoint, reminder, report;
    private DateTimeFormatter dtf;
    
    public Database(String host, String schema, String user, String pass)
            throws SQLException {
	
        app = MyApp.getAppInstance();
        
        dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        String url = "jdbc:mysql://" + host + "/" + schema;
	
	try {
		conn = DriverManager.getConnection(url,user,pass);
		System.out.println("Connected to DB: " + schema);
                appoint = new HashMap<>();
                reminder = new HashMap<>();
                report = new HashMap<>();
                createPreparedStatements();
                
	} catch(SQLException e){
		System.err.println("SQLException: " + e.getMessage());
		System.err.println("SQLState: " + e.getSQLState());
		System.err.println("VendorError: " + e.getErrorCode());
                throw e;
	}       
    }
    
    private int nextId(String idField, String table) throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(
                "select MAX("+idField+") from " + table);
        
        int id = 0;

        if(rs.next()) {
            id = rs.getInt(1) + 1;
        }
        
        return id;
    }
    
    public boolean checkLogin(String user, String pass) throws SQLException,
            InvalidLoginException {
        
        loginStmt.setString(1, user.toLowerCase());
        loginStmt.setString(2, pass);
        ResultSet rs = loginStmt.executeQuery();
        if(!rs.next())
            throw new InvalidLoginException();
        return true;
    }
    
    public List<Country> getCountries() {
        List<Country> countries = new ArrayList<>();
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("select * from country");

            while(rs.next()) {
                int col = 1;
                int id = rs.getInt(col++);
                String name = rs.getString(col++);

                countries.add(new Country(id, name));
            }
        } catch(SQLException e) {
            // TODO: could not get countries list
        }
        
        return countries;
    }
    
    public Customer[] getCustomers() throws SQLException {
        
        List<Customer> customers = new ArrayList<>();
        
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("select * from customer");
        
        while(rs.next()) {
            int col = 1;
            int id = rs.getInt(col++);
            String name = rs.getString(col++);
            Address address = new Address(rs.getInt((col++)));
            boolean active = rs.getBoolean(col++);
            
            customers.add(new Customer(id, name, address, active));
        }
        
        return customers.toArray(new Customer[customers.size()]);
    }
    
    public Appointment[] getAppointments() throws SQLException {
        List<Appointment> appointments = new ArrayList<>();
        
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("select * from appointment");
        
        while(rs.next()) {
            int col = 1;
            int id = rs.getInt(col++);
            Customer cust = new Customer(rs.getInt(col++));
            String title = rs.getString(col++);
            String description = rs.getString(col++);
            String location = rs.getString(col++);
            String contact = rs.getString(col++);
            String url = rs.getString(col++);
            Instant start = parseSqlDate(rs.getString(col++));
            Instant end = parseSqlDate(rs.getString(col++));
            
            appointments.add(new Appointment(id, cust, title, description, location, contact,
                    url, start, end));
        }
        return appointments.toArray(new Appointment[appointments.size()]);
    }
    
    public Map<String, List<AppointmentCount>> getAppointmentsReport() throws SQLException {
        Map<String, List<AppointmentCount>> counts = new HashMap<>();
        
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(
                "select count(appointmentId), customerName, month(start) "
                + "from appointment, customer "
                + "where customer.customerId = appointment.customerId "
                + "group by customer.customerId, month(start)"
        );
        
        while(rs.next()) {
            int count = rs.getInt(1);
            String customer = rs.getString(2);
            int month = rs.getInt(3);
            
            String monthName = Month.of(month).getDisplayName(TextStyle.FULL, Locale.getDefault());
            
            if(counts.containsKey(monthName) == false)
                counts.put(monthName, new ArrayList<>());
            counts.get(monthName).add(new AppointmentCount(customer, count));
        }
        return counts;
    }
    
    public Map<String, List<UserSchedule>> getUserSchedule() throws SQLException {
         Map<String, List<UserSchedule>> counts = new HashMap<>();
        
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(
                "select appointment.createdBy, title, start, end, customerName "
                        + "from appointment, customer "
                        + "where customer.customerId = appointment.customerId "
                        + "order by appointment.createdBy, start"
        );
        
        while(rs.next()) {
            String user = rs.getString(1);
            String title = rs.getString(2);
            LocalDateTime start = LocalDateTime.ofInstant(parseSqlDate(rs.getString(3)), app.getTimeZone());
            LocalDateTime end = LocalDateTime.ofInstant(parseSqlDate(rs.getString(4)), app.getTimeZone());
            String cust = rs.getString(5);
                        
            if(counts.containsKey(user) == false)
                counts.put(user, new ArrayList<>());
            counts.get(user).add(new UserSchedule(cust, title, start, end));
        }
        return counts;
    }
    
    public Customer getCustomer(int id) throws SQLException {
        Customer cust = new Customer();
        
        custDetailStmt.setInt(1, id);
        ResultSet rs = custDetailStmt.executeQuery();
        if(rs.next()) {
            int col = 2; //skip customer id
            String name = rs.getString(col++);
            boolean active = rs.getBoolean(col++);
            int addrId = rs.getInt(col++);
            String addr1 = rs.getString(col++);
            String addr2 = rs.getString(col++);
            String postal = rs.getString(col++);
            String phone = rs.getString(col++);
            int cityId = rs.getInt(col++);
            String cityName = rs.getString(col++);
            int countryId = rs.getInt(col++);
            String countryName = rs.getString(col++);
            
            Country country = new Country(countryId, countryName);
            City city = new City(cityId, cityName, country);
            Address address = new Address(addrId, addr1, addr2, city, postal, phone);
            cust = new Customer(id, name, address, active);
        }
        
        return cust;
    }
    
    public Appointment getAppointment(int id) throws SQLException {
        Appointment apt = new Appointment();
        PreparedStatement ps = appoint.get("selectOne");
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if(rs.next()) {
            int col=2; // skip appointmentId
            Customer cust = new Customer(rs.getInt(col++));
            String title = rs.getString(col++);
            String description = rs.getString(col++);
            String location = rs.getString(col++);
            String contact = rs.getString(col++);
            String url = rs.getString(col++);
            Instant start = parseSqlDate(rs.getString(col++));
            Instant end = parseSqlDate(rs.getString(col++));
            
            apt = new Appointment(id, cust, title, description, location, contact,
                    url, start, end);
        }
        return apt;
    }
    
    public void updateCustomer(Customer c) throws SQLException {
        updateCustStmt.setString(1, c.getName());
        updateCustStmt.setBoolean(2, c.isActive());
        updateCustStmt.setString(3, app.getUser());
        updateCustStmt.setInt(4, c.getId());
        int result = updateCustStmt.executeUpdate();
        if(result < 1) {
            // TODO: User was not updated
        }
        
        updateAddress(c.getAddress());
        updateCity(c.getAddress().getCity());
        //updateCountry(c.getAddress().getCity().getCountry());
    }
    
    public void insertCustomer(Customer c) throws SQLException {
        //insertCountry(c.getAddress().getCity().getCountry());
        insertCity(c.getAddress().getCity());
        insertAddress(c.getAddress());
        
        c.setId(nextId("customerId", "customer"));
        
        int col = 1;
        insCust.setInt(col++, c.getId());
        insCust.setString(col++, c.getName());
        insCust.setInt(col++, c.getAddress().getId());
        insCust.setBoolean(col++, c.isActive());
        insCust.setString(col++, app.getUser());        
        insCust.setString(col++, app.getUser()); 
        
        if(insCust.executeUpdate() < 1) {
            // handle error
        }
    }
    
    private void updateAddress(Address a) throws SQLException {
        updateAddrStmt.setString(1, a.getAddress());
        updateAddrStmt.setString(2, a.getAddress2());
        updateAddrStmt.setString(3, a.getPostalCode());
        updateAddrStmt.setString(4, a.getPhone());
        updateAddrStmt.setString(5, app.getUser());
        updateAddrStmt.setInt(6, a.getId());
        int result = updateAddrStmt.executeUpdate();
        if(result < 1) {
            // TODO: User was not updated
        }
    }
    
    private void insertAddress(Address a) throws SQLException {
        a.setId(nextId("addressId", "address"));
        
        int col = 1;
        insAddr.setInt(col++, a.getId());
        insAddr.setString(col++, a.getAddress());
        insAddr.setString(col++, a.getAddress2());
        insAddr.setInt(col++, a.getCity().getId());
        insAddr.setString(col++, a.getPostalCode());
        insAddr.setString(col++, a.getPhone());
        insAddr.setString(col++, app.getUser());        
        insAddr.setString(col++, app.getUser()); 
        
        if(insAddr.executeUpdate() < 1) {
            // handle error
        }
    }
    
    private void updateCity(City c) throws SQLException {
        updateCityStmt.setString(1, c.getCity());
        updateCityStmt.setString(2, app.getUser());
        updateCityStmt.setInt(3, c.getCountry().getId());
        updateCityStmt.setInt(4, c.getId());
        int result = updateCityStmt.executeUpdate();
        if(result < 1) {
            // TODO: City was not updated
        }
    }
    
    private void insertCity(City c) throws SQLException {
        c.setId(nextId("cityId", "city"));
        
        int col = 1;
        insCity.setInt(col++, c.getId());
        insCity.setString(col++, c.getCity());
        insCity.setInt(col++, c.getCountry().getId());
        insCity.setString(col++, app.getUser());        
        insCity.setString(col++, app.getUser());
        
        
        if(insCity.executeUpdate() < 1) {
            // handle error
        }
    }
    
    private void updateCountry(Country c) throws SQLException {
        updateCountryStmt.setString(1, c.getCountry());
        updateCountryStmt.setString(2, app.getUser());
        updateCountryStmt.setInt(3, c.getId());
        int result = updateCountryStmt.executeUpdate();
        if(result < 1) {
            // TODO: Country was not updated
        }
    }
    
    private void insertCountry(Country c) throws SQLException {
        c.setId(nextId("countryId", "country"));
        
        int col = 1;
        insCountry.setInt(col++, c.getId());
        insCountry.setString(col++, c.getCountry());
        insCountry.setString(col++, app.getUser());        
        insCountry.setString(col++, app.getUser());
        
        if(insCountry.executeUpdate() < 1) {
            // handle error
        }
    }
    
    public void updateAppointment(Appointment a) throws SQLException {
        
        PreparedStatement ps = appoint.get("update");
        int col = 1;
        ps.setInt(col++, a.getCustomer().getId());
        ps.setString(col++, a.getTitle());
        ps.setString(col++, a.getDescription());
        ps.setString(col++, a.getLocation());
        ps.setString(col++, a.getContact());
        ps.setString(col++, a.getUrl());
        ps.setString(col++, parseInstant(a.getStart()));
        ps.setString(col++, parseInstant(a.getEnd()));
        ps.setString(col++, app.getUser());
        ps.setInt(col++, a.getId());
        
        if(ps.executeUpdate() < 1) {
            System.err.println("Error in update Appointment");
        }
        
        updateReminders(a);
    }
    
    public void insertAppointment(Appointment a) throws SQLException {
        a.setId(nextId("appointmentId", "appointment"));
        
        //appointmentId, customerId, title, description, location, contact, url,
        //start, end, createDate, createdBy, lastUpdate, lastUpdateBy
        
        PreparedStatement ps = appoint.get("insert");
        int col = 1;
        ps.setInt(col++, a.getId());
        ps.setInt(col++, a.getCustomer().getId());
        ps.setString(col++, a.getTitle());
        ps.setString(col++, a.getDescription());
        ps.setString(col++, a.getLocation());
        ps.setString(col++, a.getContact());
        ps.setString(col++, a.getUrl());
        ps.setString(col++, parseInstant(a.getStart()));
        ps.setString(col++, parseInstant(a.getEnd()));
        ps.setString(col++, app.getUser());
        ps.setString(col++, app.getUser());
        
        if(ps.executeUpdate() < 1) {
            System.err.println("Error in insert Appointment");
        }
    }
    
    public List<Appointment> getAppointmentsForCalendar(Customer c, 
            Instant start, Instant end) throws SQLException {
        List<Appointment> appointment = new ArrayList<>();
        
        PreparedStatement ps = appoint.get("calendarSort");
        int col = 1;
        ps.setInt(col++, c.getId());
        ps.setString(col++, parseInstant(start));
        ps.setString(col++, parseInstant(end));
        
        ResultSet rs = ps.executeQuery();
        
        while(rs.next()) {
            col = 1;
            int id = rs.getInt(col++);
            Customer cust = new Customer(rs.getInt(col++));
            String title = rs.getString(col++);
            String description = rs.getString(col++);
            String location = rs.getString(col++);
            String contact = rs.getString(col++);
            String url = rs.getString(col++);
            Instant startDT = parseSqlDate(rs.getString(col++));
            Instant endDT = parseSqlDate(rs.getString(col++));
            
            
            appointment.add(new Appointment(id, cust, title, description,
                    location, contact, url, startDT, endDT));
        }
        return appointment;
    }
    
    public boolean isAppointmentOverlapping(Appointment a, Instant start, Instant end) throws SQLException {
        PreparedStatement ps = appoint.get("overlap");
        int param = 1;
        ps.setInt(param++, a.getId());
        ps.setString(param++, parseInstant(end));
        ps.setString(param++, parseInstant(start));
        
        ResultSet rs = ps.executeQuery();
        
        return rs.next();        
    }
    
    private Instant parseSqlDate(String datetime) {
        datetime = datetime.substring(0, 19);
        LocalDateTime dbdt = LocalDateTime.parse(datetime, dtf);
        return dbdt.toInstant(ZoneOffset.UTC);
    }
    
    private String parseInstant(Instant ins) {
        LocalDateTime ldt = LocalDateTime.ofInstant(ins, ZoneOffset.UTC);
        return ldt.format(dtf);
    }
    
    public void insertReminder(Appointment a) throws SQLException {
        int id = nextId("reminderId", "reminder");
        Instant datetime = a.getStart().plusSeconds(60 * 15);
        
        PreparedStatement ps = reminder.get("insert");
        int param = 1;
        ps.setInt(param++, id);
        ps.setString(param++, parseInstant(datetime));
        ps.setInt(param++, a.getId());
        ps.setString(param++, app.getUser());
        ps.setString(param++, a.getTitle());
        
        if(ps.executeUpdate() < 1) {
            // did not insert
        }        
    }
    
    public void updateReminders(Appointment a) throws SQLException {
        Instant datetime = a.getStart().plusSeconds(60 * 15);
        
        PreparedStatement ps = reminder.get("update");
        int param = 1;
        ps.setString(param++, parseInstant(datetime));
        ps.setInt(param++, a.getId());
        
        ps.executeUpdate();
    }
    
    public void removeReminder(Appointment a) throws SQLException {
        PreparedStatement ps = reminder.get("remove");
        int param = 1;
        ps.setInt(param++, a.getId());
        ps.setString(param++, app.getUser());
        
        ps.executeUpdate();
    }
    
    public boolean isReminderSet(Appointment a) throws SQLException {
        PreparedStatement ps = reminder.get("select");
        int param = 1;
        ps.setInt(param++, a.getId());
        ps.setString(param++, app.getUser());
        
        ResultSet rs = ps.executeQuery();
        if(rs.next())
            return true;
        return false;
    }
    
    public List<Reminder> getReminders() throws SQLException {
        List<Reminder> reminders = new ArrayList<>();
        
        PreparedStatement ps = reminder.get("getReminders");
        ps.setString(1, app.getUser());
        
        ResultSet rs = ps.executeQuery();
        while(rs.next()) {
            LocalDateTime reminderDate = LocalDateTime.ofInstant(
                    parseSqlDate(rs.getString(1)), app.getTimeZone());
            String title = rs.getString(2);
            reminders.add(new Reminder(title, reminderDate));
            
            // DEBUG
            System.out.println(title);
        }
        return reminders;
    }
    
    private void createPreparedStatements() throws SQLException {
        
        nextId = conn.prepareStatement("select max(?) from ?");
        
        loginStmt = conn.prepareStatement(
                "select * from user where userName=? and password=?");
        
        custDetailStmt = conn.prepareStatement(
                "select customerId, customerName, active, " +
                "address.addressId, address, address2, postalCode, phone, " +
                "city.cityId, city.city, " +
                "country.countryId, country.country " +
                "from customer, address, city, country " +
                "where customerId = ? " +
                "and customer.addressId = address.addressId " +
                "and address.cityId = city.cityId " +
                "and city.countryId = country.countryId "
        );
        
        updateCustStmt = conn.prepareStatement(
                "update customer set customerName=?, active=?, lastUpdateBy=?, " +
                "lastUpdate=utc_timestamp() where customerId=?"
        );
        
        updateAddrStmt = conn.prepareStatement(
                "update address set address=?, address2=?, postalCode=?, " +
                "phone=?, lastUpdateBy=?, lastUpdate=utc_timestamp() " +
                "where addressId=?"
        );
        
        updateCityStmt = conn.prepareCall(
                "update city set city=?, lastUpdateBy=?, lastUpdate=utc_timestamp(), " +
                "countryId=? where cityId=?"
        );
        
        updateCountryStmt = conn.prepareCall(
                "update country set country=?, lastUpdateBy=?, lastUpdate=utc_timestamp() " +
                "where countryId=?"
        );
        
        insCust = conn.prepareStatement("insert into customer " +
                "values(?, ?, ?, ?, utc_timestamp(), ?, utc_timestamp(), ?)");
        insAddr = conn.prepareStatement("insert into address " +
                "values(?, ?, ?, ?, ?, ?, utc_timestamp(), ?, utc_timestamp(), ?)");
        insCity = conn.prepareStatement("insert into city " +
                "values(?, ?, ?, utc_timestamp(), ?, utc_timestamp(), ?)");
        insCountry = conn.prepareStatement("insert into country " +
                "values(?, ?, utc_timestamp(), ?, utc_timestamp(), ?)");
        
        //appointmentId, customerId, title, description, location, contact, url,
        //start, end, createDate, createdBy, lastUpdate, lastUpdateBy
        appoint.put("insert", conn.prepareStatement("insert into appointment values " +
                "(?, ?, ?, ?, ?, ?, ?, ?, ?, utc_timestamp(), ?, utc_timestamp(), ?)"
        ));
        
        appoint.put("update", conn.prepareStatement("update appointment set " +
                "customerId=?, title=?, description=?, location=?, contact=?, "+
                "url=?, start=?, end=?, lastUpdate=utc_timestamp(), lastUpdateBy=? " +
                "where appointmentId=?"
        ));
        
        appoint.put("selectOne", conn.prepareStatement(
                "select * from appointment where appointmentId=?"
        ));
        
        appoint.put("calendarSort", conn.prepareStatement(
                "select * from appointment " +
                "where customerId=? " +
                "and start >= ? " +
                "and start < ? " +
                "order by start desc"
        ));
        
        appoint.put("overlap", conn.prepareStatement(
                "select * from appointment "
                + "where appointmentId != ? "
                + "and start <= ? "
                + "and end >= ?"
        ));
        
        reminder.put("insert", conn.prepareStatement(
                "insert into reminder values " +
                "(?, ?, 0, 0, ?, ?, utc_timestamp(), ?)"
        ));
        
        reminder.put("update", conn.prepareStatement(
                "update reminder set reminderDate=? where appointmentId=?"
        ));
        
        reminder.put("remove", conn.prepareStatement(
                "delete from reminder where appointmentId=? and createdBy=?"
        ));
        
        reminder.put("select", conn.prepareStatement(
                "select * from reminder "
                + "where appointmentId=? and createdBy=?"
        ));
        
        reminder.put("getReminders", conn.prepareStatement(
                "select reminderDate, remindercol from reminder "
                + "where createdBy=? "
                + "and reminderDate >= utc_timestamp() "
                + "and reminderDate < (utc_timestamp() + interval 1 hour)"
        ));
        
        report.put("appointmentsByMonth", conn.prepareStatement(
                "select * from appointment, customer "
                + "where customer.customerId = appointment.customerId "
                + "order by start"
        ));
        
        report.put("userSchedule", conn.prepareStatement(
                "select * from appointment, customer "
                + "where customer.customerId = appointment.customerId "
                + "order by appointment.createdBy"
        ));
        
    }
}
