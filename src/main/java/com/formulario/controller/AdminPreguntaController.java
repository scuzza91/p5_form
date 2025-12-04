package com.formulario.controller;

import com.formulario.model.Opcion;
import com.formulario.model.Pregunta;
import com.formulario.model.Usuario;
import com.formulario.repository.OpcionRepository;
import com.formulario.repository.PreguntaRepository;
import com.formulario.service.FileUploadService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/preguntas")
public class AdminPreguntaController {
    
    @Autowired
    private PreguntaRepository preguntaRepository;
    
    @Autowired
    private OpcionRepository opcionRepository;
    
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
     * Muestra la lista de todas las preguntas
     */
    @GetMapping
    public String listarPreguntas(
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        if (!esAdministrador(session)) {
            redirectAttributes.addFlashAttribute("error", "Acceso denegado. Solo los administradores pueden acceder.");
            return "redirect:/login";
        }
        
        try {
            List<Pregunta> preguntas = preguntaRepository.findAll();
            model.addAttribute("preguntas", preguntas);
            model.addAttribute("usuario", session.getAttribute("usuario"));
            return "admin-preguntas";
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar las preguntas: " + e.getMessage());
            model.addAttribute("preguntas", List.of());
            return "admin-preguntas";
        }
    }
    
    /**
     * Muestra el formulario para crear una nueva pregunta
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
            Pregunta pregunta = new Pregunta();
            pregunta.setActiva(true);
            pregunta.setOpcionCorrecta(1);
            
            // Inicializar opciones vacías
            List<Opcion> opciones = new ArrayList<>();
            for (int i = 1; i <= 4; i++) {
                Opcion opcion = new Opcion();
                opcion.setOrden(i);
                opcion.setPregunta(pregunta);
                opciones.add(opcion);
            }
            pregunta.setOpciones(opciones);
            
            model.addAttribute("pregunta", pregunta);
            model.addAttribute("usuario", session.getAttribute("usuario"));
            model.addAttribute("modo", "crear");
            model.addAttribute("areasConocimiento", Pregunta.AreaConocimiento.values());
            
            return "admin-pregunta-form";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cargar el formulario: " + e.getMessage());
            return "redirect:/admin/preguntas";
        }
    }
    
    /**
     * Muestra el formulario para editar una pregunta existente
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
            Optional<Pregunta> preguntaOpt = preguntaRepository.findById(id);
            if (preguntaOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Pregunta no encontrada.");
                return "redirect:/admin/preguntas";
            }
            
            Pregunta pregunta = preguntaOpt.get();
            
            // Asegurar que tenga 4 opciones
            List<Opcion> opciones = pregunta.getOpciones();
            if (opciones == null) {
                opciones = new ArrayList<>();
            }
            
            // Completar hasta 4 opciones si faltan
            while (opciones.size() < 4) {
                Opcion opcion = new Opcion();
                opcion.setOrden(opciones.size() + 1);
                opcion.setPregunta(pregunta);
                opciones.add(opcion);
            }
            
            // Ordenar opciones por orden
            opciones.sort((o1, o2) -> Integer.compare(
                o1.getOrden() != null ? o1.getOrden() : 0,
                o2.getOrden() != null ? o2.getOrden() : 0
            ));
            
            pregunta.setOpciones(opciones);
            
            model.addAttribute("pregunta", pregunta);
            model.addAttribute("usuario", session.getAttribute("usuario"));
            model.addAttribute("modo", "editar");
            model.addAttribute("areasConocimiento", Pregunta.AreaConocimiento.values());
            
            return "admin-pregunta-form";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cargar el formulario: " + e.getMessage());
            return "redirect:/admin/preguntas";
        }
    }
    
    /**
     * Procesa la creación de una nueva pregunta
     */
    @PostMapping("/crear")
    public String crearPregunta(
            @RequestParam String enunciado,
            @RequestParam Pregunta.AreaConocimiento areaConocimiento,
            @RequestParam Integer opcionCorrecta,
            @RequestParam String opcion1,
            @RequestParam String opcion2,
            @RequestParam String opcion3,
            @RequestParam String opcion4,
            @RequestParam(required = false) Boolean activa,
            @RequestParam(required = false) MultipartFile imagenFile,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        if (!esAdministrador(session)) {
            redirectAttributes.addFlashAttribute("error", "Acceso denegado.");
            return "redirect:/login";
        }
        
        try {
            // Validaciones básicas
            if (enunciado == null || enunciado.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "El enunciado es obligatorio.");
                return "redirect:/admin/preguntas/nuevo";
            }
            
            if (opcionCorrecta == null || opcionCorrecta < 1 || opcionCorrecta > 4) {
                redirectAttributes.addFlashAttribute("error", "La opción correcta debe ser entre 1 y 4.");
                return "redirect:/admin/preguntas/nuevo";
            }
            
            // Crear la pregunta
            Pregunta pregunta = new Pregunta();
            pregunta.setEnunciado(enunciado.trim());
            pregunta.setAreaConocimiento(areaConocimiento);
            pregunta.setOpcionCorrecta(opcionCorrecta);
            pregunta.setActiva(activa != null && activa);
            
            // Procesar imagen subida
            if (imagenFile != null && !imagenFile.isEmpty()) {
                if (fileUploadService.esImagenValida(imagenFile)) {
                    String imagenPath = fileUploadService.guardarImagen(imagenFile, "preguntas");
                    pregunta.setImagenUrl(imagenPath);
                } else {
                    redirectAttributes.addFlashAttribute("error", "El archivo debe ser una imagen válida (JPG, PNG, GIF, WEBP)");
                    return "redirect:/admin/preguntas/nuevo";
                }
            }
            
            // Guardar la pregunta primero para obtener el ID
            pregunta = preguntaRepository.save(pregunta);
            
            // Crear y guardar las opciones
            List<Opcion> opciones = new ArrayList<>();
            String[] textosOpciones = {opcion1, opcion2, opcion3, opcion4};
            
            for (int i = 0; i < 4; i++) {
                Opcion opcion = new Opcion();
                opcion.setTexto(textosOpciones[i] != null ? textosOpciones[i].trim() : "");
                opcion.setPregunta(pregunta);
                opcion.setOrden(i + 1);
                opciones.add(opcion);
            }
            
            opcionRepository.saveAll(opciones);
            pregunta.setOpciones(opciones);
            preguntaRepository.save(pregunta);
            
            redirectAttributes.addFlashAttribute("mensaje", "Pregunta creada exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al crear la pregunta: " + e.getMessage());
            return "redirect:/admin/preguntas/nuevo";
        }
        
        return "redirect:/admin/preguntas";
    }
    
    /**
     * Procesa la actualización de una pregunta existente
     */
    @PostMapping("/actualizar/{id}")
    public String actualizarPregunta(
            @PathVariable Long id,
            @RequestParam String enunciado,
            @RequestParam Pregunta.AreaConocimiento areaConocimiento,
            @RequestParam Integer opcionCorrecta,
            @RequestParam String opcion1,
            @RequestParam String opcion2,
            @RequestParam String opcion3,
            @RequestParam String opcion4,
            @RequestParam(required = false) Boolean activa,
            @RequestParam(required = false) MultipartFile imagenFile,
            @RequestParam(required = false) String eliminarImagen,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        if (!esAdministrador(session)) {
            redirectAttributes.addFlashAttribute("error", "Acceso denegado.");
            return "redirect:/login";
        }
        
        try {
            Optional<Pregunta> preguntaOpt = preguntaRepository.findById(id);
            if (preguntaOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Pregunta no encontrada.");
                return "redirect:/admin/preguntas";
            }
            
            // Validaciones básicas
            if (enunciado == null || enunciado.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "El enunciado es obligatorio.");
                return "redirect:/admin/preguntas/editar/" + id;
            }
            
            if (opcionCorrecta == null || opcionCorrecta < 1 || opcionCorrecta > 4) {
                redirectAttributes.addFlashAttribute("error", "La opción correcta debe ser entre 1 y 4.");
                return "redirect:/admin/preguntas/editar/" + id;
            }
            
            Pregunta pregunta = preguntaOpt.get();
            pregunta.setEnunciado(enunciado.trim());
            pregunta.setAreaConocimiento(areaConocimiento);
            pregunta.setOpcionCorrecta(opcionCorrecta);
            pregunta.setActiva(activa != null && activa);
            
            // Manejar imagen: eliminar si se solicita
            if (eliminarImagen != null && "true".equals(eliminarImagen)) {
                if (pregunta.getImagenUrl() != null) {
                    fileUploadService.eliminarImagen(pregunta.getImagenUrl());
                    pregunta.setImagenUrl(null);
                }
            }
            
            // Procesar nueva imagen subida
            if (imagenFile != null && !imagenFile.isEmpty()) {
                if (fileUploadService.esImagenValida(imagenFile)) {
                    // Eliminar imagen anterior si existe
                    if (pregunta.getImagenUrl() != null) {
                        fileUploadService.eliminarImagen(pregunta.getImagenUrl());
                    }
                    String imagenPath = fileUploadService.guardarImagen(imagenFile, "preguntas");
                    pregunta.setImagenUrl(imagenPath);
                } else {
                    redirectAttributes.addFlashAttribute("error", "El archivo debe ser una imagen válida (JPG, PNG, GIF, WEBP)");
                    return "redirect:/admin/preguntas/editar/" + id;
                }
            }
            
            // Actualizar o crear opciones
            List<Opcion> opcionesExistentes = pregunta.getOpciones();
            if (opcionesExistentes == null) {
                opcionesExistentes = new ArrayList<>();
            }
            
            // Eliminar opciones existentes
            opcionRepository.deleteAll(opcionesExistentes);
            
            // Crear nuevas opciones
            List<Opcion> nuevasOpciones = new ArrayList<>();
            String[] textosOpciones = {opcion1, opcion2, opcion3, opcion4};
            
            for (int i = 0; i < 4; i++) {
                Opcion opcion = new Opcion();
                opcion.setTexto(textosOpciones[i] != null ? textosOpciones[i].trim() : "");
                opcion.setPregunta(pregunta);
                opcion.setOrden(i + 1);
                nuevasOpciones.add(opcion);
            }
            
            opcionRepository.saveAll(nuevasOpciones);
            pregunta.setOpciones(nuevasOpciones);
            preguntaRepository.save(pregunta);
            
            redirectAttributes.addFlashAttribute("mensaje", "Pregunta actualizada exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar la pregunta: " + e.getMessage());
            return "redirect:/admin/preguntas/editar/" + id;
        }
        
        return "redirect:/admin/preguntas";
    }
    
    /**
     * Elimina (desactiva) una pregunta
     */
    @PostMapping("/eliminar/{id}")
    public String eliminarPregunta(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        if (!esAdministrador(session)) {
            redirectAttributes.addFlashAttribute("error", "Acceso denegado.");
            return "redirect:/login";
        }
        
        try {
            Optional<Pregunta> preguntaOpt = preguntaRepository.findById(id);
            if (preguntaOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Pregunta no encontrada.");
                return "redirect:/admin/preguntas";
            }
            
            Pregunta pregunta = preguntaOpt.get();
            pregunta.setActiva(false);
            preguntaRepository.save(pregunta);
            
            redirectAttributes.addFlashAttribute("mensaje", "Pregunta eliminada (desactivada) exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar la pregunta: " + e.getMessage());
        }
        
        return "redirect:/admin/preguntas";
    }
}

