package com.quadram.futh.model;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Gas {
    public String name;
    public int risk;

    public Gas() {}

    public String getName() {
        return name;
    }

    public int getRisk() {
        return risk;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRisk(int risk) {
        this.risk = risk;
    }
}
