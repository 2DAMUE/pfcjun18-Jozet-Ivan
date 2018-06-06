package com.quadram.futh;


import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.quadram.futh.model.Device;
import com.quadram.futh.model.Gas;
import com.quadram.futh.model.Humidity;
import com.quadram.futh.model.Relay;
import com.quadram.futh.model.Temperature;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import static android.support.constraint.Constraints.TAG;


/**
 * A simple {@link Fragment} subclass.
 */
public class DeviceFragment extends Fragment {

    private DatabaseReference ref, refDevice;
    private ValueEventListener vel;
    private Device device;

    private CardView cvGas, cvHumidity, cvLight, cvPlug, cvTemperature;

    private Calendar date;

    private ImageView imgGas, imgHumidity, imgRele1, imgRele2, imgTemperature;
    private TextView  txvGasName, txvGasRisk;
    private TextView txvHumidityName, txvHumidityValue;
    private TextView txvRele1Name;
    private TextView txvRele2Name;
    private TextView txvTemperatureName, txvTemperatureValue;

    public DeviceFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_device, container, false);
        String idDevice = getArguments().getString("idDevice");

        ref = FirebaseDatabase.getInstance().getReference();
        refDevice = ref.child("devices").child(idDevice);

        imgGas = v.findViewById(R.id.idGasImg);
        imgHumidity = v.findViewById(R.id.idHumidityImg);
        imgRele1 = v.findViewById(R.id.idRele1Img);
        imgRele2 = v.findViewById(R.id.idRele2Img);
        imgTemperature = v.findViewById(R.id.idTemperatureImg);

        txvGasName = v.findViewById(R.id.idGasName);
        txvGasRisk = v.findViewById(R.id.idGasRisk);

        txvHumidityName = v.findViewById(R.id.idHumidityName);
        txvHumidityValue = v.findViewById(R.id.idHumidityValue);

        txvRele1Name = v.findViewById(R.id.idRele1Name);

        txvRele2Name = v.findViewById(R.id.idRele2Name);

        txvTemperatureName = v.findViewById(R.id.idTemperatureName);
        txvTemperatureValue = v.findViewById(R.id.idTemperatureValue);

        device = new Device();
        recogerDatosFirebase();

        // Recuperar los cardview
        cvGas = v.findViewById(R.id.cardViewGas);
        cvHumidity = v.findViewById(R.id.cardViewHumidity);
        cvLight = v.findViewById(R.id.cardViewLight);
        cvPlug = v.findViewById(R.id.cardViewPlug);
        cvTemperature = v.findViewById(R.id.cardViewTemperature);

        // COLORES CARDVIEW
        cvGas.setCardBackgroundColor(Color.parseColor("#ffb3ba"));
        cvHumidity.setCardBackgroundColor(Color.parseColor("#bae1ff"));

        // Añadir listener a los cardview
        cvGas.setOnClickListener((view) -> onClickGas());
        cvHumidity.setOnClickListener((view) -> onClickHumidity());
        cvLight.setOnClickListener((view) -> onClickLight());
        cvPlug.setOnClickListener((view) -> onClickPlug());
        cvTemperature.setOnClickListener((view) -> onClickTemperature());

        cvLight.setOnLongClickListener((view) -> showLightTimerPicker());
        cvPlug.setOnLongClickListener((view) -> showPlugTimerPicker());

        return v;
    }

    public boolean showLightTimerPicker() {
        final Calendar currentDate = Calendar.getInstance();
        date = Calendar.getInstance();
        new DatePickerDialog(getContext(), (view, year, monthOfYear, dayOfMonth) -> {
            date.set(year, monthOfYear, dayOfMonth);
            new TimePickerDialog(getContext(), (view1, hourOfDay, minute) -> {
                date.set(Calendar.HOUR_OF_DAY, hourOfDay);
                date.set(Calendar.MINUTE, minute);
                date.set(Calendar.SECOND, 0);

                long futureTime = TimeUnit.MILLISECONDS.toSeconds(date.getTimeInMillis());
                long currentTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
                long leftTime = futureTime-currentTime;
                Log.d("ONLONGCLICK", "TIEMPO RESTANTE: " + leftTime + " SEGUNDOS");

                if (leftTime > 0) {  // Si se ha seleccionado una fecha posterior a la actual
                    refDevice.child("rele1").child("timer").setValue(leftTime);
                }
                else {  // Si se ha seleccionado una fecha anterior a la actual
                    Toast.makeText(getContext(), "No se puede establecer un temporizador para el pasado", Toast.LENGTH_LONG).show();
                }
            }, currentDate.get(Calendar.HOUR_OF_DAY), currentDate.get(Calendar.MINUTE), true).show();
        }, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DATE)).show();
        return true;
    }

    public boolean showPlugTimerPicker() {
        final Calendar currentDate = Calendar.getInstance();
        date = Calendar.getInstance();
        new DatePickerDialog(getContext(), (view, year, monthOfYear, dayOfMonth) -> {
            date.set(year, monthOfYear, dayOfMonth);
            new TimePickerDialog(getContext(), (view1, hourOfDay, minute) -> {
                date.set(Calendar.HOUR_OF_DAY, hourOfDay);
                date.set(Calendar.MINUTE, minute);
                date.set(Calendar.SECOND, 0);

                long futureTime = TimeUnit.MILLISECONDS.toSeconds(date.getTimeInMillis());
                long currentTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
                long leftTime = futureTime-currentTime;
                Log.d("ONLONGCLICK", "TIEMPO RESTANTE: " + leftTime + " SEGUNDOS");

                if (leftTime > 0) {  // Si se ha seleccionado una fecha posterior a la actual
                    refDevice.child("rele2").child("timer").setValue(leftTime);
                }
                else {  // Si se ha seleccionado una fecha anterior a la actual
                    Toast.makeText(getContext(), "No se puede establecer un temporizador para el pasado", Toast.LENGTH_LONG).show();
                }
            }, currentDate.get(Calendar.HOUR_OF_DAY), currentDate.get(Calendar.MINUTE), true).show();
        }, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DATE)).show();
        return true;
    }

    private void onClickGas() {
        Log.d("CARDVIEW", "Dentro del cardview onClickGas");
    }

    private void onClickHumidity() {
        Log.d("CARDVIEW", "Dentro del cardview onClickHumidity");
    }

    private void onClickLight() {
        Log.d("CARDVIEW", "Dentro del cardview onClickLight");
        if (device.getLight().getState().equalsIgnoreCase("off")) {
            refDevice.child("rele1").child("state").setValue("on");
        }
        else if (device.getLight().getState().equalsIgnoreCase("on")) {
            refDevice.child("rele1").child("state").setValue("off");
        }
    }

    private void onClickPlug() {
        Log.d("CARDVIEW", "Dentro del cardview onClickPlug");
        if (device.getPlug().getState().equalsIgnoreCase("off")) {
            refDevice.child("rele2").child("state").setValue("on");
        }
        else if (device.getPlug().getState().equalsIgnoreCase("on")) {
            refDevice.child("rele2").child("state").setValue("off");
        }
    }

    private void onClickTemperature() {
        Log.d("CARDVIEW", "Dentro del cardview onClickTemperature");
    }


    private void recogerDatosFirebase() {
        vel = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    device.setGas(dataSnapshot.child("gas1").getValue(Gas.class));
                    device.setHumidity(dataSnapshot.child("humidity1").getValue(Humidity.class));
                    device.setLight(dataSnapshot.child("rele1").getValue(Relay.class));
                    device.setPlug(dataSnapshot.child("rele2").getValue(Relay.class));
                    device.setTemperature(dataSnapshot.child("temperature1").getValue(Temperature.class));

                    rellenarCardView();
                }
                else {
                    Toast.makeText(getContext(),"No hay informacion disponible",Toast.LENGTH_LONG).show();
                }
                ref.removeEventListener(vel);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        refDevice.addValueEventListener(vel);
    }

    private void rellenarCardView() {
        // GAS
        txvGasName.setText(device.getGas().getName());
        txvGasRisk.setText(String.valueOf(device.getGas().getRisk()));
        imgGas.setImageResource(R.drawable.ic_gas_icon_vector);

        // HUMEDAD
        txvHumidityName.setText(device.getHumidity().getName());
        txvHumidityValue.setText(String.valueOf(device.getHumidity().getValue()));
        imgHumidity.setImageResource(R.drawable.ic_humidity_icon_vector);

        // LUZ
        txvRele1Name.setText(device.getLight().getName());
        if(device.getLight().getState().equals("on")){
            imgRele1.setImageResource(R.drawable.ic_light_on_icon_vector);
            cvLight.setCardBackgroundColor(Color.parseColor("#ffffba"));
        }
        else {
            imgRele1.setImageResource(R.drawable.ic_light_off_icon_vector);
            cvLight.setCardBackgroundColor(null);
        }

        // ENCHUFE
        txvRele2Name.setText(device.getPlug().getName());
        if(device.getPlug().getState().equals("on")){
            imgRele2.setImageResource(R.drawable.ic_connected_icon_vector);
            cvPlug.setCardBackgroundColor(Color.parseColor("#baffc9"));
        }
        else {
            imgRele2.setImageResource(R.drawable.ic_disconnected_icon_vector);
            cvPlug.setCardBackgroundColor(null);
        }

        // TEMPERATURA
        txvTemperatureName.setText(device.getTemperature().getName());
        txvTemperatureValue.setText(String.valueOf(device.getTemperature().getValue()));
        imgTemperature.setImageResource(R.drawable.ic_temperature_icon_vector);
    }

}
