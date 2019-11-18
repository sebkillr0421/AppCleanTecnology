package h2h.iot.panic;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.Date;

public class FormularyActivity extends AppCompatActivity {
    private Date currentTime;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private String company,numOrder;
    private TextView companyName,order,fecha,direccion,telefono,cliente;
    private TextInputEditText descripcion;
    private EditText nit,email,valor;
    private static final int STORAGE_CODE=1000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formulary);
        getdates();
        FloatingActionButton fab = findViewById(R.id.sending);
        companyName = findViewById(R.id.companyName);
        order = findViewById(R.id.numServicio);
        fecha = findViewById(R.id.date);
        cliente = findViewById(R.id.cliente);
        direccion = findViewById(R.id.adreess);
        descripcion = findViewById(R.id.direccion1);
        nit = findViewById(R.id.nit);
        email = findViewById(R.id.correo);
        telefono = findViewById(R.id.numberPhone);
        valor = findViewById(R.id.Valor);
        currentTime = Calendar.getInstance().getTime();
        fecha.setText(currentTime.toString());
        companyName.setText(company);
        order.setText(numOrder);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(FormularyActivity.this);
                dialog.setTitle("Confirmar");
                dialog.setIcon(android.R.drawable.ic_menu_save);
                dialog.setMessage("servicio tecnico finalizado");
                dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.M){
                            if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                                String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                                requestPermissions(permissions,STORAGE_CODE);
                            }else{
                                writeDate();
                            }
                        }else {

                        }

                    }
                });
                dialog.setNegativeButton("No",null);
                dialog.show();
            }
        });


    }

    private void getdates(){
        Bundle extras = getIntent().getExtras();
        company = extras.getString("company");
        numOrder = extras.getString("service");
    }

    private void writeDate(){
        Document mDoc = new Document();
        String mFile = "orden_de_servicio_"+numOrder;
        File external = Environment.getExternalStorageDirectory();
        String mFilepath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+mFile+".pdf";
        Uri uri = Uri.fromFile(new File(external.getAbsolutePath()+"/"+mFile+".pdf"));

        try {
            //create pdf writer class
            PdfWriter.getInstance(mDoc,new FileOutputStream(mFilepath));
            //open document and write
            mDoc.open();
            mDoc.addTitle("Orden de servicio");
            mDoc.addAuthor(company);

            mDoc.add(new Paragraph(company));
            mDoc.add(new Paragraph(" "));
            mDoc.add(new Paragraph(" "));
            mDoc.add(new Paragraph("Fecha: "+currentTime.toString()));
            mDoc.add(new Paragraph("numero de orden: "+numOrder));
            mDoc.add(new Paragraph("cliente: "+cliente.getText().toString()));
            mDoc.add(new Paragraph("nit: "+nit.getText().toString()));
            mDoc.add(new Paragraph("telefono: "+telefono.getText().toString()));
            mDoc.add(new Paragraph("email: "+email.getText().toString()));
            mDoc.add(new Paragraph("tecnico: "+email.getText().toString()));
            mDoc.add(new Paragraph("maquina reparada: "+email.getText().toString()));
            mDoc.add(new Paragraph(" "));
            mDoc.add(new Paragraph(" "));
            mDoc.add(new Paragraph("Descripcion:"));
            mDoc.add(new Paragraph(" "));
            mDoc.add(new Paragraph(descripcion.getText().toString()));
            mDoc.add(new Paragraph(" "));
            mDoc.add(new Paragraph(" "));
            mDoc.add(new Paragraph("Valor: $"+valor.getText().toString()));
            mDoc.close();

            //Toast.makeText(FormularyActivity.this,mFilepath+"to Save pdf",Toast.LENGTH_LONG).show();
            //send email client
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {email.getText().toString()});
            shareIntent.putExtra(Intent.EXTRA_SUBJECT,"Factura servicio tecnico");
            shareIntent.putExtra(Intent.EXTRA_TEXT,"querido "+cliente.getText().toString()+" aca esta su factura del servicio tecnico");
            shareIntent.setType("application/pdf");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri.parse(mFilepath));
            startActivity(shareIntent);

         }catch (Exception e){

         }

        database = FirebaseDatabase.getInstance();
        myRef = FirebaseDatabase.getInstance().getReference();
        myRef.child("company").child(company).child("services").child(numOrder).child("fecha").push().setValue(currentTime.toString());
        myRef.child("company").child(company).child("services").child(numOrder).child("cliente").push().setValue(currentTime.toString());
        myRef.child("company").child(company).child("services").child(numOrder).child("nit").push().setValue(nit.getText().toString());
        myRef.child("company").child(company).child("services").child(numOrder).child("telefono").push().setValue(telefono.getText().toString());
        myRef.child("company").child(company).child("services").child(numOrder).child("email").push().setValue(email.getText().toString());
        myRef.child("company").child(company).child("services").child(numOrder).child("tecnico").push().setValue(currentTime.toString());
        myRef.child("company").child(company).child("services").child(numOrder).child("objeto").push().setValue(currentTime.toString());
        myRef.child("company").child(company).child("services").child(numOrder).child("descripcion").push().setValue(descripcion.getText().toString());
        myRef.child("company").child(company).child("services").child(numOrder).child("valor").push().setValue("$"+valor.toString());

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case STORAGE_CODE:{
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    writeDate();
                }else{
                    Toast.makeText(this,"permiso denegado",Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
