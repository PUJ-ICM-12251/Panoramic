package com.example.panoramic;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.panoramic.home.BuscarEstable;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class IniciarSesion extends AppCompatActivity {

    Button boton1;
    Button boton2;

    EditText emailC, pass;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iniciar_sesion);
        boton1 = findViewById(R.id.boton1);
        boton2 = findViewById(R.id.boton2);
        emailC = findViewById(R.id.username);
        pass = findViewById(R.id.contrasenaCampo);
        mAuth = FirebaseAuth.getInstance();
        mAuth.signOut();

        boton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emailC1=emailC.getText().toString().trim();
                String pass1=pass.getText().toString().trim();
                signInUser(emailC1,pass1);
            }
        });

        boton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(IniciarSesion.this, Registrarse.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void updateUI(FirebaseUser currentUser) {
        if (currentUser != null) {
            Intent intent = new Intent(getBaseContext(),
                    BuscarEstable.class);
            intent.putExtra("user", currentUser.getEmail());
            startActivity(intent);
        } else {
            emailC.setText("");
            pass.setText("");
        }
    }

    private boolean validateForm() {
        boolean valid = true;
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
        return valid;
    }

    private void signInUser(String email, String password) {
        if (validateForm()) {
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI
                        Log.d(TAG, "signInWithEmail: Success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);
                    } else {
                        // If Sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithEmail: Failure", task.getException());
                        Toast.makeText(IniciarSesion.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                        updateUI(null);
                    }
                }
            });
        }
    }
}