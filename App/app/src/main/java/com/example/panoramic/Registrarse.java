package com.example.panoramic;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.panoramic.model.DatabasePaths;
import com.example.panoramic.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Registrarse extends AppCompatActivity {
    public static final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[^@]+@[^@]+\\.[a-zA-Z]{2,}$", Pattern.CASE_INSENSITIVE);
    private static final String TAG = Registrarse.class.getName();
    private Logger logger = Logger.getLogger(TAG);
    Button boton1;
    private FirebaseAuth mAuth;
    EditText name, emailC, pass;
    ImageView perfil;
    CheckBox conditions;
    DatabaseReference myRef;
    StorageReference mStorage;
    String storage_path;
    Uri file;
    private final int CAMERA_PERMISSION_ID = 101;
    private final int GALLERY_PERMISSION_ID = 102;
    String cameraPerm = Manifest.permission.CAMERA;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrarse);
        boton1 = findViewById(R.id.boton1);
        perfil = findViewById(R.id.imageView7);
        emailC = findViewById(R.id.username);
        pass = findViewById(R.id.contrasenaCampo);
        name = findViewById(R.id.editName);
        conditions = findViewById(R.id.checkBox);

        mAuth = FirebaseAuth.getInstance();
        mAuth.signOut();
        myRef = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance().getReference();

        logger.info("Se va a solicitar el permiso");
        requestPermission(Registrarse.this, cameraPerm, "Permiso para utiliza la camara", CAMERA_PERMISSION_ID);
        initView();

        boton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name1=name.getText().toString().trim();
                String emailC1=emailC.getText().toString().trim();
                String pass1=pass.getText().toString().trim();

                SharedPreferences preferences = getSharedPreferences("MiSharedPreferences", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("username", name1);
                editor.putString("email", emailC1);
                editor.apply();

                if(validateForm()) {
                    if (!isEmailValid(emailC1)) {
                        Toast.makeText(Registrarse.this, "Email is not a valid format",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    registrar(emailC1, pass1);
                }
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

    public void requestPermission(Activity context, String permission, String justification, int id) {
        // Se verifica si no hay permisos
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_DENIED) {
            // ¿Deberiamos mostrar una explicación?
            if (ActivityCompat.shouldShowRequestPermissionRationale(context, cameraPerm)) {
                Toast.makeText(context, justification, Toast.LENGTH_SHORT).show();
            }
            // Solicitar el permiso
            ActivityCompat.requestPermissions(context, new String[]{permission}, id);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_ID) {
            initView();
        }
    }

    private void initView() {
        if (ContextCompat.checkSelfPermission(this, cameraPerm) != PackageManager.PERMISSION_GRANTED) {
            logger.warning("Failed to getting the permission :(");
        } else {
            logger.info("Success getting the permission :)");
        }
    }

    private void updateUI(FirebaseUser currentUser){
        if(currentUser!=null){
            Intent intent = new Intent(getBaseContext(), BuscarEstable.class);
            storage_path = "fotos/" + currentUser.getUid();
            StorageReference imageRef = mStorage.child(storage_path);
            imageRef.putFile(file)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Get a URL to the uploaded content
                            Log.i("FBApp", "Succesfully upload image");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                        }
                    });
            //intent.putExtra("user", currentUser.getEmail());
            startActivity(intent);
        } else {
            name.setText("");
            emailC.setText("");
            pass.setText("");
        }
    }

    public void registrar(String mail, String password){
        if (validateForm()) {
            mAuth.createUserWithEmailAndPassword(mail, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();

                        if(user!=null){
                            User p = new User();
                            p.setName(name.getText().toString());
                            p.setMail(emailC.getText().toString());
                            p.setAvailable(true);
                            myRef=FirebaseDatabase.getInstance().getReference(DatabasePaths.USER + user.getUid());
                            myRef.setValue(p);
                            updateUI(user);

                        }
                    } else {
                        Toast.makeText(Registrarse.this, "Fallo en registrarse", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    public void startGallery(View view){
        Intent pickGalleryImage = new Intent(Intent.ACTION_PICK);
        pickGalleryImage.setType("image/*");
        startActivityForResult(pickGalleryImage, GALLERY_PERMISSION_ID);
    }

    @Override
    public void onActivityResult(int requestCode, int rresultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, rresultCode, data);
        if (rresultCode == Activity.RESULT_OK) {
            switch (requestCode){
                case GALLERY_PERMISSION_ID:
                    Uri imageUri = data.getData();
                    perfil.setImageURI(imageUri);
                    file = data.getData();
                    logger.info("Image loaded successfully");
                    break;
            }
        }
    }

    public static boolean isEmailValid(String emailStr) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr);
        return matcher.find();
    }

}