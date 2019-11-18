package h2h.iot.panic;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
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

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class PopDate extends AppCompatActivity {

    private MapView mapView;
    private MapboxMap mapbox;
    private String clientId = MqttClient.generateClientId();
    private MqttAndroidClient client;
    private String topic ;
    private Float lat,lng;
    private int qos = 1;
    private String dateId,user;
    private TextView Idlog,flow,tamb,tfilter,presure,dirWin,speedWin,rain;
    private Button starting;
    private FirebaseDatabase database;
    private DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);

        getDate();
        Mapbox.getInstance(this, "pk.eyJ1Ijoic2Via2lsbGVyMDQyMSIsImEiOiJjanppdmd3cjEwM2pzM2NwcDl5eDhybjkzIn0.gnjw9ThqB1MPnxSYeMXojg");
        setContentView(R.layout.activity_pop_date);
        Idlog = (TextView) findViewById(R.id.idDate);
        topic = "machine/dates/"+dateId;
        Toast.makeText(this,topic,Toast.LENGTH_LONG).show();
        flow = findViewById(R.id.flow);
        tamb = findViewById(R.id.tamb);
        tfilter = findViewById(R.id.tfil);
        presure = findViewById(R.id.pressure);
        dirWin = findViewById(R.id.dirWin);
        speedWin = findViewById(R.id.speedWin);
        rain = findViewById(R.id.rain);
        Idlog.setText(dateId);

        FloatingActionButton graficar = findViewById(R.id.graph);
        graficar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openGra = new Intent(PopDate.this,GraphActivity.class);
                openGra.putExtra("topic",topic);
                startActivity(openGra);
            }
        });
        client = new MqttAndroidClient(this.getApplicationContext(),"tcp://m16.cloudmqtt.com:17872",clientId);
        try {
            MqttConnectOptions options = new MqttConnectOptions();
            options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);
            options.setCleanSession(true);
            options.setUserName("bjqlvjrp");
            options.setPassword("FXAW5K6WJH8G".toCharArray());
            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Toast.makeText(PopDate.this,"onSuccess",Toast.LENGTH_LONG).show();
                    try{
                        client.subscribe(topic,qos);
                    }catch (MqttException ex){
                        ex.printStackTrace();
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(PopDate.this,"onFailure",Toast.LENGTH_LONG).show();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }



        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {

            }
            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {

                if(message.toString().equals("mf")){
                    addNotification();
                }else {

                    flow.setText(message.toString().substring(message.toString().indexOf("\"flow\":") + 7, message.toString().indexOf("\"tamb\":")) + " L/cm");
                    tamb.setText(message.toString().substring(message.toString().indexOf("\"tamb\":") + 7, message.toString().indexOf("\"Tfilter\":")) + " C");
                    tfilter.setText(message.toString().substring(message.toString().indexOf("\"Tfilter\":") + 10, message.toString().indexOf("\"pressbar\":")) + " C");
                    presure.setText(message.toString().substring(message.toString().indexOf("\"pressbar\":") + 11, message.toString().indexOf("\"dirviento\":")) + " mmH");
                    dirWin.setText(message.toString().substring(message.toString().indexOf("\"dirviento\":") + 12, message.toString().indexOf("\"velviento\":")));
                    speedWin.setText(message.toString().substring(message.toString().indexOf("\"velviento\":") + 12, message.toString().indexOf("\"intenlluvia\":")) + " KM/h");
                    rain.setText(message.toString().substring(message.toString().indexOf("\"intenlluvia\":") + 14));
                }


            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
        starting = findViewById(R.id.startupM);



        mapView = findViewById(R.id.mapView);
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

        starting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(PopDate.this);
                builder.setTitle("Elegir tipo de monitero");
                builder.setIcon(R.drawable.ic_ok);
                builder.setPositiveButton("Manual", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String mess = "manual";
                        byte[] encodePayload = new byte[0];
                        try {
                            encodePayload = mess.getBytes(StandardCharsets.UTF_8);
                            MqttMessage message = new MqttMessage(encodePayload);
                            client.publish("machine/set/"+dateId,message);
                        }catch (MqttException e){
                            e.printStackTrace();
                        }

                    }
                });
                builder.setNegativeButton("Automatico", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //open formulary

                    }
                });
                builder.show();
            }
        });

        readDB();

    }

    private void getDate(){
        Bundle extras = getIntent().getExtras();
        dateId = extras.getString("id");
        user = extras.getString("user");
    }



    private void addNotification(){

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(PopDate.this,"0")
                        .setSmallIcon(R.drawable.mapbox_logo_icon)
                        .setContentTitle("Notifications Example")
                        .setContentText("This is a test notification");


        // Add as notification
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());
        Toast.makeText(PopDate.this, "open notification", Toast.LENGTH_LONG).show();
    }


    private void readDB(){
        getDate();
        String userCorrect = user.substring(0,user.indexOf("@"));
        database = FirebaseDatabase.getInstance();
        myRef = FirebaseDatabase.getInstance().getReference();
        myRef.child(userCorrect).child(dateId).child("location").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                lat =  (Float.parseFloat(dataSnapshot.getValue(String.class).substring(7, dataSnapshot.getValue(String.class).indexOf(",\"lng\":"))));
                lng = (Float.parseFloat(dataSnapshot.getValue(String.class).substring(dataSnapshot.getValue(String.class).indexOf(",\"lng\":") + 7,dataSnapshot.getValue(String.class).indexOf("}"))));

                mapView.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(@NonNull MapboxMap mapboxMap) {
                        MarkerOptions marker = new MarkerOptions();
                        marker.title(dateId+"\n Tamb:"+tamb.getText().toString());
                        marker.position(new LatLng(lat,lng));
                        mapboxMap.addMarker(marker);
                    }
                });
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

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
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}
