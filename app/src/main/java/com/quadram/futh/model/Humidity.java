package com.quadram.futh.model;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Humidity {
    public String name;
    public float value;

    public Humidity() {}

    public String getName() {
        return name;
    }

    public float getValue() {
        return value;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(float value) {
        this.value = value;
    }
}
