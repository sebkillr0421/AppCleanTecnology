package h2h.iot.panic;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class ConfirmActivity extends AppCompatActivity {

    private static final int PERMISSION_CODE = 1000;
    private static final int IMAGE_CAPTURE_CODE = 1001;
    private String key,username,numberPhone;
    private TextView cliente1,direccion1,correo1,telefono1,servicio1;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    Uri image_uri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        requestDate();
        cliente1 = findViewById(R.id.cliente1);
        direccion1 = findViewById(R.id.direccion1);
        correo1 = findViewById(R.id.correo1);
        telefono1 = findViewById(R.id.tefelofono1);
        servicio1 = findViewById(R.id.servicioSolicitado);
        readDB();
        int ancho = displayMetrics.widthPixels;
        int alto  = displayMetrics.heightPixels;
        getWindow().setLayout((int)(ancho*0.85),(int)(alto*0.55));
        FloatingActionButton photo = findViewById(R.id.photoCall);
        photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ConfirmActivity.this,"abriendo camara",Toast.LENGTH_LONG).show();
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if(checkSelfPermission(Manifest.permission.CAMERA)== PackageManager.PERMISSION_DENIED || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_DENIED){
                       String[] permission = {Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
                       requestPermissions(permission,PERMISSION_CODE);
                    }else{
                        //permssion granted
                        openCamera();
                    }
                }else{
                    //system os < marshmallow
                }
            }
        });


        FloatingActionButton Qr = findViewById(R.id.checkCall);
        Qr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScanQr();
            }
        });

        FloatingActionButton callFriend = findViewById(R.id.phoneCall);
        callFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ConfirmActivity.this,"LLamando a "+key,Toast.LENGTH_LONG).show();
                String dial = "tel:"+numberPhone;
                startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse(dial)));

            }
        });
    }





    private void requestDate(){
        Bundle extras = getIntent().getExtras();
        key = extras.getString("key");
        username = extras.getString("username");
    }

    private void readDB(){
        requestDate();
        String userCorrect = username.substring(0,username.indexOf("@"));
        database = FirebaseDatabase.getInstance();
        myRef = FirebaseDatabase.getInstance().getReference();
        myRef.child("compania").child("JC").child("tecnicos").child(userCorrect).child("serviciosPendientes").child(key).child("cliente").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                cliente1.setText(dataSnapshot.getValue(String.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        myRef.child("compania").child("JC").child("tecnicos").child(userCorrect).child("serviciosPendientes").child(key).child("correo").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                correo1.setText(dataSnapshot.getValue(String.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        myRef.child("compania").child("JC").child("tecnicos").child(userCorrect).child("serviciosPendientes").child(key).child("direccion").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                direccion1.setText(dataSnapshot.getValue(String.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        myRef.child("compania").child("JC").child("tecnicos").child(userCorrect).child("serviciosPendientes").child(key).child("servicio").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                servicio1.setText(dataSnapshot.getValue(String.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        myRef.child("compania").child("JC").child("tecnicos").child(userCorrect).child("serviciosPendientes").child(key).child("telefono").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                numberPhone = dataSnapshot.getValue(String.class);
                telefono1.setText(numberPhone);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }

    private void openCamera(){
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"nueva foto");
        values.put(MediaStore.Images.Media.DESCRIPTION,"camara");
        image_uri =  getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(cameraIntent,IMAGE_CAPTURE_CODE);
    }

    public void startScanQr(){

        new IntentIntegrator(this).initiateScan();
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case PERMISSION_CODE:{
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    openCamera();
                }else{
                    Toast.makeText(this,"Permiso denegado", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK){

        }

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode,resultCode,data);
        if(result != null){
            if(result.getContents()==null){
                Toast.makeText(this,"cancelled",Toast.LENGTH_LONG).show();
            }else{
               // requestData();
                //String userCorrect = username.substring(0,username.indexOf("@"));
                Vibrator v = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(200);
                String ResultCorrect = result.getContents();
                String company = ResultCorrect.substring(0,ResultCorrect.indexOf("/"));
                String service = ResultCorrect.substring(ResultCorrect.indexOf("/")+1);
                Intent generate = new Intent(ConfirmActivity.this,FormularyActivity.class);
                generate.putExtra("company",company);
                generate.putExtra("service",service);
                startActivity(generate);
                Toast.makeText(this,"open formulary",Toast.LENGTH_LONG).show();
            }
        }else{
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

}
