package com.portafolio.config;

import com.portafolio.repository.ConfiguracionRepository;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.HashMap;
import java.util.Map;

/** Pone los textos del sitio disponibles en todas las plantillas como ${cfg['clave']}. */
@ControllerAdvice
public class SitioAdvice {
    private final ConfiguracionRepository repo;
    public SitioAdvice(ConfiguracionRepository repo) { this.repo = repo; }

    @ModelAttribute("cfg")
    public Map<String, String> cfg() {
        Map<String, String> m = new HashMap<>();
        repo.findAll().forEach(c -> m.put(c.clave, c.valor == null ? "" : c.valor));
        return m;
    }
}
