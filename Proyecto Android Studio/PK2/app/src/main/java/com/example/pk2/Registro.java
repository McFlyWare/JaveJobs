package com.example.pk2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pk2.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class Registro extends AppCompatActivity {

    TextView correo;
    TextView contraseña;
    TextView nombre;
    TextView cedula;
    Button botonRegistro;
    Spinner sItems ;
    //Autenticacion de la base de datos
    FirebaseAuth mAuth;
    //Base de datos
    FirebaseDatabase database;
    DatabaseReference myRef;
    //Ruta en la que se guarda el usuario
    static final String PATH_USERS = "users/";
    List<String> spinnerArray =  new ArrayList<String>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);
        getWindow().setStatusBarColor(getResources().getColor(R.color.moraitoMelo));
        //inflate base de datos y autenticacion
        mAuth =FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        //inflate texts para obtener los datos
        correo = findViewById(R.id.inputMailRegi);
        contraseña = findViewById(R.id.inputPasswordRegi);
        nombre = findViewById(R.id.nombreInputRegi);
        cedula = findViewById(R.id.cedulaInputRegi);
        botonRegistro = findViewById(R.id.botonRegistro);
        // creacion del spinner y de los datos que van dentro de este
        spinnerArray.add("Administracion");
        spinnerArray.add("Programador");
        spinnerArray.add("Diseñador");
        spinnerArray.add("Arquitecto");
        spinnerArray.add("Ingeniero");
        spinnerArray.add("Medicina");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, R.layout.spinner_registro, spinnerArray);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sItems = (Spinner) findViewById(R.id.spinner2);
        sItems.setAdapter(adapter);
        botonRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               regist(view);
            }
        });
    }
    public void regist(View v)
    {
        String mail = correo.getText().toString();
        String pass = contraseña.getText().toString();
        String name = nombre.getText().toString();
        String lastN = "testestt";
        String cc = cedula.getText().toString();
        String sector = sItems.getSelectedItem().toString();
        if(validarDatos(mail,pass))
        {
            /*Creacion de la autenticacion para el login despues de validar el formato del correo y
            tamaño de la contraseña
             */
            mAuth.createUserWithEmailAndPassword(mail,pass).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    //valida que la autenticacion se guarde de forma correcta en la BD
                    if(task.isSuccessful())
                    {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if(user != null) {
                            /*UserProfileChangeRequest.Builder upcrb = new UserProfileChangeRequest.Builder();
                            String rol = "0";
                            upcrb.setDisplayName(rol);
                            user.updateProfile(upcrb.build());*/
                            guardarDatos(mail,pass,name,lastN,cc, user.getUid(), sector);
                            actualizarPantalla(user);
                        }
                    }else
                    {
                        //error al subir a la base de datos
                        Toast.makeText(Registro.this,"Error: "+task.getException().toString(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }else
        {
            //dialogo para error en la contraseña o correo
            Toast.makeText(Registro.this,"contraseña o correo erroneo", Toast.LENGTH_LONG).show();
        }
    }
    private void actualizarPantalla(FirebaseUser user)
    {
        if(user != null)
        {
            Intent intent = new Intent(Registro.this,LogIn.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }
    private void guardarDatos(String mail,String pass,String name,String lastN,String cc, String uid, String sector)
    {
        //validacion
        int cedula = Integer.parseInt(cc);
        if(cedula > 0 && !name.isEmpty() && !lastN.isEmpty())
        {
            //creacion del objeto que se va a guardar
            Usuario usuario = new Usuario();
            usuario.setApellido(lastN);
            usuario.setNombre(name);
            usuario.setCedula(cc);
            usuario.setCcontraseña(pass);
            usuario.setCorreo(mail);
            usuario.setId(uid);
            usuario.setUbi(false);
            usuario.setSector(sector);
            myRef = database.getReference(PATH_USERS);
            //asignacion de cc como key
            myRef = database.getReference(PATH_USERS + uid);
            myRef.setValue(usuario);
        }
    }

    private boolean validarDatos(String mail, String pass)
    {
        //validacion forma de correo y tamaño de contraseña correctos
        if(Patterns.EMAIL_ADDRESS.matcher(mail).matches() && pass.length()>=6) {
            return true;
        }else
            return false;
    }
}
