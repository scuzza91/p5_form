package com.formulario.service;

import com.formulario.model.*;
import com.formulario.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RolProfesionalService {
    
    @Autowired
    private RolProfesionalRepository rolProfesionalRepository;
    
    @Autowired
    private FormularioService formularioService;
    
    /**
     * Genera recomendaciones de roles profesionales para un candidato
     */
    @Transactional(readOnly = true)
    public List<RecomendacionRolDTO> generarRecomendacionesRoles(Long personaId) {
        try {
            System.out.println("🔄 Iniciando generación de recomendaciones de roles para persona ID: " + personaId);
            
            // Obtener el examen del candidato
            Optional<Examen> examenOpt = formularioService.obtenerResultadoCompleto(personaId);
            if (examenOpt.isEmpty()) {
                System.out.println("❌ No se encontró examen para persona ID: " + personaId);
                return new ArrayList<>();
            }
            
            Examen examen = examenOpt.get();
            System.out.println("✅ Examen encontrado - ID: " + examen.getId());
            
            // Obtener todos los roles activos
            List<RolProfesional> roles = rolProfesionalRepository.findByActivoTrue();
            System.out.println("📋 Roles profesionales activos encontrados: " + roles.size());
            
            if (roles.isEmpty()) {
                System.out.println("❌ No hay roles profesionales activos en la base de datos");
                return new ArrayList<>();
            }
            
            // Calcular compatibilidad para cada rol
            List<RecomendacionRolDTO> recomendaciones = new ArrayList<>();
            
            for (RolProfesional rol : roles) {
                try {
                    System.out.println("🔄 Calculando compatibilidad para rol: " + rol.getTitulo());
                    double compatibilidad = rol.calcularCompatibilidad(examen);
                    System.out.println("📊 Compatibilidad calculada: " + compatibilidad);
                    
                    // Solo incluir roles con compatibilidad > 0
                    if (compatibilidad > 0) {
                        RecomendacionRolDTO recomendacion = new RecomendacionRolDTO(rol, examen, compatibilidad);
                        recomendaciones.add(recomendacion);
                        System.out.println("✅ Recomendación agregada: " + rol.getTitulo() + " (" + compatibilidad + ")");
                    } else {
                        System.out.println("❌ Compatibilidad insuficiente: " + rol.getTitulo() + " (" + compatibilidad + ")");
                    }
                } catch (Exception e) {
                    System.err.println("❌ Error al calcular compatibilidad para " + rol.getTitulo() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            System.out.println("📊 Total de recomendaciones de roles generadas: " + recomendaciones.size());
            
            // Ordenar por compatibilidad descendente
            recomendaciones.sort((r1, r2) -> Double.compare(r2.getCompatibilidad(), r1.getCompatibilidad()));
            
            System.out.println("✅ Generación de recomendaciones de roles completada exitosamente");
            return recomendaciones;
            
        } catch (Exception e) {
            System.err.println("❌ Error al generar recomendaciones de roles: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * Genera recomendaciones de roles filtradas por categoría
     */
    @Transactional(readOnly = true)
    public List<RecomendacionRolDTO> generarRecomendacionesRolesPorCategoria(Long personaId, String categoria) {
        try {
            Optional<Examen> examenOpt = formularioService.obtenerResultadoCompleto(personaId);
            if (examenOpt.isEmpty()) {
                return new ArrayList<>();
            }
            
            Examen examen = examenOpt.get();
            List<RolProfesional> roles = rolProfesionalRepository.findByCategoriaAndActivoTrue(categoria);
            
            List<RecomendacionRolDTO> recomendaciones = new ArrayList<>();
            
            for (RolProfesional rol : roles) {
                double compatibilidad = rol.calcularCompatibilidad(examen);
                if (compatibilidad > 0) {
                    RecomendacionRolDTO recomendacion = new RecomendacionRolDTO(rol, examen, compatibilidad);
                    recomendaciones.add(recomendacion);
                }
            }
            
            recomendaciones.sort((r1, r2) -> Double.compare(r2.getCompatibilidad(), r1.getCompatibilidad()));
            return recomendaciones;
            
        } catch (Exception e) {
            System.err.println("Error al generar recomendaciones de roles por categoría: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Genera recomendaciones de roles filtradas por nivel
     */
    @Transactional(readOnly = true)
    public List<RecomendacionRolDTO> generarRecomendacionesRolesPorNivel(Long personaId, String nivel) {
        try {
            Optional<Examen> examenOpt = formularioService.obtenerResultadoCompleto(personaId);
            if (examenOpt.isEmpty()) {
                return new ArrayList<>();
            }
            
            Examen examen = examenOpt.get();
            List<RolProfesional> roles = rolProfesionalRepository.findByNivelAndActivoTrue(nivel);
            
            List<RecomendacionRolDTO> recomendaciones = new ArrayList<>();
            
            for (RolProfesional rol : roles) {
                double compatibilidad = rol.calcularCompatibilidad(examen);
                if (compatibilidad > 0) {
                    RecomendacionRolDTO recomendacion = new RecomendacionRolDTO(rol, examen, compatibilidad);
                    recomendaciones.add(recomendacion);
                }
            }
            
            recomendaciones.sort((r1, r2) -> Double.compare(r2.getCompatibilidad(), r1.getCompatibilidad()));
            return recomendaciones;
            
        } catch (Exception e) {
            System.err.println("Error al generar recomendaciones de roles por nivel: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Obtiene las mejores recomendaciones de roles (top N)
     */
    @Transactional(readOnly = true)
    public List<RecomendacionRolDTO> obtenerMejoresRecomendacionesRoles(Long personaId, int limite) {
        List<RecomendacionRolDTO> todasLasRecomendaciones = generarRecomendacionesRoles(personaId);
        return todasLasRecomendaciones.stream()
                .limit(limite)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene estadísticas de recomendaciones de roles
     */
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerEstadisticasRecomendacionesRoles(Long personaId) {
        Map<String, Object> estadisticas = new HashMap<>();
        
        try {
            List<RecomendacionRolDTO> recomendaciones = generarRecomendacionesRoles(personaId);
            
            estadisticas.put("totalRecomendaciones", recomendaciones.size());
            
            if (!recomendaciones.isEmpty()) {
                // Mejor compatibilidad
                double mejorCompatibilidad = recomendaciones.stream()
                        .mapToDouble(RecomendacionRolDTO::getCompatibilidad)
                        .max()
                        .orElse(0.0);
                estadisticas.put("mejorCompatibilidad", String.format("%.1f", mejorCompatibilidad));
                
                // Promedio de compatibilidad
                double promedioCompatibilidad = recomendaciones.stream()
                        .mapToDouble(RecomendacionRolDTO::getCompatibilidad)
                        .average()
                        .orElse(0.0);
                estadisticas.put("promedioCompatibilidad", String.format("%.1f", promedioCompatibilidad));
                
                // Roles por categoría
                Map<String, Long> porCategoria = recomendaciones.stream()
                        .collect(Collectors.groupingBy(RecomendacionRolDTO::getCategoria, Collectors.counting()));
                estadisticas.put("porCategoria", porCategoria);
                
                // Roles por nivel
                Map<String, Long> porNivel = recomendaciones.stream()
                        .collect(Collectors.groupingBy(RecomendacionRolDTO::getNivel, Collectors.counting()));
                estadisticas.put("porNivel", porNivel);
                
            } else {
                estadisticas.put("mejorCompatibilidad", "0.0");
                estadisticas.put("promedioCompatibilidad", "0.0");
                estadisticas.put("porCategoria", new HashMap<>());
                estadisticas.put("porNivel", new HashMap<>());
            }
            
        } catch (Exception e) {
            System.err.println("Error al obtener estadísticas de roles: " + e.getMessage());
            estadisticas.put("error", e.getMessage());
        }
        
        return estadisticas;
    }
    
    /**
     * Inicializa roles profesionales de ejemplo
     */
    public void inicializarRolesEjemplo() {
        try {
            System.out.println("🔄 Iniciando inicialización de roles profesionales...");
            
            // Verificar si ya hay roles
            long count = rolProfesionalRepository.count();
            System.out.println("Roles existentes: " + count);
            
            if (count > 0) {
                System.out.println("✅ Ya existen roles profesionales, saltando inicialización");
                return;
            }
            
            List<RolProfesional> roles = new ArrayList<>();
            
            // Desarrollador Full Stack
            RolProfesional fullStack = new RolProfesional(
                "Desarrollador Full Stack",
                "Desarrolla aplicaciones web completas, desde el frontend hasta el backend, manejando tanto la interfaz de usuario como la lógica del servidor.",
                "Semi-Senior",
                "Desarrollo"
            );
            fullStack.setResponsabilidades("Desarrollar aplicaciones web completas, mantener código existente, colaborar con equipos de diseño y backend, optimizar rendimiento");
            fullStack.setHabilidadesRequeridas("Conocimientos sólidos en frontend y backend, capacidad de resolver problemas complejos, trabajo en equipo");
            fullStack.setTecnologiasRecomendadas("JavaScript, React, Node.js, Python, Java, bases de datos SQL y NoSQL, Git");
            fullStack.setRutaCarrera("Junior Developer → Full Stack Developer → Senior Full Stack → Tech Lead");
            fullStack.setMinLogica(70);
            fullStack.setMinMatematica(60);
            fullStack.setMinCreatividad(50);
            fullStack.setMinProgramacion(80);
            fullStack.setMinPromedio(70);
            fullStack.setPesoLogica(20);
            fullStack.setPesoMatematica(10);
            fullStack.setPesoCreatividad(10);
            fullStack.setPesoProgramacion(60);
            roles.add(fullStack);
            
            // Desarrollador Frontend
            RolProfesional frontend = new RolProfesional(
                "Desarrollador Frontend",
                "Se especializa en crear interfaces de usuario modernas, responsivas y accesibles que proporcionen una excelente experiencia de usuario.",
                "Junior",
                "Desarrollo"
            );
            frontend.setResponsabilidades("Desarrollar interfaces de usuario, crear componentes reutilizables, optimizar rendimiento, trabajar con APIs");
            frontend.setHabilidadesRequeridas("Creatividad visual, atención al detalle, conocimientos de UX/UI, capacidad de trabajo en equipo");
            frontend.setTecnologiasRecomendadas("HTML, CSS, JavaScript, React, Vue.js, Angular, TypeScript, Sass/Less");
            frontend.setRutaCarrera("Junior Frontend → Frontend Developer → Senior Frontend → Frontend Lead");
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
            
            // Desarrollador Backend
            RolProfesional backend = new RolProfesional(
                "Desarrollador Backend",
                "Se enfoca en crear APIs robustas, servicios web y lógica de negocio que soporten aplicaciones y sistemas complejos.",
                "Semi-Senior",
                "Desarrollo"
            );
            backend.setResponsabilidades("Desarrollar APIs y servicios, optimizar consultas de base de datos, implementar seguridad, mantener sistemas");
            backend.setHabilidadesRequeridas("Pensamiento lógico, capacidad de resolver problemas complejos, conocimientos de arquitectura de software");
            backend.setTecnologiasRecomendadas("Java, Spring Boot, Python, Django, Node.js, bases de datos SQL, microservicios");
            backend.setRutaCarrera("Junior Backend → Backend Developer → Senior Backend → Backend Lead");
            backend.setMinLogica(75);
            backend.setMinMatematica(60);
            backend.setMinCreatividad(40);
            backend.setMinProgramacion(80);
            backend.setMinPromedio(70);
            backend.setPesoLogica(25);
            backend.setPesoMatematica(15);
            backend.setPesoCreatividad(10);
            backend.setPesoProgramacion(50);
            roles.add(backend);
            
            // Analista de Datos
            RolProfesional dataAnalyst = new RolProfesional(
                "Analista de Datos",
                "Transforma datos complejos en información accionable para la toma de decisiones empresariales.",
                "Semi-Senior",
                "Análisis"
            );
            dataAnalyst.setResponsabilidades("Crear dashboards y reportes, realizar análisis estadísticos, presentar hallazgos, limpiar y validar datos");
            dataAnalyst.setHabilidadesRequeridas("Pensamiento analítico, capacidad de comunicación, atención al detalle, curiosidad intelectual");
            dataAnalyst.setTecnologiasRecomendadas("SQL, Python, R, Excel, Power BI, Tableau, Google Analytics");
            dataAnalyst.setRutaCarrera("Data Entry → Data Analyst → Senior Data Analyst → Data Scientist");
            dataAnalyst.setMinLogica(80);
            dataAnalyst.setMinMatematica(80);
            dataAnalyst.setMinCreatividad(40);
            dataAnalyst.setMinProgramacion(50);
            dataAnalyst.setMinPromedio(70);
            dataAnalyst.setPesoLogica(30);
            dataAnalyst.setPesoMatematica(40);
            dataAnalyst.setPesoCreatividad(10);
            dataAnalyst.setPesoProgramacion(20);
            roles.add(dataAnalyst);
            
            // Científico de Datos
            RolProfesional dataScientist = new RolProfesional(
                "Científico de Datos",
                "Desarrolla modelos de machine learning y análisis predictivo para resolver problemas complejos del negocio.",
                "Senior",
                "Análisis"
            );
            dataScientist.setResponsabilidades("Desarrollar modelos predictivos, realizar experimentos, presentar resultados, colaborar con equipos de negocio");
            dataScientist.setHabilidadesRequeridas("Pensamiento matemático avanzado, creatividad en resolución de problemas, capacidad de comunicación técnica");
            dataScientist.setTecnologiasRecomendadas("Python, R, TensorFlow, PyTorch, scikit-learn, Jupyter, Spark");
            dataScientist.setRutaCarrera("Data Analyst → Data Scientist → Senior Data Scientist → Lead Data Scientist");
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
            
            // Project Manager
            RolProfesional projectManager = new RolProfesional(
                "Project Manager",
                "Gestiona proyectos de desarrollo de software desde la planificación hasta la entrega, asegurando el cumplimiento de objetivos.",
                "Senior",
                "Gestión"
            );
            projectManager.setResponsabilidades("Planificar proyectos, coordinar equipos, gestionar riesgos y recursos, comunicar progreso a stakeholders");
            projectManager.setHabilidadesRequeridas("Liderazgo, comunicación efectiva, organización, resolución de conflictos, pensamiento estratégico");
            projectManager.setTecnologiasRecomendadas("Jira, Trello, Microsoft Project, Slack, metodologías ágiles (Scrum, Kanban)");
            projectManager.setRutaCarrera("Team Member → Project Coordinator → Project Manager → Program Manager");
            projectManager.setMinLogica(75);
            projectManager.setMinMatematica(60);
            projectManager.setMinCreatividad(60);
            projectManager.setMinProgramacion(40);
            projectManager.setMinPromedio(75);
            projectManager.setPesoLogica(30);
            projectManager.setPesoMatematica(20);
            projectManager.setPesoCreatividad(20);
            projectManager.setPesoProgramacion(30);
            roles.add(projectManager);
            
            // DevOps Engineer
            RolProfesional devOps = new RolProfesional(
                "DevOps Engineer",
                "Automatiza procesos de desarrollo, testing y despliegue para mejorar la eficiencia del ciclo de desarrollo de software.",
                "Semi-Senior",
                "Infraestructura"
            );
            devOps.setResponsabilidades("Mantener infraestructura, automatizar despliegues, monitorear sistemas, optimizar procesos");
            devOps.setHabilidadesRequeridas("Pensamiento sistémico, capacidad de automatización, conocimientos de infraestructura, trabajo en equipo");
            devOps.setTecnologiasRecomendadas("Docker, Kubernetes, Jenkins, AWS, Azure, Terraform, Ansible");
            devOps.setRutaCarrera("System Administrator → DevOps Engineer → Senior DevOps → DevOps Lead");
            devOps.setMinLogica(70);
            devOps.setMinMatematica(60);
            devOps.setMinCreatividad(50);
            devOps.setMinProgramacion(70);
            devOps.setMinPromedio(70);
            devOps.setPesoLogica(25);
            devOps.setPesoMatematica(15);
            devOps.setPesoCreatividad(10);
            devOps.setPesoProgramacion(50);
            roles.add(devOps);
            
            // UX/UI Designer
            RolProfesional uxDesigner = new RolProfesional(
                "UX/UI Designer",
                "Diseña experiencias de usuario intuitivas y atractivas que resuelven problemas reales de los usuarios.",
                "Semi-Senior",
                "Diseño"
            );
            uxDesigner.setResponsabilidades("Crear wireframes y prototipos, realizar testing de usabilidad, investigar necesidades de usuarios");
            uxDesigner.setHabilidadesRequeridas("Creatividad, empatía con usuarios, pensamiento visual, capacidad de investigación");
            uxDesigner.setTecnologiasRecomendadas("Figma, Adobe Creative Suite, Sketch, InVision, herramientas de investigación de usuarios");
            uxDesigner.setRutaCarrera("Junior Designer → UX/UI Designer → Senior UX/UI → Design Lead");
            uxDesigner.setMinLogica(60);
            uxDesigner.setMinMatematica(40);
            uxDesigner.setMinCreatividad(85);
            uxDesigner.setMinProgramacion(30);
            uxDesigner.setMinPromedio(60);
            uxDesigner.setPesoLogica(20);
            uxDesigner.setPesoMatematica(10);
            uxDesigner.setPesoCreatividad(60);
            uxDesigner.setPesoProgramacion(10);
            roles.add(uxDesigner);
            
            // QA Engineer
            RolProfesional qaEngineer = new RolProfesional(
                "QA Engineer",
                "Asegura la calidad del software mediante testing automatizado y manual, identificando y reportando defectos.",
                "Junior",
                "Desarrollo"
            );
            qaEngineer.setResponsabilidades("Crear casos de prueba, ejecutar testing manual y automatizado, reportar bugs, colaborar con desarrolladores");
            qaEngineer.setHabilidadesRequeridas("Atención al detalle, pensamiento crítico, capacidad de comunicación, paciencia");
            qaEngineer.setTecnologiasRecomendadas("Selenium, JUnit, TestNG, Postman, herramientas de gestión de bugs");
            qaEngineer.setRutaCarrera("QA Tester → QA Engineer → Senior QA → QA Lead");
            qaEngineer.setMinLogica(70);
            qaEngineer.setMinMatematica(50);
            qaEngineer.setMinCreatividad(60);
            qaEngineer.setMinProgramacion(50);
            qaEngineer.setMinPromedio(65);
            qaEngineer.setPesoLogica(30);
            qaEngineer.setPesoMatematica(10);
            qaEngineer.setPesoCreatividad(20);
            qaEngineer.setPesoProgramacion(40);
            roles.add(qaEngineer);
            
            // Product Manager
            RolProfesional productManager = new RolProfesional(
                "Product Manager",
                "Define la estrategia y roadmap de productos digitales, priorizando features y trabajando con stakeholders.",
                "Senior",
                "Gestión"
            );
            productManager.setResponsabilidades("Definir roadmap de productos, priorizar features, trabajar con stakeholders, analizar métricas");
            productManager.setHabilidadesRequeridas("Pensamiento estratégico, comunicación efectiva, análisis de datos, liderazgo");
            productManager.setTecnologiasRecomendadas("Product management tools (Aha!, ProductPlan), analytics tools, collaboration platforms");
            productManager.setRutaCarrera("Business Analyst → Product Manager → Senior Product Manager → Product Director");
            productManager.setMinLogica(80);
            productManager.setMinMatematica(60);
            productManager.setMinCreatividad(70);
            productManager.setMinProgramacion(40);
            productManager.setMinPromedio(75);
            productManager.setPesoLogica(30);
            productManager.setPesoMatematica(15);
            productManager.setPesoCreatividad(30);
            productManager.setPesoProgramacion(25);
            roles.add(productManager);
            
            // ROLES JUNIOR CON REQUISITOS MÁS BAJOS
            // Junior Developer
            RolProfesional juniorDev = new RolProfesional(
                "Junior Developer",
                "Aprende y desarrolla aplicaciones bajo supervisión de desarrolladores senior, adquiriendo experiencia práctica.",
                "Junior",
                "Desarrollo"
            );
            juniorDev.setResponsabilidades("Desarrollar features simples, aprender nuevas tecnologías, colaborar con el equipo, seguir mejores prácticas");
            juniorDev.setHabilidadesRequeridas("Ganas de aprender, capacidad de trabajo en equipo, pensamiento lógico básico");
            juniorDev.setTecnologiasRecomendadas("HTML, CSS, JavaScript básico, Git, un lenguaje de programación (Python, Java, etc.)");
            juniorDev.setRutaCarrera("Junior Developer → Developer → Senior Developer → Tech Lead");
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
            
            // Data Entry
            RolProfesional dataEntry = new RolProfesional(
                "Data Entry",
                "Ingresa y valida datos en sistemas informáticos de manera precisa y eficiente.",
                "Junior",
                "Análisis"
            );
            dataEntry.setResponsabilidades("Ingresar datos, validar información, mantener bases de datos actualizadas, reportar inconsistencias");
            dataEntry.setHabilidadesRequeridas("Atención al detalle, organización, paciencia, conocimientos básicos de informática");
            dataEntry.setTecnologiasRecomendadas("Excel, Google Sheets, sistemas de gestión de datos, herramientas de validación");
            dataEntry.setRutaCarrera("Data Entry → Data Analyst → Senior Data Analyst → Data Scientist");
            dataEntry.setMinLogica(30);
            dataEntry.setMinMatematica(30);
            dataEntry.setMinCreatividad(20);
            dataEntry.setMinProgramacion(20);
            dataEntry.setMinPromedio(25);
            dataEntry.setPesoLogica(40);
            dataEntry.setPesoMatematica(30);
            dataEntry.setPesoCreatividad(10);
            dataEntry.setPesoProgramacion(20);
            roles.add(dataEntry);
            
            // Customer Support
            RolProfesional customerSupport = new RolProfesional(
                "Customer Support",
                "Brinda soporte técnico y atención al cliente, resolviendo problemas y consultas de manera efectiva.",
                "Junior",
                "Gestión"
            );
            customerSupport.setResponsabilidades("Atender consultas de clientes, resolver problemas técnicos, documentar casos, escalar problemas complejos");
            customerSupport.setHabilidadesRequeridas("Comunicación efectiva, paciencia, empatía, conocimientos básicos de tecnología");
            customerSupport.setTecnologiasRecomendadas("Sistemas de tickets, CRM, herramientas de comunicación, bases de conocimiento");
            customerSupport.setRutaCarrera("Customer Support → Senior Support → Support Team Lead → Customer Success Manager");
            customerSupport.setMinLogica(35);
            customerSupport.setMinMatematica(25);
            customerSupport.setMinCreatividad(30);
            customerSupport.setMinProgramacion(25);
            customerSupport.setMinPromedio(30);
            customerSupport.setPesoLogica(30);
            customerSupport.setPesoMatematica(10);
            customerSupport.setPesoCreatividad(30);
            customerSupport.setPesoProgramacion(30);
            roles.add(customerSupport);
            
            // Guardar todos los roles
            rolProfesionalRepository.saveAll(roles);
            
            System.out.println("✅ " + roles.size() + " roles profesionales creados exitosamente");
            
        } catch (Exception e) {
            System.err.println("❌ Error al inicializar roles profesionales: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 