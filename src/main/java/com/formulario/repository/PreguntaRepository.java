package com.formulario.repository;

import com.formulario.model.Pregunta;
import com.formulario.model.Pregunta.AreaConocimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    
    // Obtener todas las preguntas activas por área con sus opciones cargadas
    // El servicio se encargará de seleccionar aleatoriamente las necesarias
    @Query("SELECT DISTINCT p FROM Pregunta p LEFT JOIN FETCH p.opciones WHERE p.areaConocimiento = :areaConocimiento AND p.activa = true")
    List<Pregunta> findByAreaConocimientoWithOpciones(@Param("areaConocimiento") AreaConocimiento areaConocimiento);
} 