package com.quadram.futh.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.quadram.futh.R;
import com.quadram.futh.helper.Constantes;
import com.quadram.futh.helper.Trace;
import com.quadram.futh.model.Device;
import com.quadram.futh.model.Gas;
import com.quadram.futh.model.Humidity;
import com.quadram.futh.model.Relay;
import com.quadram.futh.model.Temperature;
import com.quadram.futh.notification.NotificationHelper;

// TODO: Implementar notificaciones
public class ServiceListener extends Service {
    private final IBinder mBinder;
    private static ServiceListener INSTANCE = null;
    private static FirebaseDatabase database;
    private static FirebaseAuth mAuth;
    private static FirebaseUser currentUser;
    private static DatabaseReference myRef;
    private static ValueEventListener listener;
    private static Device dispositivoOld;
    private static Device dispositivoNew;
    private static boolean isFirstRead;
    private static Trace trace;
    private static NotificationHelper nh;

    public ServiceListener() {
        mBinder = new LocalBinder();
        database = FirebaseDatabase.getInstance();
        dispositivoOld = new Device();
        dispositivoNew = new Device();
        isFirstRead = true;
    }

    public static ServiceListener getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ServiceListener();
            listener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (isFirstRead) {
                        dispositivoOld.setGas(dataSnapshot.child("gas1").getValue(Gas.class));
                        dispositivoOld.setHumidity(dataSnapshot.child("humidity1").getValue(Humidity.class));
                        dispositivoOld.setLight(dataSnapshot.child("rele1").getValue(Relay.class));
                        dispositivoOld.setPlug(dataSnapshot.child("rele2").getValue(Relay.class));
                        dispositivoOld.setTemperature(dataSnapshot.child("temperature1").getValue(Temperature.class));

                        trace.logDevice(dispositivoOld);
                        isFirstRead = false;
                    }
                    else {
                        dispositivoNew.setGas(dataSnapshot.child("gas1").getValue(Gas.class));
                        dispositivoNew.setHumidity(dataSnapshot.child("humidity1").getValue(Humidity.class));
                        dispositivoNew.setLight(dataSnapshot.child("rele1").getValue(Relay.class));
                        dispositivoNew.setPlug(dataSnapshot.child("rele2").getValue(Relay.class));
                        dispositivoNew.setTemperature(dataSnapshot.child("temperature1").getValue(Temperature.class));

                        trace.logDevice(dispositivoNew);
                        processChanges(whatChanged(dispositivoOld, dispositivoNew));
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {}
            };
            myRef = database.getReference("devices/0x00000001");
        }
        return INSTANCE;
    }

    private static String whatChanged(Device dOld, Device dNew) {
        if (dOld.getGas().getRisk() != dNew.getGas().getRisk()) {
            return "gas";
        }
        else if (dOld.getHumidity().getValue() != dNew.getHumidity().getValue()) {
            return "humidity";
        }
        else if (!dOld.getLight().getState().equalsIgnoreCase(dNew.getLight().getState())) {
            return "light";
        }
        else if (!dOld.getPlug().getState().equalsIgnoreCase(dNew.getPlug().getState())) {
            return "plug";
        }
        else if (dOld.getTemperature().getValue() != dNew.getTemperature().getValue()) {
            return "temperature";
        }
        else {
            return "nochanges";
        }
    }

    private static void processChanges(String changes) {
        if (changes.equalsIgnoreCase("gas")) {
            if (dispositivoNew.getGas().getRisk() == 0) {
                nh.showNotification("Gas", "No se han detectado gases nocivos para la salud", Constantes.CHANNEL_GAS, R.mipmap.gas_risk_zero_icon, false);
            }
            else if (dispositivoNew.getGas().getRisk() == 1) {
                nh.showNotification("Gas", "¡Es posible que tengas una fuga de gas!", Constantes.CHANNEL_GAS, R.mipmap.gas_risk_one_icon, false);
            }
            else if (dispositivoNew.getGas().getRisk() == 2) {
                nh.showNotification("Gas", "Toma medidas de precaución, ¡se han detectado niveles altísimos de gas!", Constantes.CHANNEL_GAS, R.mipmap.gas_risk_two_icon, false);
            }
            dispositivoOld.getGas().setRisk(dispositivoNew.getGas().getRisk());  // Se guarda el nuevo valor en el objeto antiguo para ser comaparado de nuevo
        }
        else if (changes.equalsIgnoreCase("humidity")) {
            if (dispositivoNew.getHumidity().getValue() <= 25) {
                nh.showNotification("Humedad", "¿También has notado lo seco que está el ambiente?", Constantes.CHANNEL_HUMIDITY, R.mipmap.humidity_notification_icon, false);
            }
            else if (dispositivoNew.getHumidity().getValue() <= 50) {
                nh.showNotification("Humedad", "¡Hace la humedad perfecta para jugar a la play!", Constantes.CHANNEL_HUMIDITY, R.mipmap.humidity_notification_icon, false);
            }
            else if (dispositivoNew.getHumidity().getValue() <= 75) {
                nh.showNotification("Humedad", "Noto que estoy empezando a sudar y soy un móvil...", Constantes.CHANNEL_HUMIDITY, R.mipmap.humidity_notification_icon, false);
            }
            else if (dispositivoNew.getHumidity().getValue() <= 100) {
                nh.showNotification("Humedad", "¡Esto es peor que el Amazonas!", Constantes.CHANNEL_HUMIDITY, R.mipmap.humidity_notification_icon, false);
            }
            dispositivoOld.getHumidity().setValue(dispositivoNew.getHumidity().getValue());  // Se guarda el nuevo valor en el objeto antiguo para ser comaparado de nuevo
        }
        else if (changes.equalsIgnoreCase("light")) {
            if (dispositivoNew.getLight().getState().equalsIgnoreCase("on")) {
                nh.showNotification("Luz", "¡Alguien encendió la luz!", Constantes.CHANNEL_LIGHT, R.mipmap.light_on_notification_icon, true);
            }
            else if (dispositivoNew.getLight().getState().equalsIgnoreCase("off")) {
                nh.showNotification("Luz", "¡Alguien apagó la luz!", Constantes.CHANNEL_LIGHT, R.mipmap.light_off_notification_icon, true);
            }
            dispositivoOld.getLight().setState(dispositivoNew.getLight().getState());  // Se guarda el nuevo valor en el objeto antiguo para ser comaparado de nuevo
        }
        else if (changes.equalsIgnoreCase("plug")) {
            if (dispositivoNew.getPlug().getState().equalsIgnoreCase("on")) {
                nh.showNotification("Enchufe", "¡Alguien activó el enchufe!", Constantes.CHANNEL_PLUG, R.mipmap.connected_icon, true);
            }
            else if (dispositivoNew.getPlug().getState().equalsIgnoreCase("off")) {
                nh.showNotification("Enchufe", "¡Alguien desactivó el enchufe!", Constantes.CHANNEL_PLUG, R.mipmap.disconnected_icon, true);
            }
            dispositivoOld.getPlug().setState(dispositivoNew.getPlug().getState());  // Se guarda el nuevo valor en el objeto antiguo para ser comaparado de nuevo
        }
        else if (changes.equalsIgnoreCase("temperature")) {
            float tempNew = dispositivoNew.getTemperature().getValue();
            if (tempNew > 40.0) {
                nh.showNotification("Temperatura", "¿Es posible que tu casa esté ardiendo?", Constantes.CHANNEL_TEMPERATURE, R.mipmap.temperature_fire_icon, false);
            }
            else if (tempNew > 25.0) {
                nh.showNotification("Temperatura", "¡Con este calor no olvides hidratarte!", Constantes.CHANNEL_TEMPERATURE, R.mipmap.temperature_hot_icon, false);
            }
            else if (tempNew > 15.0) {
                nh.showNotification("Temperatura", "¡Hace una temperatura que da gusto!", Constantes.CHANNEL_TEMPERATURE, R.mipmap.temperature_ideal_icon, false);
            }
            else if (tempNew >= 5.0) {
                nh.showNotification("Temperatura", "Creo que deberías abrigarte...", Constantes.CHANNEL_TEMPERATURE, R.mipmap.temperature_cold_icon, false);
            }
            else if (tempNew < 5.0) {
                nh.showNotification("Temperatura", "Me da la sensación de estar en la Antártida...", Constantes.CHANNEL_TEMPERATURE, R.mipmap.temperature_ice_icon, false);
            }
            dispositivoOld.getTemperature().setValue(dispositivoNew.getTemperature().getValue());  // Se guarda el nuevo valor en el objeto antiguo para ser comaparado de nuevo
        }
    }

    // Clase para poder consumir datos de este servicio desde otras activities
    public class LocalBinder extends Binder {
        public ServiceListener getService() {
            return getInstance();  // Return this instance of ServiceListener so clients can call public methods
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = getInstance();  // Se inicializa el singleton del servicio cuando se completa el boot del sistema
        trace = new Trace();
        nh = new NotificationHelper(getApplicationContext());

        // Configure Firebase modules
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();  // Se obtiene el usuario actual
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Si no hay algun usuario con la sesion iniciada se limpia la sesion y se detiene el servicio
        if (currentUser == null) {
            mAuth.signOut();
            stopSelf();
            Log.d("ServiceStartup", "NOSESSION");
        }
        else {
            myRef.addValueEventListener(listener);  // Se añade el listener de Firebase
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("ServiceListener", "ONDESTROY");
        myRef.removeEventListener(listener);  // Se elimina el listener de Firebase cuando se detiene el servicio
    }
}