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
import com.quadram.futh.helper.Trace;
import com.quadram.futh.model.Device;
import com.quadram.futh.model.Gas;
import com.quadram.futh.model.Humidity;
import com.quadram.futh.model.Relay;
import com.quadram.futh.model.Temperature;

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
    //private static NotificationHelper NH;

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

            }
            else if (dispositivoNew.getGas().getRisk() == 1) {

            }
            else if (dispositivoNew.getGas().getRisk() == 2) {

            }
            dispositivoOld.getGas().setRisk(dispositivoNew.getGas().getRisk());  // Se guarda el nuevo valor en el objeto antiguo para ser comaparado de nuevo
        }
        else if (changes.equalsIgnoreCase("humidity")) {
            if (dispositivoNew.getHumidity().getValue() <= 25) {

            }
            else if (dispositivoNew.getHumidity().getValue() <= 50) {

            }
            else if (dispositivoNew.getHumidity().getValue() <= 75) {

            }
            else if (dispositivoNew.getHumidity().getValue() <= 100) {

            }
            dispositivoOld.getHumidity().setValue(dispositivoNew.getHumidity().getValue());  // Se guarda el nuevo valor en el objeto antiguo para ser comaparado de nuevo
        }
        else if (changes.equalsIgnoreCase("light")) {
            if (dispositivoNew.getLight().getState().equalsIgnoreCase("on")) {

            }
            else if (dispositivoNew.getLight().getState().equalsIgnoreCase("off")) {

            }
            dispositivoOld.getLight().setState(dispositivoNew.getLight().getState());  // Se guarda el nuevo valor en el objeto antiguo para ser comaparado de nuevo
        }
        else if (changes.equalsIgnoreCase("plug")) {
            if (dispositivoNew.getPlug().getState().equalsIgnoreCase("on")) {

            }
            else if (dispositivoNew.getPlug().getState().equalsIgnoreCase("off")) {

            }
            dispositivoOld.getPlug().setState(dispositivoNew.getPlug().getState());  // Se guarda el nuevo valor en el objeto antiguo para ser comaparado de nuevo
        }
        else if (changes.equalsIgnoreCase("temperature")) {
            if (dispositivoNew.getTemperature().getValue() > 40.0) {
                //NH.showNotification("Temperatura", "¿Me explicas cómo cojones hace "+dispositivoNew.getTemperatura().getValue()+" grados en tu casa?", android.R.drawable.stat_notify_chat);
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
        //NH = new NotificationHelper(getApplicationContext());

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