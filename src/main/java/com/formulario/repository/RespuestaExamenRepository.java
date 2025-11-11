package com.formulario.repository;

import com.formulario.model.RespuestaExamen;
import com.formulario.model.Examen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RespuestaExamenRepository extends JpaRepository<RespuestaExamen, Long> {
    
    // Buscar respuestas por examen
    List<RespuestaExamen> findByExamen(Examen examen);
    
    // Contar respuestas correctas por examen
    long countByExamenAndEsCorrectaTrue(Examen examen);
    
    // Contar total de respuestas por examen
    long countByExamen(Examen examen);
} 