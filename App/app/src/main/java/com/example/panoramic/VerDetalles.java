package com.example.panoramic;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.panoramic.home.BuscarEstable;

public class VerDetalles extends AppCompatActivity {

    Button regresar;
    TextView puntaje;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_detalles);

        regresar = findViewById(R.id.btnRegresar);
        puntaje = findViewById(R.id.puntuacionLugar);

        regresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(VerDetalles.this, BuscarEstable.class);
                startActivity(intent);
            }
        });

        puntaje.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(VerDetalles.this, Review.class);
                startActivity(intent);
            }
        });
    }
}