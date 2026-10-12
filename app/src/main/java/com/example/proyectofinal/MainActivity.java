package com.example.proyectofinal;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.cloudinary.android.MediaManager;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private Button btnBuscar;
    private Button btnOfrecer;
    private TextView txtLogin;

    private TextView txtAcercaDe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {

            Map<String, String> config = new HashMap<>();

            config.put(
                    "cloud_name",
                    "gtywsgo0"
            );

            config.put(
                    "secure",
                    "true"
            );

            MediaManager.init(
                    this,
                    config
            );

        } catch (IllegalStateException e) {

            // Cloudinary ya estaba inicializado
        }

        // Componentes
        btnBuscar = findViewById(R.id.btnBuscar);
        btnOfrecer = findViewById(R.id.btnOfrecer);
        txtLogin = findViewById(R.id.txtLogin);
        txtLogin.setPaintFlags(
                txtLogin.getPaintFlags()
                        | android.graphics.Paint.UNDERLINE_TEXT_FLAG
        );

        txtAcercaDe =
                findViewById(
                        R.id.txtAcercaDe
                );

        txtAcercaDe.setPaintFlags(
                txtAcercaDe.getPaintFlags()
                        | android.graphics.Paint.UNDERLINE_TEXT_FLAG
        );

        // Buscar talleres como visitante
        btnBuscar.setOnClickListener(v -> {

            Intent intent = new Intent(
                    MainActivity.this,
                    HomeCliente.class
            );

            startActivity(intent);
        });

        // Registro del propietario
        btnOfrecer.setOnClickListener(v -> {

            Intent intent = new Intent(
                    MainActivity.this,
                    RegistroPropietarioActivity.class
            );

            startActivity(intent);
        });

        txtLogin.setOnClickListener(v -> {

            Intent intent = new Intent(
                    MainActivity.this,
                    Login.class
            );

            startActivity(intent);
        });

        txtAcercaDe.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            MainActivity.this,
                            AcercaDeActivity.class
                    );

            startActivity(intent);
        });
    }
}