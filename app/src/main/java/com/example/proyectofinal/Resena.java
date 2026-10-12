package com.example.proyectofinal;

import com.google.firebase.Timestamp;

public class Resena {

    private String tallerId;
    private String usuarioId;
    private String usuarioNombre;
    private double calificacion;
    private String comentario;
    private Timestamp fecha;

    // Firebase necesita constructor vacío
    public Resena() {
    }

    public String getTallerId() {
        return tallerId;
    }

    public void setTallerId(String tallerId) {
        this.tallerId = tallerId;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getUsuarioNombre() {
        return usuarioNombre;
    }

    public void setUsuarioNombre(String usuarioNombre) {
        this.usuarioNombre = usuarioNombre;
    }

    public double getCalificacion() {
        return calificacion;
    }

    public void setCalificacion(double calificacion) {
        this.calificacion = calificacion;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public Timestamp getFecha() {
        return fecha;
    }

    public void setFecha(Timestamp fecha) {
        this.fecha = fecha;
    }
}
