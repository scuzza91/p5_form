package com.formulario.service;

import com.formulario.model.RecomendacionEstudios;
import com.formulario.model.RecomendacionEstudiosDTO;
import com.formulario.model.PosicionLaboral;
import com.formulario.repository.RecomendacionEstudiosRepository;
import com.formulario.repository.PosicionLaboralRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RecomendacionEstudiosService {
    
    @Autowired
    private RecomendacionEstudiosRepository recomendacionEstudiosRepository;
    
    @Autowired
    private PosicionLaboralRepository posicionLaboralRepository;
    
    /**
     * Obtiene todas las recomendaciones de estudios activas
     */
    @Transactional(readOnly = true)
    public List<RecomendacionEstudiosDTO> obtenerTodas() {
        return recomendacionEstudiosRepository.findByActivaTrue()
                .stream()
                .map(RecomendacionEstudiosDTO::new)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene una recomendación de estudios por ID
     */
    @Transactional(readOnly = true)
    public Optional<RecomendacionEstudiosDTO> obtenerPorId(Long id) {
        return recomendacionEstudiosRepository.findById(id)
                .map(RecomendacionEstudiosDTO::new);
    }
    
    /**
     * Obtiene recomendaciones de estudios vinculadas a una posición laboral
     */
    @Transactional(readOnly = true)
    public List<RecomendacionEstudiosDTO> obtenerPorPosicionLaboral(Long posicionId) {
        return recomendacionEstudiosRepository.findByPosicionLaboralId(posicionId)
                .stream()
                .map(RecomendacionEstudiosDTO::new)
                .collect(Collectors.toList());
    }
    
    /**
     * Crea una nueva recomendación de estudios recibida desde la API de Bondarea.
     * NO asigna posicionesLaborales ni imagenInstitucion — esos campos se gestionan desde el admin.
     */
    @Transactional
    public RecomendacionEstudios crearDesdeApi(Map<String, Object> datos) {
        RecomendacionEstudios rec = new RecomendacionEstudios();
        mapearCamposDesdeApi(rec, datos);
        return recomendacionEstudiosRepository.save(rec);
    }

    /**
     * Actualiza una recomendación existente recibida desde la API de Bondarea.
     * NO toca posicionesLaborales ni imagenInstitucion — esos campos se gestionan desde el admin.
     */
    @Transactional
    public RecomendacionEstudios actualizarDesdeApi(Long id, Map<String, Object> datos) {
        RecomendacionEstudios rec = recomendacionEstudiosRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Recomendación de estudios no encontrada con ID: " + id));
        mapearCamposDesdeApi(rec, datos);
        return recomendacionEstudiosRepository.save(rec);
    }

    /**
     * Mapea los campos enviados por API a la entidad.
     * Solo mapea los campos permitidos por API; ignora posicionesLaborales e imagenInstitucion.
     */
    private void mapearCamposDesdeApi(RecomendacionEstudios rec, Map<String, Object> datos) {
        if (datos.get("nombreInstitucion") != null)
            rec.setNombreInstitucion(datos.get("nombreInstitucion").toString().trim());
        if (datos.get("nombreOferta") != null)
            rec.setNombreOferta(datos.get("nombreOferta").toString().trim());
        if (datos.containsKey("duracion"))
            rec.setDuracion(datos.get("duracion") != null ? datos.get("duracion").toString().trim() : null);
        if (datos.containsKey("descripcion"))
            rec.setDescripcion(datos.get("descripcion") != null ? datos.get("descripcion").toString().trim() : null);
        if (datos.get("costo") != null)
            rec.setCosto(new BigDecimal(datos.get("costo").toString()));
        if (datos.containsKey("gratuita") && datos.get("gratuita") != null)
            rec.setGratuita(Boolean.parseBoolean(datos.get("gratuita").toString()));
        if (datos.containsKey("activa") && datos.get("activa") != null)
            rec.setActiva(Boolean.parseBoolean(datos.get("activa").toString()));
        if (datos.containsKey("recomendacionUniversal") && datos.get("recomendacionUniversal") != null)
            rec.setRecomendacionUniversal(Boolean.parseBoolean(datos.get("recomendacionUniversal").toString()));
    }

    /**
     * Crea una nueva recomendación de estudios
     */
    @Transactional
    public RecomendacionEstudiosDTO crear(RecomendacionEstudiosDTO dto) {
        RecomendacionEstudios recomendacion = new RecomendacionEstudios();
        recomendacion.setNombreInstitucion(dto.getNombreInstitucion());
        recomendacion.setNombreOferta(dto.getNombreOferta());
        recomendacion.setDuracion(dto.getDuracion());
        recomendacion.setImagenInstitucion(dto.getImagenInstitucion());
        recomendacion.setDescripcion(dto.getDescripcion());
        recomendacion.setCosto(dto.getCosto());
        recomendacion.setActiva(dto.isActiva());
        recomendacion.setRecomendacionUniversal(dto.isRecomendacionUniversal());
        recomendacion.setGratuita(dto.isGratuita());
        
        // Vincular posiciones laborales si se proporcionan
        if (dto.getPosicionesLaboralesIds() != null && !dto.getPosicionesLaboralesIds().isEmpty()) {
            for (Long posicionId : dto.getPosicionesLaboralesIds()) {
                Optional<PosicionLaboral> posicionOpt = posicionLaboralRepository.findById(posicionId);
                if (posicionOpt.isPresent()) {
                    recomendacion.agregarPosicionLaboral(posicionOpt.get());
                }
            }
        }
        
        RecomendacionEstudios guardada = recomendacionEstudiosRepository.save(recomendacion);
        return new RecomendacionEstudiosDTO(guardada);
    }
    
    /**
     * Actualiza una recomendación de estudios existente
     */
    @Transactional
    public Optional<RecomendacionEstudiosDTO> actualizar(Long id, RecomendacionEstudiosDTO dto) {
        Optional<RecomendacionEstudios> recomendacionOpt = recomendacionEstudiosRepository.findById(id);
        
        if (recomendacionOpt.isEmpty()) {
            return Optional.empty();
        }
        
        RecomendacionEstudios recomendacion = recomendacionOpt.get();
        recomendacion.setNombreInstitucion(dto.getNombreInstitucion());
        recomendacion.setNombreOferta(dto.getNombreOferta());
        recomendacion.setDuracion(dto.getDuracion());
        recomendacion.setImagenInstitucion(dto.getImagenInstitucion());
        recomendacion.setDescripcion(dto.getDescripcion());
        recomendacion.setCosto(dto.getCosto());
        recomendacion.setActiva(dto.isActiva());
        recomendacion.setRecomendacionUniversal(dto.isRecomendacionUniversal());
        recomendacion.setGratuita(dto.isGratuita());
        
        // Actualizar posiciones laborales
        // Primero, remover todas las relaciones existentes
        List<PosicionLaboral> posicionesActuales = recomendacion.getPosicionesLaborales()
                .stream()
                .collect(Collectors.toList());
        for (PosicionLaboral posicion : posicionesActuales) {
            recomendacion.removerPosicionLaboral(posicion);
        }
        
        // Luego, agregar las nuevas relaciones
        if (dto.getPosicionesLaboralesIds() != null && !dto.getPosicionesLaboralesIds().isEmpty()) {
            for (Long posicionId : dto.getPosicionesLaboralesIds()) {
                Optional<PosicionLaboral> posicionOpt = posicionLaboralRepository.findById(posicionId);
                if (posicionOpt.isPresent()) {
                    recomendacion.agregarPosicionLaboral(posicionOpt.get());
                }
            }
        }
        
        RecomendacionEstudios actualizada = recomendacionEstudiosRepository.save(recomendacion);
        return Optional.of(new RecomendacionEstudiosDTO(actualizada));
    }
    
    /**
     * Elimina (desactiva) una recomendación de estudios
     */
    @Transactional
    public boolean eliminar(Long id) {
        Optional<RecomendacionEstudios> recomendacionOpt = recomendacionEstudiosRepository.findById(id);
        
        if (recomendacionOpt.isEmpty()) {
            return false;
        }
        
        RecomendacionEstudios recomendacion = recomendacionOpt.get();
        recomendacion.setActiva(false);
        recomendacionEstudiosRepository.save(recomendacion);
        return true;
    }
    
    /**
     * Elimina físicamente una recomendación de estudios
     */
    @Transactional
    public boolean eliminarFisicamente(Long id) {
        if (recomendacionEstudiosRepository.existsById(id)) {
            recomendacionEstudiosRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    /**
     * Busca recomendaciones por nombre de institución
     */
    @Transactional(readOnly = true)
    public List<RecomendacionEstudiosDTO> buscarPorInstitucion(String nombreInstitucion) {
        return recomendacionEstudiosRepository.findByNombreInstitucionContainingIgnoreCaseAndActivaTrue(nombreInstitucion)
                .stream()
                .map(RecomendacionEstudiosDTO::new)
                .collect(Collectors.toList());
    }
    
    /**
     * Busca recomendaciones por nombre de oferta
     */
    @Transactional(readOnly = true)
    public List<RecomendacionEstudiosDTO> buscarPorOferta(String nombreOferta) {
        return recomendacionEstudiosRepository.findByNombreOfertaContainingIgnoreCaseAndActivaTrue(nombreOferta)
                .stream()
                .map(RecomendacionEstudiosDTO::new)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene recomendaciones por rango de costo
     */
    @Transactional(readOnly = true)
    public List<RecomendacionEstudiosDTO> buscarPorRangoCosto(java.math.BigDecimal costoMin, java.math.BigDecimal costoMax) {
        return recomendacionEstudiosRepository.findByCostoBetween(costoMin, costoMax)
                .stream()
                .map(RecomendacionEstudiosDTO::new)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene todas las recomendaciones ordenadas por costo ascendente
     */
    @Transactional(readOnly = true)
    public List<RecomendacionEstudiosDTO> obtenerOrdenadasPorCostoAsc() {
        return recomendacionEstudiosRepository.findByActivaTrueOrderByCostoAsc()
                .stream()
                .map(RecomendacionEstudiosDTO::new)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene todas las recomendaciones ordenadas por costo descendente
     */
    @Transactional(readOnly = true)
    public List<RecomendacionEstudiosDTO> obtenerOrdenadasPorCostoDesc() {
        return recomendacionEstudiosRepository.findByActivaTrueOrderByCostoDesc()
                .stream()
                .map(RecomendacionEstudiosDTO::new)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene todas las recomendaciones de estudios marcadas como universales (activas).
     * Se muestran a todos los candidatos sin importar el resultado del test.
     */
    @Transactional(readOnly = true)
    public List<RecomendacionEstudiosDTO> obtenerUniversales() {
        return recomendacionEstudiosRepository.findByActivaTrueAndRecomendacionUniversalTrue()
                .stream()
                .map(RecomendacionEstudiosDTO::new)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene recomendaciones de estudios para un candidato basadas en sus recomendaciones de puestos
     */
    @Transactional(readOnly = true)
    public List<RecomendacionEstudiosDTO> obtenerParaCandidato(Long personaId, RecomendacionService recomendacionService) {
        // Obtener las recomendaciones de puestos del candidato
        var recomendacionesPuestos = recomendacionService.generarRecomendaciones(personaId);
        
        // Obtener todas las recomendaciones de estudios vinculadas a esas posiciones
        return recomendacionesPuestos.stream()
                .flatMap(rp -> recomendacionEstudiosRepository.findByPosicionLaboralId(rp.getPosicionId()).stream())
                .distinct()
                .map(RecomendacionEstudiosDTO::new)
                .collect(Collectors.toList());
    }
}

