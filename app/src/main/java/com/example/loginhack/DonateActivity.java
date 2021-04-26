

package com.example.loginhack;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class DonateActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    private String email, phone;

    LocationManager locationManager;
    LocationListener locationListener;
    MaterialCardView cardView;
    ProgressBar progressBar;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public void onMapLongClick(LatLng latLng) {
        if(ContextCompat.checkSelfPermission(DonateActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            updateMapLocation(latLng);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donate2);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        progressBar = findViewById(R.id.progress2);
        progressBar.setVisibility(View.VISIBLE);

        email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        DocumentReference documentReference = db.collection("Email").document(email);
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                phone = String.valueOf(documentSnapshot.getString("phone"));
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng latLng = new LatLng(21.492077, 77.899625);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,5));

        progressBar.setVisibility(View.GONE);
        mMap.setOnMapLongClickListener(this);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION},1);

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

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                cardView.setVisibility(View.GONE);
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                FloatingActionButton f1 = findViewById(R.id.callDonate);
                FloatingActionButton f2 = findViewById(R.id.msgDonate);
                cardView = findViewById(R.id.donateCard);
                cardView.setVisibility(View.VISIBLE);

                TextView address = findViewById(R.id.address2);

                String[] title = marker.getTitle().split(",");
                String em = title[0];
                String number = title[1];

                final String[] adrs = new String[1];

                DocumentReference documentReference = db.collection("Help").document(em);
                documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        adrs[0] = String.valueOf(documentSnapshot.getString("address"));
                        address.setText(adrs[0]);
                    }
                });

                f1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_DIAL);
                        intent.setData(Uri.parse("tel:"+number));
                        startActivity(intent);
                    }
                });

                f2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_SENDTO);
                        intent.setType("vnd.android-dir/mms-sms");
                        intent.setData(Uri.parse("sms:"+number));
                        startActivity(intent);
                    }
                });
                return false;
            }
        });
    }

    public void updateMapLocation(LatLng latLng) {
        mMap.addMarker(new MarkerOptions().position(latLng).title("Users Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,10));

        String address;

        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        try {
            List<Address> listAddress = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);

            if (listAddress != null && listAddress.size() > 0) {
                address = "";

                if (listAddress.get(0).getAdminArea() != null)
                    address += listAddress.get(0).getAdminArea() + " ";

                if (listAddress.get(0).getLocality() != null)
                    address += listAddress.get(0).getLocality() + " ";

                if (listAddress.get(0).getThoroughfare() != null)
                    address += listAddress.get(0).getThoroughfare() + " ";

                if (listAddress.get(0).getPostalCode() != null)
                    address += listAddress.get(0).getPostalCode() + " ";

                if (listAddress.get(0).getSubLocality() != null)
                    address += listAddress.get(0).getSubLocality() + " ";

                if (listAddress.get(0).getSubThoroughfare() != null)
                    address += listAddress.get(0).getSubThoroughfare() + " ";

                if (listAddress.get(0).getSubAdminArea() != null)
                    address += listAddress.get(0).getSubAdminArea() + " ";

                Map<String, String> donate = new HashMap<>();
                donate.put("latitude", String.valueOf(latLng.latitude));
                donate.put("longitude", String.valueOf(latLng.longitude));
                donate.put("email",email);
                donate.put("phone",phone);
                donate.put("address",address);

                db.collection("Donate").add(donate);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}