package com.example.proyectofinal;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegistroPropietarioActivity extends AppCompatActivity {

    private EditText edtNombre;
    private EditText edtCorreo;
    private EditText edtTelefono;
    private EditText edtClave;
    private EditText edtConfirmar;

    private Button btnContinuar;

    private TextView txtIniciarSesion;

    private FirebaseAuth auth;
    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_registro_propietario
        );


        // ==========================================
        // COMPONENTES
        // ==========================================

        edtNombre =
                findViewById(
                        R.id.edtNombrePropietario
                );

        edtCorreo =
                findViewById(
                        R.id.edtCorreoPropietario
                );

        edtTelefono =
                findViewById(
                        R.id.edtTelefonoPropietario
                );

        edtClave =
                findViewById(
                        R.id.edtContrasenaPropietario
                );

        edtConfirmar =
                findViewById(
                        R.id.edtConfirmarContrasenaPropietario
                );

        PasswordUtils.configurarVisibilidad(
                edtClave
        );

        PasswordUtils.configurarVisibilidad(
                edtConfirmar
        );

        btnContinuar =
                findViewById(
                        R.id.btnContinuarPropietario
                );

        txtIniciarSesion =
                findViewById(
                        R.id.txtIniciarSesion
                );

        txtIniciarSesion.setPaintFlags(
                txtIniciarSesion.getPaintFlags()
                        | android.graphics.Paint.UNDERLINE_TEXT_FLAG
        );


        // ==========================================
        // FIREBASE
        // ==========================================

        auth =
                FirebaseAuth.getInstance();

        db =
                FirebaseFirestore.getInstance();


        // ==========================================
        // CREAR CUENTA
        // ==========================================

        btnContinuar.setOnClickListener(v ->
                registrar()
        );


        // ==========================================
        // INICIAR SESIÓN
        // ==========================================

        txtIniciarSesion.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            RegistroPropietarioActivity.this,
                            Login.class
                    );

            startActivity(intent);

            finish();
        });
    }


    // ==============================================
    // REGISTRAR
    // ==============================================

    private void registrar() {

        String nombre =
                edtNombre
                        .getText()
                        .toString()
                        .trim();

        String correo =
                edtCorreo
                        .getText()
                        .toString()
                        .trim();

        String telefono =
                edtTelefono
                        .getText()
                        .toString()
                        .trim();

        String clave =
                edtClave
                        .getText()
                        .toString()
                        .trim();

        String confirmar =
                edtConfirmar
                        .getText()
                        .toString()
                        .trim();


        // ==========================================
        // CAMPOS VACÍOS
        // ==========================================

        if (nombre.isEmpty() ||
                correo.isEmpty() ||
                telefono.isEmpty() ||
                clave.isEmpty() ||
                confirmar.isEmpty()) {

            Toast.makeText(
                    this,
                    "Completa todos los campos",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        // ==========================================
        // CORREO VÁLIDO
        // ==========================================

        if (!Patterns.EMAIL_ADDRESS
                .matcher(correo)
                .matches()) {

            edtCorreo.setError(
                    "Correo no válido"
            );

            edtCorreo.requestFocus();

            return;
        }


        // ==========================================
        // CONTRASEÑA
        // ==========================================

        if (clave.length() < 6) {

            edtClave.setError(
                    "La contraseña debe tener al menos 6 caracteres"
            );

            edtClave.requestFocus();

            return;
        }


        // ==========================================
        // CONFIRMAR CONTRASEÑA
        // ==========================================

        if (!clave.equals(confirmar)) {

            edtConfirmar.setError(
                    "Las contraseñas no coinciden"
            );

            edtConfirmar.requestFocus();

            return;
        }


        // ==========================================
        // FIREBASE AUTHENTICATION
        // ==========================================

        auth.createUserWithEmailAndPassword(
                        correo,
                        clave
                )

                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        FirebaseUser user =
                                auth.getCurrentUser();


                        if (user != null) {

                            guardarPerfil(
                                    user.getUid(),
                                    nombre,
                                    correo,
                                    telefono
                            );
                        }

                    } else {

                        Exception error =
                                task.getException();


                        // ==================================
                        // CORREO YA REGISTRADO
                        // ==================================

                        if (error instanceof
                                FirebaseAuthUserCollisionException) {

                            Toast.makeText(
                                    this,
                                    "Este correo ya está asociado a una cuenta. Inicia sesión para continuar.",
                                    Toast.LENGTH_LONG
                            ).show();


                            edtCorreo.setError(
                                    "Correo ya registrado"
                            );

                            edtCorreo.requestFocus();

                        } else {

                            Toast.makeText(
                                    this,
                                    "No se pudo crear la cuenta",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
                });
    }


    // ==============================================
    // GUARDAR PERFIL EN FIRESTORE
    // ==============================================

    private void guardarPerfil(
            String uid,
            String nombre,
            String correo,
            String telefono
    ) {

        Map<String, Object> usuario =
                new HashMap<>();


        usuario.put(
                "nombre",
                nombre
        );

        usuario.put(
                "correo",
                correo
        );

        usuario.put(
                "telefono",
                telefono
        );

        usuario.put(
                "tipo",
                "propietario"
        );
        usuario.put(
                "registroCompleto",
                false
        );


        db.collection("usuarios")
                .document(uid)
                .set(usuario)

                .addOnSuccessListener(unused -> {

                    Toast.makeText(
                            this,
                            "Cuenta de propietario creada",
                            Toast.LENGTH_SHORT
                    ).show();


                    Intent intent =
                            new Intent(
                                    RegistroPropietarioActivity.this,
                                    InformacionTallerActivity.class
                            );

                    startActivity(intent);

                    finish();
                })

                .addOnFailureListener(e -> {

                    Toast.makeText(
                            this,
                            "Cuenta creada, pero no se pudo guardar el perfil",
                            Toast.LENGTH_LONG
                    ).show();
                });
    }
}