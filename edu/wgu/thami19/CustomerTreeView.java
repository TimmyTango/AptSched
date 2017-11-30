/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.wgu.thami19;

import edu.wgu.thami19.models.Customer;
import edu.wgu.thami19.models.Appointment;
import java.sql.SQLException;
import javafx.application.Platform;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

/**
 *
 * @author thamilton
 */
public class CustomerTreeView extends TreeView<Object> {
    
    private TreeItem<Object> root;
    private Customer[] customers;
    private Appointment[] appointments;
    private MyApp app;
    
    public CustomerTreeView() {
        
        app = MyApp.getAppInstance();
        
        buildTree();
        
        setOnMouseClicked(event -> {
            if(event.getClickCount() == 1) {
                TreeItem<Object> item = getSelectionModel().getSelectedItem();
                
                if(item == null)
                    return;
                
                Class itemClass = item.getValue().getClass();
                
                if(itemClass.equals(Customer.class)) {
                    Customer c = (Customer)item.getValue();
                    try {
                        c = app.getDB().getCustomer(c.getId());
                        app.getMainStage().showCustomerPanel(c);
                    } catch(SQLException e) {                        
                        app.alert(e.getMessage());
                    }
                } else if(itemClass.equals(Appointment.class)) {
                    Appointment a = (Appointment)item.getValue();
                    try {
                        a = app.getDB().getAppointment(a.getId());
                        app.getMainStage().showAppointmentPanel(a);
                    } catch(SQLException e) {
                        app.alert(e.getMessage());
                    }
                }
            }
        });
    }
    
    private void buildTree() {     
        
        try {
            customers = app.getDB().getCustomers();
            appointments = app.getDB().getAppointments();
        } catch(SQLException e) {
            System.err.println(e.getMessage());
            Platform.exit();
        }
        
        root = new TreeItem<>("Customers");
        root.setExpanded(true);
        
        TreeItem<Object> activeBranch = new TreeItem<>("Active");
        activeBranch.setExpanded(true);
        
        TreeItem<Object> inactiveBranch = new TreeItem<>("Inactive");
        inactiveBranch.setExpanded(true);
        
        for(Customer c : customers) {
            TreeItem<Object> cItem = new TreeItem<>(c);
            TreeItem<Object> branch = c.isActive() ? activeBranch : inactiveBranch;
            for(Appointment a : appointments) {
                if(a.getCustomer().getId() == c.getId()) {
                    a.setCustomer(c);
                    cItem.getChildren().add(new TreeItem<>(a));
                }
            }
            cItem.setExpanded(true);
            branch.getChildren().add(cItem);
        }
        
        root.getChildren().addAll(activeBranch, inactiveBranch);
        setRoot(root);
    }
    
    public void rebuildTree() {
        int selection = getSelectionModel().getSelectedIndex();
        buildTree();
        getSelectionModel().select(selection);
    }
    
}
