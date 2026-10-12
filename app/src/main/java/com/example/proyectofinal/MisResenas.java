package com.example.proyectofinal;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class MisResenas extends AppCompatActivity {


    // ==================================================
    // COMPONENTES
    // ==================================================

    private TextView btnVolver;
    private TextView txtEstado;

    private LinearLayout contenedorResenas;


    // ==================================================
    // FIREBASE
    // ==================================================

    private FirebaseAuth auth;
    private FirebaseFirestore db;


    // ==================================================
    // ON CREATE
    // ==================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_mis_resenas
        );


        // --------------------------------------------------
        // COMPONENTES
        // --------------------------------------------------

        btnVolver =
                findViewById(
                        R.id.btnVolver
                );


        txtEstado =
                findViewById(
                        R.id.txtEstado
                );


        contenedorResenas =
                findViewById(
                        R.id.contenedorResenas
                );


        // --------------------------------------------------
        // FIREBASE
        // --------------------------------------------------

        auth =
                FirebaseAuth.getInstance();


        db =
                FirebaseFirestore.getInstance();


        // --------------------------------------------------
        // VOLVER
        // --------------------------------------------------

        btnVolver.setOnClickListener(v ->
                finish()
        );
    }


    // ==================================================
    // ACTUALIZAR CADA VEZ QUE VOLVEMOS
    // ==================================================

    @Override
    protected void onResume() {

        super.onResume();

        cargarMisResenas();
    }


    // ==================================================
    // CARGAR MIS RESEÑAS
    // ==================================================

    private void cargarMisResenas() {


        FirebaseUser user =
                auth.getCurrentUser();


        if (user == null) {


            Toast.makeText(
                    this,
                    "Debes iniciar sesión",
                    Toast.LENGTH_SHORT
            ).show();


            finish();

            return;
        }


        String uid =
                user.getUid();


        txtEstado.setVisibility(
                View.VISIBLE
        );


        txtEstado.setText(
                "Cargando tus reseñas..."
        );


        db.collection("resenas")
                .whereEqualTo(
                        "usuarioId",
                        uid
                )
                .get()

                .addOnSuccessListener(resultado -> {


                    limpiarTarjetas();


                    // --------------------------------------------------
                    // SIN RESEÑAS
                    // --------------------------------------------------

                    if (resultado.isEmpty()) {


                        txtEstado.setVisibility(
                                View.VISIBLE
                        );


                        txtEstado.setText(
                                "Aún no has publicado reseñas."
                        );


                        return;
                    }


                    txtEstado.setVisibility(
                            View.GONE
                    );


                    // --------------------------------------------------
                    // RECORRER RESEÑAS
                    // --------------------------------------------------

                    for (
                            DocumentSnapshot documento
                            : resultado.getDocuments()
                    ) {


                        Resena resena =
                                documento.toObject(
                                        Resena.class
                                );


                        if (resena != null) {


                            agregarResena(
                                    resena
                            );
                        }
                    }

                })

                .addOnFailureListener(e -> {


                    txtEstado.setVisibility(
                            View.VISIBLE
                    );


                    txtEstado.setText(
                            "No se pudieron cargar tus reseñas"
                    );
                });
    }


    // ==================================================
    // LIMPIAR TARJETAS ANTERIORES
    // ==================================================

    private void limpiarTarjetas() {


        // txtEstado es el primer elemento
        while (
                contenedorResenas.getChildCount() > 1
        ) {


            contenedorResenas.removeViewAt(
                    1
            );
        }
    }


    // ==================================================
    // AGREGAR UNA RESEÑA
    // ==================================================

    private void agregarResena(
            Resena resena
    ) {


        View vista =
                LayoutInflater
                        .from(this)
                        .inflate(
                                R.layout.item_resena,
                                contenedorResenas,
                                false
                        );


        // --------------------------------------------------
        // COMPONENTES DE LA TARJETA
        // --------------------------------------------------

        TextView txtTaller =
                vista.findViewById(
                        R.id.txtUsuario
                );


        RatingBar rating =
                vista.findViewById(
                        R.id.ratingResena
                );


        TextView txtComentario =
                vista.findViewById(
                        R.id.txtComentario
                );


        TextView txtFecha =
                vista.findViewById(
                        R.id.txtFecha
                );


        // NUEVO:
        // aviso cuando el taller está inactivo
        // o ya no existe.
        TextView txtTallerNoDisponible =
                vista.findViewById(
                        R.id.txtTallerNoDisponible
                );


        LinearLayout contenedorAcciones =
                vista.findViewById(
                        R.id.contenedorAccionesResena
                );


        Button btnEditar =
                vista.findViewById(
                        R.id.btnEditarResena
                );


        Button btnEliminar =
                vista.findViewById(
                        R.id.btnEliminarResena
                );


        // --------------------------------------------------
        // ESTRELLAS
        // --------------------------------------------------

        rating.setRating(
                (float)
                        resena.getCalificacion()
        );


        // --------------------------------------------------
        // COMENTARIO
        // --------------------------------------------------

        txtComentario.setText(
                resena.getComentario()
        );


        // --------------------------------------------------
        // FECHA
        // --------------------------------------------------

        Timestamp fecha =
                resena.getFecha();


        if (fecha != null) {


            SimpleDateFormat formato =
                    new SimpleDateFormat(
                            "dd/MM/yyyy",
                            Locale.getDefault()
                    );


            txtFecha.setText(
                    formato.format(
                            fecha.toDate()
                    )
            );


        } else {


            txtFecha.setText(
                    ""
            );
        }


        // --------------------------------------------------
        // BOTONES EDITAR / ELIMINAR
        // --------------------------------------------------

        contenedorAcciones.setVisibility(
                View.VISIBLE
        );


        btnEditar.setOnClickListener(v ->
                editarResena(
                        resena
                )
        );


        btnEliminar.setOnClickListener(v ->
                confirmarEliminarResena(
                        resena
                )
        );


        // --------------------------------------------------
        // CONSULTAR EL TALLER
        // --------------------------------------------------

        String tallerId =
                resena.getTallerId();


        if (tallerId != null &&
                !tallerId.isEmpty()) {


            db.collection("talleres")
                    .document(tallerId)
                    .get()

                    .addOnSuccessListener(documento -> {


                        // ==========================================
                        // EL TALLER YA NO EXISTE
                        // ==========================================

                        if (!documento.exists()) {


                            txtTaller.setText(
                                    "Taller no disponible"
                            );


                            txtTallerNoDisponible.setVisibility(
                                    View.VISIBLE
                            );


                            return;
                        }


                        // ==========================================
                        // NOMBRE DEL TALLER
                        // ==========================================

                        String nombre =
                                documento.getString(
                                        "nombre"
                                );


                        if (nombre != null &&
                                !nombre.isEmpty()) {


                            txtTaller.setText(
                                    nombre
                            );


                        } else {


                            txtTaller.setText(
                                    "Taller"
                            );
                        }


                        // ==========================================
                        // ESTADO DEL TALLER
                        // ==========================================

                        Boolean activo =
                                documento.getBoolean(
                                        "activo"
                                );


                        // ------------------------------------------
                        // TALLER INACTIVO
                        // ------------------------------------------

                        if (Boolean.FALSE.equals(
                                activo
                        )) {


                            txtTallerNoDisponible.setVisibility(
                                    View.VISIBLE
                            );


                        } else {


                            // --------------------------------------
                            // TALLER ACTIVO
                            // --------------------------------------

                            txtTallerNoDisponible.setVisibility(
                                    View.GONE
                            );
                        }
                    })

                    .addOnFailureListener(e -> {


                        // Si hubo un error consultando Firestore,
                        // no afirmamos que esté inactivo.
                        txtTaller.setText(
                                "Taller"
                        );


                        txtTallerNoDisponible.setVisibility(
                                View.GONE
                        );
                    });


        } else {


            // --------------------------------------------------
            // LA RESEÑA NO TIENE TALLER ASOCIADO
            // --------------------------------------------------

            txtTaller.setText(
                    "Taller no disponible"
            );


            txtTallerNoDisponible.setVisibility(
                    View.VISIBLE
            );
        }


        // --------------------------------------------------
        // AGREGAR TARJETA
        // --------------------------------------------------

        contenedorResenas.addView(
                vista
        );
    }


    // ==================================================
    // EDITAR RESEÑA
    // ==================================================

    private void editarResena(
            Resena resena
    ) {


        Intent intent =
                new Intent(
                        MisResenas.this,
                        CalificarTaller.class
                );


        intent.putExtra(
                "tallerId",
                resena.getTallerId()
        );


        intent.putExtra(
                "modoEdicion",
                true
        );


        intent.putExtra(
                "calificacion",
                resena.getCalificacion()
        );


        intent.putExtra(
                "comentario",
                resena.getComentario()
        );


        startActivity(
                intent
        );
    }


    // ==================================================
    // CONFIRMAR ELIMINACIÓN
    // ==================================================

    private void confirmarEliminarResena(
            Resena resena
    ) {


        new AlertDialog
                .Builder(this)

                .setTitle(
                        "Eliminar reseña"
                )

                .setMessage(
                        "¿Estás seguro de que deseas eliminar esta reseña?"
                )

                .setNegativeButton(
                        "Cancelar",
                        null
                )

                .setPositiveButton(
                        "Eliminar",
                        (dialog, which) ->
                                eliminarResena(
                                        resena
                                )
                )

                .show();
    }


    // ==================================================
    // ELIMINAR RESEÑA
    // ==================================================

    private void eliminarResena(
            Resena resena
    ) {


        FirebaseUser user =
                auth.getCurrentUser();


        if (user == null) {


            Toast.makeText(
                    this,
                    "Debes iniciar sesión",
                    Toast.LENGTH_SHORT
            ).show();


            return;
        }


        String tallerId =
                resena.getTallerId();


        String uid =
                user.getUid();


        if (tallerId == null ||
                tallerId.isEmpty()) {


            Toast.makeText(
                    this,
                    "No se pudo identificar la reseña",
                    Toast.LENGTH_SHORT
            ).show();


            return;
        }


        // --------------------------------------------------
        // ID DE LA RESEÑA
        // --------------------------------------------------
        //
        // Gou utiliza:
        //
        // tallerId_usuarioId
        //
        // como ID del documento.
        // --------------------------------------------------

        String resenaId =
                tallerId
                        + "_"
                        + uid;


        db.collection("resenas")
                .document(resenaId)
                .delete()

                .addOnSuccessListener(unused -> {


                    Toast.makeText(
                            this,
                            "Reseña eliminada correctamente",
                            Toast.LENGTH_SHORT
                    ).show();


                    // Actualizamos inmediatamente
                    // la pantalla.
                    cargarMisResenas();
                })

                .addOnFailureListener(e -> {


                    Toast.makeText(
                            this,
                            "No se pudo eliminar la reseña",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }
}