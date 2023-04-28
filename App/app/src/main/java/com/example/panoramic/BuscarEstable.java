package com.example.panoramic;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.panoramic.databinding.ActivityBuscarEstableBinding;

public class BuscarEstable extends AppCompatActivity {

    ActivityBuscarEstableBinding binding;
    Button filtrar, verDetalles, abrirMapa;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBuscarEstableBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        filtrar = binding.button11;
        verDetalles = binding.button14;
        // Boton de abrir el mapa
        abrirMapa = binding.button8;


        filtrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(BuscarEstable.this, Filtrar.class);
                startActivity(intent);
            }
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
                Intent intent = new Intent(BuscarEstable.this,VerMapa.class);
                startActivity(intent);
            }
        });


    }
}