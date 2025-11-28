package com.formulario.service;

import com.formulario.model.Localidad;
import com.formulario.model.Provincia;
import com.formulario.repository.LocalidadRepository;
import com.formulario.repository.ProvinciaRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class LocalidadService {
    
    @Autowired
    private ProvinciaRepository provinciaRepository;
    
    @Autowired
    private LocalidadRepository localidadRepository;
    
    public List<Provincia> obtenerTodasLasProvincias() {
        return provinciaRepository.findAllByOrderByNombreAsc();
    }
    
    public List<Localidad> obtenerLocalidadesPorProvincia(Long provinciaId) {
        return localidadRepository.findByProvinciaIdOrderByNombreAsc(provinciaId);
    }
    
    public Optional<Provincia> obtenerProvinciaPorId(Long id) {
        return provinciaRepository.findById(id);
    }
    
    public Optional<Localidad> obtenerLocalidadPorId(Long id) {
        return localidadRepository.findById(id);
    }
    
    public Optional<Provincia> obtenerProvinciaPorNombre(String nombre) {
        Provincia provincia = provinciaRepository.findByNombre(nombre);
        return provincia != null ? Optional.of(provincia) : Optional.empty();
    }
    
    public Optional<Localidad> obtenerLocalidadPorNombreYProvincia(String nombreLocalidad, String nombreProvincia) {
        Optional<Provincia> provinciaOpt = obtenerProvinciaPorNombre(nombreProvincia);
        if (provinciaOpt.isPresent()) {
            Localidad localidad = localidadRepository.findByNombreAndProvincia(nombreLocalidad, provinciaOpt.get());
            return localidad != null ? Optional.of(localidad) : Optional.empty();
        }
        return Optional.empty();
    }
    
    public void cargarDatosDesdeExcel(String rutaArchivo) {
        try (FileInputStream fis = new FileInputStream(rutaArchivo);
             Workbook workbook = new XSSFWorkbook(fis)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Saltar encabezados
                
                Cell provinciaCell = row.getCell(0);
                Cell localidadCell = row.getCell(1);
                
                if (provinciaCell != null && localidadCell != null) {
                    String nombreProvincia = provinciaCell.getStringCellValue().trim();
                    String nombreLocalidad = localidadCell.getStringCellValue().trim();
                    
                    if (!nombreProvincia.isEmpty() && !nombreLocalidad.isEmpty()) {
                        // Buscar o crear provincia
                        Provincia provincia = provinciaRepository.findByNombre(nombreProvincia);
                        if (provincia == null) {
                            provincia = new Provincia(nombreProvincia);
                            provincia = provinciaRepository.save(provincia);
                        }
                        
                        // Verificar si la localidad ya existe
                        Localidad localidadExistente = localidadRepository.findByNombreAndProvincia(nombreLocalidad, provincia);
                        if (localidadExistente == null) {
                            Localidad localidad = new Localidad(nombreLocalidad, provincia);
                            localidadRepository.save(localidad);
                        }
                    }
                }
            }
            
        } catch (IOException e) {
            throw new RuntimeException("Error al cargar datos desde Excel: " + e.getMessage(), e);
        }
    }
} 