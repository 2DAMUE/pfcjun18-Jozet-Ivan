package com.quadram.futh.model;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Device {
    public Gas gas;
    public Humidity humidity;
    public Relay light;
    public Relay plug;
    public Temperature temperature;

    public Device() {}

    public Gas getGas() {
        return gas;
    }

    public Humidity getHumidity() {
        return humidity;
    }

    public Relay getLight() {
        return light;
    }

    public Relay getPlug() {
        return plug;
    }

    public Temperature getTemperature() {
        return temperature;
    }

    public void setGas(Gas gas) {
        this.gas = gas;
    }

    public void setHumidity(Humidity humidity) {
        this.humidity = humidity;
    }

    public void setLight(Relay light) {
        this.light = light;
    }

    public void setPlug(Relay plug) {
        this.plug = plug;
    }

    public void setTemperature(Temperature temperature) {
        this.temperature = temperature;
    }
}
