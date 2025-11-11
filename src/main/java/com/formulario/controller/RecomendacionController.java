package com.formulario.controller;

import com.formulario.model.RecomendacionRolDTO;
import com.formulario.service.RolProfesionalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import com.formulario.model.RolProfesional;
import com.formulario.repository.ExamenRepository;
import com.formulario.repository.PersonaRepository;
import com.formulario.repository.RolProfesionalRepository;
import com.formulario.model.Examen;
import com.formulario.model.Persona;
import java.util.Optional;
import java.util.ArrayList;
import java.util.HashMap;

@Controller
@RequestMapping("/recomendaciones")
public class RecomendacionController {
    
    private static final Logger logger = LoggerFactory.getLogger(RecomendacionController.class);
    
    static {
        System.out.println("=== RECOMENDACION CONTROLLER CARGADO ===");
        System.out.println("Controlador de recomendaciones inicializado correctamente");
    }
    
    @Autowired
    private RolProfesionalService rolProfesionalService;
    
    @Autowired
    private RolProfesionalRepository rolProfesionalRepository;
    
    @Autowired
    private ExamenRepository examenRepository;
    
    @Autowired
    private PersonaRepository personaRepository;
    
    /**
     * Endpoint muy simple para verificar que el controlador funciona
     */
    @GetMapping("/ping")
    public String ping(Model model) {
        System.out.println("=== PING ENDPOINT ACCEDIDO ===");
        model.addAttribute("mensaje", "PING - Controlador de recomendaciones funcionando correctamente");
        return "error";
    }
    
    /**
     * Endpoint de prueba simple
     */
    @GetMapping("/simple")
    public String simple(Model model) {
        System.out.println("=== SIMPLE ENDPOINT ACCEDIDO ===");
        model.addAttribute("mensaje", "Endpoint simple funcionando correctamente");
        return "error";
    }
    
    /**
     * Endpoint de prueba para verificar el acceso
     */
    @GetMapping("/test")
    public String testEndpoint(Model model) {
        try {
            System.out.println("=== TEST ENDPOINT ACCEDIDO ===");
            model.addAttribute("mensaje", "Test endpoint funcionando correctamente");
            return "error";
        } catch (Exception e) {
            System.err.println("Error en test endpoint: " + e.getMessage());
            model.addAttribute("mensaje", "Error en test endpoint: " + e.getMessage());
            return "error";
        }
    }
    
    /**
     * Endpoint de diagnóstico para verificar el estado de las posiciones laborales
     */
    @GetMapping("/debug")
    public String debugEndpoint(Model model) {
        try {
            System.out.println("=== DEBUG ENDPOINT ACCEDIDO ===");
            
            StringBuilder debugInfo = new StringBuilder();
            debugInfo.append("=== DIAGNÓSTICO DEL SISTEMA ===\n\n");
            debugInfo.append("✅ Endpoint de debug funcionando correctamente\n\n");
            
            // Verificar roles profesionales
            try {
                List<RolProfesional> roles = rolProfesionalRepository.findByActivoTrue();
                debugInfo.append("📋 Roles profesionales activos: ").append(roles.size()).append("\n");
                
                if (!roles.isEmpty()) {
                    debugInfo.append("✅ Roles disponibles:\n");
                    roles.stream().limit(3).forEach(rol -> {
                        debugInfo.append("   - ").append(rol.getTitulo())
                                 .append(" (").append(rol.getCategoria()).append(", ")
                                 .append(rol.getNivel()).append(")\n");
                    });
                } else {
                    debugInfo.append("❌ No hay roles profesionales activos\n");
                }
            } catch (Exception e) {
                debugInfo.append("❌ Error al consultar roles: ").append(e.getMessage()).append("\n");
            }
            
            debugInfo.append("\n");
            
            // Verificar exámenes
            try {
                List<Examen> examenes = examenRepository.findAll();
                debugInfo.append("📊 Total de exámenes: ").append(examenes.size()).append("\n");
            } catch (Exception e) {
                debugInfo.append("❌ Error al consultar exámenes: ").append(e.getMessage()).append("\n");
            }
            
            // Verificar personas
            try {
                List<Persona> personas = personaRepository.findAll();
                debugInfo.append("👥 Total de personas: ").append(personas.size()).append("\n");
            } catch (Exception e) {
                debugInfo.append("❌ Error al consultar personas: ").append(e.getMessage()).append("\n");
            }
            
            model.addAttribute("mensaje", debugInfo.toString());
            
        } catch (Exception e) {
            System.err.println("Error en debug endpoint: " + e.getMessage());
            model.addAttribute("mensaje", "Error en debug endpoint: " + e.getMessage());
        }
        
        return "error";
    }
    
    /**
     * Inicializar posiciones laborales
     */
    @GetMapping("/init")
    public String initRolesEndpoint(Model model) {
        try {
            System.out.println("=== INIT ROLES ENDPOINT ACCEDIDO ===");
            
            // Verificar roles existentes primero
            List<RolProfesional> rolesExistentes = rolProfesionalRepository.findByActivoTrue();
            System.out.println("Roles existentes: " + rolesExistentes.size());
            
            if (rolesExistentes.isEmpty()) {
                System.out.println("No hay roles existentes, inicializando...");
                // Forzar inicialización de roles profesionales
                rolProfesionalService.inicializarRolesEjemplo();
            } else {
                System.out.println("Ya existen roles, saltando inicialización");
            }
            
            // Verificar que se crearon
            List<RolProfesional> roles = rolProfesionalRepository.findByActivoTrue();
            
            StringBuilder initInfo = new StringBuilder();
            initInfo.append("=== INICIALIZACIÓN COMPLETADA ===\n\n");
            initInfo.append("Roles profesionales totales: ").append(roles.size()).append("\n\n");
            
            if (!roles.isEmpty()) {
                initInfo.append("=== ROLES DISPONIBLES ===\n");
                roles.forEach(rol -> {
                    initInfo.append("- ").append(rol.getTitulo())
                             .append(" (").append(rol.getCategoria()).append(", ")
                             .append(rol.getNivel()).append(")\n");
                });
            }
            
            model.addAttribute("mensaje", initInfo.toString());
            model.addAttribute("titulo", "Inicialización Completada");
            
        } catch (Exception e) {
            System.err.println("Error en init endpoint: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("mensaje", "Error en inicialización: " + e.getMessage());
            model.addAttribute("titulo", "Error en Inicialización");
        }
        
        return "error";
    }
    
    /**
     * Muestra las recomendaciones para un candidato
     */
    @GetMapping("/{personaId}")
    public String mostrarRecomendaciones(@PathVariable Long personaId, Model model) {
        System.out.println("=== INICIO mostrarRecomendaciones ===");
        System.out.println("Generando recomendaciones para persona ID: " + personaId);
        
        try {
            // Paso 1: Verificar si existe la persona
            Optional<Persona> personaOpt = personaRepository.findById(personaId);
            if (personaOpt.isEmpty()) {
                model.addAttribute("error", "No se encontró la persona con ID: " + personaId);
                return "error";
            }
            
            Persona persona = personaOpt.get();
            System.out.println("Persona encontrada: " + persona.getEmail());
            
            // Paso 2: Verificar si existe el examen
            Optional<Examen> examenOpt = examenRepository.findByPersona(persona);
            if (examenOpt.isEmpty()) {
                model.addAttribute("error", "No se encontró examen para la persona con ID: " + personaId);
                return "error";
            }
            
            Examen examen = examenOpt.get();
            System.out.println("Examen encontrado - ID: " + examen.getId() + ", Completado: " + (examen.getFechaFin() != null ? "SÍ" : "NO"));
            
            // Paso 3: Verificar que el examen esté completado
            if (examen.getFechaFin() == null) {
                model.addAttribute("error", "El examen para la persona con ID " + personaId + " no ha sido completado");
                return "error";
            }
            
            // Paso 4: Verificar roles profesionales
            List<RolProfesional> roles = rolProfesionalRepository.findByActivoTrue();
            System.out.println("Roles profesionales activos encontrados: " + roles.size());
            
            if (roles.isEmpty()) {
                model.addAttribute("error", "No hay roles profesionales disponibles. Contacte al administrador.");
                return "error";
            }
            
            // Paso 5: Obtener recomendaciones
            System.out.println("Generando recomendaciones...");
            List<RecomendacionRolDTO> recomendaciones = rolProfesionalService.generarRecomendacionesRoles(personaId);
            System.out.println("Recomendaciones generadas: " + recomendaciones.size() + " roles");
            
            // Paso 6: Obtener estadísticas
            Map<String, Object> estadisticas = rolProfesionalService.obtenerEstadisticasRecomendacionesRoles(personaId);
            
            model.addAttribute("recomendaciones", recomendaciones);
            model.addAttribute("estadisticas", estadisticas);
            model.addAttribute("personaId", personaId);
            
            System.out.println("Recomendaciones cargadas exitosamente para persona ID: " + personaId);
            return "recomendaciones";
            
        } catch (Exception e) {
            System.err.println("Error al mostrar recomendaciones para persona ID: " + personaId);
            System.err.println("Error detallado: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error al generar recomendaciones: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/test-simple")
    public String testSimple(Model model) {
        System.out.println("=== TEST SIMPLE ENDPOINT ACCEDIDO ===");
        return "error";
    }

    @GetMapping("/test-ultra-simple")
    public String testUltraSimple(Model model) {
        try {
            System.out.println("=== TEST ULTRA SIMPLE ENDPOINT ACCEDIDO ===");
            
            model.addAttribute("mensaje", "¡Funciona! El servidor está respondiendo correctamente.");
            model.addAttribute("titulo", "Test Ultra Simple");
            
            System.out.println("Test ultra simple completado exitosamente");
            
        } catch (Exception e) {
            System.err.println("Error en test-ultra-simple: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("mensaje", "Error: " + e.getMessage());
            model.addAttribute("titulo", "Error");
        }
        
        return "error";
    }

    @GetMapping("/test-init")
    public String testInit(Model model) {
        try {
            System.out.println("=== TEST INIT ENDPOINT ACCEDIDO ===");
            
            // Solo verificar roles existentes sin inicializar
            List<RolProfesional> roles = rolProfesionalRepository.findByActivoTrue();
            
            StringBuilder info = new StringBuilder();
            info.append("=== ESTADO ACTUAL ===\n\n");
            info.append("Roles profesionales: ").append(roles.size()).append("\n\n");
            
            if (!roles.isEmpty()) {
                info.append("=== ROLES EXISTENTES ===\n");
                roles.forEach(rol -> {
                    info.append("- ").append(rol.getTitulo())
                         .append(" (").append(rol.getCategoria()).append(", ")
                         .append(rol.getNivel()).append(")\n");
                });
            } else {
                info.append("No hay roles profesionales. Use /init para crear algunos.\n");
            }
            
            model.addAttribute("mensaje", info.toString());
            model.addAttribute("titulo", "Estado de Roles");
            
        } catch (Exception e) {
            System.err.println("Error en test-init: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("mensaje", "Error: " + e.getMessage());
            model.addAttribute("titulo", "Error");
        }
        
        return "error";
    }

    @GetMapping("/test-template")
    public String testTemplate(Model model) {
        try {
            System.out.println("=== TEST TEMPLATE ENDPOINT ACCEDIDO ===");
            
            // Crear datos de prueba mínimos
            List<RecomendacionRolDTO> recomendaciones = new ArrayList<>();
            
            // Crear una recomendación de prueba simple
            RecomendacionRolDTO testRec1 = new RecomendacionRolDTO();
            testRec1.setTitulo("Desarrollador Frontend");
            testRec1.setDescripcion("Se especializa en crear interfaces de usuario modernas, responsivas y accesibles que proporcionen una excelente experiencia de usuario.");
            testRec1.setCategoria("Desarrollo");
            testRec1.setNivel("Junior");
            testRec1.setResponsabilidades("Desarrollar interfaces de usuario, crear componentes reutilizables, optimizar rendimiento, trabajar con APIs");
            testRec1.setHabilidadesRequeridas("Creatividad visual, atención al detalle, conocimientos de UX/UI, capacidad de trabajo en equipo");
            testRec1.setTecnologiasRecomendadas("HTML, CSS, JavaScript, React, Vue.js, Angular, TypeScript, Sass/Less");
            testRec1.setRutaCarrera("Junior Frontend → Frontend Developer → Senior Frontend → Frontend Lead");
            testRec1.setCompatibilidad(85.5);
            testRec1.setLogica(75);
            testRec1.setMatematica(60);
            testRec1.setCreatividad(80);
            testRec1.setProgramacion(90);
            testRec1.setMinLogica(60);
            testRec1.setMinMatematica(50);
            testRec1.setMinCreatividad(70);
            testRec1.setMinProgramacion(70);
            recomendaciones.add(testRec1);
            
            System.out.println("Recomendación de prueba creada: " + testRec1.getTitulo());
            
            Map<String, Object> estadisticas = new HashMap<>();
            estadisticas.put("totalRecomendaciones", 1);
            estadisticas.put("mejorCompatibilidad", 85.5);
            estadisticas.put("promedioCompatibilidad", 85.5);
            estadisticas.put("porCategoria", new HashMap<>());
            estadisticas.put("porNivel", new HashMap<>());
            estadisticas.put("porNivelCompatibilidad", new HashMap<>());
            
            System.out.println("Estadísticas creadas");
            
            model.addAttribute("recomendaciones", recomendaciones);
            model.addAttribute("estadisticas", estadisticas);
            model.addAttribute("personaId", 999L);
            
            System.out.println("Atributos agregados al modelo");
            System.out.println("Retornando template 'recomendaciones'");
            
            return "recomendaciones";
            
        } catch (Exception e) {
            System.err.println("Error en test-template: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error en test-template: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/test-calculo")
    public String testCalculo(Model model) {
        try {
            System.out.println("=== TEST CÁLCULO DE COMPATIBILIDAD ===");
            
            // Crear un examen de prueba con puntuaciones específicas
            Examen examenTest = new Examen();
            examenTest.setId(999L);
            examenTest.setLogica(75);
            examenTest.setMatematica(60);
            examenTest.setCreatividad(80);
            examenTest.setProgramacion(70);
            // El promedio se calcula automáticamente: (75+60+80+70)/4 = 71.25
            
            System.out.println("Examen de prueba creado:");
            System.out.println("- Lógica: " + examenTest.getLogica());
            System.out.println("- Matemática: " + examenTest.getMatematica());
            System.out.println("- Creatividad: " + examenTest.getCreatividad());
            System.out.println("- Programación: " + examenTest.getProgramacion());
            System.out.println("- Promedio: " + examenTest.getPromedio());
            
            // Crear roles de prueba para verificar cálculos
            List<RecomendacionRolDTO> recomendaciones = new ArrayList<>();
            
            // Test 1: Desarrollador Frontend (Junior)
            RolProfesional frontend = new RolProfesional("Desarrollador Frontend", "Test", "Junior", "Desarrollo");
            frontend.setMinLogica(60);
            frontend.setMinMatematica(40);
            frontend.setMinCreatividad(70);
            frontend.setMinProgramacion(60);
            frontend.setMinPromedio(60);
            frontend.setPesoLogica(15);
            frontend.setPesoMatematica(5);
            frontend.setPesoCreatividad(30);
            frontend.setPesoProgramacion(50);
            
            double compatFrontend = frontend.calcularCompatibilidad(examenTest);
            System.out.println("Frontend - Compatibilidad calculada: " + compatFrontend);
            
            RecomendacionRolDTO recFrontend = new RecomendacionRolDTO(frontend, examenTest, compatFrontend);
            recomendaciones.add(recFrontend);
            
            // Test 2: Científico de Datos (Senior) - Debería fallar por matemática
            RolProfesional dataScientist = new RolProfesional("Científico de Datos", "Test", "Senior", "Análisis");
            dataScientist.setMinLogica(85);
            dataScientist.setMinMatematica(90);
            dataScientist.setMinCreatividad(60);
            dataScientist.setMinProgramacion(70);
            dataScientist.setMinPromedio(80);
            dataScientist.setPesoLogica(25);
            dataScientist.setPesoMatematica(40);
            dataScientist.setPesoCreatividad(15);
            dataScientist.setPesoProgramacion(20);
            
            double compatDataScientist = dataScientist.calcularCompatibilidad(examenTest);
            System.out.println("Data Scientist - Compatibilidad calculada: " + compatDataScientist);
            
            if (compatDataScientist > 0) {
                RecomendacionRolDTO recDataScientist = new RecomendacionRolDTO(dataScientist, examenTest, compatDataScientist);
                recomendaciones.add(recDataScientist);
            }
            
            // Test 3: Junior Developer (Junior) - Debería pasar fácilmente
            RolProfesional juniorDev = new RolProfesional("Junior Developer", "Test", "Junior", "Desarrollo");
            juniorDev.setMinLogica(40);
            juniorDev.setMinMatematica(30);
            juniorDev.setMinCreatividad(40);
            juniorDev.setMinProgramacion(30);
            juniorDev.setMinPromedio(35);
            juniorDev.setPesoLogica(25);
            juniorDev.setPesoMatematica(15);
            juniorDev.setPesoCreatividad(20);
            juniorDev.setPesoProgramacion(40);
            
            double compatJuniorDev = juniorDev.calcularCompatibilidad(examenTest);
            System.out.println("Junior Developer - Compatibilidad calculada: " + compatJuniorDev);
            
            RecomendacionRolDTO recJuniorDev = new RecomendacionRolDTO(juniorDev, examenTest, compatJuniorDev);
            recomendaciones.add(recJuniorDev);
            
            // Ordenar por compatibilidad
            recomendaciones.sort((r1, r2) -> Double.compare(r2.getCompatibilidad(), r1.getCompatibilidad()));
            
            Map<String, Object> estadisticas = new HashMap<>();
            estadisticas.put("totalRecomendaciones", recomendaciones.size());
            estadisticas.put("mejorCompatibilidad", recomendaciones.isEmpty() ? 0.0 : recomendaciones.get(0).getCompatibilidad());
            estadisticas.put("promedioCompatibilidad", recomendaciones.stream().mapToDouble(r -> r.getCompatibilidad()).average().orElse(0.0));
            estadisticas.put("porCategoria", new HashMap<>());
            estadisticas.put("porNivel", new HashMap<>());
            
            model.addAttribute("recomendaciones", recomendaciones);
            model.addAttribute("estadisticas", estadisticas);
            model.addAttribute("personaId", 999L);
            model.addAttribute("testMode", true);
            
            System.out.println("Test de cálculo completado. Recomendaciones generadas: " + recomendaciones.size());
            
            return "recomendaciones";
            
        } catch (Exception e) {
            System.err.println("Error en test-calculo: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error en test-calculo: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/test-calculo-detallado")
    public String testCalculoDetallado(Model model) {
        try {
            System.out.println("=== TEST CÁLCULO DETALLADO ===");
            
            // Crear diferentes exámenes de prueba
            List<Examen> examenes = new ArrayList<>();
            
            // Examen 1: Bueno en todo
            Examen examen1 = new Examen();
            examen1.setId(1L);
            examen1.setLogica(85);
            examen1.setMatematica(80);
            examen1.setCreatividad(90);
            examen1.setProgramacion(85);
            
            // Examen 2: Débil en matemática
            Examen examen2 = new Examen();
            examen2.setId(2L);
            examen2.setLogica(75);
            examen2.setMatematica(45);
            examen2.setCreatividad(80);
            examen2.setProgramacion(70);
            
            // Examen 3: Muy débil en todo
            Examen examen3 = new Examen();
            examen3.setId(3L);
            examen3.setLogica(30);
            examen3.setMatematica(25);
            examen3.setCreatividad(35);
            examen3.setProgramacion(20);
            
            examenes.add(examen1);
            examenes.add(examen2);
            examenes.add(examen3);
            
            // Crear roles de prueba
            List<RolProfesional> roles = new ArrayList<>();
            
            // Frontend Developer
            RolProfesional frontend = new RolProfesional("Frontend Developer", "Test", "Junior", "Desarrollo");
            frontend.setMinLogica(60);
            frontend.setMinMatematica(40);
            frontend.setMinCreatividad(70);
            frontend.setMinProgramacion(60);
            frontend.setMinPromedio(60);
            frontend.setPesoLogica(15);
            frontend.setPesoMatematica(5);
            frontend.setPesoCreatividad(30);
            frontend.setPesoProgramacion(50);
            roles.add(frontend);
            
            // Data Scientist
            RolProfesional dataScientist = new RolProfesional("Data Scientist", "Test", "Senior", "Análisis");
            dataScientist.setMinLogica(85);
            dataScientist.setMinMatematica(90);
            dataScientist.setMinCreatividad(60);
            dataScientist.setMinProgramacion(70);
            dataScientist.setMinPromedio(80);
            dataScientist.setPesoLogica(25);
            dataScientist.setPesoMatematica(40);
            dataScientist.setPesoCreatividad(15);
            dataScientist.setPesoProgramacion(20);
            roles.add(dataScientist);
            
            // Junior Developer
            RolProfesional juniorDev = new RolProfesional("Junior Developer", "Test", "Junior", "Desarrollo");
            juniorDev.setMinLogica(40);
            juniorDev.setMinMatematica(30);
            juniorDev.setMinCreatividad(40);
            juniorDev.setMinProgramacion(30);
            juniorDev.setMinPromedio(35);
            juniorDev.setPesoLogica(25);
            juniorDev.setPesoMatematica(15);
            juniorDev.setPesoCreatividad(20);
            juniorDev.setPesoProgramacion(40);
            roles.add(juniorDev);
            
            // Probar cada combinación
            List<Map<String, Object>> resultados = new ArrayList<>();
            
            for (int i = 0; i < examenes.size(); i++) {
                Examen examen = examenes.get(i);
                System.out.println("\n--- EXAMEN " + (i+1) + " ---");
                System.out.println("Lógica: " + examen.getLogica() + ", Matemática: " + examen.getMatematica() + 
                                 ", Creatividad: " + examen.getCreatividad() + ", Programación: " + examen.getProgramacion() + 
                                 ", Promedio: " + examen.getPromedio());
                
                for (RolProfesional rol : roles) {
                    double compatibilidad = rol.calcularCompatibilidad(examen);
                    
                    Map<String, Object> resultado = new HashMap<>();
                    resultado.put("examen", "Examen " + (i+1));
                    resultado.put("rol", rol.getTitulo());
                    resultado.put("compatibilidad", compatibilidad);
                    resultado.put("nivelCompatibilidad", determinarNivelCompatibilidad(compatibilidad));
                    resultado.put("cumpleRequisitos", compatibilidad > 0);
                    
                    resultados.add(resultado);
                    
                    System.out.println(rol.getTitulo() + ": " + compatibilidad + "% (" + 
                                     (compatibilidad > 0 ? "CUMPLE" : "NO CUMPLE") + ")");
                }
            }
            
            model.addAttribute("resultados", resultados);
            model.addAttribute("testMode", true);
            
            return "test_calculo";
            
        } catch (Exception e) {
            System.err.println("Error en test-calculo-detallado: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error en test-calculo-detallado: " + e.getMessage());
            return "error";
        }
    }
    
    private String determinarNivelCompatibilidad(double compatibilidad) {
        if (compatibilidad >= 90) return "Excelente";
        if (compatibilidad >= 80) return "Muy Buena";
        if (compatibilidad >= 70) return "Buena";
        if (compatibilidad >= 60) return "Regular";
        return "Baja";
    }
} 