package com.enes2.burdayimogrenci;

public class Class {
    private long control;
    private String latitude;
    private String longitude;

    public Class() {
    }

    public Class(long control, String latitude, String longitude) {
        this.control = control;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public long getControl() {
        return control;
    }

    public void setControl(long control) {
        this.control = control;
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
}
