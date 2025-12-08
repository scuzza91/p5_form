package com.formulario.controller;

import com.formulario.model.Examen;
import com.formulario.model.Pregunta;
import com.formulario.model.Localidad;
import com.formulario.model.LocalidadDTO;
import com.formulario.model.Provincia;
import com.formulario.service.FormularioService;
import com.formulario.service.LocalidadService;
import com.formulario.service.ExamenService;
import com.formulario.repository.ExamenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.ArrayList;
import java.util.stream.Collectors;
import com.formulario.model.RespuestaExamen;
import com.formulario.model.Opcion;
import java.util.Objects;
import com.formulario.model.InscripcionDTO;
import com.formulario.model.Persona;
import com.formulario.repository.PersonaRepository;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ApiController {
    
    private static final Logger logger = LoggerFactory.getLogger(ApiController.class);
    
    @Autowired
    private FormularioService formularioService;
    
    @Autowired
    private LocalidadService localidadService;
    
    @Autowired
    private ExamenService examenService;
    
    @Autowired
    private ExamenRepository examenRepository;
    
    @Autowired
    private PersonaRepository personaRepository;
    
    // Endpoint de prueba simple
    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> testEndpoint() {
        logger.info("Test endpoint llamado");
        Map<String, String> response = new HashMap<>();
        response.put("message", "API funcionando correctamente");
        response.put("status", "success");
        return ResponseEntity.ok(response);
    }
    
    // Endpoint para obtener localidades por provincia
    @GetMapping("/localidades/{provinciaId}")
    public ResponseEntity<?> obtenerLocalidadesPorProvincia(@PathVariable Long provinciaId) {
        logger.info("Solicitud de localidades para provincia ID: {}", provinciaId);
        try {
            List<Localidad> localidades = localidadService.obtenerLocalidadesPorProvincia(provinciaId);
            if (localidades == null || localidades.isEmpty()) {
                logger.warn("No se encontraron localidades para provincia ID: {}", provinciaId);
                Map<String, String> error = new HashMap<>();
                error.put("error", "No se encontraron localidades para la provincia seleccionada");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            List<LocalidadDTO> dtos = localidades.stream()
                    .map(localidad -> new LocalidadDTO(localidad.getId(), localidad.getNombre()))
                    .collect(Collectors.toList());
            
            logger.info("Localidades encontradas: {} para provincia ID: {}", dtos.size(), provinciaId);
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            logger.error("Error al obtener localidades", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error interno del servidor");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    // Endpoint para obtener todas las provincias
    @GetMapping("/provincias")
    public ResponseEntity<List<Provincia>> obtenerTodasLasProvincias() {
        logger.info("Solicitud de todas las provincias");
        try {
            List<Provincia> provincias = localidadService.obtenerTodasLasProvincias();
            return ResponseEntity.ok(provincias);
        } catch (Exception e) {
            logger.error("Error al obtener provincias", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Endpoint para obtener preguntas del examen por token hash o ID (compatibilidad)
    @GetMapping("/examen/{identificador}/preguntas")
    public ResponseEntity<?> obtenerPreguntasExamen(@PathVariable String identificador) {
        try {
            logger.info("Solicitando preguntas para examen con identificador: {}", identificador);
            
            // Buscar por token hash o ID (compatibilidad con exámenes antiguos)
            Optional<Examen> examenOpt = formularioService.buscarExamenPorTokenOId(identificador);
            if (examenOpt.isEmpty()) {
                logger.warn("Examen no encontrado con identificador: {}", identificador);
                Map<String, String> error = new HashMap<>();
                error.put("error", "Examen no encontrado");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            Examen examen = examenOpt.get();
            logger.info("Examen encontrado para persona: {} (ID: {})", 
                       examen.getPersona().getEmail(), examen.getId());
            
            if (examen.getFechaFin() != null) {
                logger.warn("Examen ya completado: {}", identificador);
                Map<String, String> error = new HashMap<>();
                error.put("error", "Examen ya completado");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            List<Pregunta> preguntas = examenService.generarPreguntasExamen();
            logger.info("Preguntas generadas para examen {}: {}", identificador, preguntas.size());
            
            // Crear una lista de preguntas simplificada para evitar problemas de serialización
            List<Map<String, Object>> preguntasSimplificadas = new ArrayList<>();
            for (Pregunta pregunta : preguntas) {
                Map<String, Object> preguntaMap = new HashMap<>();
                preguntaMap.put("id", pregunta.getId());
                preguntaMap.put("enunciado", pregunta.getEnunciado());
                preguntaMap.put("areaConocimiento", pregunta.getAreaConocimiento());
                preguntaMap.put("opcionCorrecta", pregunta.getOpcionCorrecta());
                
                // Agregar opciones (ordenadas por el campo orden)
                List<Map<String, Object>> opcionesList = new ArrayList<>();
                if (pregunta.getOpciones() != null) {
                    // Ordenar opciones por el campo orden antes de agregarlas
                    List<Opcion> opcionesOrdenadas = pregunta.getOpciones().stream()
                        .sorted((o1, o2) -> Integer.compare(
                            o1.getOrden() != null ? o1.getOrden() : 0,
                            o2.getOrden() != null ? o2.getOrden() : 0
                        ))
                        .collect(Collectors.toList());
                    
                    for (var opcion : opcionesOrdenadas) {
                        Map<String, Object> opcionMap = new HashMap<>();
                        opcionMap.put("id", opcion.getId());
                        opcionMap.put("texto", opcion.getTexto());
                        opcionMap.put("orden", opcion.getOrden());
                        opcionesList.add(opcionMap);
                    }
                }
                preguntaMap.put("opciones", opcionesList);
                preguntasSimplificadas.add(preguntaMap);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("examenId", examen.getId());
            response.put("preguntas", preguntasSimplificadas);
            response.put("totalPreguntas", preguntasSimplificadas.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error al obtener preguntas para examen {}", identificador, e);
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error interno del servidor: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    // Endpoint de debug para verificar datos
    @GetMapping("/debug")
    public ResponseEntity<String> debug() {
        try {
            List<Provincia> provincias = localidadService.obtenerTodasLasProvincias();
            StringBuilder result = new StringBuilder();
            result.append("Provincias encontradas: ").append(provincias.size()).append("\n");
            
            for (Provincia provincia : provincias) {
                result.append("Provincia: ").append(provincia.getNombre()).append(" (ID: ").append(provincia.getId()).append(")\n");
                List<Localidad> localidades = localidadService.obtenerLocalidadesPorProvincia(provincia.getId());
                result.append("  Localidades: ").append(localidades.size()).append("\n");
                for (Localidad localidad : localidades) {
                    result.append("    - ").append(localidad.getNombre()).append("\n");
                }
            }
            
            return ResponseEntity.ok(result.toString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }
    
    // Endpoint de debug para verificar preguntas
    @GetMapping("/debug/preguntas")
    public ResponseEntity<String> debugPreguntas() {
        try {
            List<Pregunta> preguntas = examenService.generarPreguntasExamen();
            StringBuilder result = new StringBuilder();
            result.append("Total de preguntas generadas: ").append(preguntas.size()).append("\n\n");
            
            for (int i = 0; i < Math.min(preguntas.size(), 5); i++) {
                Pregunta pregunta = preguntas.get(i);
                result.append("Pregunta ").append(i + 1).append(":\n");
                result.append("  ID: ").append(pregunta.getId()).append("\n");
                result.append("  Área: ").append(pregunta.getAreaConocimiento()).append("\n");
                result.append("  Enunciado: ").append(pregunta.getEnunciado()).append("\n");
                result.append("  Opciones: ").append(pregunta.getOpciones() != null ? pregunta.getOpciones().size() : 0).append("\n");
                if (pregunta.getOpciones() != null) {
                    for (var opcion : pregunta.getOpciones()) {
                        result.append("    - ").append(opcion.getOrden()).append(": ").append(opcion.getTexto()).append("\n");
                    }
                }
                result.append("\n");
            }
            
            return ResponseEntity.ok(result.toString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al generar preguntas: " + e.getMessage());
        }
    }
    
    // Endpoint de debug para verificar examenes
    @GetMapping("/debug/examenes")
    public ResponseEntity<String> debugExamenes() {
        try {
            List<Examen> examenes = examenRepository.findAll();
            StringBuilder result = new StringBuilder();
            result.append("Total de examenes: ").append(examenes.size()).append("\n\n");
            
            for (Examen examen : examenes) {
                result.append("Examen ID: ").append(examen.getId()).append("\n");
                result.append("  Persona: ").append(examen.getPersona() != null ? examen.getPersona().getEmail() : "NULL").append("\n");
                result.append("  Fecha inicio: ").append(examen.getFechaInicio()).append("\n");
                result.append("  Fecha fin: ").append(examen.getFechaFin()).append("\n");
                result.append("  Completado: ").append(examen.getFechaFin() != null ? "SÍ" : "NO").append("\n");
                result.append("  Programación Básica: ").append(examen.getProgramacionBasica()).append("\n");
                result.append("  Estructuras Datos: ").append(examen.getEstructurasDatos()).append("\n");
                result.append("  Algoritmos: ").append(examen.getAlgoritmos()).append("\n");
                result.append("  Base Datos: ").append(examen.getBaseDatos()).append("\n");
                result.append("  Promedio: ").append(examen.getPromedio()).append("\n");
                result.append("  Total Preguntas: ").append(examen.getTotalPreguntas()).append("\n");
                result.append("  Respuestas Correctas: ").append(examen.getRespuestasCorrectas()).append("\n");
                
                // Verificar respuestas
                if (examen.getRespuestas() != null) {
                    result.append("  Respuestas cargadas: ").append(examen.getRespuestas().size()).append("\n");
                } else {
                    result.append("  Respuestas: NULL\n");
                }
                result.append("\n");
            }
            
            return ResponseEntity.ok(result.toString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/debug/inscripciones")
    public ResponseEntity<String> debugInscripciones() {
        try {
            List<InscripcionDTO> inscripciones = formularioService.obtenerTodasLasInscripciones();
            StringBuilder result = new StringBuilder();
            result.append("Total de inscripciones DTO: ").append(inscripciones.size()).append("\n\n");
            
            for (InscripcionDTO inscripcion : inscripciones) {
                result.append("Inscripción ID: ").append(inscripcion.getId()).append("\n");
                result.append("  Nombre: ").append(inscripcion.getNombre()).append(" ").append(inscripcion.getApellido()).append("\n");
                result.append("  Email: ").append(inscripcion.getEmail()).append("\n");
                result.append("  CUIL: ").append(inscripcion.getCuil()).append("\n");
                result.append("  DNI: ").append(inscripcion.getDni()).append("\n");
                result.append("  Programación Básica: ").append(inscripcion.getProgramacionBasica()).append("\n");
                result.append("  Estructuras Datos: ").append(inscripcion.getEstructurasDatos()).append("\n");
                result.append("  Algoritmos: ").append(inscripcion.getAlgoritmos()).append("\n");
                result.append("  Base Datos: ").append(inscripcion.getBaseDatos()).append("\n");
                result.append("  Promedio: ").append(inscripcion.getPromedio()).append("\n");
                result.append("  Promedio Calculado: ").append(inscripcion.getPromedioCalculado()).append("\n");
                result.append("  Aprobado: ").append(inscripcion.getAprobado()).append("\n");
                result.append("  Fecha Examen: ").append(inscripcion.getFechaExamen()).append("\n");
                result.append("\n");
            }
            
            return ResponseEntity.ok(result.toString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/debug/email/{email}")
    public ResponseEntity<String> debugEmail(@PathVariable String email) {
        try {
            StringBuilder result = new StringBuilder();
            result.append("Buscando email: ").append(email).append("\n\n");
            
            // Buscar persona por email
            Persona persona = formularioService.buscarPersonaPorEmail(email);
            if (persona != null) {
                result.append("Persona encontrada:\n");
                result.append("  ID: ").append(persona.getId()).append("\n");
                result.append("  Nombre: ").append(persona.getNombre()).append(" ").append(persona.getApellido()).append("\n");
                result.append("  Email: ").append(persona.getEmail()).append("\n");
                result.append("  CUIL: ").append(persona.getCuil()).append("\n");
                
                // Buscar examen para esta persona
                Optional<Examen> examenOpt = formularioService.buscarExamenPorPersona(persona);
                if (examenOpt.isPresent()) {
                    Examen examen = examenOpt.get();
                    result.append("\nExamen encontrado:\n");
                    result.append("  ID: ").append(examen.getId()).append("\n");
                    result.append("  Fecha inicio: ").append(examen.getFechaInicio()).append("\n");
                    result.append("  Fecha fin: ").append(examen.getFechaFin()).append("\n");
                    result.append("  Completado: ").append(examen.getFechaFin() != null ? "SÍ" : "NO").append("\n");
                    result.append("  Programación Básica: ").append(examen.getProgramacionBasica()).append("\n");
                    result.append("  Estructuras Datos: ").append(examen.getEstructurasDatos()).append("\n");
                    result.append("  Algoritmos: ").append(examen.getAlgoritmos()).append("\n");
                    result.append("  Base Datos: ").append(examen.getBaseDatos()).append("\n");
                    result.append("  Promedio: ").append(examen.getPromedio()).append("\n");
                    result.append("  Aprobado: ").append(examen.isAprobado()).append("\n");
                    
                    if (examen.getRespuestas() != null) {
                        result.append("  Respuestas: ").append(examen.getRespuestas().size()).append("\n");
                    } else {
                        result.append("  Respuestas: NULL\n");
                    }
                } else {
                    result.append("\nNo se encontró examen para esta persona\n");
                }
            } else {
                result.append("No se encontró persona con este email\n");
            }
            
            return ResponseEntity.ok(result.toString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
    
    // Endpoint de debug para verificar respuestas específicas
    @GetMapping("/debug/respuestas/{examenId}")
    public ResponseEntity<String> debugRespuestas(@PathVariable Long examenId) {
        try {
            Optional<Examen> examenOpt = examenRepository.findById(examenId);
            if (examenOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Examen no encontrado");
            }
            
            Examen examen = examenOpt.get();
            StringBuilder result = new StringBuilder();
            result.append("Examen ID: ").append(examenId).append("\n");
            result.append("Persona: ").append(examen.getPersona().getEmail()).append("\n");
            result.append("Completado: ").append(examen.getFechaFin() != null ? "SÍ" : "NO").append("\n\n");
            
            if (examen.getRespuestas() != null && !examen.getRespuestas().isEmpty()) {
                result.append("Respuestas detalladas:\n");
                for (RespuestaExamen respuesta : examen.getRespuestas()) {
                    Pregunta pregunta = respuesta.getPregunta();
                    result.append("Pregunta ID: ").append(pregunta.getId()).append("\n");
                    result.append("  Enunciado: ").append(pregunta.getEnunciado()).append("\n");
                    result.append("  Área: ").append(pregunta.getAreaConocimiento()).append("\n");
                    result.append("  Respuesta seleccionada: ").append(respuesta.getRespuestaSeleccionada()).append("\n");
                    result.append("  Opción correcta: ").append(pregunta.getOpcionCorrecta()).append("\n");
                    result.append("  Es correcta: ").append(respuesta.isEsCorrecta()).append("\n");
                    
                    if (pregunta.getOpciones() != null) {
                        result.append("  Opciones disponibles:\n");
                        for (Opcion opcion : pregunta.getOpciones()) {
                            result.append("    ").append(opcion.getOrden()).append(": ").append(opcion.getTexto()).append("\n");
                        }
                    }
                    result.append("\n");
                }
            } else {
                result.append("No hay respuestas guardadas para este examen.\n");
            }
            
            return ResponseEntity.ok(result.toString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }
    
    // Endpoint para recalcular puntuaciones de todos los exámenes
    @PostMapping("/recalcular-puntuaciones")
    public ResponseEntity<String> recalcularPuntuaciones() {
        try {
            List<Examen> examenes = examenRepository.findAll();
            StringBuilder result = new StringBuilder();
            result.append("Recalculando puntuaciones para ").append(examenes.size()).append(" exámenes...\n\n");
            
            int examenesActualizados = 0;
            for (Examen examen : examenes) {
                if (examen.getFechaFin() != null && examen.getRespuestas() != null && !examen.getRespuestas().isEmpty()) {
                    // Forzar la carga de las respuestas
                    examen.getRespuestas().size();
                    
                    // Guardar puntuaciones originales para comparar
                    Integer pbOriginal = examen.getProgramacionBasica();
                    Integer edOriginal = examen.getEstructurasDatos();
                    Integer algOriginal = examen.getAlgoritmos();
                    Integer bdOriginal = examen.getBaseDatos();
                    
                    // Recalcular puntuaciones
                    examen.calcularPuntuaciones();
                    
                    // Verificar si hubo cambios
                    if (!Objects.equals(pbOriginal, examen.getProgramacionBasica()) ||
                        !Objects.equals(edOriginal, examen.getEstructurasDatos()) ||
                        !Objects.equals(algOriginal, examen.getAlgoritmos()) ||
                        !Objects.equals(bdOriginal, examen.getBaseDatos())) {
                        
                        examenRepository.save(examen);
                        examenesActualizados++;
                        
                        result.append("Examen ID ").append(examen.getId()).append(" (").append(examen.getPersona().getEmail()).append(") actualizado:\n");
                        result.append("  PB: ").append(pbOriginal).append(" -> ").append(examen.getProgramacionBasica()).append("\n");
                        result.append("  ED: ").append(edOriginal).append(" -> ").append(examen.getEstructurasDatos()).append("\n");
                        result.append("  ALG: ").append(algOriginal).append(" -> ").append(examen.getAlgoritmos()).append("\n");
                        result.append("  BD: ").append(bdOriginal).append(" -> ").append(examen.getBaseDatos()).append("\n");
                        result.append("  Promedio: ").append(examen.getPromedio()).append("\n");
                        result.append("  Aprobado: ").append(examen.isAprobado()).append("\n\n");
                    }
                }
            }
            
            result.append("Proceso completado. ").append(examenesActualizados).append(" exámenes actualizados.");
            return ResponseEntity.ok(result.toString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/test-debug")
    public ResponseEntity<String> testDebug() {
        try {
            StringBuilder result = new StringBuilder();
            result.append("=== DEBUG INFO ===\n\n");
            
            // Contar exámenes
            List<Examen> examenes = examenRepository.findAll();
            result.append("Total de exámenes en BD: ").append(examenes.size()).append("\n\n");
            
            // Contar personas
            List<Persona> personas = personaRepository.findAll();
            result.append("Total de personas en BD: ").append(personas.size()).append("\n\n");
            
            // Información básica de cada examen
            for (Examen examen : examenes) {
                result.append("Examen ID: ").append(examen.getId()).append("\n");
                if (examen.getPersona() != null) {
                    result.append("  Persona: ").append(examen.getPersona().getEmail()).append("\n");
                    result.append("  Nombre: ").append(examen.getPersona().getNombre()).append(" ").append(examen.getPersona().getApellido()).append("\n");
                } else {
                    result.append("  Persona: NULL\n");
                }
                result.append("  Fecha fin: ").append(examen.getFechaFin()).append("\n");
                result.append("  Completado: ").append(examen.getFechaFin() != null ? "SÍ" : "NO").append("\n");
                result.append("  Programación: ").append(examen.getProgramacionBasica()).append("\n");
                result.append("  Estructuras: ").append(examen.getEstructurasDatos()).append("\n");
                result.append("  Algoritmos: ").append(examen.getAlgoritmos()).append("\n");
                result.append("  Base Datos: ").append(examen.getBaseDatos()).append("\n");
                result.append("  Promedio: ").append(examen.getPromedio()).append("\n");
                result.append("  Aprobado: ").append(examen.isAprobado()).append("\n");
                result.append("\n");
            }
            
            return ResponseEntity.ok(result.toString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage() + "\n" + e.getStackTrace());
        }
    }

    @GetMapping("/simple-debug")
    public ResponseEntity<String> simpleDebug() {
        try {
            StringBuilder result = new StringBuilder();
            result.append("=== SIMPLE DEBUG ===\n\n");
            
            // Contar personas
            List<Persona> personas = personaRepository.findAll();
            result.append("Personas en BD: ").append(personas.size()).append("\n");
            
            // Contar exámenes
            List<Examen> examenes = examenRepository.findAll();
            result.append("Exámenes en BD: ").append(examenes.size()).append("\n\n");
            
            // Mostrar personas
            result.append("=== PERSONAS ===\n");
            for (Persona persona : personas) {
                result.append("ID: ").append(persona.getId())
                      .append(", Email: ").append(persona.getEmail())
                      .append(", Nombre: ").append(persona.getNombre()).append(" ").append(persona.getApellido())
                      .append("\n");
            }
            
            result.append("\n=== EXÁMENES ===\n");
            for (Examen examen : examenes) {
                result.append("Examen ID: ").append(examen.getId());
                if (examen.getPersona() != null) {
                    result.append(", Persona: ").append(examen.getPersona().getEmail());
                } else {
                    result.append(", Persona: NULL");
                }
                result.append(", Fecha fin: ").append(examen.getFechaFin());
                result.append(", Completado: ").append(examen.getFechaFin() != null ? "SÍ" : "NO");
                result.append("\n");
            }
            
            return ResponseEntity.ok(result.toString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
} 