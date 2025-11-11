package com.formulario.repository;

import com.formulario.model.Pregunta;
import com.formulario.model.Pregunta.AreaConocimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PreguntaRepository extends JpaRepository<Pregunta, Long> {
    
    // Buscar preguntas por área de conocimiento
    List<Pregunta> findByAreaConocimiento(AreaConocimiento areaConocimiento);
    
    // Buscar preguntas activas por área
    List<Pregunta> findByAreaConocimientoAndActivaTrue(AreaConocimiento areaConocimiento);
    
    // Buscar todas las preguntas activas
    List<Pregunta> findByActivaTrue();
    
    // Contar preguntas por área
    @Query("SELECT COUNT(p) FROM Pregunta p WHERE p.areaConocimiento = ?1 AND p.activa = true")
    long countByAreaConocimiento(AreaConocimiento areaConocimiento);
    
    // Obtener preguntas aleatorias por área (para el examen)
    @Query(value = "SELECT * FROM preguntas WHERE area_conocimiento = ?1 AND activa = true ORDER BY RANDOM() LIMIT ?2", nativeQuery = true)
    List<Pregunta> findRandomByAreaConocimiento(String areaConocimiento, int limit);
} 