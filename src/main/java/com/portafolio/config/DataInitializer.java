package com.portafolio.config;

import com.portafolio.model.*;
import com.portafolio.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/** Crea los datos iniciales la primera vez que se ejecuta la app. */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarios;
    private final SemanaRepository semanas;
    private final ConfiguracionRepository config;
    private final PasswordEncoder encoder;
    private final String adminEmail, adminClave;

    public DataInitializer(UsuarioRepository usuarios, SemanaRepository semanas, ConfiguracionRepository config,
                           PasswordEncoder encoder, @Value("${admin.email}") String adminEmail,
                           @Value("${admin.password}") String adminClave) {
        this.usuarios = usuarios; this.semanas = semanas; this.config = config; this.encoder = encoder;
        this.adminEmail = adminEmail; this.adminClave = adminClave;
    }

    @Override
    public void run(String... args) {
        if (usuarios.count() == 0)
            usuarios.save(new Usuario(adminEmail.trim().toLowerCase(), encoder.encode(adminClave)));

        if (semanas.count() == 0) {
            for (int n = 1; n <= 16; n++) {
                String desc = switch (n) {
                    case 1 -> "Utilizando una infografía (utilizar herramientas como canvas, genially, etc) realizar el resumen del tema: Fundamentos de un Proyecto Web.";
                    case 2 -> "Utilizando infografías realizar las actividades del tema 02: actores y organización del proyecto web.";
                    case 3 -> "ACTIVIDAD 1";
                    default -> "Trabajos y actividades correspondientes a la semana " + n + ".";
                };
                semanas.save(new Semana(n, "Semana " + n, desc));
            }
        }

        Map<String, String> por = new LinkedHashMap<>();
        por.put("nombre", "Adrian");
        por.put("perfil_texto", "Desarrollador Web especializado en el stack Java, apasionado por la creación de soluciones eficientes y escalables. Con experiencia en Spring Boot, JSP y MySQL.");
        por.put("email", adminEmail);
        por.put("github", "");
        por.put("linkedin", "");
        por.put("hab_backend", "Java, Spring Boot, MySQL, JSP");
        por.put("hab_frontend", "HTML/CSS, JavaScript, Bootstrap");
        por.put("hab_tools", "Git, Maven, cPanel");
        por.put("foto", "");
        por.forEach((k, v) -> { if (!config.existsById(k)) config.save(new Configuracion(k, v)); });

        // Si el nombre aún es el de una versión anterior, lo cambia a Adrian
        config.findById("nombre").ifPresent(c -> {
            if ("EDUARDO".equalsIgnoreCase(c.valor) || "Adriany".equalsIgnoreCase(c.valor)) { c.valor = "Adrian"; config.save(c); }
        });
    }
}
