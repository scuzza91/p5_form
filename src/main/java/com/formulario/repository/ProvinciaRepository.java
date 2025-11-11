package com.formulario.repository;

import com.formulario.model.Provincia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProvinciaRepository extends JpaRepository<Provincia, Long> {
    
    List<Provincia> findAllByOrderByNombreAsc();
    
    Provincia findByNombre(String nombre);
} 