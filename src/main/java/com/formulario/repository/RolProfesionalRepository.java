package com.formulario.repository;

import com.formulario.model.RolProfesional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RolProfesionalRepository extends JpaRepository<RolProfesional, Long> {
    
    // Buscar roles activos
    List<RolProfesional> findByActivoTrue();
    
    // Buscar por categoría
    List<RolProfesional> findByCategoriaAndActivoTrue(String categoria);
    
    // Buscar por nivel
    List<RolProfesional> findByNivelAndActivoTrue(String nivel);
    
    // Buscar por categoría y nivel
    List<RolProfesional> findByCategoriaAndNivelAndActivoTrue(String categoria, String nivel);
    
    // Buscar roles que requieren un promedio mínimo
    @Query("SELECT r FROM RolProfesional r WHERE r.activo = true AND (r.minPromedio IS NULL OR r.minPromedio <= :promedio)")
    List<RolProfesional> findRolesCompatiblesPorPromedio(@Param("promedio") Double promedio);
    
    // Buscar roles que requieren puntuaciones mínimas específicas
    @Query("SELECT r FROM RolProfesional r WHERE r.activo = true " +
           "AND (r.minLogica IS NULL OR r.minLogica <= :logica) " +
           "AND (r.minMatematica IS NULL OR r.minMatematica <= :matematica) " +
           "AND (r.minCreatividad IS NULL OR r.minCreatividad <= :creatividad) " +
           "AND (r.minProgramacion IS NULL OR r.minProgramacion <= :programacion) " +
           "AND (r.minPromedio IS NULL OR r.minPromedio <= :promedio)")
    List<RolProfesional> findRolesCompatibles(
            @Param("logica") Integer logica,
            @Param("matematica") Integer matematica,
            @Param("creatividad") Integer creatividad,
            @Param("programacion") Integer programacion,
            @Param("promedio") Double promedio);
    
    // Contar roles por categoría
    @Query("SELECT r.categoria, COUNT(r) FROM RolProfesional r WHERE r.activo = true GROUP BY r.categoria")
    List<Object[]> countByCategoria();
    
    // Contar roles por nivel
    @Query("SELECT r.nivel, COUNT(r) FROM RolProfesional r WHERE r.activo = true GROUP BY r.nivel")
    List<Object[]> countByNivel();
    
    // Buscar roles por título (búsqueda parcial)
    @Query("SELECT r FROM RolProfesional r WHERE r.activo = true AND LOWER(r.titulo) LIKE LOWER(CONCAT('%', :titulo, '%'))")
    List<RolProfesional> findByTituloContainingIgnoreCase(@Param("titulo") String titulo);
} 