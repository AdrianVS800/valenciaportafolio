package com.portafolio.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class ErrorAdvice {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String demasiadoGrande(HttpServletRequest req, RedirectAttributes fl) {
        fl.addFlashAttribute("error", "El archivo es demasiado grande (máximo 25 MB por archivo).");
        String uri = req.getRequestURI();
        if (uri.startsWith("/admin/semanas")) return "redirect:/admin/semanas";
        if (uri.startsWith("/admin/proyectos")) return "redirect:/admin/proyectos";
        if (uri.startsWith("/admin/perfil")) return "redirect:/admin/perfil";
        return "redirect:/admin/documentos";
    }
}
