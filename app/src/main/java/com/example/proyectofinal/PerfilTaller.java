package com.example.proyectofinal;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class PerfilTaller extends AppCompatActivity {


    // ==================================================
    // DATOS DEL TALLER
    // ==================================================

    private TextView btnVolver;

    private TextView txtNombre;
    private TextView txtPropietario;
    private TextView txtDescripcion;
    private TextView txtHorario;
    private TextView txtTelefono;

    private ImageView imgTaller;


    // ==================================================
    // SERVICIOS
    // ==================================================

    private LinearLayout contenedorServicios;


    // ==================================================
    // RESEÑAS
    // ==================================================

    private TextView txtResumenResenas;

    private LinearLayout listaResenas;

    private Button btnCalificar;


    // ==================================================
    // FIREBASE
    // ==================================================

    private FirebaseFirestore db;
    private FirebaseAuth auth;


    // ==================================================
    // TALLER
    // ==================================================

    private String tallerId;
    private String nombreTaller;


    // ==================================================
    // ON CREATE
    // ==================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_perfil_taller
        );


        // --------------------------------------------------
        // COMPONENTES DEL TALLER
        // --------------------------------------------------

        btnVolver =
                findViewById(
                        R.id.btnVolver
                );


        txtNombre =
                findViewById(
                        R.id.txtNombre
                );


        txtPropietario =
                findViewById(
                        R.id.txtPropietario
                );


        txtDescripcion =
                findViewById(
                        R.id.txtDescripcion
                );


        txtHorario =
                findViewById(
                        R.id.txtHorario
                );


        txtTelefono =
                findViewById(
                        R.id.txtTelefono
                );


        imgTaller =
                findViewById(
                        R.id.imgTaller
                );


        // --------------------------------------------------
        // SERVICIOS
        // --------------------------------------------------

        contenedorServicios =
                findViewById(
                        R.id.contenedorServicios
                );


        // --------------------------------------------------
        // COMPONENTES DE RESEÑAS
        // --------------------------------------------------

        txtResumenResenas =
                findViewById(
                        R.id.txtResumenResenas
                );


        listaResenas =
                findViewById(
                        R.id.listaResenas
                );


        btnCalificar =
                findViewById(
                        R.id.btnCalificar
                );


        // --------------------------------------------------
        // FIREBASE
        // --------------------------------------------------

        db =
                FirebaseFirestore.getInstance();


        auth =
                FirebaseAuth.getInstance();


        // --------------------------------------------------
        // RECIBIR ID DEL TALLER
        // --------------------------------------------------

        tallerId =
                getIntent()
                        .getStringExtra(
                                "tallerId"
                        );


        // --------------------------------------------------
        // BOTÓN VOLVER
        // --------------------------------------------------

        btnVolver.setOnClickListener(v ->
                finish()
        );


        // --------------------------------------------------
        // CARGAR TALLER
        // --------------------------------------------------

        if (tallerId != null) {

            cargarTaller();

        } else {

            Toast.makeText(
                    this,
                    "No se pudo identificar el taller",
                    Toast.LENGTH_SHORT
            ).show();
        }


        // --------------------------------------------------
        // CALIFICAR
        // --------------------------------------------------

        btnCalificar.setOnClickListener(v ->
                abrirCalificacion()
        );


        actualizarBotonCalificar();
    }


    // ==================================================
    // CARGAR DATOS DEL TALLER
    // ==================================================

    private void cargarTaller() {

        db.collection("talleres")
                .document(tallerId)
                .get()

                .addOnSuccessListener(documento -> {


                    if (!documento.exists()) {

                        Toast.makeText(
                                this,
                                "El taller no existe",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }


                    // --------------------------------------------------
                    // LEER DATOS
                    // --------------------------------------------------

                    String nombre =
                            documento.getString(
                                    "nombre"
                            );


                    String propietario =
                            documento.getString(
                                    "propietarioNombre"
                            );


                    String descripcion =
                            documento.getString(
                                    "descripcion"
                            );


                    String fotoUrl =
                            documento.getString(
                                    "fotoUrl"
                            );


                    String telefono =
                            documento.getString(
                                    "telefono"
                            );


                    String horario =
                            documento.getString(
                                    "horario"
                            );


                    List<String> servicios =
                            (List<String>)
                                    documento.get(
                                            "servicios"
                                    );


                    // --------------------------------------------------
                    // FOTO PRINCIPAL
                    // --------------------------------------------------

                    if (fotoUrl != null &&
                            !fotoUrl.isEmpty()) {


                        Glide.with(
                                        PerfilTaller.this
                                )
                                .load(
                                        fotoUrl
                                )
                                .centerCrop()
                                .into(
                                        imgTaller
                                );
                    }


                    // --------------------------------------------------
                    // GUARDAR NOMBRE DEL TALLER
                    // --------------------------------------------------

                    nombreTaller =
                            nombre;


                    // --------------------------------------------------
                    // NOMBRE
                    // --------------------------------------------------

                    if (nombre != null) {

                        txtNombre.setText(
                                nombre
                        );
                    }


                    // --------------------------------------------------
                    // PROPIETARIO
                    // --------------------------------------------------

                    if (propietario != null &&
                            !propietario.isEmpty()) {


                        txtPropietario.setText(
                                propietario
                        );


                    } else {


                        txtPropietario.setText(
                                "No disponible"
                        );
                    }


                    // --------------------------------------------------
                    // DESCRIPCIÓN
                    // --------------------------------------------------

                    if (descripcion != null &&
                            !descripcion.isEmpty()) {


                        txtDescripcion.setText(
                                descripcion
                        );
                    }


                    // --------------------------------------------------
                    // TELÉFONO
                    // --------------------------------------------------

                    if (telefono != null &&
                            !telefono.isEmpty()) {


                        txtTelefono.setText(
                                "☎ " + telefono
                        );
                    }


                    // --------------------------------------------------
                    // HORARIO
                    // --------------------------------------------------

                    if (horario != null &&
                            !horario.isEmpty()) {


                        txtHorario.setText(
                                horario
                        );
                    }


                    // --------------------------------------------------
                    // SERVICIOS
                    // --------------------------------------------------

                    mostrarServicios(
                            servicios
                    );

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
    // MOSTRAR SERVICIOS COMO ETIQUETAS
    // ==================================================

    private void mostrarServicios(
            List<String> servicios
    ) {


        // Limpiamos por si la pantalla vuelve a cargar
        contenedorServicios.removeAllViews();


        // --------------------------------------------------
        // SIN SERVICIOS
        // --------------------------------------------------

        if (servicios == null ||
                servicios.isEmpty()) {


            TextView mensaje =
                    new TextView(this);


            mensaje.setText(
                    "Servicios no especificados"
            );


            mensaje.setTextColor(
                    Color.parseColor(
                            "#666666"
                    )
            );


            mensaje.setTextSize(
                    14
            );


            contenedorServicios.addView(
                    mensaje
            );


            return;
        }


        // --------------------------------------------------
        // CREAR UNA ETIQUETA POR SERVICIO
        // --------------------------------------------------

        for (String servicio : servicios) {


            if (servicio == null ||
                    servicio.trim().isEmpty()) {

                continue;
            }


            TextView etiqueta =
                    new TextView(this);


            // Texto del servicio
            etiqueta.setText(
                    servicio
            );


            // Azul Gou
            etiqueta.setTextColor(
                    Color.parseColor(
                            "#07366C"
                    )
            );


            etiqueta.setTextSize(
                    14
            );


            etiqueta.setTypeface(
                    null,
                    Typeface.BOLD
            );


            etiqueta.setGravity(
                    Gravity.CENTER_VERTICAL
            );


            // Fondo azul pálido
            etiqueta.setBackgroundResource(
                    R.drawable.fondo_servicio
            );


            // --------------------------------------------------
            // TAMAÑO Y SEPARACIÓN
            // --------------------------------------------------

            LinearLayout.LayoutParams parametros =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );


            parametros.setMargins(
                    0,
                    0,
                    0,
                    dpToPx(7)
            );


            etiqueta.setLayoutParams(
                    parametros
            );


            // --------------------------------------------------
            // AGREGAR AL CONTENEDOR
            // --------------------------------------------------

            contenedorServicios.addView(
                    etiqueta
            );
        }
    }


    // ==================================================
    // CONVERTIR DP A PIXELES
    // ==================================================

    private int dpToPx(
            int dp
    ) {

        return (int) (
                dp *
                        getResources()
                                .getDisplayMetrics()
                                .density
        );
    }


    // ==================================================
    // CARGAR TODAS LAS RESEÑAS
    // ==================================================

    private void cargarResenas() {


        if (tallerId == null) {

            return;
        }


        db.collection("resenas")
                .whereEqualTo(
                        "tallerId",
                        tallerId
                )
                .get()

                .addOnSuccessListener(resultado -> {


                    // --------------------------------------------------
                    // LIMPIAR RESEÑAS ANTERIORES
                    // --------------------------------------------------

                    listaResenas.removeAllViews();


                    double suma =
                            0;


                    int cantidad =
                            0;


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


                            cantidad++;


                            suma +=
                                    resena
                                            .getCalificacion();


                            agregarResena(
                                    resena
                            );
                        }
                    }


                    // --------------------------------------------------
                    // SIN RESEÑAS
                    // --------------------------------------------------

                    if (cantidad == 0) {


                        txtResumenResenas.setText(
                                "Este taller aún no tiene reseñas"
                        );


                        return;
                    }


                    // --------------------------------------------------
                    // PROMEDIO
                    // --------------------------------------------------

                    double promedio =
                            suma / cantidad;


                    String texto;


                    if (cantidad == 1) {


                        texto =
                                String.format(
                                        Locale.getDefault(),
                                        "★ %.1f · 1 reseña",
                                        promedio
                                );


                    } else {


                        texto =
                                String.format(
                                        Locale.getDefault(),
                                        "★ %.1f · %d reseñas",
                                        promedio,
                                        cantidad
                                );
                    }


                    txtResumenResenas.setText(
                            texto
                    );
                })

                .addOnFailureListener(e -> {


                    txtResumenResenas.setText(
                            "No se pudieron cargar las reseñas"
                    );
                });
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
                                listaResenas,
                                false
                        );


        TextView txtUsuario =
                vista.findViewById(
                        R.id.txtUsuario
                );


        TextView txtComentario =
                vista.findViewById(
                        R.id.txtComentario
                );


        TextView txtFecha =
                vista.findViewById(
                        R.id.txtFecha
                );


        RatingBar rating =
                vista.findViewById(
                        R.id.ratingResena
                );


        // --------------------------------------------------
        // NOMBRE
        // --------------------------------------------------

        txtUsuario.setText(
                resena.getUsuarioNombre()
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

        if (resena.getFecha() != null) {


            SimpleDateFormat formato =
                    new SimpleDateFormat(
                            "dd/MM/yyyy",
                            Locale.getDefault()
                    );


            String fecha =
                    formato.format(
                            resena
                                    .getFecha()
                                    .toDate()
                    );


            txtFecha.setText(
                    fecha
            );


        } else {


            txtFecha.setText(
                    ""
            );
        }


        // --------------------------------------------------
        // AGREGAR TARJETA
        // --------------------------------------------------

        listaResenas.addView(
                vista
        );
    }


    // ==================================================
    // ABRIR CALIFICACIÓN
    // ==================================================

    private void abrirCalificacion() {


        FirebaseUser user =
                auth.getCurrentUser();


        // --------------------------------------------------
        // VISITANTE
        // --------------------------------------------------

        if (user == null) {


            Toast.makeText(
                    this,
                    "Inicia sesión para calificar",
                    Toast.LENGTH_SHORT
            ).show();


            Intent intent =
                    new Intent(
                            PerfilTaller.this,
                            Login.class
                    );


            startActivity(
                    intent
            );


            return;
        }


        if (tallerId == null) {

            return;
        }


        String uid =
                user.getUid();


        // --------------------------------------------------
        // CONSULTAR TIPO DE USUARIO
        // --------------------------------------------------

        db.collection("usuarios")
                .document(uid)
                .get()

                .addOnSuccessListener(usuarioDoc -> {


                    if (!usuarioDoc.exists()) {


                        Toast.makeText(
                                this,
                                "No se encontró tu perfil",
                                Toast.LENGTH_SHORT
                        ).show();


                        return;
                    }


                    String tipo =
                            usuarioDoc.getString(
                                    "tipo"
                            );


                    // --------------------------------------------------
                    // SOLO CLIENTES PUEDEN CALIFICAR
                    // --------------------------------------------------

                    if (!"cliente".equals(tipo)) {


                        Toast.makeText(
                                this,
                                "Solo los clientes pueden calificar talleres",
                                Toast.LENGTH_SHORT
                        ).show();


                        return;
                    }


                    // --------------------------------------------------
                    // COMPROBAR SI YA CALIFICÓ
                    // --------------------------------------------------

                    String resenaId =
                            tallerId
                                    + "_"
                                    + uid;


                    db.collection("resenas")
                            .document(resenaId)
                            .get()

                            .addOnSuccessListener(resenaDoc -> {


                                if (resenaDoc.exists()) {


                                    Toast.makeText(
                                            this,
                                            "Ya calificaste este taller",
                                            Toast.LENGTH_SHORT
                                    ).show();


                                    return;
                                }


                                // --------------------------------------
                                // ABRIR CALIFICACIÓN
                                // --------------------------------------

                                Intent intent =
                                        new Intent(
                                                PerfilTaller.this,
                                                CalificarTaller.class
                                        );


                                intent.putExtra(
                                        "tallerId",
                                        tallerId
                                );


                                intent.putExtra(
                                        "tallerNombre",
                                        nombreTaller
                                );


                                startActivity(
                                        intent
                                );
                            })

                            .addOnFailureListener(e -> {


                                Toast.makeText(
                                        this,
                                        "No se pudo comprobar tu reseña",
                                        Toast.LENGTH_SHORT
                                ).show();
                            });

                })

                .addOnFailureListener(e -> {


                    Toast.makeText(
                            this,
                            "No se pudo comprobar el tipo de usuario",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }


    // ==================================================
    // ACTUALIZAR AL REGRESAR
    // ==================================================

    @Override
    protected void onResume() {

        super.onResume();


        if (tallerId != null &&
                db != null) {


            cargarResenas();


            actualizarBotonCalificar();
        }
    }


    // ==================================================
    // ACTUALIZAR BOTÓN CALIFICAR
    // ==================================================

    private void actualizarBotonCalificar() {


        FirebaseUser user =
                auth.getCurrentUser();


        // --------------------------------------------------
        // VISITANTE
        // --------------------------------------------------

        if (user == null) {


            btnCalificar.setVisibility(
                    View.VISIBLE
            );


            btnCalificar.setEnabled(
                    true
            );


            btnCalificar.setText(
                    "⭐ Calificar taller"
            );


            return;
        }


        String uid =
                user.getUid();


        // --------------------------------------------------
        // CONSULTAR TIPO DE USUARIO
        // --------------------------------------------------

        db.collection("usuarios")
                .document(uid)
                .get()

                .addOnSuccessListener(usuarioDoc -> {


                    String tipo =
                            usuarioDoc.getString(
                                    "tipo"
                            );


                    // --------------------------------------------------
                    // PROPIETARIO
                    // --------------------------------------------------

                    if (!"cliente".equals(tipo)) {


                        btnCalificar.setVisibility(
                                View.GONE
                        );


                        return;
                    }


                    // --------------------------------------------------
                    // CLIENTE
                    // --------------------------------------------------

                    btnCalificar.setVisibility(
                            View.VISIBLE
                    );


                    String resenaId =
                            tallerId
                                    + "_"
                                    + uid;


                    db.collection("resenas")
                            .document(resenaId)
                            .get()

                            .addOnSuccessListener(resenaDoc -> {


                                if (resenaDoc.exists()) {


                                    btnCalificar.setText(
                                            "✓ Ya calificaste este taller"
                                    );


                                    btnCalificar.setEnabled(
                                            false
                                    );


                                } else {


                                    btnCalificar.setText(
                                            "⭐ Calificar taller"
                                    );


                                    btnCalificar.setEnabled(
                                            true
                                    );
                                }
                            });
                });
    }
}