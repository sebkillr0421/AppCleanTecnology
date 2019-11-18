package h2h.iot.panic;

import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
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
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;

public class ArmMachines extends AppCompatActivity {

    private String username;
    private ArrayList<String> id;
    private ArrayAdapter<String> adapter;
    private ListView lv;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private MqttAndroidClient client;
    private String topic ;
    private int qos = 1;
    private String clientId = MqttClient.generateClientId();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arm_machines);
        id = new ArrayList<String>();
        adapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,id);
        readDB();
        topic="";
        lv = findViewById(R.id.list_machine);
        lv.setAdapter(adapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String listDate = lv.getItemAtPosition(position).toString().trim();
                Intent open = new Intent(ArmMachines.this, PopDate.class);
                open.putExtra("id",listDate.substring(8));
                open.putExtra("user",username);
                startActivity(open);


            }
        });
        final FloatingActionButton scan = findViewById(R.id.addMachine);
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanQr();
            }
        });

        final FloatingActionButton openMap = findViewById(R.id.mapLocation);
        openMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mapping = new Intent(ArmMachines.this, MainMap.class);
                mapping.putExtra("user",username);
                startActivity(mapping);
            }
        });


    }

    private void scanQr(){
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
                database = FirebaseDatabase.getInstance();
                myRef = FirebaseDatabase.getInstance().getReference();
                myRef.child(userCorrect).child(result.getContents()).child("status").setValue("on");
                myRef.child(userCorrect).child(result.getContents()).child("location").setValue("{\"lat\":0,\"lng\":0}");//using format JSON
                myRef.child(userCorrect).child(result.getContents()).child("key").setValue("30/11/19");// status machine if buy or rented buy
            }
        }else{
            super.onActivityResult(requestCode, resultCode, data);
        }
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
                for (DataSnapshot key : dataSnapshot.getChildren()){
                    id.add("Maquina:"+key.getKey());
                    adapter.notifyDataSetChanged();
                    Toast.makeText(ArmMachines.this,"change DB", Toast.LENGTH_LONG).show();
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


}
