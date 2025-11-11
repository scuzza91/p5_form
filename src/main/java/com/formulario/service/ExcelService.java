package com.formulario.service;

import com.formulario.model.*;
import com.formulario.repository.PersonaRepository;
import com.formulario.repository.ExamenRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
public class ExcelService {
    
    @Autowired
    private PersonaRepository personaRepository;
    
    @Autowired
    private ExamenRepository examenRepository;
    
    public byte[] generarExcelInscripciones() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            
            // Crear hoja de resumen
            Sheet sheetResumen = workbook.createSheet("Resumen");
            crearHojaResumen(sheetResumen);
            
            // Crear hoja de datos personales
            Sheet sheetDatosPersonales = workbook.createSheet("Datos Personales");
            crearHojaDatosPersonales(sheetDatosPersonales);
            
            // Crear hoja de resultados por área
            Sheet sheetResultados = workbook.createSheet("Resultados por Área");
            crearHojaResultados(sheetResultados);
            
            // Crear hoja de preguntas y respuestas
            Sheet sheetPreguntasRespuestas = workbook.createSheet("Preguntas y Respuestas");
            crearHojaPreguntasRespuestas(sheetPreguntasRespuestas);
            
            // Crear hoja de estadísticas
            Sheet sheetEstadisticas = workbook.createSheet("Estadísticas");
            crearHojaEstadisticas(sheetEstadisticas);
            
            // Escribir el archivo
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
    
    private void crearHojaResumen(Sheet sheet) {
        // Crear estilos
        CellStyle headerStyle = crearEstiloHeader(sheet.getWorkbook());
        CellStyle dataStyle = crearEstiloDatos(sheet.getWorkbook());
        
        // Crear encabezados
        Row headerRow = sheet.createRow(0);
        String[] headers = {
            "ID", "Nombre", "Apellido", "DNI", "CUIL", "Email", 
            "Trabaja Actualmente", "Sector IT", "Programación", "Lógica", 
            "Matemática", "Creatividad", "Promedio", "Aprobado", "Fecha Examen"
        };
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
            sheet.setColumnWidth(i, 15 * 256); // Ancho de columna
        }
        
        // Obtener datos
        List<InscripcionDTO> inscripciones = obtenerTodasLasInscripciones();
        
        // Llenar datos
        int rowNum = 1;
        for (InscripcionDTO inscripcion : inscripciones) {
            Row row = sheet.createRow(rowNum++);
            
            row.createCell(0).setCellValue(inscripcion.getId());
            row.createCell(1).setCellValue(inscripcion.getNombre());
            row.createCell(2).setCellValue(inscripcion.getApellido());
            row.createCell(3).setCellValue(inscripcion.getDni());
            row.createCell(4).setCellValue(inscripcion.getCuil());
            row.createCell(5).setCellValue(inscripcion.getEmail());
            row.createCell(6).setCellValue(inscripcion.getTrabajaActualmente());
            row.createCell(7).setCellValue(inscripcion.getTrabajaSectorIT());
            row.createCell(8).setCellValue(inscripcion.getProgramacionBasica() != null ? inscripcion.getProgramacionBasica() : 0);
            row.createCell(9).setCellValue(inscripcion.getEstructurasDatos() != null ? inscripcion.getEstructurasDatos() : 0);
            row.createCell(10).setCellValue(inscripcion.getAlgoritmos() != null ? inscripcion.getAlgoritmos() : 0);
            row.createCell(11).setCellValue(inscripcion.getBaseDatos() != null ? inscripcion.getBaseDatos() : 0);
            row.createCell(12).setCellValue(inscripcion.getPromedio() != null ? inscripcion.getPromedio() : 0.0);
            row.createCell(13).setCellValue(inscripcion.getAprobado() != null ? (inscripcion.getAprobado() ? "Sí" : "No") : "Pendiente");
            row.createCell(14).setCellValue(inscripcion.getFechaExamen());
            
            // Aplicar estilos
            for (int i = 0; i < headers.length; i++) {
                row.getCell(i).setCellStyle(dataStyle);
            }
        }
    }
    
    private void crearHojaDatosPersonales(Sheet sheet) {
        CellStyle headerStyle = crearEstiloHeader(sheet.getWorkbook());
        CellStyle dataStyle = crearEstiloDatos(sheet.getWorkbook());
        
        // Encabezados
        Row headerRow = sheet.createRow(0);
        String[] headers = {
            "ID", "Nombre", "Apellido", "DNI", "CUIL", "Email", 
            "Trabaja Actualmente", "Sector IT", "Fecha Examen"
        };
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
            sheet.setColumnWidth(i, 20 * 256);
        }
        
        // Obtener datos de exámenes con información de personas
        List<Examen> examenes = examenRepository.findAll();
        
        int rowNum = 1;
        for (Examen examen : examenes) {
            if (examen.getPersona() != null) {
                Persona persona = examen.getPersona();
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(persona.getId());
                row.createCell(1).setCellValue(persona.getNombre());
                row.createCell(2).setCellValue(persona.getApellido());
                row.createCell(3).setCellValue(persona.getCuil() != null && persona.getCuil().length() >= 8 ? 
                    persona.getCuil().substring(2, 10) : "");
                row.createCell(4).setCellValue(persona.getCuil());
                row.createCell(5).setCellValue(persona.getEmail());
                row.createCell(6).setCellValue(persona.getTrabajaActualmente());
                row.createCell(7).setCellValue(persona.getTrabajaSectorIT());
                row.createCell(8).setCellValue(examen.getFechaFin() != null ? 
                    examen.getFechaFin().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "No completado");
                
                for (int i = 0; i < headers.length; i++) {
                    row.getCell(i).setCellStyle(dataStyle);
                }
            }
        }
    }
    
    private void crearHojaResultados(Sheet sheet) {
        CellStyle headerStyle = crearEstiloHeader(sheet.getWorkbook());
        CellStyle dataStyle = crearEstiloDatos(sheet.getWorkbook());
        CellStyle porcentajeStyle = crearEstiloPorcentaje(sheet.getWorkbook());
        
        // Encabezados
        Row headerRow = sheet.createRow(0);
        String[] headers = {
            "ID Examen", "Nombre", "Apellido", "Email", "Programación (%)", 
            "Lógica (%)", "Matemática (%)", "Creatividad (%)", "Promedio (%)", 
            "Aprobado", "Fecha Examen", "Tiempo Total (min)"
        };
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
            sheet.setColumnWidth(i, 18 * 256);
        }
        
        // Obtener datos de exámenes
        List<Examen> examenes = examenRepository.findAll();
        
        int rowNum = 1;
        for (Examen examen : examenes) {
            if (examen.getPersona() != null) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(examen.getId());
                row.createCell(1).setCellValue(examen.getPersona().getNombre());
                row.createCell(2).setCellValue(examen.getPersona().getApellido());
                row.createCell(3).setCellValue(examen.getPersona().getEmail());
                
                // Porcentajes por área
                Cell cellProgramacion = row.createCell(4);
                cellProgramacion.setCellValue(examen.getProgramacion() != null ? examen.getProgramacion() : 0);
                cellProgramacion.setCellStyle(porcentajeStyle);
                
                Cell cellLogica = row.createCell(5);
                cellLogica.setCellValue(examen.getLogica() != null ? examen.getLogica() : 0);
                cellLogica.setCellStyle(porcentajeStyle);
                
                Cell cellMatematica = row.createCell(6);
                cellMatematica.setCellValue(examen.getMatematica() != null ? examen.getMatematica() : 0);
                cellMatematica.setCellStyle(porcentajeStyle);
                
                Cell cellCreatividad = row.createCell(7);
                cellCreatividad.setCellValue(examen.getCreatividad() != null ? examen.getCreatividad() : 0);
                cellCreatividad.setCellStyle(porcentajeStyle);
                
                Cell cellPromedio = row.createCell(8);
                cellPromedio.setCellValue(examen.getPromedio());
                cellPromedio.setCellStyle(porcentajeStyle);
                
                row.createCell(9).setCellValue(examen.isAprobado() ? "Sí" : "No");
                row.createCell(10).setCellValue(examen.getFechaFin() != null ? 
                    examen.getFechaFin().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "No completado");
                row.createCell(11).setCellValue(examen.getTiempoTotalMinutos() != null ? examen.getTiempoTotalMinutos() : 0);
                
                // Aplicar estilos
                for (int i = 0; i < headers.length; i++) {
                    if (i < 4 || i > 8) { // No aplicar a las celdas de porcentaje
                        row.getCell(i).setCellStyle(dataStyle);
                    }
                }
            }
        }
    }
    
    private void crearHojaPreguntasRespuestas(Sheet sheet) {
        CellStyle headerStyle = crearEstiloHeader(sheet.getWorkbook());
        CellStyle dataStyle = crearEstiloDatos(sheet.getWorkbook());
        
        // Encabezados
        Row headerRow = sheet.createRow(0);
        String[] headers = {
            "ID Examen", "Nombre", "Apellido", "ID Pregunta", "Área", "Pregunta", 
            "Opción Seleccionada", "Respuesta Correcta", "Es Correcta", "Opciones"
        };
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
            sheet.setColumnWidth(i, 25 * 256);
        }
        
        // Obtener datos de exámenes con respuestas
        List<Examen> examenes = examenRepository.findAll();
        
        int rowNum = 1;
        for (Examen examen : examenes) {
            if (examen.getPersona() != null && examen.getRespuestas() != null) {
                for (RespuestaExamen respuesta : examen.getRespuestas()) {
                    Row row = sheet.createRow(rowNum++);
                    
                    row.createCell(0).setCellValue(examen.getId());
                    row.createCell(1).setCellValue(examen.getPersona().getNombre());
                    row.createCell(2).setCellValue(examen.getPersona().getApellido());
                    row.createCell(3).setCellValue(respuesta.getPregunta().getId());
                    row.createCell(4).setCellValue(respuesta.getPregunta().getAreaConocimiento().getNombre());
                    
                    // Pregunta (truncar si es muy larga)
                    String pregunta = respuesta.getPregunta().getEnunciado();
                    row.createCell(5).setCellValue(pregunta.length() > 100 ? pregunta.substring(0, 100) + "..." : pregunta);
                    
                    row.createCell(6).setCellValue(respuesta.getRespuestaSeleccionada());
                    row.createCell(7).setCellValue(respuesta.getPregunta().getOpcionCorrecta());
                    row.createCell(8).setCellValue(respuesta.isEsCorrecta() ? "Sí" : "No");
                    
                    // Opciones (formato: 1) Opción1 | 2) Opción2 | 3) Opción3 | 4) Opción4
                    String opciones = respuesta.getPregunta().getOpciones().stream()
                        .sorted((o1, o2) -> Integer.compare(o1.getOrden(), o2.getOrden()))
                        .map(opcion -> opcion.getOrden() + ") " + opcion.getTexto())
                        .collect(Collectors.joining(" | "));
                    row.createCell(9).setCellValue(opciones.length() > 200 ? opciones.substring(0, 200) + "..." : opciones);
                    
                    // Aplicar estilos
                    for (int i = 0; i < headers.length; i++) {
                        row.getCell(i).setCellStyle(dataStyle);
                    }
                }
            }
        }
    }
    
    private void crearHojaEstadisticas(Sheet sheet) {
        CellStyle headerStyle = crearEstiloHeader(sheet.getWorkbook());
        CellStyle dataStyle = crearEstiloDatos(sheet.getWorkbook());
        
        // Encabezados
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Métrica", "Valor"};
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
            sheet.setColumnWidth(i, 30 * 256);
        }
        
        // Obtener estadísticas
        List<InscripcionDTO> inscripciones = obtenerTodasLasInscripciones();
        
        int rowNum = 1;
        
        // Total de inscripciones
        Row row1 = sheet.createRow(rowNum++);
        row1.createCell(0).setCellValue("Total de Inscripciones");
        row1.createCell(1).setCellValue(inscripciones.size());
        
        // Aprobados
        long aprobados = inscripciones.stream().filter(i -> Boolean.TRUE.equals(i.getAprobado())).count();
        Row row2 = sheet.createRow(rowNum++);
        row2.createCell(0).setCellValue("Aprobados");
        row2.createCell(1).setCellValue(aprobados);
        
        // Desaprobados
        long desaprobados = inscripciones.stream().filter(i -> Boolean.FALSE.equals(i.getAprobado())).count();
        Row row3 = sheet.createRow(rowNum++);
        row3.createCell(0).setCellValue("Desaprobados");
        row3.createCell(1).setCellValue(desaprobados);
        
        // Pendientes
        long pendientes = inscripciones.stream().filter(i -> i.getAprobado() == null).count();
        Row row4 = sheet.createRow(rowNum++);
        row4.createCell(0).setCellValue("Pendientes");
        row4.createCell(1).setCellValue(pendientes);
        
        // Promedio general
        double promedioGeneral = inscripciones.stream()
            .filter(i -> i.getPromedio() != null)
            .mapToDouble(InscripcionDTO::getPromedio)
            .average()
            .orElse(0.0);
        Row row5 = sheet.createRow(rowNum++);
        row5.createCell(0).setCellValue("Promedio General");
        row5.createCell(1).setCellValue(Math.round(promedioGeneral * 100.0) / 100.0);
        
        // Promedio por área
        double promProgramacion = inscripciones.stream()
            .filter(i -> i.getProgramacionBasica() != null)
            .mapToDouble(i -> i.getProgramacionBasica())
            .average()
            .orElse(0.0);
        Row row6 = sheet.createRow(rowNum++);
        row6.createCell(0).setCellValue("Promedio Programación");
        row6.createCell(1).setCellValue(Math.round(promProgramacion * 100.0) / 100.0);
        
        double promLogica = inscripciones.stream()
            .filter(i -> i.getEstructurasDatos() != null)
            .mapToDouble(i -> i.getEstructurasDatos())
            .average()
            .orElse(0.0);
        Row row7 = sheet.createRow(rowNum++);
        row7.createCell(0).setCellValue("Promedio Lógica");
        row7.createCell(1).setCellValue(Math.round(promLogica * 100.0) / 100.0);
        
        double promMatematica = inscripciones.stream()
            .filter(i -> i.getAlgoritmos() != null)
            .mapToDouble(i -> i.getAlgoritmos())
            .average()
            .orElse(0.0);
        Row row8 = sheet.createRow(rowNum++);
        row8.createCell(0).setCellValue("Promedio Matemática");
        row8.createCell(1).setCellValue(Math.round(promMatematica * 100.0) / 100.0);
        
        double promCreatividad = inscripciones.stream()
            .filter(i -> i.getBaseDatos() != null)
            .mapToDouble(i -> i.getBaseDatos())
            .average()
            .orElse(0.0);
        Row row9 = sheet.createRow(rowNum++);
        row9.createCell(0).setCellValue("Promedio Creatividad");
        row9.createCell(1).setCellValue(Math.round(promCreatividad * 100.0) / 100.0);
        
        // Aplicar estilos
        for (int i = 1; i < rowNum; i++) {
            Row row = sheet.getRow(i);
            for (int j = 0; j < 2; j++) {
                row.getCell(j).setCellStyle(dataStyle);
            }
        }
    }
    
    private CellStyle crearEstiloHeader(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }
    
    private CellStyle crearEstiloDatos(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }
    
    private CellStyle crearEstiloPorcentaje(Workbook workbook) {
        CellStyle style = crearEstiloDatos(workbook);
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("0.0"));
        return style;
    }
    
    // Método para obtener todas las inscripciones con resultados
    private List<InscripcionDTO> obtenerTodasLasInscripciones() {
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
} 