package com.formulario.repository;

import com.formulario.model.Persona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PersonaRepository extends JpaRepository<Persona, Long> {
    // Método para buscar por email
    Persona findByEmail(String email);
    
    // Método para verificar si existe un email
    boolean existsByEmail(String email);
    
    // Método para buscar por CUIL
    Persona findByCuil(String cuil);
    
    // Método para verificar si existe un CUIL (evitar que una persona vuelva a hacer el test)
    boolean existsByCuil(String cuil);
    
    // Método para buscar persona por ID de caso de Bondarea (sincronización al eliminar caso)
    Optional<Persona> findByIdCasoBondarea(String idCasoBondarea);
} 