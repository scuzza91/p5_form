package com.formulario.repository;

import com.formulario.model.Examen;
import com.formulario.model.Persona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamenRepository extends JpaRepository<Examen, Long> {
    // Método para buscar examen por persona
    Optional<Examen> findByPersona(Persona persona);
    
    // Método para verificar si existe un examen para una persona
    boolean existsByPersona(Persona persona);
    
    // Método para obtener todos los exámenes con personas cargadas
    @Query("SELECT e FROM Examen e LEFT JOIN FETCH e.persona")
    List<Examen> findAllWithPersona();
    
    // Método para obtener examen completo con todas las relaciones cargadas
    @Query("SELECT DISTINCT e FROM Examen e " +
           "LEFT JOIN FETCH e.persona p " +
           "LEFT JOIN FETCH p.provincia " +
           "LEFT JOIN FETCH p.localidad " +
           "LEFT JOIN FETCH e.respuestas r " +
           "LEFT JOIN FETCH r.pregunta pr " +
           "LEFT JOIN FETCH pr.opciones " +
           "WHERE e.persona.id = :personaId")
    Optional<Examen> findCompleteByPersonaId(@Param("personaId") Long personaId);
} 