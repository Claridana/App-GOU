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

import java.util.List;

public class HomePropietario extends AppCompatActivity {

    private TextView txtHola;
    private TextView txtTaller;
    private TextView txtServicios;
    private TextView txtEstado;
    private TextView txtVerOtrosTalleres;

    private Button btnVer;
    private Button btnEditar;
    private Button btnEstado;
    private Button btnSalir;

    private boolean tallerActivo = true;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private String tallerId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_home_propietario
        );


        // ==========================================
        // COMPONENTES
        // ==========================================

        txtHola =
                findViewById(
                        R.id.txtHola
                );

        txtTaller =
                findViewById(
                        R.id.txtTaller
                );

        txtServicios =
                findViewById(
                        R.id.txtServicios
                );

        txtEstado =
                findViewById(
                        R.id.txtEstado
                );

        txtVerOtrosTalleres =
                findViewById(
                        R.id.txtVerOtrosTalleres
                );


        btnEstado =
                findViewById(
                        R.id.btnEstado
                );

        btnVer =
                findViewById(
                        R.id.btnVer
                );

        btnEditar =
                findViewById(
                        R.id.btnEditar
                );

        btnSalir =
                findViewById(
                        R.id.btnSalir
                );


        // ==========================================
        // SUBRAYAR ENLACE
        // ==========================================

        txtVerOtrosTalleres.setPaintFlags(
                txtVerOtrosTalleres.getPaintFlags()
                        | android.graphics.Paint.UNDERLINE_TEXT_FLAG
        );


        // ==========================================
        // FIREBASE
        // ==========================================

        auth =
                FirebaseAuth.getInstance();

        db =
                FirebaseFirestore.getInstance();


        FirebaseUser user =
                auth.getCurrentUser();


        if (user == null) {

            volverInicio();

            return;
        }


        cargarNombre(
                user.getUid()
        );

        cargarTaller(
                user.getUid()
        );


        // ==========================================
        // VER PERFIL DEL TALLER
        // ==========================================

        btnVer.setOnClickListener(v ->
                abrirTaller()
        );


        // ==========================================
        // EDITAR TALLER
        // ==========================================

        btnEditar.setOnClickListener(v ->
                editarTaller()
        );


        // ==========================================
        // ACTIVAR / DESACTIVAR
        // ==========================================

        btnEstado.setOnClickListener(v ->
                cambiarEstado()
        );


        // ==========================================
        // VER OTROS TALLERES
        // ==========================================

        txtVerOtrosTalleres.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            HomePropietario.this,
                            HomeCliente.class
                    );

            startActivity(
                    intent
            );
        });


        // ==========================================
        // CERRAR SESIÓN
        // ==========================================

        btnSalir.setOnClickListener(v -> {

            auth.signOut();

            volverInicio();
        });
    }


    // ==================================================
    // EDITAR TALLER
    // ==================================================

    private void editarTaller() {

        if (tallerId == null) {

            Toast.makeText(
                    this,
                    "No tienes un taller registrado",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        Intent intent =
                new Intent(
                        HomePropietario.this,
                        InformacionTallerActivity.class
                );


        intent.putExtra(
                "tallerId",
                tallerId
        );


        startActivity(
                intent
        );
    }


    // ==================================================
    // CARGAR NOMBRE
    // ==================================================

    private void cargarNombre(
            String uid
    ) {

        db.collection("usuarios")
                .document(uid)
                .get()

                .addOnSuccessListener(documento -> {

                    String nombre =
                            documento.getString(
                                    "nombre"
                            );


                    if (nombre != null &&
                            !nombre.isEmpty()) {

                        txtHola.setText(
                                "¡Hola, "
                                        + nombre
                                        + "!"
                        );
                    }
                });
    }


    // ==================================================
    // CARGAR TALLER
    // ==================================================

    private void cargarTaller(
            String uid
    ) {

        db.collection("talleres")
                .whereEqualTo(
                        "propietarioId",
                        uid
                )
                .limit(1)
                .get()

                .addOnSuccessListener(resultado -> {

                    if (!resultado.isEmpty()) {


                        tallerId =
                                resultado
                                        .getDocuments()
                                        .get(0)
                                        .getId();


                        String nombre =
                                resultado
                                        .getDocuments()
                                        .get(0)
                                        .getString(
                                                "nombre"
                                        );


                        List<String> servicios =
                                (List<String>)
                                        resultado
                                                .getDocuments()
                                                .get(0)
                                                .get(
                                                        "servicios"
                                                );


                        Boolean activo =
                                resultado
                                        .getDocuments()
                                        .get(0)
                                        .getBoolean(
                                                "activo"
                                        );


                        if (activo != null) {

                            tallerActivo =
                                    activo;
                        }


                        mostrarEstado();


                        if (nombre != null) {

                            txtTaller.setText(
                                    nombre
                            );
                        }


                        if (servicios != null) {

                            String texto =
                                    android.text.TextUtils
                                            .join(
                                                    " • ",
                                                    servicios
                                            );


                            txtServicios.setText(
                                    texto
                            );
                        }


                    } else {


                        txtTaller.setText(
                                "Todavía no tienes un taller registrado"
                        );


                        txtServicios.setText(
                                ""
                        );


                        tallerId =
                                null;
                    }
                })

                .addOnFailureListener(e -> {

                    Toast.makeText(
                            this,
                            "No se pudo cargar el taller",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }


    // ==================================================
    // MOSTRAR ESTADO
    // ==================================================

    private void mostrarEstado() {

        if (tallerActivo) {

            txtEstado.setText(
                    "Estado: Activo"
            );

            btnEstado.setText(
                    "Desactivar taller"
            );

        } else {

            txtEstado.setText(
                    "Estado: Inactivo"
            );

            btnEstado.setText(
                    "Activar taller"
            );
        }
    }


    // ==================================================
    // CAMBIAR ESTADO
    // ==================================================

    private void cambiarEstado() {

        if (tallerId == null) {

            Toast.makeText(
                    this,
                    "No tienes un taller registrado",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        boolean nuevoEstado =
                !tallerActivo;


        db.collection("talleres")
                .document(tallerId)
                .update(
                        "activo",
                        nuevoEstado
                )

                .addOnSuccessListener(unused -> {

                    tallerActivo =
                            nuevoEstado;


                    mostrarEstado();


                    if (tallerActivo) {

                        Toast.makeText(
                                this,
                                "Taller activado",
                                Toast.LENGTH_SHORT
                        ).show();

                    } else {

                        Toast.makeText(
                                this,
                                "Taller desactivado",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                })

                .addOnFailureListener(e -> {

                    Toast.makeText(
                            this,
                            "No se pudo cambiar el estado",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }


    // ==================================================
    // ABRIR TALLER
    // ==================================================

    private void abrirTaller() {

        if (tallerId == null) {

            Toast.makeText(
                    this,
                    "No tienes un taller registrado",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        Intent intent =
                new Intent(
                        HomePropietario.this,
                        PerfilTaller.class
                );


        intent.putExtra(
                "tallerId",
                tallerId
        );


        startActivity(
                intent
        );
    }


    // ==================================================
    // VOLVER AL INICIO
    // ==================================================

    private void volverInicio() {

        Intent intent =
                new Intent(
                        HomePropietario.this,
                        MainActivity.class
                );


        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );


        startActivity(
                intent
        );

        finish();
    }
}