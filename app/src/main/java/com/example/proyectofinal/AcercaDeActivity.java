package com.example.proyectofinal;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AcercaDeActivity extends AppCompatActivity {

    private TextView btnVolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_acerca_de
        );

        btnVolver =
                findViewById(
                        R.id.btnVolver
                );

        btnVolver.setOnClickListener(v ->
                finish()
        );
    }
}