package com.example.proyectofinal;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class PerfilUsuario extends AppCompatActivity {

    private TextView txtNombre;
    private TextView txtNombreInfo;
    private TextView txtCorreo;
    private TextView txtTelefono;

    private Button btnSalir;

    private Button btnResenas;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_usuario);

        // Componentes
        txtNombre = findViewById(R.id.txtNombre);
        txtNombreInfo = findViewById(R.id.txtNombreInfo);
        txtCorreo = findViewById(R.id.txtCorreo);
        txtTelefono = findViewById(R.id.txtTelefono);

        btnSalir = findViewById(R.id.btnSalir);
        btnResenas = findViewById(R.id.btnResenas);

        // Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        cargarPerfil();

        btnSalir.setOnClickListener(v -> cerrarSesion());
        btnResenas.setOnClickListener(v -> {

            Intent intent = new Intent(
                    PerfilUsuario.this,
                    MisResenas.class
            );

            startActivity(intent);
        });
    }

    private void cargarPerfil() {

        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            volverInicio();
            return;
        }

        String uid = user.getUid();

        db.collection("usuarios")
                .document(uid)
                .get()
                .addOnSuccessListener(documento -> {

                    if (documento.exists()) {

                        String nombre = documento.getString("nombre");
                        String correo = documento.getString("correo");
                        String telefono = documento.getString("telefono");

                        txtNombre.setText(nombre);
                        txtNombreInfo.setText(nombre);
                        txtCorreo.setText(correo);
                        txtTelefono.setText(telefono);
                    }
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            this,
                            "No se pudo cargar el perfil",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }

    private void cerrarSesion() {

        auth.signOut();

        Toast.makeText(
                this,
                "Sesión cerrada",
                Toast.LENGTH_SHORT
        ).show();

        volverInicio();
    }

    private void volverInicio() {

        Intent intent = new Intent(
                PerfilUsuario.this,
                MainActivity.class
        );

        // Limpiar las pantallas anteriores
        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    }
}