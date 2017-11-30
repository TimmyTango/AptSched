package edu.wgu.thami19;
 
import edu.wgu.thami19.models.Customer;
import edu.wgu.thami19.models.Country;
import edu.wgu.thami19.models.City;
import edu.wgu.thami19.models.Address;
import java.sql.SQLException;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class CustomerDetailsView extends GridPane {
    
    MyApp app;
    Customer cust;
    Text sceneTitle, error;
    Label name, addr1, addr2, city, postal, phone, country, active;
    TextField nameField, addr1Field, addr2Field, cityField, postalField,
            phoneField;
    CheckBox activeBox;
    ChoiceBox countryBox;
    Button confirmBtn, cancelBtn, editBtn;
    
    public CustomerDetailsView() {
        this(new Customer());
    }
    
    public CustomerDetailsView(Customer cust) {
        app = MyApp.getAppInstance();
        this.cust = cust;
        
        createUI();
        
        if(cust.getId() < 0)
            enableEdit();
    }
    
    private void createUI() {
        setAlignment(Pos.TOP_LEFT);
        setHgap(10);
        setVgap(10);
        setPadding(new Insets(0, 25, 25, 25));
        
        getChildren().clear();
        
        int row = 0;
        
        sceneTitle = new Text("Customer");
        sceneTitle.setFont(Font.font("Arial", FontWeight.NORMAL, 20));
        add(sceneTitle, 0, row++, 2, 1);
        
        error = new Text();
        error.setFill(Color.FIREBRICK);
        add(error, 0, row++, 2, 1);
        
        name = new Label("Customer Name");
        nameField = new TextField(cust.getName());
        nameField.setEditable(false);
        add(name, 0, row);
        add(nameField, 1, row++);
        
        phone = new Label("Phone");
        phoneField = new TextField(cust.getAddress().getPhone());
        phoneField.setEditable(false);
        add(phone, 0, row);
        add(phoneField, 1, row++);
        
        addr1 = new Label("Address");
        addr1Field = new TextField(cust.getAddress().getAddress());
        addr1Field.setEditable(false);
        add(addr1, 0, row);
        add(addr1Field, 1, row++);
        
        addr2 = new Label("Address2");
        addr2Field = new TextField(cust.getAddress().getAddress2());
        addr2Field.setEditable(false);
        add(addr2, 0, row);
        add(addr2Field, 1, row++);
        
        city = new Label("City");
        cityField = new TextField(cust.getAddress().getCity().getCity());
        cityField.setEditable(false);
        add(city, 0, row);
        add(cityField, 1, row++);
        
        postal = new Label("Postal");
        postalField = new TextField(cust.getAddress().getPostalCode());
        postalField.setEditable(false);
        add(postal, 0, row);
        add(postalField, 1, row++);
        
        country = new Label("Country");
        
        List<Country> countries = app.getDB().getCountries();
        countryBox = new ChoiceBox(FXCollections.observableArrayList(countries));
        countryBox.setMaxWidth(Double.MAX_VALUE);
        countryBox.getSelectionModel().selectFirst();
        for(int i = 0; i < countries.size(); i++) {
            if(countries.get(i).getId() == cust.getAddress().getCity().getCountry().getId()) {
                countryBox.getSelectionModel().select(i);
            }
        }
        countryBox.setDisable(true);
                
        add(country, 0, row);
        add(countryBox, 1, row++);
        
        active = new Label("Active Customer");
        activeBox = new CheckBox("Activate");
        activeBox.setSelected(cust.isActive());
        activeBox.setDisable(true);
        add(active, 0, row);
        add(activeBox, 1, row++);
        
        confirmBtn = new Button("Ok");
        confirmBtn.setMinWidth(75);
        confirmBtn.setVisible(false);
        cancelBtn = new Button("Cancel");
        cancelBtn.setMinWidth(75);
        cancelBtn.setVisible(false);
        editBtn = new Button("Edit");
        editBtn.setMinWidth(75);
        confirmBtn.setOnAction(e -> confirmAction());
        cancelBtn.setOnAction(e -> cancelAction());
        editBtn.setOnAction(e -> enableEdit());
        row++;
        HBox hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
        hbBtn.getChildren().addAll(editBtn, confirmBtn, cancelBtn);
        add(hbBtn, 1, row++);  
    }
    
    private void enableEdit() {
        app.getMainStage().lockEdit();
        sceneTitle.setText("*Customer*");
        nameField.setEditable(true);
        addr1Field.setEditable(true);
        addr2Field.setEditable(true);
        cityField.setEditable(true);
        postalField.setEditable(true);
        phoneField.setEditable(true);
        activeBox.setDisable(false);
        countryBox.setDisable(false);
        confirmBtn.setVisible(true);
        cancelBtn.setVisible(true);
        editBtn.setVisible(false);
        
//        TextField nameField, addr1Field, addr2Field, cityField, postalField,
//                phoneField;
//        CheckBox activeBox;
//        ChoiceBox countryBox;
//        Button confirmBtn, cancelBtn, editBtn;
        
    }
    
    private void confirmAction() {
        try{
            validateInput();
        } catch(CustomerException e) {
            error.setText(e.getMessage());
            return;
        }
        
        Country updCountry = (Country)countryBox.getSelectionModel().getSelectedItem();
        City updCity = new City(cust.getAddress().getCity().getId(), cityField.getText(), updCountry);
        Address updAddress = new Address(cust.getAddress().getId(), addr1Field.getText(),
                addr2Field.getText(), updCity, postalField.getText(), phoneField.getText());
        Customer updCust = new Customer(cust.getId(), nameField.getText(), updAddress, activeBox.isSelected());
        
        try {
            if(cust.getId() < 0)
                app.getDB().insertCustomer(updCust);
            else
                app.getDB().updateCustomer(updCust);
        } catch(SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
        
        app.getMainStage().freeEdit(true);
        createUI();
    }
    
    private void cancelAction() {
        app.getMainStage().freeEdit(false);
        createUI();
    }
    
    private void validateInput() throws CustomerException {
        
        error.setText("");
        
        try {
            if(nameField.getText().trim().length() < 1) {
                throw new InvalidCustomerData("Customer name cannot be empty");
            } else {
                for(Customer c : app.getDB().getCustomers()) {
                    if(c.getName().equals(cust.getName()) &&
                            c.getId() != cust.getId()) {
                        throw new InvalidCustomerData("Duplicate customer name");
                    }

                }
            }
            
            if(phoneField.getLength() < 7) {
                throw new InvalidCustomerData("Phone must be at least 7 digits");
            }
            
            if(addr1Field.getLength() < 1) {
                throw new InvalidCustomerData("Customer must have street address");
            }
            
            if(cityField.getLength() < 1) {
                throw new InvalidCustomerData("Customer must have city");
            }
            
            if(postalField.getLength() < 5) {
                throw new InvalidCustomerData("Customer must have postal code");
            }
        } catch(SQLException e) {            
            System.err.println(e.getMessage());
            throw new CustomerException("Could not validate input: DB ERROR");
        }
    }
    
    class CustomerException extends Exception {
        public CustomerException(String message) {
            super(message);
        }
    }
    
    class InvalidCustomerData extends CustomerException {
        public InvalidCustomerData(String message) {
            super(message);
        }
    }
}
