# Funcionalidad de Exportación a Excel

## Descripción

Se ha agregado una nueva funcionalidad para descargar un archivo Excel completo con toda la información de las inscripciones del sistema. Esta funcionalidad está disponible en la página de inscripciones del dashboard.

## Características

### Botón de Descarga
- **Ubicación**: En la sección de filtros de búsqueda de la página `/inscripciones`
- **Estilo**: Botón verde con icono de Excel
- **Acción**: Descarga inmediata del archivo Excel

### Contenido del Archivo Excel

El archivo Excel incluye **5 hojas** con información detallada:

#### 1. Hoja "Resumen"
- Datos principales de todas las inscripciones
- Columnas: ID, Nombre, Apellido, DNI, CUIL, Email, Trabaja Actualmente, Sector IT, Programación, Lógica, Matemática, Creatividad, Promedio, Aprobado, Fecha Examen

#### 2. Hoja "Datos Personales"
- Información de contacto de todos los registrados
- Columnas: ID, Nombre, Apellido, DNI, CUIL, Email, Trabaja Actualmente, Sector IT, Fecha Examen

#### 3. Hoja "Resultados por Área"
- Porcentajes detallados por área de conocimiento
- Columnas: ID Examen, Nombre, Apellido, Email, Programación (%), Lógica (%), Matemática (%), Creatividad (%), Promedio (%), Aprobado, Fecha Examen, Tiempo Total (min)

#### 4. Hoja "Preguntas y Respuestas"
- Respuestas individuales de cada examen
- Columnas: ID Examen, Nombre, Apellido, ID Pregunta, Área, Pregunta, Opción Seleccionada, Respuesta Correcta, Es Correcta, Opciones

#### 5. Hoja "Estadísticas"
- Métricas generales del sistema
- Incluye: Total de inscripciones, Aprobados, Desaprobados, Pendientes, Promedio general, Promedios por área

## Implementación Técnica

### Archivos Modificados/Creados

1. **ExcelService.java** (Nuevo)
   - Servicio para generar archivos Excel
   - Utiliza Apache POI para la generación
   - Incluye estilos y formato profesional

2. **FormularioController.java** (Modificado)
   - Nuevo endpoint: `/inscripciones/excel`
   - Manejo de descarga de archivos

3. **FormularioService.java** (Modificado)
   - Método delegado para generar Excel
   - Inyección del ExcelService

4. **inscripciones.html** (Modificado)
   - Botón de descarga agregado
   - Información descriptiva sobre el archivo Excel

### Dependencias Utilizadas

- **Apache POI**: Para la generación de archivos Excel (.xlsx)
- **Spring Boot**: Para el manejo de respuestas HTTP
- **Thymeleaf**: Para la integración en la interfaz web

## Uso

1. Acceder a la página de inscripciones (`/inscripciones`)
2. En la sección de filtros, hacer clic en el botón verde "Descargar Excel"
3. El archivo se descargará automáticamente con el nombre `inscripciones_completas.xlsx`

## Características del Archivo

- **Formato**: Excel 2007+ (.xlsx)
- **Nombre**: `inscripciones_completas.xlsx`
- **Estilos**: Encabezados con fondo azul, bordes en todas las celdas
- **Formato de números**: Porcentajes con un decimal
- **Ancho de columnas**: Optimizado para mejor visualización

## Ventajas

- **Exportación completa**: Todos los datos en un solo archivo
- **Organización**: Información separada en hojas temáticas
- **Profesional**: Formato y estilos profesionales
- **Fácil análisis**: Datos estructurados para análisis posterior
- **Compatible**: Formato estándar Excel compatible con todas las versiones

## Notas Técnicas

- El archivo se genera dinámicamente con los datos actuales de la base de datos
- Se manejan casos donde los datos pueden ser nulos
- Los textos largos se truncan para evitar problemas de visualización
- El archivo incluye todas las inscripciones, independientemente de los filtros aplicados en la vista 