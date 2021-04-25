package com.example.loginhack;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;

public class HomeActivity extends FragmentActivity implements OnMapReadyCallback{

    LocationManager locationManager;
    private GoogleMap mMap;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        email = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        Button needHelp = findViewById(R.id.needHelp);
        Button donate = findViewById(R.id.Donate);

        needHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, NeedHelpActivity.class);
                startActivity(intent);
            }
        });

        donate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, DonateActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION},1);

        db.collection("Donate").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    for(QueryDocumentSnapshot documentSnapshot : Objects.requireNonNull(task.getResult())) {
                        Double lat = Double.parseDouble(String.valueOf(documentSnapshot.getData().get("latitude")));
                        Double lon = Double.parseDouble(String.valueOf(documentSnapshot.getData().get("longitude")));
                        String e = (String) documentSnapshot.getData().get("email");
                        String p = (String) documentSnapshot.getData().get("phone");
                        LatLng user = new LatLng(lat,lon);
                        mMap.addMarker(new MarkerOptions().position(user).title(e+", "+p).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                    }
                }
            }
        });


        db.collection("Help").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    for(QueryDocumentSnapshot documentSnapshot : Objects.requireNonNull(task.getResult())) {
                        Double lat = Double.parseDouble(String.valueOf(documentSnapshot.getData().get("latitude")));
                        Double lon = Double.parseDouble(String.valueOf(documentSnapshot.getData().get("longitude")));
                        String e = (String) documentSnapshot.getData().get("email");
                        String p = (String) documentSnapshot.getData().get("phone");
                        LatLng user = new LatLng(lat,lon);
                        mMap.addMarker(new MarkerOptions().position(user).title(e+", "+p));
                    }
                }
            }
        });
    }
}
