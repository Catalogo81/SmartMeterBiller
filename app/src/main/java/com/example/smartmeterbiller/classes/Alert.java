package com.example.smartmeterbiller.classes;

public class Alert
{
    int id;
    String alertType;
    String alertMessage;
    String report;
    String customerEmail;

    public Alert(){}

    public Alert(int id, String alertType, String alertMessage, String report, String customerEmail) {
        this.id = id;
        this.alertType = alertType;
        this.alertMessage = alertMessage;
        this.report = report;
        this.customerEmail = customerEmail;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAlertType() {
        return alertType;
    }

    public void setAlertType(String alertType) {
        this.alertType = alertType;
    }

    public String getAlertMessage() {
        return alertMessage;
    }

    public void setAlertMessage(String alertMessage) {
        this.alertMessage = alertMessage;
    }

    public String getReport() {
        return report;
    }

    public void setReport(String report) {
        this.report = report;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }
}
