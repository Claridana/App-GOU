package com.example.proyectofinal;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import android.Manifest;
import android.content.pm.PackageManager;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;

public class PruebaLeaflet extends AppCompatActivity {

    private WebView webMapa;
    private FirebaseFirestore db;

    private FusedLocationProviderClient ubicacion;

    private Double latitudUsuario = null;
    private Double longitudUsuario = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_prueba_leaflet);

        webMapa = findViewById(R.id.webMapa);

        db = FirebaseFirestore.getInstance();

        ubicacion =
                LocationServices
                        .getFusedLocationProviderClient(this);

        // Configurar WebView
        WebSettings settings = webMapa.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);

        // Permitir comunicación JavaScript -> Android
        webMapa.addJavascriptInterface(
                new MapaInterface(),
                "Android"
        );

        // IMPORTANTE:
        // esperamos a que mapa.html termine de cargar
        webMapa.setWebViewClient(
                new WebViewClient() {

                    @Override
                    public void onPageFinished(
                            WebView view,
                            String url
                    ) {

                        super.onPageFinished(view, url);

                        // PRUEBA JAVA -> JAVASCRIPT

                        // Solo después cargamos Firestore
                        cargarTalleres();
                        revisarPermisoUbicacion();
                    }
                }
        );

        webMapa.loadUrl(
                "file:///android_asset/mapa.html"
        );

    }

    private final ActivityResultLauncher<String>
            permisoUbicacion =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    permitido -> {

                        if (permitido) {

                            obtenerUbicacionUsuario();

                        } else {

                            Toast.makeText(
                                    this,
                                    "Puedes usar el mapa sin compartir tu ubicación",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
            );

    private void revisarPermisoUbicacion() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED) {

            obtenerUbicacionUsuario();

        } else {

            permisoUbicacion.launch(
                    Manifest.permission.ACCESS_FINE_LOCATION
            );
        }
    }

    private void obtenerUbicacionUsuario() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {

            return;
        }


        CancellationTokenSource tokenSource =
                new CancellationTokenSource();


        ubicacion.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        tokenSource.getToken()
                )
                .addOnSuccessListener(location -> {

                    if (location != null) {

                        latitudUsuario =
                                location.getLatitude();

                        longitudUsuario =
                                location.getLongitude();


                        enviarUbicacionAlMapa();

                    } else {

                        Toast.makeText(
                                this,
                                "No se pudo obtener tu ubicación actual",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            this,
                            "No se pudo obtener tu ubicación",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }

    private void enviarUbicacionAlMapa() {

        if (latitudUsuario == null ||
                longitudUsuario == null) {

            return;
        }


        String javascript =
                "mostrarUbicacionUsuario(" +
                        latitudUsuario + "," +
                        longitudUsuario +
                        ");";


        webMapa.evaluateJavascript(
                javascript,
                null
        );
    }



    // ==================================================
    // CARGAR TALLERES REALES
    // ==================================================

    private void cargarTalleres() {

        db.collection("talleres")
                .whereEqualTo("activo", true)
                .get()
                .addOnSuccessListener(resultado -> {

                    // Esto nos ayudará a comprobar
                    // cuántos talleres encontró Firebase
                    Toast.makeText(
                            this,
                            "Talleres encontrados: "
                                    + resultado.size(),
                            Toast.LENGTH_LONG
                    ).show();


                    for (QueryDocumentSnapshot documento
                            : resultado) {

                        String id =
                                documento.getId();

                        String nombre =
                                documento.getString("nombre");

                        Double latitud =
                                documento.getDouble("latitud");

                        Double longitud =
                                documento.getDouble("longitud");



                        // Si algún taller no tiene coordenadas,
                        // mostramos un aviso para diagnosticar.
                        if (latitud == null ||
                                longitud == null) {

                            Toast.makeText(
                                    this,
                                    "El taller "
                                            + nombre
                                            + " no tiene coordenadas",
                                    Toast.LENGTH_SHORT
                            ).show();

                            continue;
                        }


                        if (nombre == null ||
                                nombre.isEmpty()) {

                            nombre = "Taller";
                        }
                        Toast.makeText(
                                this,
                                nombre
                                        + "\nLat: " + latitud
                                        + "\nLon: " + longitud,
                                Toast.LENGTH_LONG
                        ).show();

                        agregarTallerAlMapa(
                                id,
                                nombre,
                                latitud,
                                longitud
                        );


                    }

                })



                .addOnFailureListener(e -> {

                    Toast.makeText(
                            this,
                            "Error Firestore: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }


    // ==================================================
    // ENVIAR TALLER A LEAFLET
    // ==================================================

    private void agregarTallerAlMapa(
            String id,
            String nombre,
            double latitud,
            double longitud
    ) {

        String idSeguro =
                escaparJavascript(id);

        String nombreSeguro =
                escaparJavascript(nombre);


        String javascript =
                "agregarTaller(" +
                        "'" + idSeguro + "'," +
                        "'" + nombreSeguro + "'," +
                        latitud + "," +
                        longitud +
                        ");";


        webMapa.evaluateJavascript(
                javascript,
                null
        );
    }


    // ==================================================
    // EVITAR PROBLEMAS CON COMILLAS
    // ==================================================

    private String escaparJavascript(
            String texto
    ) {

        if (texto == null) {
            return "";
        }

        return texto
                .replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace("\n", " ")
                .replace("\r", " ");
    }


    // ==================================================
    // LEAFLET -> ANDROID
    // ==================================================

    private class MapaInterface {

        @JavascriptInterface
        public void abrirTaller(
                String tallerId
        ) {

            runOnUiThread(() -> {

                Intent intent = new Intent(
                        PruebaLeaflet.this,
                        PerfilTaller.class
                );

                intent.putExtra(
                        "tallerId",
                        tallerId
                );

                startActivity(intent);
            });
        }
    }
}