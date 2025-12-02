package com.formulario.controller;

import com.formulario.model.RecomendacionEstudiosDTO;
import com.formulario.model.PosicionLaboral;
import com.formulario.model.Usuario;
import com.formulario.service.RecomendacionEstudiosService;
import com.formulario.service.FileUploadService;
import com.formulario.repository.PosicionLaboralRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/recomendaciones-estudios")
public class AdminRecomendacionEstudiosController {
    
    @Autowired
    private RecomendacionEstudiosService recomendacionEstudiosService;
    
    @Autowired
    private PosicionLaboralRepository posicionLaboralRepository;
    
    @Autowired
    private FileUploadService fileUploadService;
    
    /**
     * Verifica que el usuario sea administrador
     */
    private boolean esAdministrador(HttpSession session) {
        Usuario usuarioSesion = (Usuario) session.getAttribute("usuario");
        return usuarioSesion != null && usuarioSesion.getRol() == Usuario.Rol.ADMIN;
    }
    
    /**
     * Muestra la lista de todas las recomendaciones de estudios
     */
    @GetMapping
    public String listarRecomendaciones(
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        if (!esAdministrador(session)) {
            redirectAttributes.addFlashAttribute("error", "Acceso denegado. Solo los administradores pueden acceder.");
            return "redirect:/login";
        }
        
        try {
            List<RecomendacionEstudiosDTO> recomendaciones = recomendacionEstudiosService.obtenerTodas();
            model.addAttribute("recomendaciones", recomendaciones);
            model.addAttribute("usuario", session.getAttribute("usuario"));
            return "admin-recomendaciones-estudios";
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar las recomendaciones: " + e.getMessage());
            model.addAttribute("recomendaciones", List.of());
            return "admin-recomendaciones-estudios";
        }
    }
    
    /**
     * Muestra el formulario para crear una nueva recomendación
     */
    @GetMapping("/nueva")
    public String mostrarFormularioCrear(
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        if (!esAdministrador(session)) {
            redirectAttributes.addFlashAttribute("error", "Acceso denegado.");
            return "redirect:/login";
        }
        
        try {
            RecomendacionEstudiosDTO dto = new RecomendacionEstudiosDTO();
            dto.setActiva(true);
            model.addAttribute("recomendacion", dto);
            
            // Cargar todas las posiciones laborales activas para el selector
            List<PosicionLaboral> posiciones = posicionLaboralRepository.findByActivaTrue();
            model.addAttribute("posiciones", posiciones);
            model.addAttribute("usuario", session.getAttribute("usuario"));
            model.addAttribute("modo", "crear");
            
            return "admin-recomendacion-estudios-form";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cargar el formulario: " + e.getMessage());
            return "redirect:/admin/recomendaciones-estudios";
        }
    }
    
    /**
     * Muestra el formulario para editar una recomendación existente
     */
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(
            @PathVariable Long id,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        if (!esAdministrador(session)) {
            redirectAttributes.addFlashAttribute("error", "Acceso denegado.");
            return "redirect:/login";
        }
        
        try {
            var recomendacionOpt = recomendacionEstudiosService.obtenerPorId(id);
            if (recomendacionOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Recomendación no encontrada.");
                return "redirect:/admin/recomendaciones-estudios";
            }
            
            model.addAttribute("recomendacion", recomendacionOpt.get());
            
            // Cargar todas las posiciones laborales activas para el selector
            List<PosicionLaboral> posiciones = posicionLaboralRepository.findByActivaTrue();
            model.addAttribute("posiciones", posiciones);
            model.addAttribute("usuario", session.getAttribute("usuario"));
            model.addAttribute("modo", "editar");
            
            return "admin-recomendacion-estudios-form";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cargar el formulario: " + e.getMessage());
            return "redirect:/admin/recomendaciones-estudios";
        }
    }
    
    /**
     * Procesa la creación de una nueva recomendación
     */
    @PostMapping("/crear")
    public String crearRecomendacion(
            @ModelAttribute RecomendacionEstudiosDTO dto,
            @RequestParam(required = false) List<Long> posicionesIds,
            @RequestParam(required = false) Boolean activa,
            @RequestParam(required = false) MultipartFile imagenFile,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        if (!esAdministrador(session)) {
            redirectAttributes.addFlashAttribute("error", "Acceso denegado.");
            return "redirect:/login";
        }
        
        try {
            // Manejar checkbox activa (si no viene, es false)
            dto.setActiva(activa != null && activa);
            
            if (posicionesIds != null && !posicionesIds.isEmpty()) {
                dto.setPosicionesLaboralesIds(posicionesIds);
            }
            
            // Procesar imagen subida
            if (imagenFile != null && !imagenFile.isEmpty()) {
                if (fileUploadService.esImagenValida(imagenFile)) {
                    String imagenPath = fileUploadService.guardarImagen(imagenFile, "instituciones");
                    dto.setImagenInstitucion(imagenPath);
                } else {
                    redirectAttributes.addFlashAttribute("error", "El archivo debe ser una imagen válida (JPG, PNG, GIF, WEBP)");
                    return "redirect:/admin/recomendaciones-estudios/nueva";
                }
            }
            // Si no se subió imagen pero hay URL, mantener la URL
            // Si no hay ni imagen ni URL, dejar null
            
            recomendacionEstudiosService.crear(dto);
            redirectAttributes.addFlashAttribute("mensaje", "Recomendación de estudios creada exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al crear la recomendación: " + e.getMessage());
            return "redirect:/admin/recomendaciones-estudios/nueva";
        }
        
        return "redirect:/admin/recomendaciones-estudios";
    }
    
    /**
     * Procesa la actualización de una recomendación existente
     */
    @PostMapping("/actualizar/{id}")
    public String actualizarRecomendacion(
            @PathVariable Long id,
            @ModelAttribute RecomendacionEstudiosDTO dto,
            @RequestParam(required = false) List<Long> posicionesIds,
            @RequestParam(required = false) Boolean activa,
            @RequestParam(required = false) MultipartFile imagenFile,
            @RequestParam(required = false) Boolean eliminarImagen,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        if (!esAdministrador(session)) {
            redirectAttributes.addFlashAttribute("error", "Acceso denegado.");
            return "redirect:/login";
        }
        
        try {
            // Manejar checkbox activa (si no viene, es false)
            dto.setActiva(activa != null && activa);
            
            if (posicionesIds != null && !posicionesIds.isEmpty()) {
                dto.setPosicionesLaboralesIds(posicionesIds);
            }
            
            // Obtener la recomendación actual para verificar si tiene imagen
            var recomendacionActual = recomendacionEstudiosService.obtenerPorId(id);
            String imagenAnterior = recomendacionActual.map(RecomendacionEstudiosDTO::getImagenInstitucion).orElse(null);
            
            // Si se marca eliminar imagen, eliminar la imagen anterior
            if (eliminarImagen != null && eliminarImagen && imagenAnterior != null) {
                fileUploadService.eliminarImagen(imagenAnterior);
                dto.setImagenInstitucion(null);
            }
            // Si se sube una nueva imagen, guardarla y eliminar la anterior si existe
            else if (imagenFile != null && !imagenFile.isEmpty()) {
                if (fileUploadService.esImagenValida(imagenFile)) {
                    // Eliminar imagen anterior si existe
                    if (imagenAnterior != null && !imagenAnterior.startsWith("http")) {
                        fileUploadService.eliminarImagen(imagenAnterior);
                    }
                    String imagenPath = fileUploadService.guardarImagen(imagenFile, "instituciones");
                    dto.setImagenInstitucion(imagenPath);
                } else {
                    redirectAttributes.addFlashAttribute("error", "El archivo debe ser una imagen válida (JPG, PNG, GIF, WEBP)");
                    return "redirect:/admin/recomendaciones-estudios/editar/" + id;
                }
            }
            // Si no se sube nueva imagen y no se elimina, mantener la existente
            else if (imagenAnterior != null && dto.getImagenInstitucion() == null) {
                dto.setImagenInstitucion(imagenAnterior);
            }
            
            var actualizada = recomendacionEstudiosService.actualizar(id, dto);
            if (actualizada.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Recomendación no encontrada.");
            } else {
                redirectAttributes.addFlashAttribute("mensaje", "Recomendación de estudios actualizada exitosamente.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar la recomendación: " + e.getMessage());
            return "redirect:/admin/recomendaciones-estudios/editar/" + id;
        }
        
        return "redirect:/admin/recomendaciones-estudios";
    }
    
    /**
     * Elimina (desactiva) una recomendación
     */
    @PostMapping("/eliminar/{id}")
    public String eliminarRecomendacion(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        if (!esAdministrador(session)) {
            redirectAttributes.addFlashAttribute("error", "Acceso denegado.");
            return "redirect:/login";
        }
        
        try {
            boolean eliminada = recomendacionEstudiosService.eliminar(id);
            if (eliminada) {
                redirectAttributes.addFlashAttribute("mensaje", "Recomendación de estudios eliminada exitosamente.");
            } else {
                redirectAttributes.addFlashAttribute("error", "Recomendación no encontrada.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar la recomendación: " + e.getMessage());
        }
        
        return "redirect:/admin/recomendaciones-estudios";
    }
}

