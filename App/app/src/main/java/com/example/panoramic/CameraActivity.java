package com.example.panoramic;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.Manifest;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.panoramic.model.User;
import com.example.panoramic.paths.DatabasePaths;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class CameraActivity extends AppCompatActivity {

    // Setup del logger para esta clase
    public static final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[^@]+@[^@]+\\.[a-zA-Z]{2,}$", Pattern.CASE_INSENSITIVE);
    private static final String TAG = CameraActivity.class.getName();
    private Logger logger = Logger.getLogger(TAG);

    // Definición de los id de los permisos
    private final int CAMERA_PERMISSION_ID = 101;
    private final int GALLERY_PERMISSION_ID = 102;
    String cameraPerm = Manifest.permission.CAMERA;

    // Componentes de GUI donde se mostrará la imagen de la cámara
    ImageView imageView;
    private FirebaseAuth mAuth;
    EditText nameC, emailC;
    String currentPhotoPath;
    String storage_path;
    DatabaseReference myRef;
    StorageReference mStorage;
    Uri file;
    Button actualizar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        nameC = findViewById(R.id.editTextText);
        emailC = findViewById(R.id.editTextTextEmailAddress);
        imageView = findViewById(R.id.imageView);
        actualizar = findViewById(R.id.button3);
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        mStorage = FirebaseStorage.getInstance().getReference();
        myRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("user").child(userID);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String username = dataSnapshot.child("name").getValue(String.class);
                    String email = dataSnapshot.child("mail").getValue(String.class);
                    nameC.setText(username);
                    emailC.setText(email);
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
                    .into(imageView);
        }).addOnFailureListener(e -> {
            // Manejar la falla al obtener la URL de descarga de la imagen
            Log.e("EditarPerfil", "Error al obtener la URL de descarga de la imagen", e);
        });

        // Pedir el permiso cuando la aplicación inicie
        logger.info("Se va a solicitar el permiso");
        requestPermission(this, cameraPerm, "Permiso para utiliza la camara", CAMERA_PERMISSION_ID);
        initView();

        actualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name1=nameC.getText().toString().trim();
                String emailC1=emailC.getText().toString().trim();

                if (!isEmailValid(emailC1)) {
                    Toast.makeText(CameraActivity.this, "Email is not a valid format",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                actualizar(emailC1);
            }
        });
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

    private void initView() {
        if (ContextCompat.checkSelfPermission(this, cameraPerm) != PackageManager.PERMISSION_GRANTED) {
            logger.warning("Failed to getting the permission :(");
        } else {
            logger.info("Success getting the permission :)");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_ID) {
            initView();
        }
    }

    public void startCamera(View view) {
        if (ContextCompat.checkSelfPermission(this, cameraPerm) == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            logger.warning("Failed to getting the permission :(");
        }
    }

    public void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Asegurarse de que hay una actividad de camara para manejar el intent
        if (takePictureIntent != null) {
            //Crear el archivo donde debería ir la foto
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                logger.warning(ex.getMessage());
            }
            //Continua solo el archivo ha sido exitosamente creado
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "Panoramic.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAMERA_PERMISSION_ID);
            }
        }
    }

    private File createImageFile() throws IOException {
        //Crear un nombre dde archivo de imagen
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile("IMG",".jpg", storageDir);

        // Guardar un archivo: Ruta para usar con ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        logger.info("Ruta: "+currentPhotoPath);
        return image;
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
                case CAMERA_PERMISSION_ID:
                    imageView.setImageURI(Uri.parse(currentPhotoPath));
                    logger.info("Image capture successfully.");
                    break;
                case GALLERY_PERMISSION_ID:
                    Uri imageUri = data.getData();
                    imageView.setImageURI(imageUri);
                    file = data.getData();
                    logger.info("Image loaded successfully");
                    break;
            }
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
            nameC.setText("");
            emailC.setText("");
        }
    }

    public void actualizar(String mail) {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null) {
                user.updateEmail(mail)
                        .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    // Email updated successfully
                                    updateProfile(user);
                                } else {
                                    // Failed to update email
                                    Toast.makeText(CameraActivity.this, "Failed to update email", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
    }

    private void updateProfile(FirebaseUser user) {
        User p = new User();
        p.setName(nameC.getText().toString());
        p.setMail(emailC.getText().toString());
        p.setAvailable(false);
        myRef = FirebaseDatabase.getInstance().getReference(DatabasePaths.USER + user.getUid());
        myRef.setValue(p)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Profile updated successfully
                            updateUI(user);
                        } else {
                            // Failed to update profile
                            Toast.makeText(CameraActivity.this, "Failed to update profile", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }


    public static boolean isEmailValid(String emailStr) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr);
        return matcher.find();
    }


}

