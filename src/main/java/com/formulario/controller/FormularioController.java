package com.formulario.controller;

import com.formulario.model.*;
import com.formulario.service.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ArrayList;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import com.formulario.model.ResultadoDTO;
import com.formulario.model.RecomendacionRolDTO;
import com.formulario.model.RecomendacionEstudiosDTO;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class FormularioController {
    private static final Logger logger = LoggerFactory.getLogger(FormularioController.class);
    
    @Autowired
    private FormularioService formularioService;
    
    @Autowired
    private LocalidadService localidadService;
    
    @Autowired
    private ExamenService examenService;
    
    @Autowired
    private ConfiguracionService configuracionService;
    
    @Autowired
    private BondareaService bondareaService;
    
    @Autowired
    private RolProfesionalService rolProfesionalService;
    
    @Autowired
    private RecomendacionEstudiosService recomendacionEstudiosService;
    
    // Página principal - Ahora redirige directamente al examen
    @GetMapping("/")
    public String index(Model model) {
        boolean inscripcionesAbiertas = configuracionService.estanInscripcionesAbiertas();
        model.addAttribute("inscripcionesAbiertas", inscripcionesAbiertas);
        return "index";
    }
    
    // Endpoint para recibir ID de caso y obtener datos de persona desde Bondarea
    @PostMapping("/api/persona/crear")
    @CrossOrigin(origins = "*")
    public ResponseEntity<?> crearPersonaDesdeApi(
            @RequestBody Map<String, String> requestBody,
            @RequestHeader(value = "X-API-Token", required = false) String apiToken,
            @RequestHeader(value = "Authorization", required = false) String authorization,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        try {
            // Obtener idCaso del request
            String idCaso = requestBody.get("idCaso");
            if (idCaso == null || idCaso.trim().isEmpty()) {
                idCaso = requestBody.get("id"); // Intentar con alias
            }
            
            logger.info("Recibiendo solicitud para crear persona con ID de caso: {}", idCaso);
            
            // Validar token de API
            String token = apiToken;
            if (token == null && authorization != null && authorization.startsWith("Bearer ")) {
                token = authorization.substring(7);
            }
            
            if (!configuracionService.validarApiToken(token)) {
                logger.warn("Intento de acceso con token inválido desde IP: {}", 
                           getClientIpAddress(request));
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Token de API inválido o no proporcionado"));
            }
            
            logger.info("Token de API válido, procesando solicitud");
            
            // Verificar si las inscripciones están abiertas
            if (!configuracionService.estanInscripcionesAbiertas()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Las inscripciones están cerradas actualmente"));
            }
            
            // Validar que el ID del caso es obligatorio
            if (idCaso == null || idCaso.trim().isEmpty()) {
                logger.warn("ID de caso no proporcionado - Campo requerido");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "El ID de caso es requerido", 
                                 "mensaje", "Debe proporcionar el campo 'id' o 'idCaso' en el request"));
            }
            
            // Obtener datos de la persona desde Bondarea
            String idStage = "B26F5NF6"; // ID Stage específico según requerimiento
            logger.info("Obteniendo datos de persona desde Bondarea: idStage={}, idCaso={}", idStage, idCaso);
            
            Map<String, Object> datosBondarea = bondareaService.obtenerSolicitudFinanciamiento(idStage, idCaso);
            
            if (datosBondarea == null || datosBondarea.isEmpty()) {
                logger.warn("No se pudieron obtener datos de Bondarea para idCaso: {}", idCaso);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No se encontraron datos en Bondarea para el ID de caso proporcionado", 
                                 "idCaso", idCaso,
                                 "mensaje", "El ID debe existir en Bondarea usando GET /monitoring/B26F5NF6/{id}"));
            }

           datosBondarea = (Map<String, Object>) datosBondarea.get("data");
            logger.info("✅ Datos obtenidos exitosamente desde Bondarea para idCaso: {}", idCaso);
            // Mapear datos de Bondarea a Persona
            Persona persona = mapearPersonaDesdeBondarea(datosBondarea, idCaso);
            
            // Obtener email para verificar si la persona ya existe
            String email = persona.getEmail();
            
            // Verificar que el email sea válido (ya debería estar validado en mapearPersonaDesdeBondarea)
            if (email == null || email.trim().isEmpty() || !esEmailValido(email)) {
                logger.warn("Email inválido obtenido de Bondarea, usando email generado: {}", email);
                email = "sin-email-" + System.currentTimeMillis() + "@example.com";
                persona.setEmail(email);
            }
            
            // Verificar si el email ya existe
            if (formularioService.existeEmail(email)) {
                Persona personaExistente = formularioService.buscarPersonaPorEmail(email);
                logger.info("Persona ya existe con email: {}, ID: {}", email, personaExistente.getId());
                
                // Actualizar idCaso si no lo tiene
                if (personaExistente.getIdCasoBondarea() == null || personaExistente.getIdCasoBondarea().trim().isEmpty()) {
                    personaExistente.setIdCasoBondarea(idCaso);
                    formularioService.guardarPersona(personaExistente);
                    logger.info("ID de caso de Bondarea actualizado en persona existente: {}", idCaso);
                }
                
                // Si ya existe, verificar si tiene examen
                if (formularioService.existeExamenParaPersona(personaExistente)) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", "Ya existe un examen para esta persona", 
                                     "personaId", personaExistente.getId(),
                                     "email", email));
                }
                
                // Si no tiene examen, crear uno nuevo
                Examen examen = new Examen(personaExistente);
                examen = formularioService.guardarExamen(examen);
                
                // Construir URL del examen
                String examenUrl = construirUrlExamen(request, examen.getId());
                
                // Preparar respuesta con header Location para redirección 302
                HttpHeaders headers = new HttpHeaders();
                headers.setLocation(java.net.URI.create(examenUrl));
                
                return ResponseEntity.status(HttpStatus.FOUND)
                    .headers(headers)
                    .build();
            }
            
            // Guardar la persona
            Persona personaGuardada = formularioService.guardarPersona(persona);
            
            // Crear examen
            Examen examen = new Examen(personaGuardada);
            examen = formularioService.guardarExamen(examen);
            
            logger.info("Persona y examen creados exitosamente - Persona ID: {}, Examen ID: {}, Email: {}", 
                       personaGuardada.getId(), examen.getId(), personaGuardada.getEmail());
            
            // Construir URL del examen
            String examenUrl = construirUrlExamen(request, examen.getId());
            
            // Preparar respuesta con header Location para redirección 302
            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(java.net.URI.create(examenUrl));
            
            return ResponseEntity.status(HttpStatus.FOUND)
                .headers(headers)
                .build();
            
        } catch (Exception e) {
            logger.error("Error al crear persona desde API", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al procesar los datos: " + e.getMessage()));
        }
    }
    
    // Método auxiliar para mapear datos de Bondarea a Persona
    private Persona mapearPersonaDesdeBondarea(Map<String, Object> datosBondarea, String idCaso) {
        Persona persona = new Persona();
        
        logger.info("Datos de Bondarea: {}", datosBondarea);
        
        // Guardar el idCaso de Bondarea
        if (idCaso != null && !idCaso.trim().isEmpty()) {
            persona.setIdCasoBondarea(idCaso);
            logger.info("ID de caso de Bondarea guardado: {}", idCaso);
        }
        // Mapear campos desde Bondarea
        // custom_B26FNN8U = Nombre
        String nombre = obtenerValorString(datosBondarea, "custom_B26FNN8U");
        if (nombre == null || nombre.trim().isEmpty()) {
            nombre = "No especificado";
        }
        persona.setNombre(nombre);
        
        // custom_B26FNN83 = Apellido
        String apellido = obtenerValorString(datosBondarea, "custom_B26FNN83");
        if (apellido == null || apellido.trim().isEmpty()) {
            apellido = "No especificado";
        }
        persona.setApellido(apellido);
        
        // custom_B26FNN87 o custom_B26FNN8P = Email
        String email = obtenerValorString(datosBondarea, "custom_B26FNHKS");
        if (email == null || email.trim().isEmpty() || !esEmailValido(email)) {
            email = obtenerValorString(datosBondarea, "custom_B26FNHKS");
        }
        // Validar y normalizar el email
        if (email == null || email.trim().isEmpty() || !esEmailValido(email)) {
            // Generar email válido único
            email = "sin-email-" + System.currentTimeMillis() + "@example.com";
        } else {
            // Limpiar y normalizar el email
            email = email.trim().toLowerCase();
        }
        persona.setEmail(email);
        
        // custom_B26FNHKS = Documento (DNI)
        String documento = obtenerValorString(datosBondarea, "custom_B26FNN8P");
        if (documento != null && !documento.trim().isEmpty()) {
            // Si el documento tiene 11 dígitos, es CUIL; si tiene menos, es DNI
            documento = documento.replaceAll("[^0-9]", "");
            if (documento.length() == 11) {
                persona.setCuil(documento);
            } else if (documento.length() == 8) {
                // DNI de 8 dígitos - generar CUIL básico
                persona.setCuil(documento + "000");
            } else {
                persona.setCuil(documento);
            }
        } else {
            // Generar CUIL por defecto válido (11 dígitos)
            persona.setCuil("00000000000");
        }
        
        // Asegurar que el CUIL tenga exactamente 11 dígitos
        String cuil = persona.getCuil().replaceAll("[^0-9]", "");
        if (cuil.length() != 11) {
            if (cuil.length() < 11) {
                cuil = String.format("%011d", Long.parseLong(cuil.isEmpty() ? "0" : cuil));
            } else {
                cuil = cuil.substring(0, 11);
            }
            persona.setCuil(cuil);
        }
        
        // Campos requeridos por el modelo pero que no vienen de la API - valores por defecto válidos
        persona.setTelefono("0000000000");
        persona.setFechaNacimiento("1990-01-01");
        persona.setGenero("No especificado");
        persona.setDireccion("No especificada");
        persona.setConocimientosProgramacion("Ninguno");
        persona.setInternetHogar("No");
        persona.setTrabajaActualmente("No");
        persona.setTrabajaSectorIT(null);
        
        // Asignar provincia y localidad por defecto (primera disponible)
        List<Provincia> provincias = localidadService.obtenerTodasLasProvincias();
        if (!provincias.isEmpty()) {
            persona.setProvincia(provincias.get(0));
            List<Localidad> localidades = localidadService.obtenerLocalidadesPorProvincia(provincias.get(0).getId());
            if (!localidades.isEmpty()) {
                persona.setLocalidad(localidades.get(0));
            }
        }
        
        return persona;
    }
    
    // Método auxiliar para obtener valores String de un Map de forma segura
    private String obtenerValorString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        return value.toString().trim();
    }
    
    // Método auxiliar para validar formato de email
    private boolean esEmailValido(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        // Validación básica de formato de email
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return email.matches(emailRegex);
    }
    
    // Paso 1: Formulario de datos personales
    @GetMapping("/paso1")
    public String mostrarPaso1(Model model, RedirectAttributes redirectAttributes) {
        // Verificar si las inscripciones están abiertas
        if (!configuracionService.estanInscripcionesAbiertas()) {
            redirectAttributes.addFlashAttribute("error", "Las inscripciones están cerradas actualmente. Por favor, intente más tarde.");
            return "redirect:/";
        }
        
        model.addAttribute("persona", new Persona());
        model.addAttribute("provincias", localidadService.obtenerTodasLasProvincias());
        return "paso1";
    }
    
    @PostMapping("/paso1")
    public String procesarPaso1(@Valid @ModelAttribute("persona") Persona persona, 
                               BindingResult result, 
                               Model model,
                               RedirectAttributes redirectAttributes) {
        // Verificar si las inscripciones están abiertas
        if (!configuracionService.estanInscripcionesAbiertas()) {
            redirectAttributes.addFlashAttribute("error", "Las inscripciones están cerradas actualmente. Por favor, intente más tarde.");
            return "redirect:/";
        }
        
        if (result.hasErrors()) {
            // Volver a cargar las provincias cuando hay errores
            model.addAttribute("provincias", localidadService.obtenerTodasLasProvincias());
            return "paso1";
        }
        
        // Verificar si el email ya existe
        if (formularioService.existeEmail(persona.getEmail())) {
            result.rejectValue("email", "error.persona", "Este email ya está registrado");
            // Volver a cargar las provincias cuando hay errores
            model.addAttribute("provincias", localidadService.obtenerTodasLasProvincias());
            return "paso1";
        }
        
        // Guardar la persona
        Persona personaGuardada = formularioService.guardarPersona(persona);
        
        // Redirigir al paso 2 con el ID de la persona
        redirectAttributes.addFlashAttribute("personaId", personaGuardada.getId());
        redirectAttributes.addFlashAttribute("mensaje", "Datos personales guardados correctamente");
        
        return "redirect:/paso2";
    }
    
    // Paso 2: Formulario de examen - Ahora se accede directamente con examenId
    @GetMapping("/paso2")
    public String mostrarPaso2(Model model, 
                              @RequestParam(required = false) Long examenId,
                              @ModelAttribute("personaId") Long personaId,
                              RedirectAttributes redirectAttributes) {
        
        // Si viene examenId, redirigir directamente al examen
        if (examenId != null) {
            return "redirect:/examen/" + examenId;
        }
        
        // Si viene personaId (compatibilidad con flujo anterior)
        if (personaId != null) {
            Optional<Persona> persona = formularioService.buscarPersonaPorId(personaId);
            if (persona.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Persona no encontrada");
                return "redirect:/";
            }
            
            // Verificar si ya existe un examen para esta persona
            if (formularioService.existeExamenParaPersona(persona.get())) {
                redirectAttributes.addFlashAttribute("error", "Ya existe un examen para esta persona");
                return "redirect:/resultado/" + personaId;
            }
            
            // Crear nuevo examen y redirigir al examen múltiple choice
            Examen examen = new Examen(persona.get());
            examen = formularioService.guardarExamen(examen);
            
            return "redirect:/examen/" + examen.getId();
        }
        
        // Si no viene ningún parámetro, redirigir al inicio
        redirectAttributes.addFlashAttribute("error", "Debe proporcionar un ID de examen o persona");
        return "redirect:/";
    }
    
    // Página de resultado
    @GetMapping("/resultado/{personaId}")
    public String mostrarResultado(@PathVariable Long personaId, Model model) {
        try {
            logger.info("=== INICIO MOSTRAR RESULTADO ===");
            logger.info("Buscando resultado para persona ID: {}", personaId);
            
            // Usar el DTO para evitar problemas de lazy loading
            Optional<ResultadoDTO> resultadoOpt = formularioService.obtenerResultadoDTO(personaId);
            
            if (resultadoOpt.isEmpty()) {
                logger.warn("No se encontró resultado para persona ID: {}", personaId);
                return "redirect:/";
            }
            
            ResultadoDTO resultado = resultadoOpt.get();
            logger.info("Resultado encontrado - Examen ID: {}, Persona: {} {}", 
                       resultado.getExamenId(), resultado.getNombre(), resultado.getApellido());
            
            model.addAttribute("resultado", resultado);
            
            // Cargar recomendaciones directamente en la pantalla de resultado
            try {
                List<RecomendacionRolDTO> recomendaciones = rolProfesionalService.generarRecomendacionesRoles(personaId);
                Map<String, Object> estadisticas = rolProfesionalService.obtenerEstadisticasRecomendacionesRoles(personaId);
                
                model.addAttribute("recomendaciones", recomendaciones);
                model.addAttribute("estadisticas", estadisticas);
                model.addAttribute("personaId", personaId);
                
                logger.info("Recomendaciones cargadas: {} roles para persona ID: {}", recomendaciones.size(), personaId);
                
                // Cargar recomendaciones de estudios
                try {
                    List<RecomendacionEstudiosDTO> recomendacionesEstudios = recomendacionEstudiosService.obtenerTodas();
                    model.addAttribute("recomendacionesEstudios", recomendacionesEstudios);
                    logger.info("Recomendaciones de estudios cargadas: {} para persona ID: {}", recomendacionesEstudios.size(), personaId);
                } catch (Exception e) {
                    logger.warn("Error al cargar recomendaciones de estudios para persona ID: {} - {}", personaId, e.getMessage());
                    model.addAttribute("recomendacionesEstudios", new ArrayList<>());
                }
            } catch (Exception e) {
                logger.warn("Error al cargar recomendaciones para persona ID: {} - {}", personaId, e.getMessage());
                model.addAttribute("recomendaciones", new ArrayList<>());
                model.addAttribute("recomendacionesEstudios", new ArrayList<>());
            }
            
            logger.info("Resultado cargado exitosamente para persona ID: {}", personaId);
            logger.info("=== FIN MOSTRAR RESULTADO ===");
            
            return "resultado";
            
        } catch (Exception e) {
            logger.error("Error al mostrar resultado para persona ID: {}", personaId, e);
            return "redirect:/";
        }
    }
    
    // Nuevo sistema de examen múltiple choice
    @GetMapping("/examen/{examenId}")
    public String mostrarExamen(@PathVariable Long examenId, Model model, RedirectAttributes redirectAttributes) {
        try {
            logger.info("Iniciando carga del examen ID: {}", examenId);
            
            Optional<Examen> examenOpt = formularioService.buscarExamenPorId(examenId);
            
            if (examenOpt.isEmpty()) {
                logger.warn("Examen no encontrado con ID: {}", examenId);
                redirectAttributes.addFlashAttribute("error", "Examen no encontrado");
                return "redirect:/";
            }
            
            Examen examen = examenOpt.get();
            logger.info("Examen encontrado para persona: {}", examen.getPersona().getEmail());
            
            // Verificar si el examen ya fue completado
            if (examen.getFechaFin() != null) {
                logger.info("Examen ya completado para persona: {}", examen.getPersona().getEmail());
                redirectAttributes.addFlashAttribute("error", "Este examen ya fue completado");
                return "redirect:/resultado/" + examen.getPersona().getId();
            }
            
            model.addAttribute("examen", examen);
            model.addAttribute("persona", examen.getPersona());
            
            logger.info("Examen cargado exitosamente");
            logger.info("Redirigiendo a la plantilla examen.html");
            return "examen";
            
        } catch (Exception e) {
            logger.error("Error al cargar el examen ID: {}", examenId, e);
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al cargar el examen: " + e.getMessage());
            return "redirect:/paso1";
        }
    }
    
    @PostMapping("/examen/finalizar")
    public String finalizarExamen(@RequestParam Long examenId,
                                 @RequestParam String respuestas,
                                 RedirectAttributes redirectAttributes) {
        try {
            logger.info("=== INICIO FINALIZAR EXAMEN ===");
            logger.info("Finalizando examen ID: {}", examenId);
            logger.info("Respuestas recibidas: {}", respuestas);
            
            if (respuestas == null || respuestas.trim().isEmpty()) {
                logger.error("El parámetro 'respuestas' está vacío o es null");
                redirectAttributes.addFlashAttribute("error", "No se recibieron respuestas del examen");
                return "redirect:/";
            }
            
            // Parsear las respuestas JSON
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Integer> respuestasMap = mapper.readValue(respuestas, new TypeReference<Map<String, Integer>>() {});
            logger.info("Respuestas parseadas: {}", respuestasMap);
            
            if (respuestasMap.isEmpty()) {
                logger.warn("No hay respuestas para procesar");
                redirectAttributes.addFlashAttribute("error", "No se encontraron respuestas válidas");
                return "redirect:/";
            }
            
            // Convertir a Map<Long, Integer>
            Map<Long, Integer> respuestasFinales = new HashMap<>();
            for (Map.Entry<String, Integer> entry : respuestasMap.entrySet()) {
                Long preguntaId = Long.parseLong(entry.getKey());
                Integer respuesta = entry.getValue();
                respuestasFinales.put(preguntaId, respuesta);
                logger.info("Pregunta ID: {}, Respuesta: {}", preguntaId, respuesta);
            }
            
            logger.info("Respuestas finales: {}", respuestasFinales);
            
            // Procesar el examen
            Examen examen = examenService.procesarRespuestas(examenId, respuestasFinales);
            
            logger.info("Examen procesado exitosamente para persona: {}", examen.getPersona().getId());
            
            // Actualizar caso en Bondarea si la persona tiene idCaso
            String idCaso = examen.getPersona().getIdCasoBondarea();
            if (idCaso != null && !idCaso.trim().isEmpty()) {
                try {
                    logger.info("Intentando actualizar caso en Bondarea: idCaso={}", idCaso);
                    
                    // Obtener nombre de institución de las recomendaciones de estudios (primera recomendación activa)
                    String nombreInstitucion = null;
                    try {
                        List<RecomendacionEstudiosDTO> recomendacionesEstudios = recomendacionEstudiosService.obtenerTodas();
                        if (!recomendacionesEstudios.isEmpty()) {
                            nombreInstitucion = recomendacionesEstudios.get(0).getNombreInstitucion();
                            logger.info("Nombre de institución obtenido: {}", nombreInstitucion);
                        }
                    } catch (Exception e) {
                        logger.warn("No se pudo obtener nombre de institución de recomendaciones: {}", e.getMessage());
                    }
                    
                    // Construir comentarios con información del examen
                    String comentarios = String.format("Examen completado - Promedio: %.1f%%, Lógica: %d%%, Matemática: %d%%, Creatividad: %d%%, Programación: %d%%",
                        examen.getPromedio(),
                        examen.getLogica() != null ? examen.getLogica() : 0,
                        examen.getMatematica() != null ? examen.getMatematica() : 0,
                        examen.getCreatividad() != null ? examen.getCreatividad() : 0,
                        examen.getProgramacion() != null ? examen.getProgramacion() : 0);
                    
                    // Llamar al método de actualización en Bondarea
                    boolean actualizado = bondareaService.actualizarCasoEnBondarea(idCaso, examen, nombreInstitucion, comentarios);
                    if (actualizado) {
                        logger.info("✅ Caso actualizado exitosamente en Bondarea para idCaso: {}", idCaso);
                    } else {
                        logger.warn("⚠️ No se pudo actualizar el caso en Bondarea para idCaso: {}", idCaso);
                    }
                } catch (Exception e) {
                    logger.error("Error al actualizar caso en Bondarea para idCaso: {} - {}", idCaso, e.getMessage(), e);
                    // No interrumpir el flujo si falla la actualización en Bondarea
                }
            } else {
                logger.info("Persona no tiene idCaso de Bondarea, omitiendo actualización");
            }
            
            redirectAttributes.addFlashAttribute("mensaje", "Examen completado correctamente");
            return "redirect:/resultado/" + examen.getPersona().getId();
            
        } catch (Exception e) {
            logger.error("Error al procesar el examen", e);
            redirectAttributes.addFlashAttribute("error", "Error al procesar el examen: " + e.getMessage());
            return "redirect:/";
        }
    }
    
    // Endpoint de debug para verificar el estado del examen
    // Acepta tanto ID numérico (examen local) como String (idStage de Bondarea)
    @GetMapping("/debug/examen/{examenId}")
    @ResponseBody
    public Map<String, Object> debugExamen(@PathVariable String examenId) {
        Map<String, Object> debug = new HashMap<>();
        
        try {
            // Primero intentar buscar como ID numérico en la base de datos local
            Long examenIdLong = null;
            try {
                examenIdLong = Long.parseLong(examenId);
                Optional<Examen> examenOpt = formularioService.buscarExamenPorId(examenIdLong);
                if (examenOpt.isPresent()) {
                    Examen examen = examenOpt.get();
                    debug.put("examenId", examen.getId());
                    debug.put("personaId", examen.getPersona() != null ? examen.getPersona().getId() : null);
                    debug.put("personaNombre", examen.getPersona() != null ? 
                        examen.getPersona().getNombre() + " " + examen.getPersona().getApellido() : null);
                    debug.put("fechaInicio", examen.getFechaInicio());
                    debug.put("fechaFin", examen.getFechaFin());
                    debug.put("programacionBasica", examen.getProgramacionBasica());
                    debug.put("estructurasDatos", examen.getEstructurasDatos());
                    debug.put("algoritmos", examen.getAlgoritmos());
                    debug.put("baseDatos", examen.getBaseDatos());
                    debug.put("promedio", examen.getPromedio());
                    debug.put("aprobado", examen.isAprobado());
                    debug.put("totalPreguntas", examen.getTotalPreguntas());
                    debug.put("respuestasCorrectas", examen.getRespuestasCorrectas());
                    debug.put("respuestasCount", examen.getRespuestas() != null ? examen.getRespuestas().size() : 0);
                    debug.put("status", "OK");
                    debug.put("source", "local");
                    return debug;
                }
            } catch (NumberFormatException e) {
                // No es un número, probablemente es un idStage de Bondarea
                logger.debug("ID no es numérico, intentando como idStage de Bondarea: {}", examenId);
            }
            
            // Si no se encontró localmente, intentar obtener desde Bondarea
            logger.info("Examen no encontrado localmente (ID: {}), consultando API de Bondarea...", examenId);
            
            // Verificar si el token está configurado
            String token = configuracionService.obtenerApiTokenBondarea();
            if (token == null || token.isEmpty()) {
                logger.warn("Token de Bondarea no configurado - No se puede consultar la API");
                debug.put("status", "NOT_FOUND");
                debug.put("message", "Examen no encontrado localmente. Token de Bondarea no configurado para consultar la API.");
                debug.put("idBuscado", examenId);
                debug.put("sugerencia", "Configurar el token de API de Bondarea en /configuracion");
                return debug;
            }
            
            Map<String, Object> datosBondarea = bondareaService.obtenerDatosExamenDesdeBondarea(examenId);
            
            if (datosBondarea != null && !datosBondarea.isEmpty()) {
                debug.putAll(datosBondarea);
                debug.put("status", "OK");
                debug.put("message", "Datos obtenidos desde Bondarea");
                logger.info("✅ Datos obtenidos exitosamente desde Bondarea para ID: {}", examenId);
                return debug;
            } else {
                debug.put("status", "NOT_FOUND");
                debug.put("message", "Examen no encontrado ni localmente ni en Bondarea");
                debug.put("idBuscado", examenId);
                debug.put("tokenConfigurado", token != null && !token.isEmpty());
                debug.put("sugerencia", "Verificar: 1) Token configurado correctamente, 2) URL de API correcta, 3) ID válido en Bondarea. Revisar logs para más detalles.");
                logger.warn("❌ Examen no encontrado ni localmente ni en Bondarea para ID: {}. Token configurado: {}", 
                    examenId, token != null && !token.isEmpty());
            }
            
        } catch (Exception e) {
            logger.error("Error al buscar examen con ID: {}", examenId, e);
            debug.put("status", "ERROR");
            debug.put("message", e.getMessage());
            debug.put("error", e.getClass().getSimpleName());
        }
        
        return debug;
    }
    
    // Endpoint de debug para verificar el resultado completo
    @GetMapping("/debug/resultado/{personaId}")
    @ResponseBody
    public Map<String, Object> debugResultado(@PathVariable Long personaId) {
        Map<String, Object> debug = new HashMap<>();
        
        try {
            Optional<Examen> examenOpt = formularioService.obtenerResultadoCompleto(personaId);
            if (examenOpt.isPresent()) {
                Examen examen = examenOpt.get();
                debug.put("examenId", examen.getId());
                debug.put("personaId", examen.getPersona() != null ? examen.getPersona().getId() : null);
                debug.put("personaNombre", examen.getPersona() != null ? 
                    examen.getPersona().getNombre() + " " + examen.getPersona().getApellido() : null);
                debug.put("personaEmail", examen.getPersona() != null ? examen.getPersona().getEmail() : null);
                debug.put("fechaInicio", examen.getFechaInicio());
                debug.put("fechaFin", examen.getFechaFin());
                debug.put("logica", examen.getLogica());
                debug.put("matematica", examen.getMatematica());
                debug.put("creatividad", examen.getCreatividad());
                debug.put("programacion", examen.getProgramacion());
                debug.put("promedio", examen.getPromedio());
                debug.put("aprobado", examen.isAprobado());
                debug.put("totalPreguntas", examen.getTotalPreguntas());
                debug.put("respuestasCorrectas", examen.getRespuestasCorrectas());
                debug.put("respuestasCount", examen.getRespuestas() != null ? examen.getRespuestas().size() : 0);
                debug.put("status", "OK");
            } else {
                debug.put("status", "NOT_FOUND");
                debug.put("message", "Resultado no encontrado");
            }
        } catch (Exception e) {
            debug.put("status", "ERROR");
            debug.put("message", e.getMessage());
            debug.put("stackTrace", e.getStackTrace());
        }
        
        return debug;
    }
    
    // Lista de todos los registros
    @GetMapping("/lista")
    public String mostrarLista(Model model) {
        List<Persona> personas = formularioService.listarTodasLasPersonas();
        List<Examen> examenes = formularioService.listarTodosLosExamenes();
        
        model.addAttribute("personas", personas);
        model.addAttribute("examenes", examenes);
        
        return "lista";
    }
    
    // Vista de todas las inscripciones con resultados
    @GetMapping("/inscripciones")
    public String mostrarInscripciones(
            @RequestParam(required = false) String dni,
            @RequestParam(required = false) String cuil,
            @RequestParam(required = false) String email,
            Model model) {
        try {
            // Obtener datos reales de la base de datos
            List<InscripcionDTO> inscripciones = formularioService.obtenerTodasLasInscripciones();
            logger.info("Inscripciones obtenidas de BD: {}", inscripciones.size());
            
            // Aplicar filtros si están presentes
            List<InscripcionDTO> inscripcionesFiltradas = inscripciones.stream()
                .filter(inscripcion -> {
                    boolean cumpleFiltros = true;
                    
                    // Filtro por DNI
                    if (dni != null && !dni.trim().isEmpty()) {
                        cumpleFiltros = cumpleFiltros && inscripcion.getDni() != null && 
                                       inscripcion.getDni().contains(dni.trim());
                    }
                    
                    // Filtro por CUIL
                    if (cuil != null && !cuil.trim().isEmpty()) {
                        cumpleFiltros = cumpleFiltros && inscripcion.getCuil() != null && 
                                       inscripcion.getCuil().contains(cuil.trim());
                    }
                    
                    // Filtro por email
                    if (email != null && !email.trim().isEmpty()) {
                        cumpleFiltros = cumpleFiltros && inscripcion.getEmail() != null && 
                                       inscripcion.getEmail().toLowerCase().contains(email.trim().toLowerCase());
                    }
                    
                    return cumpleFiltros;
                })
                .collect(Collectors.toList());
            
            // Calcular promedio general de las inscripciones filtradas
            double promedioGeneral = 0.0;
            if (!inscripcionesFiltradas.isEmpty()) {
                double sumaPromedios = inscripcionesFiltradas.stream()
                    .mapToDouble(i -> i.getPromedio() != null ? i.getPromedio() : 0.0)
                    .sum();
                promedioGeneral = sumaPromedios / inscripcionesFiltradas.size();
                
                // Log para verificar cálculos
                logger.info("Cálculo de promedio general:");
                logger.info("Suma de promedios: {}", sumaPromedios);
                logger.info("Cantidad de inscripciones: {}", inscripcionesFiltradas.size());
                logger.info("Promedio general calculado: {}", promedioGeneral);
                
                // Verificar cálculos individuales
                for (InscripcionDTO inscripcion : inscripcionesFiltradas) {
                    Double promedioCalculado = inscripcion.getPromedioCalculado();
                    logger.info("{} {}: Promedio en DTO={}, Calculado={}", 
                               inscripcion.getNombre(), inscripcion.getApellido(),
                               inscripcion.getPromedio(), promedioCalculado);
                }
            }
            
            model.addAttribute("inscripciones", inscripcionesFiltradas);
            model.addAttribute("promedioGeneral", promedioGeneral);
            
            // Agregar parámetros de filtro para mantener los valores en el formulario
            if (dni != null) model.addAttribute("dni", dni);
            if (cuil != null) model.addAttribute("cuil", cuil);
            if (email != null) model.addAttribute("email", email);
            
            logger.info("Inscripciones filtradas cargadas: {} de {} total", inscripcionesFiltradas.size(), inscripciones.size());
            return "inscripciones";
        } catch (Exception e) {
            logger.error("Error al cargar inscripciones", e);
            model.addAttribute("error", "Error al cargar las inscripciones: " + e.getMessage());
            model.addAttribute("inscripciones", new ArrayList<>());
            model.addAttribute("promedioGeneral", 0.0);
            return "inscripciones";
        }
    }

    // Página de prueba para debug
    @GetMapping("/test-resultado")
    public String testResultado() {
        return "test_resultado";
    }
    
    // Endpoint para descargar Excel con todas las inscripciones
    @GetMapping("/inscripciones/excel")
    public ResponseEntity<byte[]> descargarExcelInscripciones() {
        try {
            byte[] excelContent = formularioService.generarExcelInscripciones();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", "inscripciones_completas.xlsx");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            
            return new ResponseEntity<>(excelContent, headers, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error al generar Excel de inscripciones", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Método auxiliar para construir la URL completa del examen
    private String construirUrlExamen(HttpServletRequest request, Long examenId) {
        String scheme = request.getScheme(); // http o https
        String serverName = request.getServerName(); // dominio o IP
        int serverPort = request.getServerPort(); // puerto
        String contextPath = request.getContextPath(); // contexto de la aplicación (puede estar vacío)
        
        // Construir la URL base
        StringBuilder url = new StringBuilder();
        url.append(scheme).append("://").append(serverName);
        
        // Agregar puerto solo si no es el estándar (80 para http, 443 para https)
        if ((scheme.equals("http") && serverPort != 80) || 
            (scheme.equals("https") && serverPort != 443)) {
            url.append(":").append(serverPort);
        }
        
        // Agregar contexto si existe
        if (contextPath != null && !contextPath.isEmpty()) {
            url.append(contextPath);
        }
        
        // Agregar ruta del examen
        url.append("/examen/").append(examenId);
        
        return url.toString();
    }
    
    // Método auxiliar para obtener la IP del cliente
    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
} 