package com.chatt.demo.model;

/**
 * Created by Admin on 12/23/2016.
 */

public class MapModel {
    private String latitude;
    private String longitude;

    public MapModel() {
    }

    public MapModel(String latitude, String longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
    public  String local(){
        return "https://maps.googleapis.com/maps/api/staticmap?center="+latitude+","+longitude+"&zoom=18&size=280x280&markers=color:red|"+latitude+","+longitude;
    }



}
