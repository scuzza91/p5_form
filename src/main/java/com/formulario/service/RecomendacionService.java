package com.formulario.service;

import com.formulario.model.*;
import com.formulario.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecomendacionService {
    
    @Autowired
    private PosicionLaboralRepository posicionLaboralRepository;
    
    @Autowired
    private FormularioService formularioService;
    
    /**
     * Genera recomendaciones de posiciones laborales para un candidato
     */
    @Transactional(readOnly = true)
    public List<RecomendacionDTO> generarRecomendaciones(Long personaId) {
        try {
            System.out.println("🔄 Iniciando generación de recomendaciones para persona ID: " + personaId);
            
            // Obtener el examen del candidato
            Optional<Examen> examenOpt = formularioService.obtenerResultadoCompleto(personaId);
            if (examenOpt.isEmpty()) {
                System.out.println("❌ No se encontró examen para persona ID: " + personaId);
                return new ArrayList<>();
            }
            
            Examen examen = examenOpt.get();
            System.out.println("✅ Examen encontrado - ID: " + examen.getId());
            
            // Obtener todas las posiciones activas
            List<PosicionLaboral> posiciones = posicionLaboralRepository.findByActivaTrue();
            System.out.println("📋 Posiciones laborales activas encontradas: " + posiciones.size());
            
            if (posiciones.isEmpty()) {
                System.out.println("❌ No hay posiciones laborales activas en la base de datos");
                return new ArrayList<>();
            }
            
            // Calcular compatibilidad para cada posición
            List<RecomendacionDTO> recomendaciones = new ArrayList<>();
            
            for (PosicionLaboral posicion : posiciones) {
                try {
                    System.out.println("🔄 Calculando compatibilidad para: " + posicion.getTitulo());
                    double compatibilidad = posicion.calcularCompatibilidad(examen);
                    System.out.println("📊 Compatibilidad calculada: " + compatibilidad);
                    
                    // Solo incluir posiciones con compatibilidad > 0
                    if (compatibilidad > 0) {
                        RecomendacionDTO recomendacion = new RecomendacionDTO(posicion, examen, compatibilidad);
                        recomendaciones.add(recomendacion);
                        System.out.println("✅ Recomendación agregada: " + posicion.getTitulo() + " (" + compatibilidad + ")");
                    } else {
                        System.out.println("❌ Compatibilidad insuficiente: " + posicion.getTitulo() + " (" + compatibilidad + ")");
                    }
                } catch (Exception e) {
                    System.err.println("❌ Error al calcular compatibilidad para " + posicion.getTitulo() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            System.out.println("📊 Total de recomendaciones generadas: " + recomendaciones.size());
            
            // Ordenar por compatibilidad descendente
            recomendaciones.sort((r1, r2) -> Double.compare(r2.getCompatibilidad(), r1.getCompatibilidad()));
            
            System.out.println("✅ Generación de recomendaciones completada exitosamente");
            return recomendaciones;
            
        } catch (Exception e) {
            System.err.println("❌ Error al generar recomendaciones: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * Genera recomendaciones filtradas por categoría
     */
    @Transactional(readOnly = true)
    public List<RecomendacionDTO> generarRecomendacionesPorCategoria(Long personaId, String categoria) {
        try {
            Optional<Examen> examenOpt = formularioService.obtenerResultadoCompleto(personaId);
            if (examenOpt.isEmpty()) {
                return new ArrayList<>();
            }
            
            Examen examen = examenOpt.get();
            List<PosicionLaboral> posiciones = posicionLaboralRepository.findByCategoriaAndActivaTrue(categoria);
            
            List<RecomendacionDTO> recomendaciones = new ArrayList<>();
            
            for (PosicionLaboral posicion : posiciones) {
                double compatibilidad = posicion.calcularCompatibilidad(examen);
                if (compatibilidad > 0) {
                    RecomendacionDTO recomendacion = new RecomendacionDTO(posicion, examen, compatibilidad);
                    recomendaciones.add(recomendacion);
                }
            }
            
            recomendaciones.sort((r1, r2) -> Double.compare(r2.getCompatibilidad(), r1.getCompatibilidad()));
            return recomendaciones;
            
        } catch (Exception e) {
            System.err.println("Error al generar recomendaciones por categoría: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Genera recomendaciones filtradas por nivel
     */
    @Transactional(readOnly = true)
    public List<RecomendacionDTO> generarRecomendacionesPorNivel(Long personaId, String nivel) {
        try {
            Optional<Examen> examenOpt = formularioService.obtenerResultadoCompleto(personaId);
            if (examenOpt.isEmpty()) {
                return new ArrayList<>();
            }
            
            Examen examen = examenOpt.get();
            List<PosicionLaboral> posiciones = posicionLaboralRepository.findByNivelAndActivaTrue(nivel);
            
            List<RecomendacionDTO> recomendaciones = new ArrayList<>();
            
            for (PosicionLaboral posicion : posiciones) {
                double compatibilidad = posicion.calcularCompatibilidad(examen);
                if (compatibilidad > 0) {
                    RecomendacionDTO recomendacion = new RecomendacionDTO(posicion, examen, compatibilidad);
                    recomendaciones.add(recomendacion);
                }
            }
            
            recomendaciones.sort((r1, r2) -> Double.compare(r2.getCompatibilidad(), r1.getCompatibilidad()));
            return recomendaciones;
            
        } catch (Exception e) {
            System.err.println("Error al generar recomendaciones por nivel: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Obtiene las mejores recomendaciones (top N)
     */
    @Transactional(readOnly = true)
    public List<RecomendacionDTO> obtenerMejoresRecomendaciones(Long personaId, int limite) {
        List<RecomendacionDTO> todasLasRecomendaciones = generarRecomendaciones(personaId);
        return todasLasRecomendaciones.stream()
                .limit(limite)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene estadísticas de recomendaciones
     */
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerEstadisticasRecomendaciones(Long personaId) {
        Map<String, Object> estadisticas = new HashMap<>();
        
        try {
            List<RecomendacionDTO> recomendaciones = generarRecomendaciones(personaId);
            
            // Inicializar valores por defecto
            estadisticas.put("totalRecomendaciones", recomendaciones.size());
            estadisticas.put("promedioCompatibilidad", 0.0);
            estadisticas.put("mejorCompatibilidad", 0.0);
            estadisticas.put("porNivelCompatibilidad", new HashMap<>());
            estadisticas.put("porCategoria", new HashMap<>());
            estadisticas.put("porNivel", new HashMap<>());
            
            if (!recomendaciones.isEmpty()) {
                // Promedio de compatibilidad
                double promedioCompatibilidad = recomendaciones.stream()
                        .mapToDouble(RecomendacionDTO::getCompatibilidad)
                        .average()
                        .orElse(0.0);
                estadisticas.put("promedioCompatibilidad", Math.round(promedioCompatibilidad * 10.0) / 10.0);
                
                // Mejor compatibilidad
                double mejorCompatibilidad = recomendaciones.stream()
                        .mapToDouble(RecomendacionDTO::getCompatibilidad)
                        .max()
                        .orElse(0.0);
                estadisticas.put("mejorCompatibilidad", Math.round(mejorCompatibilidad * 10.0) / 10.0);
                
                // Contar por nivel de compatibilidad
                Map<String, Long> porNivel = recomendaciones.stream()
                        .collect(Collectors.groupingBy(
                                RecomendacionDTO::getNivelCompatibilidad,
                                Collectors.counting()));
                estadisticas.put("porNivelCompatibilidad", porNivel);
                
                // Contar por categoría
                Map<String, Long> porCategoria = recomendaciones.stream()
                        .collect(Collectors.groupingBy(
                                RecomendacionDTO::getCategoria,
                                Collectors.counting()));
                estadisticas.put("porCategoria", porCategoria);
                
                // Contar por nivel
                Map<String, Long> porNivelLaboral = recomendaciones.stream()
                        .collect(Collectors.groupingBy(
                                RecomendacionDTO::getNivel,
                                Collectors.counting()));
                estadisticas.put("porNivel", porNivelLaboral);
            }
            
        } catch (Exception e) {
            System.err.println("Error al obtener estadísticas: " + e.getMessage());
            estadisticas.put("error", e.getMessage());
        }
        
        return estadisticas;
    }
    
    /**
     * Inicializa posiciones laborales de ejemplo
     */
    public void inicializarPosicionesEjemplo() {
        try {
            System.out.println("🔄 Iniciando inicialización de posiciones laborales...");
            
            // Verificar si ya hay posiciones
            long count = posicionLaboralRepository.count();
            System.out.println("Posiciones existentes: " + count);
            
            if (count > 0) {
                System.out.println("✅ Ya existen posiciones laborales, saltando inicialización");
                return;
            }
            
            List<PosicionLaboral> posiciones = new ArrayList<>();
            
            // Desarrollador Full Stack
            PosicionLaboral fullStack = new PosicionLaboral(
                "Desarrollador Full Stack",
                "Desarrollar aplicaciones web completas, desde el frontend hasta el backend.",
                "Semi-Senior",
                "Desarrollo"
            );
            fullStack.setDescripcion("Buscamos un desarrollador full stack versátil que pueda trabajar tanto en el frontend como en el backend, desarrollando aplicaciones web completas.");
            fullStack.setEmpresa("TechCorp");
            fullStack.setUbicacion("Buenos Aires, Argentina");
            fullStack.setTipoContrato("Tiempo completo");
            fullStack.setModalidad("Híbrida");
            fullStack.setRequisitos("Experiencia en JavaScript, React, Node.js, bases de datos SQL y NoSQL");
            fullStack.setResponsabilidades("Desarrollar y mantener aplicaciones web, colaborar con el equipo de diseño, optimizar rendimiento");
            fullStack.setMinLogica(70);
            fullStack.setMinMatematica(60);
            fullStack.setMinCreatividad(50);
            fullStack.setMinProgramacion(80);
            fullStack.setMinPromedio(70);
            fullStack.setPesoLogica(20);
            fullStack.setPesoMatematica(10);
            fullStack.setPesoCreatividad(10);
            fullStack.setPesoProgramacion(60);
            posiciones.add(fullStack);
            
            // Frontend Developer
            PosicionLaboral frontend = new PosicionLaboral(
                "Frontend Developer",
                "Desarrollar interfaces de usuario modernas y responsivas.",
                "Junior",
                "Desarrollo"
            );
            frontend.setDescripcion("Buscamos un desarrollador frontend apasionado por crear experiencias de usuario excepcionales.");
            frontend.setEmpresa("DesignStudio");
            frontend.setUbicacion("Córdoba, Argentina");
            frontend.setTipoContrato("Tiempo completo");
            frontend.setModalidad("Remota");
            frontend.setRequisitos("Conocimientos en HTML, CSS, JavaScript, React o Vue.js");
            frontend.setResponsabilidades("Desarrollar componentes reutilizables, optimizar rendimiento, trabajar con APIs");
            frontend.setMinLogica(60);
            frontend.setMinMatematica(40);
            frontend.setMinCreatividad(70);
            frontend.setMinProgramacion(60);
            frontend.setMinPromedio(60);
            frontend.setPesoLogica(15);
            frontend.setPesoMatematica(5);
            frontend.setPesoCreatividad(30);
            frontend.setPesoProgramacion(50);
            posiciones.add(frontend);
            
            // Data Analyst
            PosicionLaboral dataAnalyst = new PosicionLaboral(
                "Data Analyst",
                "Analizar datos para generar insights y reportes para la toma de decisiones.",
                "Semi-Senior",
                "Análisis"
            );
            dataAnalyst.setDescripcion("Buscamos un analista de datos que pueda transformar datos complejos en información accionable.");
            dataAnalyst.setEmpresa("DataCorp");
            dataAnalyst.setUbicacion("Rosario, Argentina");
            dataAnalyst.setTipoContrato("Tiempo completo");
            dataAnalyst.setModalidad("Presencial");
            dataAnalyst.setRequisitos("Experiencia en SQL, Excel, Python, herramientas de visualización");
            dataAnalyst.setResponsabilidades("Crear dashboards, realizar análisis estadísticos, presentar hallazgos");
            dataAnalyst.setMinLogica(80);
            dataAnalyst.setMinMatematica(80);
            dataAnalyst.setMinCreatividad(40);
            dataAnalyst.setMinProgramacion(50);
            dataAnalyst.setMinPromedio(70);
            dataAnalyst.setPesoLogica(30);
            dataAnalyst.setPesoMatematica(40);
            dataAnalyst.setPesoCreatividad(10);
            dataAnalyst.setPesoProgramacion(20);
            posiciones.add(dataAnalyst);
            
            // Project Manager
            PosicionLaboral projectManager = new PosicionLaboral(
                "Project Manager",
                "Gestionar proyectos de desarrollo de software desde la planificación hasta la entrega.",
                "Senior",
                "Gestión"
            );
            projectManager.setDescripcion("Buscamos un project manager experimentado que pueda liderar equipos y entregar proyectos exitosos.");
            projectManager.setEmpresa("ProjectCorp");
            projectManager.setUbicacion("Mendoza, Argentina");
            projectManager.setTipoContrato("Tiempo completo");
            projectManager.setModalidad("Híbrida");
            projectManager.setRequisitos("Experiencia en metodologías ágiles, herramientas de gestión, liderazgo de equipos");
            projectManager.setResponsabilidades("Planificar proyectos, coordinar equipos, gestionar riesgos y recursos");
            projectManager.setMinLogica(75);
            projectManager.setMinMatematica(60);
            projectManager.setMinCreatividad(60);
            projectManager.setMinProgramacion(40);
            projectManager.setMinPromedio(75);
            projectManager.setPesoLogica(30);
            projectManager.setPesoMatematica(20);
            projectManager.setPesoCreatividad(20);
            projectManager.setPesoProgramacion(30);
            posiciones.add(projectManager);
            
            // DevOps Engineer
            PosicionLaboral devOps = new PosicionLaboral(
                "DevOps Engineer",
                "Automatizar procesos de desarrollo, testing y despliegue de aplicaciones.",
                "Semi-Senior",
                "Infraestructura"
            );
            devOps.setDescripcion("Buscamos un ingeniero DevOps que pueda mejorar la eficiencia del ciclo de desarrollo.");
            devOps.setEmpresa("CloudCorp");
            devOps.setUbicacion("La Plata, Argentina");
            devOps.setTipoContrato("Tiempo completo");
            devOps.setModalidad("Remota");
            devOps.setRequisitos("Experiencia en Docker, Kubernetes, CI/CD, AWS/Azure");
            devOps.setResponsabilidades("Mantener infraestructura, automatizar despliegues, monitorear sistemas");
            devOps.setMinLogica(70);
            devOps.setMinMatematica(60);
            devOps.setMinCreatividad(50);
            devOps.setMinProgramacion(70);
            devOps.setMinPromedio(70);
            devOps.setPesoLogica(25);
            devOps.setPesoMatematica(15);
            devOps.setPesoCreatividad(10);
            devOps.setPesoProgramacion(50);
            posiciones.add(devOps);
            
            // Backend Developer
            PosicionLaboral backend = new PosicionLaboral(
                "Backend Developer",
                "Desarrollar APIs y servicios backend robustos y escalables.",
                "Semi-Senior",
                "Desarrollo"
            );
            backend.setDescripcion("Buscamos un desarrollador backend que pueda crear servicios robustos y eficientes.");
            backend.setEmpresa("APICorp");
            backend.setUbicacion("Tucumán, Argentina");
            backend.setTipoContrato("Tiempo completo");
            backend.setModalidad("Híbrida");
            backend.setRequisitos("Experiencia en Java, Spring Boot, bases de datos, microservicios");
            backend.setResponsabilidades("Desarrollar APIs, optimizar consultas, implementar seguridad");
            backend.setMinLogica(75);
            backend.setMinMatematica(60);
            backend.setMinCreatividad(40);
            backend.setMinProgramacion(80);
            backend.setMinPromedio(70);
            backend.setPesoLogica(25);
            backend.setPesoMatematica(15);
            backend.setPesoCreatividad(10);
            backend.setPesoProgramacion(50);
            posiciones.add(backend);
            
            // Data Scientist
            PosicionLaboral dataScientist = new PosicionLaboral(
                "Data Scientist",
                "Desarrollar modelos de machine learning y análisis predictivo.",
                "Senior",
                "Análisis"
            );
            dataScientist.setDescripcion("Buscamos un científico de datos que pueda crear modelos predictivos innovadores.");
            dataScientist.setEmpresa("AICorp");
            dataScientist.setUbicacion("Neuquén, Argentina");
            dataScientist.setTipoContrato("Tiempo completo");
            dataScientist.setModalidad("Remota");
            dataScientist.setRequisitos("Experiencia en Python, R, machine learning, estadística avanzada");
            dataScientist.setResponsabilidades("Desarrollar modelos, realizar experimentos, presentar resultados");
            dataScientist.setMinLogica(85);
            dataScientist.setMinMatematica(90);
            dataScientist.setMinCreatividad(60);
            dataScientist.setMinProgramacion(70);
            dataScientist.setMinPromedio(80);
            dataScientist.setPesoLogica(25);
            dataScientist.setPesoMatematica(40);
            dataScientist.setPesoCreatividad(15);
            dataScientist.setPesoProgramacion(20);
            posiciones.add(dataScientist);
            
            // UX/UI Designer
            PosicionLaboral uxDesigner = new PosicionLaboral(
                "UX/UI Designer",
                "Diseñar experiencias de usuario intuitivas y atractivas.",
                "Semi-Senior",
                "Diseño"
            );
            uxDesigner.setDescripcion("Buscamos un diseñador UX/UI que pueda crear experiencias digitales excepcionales.");
            uxDesigner.setEmpresa("DesignCorp");
            uxDesigner.setUbicacion("Salta, Argentina");
            uxDesigner.setTipoContrato("Tiempo completo");
            uxDesigner.setModalidad("Híbrida");
            uxDesigner.setRequisitos("Experiencia en Figma, Adobe Creative Suite, investigación de usuarios");
            uxDesigner.setResponsabilidades("Crear wireframes, prototipos, realizar testing de usabilidad");
            uxDesigner.setMinLogica(60);
            uxDesigner.setMinMatematica(40);
            uxDesigner.setMinCreatividad(85);
            uxDesigner.setMinProgramacion(30);
            uxDesigner.setMinPromedio(60);
            uxDesigner.setPesoLogica(20);
            uxDesigner.setPesoMatematica(10);
            uxDesigner.setPesoCreatividad(60);
            uxDesigner.setPesoProgramacion(10);
            posiciones.add(uxDesigner);
            
            // QA Engineer
            PosicionLaboral qaEngineer = new PosicionLaboral(
                "QA Engineer",
                "Asegurar la calidad del software mediante testing automatizado y manual.",
                "Junior",
                "Desarrollo"
            );
            qaEngineer.setDescripcion("Buscamos un ingeniero QA que pueda garantizar la calidad de nuestros productos.");
            qaEngineer.setEmpresa("QualityCorp");
            qaEngineer.setUbicacion("Entre Ríos, Argentina");
            qaEngineer.setTipoContrato("Tiempo completo");
            qaEngineer.setModalidad("Remota");
            qaEngineer.setRequisitos("Conocimientos en testing manual, automatizado, herramientas como Selenium");
            qaEngineer.setResponsabilidades("Crear casos de prueba, ejecutar testing, reportar bugs");
            qaEngineer.setMinLogica(70);
            qaEngineer.setMinMatematica(50);
            qaEngineer.setMinCreatividad(60);
            qaEngineer.setMinProgramacion(50);
            qaEngineer.setMinPromedio(65);
            qaEngineer.setPesoLogica(30);
            qaEngineer.setPesoMatematica(10);
            qaEngineer.setPesoCreatividad(20);
            qaEngineer.setPesoProgramacion(40);
            posiciones.add(qaEngineer);
            
            // Product Manager
            PosicionLaboral productManager = new PosicionLaboral(
                "Product Manager",
                "Definir la estrategia y roadmap de productos digitales.",
                "Senior",
                "Gestión"
            );
            productManager.setDescripcion("Buscamos un product manager que pueda liderar el desarrollo de productos innovadores.");
            productManager.setEmpresa("ProductCorp");
            productManager.setUbicacion("Chaco, Argentina");
            productManager.setTipoContrato("Tiempo completo");
            productManager.setModalidad("Híbrida");
            productManager.setRequisitos("Experiencia en gestión de productos, análisis de mercado, liderazgo");
            productManager.setResponsabilidades("Definir roadmap, priorizar features, trabajar con stakeholders");
            productManager.setMinLogica(80);
            productManager.setMinMatematica(60);
            productManager.setMinCreatividad(70);
            productManager.setMinProgramacion(40);
            productManager.setMinPromedio(75);
            productManager.setPesoLogica(30);
            productManager.setPesoMatematica(15);
            productManager.setPesoCreatividad(30);
            productManager.setPesoProgramacion(25);
            posiciones.add(productManager);
            
            // POSICIONES CON REQUISITOS MÁS BAJOS PARA CANDIDATOS JUNIOR
            // Junior Developer
            PosicionLaboral juniorDev = new PosicionLaboral(
                "Junior Developer",
                "Aprender y desarrollar aplicaciones bajo supervisión de desarrolladores senior.",
                "Junior",
                "Desarrollo"
            );
            juniorDev.setDescripcion("Buscamos un desarrollador junior con ganas de aprender y crecer profesionalmente.");
            juniorDev.setEmpresa("StartupCorp");
            juniorDev.setUbicacion("Buenos Aires, Argentina");
            juniorDev.setTipoContrato("Tiempo completo");
            juniorDev.setModalidad("Presencial");
            juniorDev.setRequisitos("Conocimientos básicos en programación, ganas de aprender");
            juniorDev.setResponsabilidades("Desarrollar features simples, aprender nuevas tecnologías, colaborar con el equipo");
            juniorDev.setMinLogica(40);
            juniorDev.setMinMatematica(30);
            juniorDev.setMinCreatividad(40);
            juniorDev.setMinProgramacion(30);
            juniorDev.setMinPromedio(35);
            juniorDev.setPesoLogica(25);
            juniorDev.setPesoMatematica(15);
            juniorDev.setPesoCreatividad(20);
            juniorDev.setPesoProgramacion(40);
            posiciones.add(juniorDev);
            
            // Data Entry
            PosicionLaboral dataEntry = new PosicionLaboral(
                "Data Entry",
                "Ingresar y validar datos en sistemas informáticos.",
                "Junior",
                "Análisis"
            );
            dataEntry.setDescripcion("Buscamos una persona organizada para ingresar y validar datos de manera precisa.");
            dataEntry.setEmpresa("DataCorp");
            dataEntry.setUbicacion("Córdoba, Argentina");
            dataEntry.setTipoContrato("Tiempo completo");
            dataEntry.setModalidad("Presencial");
            dataEntry.setRequisitos("Conocimientos básicos de Excel, atención al detalle");
            dataEntry.setResponsabilidades("Ingresar datos, validar información, mantener bases de datos actualizadas");
            dataEntry.setMinLogica(30);
            dataEntry.setMinMatematica(30);
            dataEntry.setMinCreatividad(20);
            dataEntry.setMinProgramacion(20);
            dataEntry.setMinPromedio(25);
            dataEntry.setPesoLogica(40);
            dataEntry.setPesoMatematica(30);
            dataEntry.setPesoCreatividad(10);
            dataEntry.setPesoProgramacion(20);
            posiciones.add(dataEntry);
            
            // Customer Support
            PosicionLaboral customerSupport = new PosicionLaboral(
                "Customer Support",
                "Brindar soporte técnico y atención al cliente.",
                "Junior",
                "Gestión"
            );
            customerSupport.setDescripcion("Buscamos una persona con buena comunicación para brindar soporte a nuestros clientes.");
            customerSupport.setEmpresa("SupportCorp");
            customerSupport.setUbicacion("Rosario, Argentina");
            customerSupport.setTipoContrato("Tiempo completo");
            customerSupport.setModalidad("Remota");
            customerSupport.setRequisitos("Buenas habilidades de comunicación, paciencia, conocimientos básicos de tecnología");
            customerSupport.setResponsabilidades("Atender consultas de clientes, resolver problemas técnicos, documentar casos");
            customerSupport.setMinLogica(35);
            customerSupport.setMinMatematica(25);
            customerSupport.setMinCreatividad(30);
            customerSupport.setMinProgramacion(25);
            customerSupport.setMinPromedio(30);
            customerSupport.setPesoLogica(30);
            customerSupport.setPesoMatematica(10);
            customerSupport.setPesoCreatividad(30);
            customerSupport.setPesoProgramacion(30);
            posiciones.add(customerSupport);
            
            // Guardar todas las posiciones
            posicionLaboralRepository.saveAll(posiciones);
            
            System.out.println("✅ " + posiciones.size() + " posiciones laborales creadas exitosamente");
            
        } catch (Exception e) {
            System.err.println("❌ Error al inicializar posiciones laborales: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 