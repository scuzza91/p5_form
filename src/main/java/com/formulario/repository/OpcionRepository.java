package com.formulario.repository;

import com.formulario.model.Opcion;
import com.formulario.model.Pregunta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OpcionRepository extends JpaRepository<Opcion, Long> {
    
    // Eliminar todas las opciones de una pregunta usando el ID de la pregunta
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Opcion o WHERE o.pregunta.id = :preguntaId")
    void deleteByPreguntaId(@Param("preguntaId") Long preguntaId);
    
    // Eliminar todas las opciones de una pregunta (método alternativo)
    void deleteByPregunta(Pregunta pregunta);
    
    // Buscar todas las opciones de una pregunta
    List<Opcion> findByPregunta(Pregunta pregunta);
    
    // Buscar todas las opciones de una pregunta por ID
    List<Opcion> findByPreguntaId(Long preguntaId);
} 