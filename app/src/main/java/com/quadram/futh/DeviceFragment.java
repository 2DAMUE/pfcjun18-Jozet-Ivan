package com.quadram.futh;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
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


/**
 * A simple {@link Fragment} subclass.
 */
public class DeviceFragment extends Fragment {

    private DatabaseReference ref, refDevice;
    private ValueEventListener vel;
    private Device device;

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

        // Añadir listener a los cardview
        CardView cvGas = v.findViewById(R.id.cardViewGas);
        CardView cvHumidity = v.findViewById(R.id.cardViewHumidity);
        CardView cvLight = v.findViewById(R.id.cardViewLight);
        CardView cvPlug = v.findViewById(R.id.cardViewPlug);
        CardView cvTemperature = v.findViewById(R.id.cardViewTemperature);

        cvGas.setOnClickListener((view) -> onClickGas(view));




        return v;
    }

    private void onClickGas(View v) {
        Log.d("CARDVIEW", "Dentro del cardview onClick");
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
                else{
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
        txvGasName.setText(device.getGas().getName());
        txvGasRisk.setText(String.valueOf(device.getGas().getRisk()));
        imgGas.setImageResource(R.mipmap.gas_risk_one_icon);


        txvHumidityName.setText(device.getHumidity().getName());
        txvHumidityValue.setText(String.valueOf(device.getHumidity().getValue()));
        imgHumidity.setImageResource(R.mipmap.humidity_notification_icon);

        txvRele1Name.setText(device.getLight().getName());

        if(device.getLight().getState().equals("on")){
            imgRele1.setImageResource(R.mipmap.light_on_notification_icon);
        }
        else{
            imgRele1.setImageResource(R.mipmap.light_off_notification_icon);
        }


        txvRele2Name.setText(device.getPlug().getName());

        if(device.getPlug().getState().equals("on")){
            imgRele2.setImageResource(R.mipmap.connected_icon);
        }
        else{
            imgRele2.setImageResource(R.mipmap.disconnected_icon);
        }

        txvTemperatureName.setText(device.getTemperature().getName());
        txvTemperatureValue.setText(String.valueOf(device.getTemperature().getValue()));

        imgTemperature.setImageResource(R.mipmap.notification_temperature_icon);
    }

}
