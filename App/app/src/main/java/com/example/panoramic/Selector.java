package com.example.panoramic;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.panoramic.databinding.ActivitySelectorBinding;

public class Selector extends AppCompatActivity {
    private ActivitySelectorBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySelectorBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
    }

    public void loadCamera(View view) {
        Intent intent = new Intent(Selector.this, Menu.class);
        startActivity(intent);
    }

}