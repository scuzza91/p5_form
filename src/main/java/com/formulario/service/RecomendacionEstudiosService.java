package com.formulario.service;

import com.formulario.model.RecomendacionEstudios;
import com.formulario.model.RecomendacionEstudiosDTO;
import com.formulario.model.PosicionLaboral;
import com.formulario.repository.RecomendacionEstudiosRepository;
import com.formulario.repository.PosicionLaboralRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

