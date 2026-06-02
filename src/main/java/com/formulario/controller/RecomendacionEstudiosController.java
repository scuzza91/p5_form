package com.formulario.controller;

import com.formulario.model.RecomendacionEstudios;
import com.formulario.model.RecomendacionEstudiosDTO;
import com.formulario.service.ConfiguracionService;
import com.formulario.service.RecomendacionEstudiosService;
import com.formulario.service.RecomendacionService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/recomendaciones-estudios")
@CrossOrigin(origins = "*")
public class RecomendacionEstudiosController {
    
    private static final Logger logger = LoggerFactory.getLogger(RecomendacionEstudiosController.class);
    
    @Autowired
    private RecomendacionEstudiosService recomendacionEstudiosService;

    @Autowired
    private RecomendacionService recomendacionService;

    @Autowired
    private ConfiguracionService configuracionService;

    /** Extrae el token del header X-API-Token o Authorization Bearer */
    private String resolverToken(String apiToken, String authorization) {
        if (apiToken != null) return apiToken;
        if (authorization != null && authorization.startsWith("Bearer ")) return authorization.substring(7);
        return null;
    }
    
    /**
     * Obtiene todas las recomendaciones de estudios activas
     */
    @GetMapping
    public ResponseEntity<List<RecomendacionEstudiosDTO>> obtenerTodas() {
        try {
            List<RecomendacionEstudiosDTO> recomendaciones = recomendacionEstudiosService.obtenerTodas();
            return ResponseEntity.ok(recomendaciones);
        } catch (Exception e) {
            logger.error("Error al obtener recomendaciones de estudios", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Obtiene una recomendación de estudios por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<RecomendacionEstudiosDTO> obtenerPorId(@PathVariable Long id) {
        try {
            Optional<RecomendacionEstudiosDTO> recomendacion = recomendacionEstudiosService.obtenerPorId(id);
            return recomendacion.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("Error al obtener recomendación de estudios con ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Obtiene recomendaciones de estudios vinculadas a una posición laboral
     */
    @GetMapping("/por-posicion/{posicionId}")
    public ResponseEntity<List<RecomendacionEstudiosDTO>> obtenerPorPosicionLaboral(@PathVariable Long posicionId) {
        try {
            List<RecomendacionEstudiosDTO> recomendaciones = recomendacionEstudiosService.obtenerPorPosicionLaboral(posicionId);
            return ResponseEntity.ok(recomendaciones);
        } catch (Exception e) {
            logger.error("Error al obtener recomendaciones de estudios para posición: {}", posicionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Obtiene recomendaciones de estudios para un candidato basadas en sus recomendaciones de puestos
     */
    @GetMapping("/para-candidato/{personaId}")
    public ResponseEntity<List<RecomendacionEstudiosDTO>> obtenerParaCandidato(@PathVariable Long personaId) {
        try {
            List<RecomendacionEstudiosDTO> recomendaciones = recomendacionEstudiosService.obtenerParaCandidato(personaId, recomendacionService);
            return ResponseEntity.ok(recomendaciones);
        } catch (Exception e) {
            logger.error("Error al obtener recomendaciones de estudios para candidato: {}", personaId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Crea una nueva recomendación de estudios desde Bondarea via API.
     * Requiere X-API-Token. Solo gestiona los campos de contenido.
     * Las posicionesLaborales e imagenInstitucion se gestionan desde el panel admin.
     */
    @PostMapping
    public ResponseEntity<?> crear(
            @RequestBody(required = false) Map<String, Object> body,
            @RequestHeader(value = "X-API-Token", required = false) String apiToken,
            @RequestHeader(value = "Authorization", required = false) String authorization,
            HttpServletRequest request) {
        try {
            String token = resolverToken(apiToken, authorization);
            if (!configuracionService.validarApiToken(token)) {
                logger.warn("Intento de crear recomendación con token inválido desde IP: {}", request.getRemoteAddr());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Token de API inválido o no proporcionado"));
            }
            if (body == null || body.get("nombreInstitucion") == null || body.get("nombreOferta") == null || body.get("costo") == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Campos obligatorios: nombreInstitucion, nombreOferta, costo"));
            }
            RecomendacionEstudios creada = recomendacionEstudiosService.crearDesdeApi(body);
            logger.info("Recomendación creada via API - ID: {}, Institución: {}", creada.getId(), creada.getNombreInstitucion());
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "id", creada.getId(),
                "nombreInstitucion", creada.getNombreInstitucion(),
                "nombreOferta", creada.getNombreOferta(),
                "mensaje", "Recomendación creada. Las posiciones laborales se vinculan desde el panel admin."
            ));
        } catch (Exception e) {
            logger.error("Error al crear recomendación de estudios via API", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al crear recomendación: " + e.getMessage()));
        }
    }

    /**
     * Actualiza una recomendación de estudios existente desde Bondarea via API.
     * Requiere X-API-Token. NO toca posicionesLaborales ni imagenInstitucion.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> body,
            @RequestHeader(value = "X-API-Token", required = false) String apiToken,
            @RequestHeader(value = "Authorization", required = false) String authorization,
            HttpServletRequest request) {
        try {
            String token = resolverToken(apiToken, authorization);
            if (!configuracionService.validarApiToken(token)) {
                logger.warn("Intento de actualizar recomendación con token inválido desde IP: {}", request.getRemoteAddr());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Token de API inválido o no proporcionado"));
            }
            if (body == null || body.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "El cuerpo de la solicitud es requerido"));
            }
            RecomendacionEstudios actualizada = recomendacionEstudiosService.actualizarDesdeApi(id, body);
            logger.info("Recomendación actualizada via API - ID: {}", actualizada.getId());
            return ResponseEntity.ok(Map.of(
                "success", true,
                "id", actualizada.getId(),
                "nombreInstitucion", actualizada.getNombreInstitucion(),
                "nombreOferta", actualizada.getNombreOferta(),
                "mensaje", "Recomendación actualizada. Las posiciones laborales se gestionan desde el panel admin."
            ));
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("no encontrada")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
            }
            logger.error("Error al actualizar recomendación ID: {} via API", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al actualizar recomendación: " + e.getMessage()));
        }
    }
    
    /**
     * Elimina (desactiva) una recomendación de estudios
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            boolean eliminada = recomendacionEstudiosService.eliminar(id);
            if (eliminada) {
                return ResponseEntity.ok(Map.of("mensaje", "Recomendación de estudios desactivada correctamente"));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error al eliminar recomendación de estudios con ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al eliminar recomendación de estudios: " + e.getMessage()));
        }
    }
    
    /**
     * Elimina físicamente una recomendación de estudios
     */
    @DeleteMapping("/{id}/fisico")
    public ResponseEntity<?> eliminarFisicamente(@PathVariable Long id) {
        try {
            boolean eliminada = recomendacionEstudiosService.eliminarFisicamente(id);
            if (eliminada) {
                return ResponseEntity.ok(Map.of("mensaje", "Recomendación de estudios eliminada físicamente"));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error al eliminar físicamente recomendación de estudios con ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al eliminar recomendación de estudios: " + e.getMessage()));
        }
    }
    
    /**
     * Busca recomendaciones por nombre de institución
     */
    @GetMapping("/buscar/institucion")
    public ResponseEntity<List<RecomendacionEstudiosDTO>> buscarPorInstitucion(@RequestParam String nombre) {
        try {
            List<RecomendacionEstudiosDTO> recomendaciones = recomendacionEstudiosService.buscarPorInstitucion(nombre);
            return ResponseEntity.ok(recomendaciones);
        } catch (Exception e) {
            logger.error("Error al buscar recomendaciones por institución: {}", nombre, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Busca recomendaciones por nombre de oferta
     */
    @GetMapping("/buscar/oferta")
    public ResponseEntity<List<RecomendacionEstudiosDTO>> buscarPorOferta(@RequestParam String nombre) {
        try {
            List<RecomendacionEstudiosDTO> recomendaciones = recomendacionEstudiosService.buscarPorOferta(nombre);
            return ResponseEntity.ok(recomendaciones);
        } catch (Exception e) {
            logger.error("Error al buscar recomendaciones por oferta: {}", nombre, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Busca recomendaciones por rango de costo
     */
    @GetMapping("/buscar/costo")
    public ResponseEntity<List<RecomendacionEstudiosDTO>> buscarPorRangoCosto(
            @RequestParam BigDecimal costoMin,
            @RequestParam BigDecimal costoMax) {
        try {
            List<RecomendacionEstudiosDTO> recomendaciones = recomendacionEstudiosService.buscarPorRangoCosto(costoMin, costoMax);
            return ResponseEntity.ok(recomendaciones);
        } catch (Exception e) {
            logger.error("Error al buscar recomendaciones por rango de costo", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Obtiene recomendaciones ordenadas por costo ascendente
     */
    @GetMapping("/ordenadas/costo-asc")
    public ResponseEntity<List<RecomendacionEstudiosDTO>> obtenerOrdenadasPorCostoAsc() {
        try {
            List<RecomendacionEstudiosDTO> recomendaciones = recomendacionEstudiosService.obtenerOrdenadasPorCostoAsc();
            return ResponseEntity.ok(recomendaciones);
        } catch (Exception e) {
            logger.error("Error al obtener recomendaciones ordenadas por costo ascendente", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Obtiene recomendaciones ordenadas por costo descendente
     */
    @GetMapping("/ordenadas/costo-desc")
    public ResponseEntity<List<RecomendacionEstudiosDTO>> obtenerOrdenadasPorCostoDesc() {
        try {
            List<RecomendacionEstudiosDTO> recomendaciones = recomendacionEstudiosService.obtenerOrdenadasPorCostoDesc();
            return ResponseEntity.ok(recomendaciones);
        } catch (Exception e) {
            logger.error("Error al obtener recomendaciones ordenadas por costo descendente", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

