package com.example.panoramic;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Registrarse extends AppCompatActivity {

    Button boton1;

    private FirebaseAuth mAuth;

    EditText name, emailC, pass;

    CheckBox conditions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrarse);
        boton1 = findViewById(R.id.boton1);
        emailC = findViewById(R.id.username);
        pass = findViewById(R.id.contrasenaCampo);
        name = findViewById(R.id.editName);
        conditions = findViewById(R.id.checkBox);
        mAuth = FirebaseAuth.getInstance();
        mAuth.signOut();

        boton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emailC1=emailC.getText().toString().trim();
                String pass1=pass.getText().toString().trim();
                registrar(emailC1,pass1);
            }
        });
    }


    private boolean validateForm() {
        boolean valid = true;
        String nameC = name.getText().toString();
        if (TextUtils.isEmpty(nameC)) {
            name.setError("Required");
            valid = false;
        } else {
            name.setError(null);
        }
        String email = emailC.getText().toString();
        if (TextUtils.isEmpty(email)) {
            emailC.setError("Required");
            valid = false;
        } else {
            emailC.setError(null);
        }
        String password = pass.getText().toString();
        if (TextUtils.isEmpty(password)) {
            pass.setError("Required");
            valid = false;
        } else {
            pass.setError(null);
        }
        if(conditions.isChecked()== false){
            conditions.setError("Must accept the terms and conditions");
            valid = false;
        }else{
            conditions.setError(null);
        }
        return valid;
    }

    public void registrar(String mail, String password){
        if (validateForm()) {
            mAuth.createUserWithEmailAndPassword(mail, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Intent intent = new Intent(Registrarse.this, BuscarEstable.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(Registrarse.this, "Fallo en registrarse", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

}