package com.example.panoramic;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.panoramic.Parse.DirectionsParser;
import com.example.panoramic.databinding.ActivityMapsUsuarioBinding;
import com.example.panoramic.model.User;
import com.example.panoramic.paths.DatabasePaths;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.Priority;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;


public class MapsUsuario extends FragmentActivity implements OnMapReadyCallback {

    // Seguimiento del funcionamiento del programa
    private static final String TAG = "MapsUsuario";
    private Logger logger = Logger.getLogger(TAG);

    public static final int REQUEST_CHECK_SETTINGS = 201;

    // Permiso de localizacion fina en cadena de texto
    String fineLocationPerm = Manifest.permission.ACCESS_FINE_LOCATION;

    //Identificador de los permisos
    private final int LOCATION_PERMISSION_ID = 124;

    // El usuario actual del programa
    private Location mLocation;

    // Marcador del usuario
    private Marker mMarker;

    // Marcador del otro usuario
    private Marker fMarker;

    // Variables del usuario que se esta siguiendo
    private User user;
    private Location fLocation;

    // Variables para obtener la localización actual del usuario
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;

    // Variables de

    // Lista de latitudes y longitudes
    ArrayList<LatLng>listPoints;

    // Variables de Google Map
    private GoogleMap mMap;
    private ActivityMapsUsuarioBinding binding;

    // Variable booleana de acercamiento
    private boolean acercamiento = true;

    private Polyline ruta;

    // Variables de Firebase
    FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference myRef;
    FirebaseUser currentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsUsuarioBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Se obtiene la instancia del autenticador.
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Si el usuario no estaba en una sesión activa se manda a home
        if(currentUser == null){
            Intent intent = new Intent(MapsUsuario.this,Home.class);
            // Se sale de la cuenta del usuario
            mAuth.signOut();
            startActivity(intent);
        }

        // Se obtiene los datos del usuario que fue elegido de la pantalla de lista
        user = (User) getIntent().getSerializableExtra("user");



        // Se obtiene la instancia de Firebase database
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();

        Log.i(TAG,"Este el UID del usuario:  "+user.getId());
        loadUsers();



        // Se inicializa el arreglo de lista de posiciones
        listPoints = new ArrayList<LatLng>();

        // Se crea la solicitud de subscripción a servicios
        mLocationRequest = createLocationRequest();

        // Se activa el servicio de localizacion
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Se hace solicitud de permiso de usar la localizacion
        requestPermission(this, fineLocationPerm, "Se requiere permiso de localizacion", LOCATION_PERMISSION_ID);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                logger.info("Location update in the callback: " + location);
                if (location != null) {
                    mLocation = location;
                    logger.info("En localizacion exitosa");
                    logger.info(String.valueOf(location.getLatitude()));
                    logger.info(String.valueOf(location.getLongitude()));
                    logger.info(String.valueOf(location.getAltitude()));
                    actualizarLocalizacion(currentUser.getUid());

                    // Obtain the SupportMapFragment and get notified when the map is ready to be used.
                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.map);
                    mapFragment.getMapAsync(MapsUsuario.this);


                }
            }
        };
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
// Gestures
        mMap.getUiSettings().setAllGesturesEnabled(true);

        // Zoom Buttons
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // Botones de inclinación
        mMap.getUiSettings().setTiltGesturesEnabled(true);

        // Se limpia la lista de puntos.
        listPoints.clear();

        // Se ubica la posicion del usuario actual con un marcador
        LatLng mUser = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
        listPoints.add(mUser);
        if (mMarker == null) {
            mMarker = mMap.addMarker(new MarkerOptions().position(mUser)
                    .title("Tu ubicación")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        } else {
            mMarker.setPosition(mUser);
        }
        if (acercamiento) {
            logger.info("Se hace acercamiento a la ubicación del usuario");
            mMap.moveCamera(CameraUpdateFactory.newLatLng(mUser));
            mMap.moveCamera(CameraUpdateFactory.zoomTo(10));
            acercamiento = false;
        }
        // Se ubica la posición del otro usuario con un marcador
        LatLng fUser = new LatLng(fLocation.getLatitude(),fLocation.getLongitude());
        listPoints.add(fUser);
        if(fMarker == null){
            fMarker = mMap.addMarker(new MarkerOptions().position(fUser)
                    .title(user.getName())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        }
        else{
            fMarker.setPosition(fUser);
        }

        // Se hace el calculo de la ruta entre los dos usuarios
        // Cada vez que su posición ha sido actualizada.
        if(listPoints.size() == 2){
            //Crear URL para obtener solicitud del primer al segundo marcador
            String url = getRequestUrl(listPoints.get(0), listPoints.get(1));
            MapsUsuario.TaskRequestDirections taskRequestDirections = new MapsUsuario.TaskRequestDirections();
            taskRequestDirections.execute(url);
        }

    }



    // Actualizar la ubicación del usuario actual en Firebase
    public void actualizarLocalizacion(String llave){
        logger.info("Se actualiza la localizacion del usuario actual");
        myRef = FirebaseDatabase.getInstance().getReference(DatabasePaths.USER);
        myRef.child(llave)
                .child("latitude")
                .setValue(mLocation.getLatitude());
        myRef.child(llave)
                .child("longitude")
                .setValue(mLocation.getLongitude());
    }


    private void logout() {
        mAuth.signOut();
        Intent intent = new Intent(MapsUsuario.this, Home.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    // Se suscribe a la base de datos para obtener la localización en tiempo real del otro usuario
    public void loadUsers() {
        myRef = database.getReference(DatabasePaths.USER);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i(TAG,"Entro aqui a la funcion parte 1");
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User myUser = snapshot.getValue(User.class);
                    Log.i(TAG,"Entra al usuario con ID"+snapshot.getKey() + " y UID del otro usuario "+user.getId());
                    if(snapshot.getKey().equals(user.getId())){
                        Log.i(TAG,"Son iguales");
                        fLocation = new Location("Nuevo provedor");
                        Log.i(TAG,"Nombre del usuario: "+myUser.getName());
                        Log.i(TAG,"Localizacion del otro usuario "+ myUser.getLatitude() + " " + myUser.getLongitude());
                        fLocation.setLongitude(myUser.getLongitude());
                        fLocation.setLatitude(myUser.getLatitude());
                        break;
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "error en la consulta", databaseError.toException());
            }
        });
    }

    private String getRequestUrl(LatLng origin, LatLng destination) {
        // Valor del origen
        String str_org = "origin="+origin.latitude+","+origin.longitude;
        // Valor del destino
        String str_des = "destination="+destination.latitude+","+destination.longitude;
        String sensor = "sensor=false";
        String mode = "mode=driving";
        String key = "key=" + BuildConfig.MAPS_API_KEY;
        //Construir el parametro completo
        String param = str_org + "&" +str_des + "&" + sensor + "&" + mode + "&"+key;
        //Formato de salida
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/" +  output + "?" + param;
        logger.info(url);
        return url;
    }

    private String requestDirection(String reqUrl) {
        String responseString = "";
        InputStream inputStream = null;
        HttpURLConnection httpURLConnection = null;
        try{
            URL url = new URL(reqUrl);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.connect();

            //Get the response result
            inputStream = httpURLConnection.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            StringBuffer stringBuffer = new StringBuffer();
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line);
            }

            responseString = stringBuffer.toString();
            bufferedReader.close();
            inputStreamReader.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            httpURLConnection.disconnect();
        }
        return responseString;
    }
    public class TaskRequestDirections extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String responseString = "";
            responseString = requestDirection(strings[0]);
            return  responseString;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //Parse json here
            TaskParser taskParser = new TaskParser();
            taskParser.execute(s);
        }
    }

    public class TaskParser extends AsyncTask<String, Void, List<List<HashMap<String, String>>> > {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject jsonObject = null;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jsonObject = new JSONObject(strings[0]);
                DirectionsParser directionsParser = new DirectionsParser();
                routes = directionsParser.parse(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            //Get list route and display it into the map

            ArrayList points = null;

            PolylineOptions polylineOptions = null;

            for (List<HashMap<String, String>> path : lists) {
                points = new ArrayList();
                polylineOptions = new PolylineOptions();

                for (HashMap<String, String> point : path) {
                    double lat = Double.parseDouble(point.get("lat"));
                    double lon = Double.parseDouble(point.get("lon"));

                    points.add(new LatLng(lat,lon));
                }

                polylineOptions.addAll(points);
                polylineOptions.width(15);
                polylineOptions.color(Color.BLUE);
                polylineOptions.geodesic(true);
            }

            if (polylineOptions!=null) {
                if(ruta == null){
                    ruta = mMap.addPolyline(polylineOptions);
                }
                else{
                    ruta.remove();
                    ruta = mMap.addPolyline(polylineOptions);
                }
            } else {
                Toast.makeText(getApplicationContext(), "Direction not found!", Toast.LENGTH_SHORT).show();
            }

        }
    }



    // Al salir de la aplicacion sin cerrarla
    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    // Al entrar de nuevo a la aplicacion sin haberla cerrada
    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
    }


    // Se inician los servicios de actualizacion de cambios
    private void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, fineLocationPerm) == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
        }
    }
    //Apagar los servicios  de actualizacion
    private void stopLocationUpdates(){
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    // Solicitud de localizacion
    private LocationRequest createLocationRequest(){
        LocationRequest mLocationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY,10000)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(5000)
                .build();
        return mLocationRequest;
    }


    // Solicitud de permisos
    private void requestPermission(Activity context, String permiso, String justificacion, int idCode){
        // Se revisa si el permiso no se ha otorgado
        if(ActivityCompat.checkSelfPermission(context,permiso) != PackageManager.PERMISSION_GRANTED){
            // Se mira si se debe mostrar un mensaje de justificacion
            if(ActivityCompat.shouldShowRequestPermissionRationale(context,permiso)){
                Toast.makeText(context,justificacion,Toast.LENGTH_LONG);
            }
            // Se hace solicitud del permiso
            ActivityCompat.requestPermissions(context,new String[]{permiso},idCode);
        }
    }

    // Se obtiene el resultado del permiso y si las condiciones son optimas
    // se enciende la subscripcion de cambios de posicion del usuario

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permission, @NonNull int [] grantResults){
        super.onRequestPermissionsResult(requestCode,permission,grantResults);
        switch(requestCode){
            case LOCATION_PERMISSION_ID:{
                if(grantResults.length>0&&grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    // El permiso ha sido autorizado y se sigue con el flujo de la aplicacion
                    Toast.makeText(this,"Autorizado",Toast.LENGTH_SHORT);

                }
                else{
                    // El permiso no fue autorizado
                    Toast.makeText(this,"No hay permiso para acceder a localizacion",Toast.LENGTH_LONG);
                }
            }
        }
        turnOnLocationAndStartUpdates();
    }




    //Se prenden los servicios y las actualizaciones
    private void turnOnLocationAndStartUpdates() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(this, locationSettingsResponse -> {
            startLocationUpdates(); // Todas las condiciones para recibiir localizaciones
        });
        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                int statusCode = ((ApiException) e).getStatusCode();
                switch (statusCode) {
                    case CommonStatusCodes.RESOLUTION_REQUIRED:
                        // Location setttings are not satisfied, but this can be fixed by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result in onActivityResult()
                            ResolvableApiException resolvable = (ResolvableApiException) e;
                            resolvable.startResolutionForResult(MapsUsuario.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException sendEx) {
                            // Ignore the error
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. No way to fix the settings so we won't show the dialog.
                        break;
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS: {
                if (resultCode == RESULT_OK) {
                    startLocationUpdates();
                } else {
                    Toast.makeText(this, "Sin acceso a localización. Hardware deshabilitado", Toast.LENGTH_LONG);
                }
            }
        }
    }


}