package com.portafolio.controller;

import com.portafolio.model.*;
import com.portafolio.repository.*;
import com.portafolio.service.ArchivoService;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.security.Principal;
import java.util.*;

/** Panel de administración (cPanel). Todo /admin/** exige iniciar sesión. */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final Map<String, List<String>> EXT = Map.of(
            "PDF", List.of("pdf"), "Word", List.of("doc", "docx"), "Excel", List.of("xls", "xlsx", "csv"));
    private static final List<String> IMAGENES = List.of("png", "jpg", "jpeg", "webp", "gif");
    private static final Set<String> CLAVES = Set.of("nombre", "perfil_texto", "email", "github", "linkedin",
            "hab_backend", "hab_frontend", "hab_tools");

    private final DocumentoRepository docs;
    private final SemanaRepository semanas;
    private final ProyectoRepository proyectos;
    private final MensajeRepository mensajes;
    private final UsuarioRepository usuarios;
    private final ConfiguracionRepository config;
    private final ArchivoService archivos;
    private final PasswordEncoder encoder;

    public AdminController(DocumentoRepository docs, SemanaRepository semanas, ProyectoRepository proyectos,
                           MensajeRepository mensajes, UsuarioRepository usuarios, ConfiguracionRepository config,
                           ArchivoService archivos, PasswordEncoder encoder) {
        this.docs = docs; this.semanas = semanas; this.proyectos = proyectos; this.mensajes = mensajes;
        this.usuarios = usuarios; this.config = config; this.archivos = archivos; this.encoder = encoder;
    }

    // ---------- Login ----------
    @GetMapping("/login")
    public String login() { return "admin/login"; }

    // ---------- Resumen ----------
    @GetMapping
    public String resumen(Model m) {
        List<Documento> todos = docs.findAll();
        m.addAttribute("totalDocs", todos.size());
        m.addAttribute("publicados", todos.stream().filter(d -> !d.borrador).count());
        m.addAttribute("totalProyectos", proyectos.count());
        m.addAttribute("totalMensajes", mensajes.count());
        return "admin/resumen";
    }

    // ---------- Semanas (con sus documentos) ----------
    @GetMapping("/semanas")
    public String listaSemanas(Model m) {
        List<Semana> lista = semanas.findAllByOrderByNumeroAsc();
        Map<Integer, List<Documento>> porSemana = new LinkedHashMap<>();
        lista.forEach(s -> porSemana.put(s.numero, new ArrayList<>()));
        for (Documento d : docs.findAllByOrderByIdDesc())
            if (d.semana != null && porSemana.containsKey(d.semana)) porSemana.get(d.semana).add(d);
        m.addAttribute("semanas", lista);
        m.addAttribute("docsPorSemana", porSemana);
        return "admin/semanas";
    }

    @PostMapping("/semanas/{n}")
    public String guardarSemana(@PathVariable int n, @RequestParam String titulo,
                                @RequestParam(defaultValue = "") String descripcion, RedirectAttributes fl) {
        semanas.findById(n).ifPresent(s -> {
            s.titulo = titulo.isBlank() ? "Semana " + n : titulo.trim();
            s.descripcion = descripcion.trim();
            semanas.save(s);
        });
        fl.addFlashAttribute("mensaje", "Semana " + n + " actualizada.");
        return "redirect:/admin/semanas#semana-" + n;
    }

    /** Sube uno o varios archivos (PDF, Word o Excel) directamente a una semana. El tipo se detecta por la extensión. */
    @PostMapping("/semanas/{n}/documentos")
    public String subirASemana(@PathVariable int n, @RequestParam("archivos") MultipartFile[] archivos,
                               @RequestParam(defaultValue = "") String titulo,
                               @RequestParam(defaultValue = "false") boolean borrador, RedirectAttributes fl) {
        if (!semanas.existsById(n)) {
            fl.addFlashAttribute("error", "La semana no existe.");
            return "redirect:/admin/semanas";
        }
        String destino = "redirect:/admin/semanas#semana-" + n;
        List<MultipartFile> validos = Arrays.stream(archivos).filter(f -> !f.isEmpty()).toList();
        if (validos.isEmpty()) {
            fl.addFlashAttribute("error", "Selecciona al menos un archivo.");
            return destino;
        }
        int ok = 0;
        List<String> fallos = new ArrayList<>();
        for (MultipartFile f : validos) {
            String original = nombreSeguro(f);
            try {
                String tipo = tipoPorExtension(original);
                if (tipo == null) throw new IllegalArgumentException(original + ": formato no permitido (usa PDF, Word o Excel).");
                String base = original.contains(".") ? original.substring(0, original.lastIndexOf('.')) : original;
                String t = (validos.size() == 1 && !titulo.isBlank()) ? titulo : base;
                crearDocumento(t, tipo, f, n, "", "", borrador);
                ok++;
            } catch (IllegalArgumentException | IOException e) {
                fallos.add(e.getMessage());
            }
        }
        if (ok > 0) fl.addFlashAttribute("mensaje",
                ok + (ok == 1 ? " documento subido" : " documentos subidos") + " a la semana " + n + (borrador ? " como borrador." : "."));
        if (!fallos.isEmpty()) fl.addFlashAttribute("error", String.join(" ", fallos));
        return destino;
    }

    // ---------- Documentos ----------
    @GetMapping("/documentos")
    public String listaDocumentos(Model m) {
        m.addAttribute("documentos", docs.findAllByOrderByIdDesc());
        m.addAttribute("semanas", semanas.findAllByOrderByNumeroAsc());
        return "admin/documentos";
    }

    @PostMapping("/documentos")
    public String subirDocumento(@RequestParam String titulo, @RequestParam String tipo,
                                 @RequestParam("archivo") MultipartFile archivo,
                                 @RequestParam(required = false) Integer semana,
                                 @RequestParam(defaultValue = "") String etiquetas,
                                 @RequestParam(defaultValue = "") String descripcion,
                                 @RequestParam(defaultValue = "false") boolean borrador, RedirectAttributes fl) {
        try {
            if (titulo.isBlank()) throw new IllegalArgumentException("Escribe un título.");
            if (!EXT.containsKey(tipo)) throw new IllegalArgumentException("Tipo de documento no válido.");
            Integer sem = (semana != null && semanas.existsById(semana)) ? semana : null;
            crearDocumento(titulo, tipo, archivo, sem, etiquetas.trim(), descripcion.trim(), borrador);
            fl.addFlashAttribute("mensaje", borrador ? "Guardado como borrador (oculto al público)." : "Documento publicado.");
        } catch (IllegalArgumentException | IOException e) {
            fl.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/documentos";
    }

    @PostMapping("/documentos/{id}/estado")
    public String cambiarEstado(@PathVariable long id, @RequestParam(required = false) String volver, RedirectAttributes fl) {
        docs.findById(id).ifPresent(d -> { d.borrador = !d.borrador; docs.save(d); });
        fl.addFlashAttribute("mensaje", "Estado actualizado.");
        return volverA(volver);
    }

    @PostMapping("/documentos/{id}/eliminar")
    public String eliminarDocumento(@PathVariable long id, @RequestParam(required = false) String volver, RedirectAttributes fl) {
        docs.findById(id).ifPresent(d -> { docs.delete(d); archivos.borrar(d.nombreArchivo); });
        fl.addFlashAttribute("mensaje", "Documento eliminado.");
        return volverA(volver);
    }

    @GetMapping("/archivo/{id}")
    public ResponseEntity<Resource> descargar(@PathVariable long id) throws IOException {
        Documento d = docs.findById(id).orElse(null);
        if (d == null) return ResponseEntity.notFound().build();
        return archivos.responder(d.nombreArchivo, d.nombreOriginal, true);
    }

    // ---------- utilidades de documentos ----------
    private Documento crearDocumento(String titulo, String tipo, MultipartFile archivo, Integer semana,
                                     String etiquetas, String descripcion, boolean borrador) throws IOException {
        ArchivoService.Guardado g = archivos.guardar(archivo, EXT.get(tipo));
        String t = titulo.trim();
        Documento d = new Documento();
        d.titulo = t.length() > 120 ? t.substring(0, 120) : t;
        d.tipo = tipo;
        d.nombreArchivo = g.nombre(); d.nombreOriginal = g.original(); d.tamanoBytes = archivo.getSize();
        d.semana = semana; d.etiquetas = etiquetas; d.descripcion = descripcion; d.borrador = borrador;
        try { return docs.save(d); }
        catch (RuntimeException e) { archivos.borrar(g.nombre()); throw e; }
    }

    private static String nombreSeguro(MultipartFile f) {
        String n = Objects.requireNonNullElse(f.getOriginalFilename(), "archivo").replace('\\', '/');
        return n.substring(n.lastIndexOf('/') + 1);
    }

    private static String tipoPorExtension(String nombre) {
        String ext = nombre.contains(".") ? nombre.substring(nombre.lastIndexOf('.') + 1).toLowerCase() : "";
        for (Map.Entry<String, List<String>> e : EXT.entrySet()) if (e.getValue().contains(ext)) return e.getKey();
        return null;
    }

    private static String volverA(String v) {
        return "semanas".equals(v) ? "redirect:/admin/semanas" : "redirect:/admin/documentos";
    }

    // ---------- Proyectos ----------
    @GetMapping("/proyectos")
    public String listaProyectos(Model m) {
        m.addAttribute("proyectos", proyectos.findAllByOrderByIdDesc());
        return "admin/proyectos";
    }

    @PostMapping("/proyectos")
    public String crearProyecto(@RequestParam String titulo, @RequestParam(defaultValue = "") String categoria,
                                @RequestParam(defaultValue = "") String descripcion,
                                @RequestParam(defaultValue = "") String url,
                                @RequestParam(value = "imagen", required = false) MultipartFile imagen,
                                RedirectAttributes fl) {
        url = url.trim();
        if (titulo.isBlank()) { fl.addFlashAttribute("error", "Escribe un título."); return "redirect:/admin/proyectos"; }
        if (!url.isEmpty() && !url.matches("(?i)^https?://.+")) {
            fl.addFlashAttribute("error", "La dirección debe empezar con http:// o https://");
            return "redirect:/admin/proyectos";
        }
        try {
            Proyecto p = new Proyecto();
            p.titulo = titulo.trim(); p.categoria = categoria.trim(); p.descripcion = descripcion.trim(); p.url = url;
            if (imagen != null && !imagen.isEmpty()) p.imagen = archivos.guardar(imagen, IMAGENES).nombre();
            proyectos.save(p);
            fl.addFlashAttribute("mensaje", "Proyecto agregado.");
        } catch (IllegalArgumentException | IOException e) {
            fl.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/proyectos";
    }

    @PostMapping("/proyectos/{id}/eliminar")
    public String eliminarProyecto(@PathVariable long id, RedirectAttributes fl) {
        proyectos.findById(id).ifPresent(p -> { archivos.borrar(p.imagen); proyectos.delete(p); });
        fl.addFlashAttribute("mensaje", "Proyecto eliminado.");
        return "redirect:/admin/proyectos";
    }

    // ---------- Mensajes de contacto ----------
    @GetMapping("/mensajes")
    public String listaMensajes(Model m) {
        m.addAttribute("mensajes", mensajes.findAllByOrderByIdDesc());
        return "admin/mensajes";
    }

    @PostMapping("/mensajes/{id}/eliminar")
    public String eliminarMensaje(@PathVariable long id, RedirectAttributes fl) {
        mensajes.deleteById(id);
        fl.addFlashAttribute("mensaje", "Mensaje eliminado.");
        return "redirect:/admin/mensajes";
    }

    // ---------- Perfil del sitio (incluye la foto de inicio) ----------
    @GetMapping("/perfil")
    public String perfil() { return "admin/perfil"; }

    @PostMapping("/perfil")
    public String guardarPerfil(@RequestParam Map<String, String> p,
                                @RequestParam(value = "foto", required = false) MultipartFile foto,
                                @RequestParam(defaultValue = "false") boolean quitarFoto,
                                RedirectAttributes fl) {
        if (p.getOrDefault("nombre", "").isBlank()) {
            fl.addFlashAttribute("error", "El nombre no puede estar vacío.");
            return "redirect:/admin/perfil";
        }
        for (String k : List.of("github", "linkedin")) {
            String v = p.getOrDefault(k, "").trim();
            if (!v.isEmpty() && !v.matches("(?i)^https?://.+")) {
                fl.addFlashAttribute("error", "Los enlaces de redes deben empezar con http:// o https://");
                return "redirect:/admin/perfil";
            }
        }
        CLAVES.forEach(k -> { if (p.containsKey(k)) config.save(new Configuracion(k, p.get(k).trim())); });
        try {
            if (quitarFoto) {
                config.findById("foto").ifPresent(c -> archivos.borrar(c.valor));
                config.save(new Configuracion("foto", ""));
            }
            if (foto != null && !foto.isEmpty()) {
                ArchivoService.Guardado g = archivos.guardar(foto, IMAGENES);
                config.findById("foto").ifPresent(c -> archivos.borrar(c.valor));
                config.save(new Configuracion("foto", g.nombre()));
            }
            fl.addFlashAttribute("mensaje", "Perfil actualizado.");
        } catch (IllegalArgumentException | IOException e) {
            fl.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/perfil";
    }

    // ---------- Cuenta ----------
    @GetMapping("/cuenta")
    public String cuenta(Principal pr, Model m) {
        m.addAttribute("email", pr.getName());
        return "admin/cuenta";
    }

    @PostMapping("/cuenta")
    public String cambiarClave(@RequestParam String actual, @RequestParam String nueva,
                               @RequestParam String confirmar, Principal pr, RedirectAttributes fl) {
        Usuario u = usuarios.findByEmail(pr.getName()).orElseThrow();
        if (!encoder.matches(actual, u.passwordHash)) fl.addFlashAttribute("error", "La contraseña actual no es correcta.");
        else if (nueva.length() < 8) fl.addFlashAttribute("error", "La nueva contraseña debe tener al menos 8 caracteres.");
        else if (!nueva.equals(confirmar)) fl.addFlashAttribute("error", "La confirmación no coincide.");
        else {
            u.passwordHash = encoder.encode(nueva);
            usuarios.save(u);
            fl.addFlashAttribute("mensaje", "Contraseña actualizada.");
        }
        return "redirect:/admin/cuenta";
    }
}
