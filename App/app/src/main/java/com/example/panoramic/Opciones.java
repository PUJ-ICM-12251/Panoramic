package com.example.panoramic;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class Opciones extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opciones);
    }

    public void watchProfile(View view) {
        Intent intent = new Intent(Opciones.this, EditarPerfil.class);
        startActivity(intent);
    }
}