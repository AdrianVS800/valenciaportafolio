package com.portafolio.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** Mensaje enviado desde el formulario de contacto del sitio público. */
@Entity
@Table(name = "mensajes")
public class Mensaje {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @Column(nullable = false, length = 120) public String nombre;
    @Column(nullable = false, length = 150) public String email;
    @Column(length = 150) public String asunto;
    @Column(nullable = false, length = 2000) public String texto;
    public LocalDateTime fecha = LocalDateTime.now();
}
