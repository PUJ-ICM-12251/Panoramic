package com.example.panoramic;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class EditarPerfil extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_perfil);
    }

    public void edit(View view) {
        Intent intent = new Intent(EditarPerfil.this, CameraActivity.class);
        startActivity(intent);
    }


}