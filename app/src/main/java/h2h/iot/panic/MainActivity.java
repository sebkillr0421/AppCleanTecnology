package h2h.iot.panic;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

import java.io.UnsupportedEncodingException;



public class MainActivity extends AppCompatActivity {


    public String name,psw;
    private EditText user;
    private int cont;
    private EditText pass;
    private DatabaseReference mDataBase;
    private FirebaseAuth fireAuth;
    private Button Log,register;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //requestPermisions();

        user = (EditText) findViewById(R.id.email);
        pass = (EditText) findViewById(R.id.password);
        mDataBase = FirebaseDatabase.getInstance().getReference("varFirebase");
        fireAuth = FirebaseAuth.getInstance();
        Log = findViewById(R.id.signIn);
        register = findViewById(R.id.signUp);
        Log.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readDates();
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent entry = new Intent(MainActivity.this,MainRegister.class);
                startActivity(entry);
            }
        });

    }


    //sign in existing users

    public void readDates(){
        name = user.getText().toString().trim();
        psw = pass.getText().toString().trim();
        if(!TextUtils.isEmpty(name) || !TextUtils.isEmpty(psw)){
            fireAuth.signInWithEmailAndPassword(name,psw).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(MainActivity.this,"success acount",Toast.LENGTH_LONG).show();
                        Intent entry = new Intent(MainActivity.this,ArmMachines.class);
                        entry.putExtra("user",name);
                        startActivity(entry);

                    }else{
                        Toast.makeText(MainActivity.this,"Email or password incorrect",Toast.LENGTH_LONG).show();
                    }
                }
            });
        }else{
            Toast.makeText(this, "please insert user or pass",Toast.LENGTH_LONG).show();
            return;
        }
    }

}
