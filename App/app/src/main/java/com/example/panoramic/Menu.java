package com.example.panoramic;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.panoramic.databinding.ActivityMenuBinding;
import com.example.panoramic.model.User;
import com.example.panoramic.paths.DatabasePaths;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Menu extends AppCompatActivity {

    // Variables de Firebase.
    private FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference myRef;
    FirebaseUser currentUser;
    StorageReference mStorage;

    // Datos del usuario
    private String nombre;
    private String foto;

    // Componentes de UI
    private Button conectarse;

    User user;

    // Texto del boton
    private String[] textoBoton = {"Conectarse","Desconectarse"};

    // Al inciar la pantalla se realiza estas validaciones con el usuario para mostrar las opciones correctas
    @Override
    protected void onStart() {
        super.onStart();
        // Se obtiene la instancia de Firebase database
        database = FirebaseDatabase.getInstance();

        // Se instancia una sesión de firebase del usuario
        mAuth = FirebaseAuth.getInstance();

        // Se instancia una sesión de firebase storage
        mStorage = FirebaseStorage.getInstance().getReference();

        myRef = database.getReference();


        currentUser = mAuth.getCurrentUser();
        if(currentUser == null) {
            // Si el usuario no esta registrado, se saca de la pantalla.
            logout();
        }
        // Se obtienen los datos del usuario con una subscripción a la base de datos
        loadPerson();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMenuBinding binding = ActivityMenuBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        // Se inicializan los componentes UI en la pantalla
        conectarse = binding.conectarse;

        // Se suscribe al usuario al servicio de notificaciones para que reciban notificaciones de cualquier usuario.
        FirebaseMessaging.getInstance().subscribeToTopic("enviaratodos").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(Menu.this,"Registrado",Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void watchProfile(View view) {
        Intent intent = new Intent(Menu.this, EditarPerfil.class);
        startActivity(intent);
    }


    public void watchUsers(View view) {
        Intent intent = new Intent(Menu.this, ListarUsuarios.class);
        startActivity(intent);
    }


    public void connect(View view){
        // Cuando el usuario quiere mostrarse como disponible a los demas usuarios, se envia la notificación a tópico.

        // Se mira si su estado ha cambiado en la base de datos.
        loadPerson();

        // Se verifica el mensaje que aparece en el boton

        // Si el mensaje es conectarse en el texto del boton
        if(conectarse.getText().equals(textoBoton[0])){
            // Se cambia el estado del usuario a disponible
            updateEstado(mAuth.getCurrentUser().getUid(),true);
            // Se pone el texto del boton a desconectarse
            conectarse.setText(textoBoton[1]);
            // Se descarga la imagen del usuario para mandar la notificacion
            downloadFile();

        }
        // Si el mensaje es desconectarse en el texto del boton
        else if(conectarse.getText().equals(textoBoton[1])){
            // Se cambia el estado del usuario a no disponible
            updateEstado(mAuth.getCurrentUser().getUid(),false);
            // Se pone el texto del boton a conectarse
            conectarse.setText(textoBoton[0]);
        }
    }

    public void updateEstado(String llave, boolean estado){
        HashMap map = new HashMap();
        map.put("available",estado);

        Log.i("INFO","Se va a cambiar el estado del usuario");
        // Se busca el usuario dentro de la base de datos para actualizar su valor de disponibilidad.
        myRef = FirebaseDatabase.getInstance().getReference(DatabasePaths.USER);
        myRef.child(llave).updateChildren(map).addOnSuccessListener(new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {
                Toast.makeText(Menu.this,"Te has conectado al sistema\n",Toast.LENGTH_LONG);
            }
        });
    }

    // Funciones utilizadas para la creación de notificaciones
    public void logout(){
        // Se saca de la sesión el usuario
        mAuth.signOut();
        // Se devuelve al usuario a la pantalla de inicio de sesion
        Intent intent = new Intent(Menu.this,Home.class);
        startActivity(intent);
    }

    // Cargar los datos del usuario
    public void loadPerson(){
        myRef = database.getReference(DatabasePaths.USER+currentUser.getUid());
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Esto es para la busqueda de un solo documento de la base de datos
                // El snapshot se ubica en el id del documento del usuario
                user = snapshot.getValue(User.class);
                nombre = user.getName();
                Log.i("Info", "El usuario es: " + user.getName()+" "+user.getMail());
                if(user.isAvailable()){
                    // Mensaje del boton es desconectarse
                    conectarse.setText(textoBoton[1]);
                }
                else{
                    // Mensaje del boton es conectarse
                    conectarse.setText(textoBoton[0]);

                }
                // Si el usuario no estaba conectado el texto mostrado en el boton: "Conectarse"
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("TAG","Error en la consulta",error.toException());
            }
        });
    }

    // Se descarga la foto de perfil del usuario para mostrarla en la notificación
    public void downloadFile(){
        StorageReference imageRefl = mStorage.child(DatabasePaths.IMAGES+currentUser.getUid());
        imageRefl.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Se obtiene la dirección de la imagen del usuario y se llama el topico para enviar notificaciones a los usuarios.
                Uri downloadUri = uri;
                Log.i("INFO","La URI de la imagen es" +uri.toString());
                foto = downloadUri.toString();
                // Se llama a la función de enviar notificaciones
                llamartopico();
            }
        });
    }

    // Hacer el envio de notificacion de que un usuario esta disponible
    private void llamartopico() {

        RequestQueue myrequest= Volley.newRequestQueue(getApplicationContext());
        JSONObject json = new JSONObject();

        try {
            String url_foto=foto+".jpg";
            Log.i("INFO","El URL de la imagen es"+foto);
            // El token del servicio de comunicación entre dispositivos con la aplicación para notificaciones
            String token="AAAAARWlgcY:APA91bG5sb8eBdk7CX4HytAilKk9_b3vpIohT2qv3ZDokNgbKRusT3-MyV5ngOmUF_ehA2f0a7_-xSO4Nr8-Ve2aRE_vvGDBy6ajjeKY0F0tdCJl01A5wP1FOO6GSBauX8ol62MORnOZ";
            Log.i("INFO","El token del servicio es " + token);
            json.put("to","/topics/"+"enviaratodos");
            JSONObject notificacion=new JSONObject();
            notificacion.put("titulo","Usuario de la aplicacion conectado");
            notificacion.put("detalle","Se ha conectado el usuario: "+ nombre);
            notificacion.put("foto",url_foto);
            notificacion.put("UID",mAuth.getCurrentUser().getUid());

            json.put("data",notificacion);
            String URL="https://fcm.googleapis.com/fcm/send";

            // Se envia la petición al sistema de firebase para el CloudMessaging y enviar notificaciones
            JsonObjectRequest request=new JsonObjectRequest(Request.Method.POST,URL,json,null,null){
                @Override
                public Map<String, String> getHeaders() {
                    Map<String,String>header=new HashMap<>();
                    header.put("content-type","application/json");
                    header.put("authorization","key="+token);
                    return  header;
                }
            };
            myrequest.add(request);
        }catch (JSONException e){
            e.printStackTrace();
        }
    }
}