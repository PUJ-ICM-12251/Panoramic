package com.example.panoramic;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import com.example.panoramic.adapters.BadgesAdapter;
import com.example.panoramic.model.Badge;
import com.example.panoramic.paths.DatabasePaths;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.ArrayList;
import java.util.List;


public class Badges extends AppCompatActivity {


    private static final String TAG = ListarUsuarios.class.getName();
    private FirebaseAuth mAuth;

    // Auth badge
    FirebaseUser currentUser;

    // Variables for Firebase DB
    FirebaseDatabase database;
    DatabaseReference myRef;
    ValueEventListener valueEventListener;

    //Local Data
    BadgesAdapter adapter;
    ArrayList<Badge> badgeListLocal = new ArrayList<>();
    GridView gridView;

    Button scanQR;

    String scanResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_badges);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        // Initialize Firebase database
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();

        // Initialize Adapter
        adapter = new BadgesAdapter(Badges.this, badgeListLocal);
        gridView = findViewById(R.id.badgeGrid);
        gridView.setAdapter(adapter);

        Log.i(TAG, "[Badges] onCreate: " + badgeListLocal.size());
        gridView.setOnItemClickListener((parent, view, position, id) -> {
            Badge badge = badgeListLocal.get(position);
            Log.i(TAG, "Badge selected: " + badge.toString());
            Intent intent = new Intent(Badges.this, BadgeActivity.class);
            intent.putExtra("badge", badge);
            startActivity(intent);
        });

        // QR SCAN
        scanQR = findViewById(R.id.scanQRButton);
        scanQR.setOnClickListener(v -> {
            ScanOptions options = new ScanOptions();
            options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
            options.setPrompt("Scan");
            options.setCameraId(0);
            options.setOrientationLocked(false);
            options.setBeepEnabled(false);
            options.setCaptureActivity(CaptureActivityPortrait.class);
            options.setBarcodeImageEnabled(false);
            barcodeLauncher.launch(options);


        });

    }





    @Override
    protected void onStart() {
        super.onStart();
        currentUser = mAuth.getCurrentUser();
        if(currentUser == null) {
            logout();
        }
        loadQueryBadge();
    }

    public void loadQueryBadge() {
        myRef = database.getReference(DatabasePaths.BADGE);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                badgeListLocal.clear();
                for (DataSnapshot snapshot: datasnapshot.getChildren()) {
                    Badge badg = snapshot.getValue(Badge.class);
                    badg.id = snapshot.getKey();
                    Log.i(TAG, "Badge: " + badg);
                    badgeListLocal.add(badg);
                }
                adapter.notifyDataSetChanged();
                Log.i(TAG, "Data changed from realtime DB");
                gridView.post(() -> gridView.setSelection(badgeListLocal.size()-1));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Log.w(TAG, "Error en la consulta", databaseError.toException());
            }

        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (valueEventListener != null) {
            myRef.removeEventListener(valueEventListener);
        }
    }


    private void logout() {
        mAuth.signOut();
        Intent intent = new Intent(Badges.this, IniciarSesion.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }



    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult (new ScanContract(), result -> {
        if(result.getContents() == null) {
            Toast.makeText(Badges.this, "Cancelled", Toast.LENGTH_LONG).show();
            Log.i(TAG, "Cancelled scan");
        } else {
            scanResult = result.getContents();
            Log.i(TAG, "Scanned: " + scanResult);
            // Create an intent with the URL you want to open
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(scanResult));

            // Verify that there is an app available to handle the intent
            PackageManager packageManager = getPackageManager();
            List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
            boolean isIntentSafe = activities.size() > 0;

           // Start the activity if an app is available to handle the intent
            if (isIntentSafe) {
                startActivity(intent);
            }else{
                Toast.makeText(Badges.this, "No app available to handle the intent", Toast.LENGTH_LONG).show();
            }
        }
    });

}