package com.formulario.controller;

import com.formulario.model.ConfiguracionSistema;
import com.formulario.model.Usuario;
import com.formulario.service.ConfiguracionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/configuracion")
public class ConfiguracionController {
    
    @Autowired
    private ConfiguracionService configuracionService;
    
    /**
     * Muestra la página de configuraciones
     */
    @GetMapping
    public String mostrarConfiguraciones(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Usuario usuarioSesion = (Usuario) session.getAttribute("usuario");
        if (usuarioSesion == null || usuarioSesion.getRol() != Usuario.Rol.ADMIN) {
            redirectAttributes.addFlashAttribute("error", "Acceso denegado. Solo los administradores pueden acceder a las configuraciones.");
            return "redirect:/login";
        }
        
        List<ConfiguracionSistema> configuraciones = configuracionService.obtenerTodasLasConfiguraciones();
        boolean inscripcionesAbiertas = configuracionService.estanInscripcionesAbiertas();
        String apiTokenBondarea = configuracionService.obtenerApiTokenBondarea();
        
        model.addAttribute("configuraciones", configuraciones);
        model.addAttribute("inscripcionesAbiertas", inscripcionesAbiertas);
        model.addAttribute("apiTokenBondarea", apiTokenBondarea);
        model.addAttribute("usuario", usuarioSesion);
        
        return "configuracion";
    }
    
    /**
     * Cambia el estado de las inscripciones
     */
    @PostMapping("/inscripciones")
    public String cambiarEstadoInscripciones(
            @RequestParam boolean abiertas,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        Usuario usuarioSesion = (Usuario) session.getAttribute("usuario");
        if (usuarioSesion == null || usuarioSesion.getRol() != Usuario.Rol.ADMIN) {
            redirectAttributes.addFlashAttribute("error", "Acceso denegado");
            return "redirect:/login";
        }
        
        try {
            configuracionService.setInscripcionesAbiertas(abiertas, usuarioSesion.getUsername());
            
            String mensaje = abiertas ? 
                "Las inscripciones han sido abiertas exitosamente" :
                "Las inscripciones han sido cerradas exitosamente";
            
            redirectAttributes.addFlashAttribute("mensaje", mensaje);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cambiar el estado de las inscripciones: " + e.getMessage());
        }
        
        return "redirect:/configuracion";
    }
    
    /**
     * Actualiza una configuración específica
     */
    @PostMapping("/actualizar")
    public String actualizarConfiguracion(
            @RequestParam String clave,
            @RequestParam String valor,
            @RequestParam String descripcion,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        Usuario usuarioSesion = (Usuario) session.getAttribute("usuario");
        if (usuarioSesion == null || usuarioSesion.getRol() != Usuario.Rol.ADMIN) {
            redirectAttributes.addFlashAttribute("error", "Acceso denegado");
            return "redirect:/login";
        }
        
        try {
            configuracionService.guardarConfiguracion(clave, valor, descripcion, usuarioSesion.getUsername());
            redirectAttributes.addFlashAttribute("mensaje", "Configuración actualizada exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar la configuración: " + e.getMessage());
        }
        
        return "redirect:/configuracion";
    }
    
    /**
     * Guarda o actualiza el token de API de Bondarea
     */
    @PostMapping("/api-token")
    public String guardarApiToken(
            @RequestParam String token,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        Usuario usuarioSesion = (Usuario) session.getAttribute("usuario");
        if (usuarioSesion == null || usuarioSesion.getRol() != Usuario.Rol.ADMIN) {
            redirectAttributes.addFlashAttribute("error", "Acceso denegado");
            return "redirect:/login";
        }
        
        try {
            configuracionService.guardarApiTokenBondarea(token, usuarioSesion.getUsername());
            redirectAttributes.addFlashAttribute("mensaje", "Token de API de Bondarea guardado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al guardar el token: " + e.getMessage());
        }
        
        return "redirect:/configuracion";
    }
} 