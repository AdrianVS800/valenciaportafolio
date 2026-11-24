package com.portafolio.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "documentos")
public class Documento {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @Column(nullable = false, length = 120) public String titulo;
    @Column(nullable = false, length = 10) public String tipo;      // PDF, Word, Excel
    @Column(nullable = false) public String nombreArchivo;          // nombre guardado en uploads/
    @Column(nullable = false) public String nombreOriginal;
    public long tamanoBytes;
    public Integer semana;                                          // 1..16 (o null)
    @Column(length = 255) public String etiquetas = "";
    @Column(length = 2000) public String descripcion = "";
    public boolean borrador;                                        // true = oculto en el sitio público
    public LocalDate fecha = LocalDate.now();

    public String getTamanoLegible() {
        if (tamanoBytes < 1024) return tamanoBytes + " B";
        if (tamanoBytes < 1024 * 1024) return (tamanoBytes / 1024) + " KB";
        return String.format("%.1f MB", tamanoBytes / (1024.0 * 1024.0));
    }
    public String getClaseTipo() { return tipo == null ? "" : tipo.toLowerCase(); }
}
