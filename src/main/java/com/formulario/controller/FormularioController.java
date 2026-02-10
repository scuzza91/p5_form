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
import com.formulario.repository.RecomendacionEstudiosRepository;
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
    
    @Autowired
    private RecomendacionEstudiosRepository recomendacionEstudiosRepository;
    
    // Página principal - Ahora redirige directamente al examen
    @GetMapping("/")
    public String index(Model model) {
        boolean inscripcionesAbiertas = configuracionService.estanInscripcionesAbiertas();
        model.addAttribute("inscripcionesAbiertas", inscripcionesAbiertas);
        return "index";
    }
    
    // Endpoint OPTIONS para manejar preflight CORS
    @RequestMapping(value = "/api/persona/crear", method = RequestMethod.OPTIONS)
    @CrossOrigin(origins = "*")
    public ResponseEntity<?> optionsCrearPersona() {
        logger.info("=== OPTIONS REQUEST RECIBIDO ===");
        return ResponseEntity.ok().build();
    }
    
    // Endpoint GET para mostrar mensaje de error claro (solo POST está permitido)
    @GetMapping("/api/persona/crear")
    @CrossOrigin(origins = "*")
    public ResponseEntity<?> getCrearPersona(HttpServletRequest request) {
        logger.warn("=== INTENTO DE ACCESO CON GET (NO PERMITIDO) ===");
        logger.warn("URI: {}", request.getRequestURI());
        logger.warn("Query String: {}", request.getQueryString());
        logger.warn("Remote Address: {}", request.getRemoteAddr());
        logger.warn("User-Agent: {}", request.getHeader("User-Agent"));
        
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
            .body(Map.of(
                "error", "Method Not Allowed",
                "status", 405,
                "message", "Este endpoint solo acepta peticiones POST. Por favor, usa POST en lugar de GET.",
                "path", "/api/persona/crear",
                "allowedMethods", new String[]{"POST", "OPTIONS"}
            ));
    }
    
    // Endpoint para recibir ID de caso y obtener datos de persona desde Bondarea
    @PostMapping("/api/persona/crear")
    @CrossOrigin(origins = "*")
    public ResponseEntity<?> crearPersonaDesdeApi(
            @RequestBody(required = false) Map<String, Object> requestBody,
            @RequestHeader(value = "X-API-Token", required = false) String apiToken,
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "idCaso", required = false) String idCasoParam,
            @RequestParam(value = "id", required = false) String idParam,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        try {
            // Log para diagnosticar problemas de método HTTP
            logger.info("=== REQUEST RECIBIDO ===");
            logger.info("Método HTTP: {}", request.getMethod());
            logger.info("URI: {}", request.getRequestURI());
            logger.info("Query String: {}", request.getQueryString());
            logger.info("Content-Type: {}", request.getContentType());
            logger.info("Headers: X-API-Token={}, Authorization={}", apiToken != null ? "presente" : "ausente", 
                       authorization != null ? "presente" : "ausente");
            
            // Log del body si está presente
            if (requestBody != null) {
                logger.info("Request Body recibido: {}", requestBody);
                logger.info("Request Body keys: {}", requestBody.keySet());
            } else {
                logger.info("Request Body es null o vacío");
            }
            
            // Obtener idCaso del request - intentar desde query parameters primero, luego body
            String idCaso = null;
            
            // 1. Intentar desde parámetros de query string (prioridad)
            if (idCasoParam != null && !idCasoParam.trim().isEmpty()) {
                idCaso = idCasoParam.trim();
                logger.info("ID de caso obtenido desde query parameter 'idCaso': {}", idCaso);
            } else if (idParam != null && !idParam.trim().isEmpty()) {
                idCaso = idParam.trim();
                logger.info("ID de caso obtenido desde query parameter 'id': {}", idCaso);
            }
            
            // 2. Si no se encontró en query params, intentar desde el body
            if ((idCaso == null || idCaso.trim().isEmpty()) && requestBody != null) {
                // Campos posibles que Bondarea podría enviar
                String[] camposPosibles = {
                    "idCaso", "id", "caseId", "case_id", "idCasoBondarea", 
                    "id_caso", "case", "idStage", "id_stage"
                };
                
                for (String campo : camposPosibles) {
                    Object valor = requestBody.get(campo);
                    if (valor != null) {
                        String valorStr = valor.toString().trim();
                        if (!valorStr.isEmpty()) {
                            idCaso = valorStr;
                            logger.info("ID de caso obtenido desde body campo '{}': {}", campo, idCaso);
                            break;
                        }
                    }
                }
            }
            
            // 3. Si aún no se encontró, buscar en objetos anidados (data.id, data.idCaso, etc.)
            if ((idCaso == null || idCaso.trim().isEmpty()) && requestBody != null) {
                Object dataObj = requestBody.get("data");
                if (dataObj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> data = (Map<String, Object>) dataObj;
                    Object valor = data.get("id");
                    if (valor == null) {
                        valor = data.get("idCaso");
                    }
                    if (valor == null) {
                        valor = data.get("caseId");
                    }
                    if (valor != null) {
                        idCaso = valor.toString().trim();
                        logger.info("ID de caso obtenido desde data.id/data.idCaso: {}", idCaso);
                    }
                }
            }
            
            logger.info("ID de caso final obtenido: {}", idCaso);
            
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
                logger.warn("Query parameters recibidos: idCaso={}, id={}", idCasoParam, idParam);
                
                // Construir mensaje de error más descriptivo
                StringBuilder mensajeError = new StringBuilder();
                mensajeError.append("El ID de caso es requerido. ");
                mensajeError.append("Debe proporcionar el campo 'id' o 'idCaso' en el request body o como query parameter. ");
                if (requestBody != null && !requestBody.isEmpty()) {
                    mensajeError.append("Campos recibidos en body: ").append(requestBody.keySet()).append(". ");
                }
                mensajeError.append("Ejemplos: /api/persona/crear?idCaso=123 o body: {\"idCaso\": \"123\"}");
                
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "El ID de caso es requerido");
                errorResponse.put("mensaje", mensajeError.toString());
                errorResponse.put("camposEsperados", java.util.Arrays.asList("idCaso (body o query param)", "id (body o query param)"));
                errorResponse.put("ejemploQueryParam", "/api/persona/crear?idCaso=123");
                errorResponse.put("ejemploBody", "{\"idCaso\": \"123\"}");
                if (requestBody != null) {
                    errorResponse.put("camposRecibidosEnBody", requestBody.keySet());
                }
                
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
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
                
                // Construir URL del examen usando token hash
                String examenUrl = construirUrlExamen(request, examen);
                logger.info("URL del examen construida (persona existente): {}", examenUrl);
                
                // Verificar si el cliente solicita redirección explícitamente
                String acceptHeader = request.getHeader("Accept");
                String responseType = request.getHeader("X-Response-Type");
                boolean solicitaRedireccion = "redirect".equalsIgnoreCase(responseType) 
                                           || (acceptHeader != null && acceptHeader.contains("text/html"));
                
                // Por defecto, devolver JSON (mejor para APIs/webhooks)
                if (solicitaRedireccion) {
                    logger.info("Devolviendo redirección 302 a: {}", examenUrl);
                    HttpHeaders headers = new HttpHeaders();
                    headers.setLocation(java.net.URI.create(examenUrl));
                    
                    return ResponseEntity.status(HttpStatus.FOUND)
                        .headers(headers)
                        .build();
                }
                
                // Por defecto, devolver JSON con la URL del examen
                logger.info("Devolviendo respuesta JSON con examenUrl: {}", examenUrl);
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "mensaje", "Persona encontrada y examen creado",
                    "personaId", personaExistente.getId(),
                    "examenId", examen.getId(),
                    "examenUrl", examenUrl,
                    "email", email
                ));
            }
            
            // Guardar la persona
            Persona personaGuardada = formularioService.guardarPersona(persona);
            
            // Crear examen
            Examen examen = new Examen(personaGuardada);
            examen = formularioService.guardarExamen(examen);
            
            logger.info("Persona y examen creados exitosamente - Persona ID: {}, Examen ID: {}, Email: {}", 
                       personaGuardada.getId(), examen.getId(), personaGuardada.getEmail());
            
            // Construir URL del examen usando token hash
            String examenUrl = construirUrlExamen(request, examen);
            logger.info("URL del examen construida: {}", examenUrl);
            
            // Verificar si el cliente solicita redirección explícitamente
            String acceptHeader = request.getHeader("Accept");
            String responseType = request.getHeader("X-Response-Type");
            boolean solicitaRedireccion = "redirect".equalsIgnoreCase(responseType) 
                                       || (acceptHeader != null && acceptHeader.contains("text/html"));
            
            logger.info("Accept header: {}, X-Response-Type: {}, Solicita redirección: {}", 
                       acceptHeader, responseType, solicitaRedireccion);
            
            // Por defecto, devolver JSON (mejor para APIs/webhooks que no siguen redirecciones)
            // Solo redirigir si se solicita explícitamente
            if (solicitaRedireccion) {
                logger.info("Devolviendo redirección 302 a: {}", examenUrl);
                HttpHeaders headers = new HttpHeaders();
                headers.setLocation(java.net.URI.create(examenUrl));
                
                return ResponseEntity.status(HttpStatus.FOUND)
                    .headers(headers)
                    .build();
            }
            
            // Por defecto, devolver JSON con la URL del examen
            logger.info("Devolviendo respuesta JSON con examenUrl: {}", examenUrl);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "mensaje", "Persona y examen creados exitosamente",
                "personaId", personaGuardada.getId(),
                "examenId", examen.getId(),
                "examenUrl", examenUrl,
                "email", personaGuardada.getEmail()
            ));
            
        } catch (Exception e) {
            logger.error("Error al crear persona desde API", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al procesar los datos: " + e.getMessage()));
        }
    }
    
    /**
     * Webhook para Bondarea: cuando se elimina un caso en Bondarea, Bondarea puede llamar
     * a este endpoint para eliminar también el examen asociado en la base de datos local.
     * Requiere el mismo token de API que /api/persona/crear (X-API-Token o Authorization: Bearer).
     * Body esperado: { "idCaso": "128276" } (o id, caseId, case_id, idCasoBondarea).
     */
    @PostMapping("/api/bondarea/caso-eliminado")
    @CrossOrigin(origins = "*")
    public ResponseEntity<?> casoEliminadoEnBondarea(
            @RequestBody(required = false) Map<String, Object> requestBody,
            @RequestHeader(value = "X-API-Token", required = false) String apiToken,
            @RequestHeader(value = "Authorization", required = false) String authorization,
            HttpServletRequest request) {
        try {
            // Validar token (mismo criterio que /api/persona/crear)
            String token = apiToken;
            if (token == null && authorization != null && authorization.startsWith("Bearer ")) {
                token = authorization.substring(7);
            }
            if (!configuracionService.validarApiToken(token)) {
                logger.warn("Intento de acceso a caso-eliminado con token inválido desde IP: {}", getClientIpAddress(request));
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Token de API inválido o no configurado"));
            }
            // Extraer idCaso del body (mismos nombres que en crearPersonaDesdeApi)
            String idCaso = null;
            if (requestBody != null) {
                for (String key : new String[]{"idCaso", "id", "caseId", "case_id", "idCasoBondarea", "id_caso"}) {
                    Object v = requestBody.get(key);
                    if (v != null && !v.toString().trim().isEmpty()) {
                        idCaso = v.toString().trim();
                        break;
                    }
                }
                if (idCaso == null && requestBody.get("data") instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> data = (Map<String, Object>) requestBody.get("data");
                    Object v = data.get("id");
                    if (v == null) v = data.get("idCaso");
                    if (v != null) idCaso = v.toString().trim();
                }
            }
            if (idCaso == null || idCaso.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Falta el identificador del caso", "mensaje", "Incluir idCaso (o id, caseId) en el body"));
            }
            formularioService.eliminarExamenPorIdCasoBondarea(idCaso);
            logger.info("Examen eliminado localmente por caso Bondarea eliminado: idCaso={}", idCaso);
            return ResponseEntity.ok(Map.of(
                "ok", true,
                "mensaje", "Examen asociado al caso eliminado en la base de datos local",
                "idCaso", idCaso
            ));
        } catch (Exception e) {
            logger.error("Error al procesar caso eliminado desde Bondarea", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al procesar: " + e.getMessage()));
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
        
        // Si viene examenId, redirigir directamente al examen usando token hash
        if (examenId != null) {
            Optional<Examen> examenOpt = formularioService.buscarExamenPorId(examenId);
            if (examenOpt.isPresent()) {
                String token = com.formulario.util.ExamenTokenUtil.generarToken(examenId);
                return "redirect:/examen/" + token;
            }
            // Fallback a ID si no se encuentra el examen
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
            
            // Crear nuevo examen y redirigir al examen múltiple choice usando token hash
            Examen examen = new Examen(persona.get());
            examen = formularioService.guardarExamen(examen);
            
            String token = com.formulario.util.ExamenTokenUtil.generarToken(examen.getId());
            return "redirect:/examen/" + token;
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
                
                // Obtener la recomendación ya seleccionada si existe
                // Las recomendaciones de estudios ahora están incluidas en cada rol
                try {
                    Optional<Persona> personaOpt = formularioService.buscarPersonaPorId(personaId);
                    if (personaOpt.isPresent()) {
                        Optional<Examen> examenOpt = formularioService.buscarExamenPorPersona(personaOpt.get());
                        if (examenOpt.isPresent() && examenOpt.get().getRecomendacionEstudiosSeleccionada() != null) {
                            Long recomendacionSeleccionadaId = examenOpt.get().getRecomendacionEstudiosSeleccionada().getId();
                            model.addAttribute("recomendacionSeleccionadaId", recomendacionSeleccionadaId);
                            logger.info("Recomendación ya seleccionada: ID {}", recomendacionSeleccionadaId);
                        }
                    }
                } catch (Exception e) {
                    logger.warn("Error al obtener recomendación seleccionada para persona ID: {} - {}", personaId, e.getMessage());
                }
            } catch (Exception e) {
                logger.warn("Error al cargar recomendaciones para persona ID: {} - {}", personaId, e.getMessage());
                model.addAttribute("recomendaciones", new ArrayList<>());
            }
            
            logger.info("Resultado cargado exitosamente para persona ID: {}", personaId);
            logger.info("=== FIN MOSTRAR RESULTADO ===");
            
            return "resultado";
            
        } catch (Exception e) {
            logger.error("Error al mostrar resultado para persona ID: {}", personaId, e);
            return "redirect:/";
        }
    }
    
    // Nuevo sistema de examen múltiple choice - Acepta token hash o ID para compatibilidad
    @GetMapping("/examen/{identificador}")
    public String mostrarExamen(@PathVariable String identificador, Model model, RedirectAttributes redirectAttributes) {
        try {
            logger.info("Iniciando carga del examen con identificador: {}", identificador);
            
            // Buscar por token hash o ID (compatibilidad con exámenes antiguos)
            Optional<Examen> examenOpt = formularioService.buscarExamenPorTokenOId(identificador);
            
            if (examenOpt.isEmpty()) {
                logger.warn("Examen no encontrado con identificador: {}", identificador);
                redirectAttributes.addFlashAttribute("error", "Examen no encontrado");
                return "redirect:/";
            }
            
            Examen examen = examenOpt.get();
            logger.info("Examen encontrado para persona: {} (ID: {})", 
                       examen.getPersona().getEmail(), examen.getId());
            
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
            logger.error("Error al cargar el examen con identificador: {}", identificador, e);
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al cargar el examen: " + e.getMessage());
            return "redirect:/paso1";
        }
    }
    
    @PostMapping("/examen/finalizar")
    public String finalizarExamen(@RequestParam String examenToken,
                                 @RequestParam String respuestas,
                                 RedirectAttributes redirectAttributes) {
        try {
            logger.info("=== INICIO FINALIZAR EXAMEN ===");
            logger.info("Finalizando examen con token/ID: {}", examenToken);
            logger.info("Respuestas recibidas: {}", respuestas);
            
            if (respuestas == null || respuestas.trim().isEmpty()) {
                logger.error("El parámetro 'respuestas' está vacío o es null");
                redirectAttributes.addFlashAttribute("error", "No se recibieron respuestas del examen");
                return "redirect:/";
            }
            
            // Buscar examen por token hash o ID (compatibilidad)
            Optional<Examen> examenOpt = formularioService.buscarExamenPorTokenOId(examenToken);
            if (examenOpt.isEmpty()) {
                logger.error("Examen no encontrado con token/ID: {}", examenToken);
                redirectAttributes.addFlashAttribute("error", "Examen no encontrado");
                return "redirect:/";
            }
            
            Examen examen = examenOpt.get();
            Long examenId = examen.getId();
            logger.info("Examen encontrado - ID: {}", examenId);
            
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
            examen = examenService.procesarRespuestas(examenId, respuestasFinales);
            
            logger.info("Examen procesado exitosamente para persona: {}", examen.getPersona().getId());
            
            // Actualizar caso en Bondarea si la persona tiene idCaso
            String idCaso = examen.getPersona().getIdCasoBondarea();
            if (idCaso != null && !idCaso.trim().isEmpty()) {
                try {
                    logger.info("Intentando actualizar caso en Bondarea: idCaso={}", idCaso);
                    
                    // Obtener datos de la recomendación de estudios seleccionada por el usuario
                    String nombreInstitucion = null;
                    String nombreCurso = null;
                    String duracion = null;
                    Long idCurso = null;
                    java.math.BigDecimal monto = null;
                    
                    if (examen.getRecomendacionEstudiosSeleccionada() != null) {
                        RecomendacionEstudios recomendacion = examen.getRecomendacionEstudiosSeleccionada();
                        nombreInstitucion = recomendacion.getNombreInstitucion();
                        nombreCurso = recomendacion.getNombreOferta();
                        duracion = recomendacion.getDuracion();
                        idCurso = recomendacion.getId();
                        monto = recomendacion.getCosto();
                        logger.info("Datos de recomendación seleccionada - Institución: {}, Curso: {}, Duración: {}, ID: {}, Monto: {}", 
                                   nombreInstitucion, nombreCurso, duracion, idCurso, monto);
                    } else {
                        logger.warn("No hay recomendación de estudios seleccionada para el examen");
                    }
                    
                    // Construir comentarios con información del examen
                    String comentarios = String.format("Examen completado - Promedio: %.1f%%, Lógica: %d%%, Matemática: %d%%, Creatividad: %d%%, Programación: %d%%",
                        examen.getPromedio(),
                        examen.getLogica() != null ? examen.getLogica() : 0,
                        examen.getMatematica() != null ? examen.getMatematica() : 0,
                        examen.getCreatividad() != null ? examen.getCreatividad() : 0,
                        examen.getProgramacion() != null ? examen.getProgramacion() : 0);
                    
                    // Llamar al método de actualización en Bondarea
                    Map<String, Object> resultadoActualizacion = bondareaService.actualizarCasoEnBondarea(idCaso, examen, nombreInstitucion, nombreCurso, duracion, idCurso, monto, comentarios);
                    Boolean actualizado = (Boolean) resultadoActualizacion.get("success");
                    if (actualizado != null && actualizado) {
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
    
    // Endpoint para guardar la recomendación de estudios seleccionada
    @PostMapping("/api/examen/guardar-recomendacion-estudios")
    @ResponseBody
    @CrossOrigin(origins = "*")
    public ResponseEntity<?> guardarRecomendacionEstudios(@RequestBody Map<String, Object> request) {
        try {
            logger.info("=== INICIO GUARDAR RECOMENDACIÓN ESTUDIOS ===");
            
            Long estudioId = null;
            Long personaId = null;
            
            // Obtener los IDs del request
            if (request.get("estudioId") != null) {
                if (request.get("estudioId") instanceof Integer) {
                    estudioId = ((Integer) request.get("estudioId")).longValue();
                } else if (request.get("estudioId") instanceof Long) {
                    estudioId = (Long) request.get("estudioId");
                } else {
                    estudioId = Long.parseLong(request.get("estudioId").toString());
                }
            }
            
            if (request.get("personaId") != null) {
                if (request.get("personaId") instanceof Integer) {
                    personaId = ((Integer) request.get("personaId")).longValue();
                } else if (request.get("personaId") instanceof Long) {
                    personaId = (Long) request.get("personaId");
                } else {
                    personaId = Long.parseLong(request.get("personaId").toString());
                }
            }
            
            logger.info("Estudio ID: {}, Persona ID: {}", estudioId, personaId);
            
            if (estudioId == null || personaId == null) {
                logger.error("Faltan parámetros requeridos: estudioId={}, personaId={}", estudioId, personaId);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "error", "Faltan parámetros requeridos: estudioId y personaId"));
            }
            
            // Buscar el examen de la persona
            Optional<Persona> personaOpt = formularioService.buscarPersonaPorId(personaId);
            if (personaOpt.isEmpty()) {
                logger.error("Persona no encontrada con ID: {}", personaId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "error", "Persona no encontrada"));
            }
            
            Optional<Examen> examenOpt = formularioService.buscarExamenPorPersona(personaOpt.get());
            if (examenOpt.isEmpty()) {
                logger.error("Examen no encontrado para persona ID: {}", personaId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "error", "Examen no encontrado"));
            }
            
            Examen examen = examenOpt.get();
            
            // Verificar si ya hay una recomendación guardada (no permitir cambios)
            if (examen.getRecomendacionEstudiosSeleccionada() != null) {
                logger.warn("Intento de cambiar recomendación ya guardada para examen ID: {}", examen.getId());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "error", "Ya has seleccionado una recomendación. No puedes cambiar tu selección."));
            }
            
            // Buscar la recomendación de estudios
            Optional<RecomendacionEstudios> recomendacionOpt = recomendacionEstudiosRepository.findById(estudioId);
            if (recomendacionOpt.isEmpty()) {
                logger.error("Recomendación de estudios no encontrada con ID: {}", estudioId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "error", "Recomendación de estudios no encontrada"));
            }
            
            RecomendacionEstudios recomendacion = recomendacionOpt.get();
            
            // Guardar la selección
            examen.setRecomendacionEstudiosSeleccionada(recomendacion);
            formularioService.guardarExamen(examen);
            
            logger.info("✅ Recomendación de estudios guardada exitosamente - Examen ID: {}, Recomendación ID: {}", 
                       examen.getId(), recomendacion.getId());
            
            // Actualizar caso en Bondarea si la persona tiene idCaso
            // Asegurar que la persona esté cargada
            Persona persona = examen.getPersona();
            if (persona == null) {
                logger.warn("El examen no tiene persona asociada, no se puede enviar a Bondarea");
            } else {
                String idCaso = persona.getIdCasoBondarea();
                if (idCaso != null && !idCaso.trim().isEmpty()) {
                    try {
                        logger.info("Intentando actualizar caso en Bondarea con recomendación de estudios: idCaso={}", idCaso);
                        
                        // Obtener datos de la recomendación de estudios seleccionada
                        String nombreInstitucion = recomendacion.getNombreInstitucion();
                        String nombreCurso = recomendacion.getNombreOferta();
                        String duracion = recomendacion.getDuracion();
                        Long idCurso = recomendacion.getId();
                        java.math.BigDecimal monto = recomendacion.getCosto();
                        
                        logger.info("Datos de recomendación a enviar a Bondarea - Institución: {}, Curso: {}, Duración: {}, ID: {}, Monto: {}", 
                                   nombreInstitucion, nombreCurso, duracion, idCurso, monto);
                        
                        // Construir comentarios con información del examen y la recomendación
                        String comentarios = String.format("Recomendación de estudios seleccionada - Institución: %s, Curso: %s, Duración: %s, Monto: %s",
                            nombreInstitucion != null ? nombreInstitucion : "N/A",
                            nombreCurso != null ? nombreCurso : "N/A",
                            duracion != null ? duracion : "N/A",
                            monto != null ? monto.toString() : "N/A");
                        
                        // Llamar al método de actualización en Bondarea
                        Map<String, Object> resultadoActualizacion = bondareaService.actualizarCasoEnBondarea(idCaso, examen, nombreInstitucion, nombreCurso, duracion, idCurso, monto, comentarios);
                        Boolean actualizado = (Boolean) resultadoActualizacion.get("success");
                        String trackingPars = (String) resultadoActualizacion.get("trackingPars");
                        
                        if (actualizado != null && actualizado) {
                            logger.info("✅ Caso actualizado exitosamente en Bondarea con recomendación de estudios para idCaso: {}", idCaso);
                            
                            // Construir URL de redirección a Bondarea si tenemos trackingPars
                            if (trackingPars != null && !trackingPars.trim().isEmpty()) {
                                String emailCliente = persona.getEmail();
                                if (emailCliente != null && !emailCliente.trim().isEmpty()) {
                                    // Construir URL: https://argentinatech.bondarea.com/?c=public&ui=-217&originmail=mail@mail.com&caseid=1234&casetoken=abc12345678900123456
                                    String urlBondarea = construirUrlRedireccionBondarea(emailCliente, trackingPars);
                                    logger.info("URL de redirección a Bondarea construida: {}", urlBondarea);
                                    
                                    logger.info("=== FIN GUARDAR RECOMENDACIÓN ESTUDIOS ===");
                                    
                                    return ResponseEntity.ok(Map.of(
                                        "success", true,
                                        "message", "Recomendación guardada exitosamente",
                                        "examenId", examen.getId(),
                                        "recomendacionId", recomendacion.getId(),
                                        "recomendacionNombre", recomendacion.getNombreOferta(),
                                        "redirectUrl", urlBondarea
                                    ));
                                } else {
                                    logger.warn("Email del cliente no disponible, no se puede construir URL de redirección");
                                }
                            } else {
                                logger.warn("trackingPars no disponible en la respuesta de Bondarea");
                            }
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
            }
            
            logger.info("=== FIN GUARDAR RECOMENDACIÓN ESTUDIOS ===");
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Recomendación guardada exitosamente",
                "examenId", examen.getId(),
                "recomendacionId", recomendacion.getId(),
                "recomendacionNombre", recomendacion.getNombreOferta()
            ));
            
        } catch (Exception e) {
            logger.error("Error al guardar recomendación de estudios", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", "Error al guardar la recomendación: " + e.getMessage()));
        }
    }
    
    // Endpoint para obtener la recomendación de estudios seleccionada por un usuario
    @GetMapping("/api/examen/recomendacion-estudios/{examenId}")
    @ResponseBody
    @CrossOrigin(origins = "*")
    public ResponseEntity<?> obtenerRecomendacionEstudios(@PathVariable Long examenId) {
        try {
            logger.info("Consultando recomendación de estudios para examen ID: {}", examenId);
            
            // Buscar el examen
            Optional<Examen> examenOpt = formularioService.buscarExamenPorId(examenId);
            if (examenOpt.isEmpty()) {
                logger.warn("Examen no encontrado con ID: {}", examenId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "error", "Examen no encontrado"));
            }
            
            Examen examen = examenOpt.get();
            
            // Verificar si tiene una recomendación de estudios seleccionada
            if (examen.getRecomendacionEstudiosSeleccionada() == null) {
                logger.info("El examen ID {} no tiene recomendación de estudios seleccionada", examenId);
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "recomendacion", null,
                    "message", "El usuario no ha seleccionado una recomendación de estudios"
                ));
            }
            
            // Convertir la recomendación a DTO
            RecomendacionEstudios recomendacion = examen.getRecomendacionEstudiosSeleccionada();
            
            // Forzar la carga de las posiciones laborales (relación LAZY)
            if (recomendacion.getPosicionesLaborales() != null) {
                recomendacion.getPosicionesLaborales().size(); // Esto fuerza la carga
            }
            
            RecomendacionEstudiosDTO recomendacionDTO = new RecomendacionEstudiosDTO(recomendacion);
            
            logger.info("Recomendación de estudios encontrada: ID {}, Nombre: {}, Posiciones: {}", 
                       recomendacion.getId(), recomendacion.getNombreOferta(),
                       recomendacion.getPosicionesLaborales() != null ? recomendacion.getPosicionesLaborales().size() : 0);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "recomendacion", recomendacionDTO,
                "message", "Recomendación de estudios obtenida exitosamente"
            ));
            
        } catch (Exception e) {
            logger.error("Error al obtener recomendación de estudios para examen ID: {}", examenId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", "Error al obtener la recomendación: " + e.getMessage()));
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
    
    // Método auxiliar para construir la URL de redirección a Bondarea
    private String construirUrlRedireccionBondarea(String emailCliente, String trackingPars) {
        // URL base fija
        String urlBase = "https://argentinatech.bondarea.com/?c=public&ui=-217";
        
        // Agregar email del cliente
        String url = urlBase + "&originmail=" + java.net.URLEncoder.encode(emailCliente, java.nio.charset.StandardCharsets.UTF_8);
        
        // Agregar trackingPars (ya viene con &caseid=...&casetoken=...)
        if (trackingPars != null && !trackingPars.trim().isEmpty()) {
            // Si trackingPars no empieza con &, agregarlo
            if (!trackingPars.startsWith("&")) {
                url += "&";
            }
            url += trackingPars;
        }
        
        logger.info("URL de redirección a Bondarea construida: {}", url);
        return url;
    }
    
    // Método auxiliar para construir la URL completa del examen usando token hash
    private String construirUrlExamen(HttpServletRequest request, Examen examen) {
        // Intentar obtener el host desde headers (útil cuando hay proxy/load balancer)
        String host = request.getHeader("Host");
        if (host == null || host.isEmpty()) {
            host = request.getServerName();
            int serverPort = request.getServerPort();
            if ((request.getScheme().equals("http") && serverPort != 80) || 
                (request.getScheme().equals("https") && serverPort != 443)) {
                host = host + ":" + serverPort;
            }
        }
        
        // Determinar el scheme (http o https)
        String scheme = request.getScheme();
        // Si hay un header X-Forwarded-Proto, usarlo (útil con proxy/load balancer)
        String forwardedProto = request.getHeader("X-Forwarded-Proto");
        if (forwardedProto != null && !forwardedProto.isEmpty()) {
            scheme = forwardedProto;
        }
        
        String contextPath = request.getContextPath(); // contexto de la aplicación (puede estar vacío)
        
        // Construir la URL completa
        StringBuilder url = new StringBuilder();
        url.append(scheme).append("://").append(host);
        
        // Agregar contexto si existe
        if (contextPath != null && !contextPath.isEmpty()) {
            url.append(contextPath);
        }
        
        // Generar token hash para el examen
        String token = com.formulario.util.ExamenTokenUtil.generarToken(examen.getId());
        
        // Agregar ruta del examen con token
        url.append("/examen/").append(token);
        
        String finalUrl = url.toString();
        logger.info("URL construida - Scheme: {}, Host: {}, ContextPath: {}, ExamenId: {}, Token: {}, URL final: {}", 
                   scheme, host, contextPath, examen.getId(), token, finalUrl);
        
        return finalUrl;
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