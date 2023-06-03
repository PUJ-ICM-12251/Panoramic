package com.example.panoramic;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.panoramic.home.BuscarEstable;

public class Filtrar extends AppCompatActivity {

    Button aplicar;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filtrar);

        aplicar = findViewById(R.id.botonAp);

        aplicar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Filtrar.this, BuscarEstable.class);
                startActivity(intent);
            }
        });
    }
}