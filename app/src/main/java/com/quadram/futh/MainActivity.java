package com.quadram.futh;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.quadram.futh.helper.Constantes;
import com.quadram.futh.service.ServiceListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import me.aflak.libraries.callback.FingerprintDialogCallback;
import me.aflak.libraries.dialog.DialogAnimation;
import me.aflak.libraries.dialog.FingerprintDialog;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;
    private FirebaseUser currentUser;

    private FloatingActionButton fabAddDevices;

    private NavigationView navigationView;
    private Menu mDevices;

    private ServiceListener mService;
    private boolean mBound;
    private Map<String, String> devicesMap;

    private ImageView imgGoogle;
    private TextView txvNameGoogle, txvGmail;
    private DeviceFragment df;
    private boolean isFingerprintActivated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!isServiceRunning(ServiceListener.class)) {
            Intent i = new Intent(this, ServiceListener.class);
            startService(i);
        }

        checkSharedPreferences();  // Se comprueba si existe SharedPreferences, y de no existir se inicializa

        devicesMap = new HashMap<>();  // Se inicializa vacio el Map de dispositivos

        mBound = false;  // Por defecto se indica que no está enlazado al servicio de notificaciones

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        fabAddDevices = findViewById(R.id.fabAddDevices);

        fabAddDevices.setOnClickListener(view -> addDevice());

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Al abrir la app, creamos un submenu, que será el que contenga los item de los dispositivos
        Menu menu = navigationView.getMenu();
        mDevices = menu.addSubMenu("Devices");

        View hView = navigationView.getHeaderView(0);

        imgGoogle = hView.findViewById(R.id.fotoPerfil);
        txvNameGoogle = hView.findViewById(R.id.txvNameGoogle);
        txvGmail = hView.findViewById(R.id.txvGmail);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        firebaseAuthListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if(user != null){
                setUserData(user);
            }
            else{
                goLoginActivity();
            }
        };
        checkDevices(); // Se comprueba el estado del usuario en Real-Time Database
    }

    private void goLoginActivity() {
        Intent i = new Intent(this, LoginActivity.class);
        i.putExtra("activity", "mainactivity");
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    private void setUserData(FirebaseUser user) {
        txvNameGoogle.setText(user.getDisplayName());
        txvGmail.setText(user.getEmail());
        Glide.with(this)
                .load(user.getPhotoUrl())
                .into(imgGoogle);
    }


    private void addDevice() {
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        AlertDialog.Builder aBuilder = new AlertDialog.Builder(MainActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.dialog_add_device, null);

        final EditText etIdAddDevice = mView.findViewById(R.id.etIdAddDevice);
        Button btnAddDevice = mView.findViewById(R.id.btnAddDevice);

        aBuilder.setView(mView);
        final AlertDialog dialog = aBuilder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        btnAddDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String idDevice = etIdAddDevice.getText().toString().trim();  // Se obtiene el ID introducido

                reference.child("users").child(currentUser.getUid()).child("devices").child(idDevice).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.getValue() != null) {  // Si en Firebase ya existe no le dejamos al usuario añadirlo de nuevo
                            Toast.makeText(getApplicationContext(), "El ID introducido ya ha sido añadido", Toast.LENGTH_SHORT).show();
                        }
                        else if(idDevice.equals("")){  // Si no se introdujo nada se le muestra al usuario un Toast indicandolo
                            Toast.makeText(getApplicationContext(), "Debes introducir un ID", Toast.LENGTH_SHORT).show();
                        }
                        else {  // Sincronizamos el dispositivo con el usuario
                            devicesMap.put(idDevice, idDevice);
                            addDeviceFirebase(idDevice, dialog);
                        }
                        reference.removeEventListener(this);  // Se elimina el listener para liberar memoria
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        reference.removeEventListener(this);  // Se elimina el listener para liberar memoria
                    }
                });
            }
        });
    }

    private void addDeviceFirebase(String idDevice, AlertDialog dialog) {
        mDevices.add(R.id.gDevices, mDevices.size(),0,idDevice).setIcon(R.drawable.ic_arduino).setOnMenuItemClickListener(menuItem -> {
            checkSharedPreferences();  // Se actualizan las preferencias del usuario
            if (isFingerprintActivated) {  // Si la proteccion con huella esta activada
                if (!FingerprintDialog.isAvailable(getApplicationContext())) {  // Si el dispositivo no soporta la autenticacion con huella o no hay ninguna registrada
                    Toast.makeText(getApplicationContext(), "El dispositivo no soporta la autenticacion con huella dactilar o no hay ninguna registrada", Toast.LENGTH_LONG).show();
                    openFragmentDevice(idDevice);  // Se abre el fragment seleccionado
                }
                else {  // Si el dispositivo soporta autenticacion con huella
                    showFingerPrintDialog(idDevice);  // Se muestra el dialogo para autenticarse con huella dactilar
                }
            }
            else {  // No se ha activado la proteccion mediante huella dactilar
                openFragmentDevice(idDevice);  // Se abre el fragment seleccionado
            }
            return  onOptionsItemSelected(menuItem);
        });
        DatabaseReference refRaiz = FirebaseDatabase.getInstance().getReference();
        DatabaseReference refUsers = refRaiz.child("users").child(firebaseAuth.getCurrentUser().getUid());
        refUsers.child("devices").child(idDevice).setValue(idDevice);
        dialog.dismiss();
    }

    private void removeDevice() {
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        AlertDialog.Builder aBuilder = new AlertDialog.Builder(MainActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.dialog_delete_device, null);

        final EditText etIdDeleteDevice = mView.findViewById(R.id.etIdDeleteDevice);
        Button btnDeleteDevice = mView.findViewById(R.id.btnDeleteDevice);

        aBuilder.setView(mView);
        final AlertDialog dialog = aBuilder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        btnDeleteDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String idDevice = etIdDeleteDevice.getText().toString().trim();  // Se obtiene el ID introducido

                reference.child("users").child(currentUser.getUid()).child("devices").child(idDevice).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if(idDevice.equals("")){  // Si se dejo el EditText vacio
                            Toast.makeText(getApplicationContext(), "Debes introducir un ID", Toast.LENGTH_SHORT).show();
                        }
                        else if (snapshot.getValue() != null) {  // Si el dispositivo esta sincronizado con el usuario
                            for (int i=0; i<mDevices.size(); i++) {  // Se comprueban todos los items del menu
                                MenuItem mItem = mDevices.getItem(i);  // Se recupera el item
                                String mItemIdDevice = getKeyFromValue(devicesMap, mItem.getTitle().toString()).toString();  // Se recupera el id a partir del texto del item
                                if (idDevice.equalsIgnoreCase(mItemIdDevice)) {  // Si el id coincide con el del item
                                    mDevices.removeItem(mItem.getItemId());  // Se elimina el item del menu
                                    devicesMap.remove(mItemIdDevice);  // Se elimina el id del Map que los contiene
                                    reference.child("users").child(currentUser.getUid()).child("devices").child(idDevice).setValue(null);  // Se elimina el dispositivo sincronizado del usuario en Firebase
                                }
                            }
                            dialog.dismiss();  // Se cierra el dialogo
                        }
                        else {  // El dispositivo no esta sincronizado con el usuario
                            Toast.makeText(getApplicationContext(), "El dispositivo introducido no esta sincronizado", Toast.LENGTH_SHORT).show();
                        }
                        reference.removeEventListener(this);  // Se elimina el listener para liberar memoria
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        dialog.dismiss();
                        reference.removeEventListener(this);  // Se elimina el listener para liberar memoria
                    }
                });
            }
        });
    }

    private void renameDevice() {
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        AlertDialog.Builder aBuilder = new AlertDialog.Builder(MainActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.dialog_rename_device, null);

        final EditText etIdRenameDevice = mView.findViewById(R.id.etIdRenameDevice);
        final EditText etNewNameRenameDevice = mView.findViewById(R.id.etNewNameDevice);
        Button btnRenameDevice = mView.findViewById(R.id.btnRenameDevice);

        aBuilder.setView(mView);
        final AlertDialog dialog = aBuilder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        btnRenameDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String idDevice = etIdRenameDevice.getText().toString().trim();  // Se obtiene el ID introducido
                final String newNameDevice = etNewNameRenameDevice.getText().toString().trim();  // Se obtiene el ID introducido

                reference.child("users").child(currentUser.getUid()).child("devices").child(idDevice).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if(idDevice.equals("") && newNameDevice.equals("")){  // Si se dejo el EditText vacio
                            Toast.makeText(getApplicationContext(), "Debes rellenar todos los campos", Toast.LENGTH_SHORT).show();
                        }
                        else if (snapshot.getValue() != null) {  // Si el dispositivo esta sincronizado con el usuario
                            for (int i=0; i<mDevices.size(); i++) {  // Se comprueban todos los items del menu
                                MenuItem mItem = mDevices.getItem(i);  // Se recupera el item
                                String mItemIdDevice = getKeyFromValue(devicesMap, mItem.getTitle().toString()).toString();  // Se recupera el id a partir del texto del item
                                if (idDevice.equalsIgnoreCase(mItemIdDevice)) {  // Si el id coincide con el del item
                                    if (devicesMap.containsValue(newNameDevice)) {  // Ya existe un dispositivo con ese nombre
                                        Toast.makeText(getApplicationContext(), "Ya existe un dispositivo con ese nombre", Toast.LENGTH_SHORT).show();
                                    }
                                    else {  // El nombre esta libre
                                        // Se actualiza el nombre del boton en el hamburger menu y su OnMenuItemClickListener
                                        mDevices.getItem(mItem.getItemId()).setTitle(newNameDevice).setOnMenuItemClickListener(menuItem -> {
                                            checkSharedPreferences();  // Se actualizan las preferencias del usuario
                                            if (isFingerprintActivated) {  // Si la proteccion con huella esta activada
                                                if (!FingerprintDialog.isAvailable(getApplicationContext())) {  // Si el dispositivo no soporta la autenticacion con huella o no hay ninguna registrada
                                                    Toast.makeText(getApplicationContext(), "El dispositivo no soporta la autenticacion con huella dactilar o no hay ninguna registrada", Toast.LENGTH_LONG).show();
                                                    openFragmentDevice(newNameDevice);  // Se abre el fragment seleccionado
                                                }
                                                else {  // Si el dispositivo soporta autenticacion con huella
                                                    showFingerPrintDialog(newNameDevice);  // Se muestra el dialogo para autenticarse con huella dactilar
                                                }
                                            }
                                            else {  // No se ha activado la proteccion mediante huella dactilar
                                                openFragmentDevice(newNameDevice);  // Se abre el fragment seleccionado
                                            }
                                            return onOptionsItemSelected(menuItem);
                                        });
                                        devicesMap.put(mItemIdDevice, newNameDevice);  // Se actualiza el nombre del dispositivo en el Map que los contiene
                                        reference.child("users").child(currentUser.getUid()).child("devices").child(idDevice).setValue(newNameDevice);  // Se renombra el dispositivo sincronizado del usuario en Firebase
                                        dialog.dismiss();  // Se cierra el dialogo
                                    }
                                }
                            }
                        }
                        else {  // El dispositivo no esta sincronizado con el usuario
                            Toast.makeText(getApplicationContext(), "El dispositivo introducido no esta sincronizado", Toast.LENGTH_SHORT).show();
                        }
                        reference.removeEventListener(this);  // Se elimina el listener para liberar memoria
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        dialog.dismiss();
                        reference.removeEventListener(this);  // Se elimina el listener para liberar memoria
                    }
                });
            }
        });
    }

    private void openFragmentDevice(String idDevice) {
        Bundle args = new Bundle();
        idDevice = getKeyFromValue(devicesMap, idDevice).toString();
        args.putString("idDevice", idDevice);
        args.putString("deviceName", devicesMap.get(idDevice));
        df = new DeviceFragment();
        df.setArguments(args);
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.containerFragment, df).commit();
    }

    private void openFragmentWelcome(){
        WelcomeFragment dfWelcome = new WelcomeFragment();
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.containerFragment,dfWelcome).commit();
    }

    private void openFragmentSettings() {
        SettingsFragment sf = new SettingsFragment();
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.containerFragment, sf).commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        firebaseAuth.addAuthStateListener(firebaseAuthListener);
        // Bind to LocalService
        Intent i = new Intent(this, ServiceListener.class);
        bindService(i, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            checkSharedPreferences();
            if (isFingerprintActivated && FingerprintDialog.isAvailable(getApplicationContext())) {
                showFingerPrintDialog(Constantes.ACTION_SETTINGS);
            }
            else {
                openFragmentSettings();
            }
        }
        else if (id == R.id.action_rename) {
            checkSharedPreferences();
            if (isFingerprintActivated && FingerprintDialog.isAvailable(getApplicationContext())) {
                showFingerPrintDialog(Constantes.ACTION_RENAME);
            }
            else {
                renameDevice();
            }
        }
        else if (id == R.id.action_delete) {
            checkSharedPreferences();
            if (isFingerprintActivated && FingerprintDialog.isAvailable(getApplicationContext())) {
                showFingerPrintDialog(Constantes.ACTION_DELETE);
            }
            else {
                removeDevice();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_logout) {
            firebaseAuth.signOut();
            stopService(new Intent(getApplicationContext(), ServiceListener.class));  // Se detiene el listener de Firebase a la vez que se cierra sesion
            goLoginActivity();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }

        if (firebaseAuthListener != null) {
            firebaseAuth.removeAuthStateListener(firebaseAuthListener);
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            ServiceListener.LocalBinder binder = (ServiceListener.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            Log.d("MainActivity", "CONNECTED");
        }

        @Override
        public void onServiceDisconnected(ComponentName cn) {
            mBound = false;
        }
    };

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void checkDevices() {
        // Se comprueba si es la primera vez que inicia sesion y se le asigna un perfil en Real-Time Database
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        ValueEventListener vel = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    devicesMap = (HashMap<String,String>) snapshot.getValue();  // Se obtienen los dispositivos en formato clave:valor
                    final ArrayList<String> devices = new ArrayList<>(devicesMap.values());  // Se meten los valores en un ArrayList

                    for (int i = 0; i < devices.size(); i++) {
                        final int finalI = i;
                        mDevices.add(R.id.gDevices, mDevices.size(),0,devices.get(i)).setIcon(R.drawable.ic_arduino).setOnMenuItemClickListener(menuItem -> {
                            String device = devices.get(finalI);  // Se obtiene el id del dispositivo
                            checkSharedPreferences();  // Se actualizan las preferencias del usuario
                            if (isFingerprintActivated) {  // Si la proteccion con huella esta activada
                                if (!FingerprintDialog.isAvailable(getApplicationContext())) {  // Si el dispositivo no soporta la autenticacion con huella o no hay ninguna registrada
                                    Toast.makeText(getApplicationContext(), "El dispositivo no soporta la autenticacion con huella dactilar o no hay ninguna registrada", Toast.LENGTH_LONG).show();
                                    openFragmentDevice(device);  // Se abre el fragment seleccionado
                                }
                                else {  // Si el dispositivo soporta autenticacion con huella
                                    showFingerPrintDialog(device);  // Se muestra el dialogo para autenticarse con huella dactilar
                                }
                            }
                            else {  // No se ha activado la proteccion mediante huella dactilar
                                openFragmentDevice(device);  // Se abre el fragment seleccionado
                            }
                            return onOptionsItemSelected(menuItem);
                        });
                        Log.d("DEVICE", devices.get(i));
                    }
                    // Se abre el primer dispositivo acorde a los ajustes de seguridad que tenga el usuario
                    if (isFingerprintActivated) {  // Si la proteccion con huella esta activada
                        if (!FingerprintDialog.isAvailable(getApplicationContext())) {  // Si el dispositivo no soporta la autenticacion con huella o no hay ninguna registrada
                            Toast.makeText(getApplicationContext(), "El dispositivo no soporta la autenticacion con huella dactilar o no hay ninguna registrada", Toast.LENGTH_LONG).show();
                            openFragmentDevice(devices.get(0));  // Se abre el fragment seleccionado
                        }
                        else {  // Si el dispositivo soporta autenticacion con huella
                            showFingerPrintDialog(devices.get(0));  // Se muestra el dialogo para autenticarse con huella dactilar
                        }
                    }
                    else {  // No se ha activado la proteccion mediante huella dactilar
                        openFragmentDevice(devices.get(0));  // Se abre el fragment seleccionado
                    }
                }
                else {
                    openFragmentWelcome();
                }
                reference.removeEventListener(this);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("USER", databaseError.getMessage());
                reference.removeEventListener(this);
            }
        };
        reference.child("users").child(currentUser.getUid()).child("devices").addListenerForSingleValueEvent(vel);
    }

    // Funcion para obtener la clave a partir de un valor en un Map
    public Object getKeyFromValue(Map hm, Object value) {
        for (Object o : hm.keySet()) {
            if (hm.get(o).equals(value)) {
                return o;
            }
        }
        return null;
    }

    private void showFingerPrintDialog(String device) {
        FingerprintDialog.initialize(this)
            .title("Autenticación")  // Titulo del dialogo
            .message("Huella dactilar requerida")  // Subtitulo del dialogo
            .enterAnimation(DialogAnimation.Enter.LEFT)  // Animacion de aparicion
            .exitAnimation(DialogAnimation.Exit.RIGHT)  // Animacion de desaparicion
            .circleSuccessColor(android.R.color.holo_green_light)  // Color del fondo del icono de autenticacion exitosa
            .statusSuccessColor(android.R.color.holo_green_dark)  // Color del texto de autenticacion exitosa
            .fingerprintSuccessColor(android.R.color.white)  // Color del icono de autenticacion exitosa
            .circleErrorColor(android.R.color.holo_red_light)  // Color del fondo del icono de autenticacion fallida
            .statusErrorColor(android.R.color.holo_red_dark)  // Color del texto de autenticacion fallida
            .fingerprintErrorColor(android.R.color.white)  // Color del icono de autenticacion fallida
            .circleScanningColor(R.color.colorPrimary)  // Color del fondo del icono de autenticacion con huella
            .statusScanningColor(R.color.colorPrimaryDark)  // Color del texto de autenticacion con huella
            .fingerprintScanningColor(android.R.color.white)  // Color del icono de autenticacion con huella
            .cancelOnPressBack(true)  // Se permite que se cancele el dialogo pulsando el boton de atras
            .cancelOnTouchOutside(true)  // Se permite que se cancele el dialogo pulsando fuera de el
            .tryLimit(3, (fingerprintDialog) -> {  // Se establece el numero de intentos
                removeFragments();
                fingerprintDialog.dismiss();  // Se cierra el dialogo cuando se alcanza el limite
                Toast.makeText(getApplicationContext(), "Has alcanzado el limite de intentos", Toast.LENGTH_LONG).show();
            })
            .callback(new FingerprintDialogCallback() {
                @Override
                public void onAuthenticationSucceeded() {
                    if (device.equals(Constantes.ACTION_SETTINGS)){  // Si se indica que es para el fragment de settings
                        openFragmentSettings();
                    }
                    else if (device.equals(Constantes.ACTION_RENAME)){  // Si se indica que es para el dialogo de renombrar dispositivo
                        renameDevice();
                    }
                    else if (device.equals(Constantes.ACTION_DELETE)){  // Si se indica que es para el dialogo de eliminar dispositivo
                        removeDevice();
                    }
                    else {
                        openFragmentDevice(device);  // Se abre el fragment seleccionado
                    }
                }

                @Override
                public void onAuthenticationCancel() {
                    Toast.makeText(getApplicationContext(), "Se ha cancelado la operación", Toast.LENGTH_LONG).show();
                    removeFragments();
                }
            })
            .show();
    }

    private void removeFragments() {
        if (getSupportFragmentManager().getFragments() != null && getSupportFragmentManager().getFragments().size() > 1) {
            for (int i = 1; i < getSupportFragmentManager().getFragments().size(); i++) {
                Fragment mFragment = getSupportFragmentManager().getFragments().get(i);
                if (mFragment != null) {
                    getSupportFragmentManager().beginTransaction().remove(mFragment).commit();
                }
            }
        }
        openFragmentWelcome();
    }

    private void checkSharedPreferences() {
        SharedPreferences sp = getSharedPreferences(Constantes.SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor;
        if (!sp.contains(Constantes.SHARED_PREFERENCES_FINGERPRINT)) {
            editor = sp.edit();
            editor.putBoolean(Constantes.SHARED_PREFERENCES_FINGERPRINT, false);
            editor.apply();
        }
        else {
            isFingerprintActivated = sp.getBoolean(Constantes.SHARED_PREFERENCES_FINGERPRINT, false);
        }
        Log.d("SHAREDPREFERENCES", "checkSharedPreferences: "+isFingerprintActivated);
    }
}
