package com.example.panoramic;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class BuscarEstable extends AppCompatActivity {

    Button boton1;
    Button boton2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buscar_estable);
        boton1 = findViewById(R.id.button11);
        boton2 = findViewById(R.id.button14);

        boton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(BuscarEstable.this, Filtrar.class);
                startActivity(intent);
            }
        });

        boton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(BuscarEstable.this, VerDetalles.class);
                startActivity(intent);
            }
        });
    }
}