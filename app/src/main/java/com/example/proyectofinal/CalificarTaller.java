package com.example.proyectofinal;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CalificarTaller extends AppCompatActivity {

    private TextView btnVolver;
    private TextView txtTaller;
    private TextView txtNota;

    private RatingBar rating;
    private EditText edtComentario;

    private Button btnPublicar;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private String tallerId;
    private String tallerNombre;
    private boolean modoEdicion = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calificar_taller);

        // Componentes
        btnVolver = findViewById(R.id.btnVolver);
        txtTaller = findViewById(R.id.txtTaller);
        txtNota = findViewById(R.id.txtNota);

        rating = findViewById(R.id.rating);
        edtComentario = findViewById(R.id.edtComentario);

        btnPublicar = findViewById(R.id.btnPublicar);

        // Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Datos del taller
        tallerId = getIntent().getStringExtra("tallerId");
        tallerNombre = getIntent().getStringExtra("tallerNombre");
        modoEdicion = getIntent().getBooleanExtra("modoEdicion", false);

        if (modoEdicion) {

            double calificacionAnterior =
                    getIntent().getDoubleExtra(
                            "calificacion",
                            0
                    );

            String comentarioAnterior =
                    getIntent().getStringExtra(
                            "comentario"
                    );


            rating.setRating(
                    (float) calificacionAnterior
            );


            if (comentarioAnterior != null) {

                edtComentario.setText(
                        comentarioAnterior
                );
            }


            txtNota.setText(
                    (int) calificacionAnterior
                            + " de 5 estrellas"
            );


            btnPublicar.setText(
                    "Guardar cambios"
            );
        }

        if (tallerNombre != null) {
            txtTaller.setText(tallerNombre);
        }

        // Volver
        btnVolver.setOnClickListener(v -> finish());

        // Estrellas
        rating.setOnRatingBarChangeListener(
                (ratingBar, valor, fromUser) -> {

                    int nota = (int) valor;

                    if (nota == 0) {

                        txtNota.setText(
                                "Selecciona una calificación"
                        );

                    } else {

                        txtNota.setText(
                                nota + " de 5 estrellas"
                        );
                    }
                }
        );

        // Publicar
        btnPublicar.setOnClickListener(v ->
                publicar()
        );
    }


    private void publicar() {

        float nota = rating.getRating();

        String comentario =
                edtComentario
                        .getText()
                        .toString()
                        .trim();


        // Validar estrellas
        if (nota == 0) {

            Toast.makeText(
                    this,
                    "Selecciona una calificación",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        // Validar comentario
        if (comentario.isEmpty()) {

            edtComentario.setError(
                    "Escribe un comentario"
            );

            edtComentario.requestFocus();

            return;
        }


        // Usuario actual
        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {

            Toast.makeText(
                    this,
                    "Debes iniciar sesión para calificar",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        if (tallerId == null) {

            Toast.makeText(
                    this,
                    "No se pudo identificar el taller",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        String uid = user.getUid();

        // Consultar perfil del usuario
        db.collection("usuarios")
                .document(uid)
                .get()
                .addOnSuccessListener(documento -> {

                    if (!documento.exists()) {

                        Toast.makeText(
                                this,
                                "No se encontró tu perfil",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }


                    String tipo =
                            documento.getString("tipo");

                    String nombre =
                            documento.getString("nombre");


                    // Solo clientes
                    if (!"cliente".equals(tipo)) {

                        Toast.makeText(
                                this,
                                "Solo los clientes pueden calificar talleres",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }


                    guardarResena(
                            uid,
                            nombre,
                            nota,
                            comentario
                    );
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            this,
                            "No se pudo consultar tu perfil",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }


    private void guardarResena(
            String uid,
            String nombre,
            float nota,
            String comentario
    ) {

        Map<String, Object> resena =
                new HashMap<>();

        resena.put("tallerId", tallerId);
        resena.put("usuarioId", uid);
        resena.put("usuarioNombre", nombre);
        resena.put("calificacion", nota);
        resena.put("comentario", comentario);
        resena.put("fecha", Timestamp.now());


        // Una reseña por usuario y taller
        String resenaId =
                tallerId + "_" + uid;


        db.collection("resenas")
                .document(resenaId)
                .set(resena)
                .addOnSuccessListener(unused -> {

                    if (modoEdicion) {

                        Toast.makeText(
                                this,
                                "Reseña actualizada",
                                Toast.LENGTH_SHORT
                        ).show();

                    } else {

                        Toast.makeText(
                                this,
                                "Reseña publicada",
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                    finish();
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            this,
                            "No se pudo publicar la reseña",
                            Toast.LENGTH_LONG
                    ).show();
                });
    }
}