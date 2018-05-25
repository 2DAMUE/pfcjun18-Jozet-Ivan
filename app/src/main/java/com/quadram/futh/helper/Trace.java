package com.quadram.futh.helper;

import android.util.Log;
import com.quadram.futh.model.Device;

public class Trace {
    public Trace() {}

    public void logDevice(Device d) {
        String tag = "DEVICE";

        Log.d(tag, "Gas -> Nombre: " + d.getGas().getName() + " - Riesgo: " + d.getGas().getRisk());
        Log.d(tag, "Humedad -> Nombre: " + d.getHumidity().getName() + " - Valor: " + d.getHumidity().getValue());
        Log.d(tag, "Luz -> Nombre: " + d.getLight().getName() + " - Estado: " + d.getLight().getState());
        Log.d(tag, "Enchufe -> Nombre: " + d.getPlug().getName() + " - Estado: " + d.getPlug().getState());
        Log.d(tag, "Temperatura -> Nombre: " + d.getTemperature().getName() + " - Valor: " + d.getTemperature().getValue());
    }
}
