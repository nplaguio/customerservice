package com.bpi.customerservice.entity;

import org.springframework.data.annotation.Id; // Corrected import for MongoDB
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime; // Added missing import

@Document(collection = "audit_logs")
public class AuditLog {

    @Id
    private String id;
    private String requestUID;
    private String resourceOwnerID;
    private String action;
    private String customerNumber;
    private Object customerDetails;
    private LocalDateTime timestamp; // Corrected typo

    public AuditLog() {
    }

    // Corrected typo in parameter
    public AuditLog(String requestUID, String resourceOwnerID, String action, String customerNumber, Object customerDetails, LocalDateTime timestamp) {
        this.requestUID = requestUID;
        this.resourceOwnerID = resourceOwnerID;
        this.action = action;
        this.customerNumber = customerNumber;
        this.customerDetails = customerDetails;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRequestUID() {
        return requestUID;
    }

    public void setRequestUID(String requestUID) {
        this.requestUID = requestUID;
    }

    public String getResourceOwnerID() {
        return resourceOwnerID;
    }

    public void setResourceOwnerID(String resourceOwnerID) {
        this.resourceOwnerID = resourceOwnerID;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getCustomerNumber() {
        return customerNumber;
    }

    public void setCustomerNumber(String customerNumber) {
        this.customerNumber = customerNumber;
    }

    public Object getCustomerDetails() {
        return customerDetails;
    }

    public void setCustomerDetails(Object customerDetails) {
        this.customerDetails = customerDetails;
    }

    // Corrected typo in return type
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    // Corrected typo in parameter
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}