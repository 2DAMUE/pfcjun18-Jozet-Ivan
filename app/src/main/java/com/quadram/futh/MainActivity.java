package com.quadram.futh;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
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
import com.quadram.futh.service.ServiceListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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

    ImageView imgGoogle;
    TextView txvNameGoogle, txvGmail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!isServiceRunning(ServiceListener.class)) {
            Intent i = new Intent(this, ServiceListener.class);
            startService(i);
        }

        mBound = false;

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        fabAddDevices = findViewById(R.id.fabAddDevices);

        fabAddDevices.setOnClickListener(view -> addDevices());

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


    private void addDevices() {
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        AlertDialog.Builder aBuilder = new AlertDialog.Builder(MainActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.dialog_add_devices, null);

        final EditText etIdDevice = mView.findViewById(R.id.etIdDevice);
        Button btnAddDevice = mView.findViewById(R.id.btnAdd);

        aBuilder.setView(mView);
        final AlertDialog dialog = aBuilder.create();
        dialog.show();

        btnAddDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String idDevice = etIdDevice.getText().toString().trim();  // Se obtiene el ID introducido

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
        mDevices.add(R.id.gDevices,101,0,idDevice).setIcon(R.drawable.ic_arduino).setOnMenuItemClickListener(menuItem -> {
            openFragmentDevice(idDevice);
            return  onOptionsItemSelected(menuItem);
        });
        DatabaseReference refRaiz = FirebaseDatabase.getInstance().getReference();
        DatabaseReference refUsers = refRaiz.child("users").child(firebaseAuth.getCurrentUser().getUid());
        refUsers.child("devices").child(idDevice).setValue(idDevice);
        dialog.dismiss();
    }

    private void openFragmentDevice(String idDevice) {
        Bundle args = new Bundle();
        idDevice = getKeyFromValue(devicesMap, idDevice).toString();
        args.putString("idDevice", idDevice);
        DeviceFragment df = new DeviceFragment();
        df.setArguments(args);
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.containerFragment, df).commit();
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
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
            finish();
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
                        mDevices.add(devices.get(i)).setIcon(R.drawable.ic_arduino).setOnMenuItemClickListener(menuItem -> {
                            openFragmentDevice(devices.get(finalI));
                            return onOptionsItemSelected(menuItem);
                        });
                        Log.d("DEVICE", devices.get(i));
                    }
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
}
