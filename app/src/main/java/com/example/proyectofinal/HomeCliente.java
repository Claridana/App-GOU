package com.example.proyectofinal;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeCliente extends AppCompatActivity {

    // COMPONENTES
    private Button btnUbicacion;
    private TextView txtUbicacion;
    private TextView txtHola;
    private TextView txtSinTalleres;
    private TextView navPerfil;
    private TextView navInicio;
    private TextView navBuscar;
    private EditText edtBuscar;
    private ScrollView scrollHome;
    // Ahora es LinearLayout, no RecyclerView
    private LinearLayout listaTalleres;
    private WebView webMapa;
    // FIREBASE
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    // UBICACIÓN
    private FusedLocationProviderClient ubicacion;
    // TALLERES
    private ArrayList<Taller> talleres;
    private static final float RADIO_BUSQUEDA_METROS = 20000f;
    private boolean filtroUbicacionActivo = false;
    private String nombreUbicacionBuscada = "";
    // MAPA
    private boolean mapaListo = false;

    // PERMISO DE UBICACIÓN
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
                                    "No se concedió permiso de ubicación",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
            );

    // ON CREATE
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(
                R.layout.activity_home_cliente
        );

        // COMPONENTES
        btnUbicacion =
                findViewById(
                        R.id.btnUbicacion
                );
        txtUbicacion =
                findViewById(
                        R.id.txtUbicacion
                );
        txtHola =
                findViewById(
                        R.id.txtHola
                );
        txtSinTalleres =
                findViewById(
                        R.id.txtSinTalleres
                );
        navPerfil =
                findViewById(
                        R.id.navPerfil
                );
        navInicio =
                findViewById(
                        R.id.navInicio
                );
        navBuscar =
                findViewById(
                        R.id.navBuscar
                );
        scrollHome =
                findViewById(
                        R.id.scrollHome
                );
        listaTalleres =
                findViewById(
                        R.id.listaTalleres
                );

        edtBuscar =
                findViewById(
                        R.id.edtBuscar
                );

        webMapa =
                findViewById(
                        R.id.webMapa
                );

        // GESTOS DEL MAPA
        webMapa.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                    v.getParent()
                            .requestDisallowInterceptTouchEvent(
                                    true
                            );

                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.getParent()
                            .requestDisallowInterceptTouchEvent(
                                    false
                            );
                    break;
            }
            return false;
        });

        // FIREBASE
        auth =
                FirebaseAuth.getInstance();
        db =
                FirebaseFirestore.getInstance();

        // UBICACIÓN
        ubicacion =
                LocationServices
                        .getFusedLocationProviderClient(
                                this
                        );
        // LISTA DE TALLERES
        talleres =
                new ArrayList<>();
        // MAPA LEAFLET
        configurarMapa();

        // --------------------------------------------------
        // BOTÓN USAR UBICACIÓN
        // --------------------------------------------------

        btnUbicacion.setOnClickListener(v ->
                revisarPermiso()
        );


        // --------------------------------------------------
        // PERFIL
        // --------------------------------------------------

        navPerfil.setOnClickListener(v -> {

            FirebaseUser user =
                    auth.getCurrentUser();


            // ------------------------------------------
            // VISITANTE
            // ------------------------------------------

            if (user == null) {

                Toast.makeText(
                        this,
                        "Inicia sesión para ver tu perfil",
                        Toast.LENGTH_SHORT
                ).show();


                Intent intent =
                        new Intent(
                                HomeCliente.this,
                                Login.class
                        );

                startActivity(intent);

                return;
            }


            // ------------------------------------------
            // USUARIO AUTENTICADO
            // ------------------------------------------

            String uid =
                    user.getUid();


            db.collection("usuarios")
                    .document(uid)
                    .get()

                    .addOnSuccessListener(documento -> {

                        if (!documento.exists()) {

                            Toast.makeText(
                                    this,
                                    "No se encontró el perfil",
                                    Toast.LENGTH_SHORT
                            ).show();

                            return;
                        }


                        String tipo =
                                documento.getString(
                                        "tipo"
                                );


                        // CLIENTE
                        if ("cliente".equals(tipo)) {

                            Intent intent =
                                    new Intent(
                                            HomeCliente.this,
                                            PerfilUsuario.class
                                    );

                            startActivity(intent);


                            // PROPIETARIO
                        } else if ("propietario".equals(tipo)) {

                            Boolean registroCompleto =
                                    documento.getBoolean(
                                            "registroCompleto"
                                    );


                            if (registroCompleto == null ||
                                    !registroCompleto) {


                                Toast.makeText(
                                        this,
                                        "Completa el registro de tu taller para continuar",
                                        Toast.LENGTH_LONG
                                ).show();


                                Intent intent =
                                        new Intent(
                                                HomeCliente.this,
                                                InformacionTallerActivity.class
                                        );


                                startActivity(intent);


                            } else {


                                Intent intent =
                                        new Intent(
                                                HomeCliente.this,
                                                HomePropietario.class
                                        );


                                startActivity(intent);
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
        });


        // --------------------------------------------------
        // INICIO
        // --------------------------------------------------

        navInicio.setOnClickListener(v -> {

            scrollHome.smoothScrollTo(
                    0,
                    0
            );
        });


        // --------------------------------------------------
        // BUSCAR
        // --------------------------------------------------

        navBuscar.setOnClickListener(v -> {

            scrollHome.smoothScrollTo(
                    0,
                    0
            );

            edtBuscar.requestFocus();
        });


        // --------------------------------------------------
        // TECLA BUSCAR
        // --------------------------------------------------

        edtBuscar.setOnEditorActionListener(
                (v, actionId, event) -> {

                    if (actionId ==
                            EditorInfo.IME_ACTION_SEARCH) {

                        buscarLugar();

                        return true;
                    }

                    return false;
                }
        );


        // --------------------------------------------------
        // CARGAR INFORMACIÓN
        // --------------------------------------------------

        mostrarNombre();

        cargarTalleres();
    }


    // ==================================================
    // CONFIGURAR MAPA
    // ==================================================

    private void configurarMapa() {

        WebSettings settings =
                webMapa.getSettings();


        settings.setJavaScriptEnabled(
                true
        );

        settings.setDomStorageEnabled(
                true
        );


        // JavaScript -> Android
        webMapa.addJavascriptInterface(
                new MapaInterface(),
                "Android"
        );


        webMapa.setWebViewClient(

                new WebViewClient() {

                    @Override
                    public void onPageFinished(
                            WebView view,
                            String url
                    ) {

                        super.onPageFinished(
                                view,
                                url
                        );


                        mapaListo = true;


                        // Si Firebase ya cargó los talleres,
                        // se mostrarán cuando el mapa termine.
                        mostrarTalleresEnMapa();
                    }
                }
        );


        webMapa.loadUrl(
                "file:///android_asset/mapa.html"
        );
    }


    // ==================================================
    // BUSCAR LUGAR
    // ==================================================

    private void buscarLugar() {

        String lugar =
                edtBuscar
                        .getText()
                        .toString()
                        .trim();


        if (lugar.isEmpty()) {

            edtBuscar.setError(
                    "Escribe una ubicación"
            );

            return;
        }


        txtUbicacion.setText(
                "📍 Buscando cerca de " + lugar + "..."
        );

        txtSinTalleres.setVisibility(
                View.GONE
        );


        Geocoder geocoder =
                new Geocoder(
                        this,
                        Locale.getDefault()
                );


        try {

            List<Address> resultados =
                    geocoder.getFromLocationName(
                            lugar + ", Sololá, Guatemala",
                            1
                    );


            if (resultados != null &&
                    !resultados.isEmpty()) {

                Address direccion =
                        resultados.get(0);

                double lat =
                        direccion.getLatitude();

                double lon =
                        direccion.getLongitude();


                filtroUbicacionActivo = true;
                nombreUbicacionBuscada = lugar;


                calcularDistancias(
                        lat,
                        lon
                );


                txtUbicacion.setText(
                        "📍 Ubicación: " + lugar
                );


                centrarMapa(
                        lat,
                        lon
                );


            } else {

                txtUbicacion.setText(
                        ""
                );

                txtSinTalleres.setVisibility(
                        View.GONE
                );

                Toast.makeText(
                        this,
                        "No se encontró la ubicación",
                        Toast.LENGTH_SHORT
                ).show();
            }


        } catch (IOException e) {

            txtUbicacion.setText(
                    ""
            );

            txtSinTalleres.setVisibility(
                    View.GONE
            );

            Toast.makeText(
                    this,
                    "No se pudo realizar la búsqueda",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }


    // ==================================================
    // CARGAR TALLERES
    // ==================================================

    private void cargarTalleres() {

        db.collection("talleres")

                .whereEqualTo(
                        "activo",
                        true
                )

                .get()

                .addOnSuccessListener(resultado -> {

                    talleres.clear();


                    resultado
                            .getDocuments()
                            .forEach(documento -> {

                                Taller taller =
                                        documento.toObject(
                                                Taller.class
                                        );


                                if (taller != null) {

                                    taller.setId(
                                            documento.getId()
                                    );

                                    talleres.add(
                                            taller
                                    );
                                }
                            });


                    // Mostrar talleres en mapa
                    mostrarTalleresEnMapa();


                    // Calcular reputación
                    cargarCalificacionesTalleres();


                    if (talleres.isEmpty()) {

                        listaTalleres.removeAllViews();

                        Toast.makeText(
                                this,
                                "No hay talleres registrados",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                })

                .addOnFailureListener(e -> {

                    Toast.makeText(
                            this,
                            "No se pudieron cargar los talleres",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }


    // ==================================================
    // CARGAR CALIFICACIONES
    // ==================================================

    private void cargarCalificacionesTalleres() {

        db.collection("resenas")

                .get()

                .addOnSuccessListener(resultado -> {


                    // Reiniciar reputación
                    for (Taller taller : talleres) {

                        taller.setPromedioCalificacion(
                                0.0
                        );

                        taller.setCantidadResenas(
                                0
                        );
                    }


                    // --------------------------------------------------
                    // RECORRER RESEÑAS
                    // --------------------------------------------------

                    for (DocumentSnapshot documento
                            : resultado.getDocuments()) {


                        String tallerId =
                                documento.getString(
                                        "tallerId"
                                );

                        Double calificacion =
                                documento.getDouble(
                                        "calificacion"
                                );


                        if (tallerId == null ||
                                calificacion == null) {

                            continue;
                        }


                        for (Taller taller : talleres) {

                            if (tallerId.equals(
                                    taller.getId()
                            )) {

                                double sumaAnterior =
                                        taller
                                                .getPromedioCalificacion();

                                int cantidadAnterior =
                                        taller
                                                .getCantidadResenas();


                                // Temporalmente usamos promedio
                                // como acumulador.
                                taller.setPromedioCalificacion(
                                        sumaAnterior
                                                + calificacion
                                );

                                taller.setCantidadResenas(
                                        cantidadAnterior + 1
                                );

                                break;
                            }
                        }
                    }

                    // CALCULAR PROMEDIOS
                    for (Taller taller : talleres) {
                        int cantidad =
                                taller.getCantidadResenas();
                        if (cantidad > 0) {
                            double suma =
                                    taller
                                            .getPromedioCalificacion();
                            double promedio =
                                    suma / cantidad;
                            taller.setPromedioCalificacion(
                                    promedio
                            );
                        }
                    }


                    // --------------------------------------------------
                    // ORDENAR POR CALIFICACIÓN
                    // --------------------------------------------------

                    talleres.sort((t1, t2) -> {

                        boolean t1TieneResenas =
                                t1.getCantidadResenas()
                                        > 0;

                        boolean t2TieneResenas =
                                t2.getCantidadResenas()
                                        > 0;


                        // Evaluado antes que sin reseñas
                        if (t1TieneResenas &&
                                !t2TieneResenas) {

                            return -1;
                        }


                        if (!t1TieneResenas &&
                                t2TieneResenas) {

                            return 1;
                        }


                        // Ninguno tiene reseñas
                        if (!t1TieneResenas) {

                            return 0;
                        }


                        // Mayor promedio primero
                        return Double.compare(

                                t2.getPromedioCalificacion(),

                                t1.getPromedioCalificacion()
                        );
                    });


                    // Dibujar TODAS las tarjetas
                    mostrarTalleresEnLista();
                })

                .addOnFailureListener(e -> {

                    // Si fallan las reseñas,
                    // los talleres igualmente aparecen.
                    mostrarTalleresEnLista();


                    Toast.makeText(
                            this,
                            "No se pudieron cargar las calificaciones",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }


    // ==================================================
    // MOSTRAR TALLERES EN LISTA
    // ==================================================

    private void mostrarTalleresEnLista() {

        // Limpiar tarjetas anteriores
        listaTalleres.removeAllViews();

        int cantidadMostrada = 0;


        for (Taller taller : talleres) {

            // Si hay una búsqueda/ubicación activa,
            // mostrar únicamente talleres dentro de 20 km.
            if (filtroUbicacionActivo &&
                    taller.getDistancia() > RADIO_BUSQUEDA_METROS) {

                continue;
            }

            cantidadMostrada++;


            View vista =
                    LayoutInflater
                            .from(this)
                            .inflate(
                                    R.layout.item_taller,
                                    listaTalleres,
                                    false
                            );


            // --------------------------------------------------
            // COMPONENTES DE LA TARJETA
            // --------------------------------------------------

            LinearLayout cardTaller =
                    vista.findViewById(
                            R.id.cardTaller
                    );

            ImageView imgTaller =
                    vista.findViewById(
                            R.id.imgTaller
                    );

            TextView txtNombre =
                    vista.findViewById(
                            R.id.txtNombre
                    );

            TextView txtServicios =
                    vista.findViewById(
                            R.id.txtServicios
                    );

            TextView txtCalificacion =
                    vista.findViewById(
                            R.id.txtCalificacion
                    );

            TextView txtHorario =
                    vista.findViewById(
                            R.id.txtHorario
                    );

            TextView txtDistancia =
                    vista.findViewById(
                            R.id.txtDistancia
                    );


            // --------------------------------------------------
            // FOTO
            // --------------------------------------------------

            if (taller.getFotoUrl() != null &&
                    !taller.getFotoUrl().isEmpty()) {


                Glide.with(this)

                        .load(
                                taller.getFotoUrl()
                        )

                        .centerCrop()

                        .into(
                                imgTaller
                        );


            } else {

                imgTaller.setImageResource(
                        R.drawable.ic_taller
                );
            }


            // --------------------------------------------------
            // NOMBRE
            // --------------------------------------------------

            if (taller.getNombre() != null) {

                txtNombre.setText(
                        taller.getNombre()
                );

            } else {

                txtNombre.setText(
                        "Taller"
                );
            }


            // --------------------------------------------------
            // SERVICIOS
            // --------------------------------------------------

            if (taller.getServicios() != null &&
                    !taller.getServicios().isEmpty()) {


                String servicios =
                        android.text.TextUtils.join(
                                " • ",
                                taller.getServicios()
                        );


                txtServicios.setText(
                        servicios
                );


            } else {

                txtServicios.setText(
                        "Servicios no especificados"
                );
            }


            // --------------------------------------------------
            // CALIFICACIÓN
            // --------------------------------------------------

            int cantidad =
                    taller.getCantidadResenas();


            if (cantidad > 0) {

                double promedio =
                        taller
                                .getPromedioCalificacion();


                String textoCalificacion;


                if (cantidad == 1) {

                    textoCalificacion =
                            String.format(
                                    Locale.getDefault(),
                                    "★ %.1f · 1 reseña",
                                    promedio
                            );

                } else {

                    textoCalificacion =
                            String.format(
                                    Locale.getDefault(),
                                    "★ %.1f · %d reseñas",
                                    promedio,
                                    cantidad
                            );
                }


                txtCalificacion.setText(
                        textoCalificacion
                );


            } else {

                txtCalificacion.setText(
                        "☆ Sin reseñas todavía"
                );
            }


            // --------------------------------------------------
            // HORARIO
            // --------------------------------------------------

            if (taller.getHorario() != null &&
                    !taller.getHorario().isEmpty()) {


                txtHorario.setText(
                        taller.getHorario()
                );


            } else {

                txtHorario.setText(
                        "Horario no disponible"
                );
            }


            // --------------------------------------------------
            // DISTANCIA
            // --------------------------------------------------

            if (taller.getDistancia() > 0) {


                float km =
                        taller.getDistancia()
                                / 1000;


                txtDistancia.setText(

                        String.format(
                                Locale.getDefault(),
                                "📍 %.1f km",
                                km
                        )
                );


            } else {

                txtDistancia.setText(
                        ""
                );
            }


            // --------------------------------------------------
            // ABRIR PERFIL
            // --------------------------------------------------

            cardTaller.setOnClickListener(v ->

                    abrirTaller(
                            taller
                    )
            );


            // --------------------------------------------------
            // AGREGAR TARJETA
            // --------------------------------------------------

            listaTalleres.addView(
                    vista
            );
        }


        // --------------------------------------------------
        // SIN RESULTADOS PARA LA UBICACIÓN
        // --------------------------------------------------

        if (filtroUbicacionActivo &&
                cantidadMostrada == 0) {

            String destino = nombreUbicacionBuscada;

            if (destino == null || destino.isEmpty()) {
                destino = "esta ubicación";
            }

            if ("tu ubicación".equals(destino)) {

                txtSinTalleres.setText(
                        "No hay talleres disponibles cerca de tu ubicación.\nIntenta buscar en otra zona."
                );

            } else {

                txtSinTalleres.setText(
                        "No hay talleres disponibles cerca de "
                                + destino
                                + ".\nIntenta buscar en otra ubicación."
                );
            }

            txtSinTalleres.setVisibility(
                    View.VISIBLE
            );

        } else {

            txtSinTalleres.setVisibility(
                    View.GONE
            );
        }
    }


    // ==================================================
    // MOSTRAR TALLERES EN LEAFLET
    // ==================================================

    private void mostrarTalleresEnMapa() {

        if (!mapaListo) {

            return;
        }


        // Limpiar marcadores anteriores
        webMapa.evaluateJavascript(
                "limpiarTalleres();",
                null
        );


        for (Taller taller : talleres) {


            String id =
                    escaparJavascript(
                            taller.getId()
                    );


            String nombre =
                    escaparJavascript(
                            taller.getNombre()
                    );


            String javascript =
                    "agregarTaller(" +

                            "'" + id + "'," +

                            "'" + nombre + "'," +

                            taller.getLatitud() + "," +

                            taller.getLongitud() +

                            ");";


            webMapa.evaluateJavascript(
                    javascript,
                    null
            );
        }
    }


    // ==================================================
    // ESCAPAR TEXTO PARA JAVASCRIPT
    // ==================================================

    private String escaparJavascript(
            String texto
    ) {

        if (texto == null) {

            return "";
        }


        return texto

                .replace(
                        "\\",
                        "\\\\"
                )

                .replace(
                        "'",
                        "\\'"
                )

                .replace(
                        "\n",
                        " "
                )

                .replace(
                        "\r",
                        " "
                );
    }


    // ==================================================
    // JAVASCRIPT -> ANDROID
    // ==================================================

    private class MapaInterface {


        @JavascriptInterface
        public void abrirTaller(
                String tallerId
        ) {


            runOnUiThread(() -> {


                Intent intent =
                        new Intent(
                                HomeCliente.this,
                                PerfilTaller.class
                        );


                intent.putExtra(
                        "tallerId",
                        tallerId
                );


                startActivity(
                        intent
                );
            });
        }
    }


    // ==================================================
    // ABRIR TALLER DESDE LISTA
    // ==================================================

    private void abrirTaller(
            Taller taller
    ) {


        Intent intent =
                new Intent(
                        HomeCliente.this,
                        PerfilTaller.class
                );


        intent.putExtra(
                "tallerId",
                taller.getId()
        );


        startActivity(
                intent
        );
    }


    // ==================================================
    // MOSTRAR NOMBRE
    // ==================================================

    private void mostrarNombre() {

        FirebaseUser user =
                auth.getCurrentUser();


        // Visitante
        if (user == null) {

            txtHola.setText(
                    "¡Hola!"
            );

            return;
        }


        String uid =
                user.getUid();


        db.collection("usuarios")
                .document(uid)
                .get()

                .addOnSuccessListener(documento -> {

                    if (documento.exists()) {

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

                        } else {

                            txtHola.setText(
                                    "¡Hola!"
                            );
                        }
                    }
                })

                .addOnFailureListener(e -> {

                    txtHola.setText(
                            "¡Hola!"
                    );
                });
    }


    // ==================================================
    // REVISAR PERMISO
    // ==================================================

    private void revisarPermiso() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED) {


            obtenerUbicacion();


        } else {

            permisoUbicacion.launch(
                    Manifest.permission
                            .ACCESS_FINE_LOCATION
            );
        }
    }


    // ==================================================
    // OBTENER UBICACIÓN ACTUAL
    // ==================================================

    private void obtenerUbicacion() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {

            return;
        }


        txtUbicacion.setText(
                "📍 Obteniendo ubicación..."
        );


        CancellationTokenSource tokenSource =
                new CancellationTokenSource();


        ubicacion.getCurrentLocation(

                Priority.PRIORITY_HIGH_ACCURACY,

                tokenSource.getToken()

        ).addOnSuccessListener(location -> {


            if (location != null) {


                double lat =
                        location.getLatitude();

                double lon =
                        location.getLongitude();


                txtUbicacion.setText(
                        "📍 Ubicación actual obtenida"
                );


                // Activar filtro por cercanía
                filtroUbicacionActivo = true;
                nombreUbicacionBuscada = "tu ubicación";

                // Calcular distancias
                calcularDistancias(
                        lat,
                        lon
                );


                // Mostrar punto azul
                mostrarUsuarioEnMapa(
                        lat,
                        lon
                );


            } else {

                Toast.makeText(
                        this,
                        "No se pudo obtener la ubicación actual",
                        Toast.LENGTH_SHORT
                ).show();
            }


        }).addOnFailureListener(e -> {


            Toast.makeText(
                    this,
                    "No se pudo obtener la ubicación",
                    Toast.LENGTH_SHORT
            ).show();
        });
    }


    // ==================================================
    // MOSTRAR USUARIO EN MAPA
    // ==================================================

    private void mostrarUsuarioEnMapa(
            double latitud,
            double longitud
    ) {

        if (!mapaListo) {

            return;
        }


        String javascript =
                "mostrarUbicacionUsuario(" +

                        latitud + "," +

                        longitud +

                        ");";


        webMapa.evaluateJavascript(
                javascript,
                null
        );
    }


    // ==================================================
    // CENTRAR MAPA
    // ==================================================

    private void centrarMapa(
            double latitud,
            double longitud
    ) {

        if (!mapaListo) {

            return;
        }


        String javascript =
                "map.setView([" +

                        latitud + "," +

                        longitud +

                        "],14);";


        webMapa.evaluateJavascript(
                javascript,
                null
        );
    }


    // ==================================================
    // CALCULAR DISTANCIAS
    // ==================================================

    private void calcularDistancias(
            double latUsuario,
            double lonUsuario
    ) {


        for (Taller taller : talleres) {


            float[] resultado =
                    new float[1];


            android.location.Location
                    .distanceBetween(

                            latUsuario,

                            lonUsuario,

                            taller.getLatitud(),

                            taller.getLongitud(),

                            resultado
                    );


            taller.setDistancia(
                    resultado[0]
            );
        }


        // Más cercano -> más lejano
        talleres.sort(

                (t1, t2) ->

                        Float.compare(

                                t1.getDistancia(),

                                t2.getDistancia()
                        )
        );


        // Volver a dibujar en el nuevo orden
        mostrarTalleresEnLista();
    }
}
