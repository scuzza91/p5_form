package com.formulario.repository;

import com.formulario.model.RecomendacionEstudios;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecomendacionEstudiosRepository extends JpaRepository<RecomendacionEstudios, Long> {
    
    // Buscar recomendaciones activas
    List<RecomendacionEstudios> findByActivaTrue();
    
    // Buscar por nombre de institución
    List<RecomendacionEstudios> findByNombreInstitucionContainingIgnoreCaseAndActivaTrue(String nombreInstitucion);
    
    // Buscar por nombre de oferta
    List<RecomendacionEstudios> findByNombreOfertaContainingIgnoreCaseAndActivaTrue(String nombreOferta);
    
    // Buscar recomendaciones vinculadas a una posición laboral específica
    @Query("SELECT re FROM RecomendacionEstudios re JOIN re.posicionesLaborales pl WHERE pl.id = :posicionId AND re.activa = true")
    List<RecomendacionEstudios> findByPosicionLaboralId(@Param("posicionId") Long posicionId);
    
    // Buscar recomendaciones por rango de costo
    @Query("SELECT re FROM RecomendacionEstudios re WHERE re.activa = true AND re.costo BETWEEN :costoMin AND :costoMax ORDER BY re.costo ASC")
    List<RecomendacionEstudios> findByCostoBetween(@Param("costoMin") java.math.BigDecimal costoMin, 
                                                    @Param("costoMax") java.math.BigDecimal costoMax);
    
    // Buscar recomendaciones ordenadas por costo ascendente
    List<RecomendacionEstudios> findByActivaTrueOrderByCostoAsc();
    
    // Buscar recomendaciones ordenadas por costo descendente
    List<RecomendacionEstudios> findByActivaTrueOrderByCostoDesc();
    
    // Contar recomendaciones activas
    long countByActivaTrue();
    
    // Recomendaciones que se muestran a todos los candidatos (sin depender del resultado del test)
    List<RecomendacionEstudios> findByActivaTrueAndRecomendacionUniversalTrue();
}

