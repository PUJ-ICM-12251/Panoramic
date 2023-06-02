package com.example.panoramic;

import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.panoramic.Parse.DirectionsParser;

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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
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

public class MapsUsuario extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MapsUsuario";

    public static final int REQUEST_CHECK_SETTINGS = 201;

    private GoogleMap mMap;

    private com.example.panoramic.model.User user;

    private FirebaseUser currentUser;

    private Marker marker;

    private Location mLocation, fLocation;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;

    private DatabaseReference frRef;
    private DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_usuario);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        user = (com.example.panoramic.model.User) getIntent().getSerializableExtra("user");

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        frRef = FirebaseDatabase.getInstance().getReference(DatabasePaths.USER).child(user.getId());
        myRef = FirebaseDatabase.getInstance().getReference(DatabasePaths.USER).child(currentUser.getUid());
        mLocationRequest = createLocationRequest();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult (@NonNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    mLocation = location;
                    myRef.child("latitude").setValue(mLocation.getLatitude());
                    myRef.child("longitude").setValue(mLocation.getLongitude());
                }
            }
        };
        turnOnLocationAndStartUpdates();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
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
        //logger.info(url);
        return url;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Gestures
        mMap.getUiSettings().setAllGesturesEnabled(true);

        // Zoom Buttons
        mMap.getUiSettings().setZoomControlsEnabled(true);


        //mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(MapsUsuario.this, R.raw.style_user_map));


        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Aquí puedes manejar los cambios en los datos de Firebase
                // dataSnapshot contiene los datos actuales en el nodo

                // Iterar sobre los hijos del nodo
                user = dataSnapshot.getValue(com.example.panoramic.model.User.class);
                user.setId(dataSnapshot.getKey());
                LatLng friendLocation = new LatLng(user.getLatitude(), user.getLongitude());
                if(marker != null){
                    marker.remove();
                }
                marker = mMap.addMarker(new MarkerOptions().position(friendLocation).title(user.getName()+" "+user.getMail()));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(friendLocation));
                mMap.moveCamera(CameraUpdateFactory.zoomTo(10));
                fLocation = new Location("");
                fLocation.setLatitude(user.getLatitude());
                fLocation.setLongitude(user.getLongitude());
                float distance = fLocation.distanceTo(mLocation) / 1000;
                Toast.makeText(MapsUsuario.this, "Distancia: " + distance + " km", Toast.LENGTH_SHORT).show();
                //Imprimir ruta
                String url = getRequestUrl(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()), new LatLng(fLocation.getLatitude(), fLocation.getLongitude()));
                TaskRequestDirections taskRequestDirections = new TaskRequestDirections();
                taskRequestDirections.execute(url);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Aquí puedes manejar los errores, si los hay
            }
        };



        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());
//                mMap.addMarker(new MarkerOptions().position(myLocation).title("Marker in My Location"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
                mMap.moveCamera(CameraUpdateFactory.zoomTo(10));
                mLocation = location;
                Log.i(TAG, "onMapReady: "+ mLocation);
                frRef.addValueEventListener(valueEventListener);

            }
        });



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

    private void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.requestLocationUpdates((LocationRequest) mLocationRequest, (LocationCallback) mLocationCallback, null);
        }
    }

    private void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
    }

    private LocationRequest createLocationRequest() {
        return new LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY)

                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(5000)
                .build();
    }

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


}