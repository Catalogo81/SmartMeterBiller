package com.example.smartmeterbiller.classes;

public class Mobile_Device
{
    String id;
    int number;

    public Mobile_Device(){}

    public Mobile_Device(String id, int number) {
        this.id = id;
        this.number = number;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}
