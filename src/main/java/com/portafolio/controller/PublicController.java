package com.portafolio.controller;

import com.portafolio.model.*;
import com.portafolio.repository.*;
import com.portafolio.service.ArchivoService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/** Sitio público: lo que ven los visitantes. */
@Controller
public class PublicController {

    private final SemanaRepository semanas;
    private final DocumentoRepository docs;
    private final ProyectoRepository proyectos;
    private final MensajeRepository mensajes;
    private final ConfiguracionRepository config;
    private final ArchivoService archivos;

    public PublicController(SemanaRepository semanas, DocumentoRepository docs, ProyectoRepository proyectos,
                            MensajeRepository mensajes, ConfiguracionRepository config, ArchivoService archivos) {
        this.semanas = semanas; this.docs = docs; this.proyectos = proyectos;
        this.mensajes = mensajes; this.config = config; this.archivos = archivos;
    }

    private String cfg(String clave) { return config.findById(clave).map(c -> c.valor == null ? "" : c.valor).orElse(""); }

    private List<String> lista(String clave) {
        return Arrays.stream(cfg(clave).split(",")).map(String::trim).filter(x -> !x.isEmpty()).toList();
    }

    @GetMapping("/")
    public String inicio(Model m) {
        m.addAttribute("semanas", semanas.findAllByOrderByNumeroAsc());
        m.addAttribute("proyectos", proyectos.findAllByOrderByIdDesc());
        m.addAttribute("backend", lista("hab_backend"));
        m.addAttribute("frontend", lista("hab_frontend"));
        m.addAttribute("tools", lista("hab_tools"));
        return "index";
    }

    @GetMapping("/trabajos")
    public String trabajos(Model m) {
        m.addAttribute("documentos", docs.findByBorradorFalseOrderByIdDesc());
        return "trabajos";
    }

    @GetMapping("/semana/{n}")
    public String semana(@PathVariable int n, Model m) {
        Semana s = semanas.findById(n).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        m.addAttribute("semana", s);
        m.addAttribute("documentos", docs.findBySemanaAndBorradorFalseOrderByIdDesc(n));
        return "semana";
    }

    @PostMapping("/contacto")
    public String contacto(@RequestParam String nombre, @RequestParam String email,
                           @RequestParam(defaultValue = "") String asunto, @RequestParam String mensaje,
                           RedirectAttributes fl) {
        nombre = nombre.trim(); email = email.trim(); asunto = asunto.trim(); mensaje = mensaje.trim();
        if (nombre.isEmpty() || mensaje.isEmpty() || !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            fl.addFlashAttribute("errorContacto", "Completa tu nombre, un correo válido y tu mensaje.");
        } else {
            Mensaje m = new Mensaje();
            m.nombre = corta(nombre, 120); m.email = corta(email, 150);
            m.asunto = corta(asunto, 150); m.texto = corta(mensaje, 2000);
            mensajes.save(m);
            fl.addFlashAttribute("okContacto", "¡Mensaje enviado! Gracias por escribirme.");
        }
        return "redirect:/#contacto";
    }

    private static String corta(String s, int max) { return s.length() > max ? s.substring(0, max) : s; }

    /** Solo se pueden descargar documentos publicados (no borradores). */
    @GetMapping("/archivo/{id}")
    public ResponseEntity<Resource> archivo(@PathVariable long id) throws IOException {
        Documento d = docs.findById(id).filter(x -> !x.borrador).orElse(null);
        if (d == null) return ResponseEntity.notFound().build();
        return archivos.responder(d.nombreArchivo, d.nombreOriginal, true);
    }

    @GetMapping("/perfil-foto")
    public ResponseEntity<Resource> foto() throws IOException {
        String f = cfg("foto");
        if (f.isBlank()) return ResponseEntity.notFound().build();
        return archivos.responder(f, f, false);
    }

    @GetMapping("/proyecto-imagen/{id}")
    public ResponseEntity<Resource> imagenProyecto(@PathVariable long id) throws IOException {
        Proyecto p = proyectos.findById(id).orElse(null);
        if (p == null || p.imagen == null || p.imagen.isBlank()) return ResponseEntity.notFound().build();
        return archivos.responder(p.imagen, p.imagen, false);
    }
}
