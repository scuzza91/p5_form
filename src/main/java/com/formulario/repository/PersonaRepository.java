package com.formulario.repository;

import com.formulario.model.Persona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonaRepository extends JpaRepository<Persona, Long> {
    // Método para buscar por email
    Persona findByEmail(String email);
    
    // Método para verificar si existe un email
    boolean existsByEmail(String email);
} 