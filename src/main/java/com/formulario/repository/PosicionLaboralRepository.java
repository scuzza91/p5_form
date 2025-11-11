package com.formulario.repository;

import com.formulario.model.PosicionLaboral;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PosicionLaboralRepository extends JpaRepository<PosicionLaboral, Long> {
    
    // Buscar posiciones activas
    List<PosicionLaboral> findByActivaTrue();
    
    // Buscar por categoría
    List<PosicionLaboral> findByCategoriaAndActivaTrue(String categoria);
    
    // Buscar por nivel
    List<PosicionLaboral> findByNivelAndActivaTrue(String nivel);
    
    // Buscar por empresa
    List<PosicionLaboral> findByEmpresaAndActivaTrue(String empresa);
    
    // Buscar por modalidad
    List<PosicionLaboral> findByModalidadAndActivaTrue(String modalidad);
    
    // Buscar posiciones que requieren un promedio mínimo
    @Query("SELECT p FROM PosicionLaboral p WHERE p.activa = true AND (p.minPromedio IS NULL OR p.minPromedio <= :promedio)")
    List<PosicionLaboral> findPosicionesCompatiblesPorPromedio(@Param("promedio") Double promedio);
    
    // Buscar posiciones que requieren puntuaciones mínimas específicas
    @Query("SELECT p FROM PosicionLaboral p WHERE p.activa = true " +
           "AND (p.minLogica IS NULL OR p.minLogica <= :logica) " +
           "AND (p.minMatematica IS NULL OR p.minMatematica <= :matematica) " +
           "AND (p.minCreatividad IS NULL OR p.minCreatividad <= :creatividad) " +
           "AND (p.minProgramacion IS NULL OR p.minProgramacion <= :programacion) " +
           "AND (p.minPromedio IS NULL OR p.minPromedio <= :promedio)")
    List<PosicionLaboral> findPosicionesCompatibles(
            @Param("logica") Integer logica,
            @Param("matematica") Integer matematica,
            @Param("creatividad") Integer creatividad,
            @Param("programacion") Integer programacion,
            @Param("promedio") Double promedio);
    
    // Buscar posiciones por categoría y nivel
    List<PosicionLaboral> findByCategoriaAndNivelAndActivaTrue(String categoria, String nivel);
    
    // Buscar posiciones por empresa y modalidad
    List<PosicionLaboral> findByEmpresaAndModalidadAndActivaTrue(String empresa, String modalidad);
    
    // Contar posiciones por categoría
    @Query("SELECT p.categoria, COUNT(p) FROM PosicionLaboral p WHERE p.activa = true GROUP BY p.categoria")
    List<Object[]> countByCategoria();
    
    // Contar posiciones por nivel
    @Query("SELECT p.nivel, COUNT(p) FROM PosicionLaboral p WHERE p.activa = true GROUP BY p.nivel")
    List<Object[]> countByNivel();
    
    // Buscar posiciones por título (búsqueda parcial)
    @Query("SELECT p FROM PosicionLaboral p WHERE p.activa = true AND LOWER(p.titulo) LIKE LOWER(CONCAT('%', :titulo, '%'))")
    List<PosicionLaboral> findByTituloContainingIgnoreCase(@Param("titulo") String titulo);
} 