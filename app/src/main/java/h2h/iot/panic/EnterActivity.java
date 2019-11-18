package h2h.iot.panic;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.ArrayList;

public class EnterActivity extends AppCompatActivity {

    private String username;
    private ArrayList<String> id;
    private ArrayAdapter<String> adapter;
    private ListView lv;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private String clientId = MqttClient.generateClientId();
    private MqttAndroidClient client;
    private String Access;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter);
        requestData();
        Toast.makeText(this,"welcome "+username,Toast.LENGTH_LONG).show();
        client = new MqttAndroidClient(EnterActivity.this,"tcp://m16.cloudmqtt.com:17872",clientId);
        try {
            MqttConnectOptions options = new MqttConnectOptions();
            options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);
            options.setCleanSession(false);
            options.setUserName("bjqlvjrp");
            options.setPassword("FXAW5K6WJH8G".toCharArray());
            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Toast.makeText(EnterActivity.this,"onSuccess",Toast.LENGTH_LONG).show();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(EnterActivity.this,"onFailure",Toast.LENGTH_LONG).show();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
        id = new ArrayList<String>();
        adapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,id);
        readDB();
        lv = findViewById(R.id.list);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final int posicion=position;
                String listDate = lv.getItemAtPosition(posicion).toString().trim();
                Access = listDate.substring(9);
                Intent intent1 = new Intent(EnterActivity.this, ConfirmActivity.class);
                intent1.putExtra("key",Access);
                intent1.putExtra("username",username);
                startActivity(intent1);

                //startScanQr();

            }
        });
        FloatingActionButton fab = findViewById(R.id.mapOpen);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent intent = new Intent(EnterActivity.this,MapLocation.class);
                //startActivity(intent);
            }
        });

        FloatingActionButton addQr = findViewById(R.id.addQrRead);
        addQr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userCorrect = username.substring(0,username.indexOf("@"));
                myRef.child("compania").child("JC").child("tecnicos").child(userCorrect).child("serviciosPendientes").child("Buger King cra93").child("cliente").setValue("burger king");
                myRef.child("compania").child("JC").child("tecnicos").child(userCorrect).child("serviciosPendientes").child("Buger King cra93").child("direccion").setValue("cr93#88-47");
                myRef.child("compania").child("JC").child("tecnicos").child(userCorrect).child("serviciosPendientes").child("Buger King cra93").child("correo").setValue("burgerKing@gmail.com");
                myRef.child("compania").child("JC").child("tecnicos").child(userCorrect).child("serviciosPendientes").child("Buger King cra93").child("telefono").setValue("3178956457");
                myRef.child("compania").child("JC").child("tecnicos").child(userCorrect).child("serviciosPendientes").child("Buger King cra93").child("servicio").setValue("maquina helado");
                myRef.child("compania").child("JC").child("tecnicos").child(userCorrect).child("serviciosPendientes").child("KFC centro mayor").child("cliente").setValue("KFC");
                myRef.child("compania").child("JC").child("tecnicos").child(userCorrect).child("serviciosPendientes").child("KFC centro mayor").child("direccion").setValue("centro mayor");
                myRef.child("compania").child("JC").child("tecnicos").child(userCorrect).child("serviciosPendientes").child("KFC centro mayor").child("correo").setValue("kfc@gmail.com");
                myRef.child("compania").child("JC").child("tecnicos").child(userCorrect).child("serviciosPendientes").child("KFC centro mayor").child("telefono").setValue("3178956457");
                myRef.child("compania").child("JC").child("tecnicos").child(userCorrect).child("serviciosPendientes").child("KFC centro mayor").child("servicio").setValue("maquina freidora");
            }
        });
    }

    private void readDB(){
        requestData();
        String userCorrect = username.substring(0,username.indexOf("@"));
        database = FirebaseDatabase.getInstance();
        myRef = FirebaseDatabase.getInstance().getReference();
        myRef.child("compania").child("JC").child("tecnicos").child(userCorrect).child("serviciosPendientes").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                id.clear();
                for (DataSnapshot key : dataSnapshot.getChildren()){
                    id.add("Servicio:"+key.getKey());
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void requestData(){
        Bundle extras = getIntent().getExtras();
        username = extras.getString("user");

    }

    public void startScanQr(){

        new IntentIntegrator(this).initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode,resultCode,data);
        if(result != null){
            if(result.getContents()==null){
                Toast.makeText(this,"cancelled",Toast.LENGTH_LONG).show();
            }else{
                requestData();
                String userCorrect = username.substring(0,username.indexOf("@"));
                Vibrator v = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(200);
                String ResultCorrect = result.getContents();
                String company = ResultCorrect.substring(0,ResultCorrect.indexOf("/"));
                String service = ResultCorrect.substring(ResultCorrect.indexOf("/")+1);
                //Toast.makeText(this,result.getContents().substring(0,3),Toast.LENGTH_LONG).show();
//                Intent generate = new Intent(EnterActivity.this,FormularyActivity.class);
//                generate.putExtra("company",company);
//                generate.putExtra("service",service);
//                startActivity(generate);
//                Toast.makeText(this,"open formulary",Toast.LENGTH_LONG).show();
            }
        }else{
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onPause() {

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
