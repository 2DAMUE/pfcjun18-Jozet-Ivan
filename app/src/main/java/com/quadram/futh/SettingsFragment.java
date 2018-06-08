package com.quadram.futh;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import com.quadram.futh.helper.Constantes;

import static android.content.Context.MODE_PRIVATE;


public class SettingsFragment extends Fragment implements View.OnClickListener {
    private Switch swFingerprint;
    private boolean isFingerprintActivated;

    public SettingsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_settings, container, false);

        swFingerprint = v.findViewById(R.id.swFingerprint);
        swFingerprint.setOnClickListener(this);

        checkSwitchFingerprint();

        return v;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.swFingerprint) {
            processSwitchFingerprint();
        }
    }

    private void processSwitchFingerprint() {
        SharedPreferences sp = getContext().getSharedPreferences(Constantes.SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor;
        editor = sp.edit();
        if (swFingerprint.isChecked()) {
            Log.d("SWITCH", "CHECKED");
            editor.putBoolean(Constantes.SHARED_PREFERENCES_FINGERPRINT, true);
        }
        else {
            Log.d("SWITCH", "NOCHECKED");
            editor.putBoolean(Constantes.SHARED_PREFERENCES_FINGERPRINT, false);
        }
        editor.apply();
    }

    private void checkSwitchFingerprint() {
        SharedPreferences sp = getContext().getSharedPreferences(Constantes.SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        isFingerprintActivated = sp.getBoolean(Constantes.SHARED_PREFERENCES_FINGERPRINT, false);
        if (isFingerprintActivated) {
            swFingerprint.setChecked(true);
        }
        else {
            swFingerprint.setChecked(false);
        }
    }
}
