# Configuración de Inscripciones - Sistema Piso Cinco

## Resumen
Se ha implementado una nueva funcionalidad que permite a los administradores del sistema abrir y cerrar las inscripciones de forma dinámica, controlando si los usuarios pueden registrarse o no en el sistema.

## Características Implementadas

### 1. Modelo de Configuración
- **Archivo**: `src/main/java/com/formulario/model/ConfiguracionSistema.java`
- **Descripción**: Modelo JPA para almacenar configuraciones del sistema
- **Campos principales**:
  - `clave`: Identificador único de la configuración
  - `valor`: Valor de la configuración
  - `descripcion`: Descripción de la configuración
  - `fechaActualizacion`: Fecha de última modificación
  - `usuarioActualizacion`: Usuario que realizó el cambio

### 2. Repositorio
- **Archivo**: `src/main/java/com/formulario/repository/ConfiguracionSistemaRepository.java`
- **Funcionalidad**: Interfaz JPA para operaciones de base de datos

### 3. Servicio
- **Archivo**: `src/main/java/com/formulario/service/ConfiguracionService.java`
- **Métodos principales**:
  - `estanInscripcionesAbiertas()`: Verifica si las inscripciones están abiertas
  - `setInscripcionesAbiertas(boolean, String)`: Cambia el estado de las inscripciones
  - `inicializarConfiguracionesPorDefecto()`: Crea configuraciones iniciales

### 4. Controlador
- **Archivo**: `src/main/java/com/formulario/controller/ConfiguracionController.java`
- **Endpoints**:
  - `GET /configuracion`: Muestra la página de configuraciones
  - `POST /configuracion/inscripciones`: Cambia el estado de las inscripciones
  - `POST /configuracion/actualizar`: Actualiza configuraciones específicas

### 5. Interfaz de Usuario
- **Archivo**: `src/main/resources/templates/configuracion.html`
- **Características**:
  - Panel de control visual para abrir/cerrar inscripciones
  - Indicadores de estado (abiertas/cerradas)
  - Información detallada sobre cada configuración
  - Interfaz responsiva y moderna

## Funcionalidades

### Control de Acceso
- **Verificación automática**: El sistema verifica el estado de las inscripciones antes de permitir el acceso al formulario
- **Redirección**: Si las inscripciones están cerradas, los usuarios son redirigidos a la página principal con un mensaje informativo
- **Seguridad**: Solo los administradores pueden cambiar las configuraciones

### Indicadores Visuales
- **Página principal**: Muestra el estado actual de las inscripciones
- **Header**: Indicador visual en la barra superior
- **Botones**: El botón de "Comenzar Inscripción" se deshabilita cuando están cerradas
- **Mensajes**: Información clara sobre el estado actual

### Persistencia
- **Base de datos**: Las configuraciones se almacenan en PostgreSQL
- **Inicialización automática**: Se crean configuraciones por defecto al iniciar el sistema
- **Auditoría**: Se registra quién y cuándo realizó cada cambio

## Instalación y Configuración

### 1. Ejecutar Script SQL
```sql
-- Ejecutar el archivo database/crear_tabla_configuracion_sistema.sql
```

### 2. Reiniciar la Aplicación
Los cambios en el código se aplicarán automáticamente al reiniciar.

### 3. Verificar Configuración Inicial
- Las inscripciones estarán abiertas por defecto
- Se puede acceder a la configuración desde el dashboard

## Uso del Sistema

### Para Administradores

1. **Acceder a Configuraciones**:
   - Iniciar sesión como administrador
   - Ir al Dashboard
   - Hacer clic en "Configuración" en la sección "Gestión de Usuarios"

2. **Cambiar Estado de Inscripciones**:
   - En la página de configuraciones, ver el estado actual
   - Hacer clic en "Abrir Inscripciones" o "Cerrar Inscripciones"
   - Confirmar el cambio

3. **Verificar Cambios**:
   - Los cambios se aplican inmediatamente
   - Verificar en la página principal que el estado se actualizó

### Para Usuarios

1. **Inscripciones Abiertas**:
   - Pueden acceder normalmente al formulario
   - El botón "Comenzar Inscripción" está habilitado
   - Se muestra indicador verde "Inscripciones Abiertas"

2. **Inscripciones Cerradas**:
   - No pueden acceder al formulario
   - El botón está deshabilitado y muestra "Inscripciones Cerradas"
   - Se muestra mensaje informativo en rojo

## Archivos Modificados

### Nuevos Archivos
- `src/main/java/com/formulario/model/ConfiguracionSistema.java`
- `src/main/java/com/formulario/repository/ConfiguracionSistemaRepository.java`
- `src/main/java/com/formulario/service/ConfiguracionService.java`
- `src/main/java/com/formulario/controller/ConfiguracionController.java`
- `src/main/resources/templates/configuracion.html`
- `database/crear_tabla_configuracion_sistema.sql`
- `README_CONFIGURACION_INSCRIPCIONES.md`

### Archivos Modificados
- `src/main/java/com/formulario/controller/FormularioController.java`
- `src/main/java/com/formulario/config/DataInitializer.java`
- `src/main/resources/templates/dashboard.html`
- `src/main/resources/templates/index.html`

## Seguridad

- **Acceso restringido**: Solo usuarios con rol ADMIN pueden acceder a las configuraciones
- **Validación**: Se verifica la autenticación en cada endpoint
- **Auditoría**: Se registra el usuario que realiza cada cambio
- **Persistencia**: Los cambios se guardan en la base de datos

## Beneficios

1. **Control Dinámico**: Permite abrir/cerrar inscripciones sin reiniciar el sistema
2. **Interfaz Intuitiva**: Panel de control visual fácil de usar
3. **Información Clara**: Los usuarios saben inmediatamente si pueden registrarse
4. **Seguridad**: Solo administradores pueden cambiar configuraciones
5. **Auditoría**: Se registra quién realizó cada cambio
6. **Flexibilidad**: Sistema extensible para futuras configuraciones

## Notas Técnicas

- **Base de datos**: PostgreSQL con tabla `configuracion_sistema`
- **Framework**: Spring Boot con JPA/Hibernate
- **Frontend**: Thymeleaf con Tailwind CSS
- **Seguridad**: Spring Security para control de acceso
- **Inicialización**: Configuraciones por defecto al arrancar el sistema

## Próximas Mejoras

1. **Configuraciones adicionales**: Fechas límite, cupos máximos, etc.
2. **Notificaciones**: Alertas cuando se cambia el estado
3. **Historial**: Ver cambios anteriores
4. **Programación**: Abrir/cerrar automáticamente en fechas específicas
5. **API REST**: Endpoints para integración externa 