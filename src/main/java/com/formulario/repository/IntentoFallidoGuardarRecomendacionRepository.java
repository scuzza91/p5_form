package com.formulario.repository;

import com.formulario.model.IntentoFallidoGuardarRecomendacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IntentoFallidoGuardarRecomendacionRepository extends JpaRepository<IntentoFallidoGuardarRecomendacion, Long> {

    Optional<IntentoFallidoGuardarRecomendacion> findFirstByExamenIdOrderByFechaHoraDesc(Long examenId);
}
