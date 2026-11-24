package com.portafolio.model;

import jakarta.persistence.*;

/** Textos editables del sitio (clave/valor): nombre, titular, correo, foto... */
@Entity
@Table(name = "configuracion")
public class Configuracion {
    @Id public String clave;
    @Column(length = 2000) public String valor;

    public Configuracion() { }
    public Configuracion(String clave, String valor) { this.clave = clave; this.valor = valor; }
}
