package com.portafolio.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

@Service
public class ArchivoService {

    public record Guardado(String nombre, String original) { }

    private final Path carpeta;

    public ArchivoService(@Value("${portafolio.carpeta-subidas}") String ruta) throws IOException {
        this.carpeta = Paths.get(ruta).toAbsolutePath().normalize();
        Files.createDirectories(carpeta);
    }

    /** Guarda el archivo con un nombre aleatorio, validando la extensión. */
    public Guardado guardar(MultipartFile f, Collection<String> extensiones) throws IOException {
        if (f == null || f.isEmpty()) throw new IllegalArgumentException("Selecciona un archivo.");
        String original = Paths.get(Objects.requireNonNullElse(f.getOriginalFilename(), "archivo")).getFileName().toString();
        String ext = original.contains(".") ? original.substring(original.lastIndexOf('.') + 1).toLowerCase() : "";
        if (!extensiones.contains(ext))
            throw new IllegalArgumentException("Archivo no válido. Extensiones permitidas: " + extensiones);
        String nombre = UUID.randomUUID() + "." + ext;
        Files.copy(f.getInputStream(), carpeta.resolve(nombre));
        return new Guardado(nombre, original);
    }

    public void borrar(String nombre) {
        if (nombre == null || nombre.isBlank()) return;
        try {
            Path p = carpeta.resolve(nombre).normalize();
            if (p.startsWith(carpeta)) Files.deleteIfExists(p);
        } catch (IOException ignorado) { }
    }

    public ResponseEntity<Resource> responder(String guardado, String original, boolean adjunto) throws IOException {
        Path p = carpeta.resolve(guardado).normalize();
        if (!p.startsWith(carpeta) || !Files.exists(p)) return ResponseEntity.notFound().build();
        Resource recurso = new UrlResource(p.toUri());
        MediaType tipo = MediaTypeFactory.getMediaType(recurso).orElse(MediaType.APPLICATION_OCTET_STREAM);
        ContentDisposition cd = (adjunto ? ContentDisposition.attachment() : ContentDisposition.inline())
                .filename(original, StandardCharsets.UTF_8).build();
        return ResponseEntity.ok().contentType(tipo).header(HttpHeaders.CONTENT_DISPOSITION, cd.toString()).body(recurso);
    }
}
