package com.example.panoramic.domain;


import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;


@IgnoreExtraProperties
public class User  implements Serializable {
    private String id;
    private String name;
    private String lastName;
    private double latitude;
    private double longitude;
    private String numID;
    private boolean available;
    public boolean isAvailable() {
        return available;
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

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getNumID() {
        return numID;
    }

    public void setNumID(String numID) {
        this.numID = numID;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public User() {
    }

    public User(String id, String name, String lastName, double latitude, double longitude, String numID, boolean available) {
        this.id = id;
        this.name = name;
        this.lastName = lastName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.numID = numID;
        this.available = available;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", lastName='" + lastName + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", numID='" + numID + '\'' +
                ", available=" + available +
                '}';
    }
}
