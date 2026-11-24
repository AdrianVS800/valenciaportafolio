package com.portafolio.model;

import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @Column(nullable = false, unique = true) public String email;
    @Column(nullable = false) public String passwordHash;

    public Usuario() { }
    public Usuario(String email, String passwordHash) { this.email = email; this.passwordHash = passwordHash; }
}
