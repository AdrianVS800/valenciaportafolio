package com.portafolio.model;

import jakarta.persistence.*;

@Entity
@Table(name = "proyectos")
public class Proyecto {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @Column(nullable = false) public String titulo;
    @Column(length = 120) public String categoria;      // ej. "Spring Boot"
    @Column(length = 1000) public String descripcion;
    public String url;
    public String imagen;                                // archivo guardado en uploads/ (opcional)
}
