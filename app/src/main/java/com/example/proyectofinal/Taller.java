package com.example.proyectofinal;

import java.util.List;

public class Taller {

    private String id;
    private String nombre;
    private String telefono;
    private String descripcion;
    private String horario;
    private String propietarioId;
    private String propietarioNombre;
    private List<String> servicios;
    private boolean activo;
    private double latitud;
    private double longitud;
    // Esta NO viene de Firebase.
// La calcularemos según la ubicación del usuario.
    private float distancia;
    private String fotoUrl;

    // Estos datos NO vienen directamente del documento del taller.
// Se calculan usando la colección "resenas".
    private double promedioCalificacion = 0.0;
    private int cantidadResenas = 0;

    // Constructor vacío necesario para Firebase
    public Taller() {
    }

    // ID del documento
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // Nombre
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    // Teléfono
    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    // Descripción
    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    // Horario
    public String getHorario() {
        return horario;
    }

    public void setHorario(String horario) {
        this.horario = horario;
    }

    // Propietario ID
    public String getPropietarioId() {
        return propietarioId;
    }

    public void setPropietarioId(String propietarioId) {
        this.propietarioId = propietarioId;
    }

    // Nombre del propietario
    public String getPropietarioNombre() {
        return propietarioNombre;
    }

    public void setPropietarioNombre(String propietarioNombre) {
        this.propietarioNombre = propietarioNombre;
    }

    // Servicios
    public List<String> getServicios() {
        return servicios;
    }

    public void setServicios(List<String> servicios) {
        this.servicios = servicios;
    }

    // Estado
    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public double getLatitud() {
        return latitud;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }

    public float getDistancia() {
        return distancia;
    }

    public void setDistancia(float distancia) {
        this.distancia = distancia;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }
    public double getPromedioCalificacion() {
        return promedioCalificacion;
    }

    public void setPromedioCalificacion(double promedioCalificacion) {
        this.promedioCalificacion = promedioCalificacion;
    }

    public int getCantidadResenas() {
        return cantidadResenas;
    }

    public void setCantidadResenas(int cantidadResenas) {
        this.cantidadResenas = cantidadResenas;
    }
}
