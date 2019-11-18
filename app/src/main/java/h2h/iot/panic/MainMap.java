package h2h.iot.panic;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

import java.util.ArrayList;

public class MainMap extends AppCompatActivity {

    private MapView mapView;
    private MapboxMap mapbox;
    private String username;
    private ArrayList<String> positions,id;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private Float lat,lng;
    private int i;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, "pk.eyJ1Ijoic2Via2lsbGVyMDQyMSIsImEiOiJjanppdmd3cjEwM2pzM2NwcDl5eDhybjkzIn0.gnjw9ThqB1MPnxSYeMXojg");
        setContentView(R.layout.activity_main_map);
        id = new ArrayList<String>();
        positions = new ArrayList<String>();
        mapView = findViewById(R.id.mapLoc);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mapboxMap){
                mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                    }
                });
            }
        });
        readDB();
    }

    private void readDB(){
        requestData();
        String userCorrect = username.substring(0,username.indexOf("@"));
        database = FirebaseDatabase.getInstance();
        myRef = FirebaseDatabase.getInstance().getReference();
        myRef.child(userCorrect).addValueEventListener(new ValueEventListener(){
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                id.clear();
                positions.clear();
                mapView.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(@NonNull MapboxMap mapboxMap) {
                        mapboxMap.clear();
                    }
                });
                for (DataSnapshot key : dataSnapshot.getChildren()){
                    id.add(key.getKey());
                    myRef.child(userCorrect).child(key.getKey()).child("location").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot1) {
                            lat = (Float.parseFloat(dataSnapshot1.getValue(String.class).substring(7, dataSnapshot1.getValue(String.class).indexOf(",\"lng\":"))));
                            lng = (Float.parseFloat(dataSnapshot1.getValue(String.class).substring(dataSnapshot1.getValue(String.class).indexOf(",\"lng\":") + 7,dataSnapshot1.getValue(String.class).indexOf("}"))));
                            Log.w("ERROR",lat.toString()+","+lng.toString());
                            mapView.getMapAsync(new OnMapReadyCallback() {
                                @Override
                                public void onMapReady(@NonNull MapboxMap mapboxMap) {
                                    MarkerOptions options = new MarkerOptions();
                                    options.title(key.getKey());
                                    options.position(new LatLng(lat, lng));
                                    mapboxMap.addMarker(options);
                                }
                            });
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void requestData(){
        Bundle dates = getIntent().getExtras();
        username = dates.getString("user");
    }




    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}
