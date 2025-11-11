package com.formulario.repository;

import com.formulario.model.Localidad;
import com.formulario.model.Provincia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocalidadRepository extends JpaRepository<Localidad, Long> {
    
    List<Localidad> findByProvinciaOrderByNombreAsc(Provincia provincia);
    
    List<Localidad> findByProvinciaIdOrderByNombreAsc(Long provinciaId);
    
    Localidad findByNombreAndProvincia(String nombre, Provincia provincia);
} 