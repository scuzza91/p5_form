package com.formulario.repository;

import com.formulario.model.Opcion;
import com.formulario.model.Pregunta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OpcionRepository extends JpaRepository<Opcion, Long> {
    
    // Eliminar todas las opciones de una pregunta
    void deleteByPregunta(Pregunta pregunta);
    
    // Buscar todas las opciones de una pregunta
    List<Opcion> findByPregunta(Pregunta pregunta);
} 