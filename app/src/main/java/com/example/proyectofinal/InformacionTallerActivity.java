package com.example.proyectofinal;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InformacionTallerActivity extends AppCompatActivity {


    // ==================================================
    // COMPONENTES
    // ==================================================

    private EditText edtNombre;
    private EditText edtTelefono;
    private EditText edtDescripcion;
    private EditText edtHorario;


    // ==================================================
    // SERVICIOS PREDETERMINADOS
    // ==================================================

    private CheckBox chkMecanica;
    private CheckBox chkAceite;
    private CheckBox chkFrenos;
    private CheckBox chkMotor;
    private CheckBox chkElectricidad;
    private CheckBox chkLlantas;
    private CheckBox chkDiagnostico;


    // ==================================================
    // SERVICIOS PERSONALIZADOS
    // ==================================================

    private LinearLayout contenedorServiciosPersonalizados;
    private Button btnAgregarServicio;

    private static final int MAX_SERVICIOS_PERSONALIZADOS = 5;


    // ==================================================
    // BOTONES
    // ==================================================

    private Button btnGuardar;
    private Button btnUbicacion;
    private Button btnFotos;

    private TextView txtUbicacion;

    private ImageView imgFotoTaller;


    // ==================================================
    // FIREBASE
    // ==================================================

    private FirebaseAuth auth;
    private FirebaseFirestore db;


    // ==================================================
    // UBICACIÓN
    // ==================================================

    private FusedLocationProviderClient ubicacion;

    private Double latitud = null;
    private Double longitud = null;


    // ==================================================
    // TALLER
    // ==================================================

    private String tallerId;
    private boolean editando = false;


    // ==================================================
    // FOTOGRAFÍA
    // ==================================================

    private Uri fotoUri = null;

    private String fotoUrl = null;

    private boolean subiendoFoto = false;


    // ==================================================
    // ON CREATE
    // ==================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_informacion_taller
        );


        // --------------------------------------------------
        // CAMPOS
        // --------------------------------------------------

        edtNombre =
                findViewById(
                        R.id.edtNombreTaller
                );

        edtTelefono =
                findViewById(
                        R.id.edtTelefonoTaller
                );

        edtDescripcion =
                findViewById(
                        R.id.edtDescripcionTaller
                );

        edtHorario =
                findViewById(
                        R.id.edtHorario
                );


        // --------------------------------------------------
        // SERVICIOS PREDETERMINADOS
        // --------------------------------------------------

        chkMecanica =
                findViewById(
                        R.id.chkMecanicaGeneral
                );

        chkAceite =
                findViewById(
                        R.id.chkCambioAceite
                );

        chkFrenos =
                findViewById(
                        R.id.chkFrenos
                );

        chkMotor =
                findViewById(
                        R.id.chkMotor
                );

        chkElectricidad =
                findViewById(
                        R.id.chkElectricidad
                );

        chkLlantas =
                findViewById(
                        R.id.chkLlantas
                );

        chkDiagnostico =
                findViewById(
                        R.id.chkDiagnostico
                );


        // --------------------------------------------------
        // SERVICIOS PERSONALIZADOS
        // --------------------------------------------------

        contenedorServiciosPersonalizados =
                findViewById(
                        R.id.contenedorServiciosPersonalizados
                );

        btnAgregarServicio =
                findViewById(
                        R.id.btnAgregarServicio
                );


        // --------------------------------------------------
        // BOTONES Y OTROS COMPONENTES
        // --------------------------------------------------

        btnGuardar =
                findViewById(
                        R.id.btnFinalizarTaller
                );

        btnUbicacion =
                findViewById(
                        R.id.btnUbicacion
                );

        txtUbicacion =
                findViewById(
                        R.id.txtUbicacion
                );

        btnFotos =
                findViewById(
                        R.id.btnFotos
                );

        imgFotoTaller =
                findViewById(
                        R.id.imgFotoTaller
                );


        // --------------------------------------------------
        // FIREBASE
        // --------------------------------------------------

        auth =
                FirebaseAuth.getInstance();

        db =
                FirebaseFirestore.getInstance();


        // --------------------------------------------------
        // UBICACIÓN
        // --------------------------------------------------

        ubicacion =
                LocationServices
                        .getFusedLocationProviderClient(
                                this
                        );


        // --------------------------------------------------
        // ¿CREAR O EDITAR?
        // --------------------------------------------------

        tallerId =
                getIntent()
                        .getStringExtra(
                                "tallerId"
                        );


        if (tallerId != null) {

            editando = true;

            btnGuardar.setText(
                    "Guardar cambios"
            );

            cargarDatos();
        }


        // --------------------------------------------------
        // AGREGAR SERVICIO PERSONALIZADO
        // --------------------------------------------------

        btnAgregarServicio.setOnClickListener(v ->
                agregarCampoServicio("")
        );


        // --------------------------------------------------
        // BOTÓN UBICACIÓN
        // --------------------------------------------------

        btnUbicacion.setOnClickListener(
                v -> revisarPermiso()
        );


        // --------------------------------------------------
        // BOTÓN FOTOGRAFÍA
        // --------------------------------------------------

        btnFotos.setOnClickListener(v -> {

            selectorFoto.launch(

                    new PickVisualMediaRequest
                            .Builder()

                            .setMediaType(
                                    ActivityResultContracts
                                            .PickVisualMedia
                                            .ImageOnly.INSTANCE
                            )

                            .build()
            );
        });


        // --------------------------------------------------
        // BOTÓN GUARDAR
        // --------------------------------------------------

        btnGuardar.setOnClickListener(
                v -> guardarTaller()
        );
    }


    // ==================================================
    // AGREGAR SERVICIO PERSONALIZADO
    // ==================================================

    private void agregarCampoServicio(
            String servicioExistente
    ) {

        if (contenedorServiciosPersonalizados
                .getChildCount()
                >= MAX_SERVICIOS_PERSONALIZADOS) {

            Toast.makeText(
                    this,
                    "Puedes agregar hasta 5 servicios adicionales",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        // --------------------------------------------------
        // FILA
        // --------------------------------------------------

        LinearLayout fila =
                new LinearLayout(this);

        fila.setOrientation(
                LinearLayout.HORIZONTAL
        );

        fila.setGravity(
                Gravity.CENTER_VERTICAL
        );


        LinearLayout.LayoutParams parametrosFila =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        parametrosFila.setMargins(
                0,
                dpToPx(5),
                0,
                dpToPx(5)
        );

        fila.setLayoutParams(
                parametrosFila
        );


        // --------------------------------------------------
        // CAMPO DE TEXTO
        // --------------------------------------------------

        EditText edtServicio =
                new EditText(this);

        edtServicio.setHint(
                "Ej. Alineación y balanceo"
        );

        edtServicio.setSingleLine(
                true
        );

        edtServicio.setText(
                servicioExistente
        );


        LinearLayout.LayoutParams parametrosCampo =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                );

        edtServicio.setLayoutParams(
                parametrosCampo
        );


        // --------------------------------------------------
        // BOTÓN ELIMINAR
        // --------------------------------------------------

        Button btnEliminar =
                new Button(this);

        btnEliminar.setText(
                "✕"
        );

        btnEliminar.setTextSize(
                16
        );

        btnEliminar.setContentDescription(
                "Eliminar servicio"
        );


        LinearLayout.LayoutParams parametrosBoton =
                new LinearLayout.LayoutParams(
                        dpToPx(50),
                        dpToPx(48)
                );

        parametrosBoton.setMargins(
                dpToPx(8),
                0,
                0,
                0
        );

        btnEliminar.setLayoutParams(
                parametrosBoton
        );


        btnEliminar.setOnClickListener(v ->
                contenedorServiciosPersonalizados
                        .removeView(fila)
        );


        // --------------------------------------------------
        // AGREGAR ELEMENTOS
        // --------------------------------------------------

        fila.addView(
                edtServicio
        );

        fila.addView(
                btnEliminar
        );


        contenedorServiciosPersonalizados
                .addView(
                        fila
                );
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
    // SELECTOR DE FOTOGRAFÍA
    // ==================================================

    private final ActivityResultLauncher<PickVisualMediaRequest>
            selectorFoto =

            registerForActivityResult(

                    new ActivityResultContracts
                            .PickVisualMedia(),

                    uri -> {

                        if (uri != null) {

                            fotoUri = uri;

                            imgFotoTaller.setVisibility(
                                    View.VISIBLE
                            );

                            imgFotoTaller.setImageURI(
                                    uri
                            );

                        } else {

                            Toast.makeText(
                                    this,
                                    "No seleccionaste ninguna imagen",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
            );


    // ==================================================
    // PERMISO DE UBICACIÓN
    // ==================================================

    private final ActivityResultLauncher<String>
            permisoUbicacion =

            registerForActivityResult(

                    new ActivityResultContracts
                            .RequestPermission(),

                    permitido -> {

                        if (permitido) {

                            obtenerUbicacion();

                        } else {

                            Toast.makeText(
                                    this,
                                    "Se necesita permiso de ubicación",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
            );


    // ==================================================
    // CARGAR TALLER EXISTENTE
    // ==================================================

    private void cargarDatos() {

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


                    String nombre =
                            documento.getString(
                                    "nombre"
                            );

                    String telefono =
                            documento.getString(
                                    "telefono"
                            );

                    String descripcion =
                            documento.getString(
                                    "descripcion"
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


                    Double lat =
                            documento.getDouble(
                                    "latitud"
                            );

                    Double lon =
                            documento.getDouble(
                                    "longitud"
                            );


                    String fotoExistente =
                            documento.getString(
                                    "fotoUrl"
                            );


                    // --------------------------------------------------
                    // FOTO EXISTENTE
                    // --------------------------------------------------

                    if (fotoExistente != null &&
                            !fotoExistente.isEmpty()) {

                        fotoUrl =
                                fotoExistente;

                        imgFotoTaller.setVisibility(
                                View.VISIBLE
                        );

                        Glide.with(this)
                                .load(fotoExistente)
                                .centerCrop()
                                .into(imgFotoTaller);
                    }


                    // --------------------------------------------------
                    // DATOS GENERALES
                    // --------------------------------------------------

                    if (nombre != null) {

                        edtNombre.setText(
                                nombre
                        );
                    }


                    if (telefono != null) {

                        edtTelefono.setText(
                                telefono
                        );
                    }


                    if (descripcion != null) {

                        edtDescripcion.setText(
                                descripcion
                        );
                    }


                    if (horario != null) {

                        edtHorario.setText(
                                horario
                        );
                    }


                    // --------------------------------------------------
                    // SERVICIOS
                    // --------------------------------------------------

                    if (servicios != null) {


                        chkMecanica.setChecked(
                                servicios.contains(
                                        "Mecánica general"
                                )
                        );


                        chkAceite.setChecked(
                                servicios.contains(
                                        "Cambio de aceite"
                                )
                        );


                        chkFrenos.setChecked(
                                servicios.contains(
                                        "Frenos"
                                )
                        );


                        chkMotor.setChecked(
                                servicios.contains(
                                        "Motor"
                                )
                        );


                        chkElectricidad.setChecked(
                                servicios.contains(
                                        "Electricidad automotriz"
                                )
                        );


                        chkLlantas.setChecked(
                                servicios.contains(
                                        "Llantas"
                                )
                        );


                        chkDiagnostico.setChecked(
                                servicios.contains(
                                        "Diagnóstico"
                                )
                        );


                        // ----------------------------------------------
                        // SERVICIOS PERSONALIZADOS
                        // ----------------------------------------------

                        contenedorServiciosPersonalizados
                                .removeAllViews();


                        List<String> serviciosPredeterminados =
                                obtenerListaServiciosPredeterminados();


                        for (String servicio : servicios) {

                            if (!serviciosPredeterminados
                                    .contains(servicio)) {

                                agregarCampoServicio(
                                        servicio
                                );
                            }
                        }
                    }


                    // --------------------------------------------------
                    // UBICACIÓN
                    // --------------------------------------------------

                    if (lat != null &&
                            lon != null) {

                        latitud = lat;
                        longitud = lon;

                        txtUbicacion.setText(
                                "📍 Ubicación configurada ✓"
                        );
                    }

                })

                .addOnFailureListener(e -> {

                    Toast.makeText(
                            this,
                            "No se pudieron cargar los datos",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }


    // ==================================================
    // LISTA DE SERVICIOS PREDETERMINADOS
    // ==================================================

    private List<String> obtenerListaServiciosPredeterminados() {

        List<String> servicios =
                new ArrayList<>();


        servicios.add(
                "Mecánica general"
        );

        servicios.add(
                "Cambio de aceite"
        );

        servicios.add(
                "Frenos"
        );

        servicios.add(
                "Motor"
        );

        servicios.add(
                "Electricidad automotriz"
        );

        servicios.add(
                "Llantas"
        );

        servicios.add(
                "Diagnóstico"
        );


        return servicios;
    }


    // ==================================================
    // REVISAR PERMISO
    // ==================================================

    private void revisarPermiso() {

        if (ActivityCompat
                .checkSelfPermission(
                        this,
                        Manifest.permission
                                .ACCESS_FINE_LOCATION
                )
                ==
                PackageManager.PERMISSION_GRANTED) {

            obtenerUbicacion();

        } else {

            permisoUbicacion.launch(
                    Manifest.permission
                            .ACCESS_FINE_LOCATION
            );
        }
    }


    // ==================================================
    // OBTENER UBICACIÓN
    // ==================================================

    private void obtenerUbicacion() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {

            return;
        }


        txtUbicacion.setText(
                "📍 Obteniendo ubicación actual..."
        );


        CancellationTokenSource tokenSource =
                new CancellationTokenSource();


        ubicacion.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        tokenSource.getToken()
                )

                .addOnSuccessListener(location -> {

                    if (location != null) {

                        latitud =
                                location.getLatitude();

                        longitud =
                                location.getLongitude();


                        txtUbicacion.setText(
                                "📍 Ubicación configurada ✓"
                        );

                    } else {

                        txtUbicacion.setText(
                                "Ubicación no configurada"
                        );


                        Toast.makeText(
                                this,
                                "No se pudo obtener la ubicación actual",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                })

                .addOnFailureListener(e -> {

                    txtUbicacion.setText(
                            "Ubicación no configurada"
                    );


                    Toast.makeText(
                            this,
                            "Error al obtener ubicación: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }


    // ==================================================
    // GUARDAR TALLER
    // ==================================================

    private void guardarTaller() {


        // --------------------------------------------------
        // LEER CAMPOS
        // --------------------------------------------------

        String nombre =
                edtNombre
                        .getText()
                        .toString()
                        .trim();


        String telefono =
                edtTelefono
                        .getText()
                        .toString()
                        .trim();


        String descripcion =
                edtDescripcion
                        .getText()
                        .toString()
                        .trim();


        String horario =
                edtHorario
                        .getText()
                        .toString()
                        .trim();


        // --------------------------------------------------
        // VALIDAR CAMPOS
        // --------------------------------------------------

        if (nombre.isEmpty() ||
                telefono.isEmpty() ||
                descripcion.isEmpty() ||
                horario.isEmpty()) {

            Toast.makeText(
                    this,
                    "Completa todos los campos",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        // --------------------------------------------------
        // SERVICIOS
        // --------------------------------------------------

        List<String> servicios =
                obtenerServicios();


        if (servicios.isEmpty()) {

            Toast.makeText(
                    this,
                    "Selecciona o agrega al menos un servicio",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        // --------------------------------------------------
        // UBICACIÓN
        // --------------------------------------------------

        if (latitud == null ||
                longitud == null) {

            Toast.makeText(
                    this,
                    "Configura la ubicación del taller",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        // --------------------------------------------------
        // USUARIO
        // --------------------------------------------------

        FirebaseUser user =
                auth.getCurrentUser();


        if (user == null) {

            Toast.makeText(
                    this,
                    "No hay un propietario autenticado",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        // --------------------------------------------------
        // ¿HAY UNA FOTO NUEVA?
        // --------------------------------------------------

        if (fotoUri != null) {

            subirFoto(
                    user,
                    nombre,
                    telefono,
                    descripcion,
                    horario,
                    servicios
            );

        } else {

            continuarGuardado(
                    user,
                    nombre,
                    telefono,
                    descripcion,
                    horario,
                    servicios
            );
        }
    }


    // ==================================================
    // SUBIR FOTO A CLOUDINARY
    // ==================================================

    private void subirFoto(
            FirebaseUser user,
            String nombre,
            String telefono,
            String descripcion,
            String horario,
            List<String> servicios
    ) {

        if (fotoUri == null) {

            return;
        }


        if (subiendoFoto) {

            return;
        }


        subiendoFoto = true;


        btnGuardar.setEnabled(
                false
        );


        btnGuardar.setText(
                "Subiendo fotografía..."
        );


        MediaManager
                .get()

                .upload(fotoUri)

                .unsigned(
                        "gou_talleres"
                )

                .option(
                        "resource_type",
                        "image"
                )

                .callback(
                        new UploadCallback() {

                            @Override
                            public void onStart(
                                    String requestId
                            ) {
                            }


                            @Override
                            public void onProgress(
                                    String requestId,
                                    long bytes,
                                    long totalBytes
                            ) {
                            }


                            @Override
                            public void onSuccess(
                                    String requestId,
                                    Map resultData
                            ) {

                                subiendoFoto =
                                        false;


                                Object url =
                                        resultData.get(
                                                "secure_url"
                                        );


                                if (url == null) {

                                    restaurarBotonGuardar();


                                    Toast.makeText(
                                            InformacionTallerActivity.this,
                                            "Cloudinary no devolvió la URL de la fotografía",
                                            Toast.LENGTH_LONG
                                    ).show();

                                    return;
                                }


                                fotoUrl =
                                        url.toString();


                                continuarGuardado(
                                        user,
                                        nombre,
                                        telefono,
                                        descripcion,
                                        horario,
                                        servicios
                                );
                            }


                            @Override
                            public void onError(
                                    String requestId,
                                    ErrorInfo error
                            ) {

                                subiendoFoto =
                                        false;


                                restaurarBotonGuardar();


                                Toast.makeText(
                                        InformacionTallerActivity.this,
                                        "No se pudo subir la fotografía: "
                                                + error.getDescription(),
                                        Toast.LENGTH_LONG
                                ).show();
                            }


                            @Override
                            public void onReschedule(
                                    String requestId,
                                    ErrorInfo error
                            ) {
                            }
                        }
                )

                .dispatch();
    }


    // ==================================================
    // CONTINUAR GUARDADO
    // ==================================================

    private void continuarGuardado(
            FirebaseUser user,
            String nombre,
            String telefono,
            String descripcion,
            String horario,
            List<String> servicios
    ) {

        String uid =
                user.getUid();


        db.collection("usuarios")
                .document(uid)
                .get()

                .addOnSuccessListener(documento -> {

                    if (documento.exists()) {

                        String propietario =
                                documento.getString(
                                        "nombre"
                                );


                        guardarDatos(
                                uid,
                                propietario,
                                nombre,
                                telefono,
                                descripcion,
                                horario,
                                servicios,
                                latitud,
                                longitud
                        );

                    } else {

                        restaurarBotonGuardar();


                        Toast.makeText(
                                this,
                                "No se encontró el propietario",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                })

                .addOnFailureListener(e -> {

                    restaurarBotonGuardar();


                    Toast.makeText(
                            this,
                            "No se pudo obtener el propietario",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }


    // ==================================================
    // GUARDAR DATOS EN FIRESTORE
    // ==================================================

    private void guardarDatos(
            String uid,
            String propietario,
            String nombre,
            String telefono,
            String descripcion,
            String horario,
            List<String> servicios,
            Double latitud,
            Double longitud
    ) {

        Map<String, Object> taller =
                new HashMap<>();


        taller.put(
                "nombre",
                nombre
        );

        taller.put(
                "telefono",
                telefono
        );

        taller.put(
                "descripcion",
                descripcion
        );

        taller.put(
                "horario",
                horario
        );

        taller.put(
                "servicios",
                servicios
        );

        taller.put(
                "propietarioId",
                uid
        );

        taller.put(
                "propietarioNombre",
                propietario
        );

        taller.put(
                "activo",
                true
        );

        taller.put(
                "latitud",
                latitud
        );

        taller.put(
                "longitud",
                longitud
        );


        // --------------------------------------------------
        // FOTO
        // --------------------------------------------------

        if (fotoUrl != null &&
                !fotoUrl.isEmpty()) {

            taller.put(
                    "fotoUrl",
                    fotoUrl
            );
        }


        // --------------------------------------------------
        // EDITAR
        // --------------------------------------------------

        if (editando) {

            db.collection("talleres")
                    .document(tallerId)
                    .update(taller)

                    .addOnSuccessListener(unused -> {

                        Toast.makeText(
                                this,
                                "Taller actualizado correctamente",
                                Toast.LENGTH_SHORT
                        ).show();


                        finish();
                    })

                    .addOnFailureListener(e -> {

                        restaurarBotonGuardar();


                        Toast.makeText(
                                this,
                                "No se pudo actualizar el taller",
                                Toast.LENGTH_LONG
                        ).show();
                    });


            // --------------------------------------------------
            // CREAR
            // --------------------------------------------------

        } else {

            db.collection("talleres")
                    .add(taller)

                    .addOnSuccessListener(documento -> {


                        // ----------------------------------
                        // REGISTRO COMPLETO
                        // ----------------------------------

                        db.collection("usuarios")
                                .document(uid)
                                .update(
                                        "registroCompleto",
                                        true
                                )

                                .addOnSuccessListener(unused -> {

                                    Toast.makeText(
                                            this,
                                            "Taller registrado correctamente",
                                            Toast.LENGTH_SHORT
                                    ).show();


                                    Intent intent =
                                            new Intent(
                                                    InformacionTallerActivity.this,
                                                    HomePropietario.class
                                            );


                                    intent.setFlags(
                                            Intent.FLAG_ACTIVITY_NEW_TASK |
                                                    Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    );


                                    startActivity(
                                            intent
                                    );

                                    finish();
                                })

                                .addOnFailureListener(e -> {

                                    restaurarBotonGuardar();


                                    Toast.makeText(
                                            this,
                                            "El taller fue creado, pero no se pudo completar el registro",
                                            Toast.LENGTH_LONG
                                    ).show();
                                });
                    })

                    .addOnFailureListener(e -> {

                        restaurarBotonGuardar();


                        Toast.makeText(
                                this,
                                "No se pudo guardar el taller",
                                Toast.LENGTH_LONG
                        ).show();
                    });
        }
    }


    // ==================================================
    // RESTAURAR BOTÓN
    // ==================================================

    private void restaurarBotonGuardar() {

        btnGuardar.setEnabled(
                true
        );


        if (editando) {

            btnGuardar.setText(
                    "Guardar cambios"
            );

        } else {

            btnGuardar.setText(
                    "Guardar taller"
            );
        }
    }


    // ==================================================
    // OBTENER TODOS LOS SERVICIOS
    // ==================================================

    private List<String> obtenerServicios() {

        List<String> servicios =
                new ArrayList<>();


        // --------------------------------------------------
        // PREDETERMINADOS
        // --------------------------------------------------

        if (chkMecanica.isChecked()) {

            servicios.add(
                    "Mecánica general"
            );
        }


        if (chkAceite.isChecked()) {

            servicios.add(
                    "Cambio de aceite"
            );
        }


        if (chkFrenos.isChecked()) {

            servicios.add(
                    "Frenos"
            );
        }


        if (chkMotor.isChecked()) {

            servicios.add(
                    "Motor"
            );
        }


        if (chkElectricidad.isChecked()) {

            servicios.add(
                    "Electricidad automotriz"
            );
        }


        if (chkLlantas.isChecked()) {

            servicios.add(
                    "Llantas"
            );
        }


        if (chkDiagnostico.isChecked()) {

            servicios.add(
                    "Diagnóstico"
            );
        }


        // --------------------------------------------------
        // PERSONALIZADOS
        // --------------------------------------------------

        for (
                int i = 0;
                i < contenedorServiciosPersonalizados
                        .getChildCount();
                i++
        ) {

            View fila =
                    contenedorServiciosPersonalizados
                            .getChildAt(i);


            if (fila instanceof LinearLayout) {

                LinearLayout filaServicio =
                        (LinearLayout) fila;


                if (filaServicio.getChildCount() > 0 &&
                        filaServicio.getChildAt(0)
                                instanceof EditText) {


                    EditText edtServicio =
                            (EditText)
                                    filaServicio
                                            .getChildAt(0);


                    String servicio =
                            edtServicio
                                    .getText()
                                    .toString()
                                    .trim();


                    if (!servicio.isEmpty() &&
                            !servicios.contains(servicio)) {

                        servicios.add(
                                servicio
                        );
                    }
                }
            }
        }


        return servicios;
    }
}