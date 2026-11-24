package com.portafolio.model;

import jakarta.persistence.*;

@Entity
@Table(name = "semanas")
public class Semana {
    @Id public Integer numero;
    @Column(nullable = false) public String titulo;
    @Column(length = 1000) public String descripcion;

    public Semana() { }
    public Semana(Integer numero, String titulo, String descripcion) {
        this.numero = numero; this.titulo = titulo; this.descripcion = descripcion;
    }
}
