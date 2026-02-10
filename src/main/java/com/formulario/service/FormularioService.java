package com.formulario.service;

import com.formulario.model.Examen;
import com.formulario.model.Persona;
import com.formulario.model.InscripcionDTO;
import com.formulario.repository.ExamenRepository;
import com.formulario.repository.PersonaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.formulario.model.RespuestaExamen;
import com.formulario.model.ResultadoDTO;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.time.format.DateTimeFormatter;
import java.io.IOException;

@Service
public class FormularioService {
    
    @Autowired
    private PersonaRepository personaRepository;
    
    @Autowired
    private ExamenRepository examenRepository;
    
    @Autowired
    private ExcelService excelService;
    
    // Métodos para Persona
    public Persona guardarPersona(Persona persona) {
        return personaRepository.save(persona);
    }
    
    public Optional<Persona> buscarPersonaPorId(Long id) {
        return personaRepository.findById(id);
    }
    
    public Persona buscarPersonaPorEmail(String email) {
        return personaRepository.findByEmail(email);
    }
    
    public boolean existeEmail(String email) {
        return personaRepository.existsByEmail(email);
    }
    
    public List<Persona> listarTodasLasPersonas() {
        return personaRepository.findAll();
    }
    
    // Métodos para Examen
    public Examen guardarExamen(Examen examen) {
        return examenRepository.save(examen);
    }
    
    public Optional<Examen> buscarExamenPorId(Long id) {
        return examenRepository.findById(id);
    }
    
    /**
     * Busca un examen por token (hash calculado)
     * El token se valida y se extrae el ID del examen
     */
    public Optional<Examen> buscarExamenPorToken(String token) {
        Long examenId = com.formulario.util.ExamenTokenUtil.validarYExtraerId(token);
        if (examenId == null) {
            return Optional.empty();
        }
        return examenRepository.findById(examenId);
    }
    
    /**
     * Busca examen por token o ID (compatibilidad)
     * Intenta primero como token, luego como ID numérico
     */
    public Optional<Examen> buscarExamenPorTokenOId(String identificador) {
        // Intentar como token primero
        Optional<Examen> porToken = buscarExamenPorToken(identificador);
        if (porToken.isPresent()) {
            return porToken;
        }
        
        // Si no es token válido, intentar como ID numérico
        try {
            Long id = Long.parseLong(identificador);
            return examenRepository.findById(id);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
    
    public Optional<Examen> buscarExamenPorPersona(Persona persona) {
        return examenRepository.findByPersona(persona);
    }
    
    public boolean existeExamenParaPersona(Persona persona) {
        return examenRepository.existsByPersona(persona);
    }
    
    /**
     * Elimina el examen asociado a un caso de Bondarea cuando el caso es eliminado allí.
     * Usado por el webhook /api/bondarea/caso-eliminado.
     * @param idCaso ID del caso en Bondarea (idCasoBondarea de la persona)
     * @return true si se eliminó un examen o no había nada que eliminar (sincronizado); false solo en error
     */
    @Transactional
    public boolean eliminarExamenPorIdCasoBondarea(String idCaso) {
        if (idCaso == null || idCaso.trim().isEmpty()) {
            return true;
        }
        Optional<Persona> personaOpt = personaRepository.findByIdCasoBondarea(idCaso.trim());
        if (personaOpt.isEmpty()) {
            return true; // Sin persona asociada, nada que eliminar
        }
        Optional<Examen> examenOpt = examenRepository.findByPersona(personaOpt.get());
        if (examenOpt.isEmpty()) {
            return true; // Sin examen asociado, ya sincronizado
        }
        examenRepository.delete(examenOpt.get());
        return true;
    }
    
    public List<Examen> listarTodosLosExamenes() {
        return examenRepository.findAll();
    }
    
    // Método para obtener el resultado completo (persona + examen)
    @Transactional(readOnly = true)
    public Optional<Examen> obtenerResultadoCompleto(Long personaId) {
        try {
            // Buscar la persona primero
            Optional<Persona> persona = buscarPersonaPorId(personaId);
            if (persona.isEmpty()) {
                System.out.println("Persona no encontrada con ID: " + personaId);
                return Optional.empty();
            }
            
            // Buscar el examen para esa persona
            Optional<Examen> examenOpt = buscarExamenPorPersona(persona.get());
            if (examenOpt.isEmpty()) {
                System.out.println("Examen no encontrado para persona ID: " + personaId);
                return Optional.empty();
            }
            
            Examen examen = examenOpt.get();
            System.out.println("Examen encontrado - ID: " + examen.getId());
            
            // Verificar que el examen esté completado
            if (examen.getFechaFin() == null) {
                System.out.println("Examen no completado - ID: " + examen.getId());
                return Optional.empty();
            }
            
            // Verificar que las puntuaciones estén calculadas
            if (examen.getLogica() == null || 
                examen.getMatematica() == null || 
                examen.getCreatividad() == null || 
                examen.getProgramacion() == null) {
                System.out.println("Puntuaciones no calculadas - recalculando...");
                // Recalcular puntuaciones si es necesario
                examen.calcularPuntuaciones();
                examenRepository.save(examen);
                System.out.println("Puntuaciones recalculadas");
            }
            
            System.out.println("Resultado cargado exitosamente");
            return Optional.of(examen);
            
        } catch (Exception e) {
            System.err.println("Error al obtener resultado completo: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }
    
    // Método para obtener el resultado como DTO (evita problemas de lazy loading)
    @Transactional(readOnly = true)
    public Optional<ResultadoDTO> obtenerResultadoDTO(Long personaId) {
        try {
            Optional<Examen> examenOpt = obtenerResultadoCompleto(personaId);
            if (examenOpt.isPresent()) {
                ResultadoDTO resultado = new ResultadoDTO(examenOpt.get());
                return Optional.of(resultado);
            }
            return Optional.empty();
        } catch (Exception e) {
            System.err.println("Error al obtener resultado DTO: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }
    
    // Método para obtener todas las inscripciones con resultados
    public List<InscripcionDTO> obtenerTodasLasInscripciones() {
        try {
            System.out.println("=== INICIO obtenerTodasLasInscripciones ===");
            
            // Usar el método estándar primero para evitar problemas con JOIN FETCH
            List<Examen> examenes = examenRepository.findAll();
            System.out.println("Total de exámenes encontrados: " + examenes.size());
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            
            List<InscripcionDTO> resultado = examenes.stream()
                .filter(examen -> {
                    boolean tienePersona = examen.getPersona() != null;
                    if (!tienePersona) {
                        System.out.println("Examen ID " + examen.getId() + " sin persona asociada");
                    }
                    return tienePersona;
                })
                .map(examen -> {
                    Persona persona = examen.getPersona();
                    String fechaExamen = examen.getFechaFin() != null ? 
                        examen.getFechaFin().format(formatter) : "No completado";
                    
                    System.out.println("Procesando examen ID: " + examen.getId() + " para persona: " + persona.getEmail());
                    
                    // Forzar la carga de las respuestas para asegurar que las puntuaciones estén calculadas
                    if (examen.getRespuestas() != null) {
                        int respuestasCount = examen.getRespuestas().size();
                        System.out.println("  - Respuestas cargadas: " + respuestasCount);
                        
                        // Si el examen está completado pero las puntuaciones son null, recalcular
                        if (examen.getFechaFin() != null && 
                            (examen.getLogica() == null || 
                             examen.getMatematica() == null || 
                             examen.getCreatividad() == null || 
                             examen.getProgramacion() == null)) {
                            System.out.println("  - Recalculando puntuaciones para examen ID: " + examen.getId());
                            examen.calcularPuntuaciones();
                            examenRepository.save(examen);
                        }
                    } else {
                        System.out.println("  - No hay respuestas asociadas");
                    }
                    
                    // Log de puntuaciones
                    System.out.println("  - Lógica: " + examen.getLogica());
                    System.out.println("  - Matemática: " + examen.getMatematica());
                    System.out.println("  - Creatividad: " + examen.getCreatividad());
                    System.out.println("  - Programación: " + examen.getProgramacion());
                    System.out.println("  - Promedio: " + examen.getPromedio());
                    System.out.println("  - Aprobado: " + examen.isAprobado());
                    
                    InscripcionDTO dto = new InscripcionDTO(
                        examen.getId(),
                        persona.getNombre(),
                        persona.getApellido(),
                        persona.getCuil(),
                        persona.getEmail(),
                        persona.getTrabajaActualmente(),
                        persona.getTrabajaSectorIT(),
                        examen.getLogica(),
                        examen.getMatematica(),
                        examen.getCreatividad(),
                        examen.getProgramacion(),
                        examen.getPromedio(),
                        examen.isAprobado(),
                        fechaExamen
                    );
                    
                    System.out.println("  - DTO creado exitosamente");
                    return dto;
                })
                .collect(Collectors.toList());
            
            System.out.println("Total de DTOs creados: " + resultado.size());
            System.out.println("=== FIN obtenerTodasLasInscripciones ===");
            
            return resultado;
        } catch (Exception e) {
            System.err.println("Error en obtenerTodasLasInscripciones: " + e.getMessage());
            e.printStackTrace();
            // Si hay error, devolver lista vacía
            return new ArrayList<>();
        }
    }
    
    // Método para generar Excel de inscripciones
    public byte[] generarExcelInscripciones() throws IOException {
        // Delegar al ExcelService
        return excelService.generarExcelInscripciones();
    }
} 