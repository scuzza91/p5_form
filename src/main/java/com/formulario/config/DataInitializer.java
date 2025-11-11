package com.formulario.config;

import com.formulario.model.Provincia;
import com.formulario.model.Localidad;
import com.formulario.model.Usuario;
import com.formulario.repository.ProvinciaRepository;
import com.formulario.repository.LocalidadRepository;
import com.formulario.repository.UsuarioRepository;
import com.formulario.repository.PreguntaRepository;
import com.formulario.service.AuthService;
import com.formulario.service.ExamenService;
import com.formulario.service.RecomendacionService;
import com.formulario.service.RolProfesionalService;
import com.formulario.service.ConfiguracionService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private ProvinciaRepository provinciaRepository;
    
    @Autowired
    private LocalidadRepository localidadRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private PreguntaRepository preguntaRepository;
    
    @Autowired
    private ExamenService examenService;
    
    @Autowired
    private RecomendacionService recomendacionService;
    
    @Autowired
    private RolProfesionalService rolProfesionalService;
    
    @Autowired
    private ConfiguracionService configuracionService;

    @Override
    public void run(String... args) throws Exception {
        // Cargar provincias argentinas si no existen
        if (provinciaRepository.count() == 0) {
            cargarProvinciasArgentinas();
        }
        
        // Cargar localidades desde Excel si no existen
        if (localidadRepository.count() == 0) {
            cargarLocalidadesDesdeExcel();
        }
        
        // Forzar carga de localidades básicas si no hay ninguna
        if (localidadRepository.count() == 0) {
            System.out.println("🔄 Forzando carga de localidades básicas...");
            cargarLocalidadesBasicas();
        }
        
        // Inicializar preguntas de ejemplo si no existen
        if (preguntaRepository.count() == 0) {
            System.out.println("🔄 Inicializando preguntas de ejemplo...");
            examenService.inicializarPreguntasEjemplo();
        }
        
        // Inicializar posiciones laborales de ejemplo
        System.out.println("🔄 Inicializando posiciones laborales de ejemplo...");
        recomendacionService.inicializarPosicionesEjemplo();
        
        // Inicializar roles profesionales de ejemplo
        System.out.println("🔄 Inicializando roles profesionales de ejemplo...");
        rolProfesionalService.inicializarRolesEjemplo();
        
        // Crear usuario administrador por defecto (siempre recrear para debug)
        System.out.println("🔄 Forzando recreación del usuario administrador...");
        crearUsuarioAdministrador();
        
        // Inicializar configuraciones del sistema
        System.out.println("🔄 Inicializando configuraciones del sistema...");
        configuracionService.inicializarConfiguracionesPorDefecto();
    }

    private void cargarProvinciasArgentinas() {
        List<String> provincias = Arrays.asList(
            "Buenos Aires",
            "Ciudad Autónoma de Buenos Aires",
            "Catamarca",
            "Chaco",
            "Chubut",
            "Córdoba",
            "Corrientes",
            "Entre Ríos",
            "Formosa",
            "Jujuy",
            "La Pampa",
            "La Rioja",
            "Mendoza",
            "Misiones",
            "Neuquén",
            "Río Negro",
            "Salta",
            "San Juan",
            "San Luis",
            "Santa Cruz",
            "Santa Fe",
            "Santiago del Estero",
            "Tierra del Fuego",
            "Tucumán"
        );

        for (String nombreProvincia : provincias) {
            Provincia provincia = new Provincia(nombreProvincia);
            provinciaRepository.save(provincia);
        }

        System.out.println("✅ Provincias argentinas cargadas correctamente: " + provincias.size() + " provincias");
    }
    
    private void cargarLocalidadesDesdeExcel() {
        try {
            String rutaArchivo = "Localidades.xlsx";
            System.out.println("🔄 Cargando localidades desde: " + rutaArchivo);
            
            try (FileInputStream fis = new FileInputStream(rutaArchivo);
                 Workbook workbook = new XSSFWorkbook(fis)) {
                
                Sheet sheet = workbook.getSheetAt(0);
                int localidadesCargadas = 0;
                
                for (Row row : sheet) {
                    if (row.getRowNum() == 0) continue; // Saltar encabezados
                    
                    Cell provinciaCell = row.getCell(0);
                    Cell localidadCell = row.getCell(1);
                    
                    if (provinciaCell != null && localidadCell != null) {
                        String nombreProvincia = provinciaCell.getStringCellValue().trim();
                        String nombreLocalidad = localidadCell.getStringCellValue().trim();
                        
                        if (!nombreProvincia.isEmpty() && !nombreLocalidad.isEmpty()) {
                            // Buscar provincia
                            Provincia provincia = provinciaRepository.findByNombre(nombreProvincia);
                            if (provincia != null) {
                                // Verificar si la localidad ya existe
                                Localidad localidadExistente = localidadRepository.findByNombreAndProvincia(nombreLocalidad, provincia);
                                if (localidadExistente == null) {
                                    Localidad localidad = new Localidad(nombreLocalidad, provincia);
                                    localidadRepository.save(localidad);
                                    localidadesCargadas++;
                                }
                            }
                        }
                    }
                }
                
                System.out.println("✅ Localidades cargadas correctamente: " + localidadesCargadas + " localidades");
                
            } catch (IOException e) {
                System.err.println("❌ Error al cargar localidades desde Excel: " + e.getMessage());
                // Si no se puede cargar desde Excel, intentar cargar algunas localidades básicas
                cargarLocalidadesBasicas();
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error general al cargar localidades: " + e.getMessage());
            cargarLocalidadesBasicas();
        }
    }
    
    private void cargarLocalidadesBasicas() {
        System.out.println("🔄 Cargando localidades básicas...");
        
        // Cargar algunas localidades básicas para cada provincia
        Provincia buenosAires = provinciaRepository.findByNombre("Buenos Aires");
        if (buenosAires != null) {
            cargarLocalidad("La Plata", buenosAires);
            cargarLocalidad("Mar del Plata", buenosAires);
            cargarLocalidad("Bahía Blanca", buenosAires);
            cargarLocalidad("Quilmes", buenosAires);
            cargarLocalidad("San Isidro", buenosAires);
        }
        
        Provincia cordoba = provinciaRepository.findByNombre("Córdoba");
        if (cordoba != null) {
            cargarLocalidad("Córdoba", cordoba);
            cargarLocalidad("Villa María", cordoba);
            cargarLocalidad("Río Cuarto", cordoba);
            cargarLocalidad("San Francisco", cordoba);
        }
        
        Provincia santaFe = provinciaRepository.findByNombre("Santa Fe");
        if (santaFe != null) {
            cargarLocalidad("Santa Fe", santaFe);
            cargarLocalidad("Rosario", santaFe);
            cargarLocalidad("Rafaela", santaFe);
            cargarLocalidad("Venado Tuerto", santaFe);
        }
        
        Provincia mendoza = provinciaRepository.findByNombre("Mendoza");
        if (mendoza != null) {
            cargarLocalidad("Mendoza", mendoza);
            cargarLocalidad("San Rafael", mendoza);
            cargarLocalidad("Tunuyán", mendoza);
        }
        
        Provincia salta = provinciaRepository.findByNombre("Salta");
        if (salta != null) {
            cargarLocalidad("Salta", salta);
            cargarLocalidad("San Ramón de la Nueva Orán", salta);
            cargarLocalidad("Tartagal", salta);
        }
        
        Provincia tucuman = provinciaRepository.findByNombre("Tucumán");
        if (tucuman != null) {
            cargarLocalidad("San Miguel de Tucumán", tucuman);
            cargarLocalidad("Yerba Buena", tucuman);
            cargarLocalidad("Tafí Viejo", tucuman);
        }
        
        System.out.println("✅ Localidades básicas cargadas");
    }
    
    private void cargarLocalidad(String nombreLocalidad, Provincia provincia) {
        Localidad localidadExistente = localidadRepository.findByNombreAndProvincia(nombreLocalidad, provincia);
        if (localidadExistente == null) {
            Localidad localidad = new Localidad(nombreLocalidad, provincia);
            localidadRepository.save(localidad);
        }
    }
    
    private void crearUsuarioAdministrador() {
        System.out.println("🔄 Creando usuario administrador por defecto...");
        
        try {
            // Primero, eliminar el usuario admin si existe
            var usuarioExistente = usuarioRepository.findByUsername("admin");
            if (usuarioExistente.isPresent()) {
                System.out.println("🗑️ Eliminando usuario admin existente...");
                usuarioRepository.delete(usuarioExistente.get());
            }
            
            Usuario admin = new Usuario();
            admin.setUsername("admin");
            admin.setEmail("admin@piso5.com");
            admin.setPassword("admin123");
            admin.setNombreCompleto("Administrador del Sistema");
            admin.setRol(Usuario.Rol.ADMIN);
            admin.setActivo(true);
            
            Usuario usuarioCreado = authService.crearUsuario(admin);
            
            System.out.println("✅ Usuario administrador creado correctamente");
            System.out.println("📧 Usuario: " + usuarioCreado.getUsername());
            System.out.println("🔑 Contraseña: admin123");
            System.out.println("🔐 Contraseña encriptada: " + usuarioCreado.getPassword());
            System.out.println("⚠️  IMPORTANTE: Cambie la contraseña después del primer inicio de sesión");
            
        } catch (Exception e) {
            System.err.println("❌ Error al crear usuario administrador: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 