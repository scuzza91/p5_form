package com.formulario.service;

import com.formulario.model.*;
import com.formulario.repository.*;
import com.formulario.repository.OpcionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExamenService {
    
    @Autowired
    private PreguntaRepository preguntaRepository;
    
    @Autowired
    private ExamenRepository examenRepository;
    
    @Autowired
    private RespuestaExamenRepository respuestaExamenRepository;
    
    @Autowired
    private OpcionRepository opcionRepository;
    
    private static final int PREGUNTAS_POR_AREA = 8;
    private static final int TOTAL_PREGUNTAS = 32; // 8 * 4 áreas
    
    /**
     * Genera preguntas aleatorias para el examen
     */
    public List<Pregunta> generarPreguntasExamen() {
        List<Pregunta> todasLasPreguntas = new ArrayList<>();
        
        try {
            // Verificar si hay preguntas en la base de datos
            long totalPreguntas = preguntaRepository.count();
            System.out.println("Total de preguntas en BD: " + totalPreguntas);
            
            if (totalPreguntas == 0) {
                System.out.println("No hay preguntas en la base de datos. Inicializando preguntas de ejemplo...");
                inicializarPreguntasEjemplo();
            }
            
            // Obtener preguntas aleatorias por área
            List<Pregunta> preguntasLogica = preguntaRepository.findRandomByAreaConocimiento(
                Pregunta.AreaConocimiento.LOGICA.name(), PREGUNTAS_POR_AREA);
            System.out.println("Preguntas lógica: " + preguntasLogica.size());
            
            List<Pregunta> preguntasMatematica = preguntaRepository.findRandomByAreaConocimiento(
                Pregunta.AreaConocimiento.MATEMATICA.name(), PREGUNTAS_POR_AREA);
            System.out.println("Preguntas matemática: " + preguntasMatematica.size());
            
            List<Pregunta> preguntasCreatividad = preguntaRepository.findRandomByAreaConocimiento(
                Pregunta.AreaConocimiento.CREATIVIDAD.name(), PREGUNTAS_POR_AREA);
            System.out.println("Preguntas creatividad: " + preguntasCreatividad.size());
            
            List<Pregunta> preguntasProgramacion = preguntaRepository.findRandomByAreaConocimiento(
                Pregunta.AreaConocimiento.PROGRAMACION.name(), PREGUNTAS_POR_AREA);
            System.out.println("Preguntas programación: " + preguntasProgramacion.size());
            
            // Combinar todas las preguntas
            todasLasPreguntas.addAll(preguntasLogica);
            todasLasPreguntas.addAll(preguntasMatematica);
            todasLasPreguntas.addAll(preguntasCreatividad);
            todasLasPreguntas.addAll(preguntasProgramacion);
            
            System.out.println("Total de preguntas combinadas: " + todasLasPreguntas.size());
            
            // Mezclar las preguntas para que no estén agrupadas por área
            Collections.shuffle(todasLasPreguntas);
            
            return todasLasPreguntas;
            
        } catch (Exception e) {
            System.err.println("Error al generar preguntas del examen: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * Procesa las respuestas del examen y calcula las puntuaciones
     */
    @Transactional
    public Examen procesarRespuestas(Long examenId, Map<Long, Integer> respuestas) {
        System.out.println("=== INICIO PROCESAR RESPUESTAS ===");
        System.out.println("Procesando respuestas para examen ID: " + examenId);
        System.out.println("Respuestas recibidas: " + respuestas);
        
        Examen examen = examenRepository.findById(examenId)
            .orElseThrow(() -> new RuntimeException("Examen no encontrado"));
        
        System.out.println("Examen encontrado para persona: " + examen.getPersona().getEmail());
        
        // Marcar fin del examen
        examen.setFechaFin(LocalDateTime.now());
        
        // Calcular tiempo total
        if (examen.getFechaInicio() != null) {
            long minutos = ChronoUnit.MINUTES.between(examen.getFechaInicio(), examen.getFechaFin());
            examen.setTiempoTotalMinutos((int) minutos);
        }
        
        // Procesar cada respuesta
        List<RespuestaExamen> respuestasExamen = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : respuestas.entrySet()) {
            Long preguntaId = entry.getKey();
            Integer respuestaSeleccionada = entry.getValue();
            
            System.out.println("Procesando pregunta ID: " + preguntaId + ", respuesta: " + respuestaSeleccionada);
            
            Pregunta pregunta = preguntaRepository.findById(preguntaId)
                .orElseThrow(() -> new RuntimeException("Pregunta no encontrada: " + preguntaId));
            
            System.out.println("Pregunta encontrada: " + pregunta.getEnunciado());
            System.out.println("Opción correcta de la pregunta: " + pregunta.getOpcionCorrecta());
            
            RespuestaExamen respuestaExamen = new RespuestaExamen(examen, pregunta, respuestaSeleccionada);
            System.out.println("Respuesta creada - Es correcta: " + respuestaExamen.isEsCorrecta());
            respuestasExamen.add(respuestaExamen);
        }
        
        System.out.println("Total de respuestas a guardar: " + respuestasExamen.size());
        
        // Guardar respuestas
        List<RespuestaExamen> respuestasGuardadas = respuestaExamenRepository.saveAll(respuestasExamen);
        System.out.println("Respuestas guardadas exitosamente: " + respuestasGuardadas.size());
        
        // Asignar las respuestas al examen
        examen.setRespuestas(respuestasGuardadas);
        
        // Calcular puntuaciones
        System.out.println("Calculando puntuaciones...");
        examen.calcularPuntuaciones();
        
        System.out.println("Puntuaciones calculadas:");
        System.out.println("  - Programación Básica: " + examen.getProgramacionBasica());
        System.out.println("  - Estructuras Datos: " + examen.getEstructurasDatos());
        System.out.println("  - Algoritmos: " + examen.getAlgoritmos());
        System.out.println("  - Base Datos: " + examen.getBaseDatos());
        System.out.println("  - Total Preguntas: " + examen.getTotalPreguntas());
        System.out.println("  - Respuestas Correctas: " + examen.getRespuestasCorrectas());
        System.out.println("  - Promedio: " + examen.getPromedio());
        
        // Guardar el examen actualizado
        Examen examenGuardado = examenRepository.save(examen);
        System.out.println("Examen guardado exitosamente");
        System.out.println("=== FIN PROCESAR RESPUESTAS ===");
        
        return examenGuardado;
    }
    
    /**
     * Obtiene las estadísticas del examen para el dashboard
     */
    public Map<String, Object> obtenerEstadisticas() {
        Map<String, Object> estadisticas = new HashMap<>();
        
        List<Examen> examenes = examenRepository.findAll();
        
        if (examenes.isEmpty()) {
            estadisticas.put("totalInscripciones", 0);
            estadisticas.put("examenesCompletados", 0);
            estadisticas.put("promedioCalificacion", 0.0);
            estadisticas.put("promedioPorArea", new HashMap<>());
            return estadisticas;
        }
        
        // Estadísticas generales
        estadisticas.put("totalInscripciones", examenes.size());
        estadisticas.put("examenesCompletados", examenes.size());
        
        // Promedio general
        double promedioGeneral = examenes.stream()
            .mapToDouble(Examen::getPromedio)
            .average()
            .orElse(0.0);
        estadisticas.put("promedioCalificacion", Math.round(promedioGeneral * 10.0) / 10.0);
        
        // Promedio por área
        Map<String, Double> promedioPorArea = new HashMap<>();
        promedioPorArea.put("programacionBasica", 
            examenes.stream().mapToDouble(e -> e.getProgramacionBasica() != null ? e.getProgramacionBasica() : 0).average().orElse(0.0));
        promedioPorArea.put("estructurasDatos", 
            examenes.stream().mapToDouble(e -> e.getEstructurasDatos() != null ? e.getEstructurasDatos() : 0).average().orElse(0.0));
        promedioPorArea.put("algoritmos", 
            examenes.stream().mapToDouble(e -> e.getAlgoritmos() != null ? e.getAlgoritmos() : 0).average().orElse(0.0));
        promedioPorArea.put("baseDatos", 
            examenes.stream().mapToDouble(e -> e.getBaseDatos() != null ? e.getBaseDatos() : 0).average().orElse(0.0));
        
        estadisticas.put("promedioPorArea", promedioPorArea);
        
        return estadisticas;
    }
    
    /**
     * Inicializa preguntas de ejemplo si no existen
     */
    @Transactional
    public void inicializarPreguntasEjemplo() {
        if (preguntaRepository.count() > 0) {
            return; // Ya existen preguntas
        }
        
        // Crear preguntas de ejemplo para cada área
        crearPreguntasLogica();
        crearPreguntasMatematica();
        crearPreguntasCreatividad();
        crearPreguntasProgramacion();
    }
    
    private void crearPreguntasLogica() {
        // Pregunta 1
        Pregunta p1 = new Pregunta("¿Cuál es el siguiente número en la secuencia: 2, 4, 8, 16, ...?", 
            Pregunta.AreaConocimiento.LOGICA, 1);
        p1 = preguntaRepository.save(p1);
        
        List<Opcion> opciones1 = new ArrayList<>(Arrays.asList(
            new Opcion("32", p1, 1),
            new Opcion("30", p1, 2),
            new Opcion("28", p1, 3),
            new Opcion("34", p1, 4)
        );
        opcionRepository.saveAll(opciones1);
        p1.setOpciones(opciones1);
        preguntaRepository.save(p1);
        
        // Pregunta 2
        Pregunta p2 = new Pregunta("Si todos los programadores son lógicos y Juan es programador, entonces:", 
            Pregunta.AreaConocimiento.LOGICA, 1);
        p2 = preguntaRepository.save(p2);
        
        List<Opcion> opciones2 = new ArrayList<>(Arrays.asList(
            new Opcion("Juan es lógico", p2, 1),
            new Opcion("Juan no es lógico", p2, 2),
            new Opcion("No se puede determinar", p2, 3),
            new Opcion("Juan es programador", p2, 4)
        );
        opcionRepository.saveAll(opciones2);
        p2.setOpciones(opciones2);
        preguntaRepository.save(p2);
        
        // Pregunta 3
        Pregunta p3 = new Pregunta("¿Cuál es el siguiente número en la secuencia: 1, 3, 6, 10, 15, ...?", 
            Pregunta.AreaConocimiento.LOGICA, 1);
        p3 = preguntaRepository.save(p3);
        
        List<Opcion> opciones3 = new ArrayList<>(Arrays.asList(
            new Opcion("21", p3, 1),
            new Opcion("20", p3, 2),
            new Opcion("22", p3, 3),
            new Opcion("19", p3, 4)
        );
        opcionRepository.saveAll(opciones3);
        p3.setOpciones(opciones3);
        preguntaRepository.save(p3);
        
        // Pregunta 4
        Pregunta p4 = new Pregunta("Si A = B y B = C, entonces:", 
            Pregunta.AreaConocimiento.LOGICA, 1);
        p4 = preguntaRepository.save(p4);
        
        List<Opcion> opciones4 = new ArrayList<>(Arrays.asList(
            new Opcion("A = C", p4, 1),
            new Opcion("A ≠ C", p4, 2),
            new Opcion("No se puede determinar", p4, 3),
            new Opcion("A > C", p4, 4)
        );
        opcionRepository.saveAll(opciones4);
        p4.setOpciones(opciones4);
        preguntaRepository.save(p4);
        
        // Pregunta 5
        Pregunta p5 = new Pregunta("¿Cuál es la negación de 'Todos los estudiantes son inteligentes'?", 
            Pregunta.AreaConocimiento.LOGICA, 1);
        p5 = preguntaRepository.save(p5);
        
        List<Opcion> opciones5 = new ArrayList<>(Arrays.asList(
            new Opcion("Al menos un estudiante no es inteligente", p5, 1),
            new Opcion("Ningún estudiante es inteligente", p5, 2),
            new Opcion("Todos los estudiantes son tontos", p5, 3),
            new Opcion("Algunos estudiantes son inteligentes", p5, 4)
        );
        opcionRepository.saveAll(opciones5);
        p5.setOpciones(opciones5);
        preguntaRepository.save(p5);
        
        // Pregunta 6
        Pregunta p6 = new Pregunta("¿Cuál es el patrón en la secuencia: 2, 6, 12, 20, 30, ...?", 
            Pregunta.AreaConocimiento.LOGICA, 1);
        p6 = preguntaRepository.save(p6);
        
        List<Opcion> opciones6 = new ArrayList<>(Arrays.asList(
            new Opcion("Sumar números pares consecutivos", p6, 1),
            new Opcion("Multiplicar por 2", p6, 2),
            new Opcion("Sumar 4", p6, 3),
            new Opcion("Multiplicar por 3", p6, 4)
        );
        opcionRepository.saveAll(opciones6);
        p6.setOpciones(opciones6);
        preguntaRepository.save(p6);
        
        // Pregunta 7
        Pregunta p7 = new Pregunta("Si P implica Q y Q es falso, entonces:", 
            Pregunta.AreaConocimiento.LOGICA, 1);
        p7 = preguntaRepository.save(p7);
        
        List<Opcion> opciones7 = new ArrayList<>(Arrays.asList(
            new Opcion("P debe ser falso", p7, 1),
            new Opcion("P debe ser verdadero", p7, 2),
            new Opcion("No se puede determinar P", p7, 3),
            new Opcion("P puede ser verdadero o falso", p7, 4)
        );
        opcionRepository.saveAll(opciones7);
        p7.setOpciones(opciones7);
        preguntaRepository.save(p7);
        
        // Pregunta 8
        Pregunta p8 = new Pregunta("¿Cuál es el siguiente número en la secuencia: 1, 4, 9, 16, 25, ...?", 
            Pregunta.AreaConocimiento.LOGICA, 1);
        p8 = preguntaRepository.save(p8);
        
        List<Opcion> opciones8 = new ArrayList<>(Arrays.asList(
            new Opcion("36", p8, 1),
            new Opcion("35", p8, 2),
            new Opcion("37", p8, 3),
            new Opcion("34", p8, 4)
        );
        opcionRepository.saveAll(opciones8);
        p8.setOpciones(opciones8);
        preguntaRepository.save(p8);
        

    }
    
    private void crearPreguntasMatematica() {
        // Pregunta 1
        Pregunta p1 = new Pregunta("¿Cuál es el resultado de 15 x 7?", 
            Pregunta.AreaConocimiento.MATEMATICA, 1);
        p1 = preguntaRepository.save(p1);
        
        List<Opcion> opciones1 = new ArrayList<>(Arrays.asList(
            new Opcion("105", p1, 1),
            new Opcion("95", p1, 2),
            new Opcion("115", p1, 3),
            new Opcion("125", p1, 4)
        );
        opcionRepository.saveAll(opciones1);
        p1.setOpciones(opciones1);
        preguntaRepository.save(p1);
        
        // Pregunta 2
        Pregunta p2 = new Pregunta("¿Cuál es el 20% de 150?", 
            Pregunta.AreaConocimiento.MATEMATICA, 1);
        p2 = preguntaRepository.save(p2);
        
        List<Opcion> opciones2 = new ArrayList<>(Arrays.asList(
            new Opcion("30", p2, 1),
            new Opcion("25", p2, 2),
            new Opcion("35", p2, 3),
            new Opcion("40", p2, 4)
        );
        opcionRepository.saveAll(opciones2);
        p2.setOpciones(opciones2);
        preguntaRepository.save(p2);
        
        // Pregunta 3
        Pregunta p3 = new Pregunta("¿Cuál es la raíz cuadrada de 64?", 
            Pregunta.AreaConocimiento.MATEMATICA, 1);
        p3 = preguntaRepository.save(p3);
        
        List<Opcion> opciones3 = new ArrayList<>(Arrays.asList(
            new Opcion("8", p3, 1),
            new Opcion("6", p3, 2),
            new Opcion("10", p3, 3),
            new Opcion("12", p3, 4)
        );
        opcionRepository.saveAll(opciones3);
        p3.setOpciones(opciones3);
        preguntaRepository.save(p3);
        
        // Pregunta 4
        Pregunta p4 = new Pregunta("¿Cuál es el promedio de 10, 15, 20, 25?", 
            Pregunta.AreaConocimiento.MATEMATICA, 1);
        p4 = preguntaRepository.save(p4);
        
        List<Opcion> opciones4 = new ArrayList<>(Arrays.asList(
            new Opcion("17.5", p4, 1),
            new Opcion("16.5", p4, 2),
            new Opcion("18.5", p4, 3),
            new Opcion("19.5", p4, 4)
        );
        opcionRepository.saveAll(opciones4);
        p4.setOpciones(opciones4);
        preguntaRepository.save(p4);
        
        // Pregunta 5
        Pregunta p5 = new Pregunta("¿Cuál es el resultado de 2^5?", 
            Pregunta.AreaConocimiento.MATEMATICA, 1);
        p5 = preguntaRepository.save(p5);
        
        List<Opcion> opciones5 = new ArrayList<>(Arrays.asList(
            new Opcion("32", p5, 1),
            new Opcion("25", p5, 2),
            new Opcion("30", p5, 3),
            new Opcion("35", p5, 4)
        );
        opcionRepository.saveAll(opciones5);
        p5.setOpciones(opciones5);
        preguntaRepository.save(p5);
        
        // Pregunta 6
        Pregunta p6 = new Pregunta("¿Cuál es el área de un cuadrado de lado 6?", 
            Pregunta.AreaConocimiento.MATEMATICA, 1);
        p6 = preguntaRepository.save(p6);
        
        List<Opcion> opciones6 = new ArrayList<>(Arrays.asList(
            new Opcion("36", p6, 1),
            new Opcion("24", p6, 2),
            new Opcion("30", p6, 3),
            new Opcion("42", p6, 4)
        );
        opcionRepository.saveAll(opciones6);
        p6.setOpciones(opciones6);
        preguntaRepository.save(p6);
        
        // Pregunta 7
        Pregunta p7 = new Pregunta("¿Cuál es el resultado de 100 ÷ 4?", 
            Pregunta.AreaConocimiento.MATEMATICA, 1);
        p7 = preguntaRepository.save(p7);
        
        List<Opcion> opciones7 = new ArrayList<>(Arrays.asList(
            new Opcion("25", p7, 1),
            new Opcion("20", p7, 2),
            new Opcion("30", p7, 3),
            new Opcion("15", p7, 4)
        );
        opcionRepository.saveAll(opciones7);
        p7.setOpciones(opciones7);
        preguntaRepository.save(p7);
        
        // Pregunta 8
        Pregunta p8 = new Pregunta("¿Cuál es el resultado de 3 + 7 x 2?", 
            Pregunta.AreaConocimiento.MATEMATICA, 1);
        p8 = preguntaRepository.save(p8);
        
        List<Opcion> opciones8 = new ArrayList<>(Arrays.asList(
            new Opcion("17", p8, 1),
            new Opcion("20", p8, 2),
            new Opcion("14", p8, 3),
            new Opcion("23", p8, 4)
        );
        opcionRepository.saveAll(opciones8);
        p8.setOpciones(opciones8);
        preguntaRepository.save(p8);
    }
    
    private void crearPreguntasCreatividad() {
        // Pregunta 1
        Pregunta p1 = new Pregunta("¿Cómo mejorarías la experiencia de usuario de una aplicación de transporte?", 
            Pregunta.AreaConocimiento.CREATIVIDAD, 1);
        p1 = preguntaRepository.save(p1);
        
        List<Opcion> opciones1 = new ArrayList<>(Arrays.asList(
            new Opcion("Agregando un sistema de gamificación", p1, 1),
            new Opcion("Eliminando todas las funciones", p1, 2),
            new Opcion("Haciendo la interfaz más compleja", p1, 3),
            new Opcion("Reduciendo las opciones de pago", p1, 4)
        );
        opcionRepository.saveAll(opciones1);
        p1.setOpciones(opciones1);
        preguntaRepository.save(p1);
        
        // Pregunta 2
        Pregunta p2 = new Pregunta("¿Qué color usarías para representar la confianza en una aplicación bancaria?", 
            Pregunta.AreaConocimiento.CREATIVIDAD, 1);
        p2 = preguntaRepository.save(p2);
        
        List<Opcion> opciones2 = new ArrayList<>(Arrays.asList(
            new Opcion("Azul", p2, 1),
            new Opcion("Rojo", p2, 2),
            new Opcion("Amarillo", p2, 3),
            new Opcion("Verde", p2, 4)
        );
        opcionRepository.saveAll(opciones2);
        p2.setOpciones(opciones2);
        preguntaRepository.save(p2);
        
        // Pregunta 3
        Pregunta p3 = new Pregunta("¿Cómo diseñarías un logo para una empresa de tecnología verde?", 
            Pregunta.AreaConocimiento.CREATIVIDAD, 1);
        p3 = preguntaRepository.save(p3);
        
        List<Opcion> opciones3 = new ArrayList<>(Arrays.asList(
            new Opcion("Usando elementos naturales y tecnología", p3, 1),
            new Opcion("Solo usando colores oscuros", p3, 2),
            new Opcion("Sin considerar el medio ambiente", p3, 3),
            new Opcion("Copiando otros logos", p3, 4)
        );
        opcionRepository.saveAll(opciones3);
        p3.setOpciones(opciones3);
        preguntaRepository.save(p3);
        
        // Pregunta 4
        Pregunta p4 = new Pregunta("¿Qué función agregarías a un smartphone para hacerlo más útil?", 
            Pregunta.AreaConocimiento.CREATIVIDAD, 1);
        p4 = preguntaRepository.save(p4);
        
        List<Opcion> opciones4 = new ArrayList<>(Arrays.asList(
            new Opcion("Un asistente de salud personal", p4, 1),
            new Opcion("Más botones físicos", p4, 2),
            new Opcion("Una pantalla más pequeña", p4, 3),
            new Opcion("Menos aplicaciones", p4, 4)
        );
        opcionRepository.saveAll(opciones4);
        p4.setOpciones(opciones4);
        preguntaRepository.save(p4);
        
        // Pregunta 5
        Pregunta p5 = new Pregunta("¿Cómo harías más atractiva una página web de una tienda online?", 
            Pregunta.AreaConocimiento.CREATIVIDAD, 1);
        p5 = preguntaRepository.save(p5);
        
        List<Opcion> opciones5 = new ArrayList<>(Arrays.asList(
            new Opcion("Usando imágenes de alta calidad y testimonios", p5, 1),
            new Opcion("Agregando más texto", p5, 2),
            new Opcion("Usando solo colores grises", p5, 3),
            new Opcion("Eliminando las imágenes", p5, 4)
        );
        opcionRepository.saveAll(opciones5);
        p5.setOpciones(opciones5);
        preguntaRepository.save(p5);
        
        // Pregunta 6
        Pregunta p6 = new Pregunta("¿Qué innovación aplicarías a un sistema de educación online?", 
            Pregunta.AreaConocimiento.CREATIVIDAD, 1);
        p6 = preguntaRepository.save(p6);
        
        List<Opcion> opciones6 = new ArrayList<>(Arrays.asList(
            new Opcion("Realidad virtual para prácticas", p6, 1),
            new Opcion("Solo videos largos", p6, 2),
            new Opcion("Sin interacción", p6, 3),
            new Opcion("Sin evaluación", p6, 4)
        );
        opcionRepository.saveAll(opciones6);
        p6.setOpciones(opciones6);
        preguntaRepository.save(p6);
        
        // Pregunta 7
        Pregunta p7 = new Pregunta("¿Cómo mejorarías la accesibilidad de una aplicación para personas con discapacidad visual?", 
            Pregunta.AreaConocimiento.CREATIVIDAD, 1);
        p7 = preguntaRepository.save(p7);
        
        List<Opcion> opciones7 = new ArrayList<>(Arrays.asList(
            new Opcion("Agregando funciones de audio y alto contraste", p7, 1),
            new Opcion("Usando solo colores claros", p7, 2),
            new Opcion("Eliminando el texto", p7, 3),
            new Opcion("Sin considerar la accesibilidad", p7, 4)
        );
        opcionRepository.saveAll(opciones7);
        p7.setOpciones(opciones7);
        preguntaRepository.save(p7);
        
        // Pregunta 8
        Pregunta p8 = new Pregunta("¿Qué elemento agregarías a un parque para hacerlo más atractivo para niños?", 
            Pregunta.AreaConocimiento.CREATIVIDAD, 1);
        p8 = preguntaRepository.save(p8);
        
        List<Opcion> opciones8 = new ArrayList<>(Arrays.asList(
            new Opcion("Un área de juegos interactivos", p8, 1),
            new Opcion("Solo bancos", p8, 2),
            new Opcion("Sin colores", p8, 3),
            new Opcion("Sin juegos", p8, 4)
        );
        opcionRepository.saveAll(opciones8);
        p8.setOpciones(opciones8);
        preguntaRepository.save(p8);
    }
    
    private void crearPreguntasProgramacion() {
        // Pregunta 1
        Pregunta p1 = new Pregunta("¿Qué es una variable en programación?", 
            Pregunta.AreaConocimiento.PROGRAMACION, 1);
        p1 = preguntaRepository.save(p1);
        
        List<Opcion> opciones1 = new ArrayList<>(Arrays.asList(
            new Opcion("Un contenedor que almacena datos", p1, 1),
            new Opcion("Un tipo de bucle", p1, 2),
            new Opcion("Una función", p1, 3),
            new Opcion("Un comentario", p1, 4)
        );
        opcionRepository.saveAll(opciones1);
        p1.setOpciones(opciones1);
        preguntaRepository.save(p1);
        
        // Pregunta 2
        Pregunta p2 = new Pregunta("¿Cuál es la diferencia entre '==' y '===' en JavaScript?", 
            Pregunta.AreaConocimiento.PROGRAMACION, 2);
        p2 = preguntaRepository.save(p2);
        
        List<Opcion> opciones2 = new ArrayList<>(Arrays.asList(
            new Opcion("No hay diferencia", p2, 1),
            new Opcion("'===' compara valor y tipo, '==' solo valor", p2, 2),
            new Opcion("'==' es más rápido", p2, 3),
            new Opcion("'===' es obsoleto", p2, 4)
        );
        opcionRepository.saveAll(opciones2);
        p2.setOpciones(opciones2);
        preguntaRepository.save(p2);
        
        // Pregunta 3
        Pregunta p3 = new Pregunta("¿Qué es la programación orientada a objetos?", 
            Pregunta.AreaConocimiento.PROGRAMACION, 1);
        p3 = preguntaRepository.save(p3);
        
        List<Opcion> opciones3 = new ArrayList<>(Arrays.asList(
            new Opcion("Un paradigma que organiza el código en objetos", p3, 1),
            new Opcion("Un tipo de base de datos", p3, 2),
            new Opcion("Un lenguaje de programación", p3, 3),
            new Opcion("Un framework web", p3, 4)
        );
        opcionRepository.saveAll(opciones3);
        p3.setOpciones(opciones3);
        preguntaRepository.save(p3);
        
        // Pregunta 4
        Pregunta p4 = new Pregunta("¿Qué es un bucle?", 
            Pregunta.AreaConocimiento.PROGRAMACION, 1);
        p4 = preguntaRepository.save(p4);
        
        List<Opcion> opciones4 = new ArrayList<>(Arrays.asList(
            new Opcion("Una estructura que repite código un número específico de veces", p4, 1),
            new Opcion("Una función matemática", p4, 2),
            new Opcion("Un tipo de variable", p4, 3),
            new Opcion("Un error de programación", p4, 4)
        );
        opcionRepository.saveAll(opciones4);
        p4.setOpciones(opciones4);
        preguntaRepository.save(p4);
        
        // Pregunta 5
        Pregunta p5 = new Pregunta("¿Qué es una función?", 
            Pregunta.AreaConocimiento.PROGRAMACION, 1);
        p5 = preguntaRepository.save(p5);
        
        List<Opcion> opciones5 = new ArrayList<>(Arrays.asList(
            new Opcion("Un bloque de código reutilizable", p5, 1),
            new Opcion("Una variable global", p5, 2),
            new Opcion("Un tipo de dato", p5, 3),
            new Opcion("Un comentario", p5, 4)
        );
        opcionRepository.saveAll(opciones5);
        p5.setOpciones(opciones5);
        preguntaRepository.save(p5);
        
        // Pregunta 6
        Pregunta p6 = new Pregunta("¿Qué es un array?", 
            Pregunta.AreaConocimiento.PROGRAMACION, 1);
        p6 = preguntaRepository.save(p6);
        
        List<Opcion> opciones6 = new ArrayList<>(Arrays.asList(
            new Opcion("Una colección ordenada de elementos", p6, 1),
            new Opcion("Un tipo de bucle", p6, 2),
            new Opcion("Una función", p6, 3),
            new Opcion("Una variable", p6, 4)
        );
        opcionRepository.saveAll(opciones6);
        p6.setOpciones(opciones6);
        preguntaRepository.save(p6);
        
        // Pregunta 7
        Pregunta p7 = new Pregunta("¿Qué es la recursión?", 
            Pregunta.AreaConocimiento.PROGRAMACION, 1);
        p7 = preguntaRepository.save(p7);
        
        List<Opcion> opciones7 = new ArrayList<>(Arrays.asList(
            new Opcion("Una función que se llama a sí misma", p7, 1),
            new Opcion("Un tipo de bucle", p7, 2),
            new Opcion("Una variable", p7, 3),
            new Opcion("Un error", p7, 4)
        );
        opcionRepository.saveAll(opciones7);
        p7.setOpciones(opciones7);
        preguntaRepository.save(p7);
        
        // Pregunta 8
        Pregunta p8 = new Pregunta("¿Qué es un string?", 
            Pregunta.AreaConocimiento.PROGRAMACION, 1);
        p8 = preguntaRepository.save(p8);
        
        List<Opcion> opciones8 = new ArrayList<>(Arrays.asList(
            new Opcion("Una secuencia de caracteres", p8, 1),
            new Opcion("Un número entero", p8, 2),
            new Opcion("Un booleano", p8, 3),
            new Opcion("Un array", p8, 4)
        );
        opcionRepository.saveAll(opciones8);
        p8.setOpciones(opciones8);
        preguntaRepository.save(p8);
    }
    

} 
