package com.example.panoramic;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class EditarPerfil extends AppCompatActivity {
    private FirebaseAuth mAuth;
    ImageView perfil;
    TextView nombre, correo;
    String storage_path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_perfil);
        nombre = findViewById(R.id.textView2);
        correo = findViewById(R.id.textView6);
        perfil = findViewById(R.id.imageView6);
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        mAuth = FirebaseAuth.getInstance();


        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("user").child(userID);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String username = dataSnapshot.child("name").getValue(String.class);
                    String email = dataSnapshot.child("mail").getValue(String.class);
                    nombre.setText(username);
                    correo.setText(email);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Manejar el error de consulta a la base de datos
                Log.e("EditarPerfil", "Error al obtener los datos del usuario", databaseError.toException());
            }
        });
        System.out.println("userID: " + userID);
        storage_path = "fotos/" + userID;
        StorageReference imageRef = storageRef.child(storage_path);

        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            String imageUrl = uri.toString();
            Glide.with(this)
                    .load(imageUrl)
                    .into(perfil);
        }).addOnFailureListener(e -> {
            // Manejar la falla al obtener la URL de descarga de la imagen
            Log.e("EditarPerfil", "Error al obtener la URL de descarga de la imagen", e);
        });
    }


    public void edit(View view) {
        Intent intent = new Intent(EditarPerfil.this, CameraActivity.class);
        startActivity(intent);
    }
}
