package com.example.panoramic;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.panoramic.Parse.DirectionsParser;
import com.example.panoramic.databinding.ActivityVerMapaBinding;
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
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;

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


public class VerMapa extends FragmentActivity implements OnMapReadyCallback {

    // Trazabilidad de la aplicacion con un logger
    private static final String TAG = VerMapa.class.getName();
    private Logger logger = Logger.getLogger(TAG);



    // Permiso de localizacion fina en cadena de texto
    String fineLocationPerm = Manifest.permission.ACCESS_FINE_LOCATION;

    //Identificador de los permisos
    private final int LOCATION_PERMISSION_ID = 103;
    private final int REQUEST_CHECK_SETTINGS = 201;

    //Variables de localizacion
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;

    private Location mLocation, nLocation;
    private GoogleMap mMap;
    private ActivityVerMapaBinding binding;


    // Variables de sensores
    SensorManager sensorManager;
    Sensor lightSensor;
    SensorEventListener lightSensorListener;

    //Variable de campo de texto
    EditText mAddress;

    TextView distancia;

    Geocoder mGeocoder;

    // Lista de los puntos para dibujar la ruta entre dos puntos
    ArrayList<LatLng> listPoints;

    // Checkbox para activar y desactivar ubicación del usuario
    CheckBox activarUbicacion;

    // Controles de busqueda y creacion de rutas

    boolean limpiarPantalla = false;

    boolean permitirBuscar = true;

    boolean centradoInicial = true;

    // Direccion del usuario o colocado como origen
    TextView originAddress;

    // Boton para centrar la vista
    Button centrarVista;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityVerMapaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize the sensors
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        //Obtención del texto ingresado por el usuario

        mAddress = binding.address;

        // Checkbox
        activarUbicacion = binding.btnAct;

        //Textview
        originAddress = binding.miDir;

        //Centrar vista
        centrarVista = binding.btnCen;


        centrarVista.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!activarUbicacion.isChecked()){
                    centradoInicial = true;
                }
                else{
                    Toast.makeText(VerMapa.this,"No se puede centrar vista", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mAddress.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    findAddress();
                }
                return false;
            }
        });



        // Se crea la solicitud de subscripción a servicios
        mLocationRequest = createLocationRequest();

        // Se activa el servicio de localizacion
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Se hace solicitud de permiso de usar la localizacion
        requestPermission(this,fineLocationPerm,"Se requiere permiso de localizacion",LOCATION_PERMISSION_ID);

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

                    // Obtain the SupportMapFragment and get notified when the map is ready to be used.
                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.map);
                    mapFragment.getMapAsync(VerMapa.this);


                }
            }
        };

        lightSensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (mMap != null) {
                    if (event.values[0] > 5000) {
                        Log.i("MAPS", "DARK MAP " + event.values[0]);
                        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(VerMapa.this, R.raw.style_day_retro));
                    } else {
                        Log.i("MAPS", "LIGHT MAP " + event.values[0]);
                        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(VerMapa.this, R.raw.style_night));
                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {}
        };

        //Inicializar Geocoder
        mGeocoder = new Geocoder(getBaseContext());
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
        Geocoder geocoder = new Geocoder(VerMapa.this);

        Location longclick = new Location("");
        mMap = googleMap;


        // Add a marker in Sydney and move the camera
        if(!activarUbicacion.isChecked()) {
            limpiarPantalla = false;
            permitirBuscar = true;
            LatLng user = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
            mMap.addMarker(new MarkerOptions().position(user).title("Tu ubicación actual"));
            List<Address> addresses;
            try {
                addresses = geocoder.getFromLocation(user.latitude,user.longitude,1);
                originAddress.setText(addresses.get(0).getAddressLine(0));
            } catch (IOException e) {
                logger.info("No se proceso la dirección\n");
            }

            if(centradoInicial) {
                centradoInicial = false;
                mMap.moveCamera(CameraUpdateFactory.newLatLng(user));
                mMap.moveCamera(CameraUpdateFactory.zoomTo(10));
            }
        }
        else{
            if(!limpiarPantalla){
                permitirBuscar = false;
                limpiarPantalla = true;
                mMap.clear();
            }
        }

        listPoints = new ArrayList<LatLng>();

        List<Marker> marcadores = new ArrayList<>();
        // Hacer long-click
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(@NonNull LatLng latLng) {

                // Ya hay más de dos puntos en el mapa
                if (listPoints.size() >= 2) {
                    // Se eliminan los marcadores del mapa
                    logger.info("Ya hay más de dos marcadores en el mapa");
                    mMap.clear();
                    // Se vacia la lista de puntos
                    listPoints.clear();
                }
                // Se agrega el primer punto en el mapa
                listPoints.add(latLng);
                // Se busca la dirección del punto con Geocoder


                List<Address> listaDirecciones = null;
                try {
                    listaDirecciones = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                } catch (IOException e) {
                    logger.info(String.valueOf(e));
                }

                Address direccion = listaDirecciones.get(0);

                // Se crea el marcador
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng).title(direccion.getAddressLine(0));

                // Se coloca el marcador
                if (listPoints.size() == 1) {
                    //Se agrega la direccion encontrada al text view
                    originAddress.setText(direccion.getAddressLine(0).toString());
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.moveCamera(CameraUpdateFactory.zoomTo(10));
                } else {
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.moveCamera(CameraUpdateFactory.zoomTo(10));
                }
                mMap.addMarker(markerOptions);


                //Cuando el tamaño de la lista es dos, se dibuja la ruta entre los dos puntos
                if(listPoints.size() == 2) {
                    //Crear URL para obtener solicitud del primer al segundo marcador
                    String url = getRequestUrl(listPoints.get(0), listPoints.get(1));
                    TaskRequestDirections taskRequestDirections = new TaskRequestDirections();
                    taskRequestDirections.execute(url);
                }
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
                mMap.addPolyline(polylineOptions);
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
        sensorManager.unregisterListener(lightSensorListener);
    }

    // Al entrar de nuevo a la aplicacion sin haberla cerrada
    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
        if(sensorManager != null) {
            sensorManager.registerListener(lightSensorListener,
                    lightSensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
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
                            resolvable.startResolutionForResult(VerMapa.this, REQUEST_CHECK_SETTINGS);
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

    public void search(View view) {
        mMap.clear();
        findAddress();
    }

    private void findAddress() {
        String addressString = mAddress.getText().toString();
        if(permitirBuscar) {
            if (!addressString.isEmpty()) {
                try {
                    List<Address> addresses = mGeocoder.getFromLocationName(addressString, 2);
                    if (addresses != null && !addresses.isEmpty()) {
                        Address addressResult = addresses.get(0);
                        nLocation = new Location("Nuevo provedor");
                        nLocation.setLatitude(addressResult.getLatitude());
                        nLocation.setLongitude(addressResult.getLongitude());
                        float distance = mLocation.distanceTo(nLocation);
                        LatLng position = new LatLng(addressResult.getLatitude(), addressResult.getLongitude());
                        Toast.makeText(VerMapa.this, "La distancia entre tu ubicación y el" +
                                " último marcador puesto es " + distance, Toast.LENGTH_LONG);
                        // Se obtiene la distancia entre la ubicacion del usuario
                        // y la localizacion de la direccion encontrada por GeoCoder
                        if (mMap != null) {
                            mMap.addMarker(new MarkerOptions().position(position)
                                    .title(addressResult.getFeatureName())
                                    .snippet(addressResult.getAddressLine(0))
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(position));

                            // Se dibuja la ruta entre la ubicación actual del usuario y la ubicación entegrada por Geocoder
                            String url = getRequestUrl(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()), new LatLng(nLocation.getLatitude(), nLocation.getLongitude()));
                            TaskRequestDirections taskRequestDirections = new TaskRequestDirections();
                            taskRequestDirections.execute(url);

                        }
                    } else {
                        Toast.makeText(VerMapa.this, "Dirección no encontrada", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(VerMapa.this, "La dirección está vacía", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Toast.makeText(VerMapa.this,"No se puede buscar", Toast.LENGTH_SHORT).show();
        }
    }

}