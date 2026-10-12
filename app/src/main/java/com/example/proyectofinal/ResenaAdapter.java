package com.example.proyectofinal;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ResenaAdapter
        extends RecyclerView.Adapter<ResenaAdapter.ResenaViewHolder> {

    private final List<Resena> lista;

    public ResenaAdapter(List<Resena> lista) {
        this.lista = lista;
    }


    @NonNull
    @Override
    public ResenaViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View vista = LayoutInflater
                .from(parent.getContext())
                .inflate(
                        R.layout.item_resena,
                        parent,
                        false
                );

        return new ResenaViewHolder(vista);
    }


    @Override
    public void onBindViewHolder(
            @NonNull ResenaViewHolder holder,
            int position
    ) {

        Resena resena = lista.get(position);

        // Nombre
        holder.txtUsuario.setText(
                resena.getUsuarioNombre()
        );

        // Estrellas
        holder.rating.setRating(
                (float) resena.getCalificacion()
        );

        // Comentario
        holder.txtComentario.setText(
                resena.getComentario()
        );

        // Fecha
        if (resena.getFecha() != null) {

            SimpleDateFormat formato =
                    new SimpleDateFormat(
                            "dd/MM/yyyy",
                            Locale.getDefault()
                    );

            String fecha = formato.format(
                    resena.getFecha().toDate()
            );

            holder.txtFecha.setText(fecha);

        } else {

            holder.txtFecha.setText("");
        }
    }


    @Override
    public int getItemCount() {
        return lista.size();
    }


    static class ResenaViewHolder
            extends RecyclerView.ViewHolder {

        TextView txtUsuario;
        TextView txtComentario;
        TextView txtFecha;

        RatingBar rating;

        public ResenaViewHolder(
                @NonNull View itemView
        ) {
            super(itemView);

            txtUsuario =
                    itemView.findViewById(
                            R.id.txtUsuario
                    );

            txtComentario =
                    itemView.findViewById(
                            R.id.txtComentario
                    );

            txtFecha =
                    itemView.findViewById(
                            R.id.txtFecha
                    );

            rating =
                    itemView.findViewById(
                            R.id.ratingResena
                    );
        }
    }
}