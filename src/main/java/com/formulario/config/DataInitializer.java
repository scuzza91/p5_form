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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Value("${admin.password:admin123}")
    private String adminPassword;

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
            logger.info("🔄 Forzando carga de localidades básicas...");
            cargarLocalidadesBasicas();
        }
        
        // Inicializar preguntas de ejemplo si no existen
        if (preguntaRepository.count() == 0) {
            logger.info("🔄 Inicializando preguntas de ejemplo...");
            examenService.inicializarPreguntasEjemplo();
        }
        
        // Inicializar posiciones laborales de ejemplo
        logger.info("🔄 Inicializando posiciones laborales de ejemplo...");
        recomendacionService.inicializarPosicionesEjemplo();
        
        // Inicializar roles profesionales de ejemplo
        logger.info("🔄 Inicializando roles profesionales de ejemplo...");
        rolProfesionalService.inicializarRolesEjemplo();
        
        // Crear usuario administrador solo si no existe
        if (usuarioRepository.findByUsername("admin").isEmpty()) {
            logger.info("🔄 Usuario admin no existe, creando por primera vez...");
            crearUsuarioAdministrador();
        } else {
            logger.info("✅ Usuario administrador ya existe, no se modifica");
        }
        
        // Inicializar configuraciones del sistema
        logger.info("🔄 Inicializando configuraciones del sistema...");
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

        logger.info("✅ Provincias argentinas cargadas correctamente: " + provincias.size() + " provincias");
    }
    
    private void cargarLocalidadesDesdeExcel() {
        try {
            String rutaArchivo = "Localidades.xlsx";
            logger.info("🔄 Cargando localidades desde: " + rutaArchivo);
            
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
                
                logger.info("✅ Localidades cargadas correctamente: " + localidadesCargadas + " localidades");
                
            } catch (IOException e) {
                logger.error("❌ Error al cargar localidades desde Excel: " + e.getMessage());
                // Si no se puede cargar desde Excel, intentar cargar algunas localidades básicas
                cargarLocalidadesBasicas();
            }
            
        } catch (Exception e) {
            logger.error("❌ Error general al cargar localidades: " + e.getMessage());
            cargarLocalidadesBasicas();
        }
    }
    
    private void cargarLocalidadesBasicas() {
        logger.info("🔄 Cargando localidades básicas...");
        
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
        
        logger.info("✅ Localidades básicas cargadas");
    }
    
    private void cargarLocalidad(String nombreLocalidad, Provincia provincia) {
        Localidad localidadExistente = localidadRepository.findByNombreAndProvincia(nombreLocalidad, provincia);
        if (localidadExistente == null) {
            Localidad localidad = new Localidad(nombreLocalidad, provincia);
            localidadRepository.save(localidad);
        }
    }
    
    private void crearUsuarioAdministrador() {
        try {
            Usuario admin = new Usuario();
            admin.setUsername("admin");
            admin.setEmail("admin@piso5.com");
            admin.setPassword(adminPassword);
            admin.setNombreCompleto("Administrador del Sistema");
            admin.setRol(Usuario.Rol.ADMIN);
            admin.setActivo(true);

            authService.crearUsuario(admin);

            logger.info("✅ Usuario administrador creado. Usuario: admin");
            logger.info("⚠️  Usá la variable ADMIN_PASSWORD para definir la contraseña");

        } catch (Exception e) {
            logger.error("❌ Error al crear usuario administrador: {}", e.getMessage());
        }
    }
} 