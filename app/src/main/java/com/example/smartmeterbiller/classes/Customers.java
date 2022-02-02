package com.example.smartmeterbiller.classes;

public class Customers
{
    String id;
    String name;
    String surname;
    String homeAddress;
    String meterNumber;
    String emailAddress;
    String phoneNumber;

    public Customers() {}

    public Customers(String id, String name, String surname, String homeAddress, String meterNumber, String emailAddress, String phoneNumber) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.homeAddress = homeAddress;
        this.meterNumber = meterNumber;
        this.emailAddress = emailAddress;
        this.phoneNumber = phoneNumber;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getHomeAddress() {
        return homeAddress;
    }

    public void setHomeAddress(String homeAddress) {
        this.homeAddress = homeAddress;
    }

    public String getMeterNumber() {
        return meterNumber;
    }

    public void setMeterNumber(String meterNumber) {
        this.meterNumber = meterNumber;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
