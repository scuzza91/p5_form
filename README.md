# Sistema de Evaluación y Administración de Candidatos - Piso Cinco

## 📋 Descripción del Proyecto

Sistema completo de evaluación de candidatos desarrollado en Java con Spring Boot que permite gestionar el proceso de selección en dos fases: recolección de datos personales y evaluación técnica de conocimientos.

### 🎯 Características Principales

- **Proceso de 2 pasos**: Datos personales + Evaluación técnica
- **Sistema de autenticación**: Panel administrativo seguro
- **Evaluación automática**: Cálculo de puntuaciones y criterios de aprobación
- **Generación de reportes**: PDFs con resultados detallados
- **Gestión de usuarios**: Administración completa del sistema
- **Interfaz moderna**: Diseño responsivo con Tailwind CSS

## 🚀 Tecnologías Utilizadas

| Componente | Tecnología | Versión |
|------------|------------|---------|
| **Backend** | Java | 17+ |
| **Framework** | Spring Boot | 3.2.0 |
| **Base de Datos** | PostgreSQL | 12+ |
| **Frontend** | Thymeleaf + Tailwind CSS | - |
| **Seguridad** | Spring Security + BCrypt | - |
| **Reportes** | iText PDF | 7.2.5 |
| **Excel** | Apache POI | 5.2.3 |

## 📁 Estructura del Proyecto

```
p5_form/
├── 📁 src/main/java/com/formulario/
│   ├── 🎯 FormularioApplication.java
│   ├── 🎮 controller/          # Controladores REST y MVC
│   ├── 📊 model/              # Entidades JPA
│   ├── 💾 repository/         # Repositorios de datos
│   ├── ⚙️ service/            # Lógica de negocio
│   └── 🔧 config/             # Configuraciones
├── 📁 src/main/resources/
│   ├── 📄 application.properties
│   ├── 📁 templates/          # Plantillas Thymeleaf
│   └── 📁 static/             # Recursos estáticos
├── 📁 database/               # Scripts SQL y migraciones
├── 📁 docs/                   # Documentación técnica
├── 📁 datos/                  # Archivos de datos
├── 📄 pom.xml                 # Dependencias Maven
├── 🚀 ejecutar.bat            # Script de ejecución Windows
├── 🔧 configurar_base_datos.bat # Script de configuración DB
├── ⚡ INSTALACION_RAPIDA.md   # Guía de instalación rápida
└── 📚 README.md               # Documentación principal
```

## 🛠️ Instalación y Configuración

### Prerrequisitos

- **Java 17** o superior
- **Maven 3.6** o superior  
- **PostgreSQL 12** o superior
- **Git** (opcional)

### Pasos de Instalación

#### 1. Clonar/Descargar el Proyecto
```bash
git clone <url-del-repositorio>
cd p5_form
```

#### 2. Instalación Rápida (Recomendado)

**Para Windows - Instalación automática:**
```bash
# Paso 1: Configurar base de datos
configurar_base_datos.bat

# Paso 2: Ejecutar aplicación
ejecutar.bat
```

**Para Linux/Mac - Instalación manual:**
```bash
# Paso 1: Configurar base de datos
psql -U postgres -d p5_form_dev -f database/INSTALACION_COMPLETA.sql

# Paso 2: Ejecutar aplicación
./mvnw spring-boot:run
```

#### 3. Configurar Variables de Entorno (Opcional)

Si necesitas configurar credenciales específicas:
```bash
# Windows (PowerShell)
$env:DB_PASSWORD="tu_password_postgres"

# Linux/Mac
export DB_PASSWORD=tu_password_postgres
```

#### 4. Acceder a la Aplicación

- **URL Principal**: http://localhost:8083
- **Panel Administrativo**: http://localhost:8083/login
  - Usuario: `admin`
  - Contraseña: `admin123`

#### 5. Instalación Manual Completa (Opcional)

Si prefieres hacer todo manualmente:

**Configurar Base de Datos:**
```sql
-- Conectar a PostgreSQL
psql -U postgres

-- Crear base de datos
CREATE DATABASE p5_form_dev;

-- Ejecutar script de instalación completa
\i database/INSTALACION_COMPLETA.sql
```

**Compilar y Ejecutar:**
```bash
# Compilar
mvn clean compile

# Ejecutar
mvn spring-boot:run
```

## 📖 Guías de Uso

### Para Administradores

1. **Acceso al Sistema**
   - Navegar a `/login`
   - Usar credenciales de administrador

2. **Gestión de Usuarios**
   - Crear nuevos usuarios administrativos
   - Activar/desactivar usuarios
   - Gestionar roles y permisos

3. **Visualización de Datos**
   - Ver lista completa de candidatos
   - Generar reportes PDF
   - Exportar datos a Excel

### Para Candidatos

1. **Inscripción**
   - Completar datos personales (Paso 1)
   - Realizar evaluación técnica (Paso 2)

2. **Resultados**
   - Ver puntuación por áreas
   - Estado de aprobación/reprobación
   - Descargar certificado PDF

## 🔧 Configuración Avanzada

### Perfiles de Ejecución

```bash
# Desarrollo
mvn spring-boot:run -Dspring.profiles.active=dev

# Producción  
mvn spring-boot:run -Dspring.profiles.active=prod
```

### Configuración de Base de Datos

Editar `src/main/resources/application.properties`:
```properties
# PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/p5_form_dev
spring.datasource.username=postgres
spring.datasource.password=${DB_PASSWORD:tu_password}

# H2 (para desarrollo rápido)
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
```

### Personalización

#### Cambiar Criterio de Aprobación
En `Examen.java`:
```java
public boolean isAprobado() {
    return getPromedio() >= 70; // Modificar este valor
}
```

#### Agregar Nuevas Áreas de Evaluación
1. Agregar campo en `Examen.java`
2. Actualizar método `getPromedio()`
3. Modificar plantillas HTML
4. Actualizar validaciones

## 📊 Funcionalidades del Sistema

### Módulo de Inscripción
- ✅ Validación de datos personales
- ✅ Verificación de email único
- ✅ Evaluación técnica en 4 áreas
- ✅ Cálculo automático de puntuaciones

### Módulo Administrativo
- ✅ Autenticación segura
- ✅ Dashboard con estadísticas
- ✅ Gestión de usuarios
- ✅ Generación de reportes

### Módulo de Reportes
- ✅ PDF con resultados detallados
- ✅ Exportación a Excel
- ✅ Certificados de evaluación

## 🔒 Seguridad

- **Autenticación**: Spring Security con BCrypt
- **Validación**: Bean Validation + HTML5
- **Sesiones**: Control de acceso por roles
- **Encriptación**: Contraseñas hasheadas

## 🐛 Solución de Problemas

### Errores Comunes

| Error | Solución |
|-------|----------|
| Puerto ocupado | Cambiar `server.port` en `application.properties` |
| Error de conexión DB | Verificar PostgreSQL y credenciales |
| Error de compilación | Verificar Java 17+ y Maven |
| Error de caracteres | Verificar encoding UTF-8 en DB |

### Logs y Debugging

```bash
# Ver logs detallados
mvn spring-boot:run -Dlogging.level.com.formulario=DEBUG

# Verificar conexión DB
psql -h localhost -U postgres -d p5_form_dev
```

## 📞 Soporte

Para soporte técnico o consultas sobre el sistema:

- **Email**: soporte@piso5.com
- **Documentación**: Ver archivos README específicos en `/docs`
- **Issues**: Crear ticket en el sistema de gestión

## 📄 Licencia

Este proyecto está desarrollado para Piso Cinco. Todos los derechos reservados.

---

**Versión**: 1.0.0  
**Última actualización**: Diciembre 2024  
**Desarrollado por**: Equipo de Desarrollo Piso Cinco 