package com.example.proyectofinal;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;
import java.util.Locale;

public class TallerAdapter
        extends RecyclerView.Adapter<TallerAdapter.TallerViewHolder> {

    private final List<Taller> lista;
    private final OnTallerClick listener;


    // ==================================================
    // CONSTRUCTOR
    // ==================================================

    public TallerAdapter(
            List<Taller> lista,
            OnTallerClick listener
    ) {

        this.lista = lista;
        this.listener = listener;
    }


    // ==================================================
    // CLICK
    // ==================================================

    public interface OnTallerClick {

        void onClick(
                Taller taller
        );
    }


    // ==================================================
    // CREAR TARJETA
    // ==================================================

    @NonNull
    @Override
    public TallerViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View vista =
                LayoutInflater
                        .from(
                                parent.getContext()
                        )
                        .inflate(
                                R.layout.item_taller,
                                parent,
                                false
                        );


        return new TallerViewHolder(
                vista
        );
    }


    // ==================================================
    // MOSTRAR DATOS
    // ==================================================

    @Override
    public void onBindViewHolder(
            @NonNull TallerViewHolder holder,
            int position
    ) {

        Taller taller =
                lista.get(position);

        android.util.Log.d(
                "GOU_TALLER",
                "Dibujando posición "
                        + position
                        + " - "
                        + taller.getNombre()
        );


        // --------------------------------------------------
        // FOTO
        // --------------------------------------------------

        if (taller.getFotoUrl() != null &&
                !taller.getFotoUrl().isEmpty()) {


            Glide.with(
                            holder.itemView.getContext()
                    )
                    .load(
                            taller.getFotoUrl()
                    )
                    .centerCrop()
                    .into(
                            holder.imgTaller
                    );


        } else {

            holder.imgTaller.setImageResource(
                    R.drawable.ic_taller
            );
        }


        // --------------------------------------------------
        // NOMBRE
        // --------------------------------------------------

        holder.txtNombre.setText(
                taller.getNombre()
        );


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


            holder.txtServicios.setText(
                    servicios
            );


        } else {

            holder.txtServicios.setText(
                    "Servicios no especificados"
            );
        }


        // --------------------------------------------------
        // CALIFICACIÓN
        // --------------------------------------------------

        int cantidadResenas =
                taller.getCantidadResenas();


        if (cantidadResenas > 0) {

            double promedio =
                    taller.getPromedioCalificacion();


            String textoCalificacion;


            if (cantidadResenas == 1) {

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
                                cantidadResenas
                        );
            }


            holder.txtCalificacion.setText(
                    textoCalificacion
            );


        } else {

            holder.txtCalificacion.setText(
                    "☆ Sin reseñas todavía"
            );
        }


        // --------------------------------------------------
        // HORARIO
        // --------------------------------------------------

        if (taller.getHorario() != null &&
                !taller.getHorario().isEmpty()) {


            holder.txtHorario.setText(
                    taller.getHorario()
            );


        } else {

            holder.txtHorario.setText(
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


            holder.txtDistancia.setText(

                    String.format(
                            Locale.getDefault(),
                            "📍 %.1f km",
                            km
                    )
            );


        } else {

            holder.txtDistancia.setText(
                    ""
            );
        }


        // --------------------------------------------------
        // PULSAR TARJETA
        // --------------------------------------------------

        holder.cardTaller
                .setOnClickListener(v -> {

                    listener.onClick(
                            taller
                    );
                });
    }


    // ==================================================
    // CANTIDAD
    // ==================================================

    @Override
    public int getItemCount() {

        return lista.size();
    }


    // ==================================================
    // VIEW HOLDER
    // ==================================================

    static class TallerViewHolder
            extends RecyclerView.ViewHolder {


        LinearLayout cardTaller;

        TextView txtNombre;
        TextView txtServicios;
        TextView txtCalificacion;
        TextView txtHorario;
        TextView txtDistancia;

        ImageView imgTaller;


        public TallerViewHolder(
                @NonNull View itemView
        ) {

            super(itemView);


            cardTaller =
                    itemView.findViewById(
                            R.id.cardTaller
                    );


            txtNombre =
                    itemView.findViewById(
                            R.id.txtNombre
                    );


            txtServicios =
                    itemView.findViewById(
                            R.id.txtServicios
                    );


            txtCalificacion =
                    itemView.findViewById(
                            R.id.txtCalificacion
                    );


            txtHorario =
                    itemView.findViewById(
                            R.id.txtHorario
                    );


            txtDistancia =
                    itemView.findViewById(
                            R.id.txtDistancia
                    );


            imgTaller =
                    itemView.findViewById(
                            R.id.imgTaller
                    );
        }
    }
}