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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class Login extends AppCompatActivity {

    private EditText edtCorreo;
    private EditText edtClave;

    private Button btnEntrar;
    private TextView txtCrear;
    private TextView txtOlvideClave;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Componentes
        edtCorreo = findViewById(R.id.edtCorreo);
        edtClave = findViewById(R.id.edtClave);
        PasswordUtils.configurarVisibilidad( edtClave);

        btnEntrar = findViewById(R.id.btnEntrar);
        txtCrear = findViewById(R.id.txtCrear);
        txtOlvideClave = findViewById(R.id.txtOlvideClave);

        // Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        // Iniciar sesión
        btnEntrar.setOnClickListener(v -> iniciar());

        // Crear cuenta
        txtCrear.setOnClickListener(v -> {

            Intent intent = new Intent(
                    Login.this,
                    RegistroClienteActivity.class
            );

            startActivity(intent);
        });

        txtOlvideClave.setOnClickListener(v ->
                recuperarContrasena()
        );
    }
    private void recuperarContrasena() {

        String correo =
                edtCorreo
                        .getText()
                        .toString()
                        .trim();


        // No escribió correo
        if (correo.isEmpty()) {

            edtCorreo.setError(
                    "Escribe tu correo electrónico"
            );

            edtCorreo.requestFocus();

            Toast.makeText(
                    this,
                    "Escribe tu correo para recuperar la contraseña",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        // Formato incorrecto
        if (!Patterns.EMAIL_ADDRESS
                .matcher(correo)
                .matches()) {

            edtCorreo.setError(
                    "Correo no válido"
            );

            edtCorreo.requestFocus();

            return;
        }


        // Evitar varios clics mientras Firebase responde
        txtOlvideClave.setEnabled(false);

        txtOlvideClave.setText(
                "Enviando correo..."
        );

        auth.setLanguageCode("es");
        auth.sendPasswordResetEmail(correo)

                .addOnCompleteListener(task -> {

                    txtOlvideClave.setEnabled(true);

                    txtOlvideClave.setText(
                            "¿Olvidaste tu contraseña?"
                    );


                    if (task.isSuccessful()) {

                        Toast.makeText(
                                this,
                                "Correo de recuperación enviado. Revisa tu bandeja de entrada.",
                                Toast.LENGTH_LONG
                        ).show();

                    } else {

                        Toast.makeText(
                                this,
                                "No se pudo enviar el correo de recuperación",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }


    private void iniciar() {

        String correo = edtCorreo.getText().toString().trim();
        String clave = edtClave.getText().toString().trim();

        // Campos vacíos
        if (correo.isEmpty() || clave.isEmpty()) {

            Toast.makeText(
                    this,
                    "Completa todos los campos",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // Correo válido
        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {

            edtCorreo.setError("Correo no válido");
            edtCorreo.requestFocus();
            return;
        }

        // Firebase Authentication
        auth.signInWithEmailAndPassword(correo, clave)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        revisarTipo();

                    } else {

                        Toast.makeText(
                                this,
                                "Correo o contraseña incorrectos",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }
    private void revisarTipo() {

        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {

            Toast.makeText(
                    this,
                    "No se pudo identificar al usuario",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String uid = user.getUid();

        db.collection("usuarios")
                .document(uid)
                .get()
                .addOnSuccessListener(documento -> {

                    if (!documento.exists()) {

                        Toast.makeText(
                                this,
                                "No se encontró el perfil del usuario",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }

                    String tipo = documento.getString("tipo");

                    if ("cliente".equals(tipo)) {

                        Intent intent = new Intent(
                                Login.this,
                                HomeCliente.class
                        );

                        startActivity(intent);
                        finish();

                    } else if ("propietario".equals(tipo)) {

                        Boolean registroCompleto =
                                documento.getBoolean(
                                        "registroCompleto"
                                );


                        // ==========================================
                        // REGISTRO DE TALLER INCOMPLETO
                        // ==========================================

                        if (registroCompleto == null ||
                                !registroCompleto) {


                            Toast.makeText(
                                    this,
                                    "Completa el registro de tu taller para continuar",
                                    Toast.LENGTH_LONG
                            ).show();


                            Intent intent =
                                    new Intent(
                                            Login.this,
                                            InformacionTallerActivity.class
                                    );


                            intent.setFlags(
                                    Intent.FLAG_ACTIVITY_NEW_TASK |
                                            Intent.FLAG_ACTIVITY_CLEAR_TASK
                            );


                            startActivity(intent);

                            finish();


                        } else {


                            // ======================================
                            // PROPIETARIO COMPLETAMENTE REGISTRADO
                            // ======================================

                            Intent intent =
                                    new Intent(
                                            Login.this,
                                            HomePropietario.class
                                    );


                            startActivity(intent);

                            finish();
                        }

                    } else {

                        Toast.makeText(
                                this,
                                "Tipo de usuario no válido",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            this,
                            "No se pudo consultar el perfil",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }
}