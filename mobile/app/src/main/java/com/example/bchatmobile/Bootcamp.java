package com.example.bchatmobile;

public class Bootcamp {

    private int id;
    private int budget;
    private int members_count;
    private int current_members_count;
    private String address;
    private String visible_address;
    private String start_time;
    private String end_time;
    private String description;
    private int geoposition_longitude;
    private int geoposition_latitude;


    public Bootcamp(int id, int budget, int members_count, int current_members_count, String address,
                    String visible_address,String start_time, String end_time, String description,
                    int geoposition_longitude, int geoposition_latitude) {
        this.id = id;
        this.budget = budget;
        this.members_count = members_count;
        this.current_members_count = current_members_count;
        this.visible_address = visible_address;
        this.address = address;
        this.start_time = start_time;
        this.end_time = end_time;
        this.description = description;
        this.geoposition_longitude = geoposition_longitude;
        this.geoposition_latitude = geoposition_latitude;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBudget() {
        return budget;
    }

    public void setBudget(int budget) {
        this.budget = budget;
    }

    public int getMembers_count() {
        return members_count;
    }

    public void setMembers_count(int members_count) {
        this.members_count = members_count;
    }

    public int getCurrent_members_count() {
        return current_members_count;
    }

    public void getCurrent_members_count(int current_members_count) {
        this.current_members_count = current_members_count;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getVAddress() {
        return visible_address;
    }

    public void setVAddress(String address) {
        this.visible_address = visible_address;
    }

    public String getStart_time() {
        return start_time;
    }

    public void setStart_time(String start_time) {
        this.start_time = start_time;
    }

    public String getEnd_time() {
        return end_time;
    }

    public void setEnd_time(String end_time) {
        this.end_time = end_time;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getGeoposition_longitude() {
        return geoposition_longitude;
    }

    public void setGeoposition_longitude(int geoposition_longitude) {
        this.geoposition_longitude = geoposition_longitude;
    }

    public int getGeoposition_latitude() {
        return geoposition_latitude;
    }

    public void setGeoposition_latitude(int geoposition_latitude) {
        this.geoposition_latitude = geoposition_latitude;
    }
}

