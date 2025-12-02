package com.formulario.controller;

import com.formulario.model.RolProfesional;
import com.formulario.model.Usuario;
import com.formulario.repository.RolProfesionalRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/roles-profesionales")
public class AdminRolProfesionalController {
    
    @Autowired
    private RolProfesionalRepository rolProfesionalRepository;
    
    /**
     * Verifica que el usuario sea administrador
     */
    private boolean esAdministrador(HttpSession session) {
        Usuario usuarioSesion = (Usuario) session.getAttribute("usuario");
        return usuarioSesion != null && usuarioSesion.getRol() == Usuario.Rol.ADMIN;
    }
    
    /**
     * Muestra la lista de todos los roles profesionales
     */
    @GetMapping
    public String listarRoles(
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        if (!esAdministrador(session)) {
            redirectAttributes.addFlashAttribute("error", "Acceso denegado. Solo los administradores pueden acceder.");
            return "redirect:/login";
        }
        
        try {
            List<RolProfesional> roles = rolProfesionalRepository.findAll();
            model.addAttribute("roles", roles);
            model.addAttribute("usuario", session.getAttribute("usuario"));
            return "admin-roles-profesionales";
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar los roles profesionales: " + e.getMessage());
            model.addAttribute("roles", List.of());
            return "admin-roles-profesionales";
        }
    }
    
    /**
     * Muestra el formulario para crear un nuevo rol profesional
     */
    @GetMapping("/nuevo")
    public String mostrarFormularioCrear(
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        if (!esAdministrador(session)) {
            redirectAttributes.addFlashAttribute("error", "Acceso denegado.");
            return "redirect:/login";
        }
        
        try {
            RolProfesional rol = new RolProfesional();
            rol.setActivo(true);
            // Valores por defecto para los pesos
            rol.setPesoLogica(25);
            rol.setPesoMatematica(25);
            rol.setPesoCreatividad(25);
            rol.setPesoProgramacion(25);
            
            model.addAttribute("rol", rol);
            model.addAttribute("usuario", session.getAttribute("usuario"));
            model.addAttribute("modo", "crear");
            
            return "admin-rol-profesional-form";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cargar el formulario: " + e.getMessage());
            return "redirect:/admin/roles-profesionales";
        }
    }
    
    /**
     * Muestra el formulario para editar un rol profesional existente
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
            Optional<RolProfesional> rolOpt = rolProfesionalRepository.findById(id);
            if (rolOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Rol profesional no encontrado.");
                return "redirect:/admin/roles-profesionales";
            }
            
            model.addAttribute("rol", rolOpt.get());
            model.addAttribute("usuario", session.getAttribute("usuario"));
            model.addAttribute("modo", "editar");
            
            return "admin-rol-profesional-form";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cargar el formulario: " + e.getMessage());
            return "redirect:/admin/roles-profesionales";
        }
    }
    
    /**
     * Procesa la creación de un nuevo rol profesional
     */
    @PostMapping("/crear")
    public String crearRol(
            @ModelAttribute @Valid RolProfesional rol,
            BindingResult bindingResult,
            @RequestParam(required = false) Boolean activo,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        if (!esAdministrador(session)) {
            redirectAttributes.addFlashAttribute("error", "Acceso denegado.");
            return "redirect:/login";
        }
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("rol", rol);
            model.addAttribute("usuario", session.getAttribute("usuario"));
            model.addAttribute("modo", "crear");
            return "admin-rol-profesional-form";
        }
        
        try {
            rol.setActivo(activo != null && activo);
            rolProfesionalRepository.save(rol);
            redirectAttributes.addFlashAttribute("mensaje", "Rol profesional creado exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al crear el rol profesional: " + e.getMessage());
            return "redirect:/admin/roles-profesionales/nuevo";
        }
        
        return "redirect:/admin/roles-profesionales";
    }
    
    /**
     * Procesa la actualización de un rol profesional existente
     */
    @PostMapping("/actualizar/{id}")
    public String actualizarRol(
            @PathVariable Long id,
            @ModelAttribute @Valid RolProfesional rol,
            BindingResult bindingResult,
            @RequestParam(required = false) Boolean activo,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        if (!esAdministrador(session)) {
            redirectAttributes.addFlashAttribute("error", "Acceso denegado.");
            return "redirect:/login";
        }
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("rol", rol);
            model.addAttribute("usuario", session.getAttribute("usuario"));
            model.addAttribute("modo", "editar");
            return "admin-rol-profesional-form";
        }
        
        try {
            Optional<RolProfesional> rolExistenteOpt = rolProfesionalRepository.findById(id);
            if (rolExistenteOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Rol profesional no encontrado.");
                return "redirect:/admin/roles-profesionales";
            }
            
            RolProfesional rolExistente = rolExistenteOpt.get();
            
            // Actualizar todos los campos
            rolExistente.setTitulo(rol.getTitulo());
            rolExistente.setDescripcion(rol.getDescripcion());
            rolExistente.setNivel(rol.getNivel());
            rolExistente.setCategoria(rol.getCategoria());
            rolExistente.setResponsabilidades(rol.getResponsabilidades());
            rolExistente.setHabilidadesRequeridas(rol.getHabilidadesRequeridas());
            rolExistente.setTecnologiasRecomendadas(rol.getTecnologiasRecomendadas());
            rolExistente.setRutaCarrera(rol.getRutaCarrera());
            
            // Requisitos mínimos
            rolExistente.setMinLogica(rol.getMinLogica());
            rolExistente.setMinMatematica(rol.getMinMatematica());
            rolExistente.setMinCreatividad(rol.getMinCreatividad());
            rolExistente.setMinProgramacion(rol.getMinProgramacion());
            rolExistente.setMinPromedio(rol.getMinPromedio());
            
            // Pesos
            rolExistente.setPesoLogica(rol.getPesoLogica());
            rolExistente.setPesoMatematica(rol.getPesoMatematica());
            rolExistente.setPesoCreatividad(rol.getPesoCreatividad());
            rolExistente.setPesoProgramacion(rol.getPesoProgramacion());
            
            rolExistente.setActivo(activo != null && activo);
            
            rolProfesionalRepository.save(rolExistente);
            redirectAttributes.addFlashAttribute("mensaje", "Rol profesional actualizado exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar el rol profesional: " + e.getMessage());
            return "redirect:/admin/roles-profesionales/editar/" + id;
        }
        
        return "redirect:/admin/roles-profesionales";
    }
    
    /**
     * Elimina (desactiva) un rol profesional
     */
    @PostMapping("/eliminar/{id}")
    public String eliminarRol(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        if (!esAdministrador(session)) {
            redirectAttributes.addFlashAttribute("error", "Acceso denegado.");
            return "redirect:/login";
        }
        
        try {
            Optional<RolProfesional> rolOpt = rolProfesionalRepository.findById(id);
            if (rolOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Rol profesional no encontrado.");
                return "redirect:/admin/roles-profesionales";
            }
            
            RolProfesional rol = rolOpt.get();
            rol.setActivo(false);
            rolProfesionalRepository.save(rol);
            
            redirectAttributes.addFlashAttribute("mensaje", "Rol profesional eliminado (desactivado) exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar el rol profesional: " + e.getMessage());
        }
        
        return "redirect:/admin/roles-profesionales";
    }
}

