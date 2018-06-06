package com.quadram.futh.model;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Relay {
    public String name;
    public String state;
    public int timer;

    public Relay() {}

    public String getName() {
        return name;
    }

    public String getState() {
        return state;
    }

    public int getTimer() {
        return timer;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setTimer(int timer) {
        this.timer = timer;
    }
}
