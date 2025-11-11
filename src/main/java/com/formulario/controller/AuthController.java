package com.formulario.controller;

import com.formulario.model.Usuario;
import com.formulario.service.AuthService;
import com.formulario.service.ExamenService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;
import java.util.Map;

@Controller
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private ExamenService examenService;
    
    // Mostrar página de login
    @GetMapping("/login")
    public String mostrarLogin(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "login";
    }
    
    // Procesar login (ya no se usa, Spring Security maneja esto)
    @PostMapping("/login")
    public String procesarLogin(@RequestParam String username,
                               @RequestParam String password,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        
        Optional<Usuario> usuarioOpt = authService.autenticarUsuario(username, password);
        
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            session.setAttribute("usuario", usuario);
            session.setAttribute("usuarioId", usuario.getId());
            session.setAttribute("usuarioNombre", usuario.getNombreCompleto());
            session.setAttribute("usuarioRol", usuario.getRol());
            
            redirectAttributes.addFlashAttribute("mensaje", "Bienvenido, " + usuario.getNombreCompleto());
            return "redirect:/dashboard";
        } else {
            redirectAttributes.addFlashAttribute("error", "Usuario o contraseña incorrectos");
            return "redirect:/login";
        }
    }
    
    // Logout
    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("mensaje", "Sesión cerrada correctamente");
        return "redirect:/login";
    }
    
    // Dashboard (requiere autenticación)
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, RedirectAttributes redirectAttributes, Model model) {
        // Usar Spring Security para obtener la autenticación
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() || 
            "anonymousUser".equals(authentication.getName())) {
            redirectAttributes.addFlashAttribute("error", "Debe iniciar sesión para acceder al dashboard");
            return "redirect:/login";
        }
        
        // Obtener el usuario desde la base de datos usando el username de Spring Security
        String username = authentication.getName();
        Optional<Usuario> usuarioOpt = authService.buscarPorUsername(username);
        
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            // Guardar en sesión para compatibilidad
            session.setAttribute("usuario", usuario);
            session.setAttribute("usuarioId", usuario.getId());
            session.setAttribute("usuarioNombre", usuario.getNombreCompleto());
            session.setAttribute("usuarioRol", usuario.getRol());
            
            // Cargar estadísticas reales
            Map<String, Object> estadisticas = examenService.obtenerEstadisticas();
            model.addAttribute("estadisticas", estadisticas);
            model.addAttribute("usuario", usuario);
            return "dashboard";
        } else {
            redirectAttributes.addFlashAttribute("error", "Usuario no encontrado");
            return "redirect:/login";
        }
    }
    
    // Mostrar formulario de registro (solo para administradores)
    @GetMapping("/registro")
    public String mostrarRegistro(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        Usuario usuarioSesion = (Usuario) session.getAttribute("usuario");
        if (usuarioSesion == null || usuarioSesion.getRol() != Usuario.Rol.ADMIN) {
            redirectAttributes.addFlashAttribute("error", "Acceso denegado");
            return "redirect:/login";
        }
        
        model.addAttribute("usuario", new Usuario());
        return "registro";
    }
    
    // Procesar registro
    @PostMapping("/registro")
    public String procesarRegistro(@Valid @ModelAttribute("usuario") Usuario usuario,
                                  BindingResult result,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        
        Usuario usuarioSesion = (Usuario) session.getAttribute("usuario");
        if (usuarioSesion == null || usuarioSesion.getRol() != Usuario.Rol.ADMIN) {
            redirectAttributes.addFlashAttribute("error", "Acceso denegado");
            return "redirect:/login";
        }
        
        if (result.hasErrors()) {
            return "registro";
        }
        
        // Verificar si el username ya existe
        if (authService.existeUsername(usuario.getUsername())) {
            result.rejectValue("username", "error.usuario", "Este nombre de usuario ya está en uso");
            return "registro";
        }
        
        // Verificar si el email ya existe
        if (authService.existeEmail(usuario.getEmail())) {
            result.rejectValue("email", "error.usuario", "Este email ya está registrado");
            return "registro";
        }
        
        try {
            authService.crearUsuario(usuario);
            redirectAttributes.addFlashAttribute("mensaje", "Usuario creado correctamente");
            return "redirect:/dashboard";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al crear el usuario: " + e.getMessage());
            return "redirect:/registro";
        }
    }
} 