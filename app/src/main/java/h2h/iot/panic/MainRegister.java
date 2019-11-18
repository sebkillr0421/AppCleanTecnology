package h2h.iot.panic;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainRegister extends AppCompatActivity {

    private FirebaseAuth fireAuth;
    private Button register;
    private EditText email, pass, confirmPass;
    private EditText phone;
    private String emailNew,passNew, confirmPassNew, phoneNew;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_register);

        fireAuth = FirebaseAuth.getInstance();
        register = findViewById(R.id.Reg);
        email = (EditText) findViewById(R.id.email);
        pass = (EditText) findViewById(R.id.passNew);
        confirmPass = (EditText) findViewById(R.id.confirmPassNew);
        phone = (EditText) findViewById(R.id.phone);
        // add phone

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

    }

    public void registerUser(){
        emailNew = email.getText().toString().trim();
        passNew = pass.getText().toString().trim();
        confirmPassNew = confirmPass.getText().toString().trim();
        phoneNew = phone.getText().toString().trim();
        if(!TextUtils.isEmpty(emailNew) || !TextUtils.isEmpty(passNew) || !TextUtils.isEmpty(confirmPassNew) || !TextUtils.isEmpty(phoneNew)){
            // read data base firebase
            // note: consults in real time is the cloud mqtt
            if(passNew.equals(confirmPassNew)) {

                fireAuth.createUserWithEmailAndPassword(emailNew,passNew).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(MainRegister.this,"Usuario creado",Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(MainRegister.this,MainActivity.class);
                            startActivity(intent);
                        }else{
                            Toast.makeText(MainRegister.this,"usuario ya existe",Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }else{Toast.makeText(this, "verificar contraseña",Toast.LENGTH_LONG).show();}
        }else{
            Toast.makeText(this, "completar formulario porfavor",Toast.LENGTH_LONG).show();

        }
    }
}
