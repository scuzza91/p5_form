# Funcionalidad de Generación de PDFs

## Descripción

Se ha implementado una funcionalidad completa para generar PDFs con los resultados del examen y recomendaciones laborales. Esta funcionalidad permite a los usuarios y administradores descargar documentos PDF profesionales con la información de evaluación.

## Características

### Tipos de PDF Disponibles

1. **PDF Solo Resultado** (`/pdf/solo-resultado/{personaId}`)
   - Contiene únicamente el resultado del examen
   - Incluye información personal del candidato
   - Muestra puntuaciones por áreas (Lógica, Matemática, Creatividad, Programación)
   - Incluye estadísticas del examen (tiempo, preguntas respondidas, etc.)

2. **PDF Solo Recomendaciones** (`/pdf/solo-recomendaciones/{personaId}`)
   - Contiene únicamente las recomendaciones laborales
   - Incluye análisis de compatibilidad por rol
   - Muestra estadísticas de recomendaciones
   - Incluye información adicional y próximos pasos

3. **PDF Completo** (`/pdf/resultado-completo/{personaId}`)
   - Combina resultado del examen + recomendaciones laborales
   - Documento completo en múltiples páginas
   - Incluye toda la información disponible

### Endpoints Disponibles

#### Endpoints Específicos
- `GET /pdf/resultado-completo/{personaId}` - PDF completo
- `GET /pdf/solo-resultado/{personaId}` - Solo resultado del examen
- `GET /pdf/solo-recomendaciones/{personaId}` - Solo recomendaciones

#### Endpoint Genérico
- `GET /pdf/{personaId}?tipo={tipo}` - PDF según tipo especificado
  - `tipo=completo` (por defecto)
  - `tipo=resultado`
  - `tipo=recomendaciones`

## Ubicación de los Botones

### Para Candidatos
1. **Página de Resultado** (`/resultado/{personaId}`)
   - Botones para descargar "Solo Resultado" y "Resultado + Recomendaciones"

2. **Página de Recomendaciones** (`/recomendaciones/{personaId}`)
   - Botones para descargar "Solo Recomendaciones" y "Resultado + Recomendaciones"

### Para Administradores
1. **Lista de Inscripciones** (`/lista`)
   - Iconos de PDF en la columna de acciones para cada candidato
   - Botón rojo: PDF solo resultado
   - Botón verde: PDF completo

## Características Técnicas

### Dependencias Agregadas
```xml
<!-- iText 7 para generación de PDFs -->
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>itext7-core</artifactId>
    <version>7.2.5</version>
    <type>pom</type>
</dependency>

<!-- HTML a PDF -->
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>html2pdf</artifactId>
    <version>4.0.5</version>
</dependency>

<!-- Flying Saucer para renderizado HTML -->
<dependency>
    <groupId>org.xhtmlrenderer</groupId>
    <artifactId>flying-saucer-pdf</artifactId>
    <version>9.1.22</version>
</dependency>
```

### Archivos Creados

#### Servicios
- `src/main/java/com/formulario/service/PdfService.java` - Servicio principal para generación de PDFs

#### Controladores
- `src/main/java/com/formulario/controller/PdfController.java` - Controlador para endpoints de PDF

#### Plantillas HTML para PDF
- `src/main/resources/templates/pdf-resultado.html` - Plantilla para PDF completo
- `src/main/resources/templates/pdf-solo-resultado.html` - Plantilla para solo resultado
- `src/main/resources/templates/pdf-solo-recomendaciones.html` - Plantilla para solo recomendaciones

### Características de las Plantillas PDF

#### Estilos Optimizados para PDF
- Uso de CSS específico para impresión
- Configuración de página A4 con márgenes apropiados
- Control de saltos de página
- Fuentes y tamaños optimizados para lectura

#### Contenido Estructurado
- Encabezado con información del sistema
- Secciones claramente definidas
- Información del candidato
- Resultados detallados del examen
- Recomendaciones laborales con análisis de compatibilidad
- Pie de página con información legal

#### Elementos Visuales
- Barras de progreso para compatibilidad
- Colores diferenciados por áreas
- Iconos y elementos visuales apropiados
- Diseño profesional y limpio

## Uso

### Para Usuarios
1. Completar el examen
2. Ir a la página de resultado
3. Hacer clic en los botones de descarga de PDF
4. El archivo se descargará automáticamente

### Para Administradores
1. Acceder a la lista de inscripciones
2. Usar los iconos de PDF en la columna de acciones
3. Seleccionar el tipo de PDF deseado

## Formato de Archivos

Los archivos PDF se generan con el siguiente formato de nombre:
- `resultado_examen_{personaId}_{timestamp}.pdf`
- `recomendaciones_laborales_{personaId}_{timestamp}.pdf`
- `resultado_completo_{personaId}_{timestamp}.pdf`

## Consideraciones

### Seguridad
- Los PDFs solo se generan para personas que han completado el examen
- Validación de existencia de datos antes de la generación
- Manejo de errores apropiado

### Rendimiento
- Generación asíncrona de PDFs
- Optimización de plantillas HTML
- Uso eficiente de memoria

### Compatibilidad
- PDFs compatibles con lectores estándar
- Codificación UTF-8 para caracteres especiales
- Formato A4 estándar

## Mantenimiento

### Actualización de Plantillas
Para modificar el diseño de los PDFs, editar las plantillas HTML correspondientes en `src/main/resources/templates/`.

### Agregar Nuevos Tipos de PDF
1. Crear nueva plantilla HTML
2. Agregar método en `PdfService`
3. Agregar endpoint en `PdfController`
4. Actualizar botones en las plantillas web

### Logs
La generación de PDFs se registra en los logs del sistema para monitoreo y debugging. 