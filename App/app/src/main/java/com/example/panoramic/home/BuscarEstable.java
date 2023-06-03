package com.example.panoramic.home;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.panoramic.Filtrar;
import com.example.panoramic.Menu;
import com.example.panoramic.VerDetalles;
import com.example.panoramic.VerMapa;
import com.example.panoramic.adapters.ClubAdapterLarge;
import com.example.panoramic.databinding.ActivityBuscarEstableBinding;
import com.example.panoramic.services.ClubService;

import javax.inject.Inject;

public class BuscarEstable extends AppCompatActivity {

    // Sensor
    private SensorManager sensorManager;
    private Sensor stepSensor;
    private SensorEventListener stepSensorListener;

    private RecyclerView.Adapter adapter;
    private ActivityBuscarEstableBinding binding;
    private Button filtrar, verDetalles, abrirMapa, perfil;
    private TextView steps;
    private final int ACTIVITY_RECOGNITION_CODE = 101;

    @Inject
    ClubService clubService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBuscarEstableBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        filtrar = binding.button11;
        verDetalles = binding.button14;
        abrirMapa = binding.button8;
        perfil = binding.profileButton;
        steps = binding.stepCountTextView;

        // Sensor
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        stepSensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                steps.setText(String.valueOf(Math.round(event.values[0])));
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {}
        };

        requestPermission(this, "android.Manifest.permission.ACTIVITY_RECOGNITION", "Es necesario este permiso para contar los pasos", ACTIVITY_RECOGNITION_CODE);

        filtrar.setOnClickListener(view1 -> {
            Intent intent = new Intent(BuscarEstable.this, Filtrar.class);
            startActivity(intent);
        });

        verDetalles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(BuscarEstable.this, VerDetalles.class);
                startActivity(intent);
            }
        });

        abrirMapa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(BuscarEstable.this, VerMapa.class);
                startActivity(intent);
            }
        });

        perfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(BuscarEstable.this, Menu.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(stepSensorListener, stepSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(stepSensorListener, stepSensor);
    }

    private void requestPermission(Activity context, String permiso, String justificacion, int idCode) {
        if (ContextCompat.checkSelfPermission(context, permiso) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(context, permiso)) {
                Toast.makeText(context, justificacion, Toast.LENGTH_SHORT).show();
            }
            ActivityCompat.requestPermissions(context, new String[]{permiso}, idCode);
        } else {
            Toast.makeText(context, "Permiso concedido", Toast.LENGTH_SHORT).show();
        }
    }
}
