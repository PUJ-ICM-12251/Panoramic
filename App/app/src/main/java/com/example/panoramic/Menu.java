package com.example.panoramic;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class Menu extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
    }

    public void watchProfile(View view) {
        Intent intent = new Intent(Menu.this, EditarPerfil.class);
        startActivity(intent);
    }


    public void watchUsers(View view) {
        Intent intent = new Intent(Menu.this, ListarUsuarios.class);
        startActivity(intent);
    }
}