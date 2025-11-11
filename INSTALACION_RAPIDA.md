# ⚡ Instalación Rápida - Sistema de Evaluación Piso Cinco

## 🎯 Instalación en 5 Pasos

### 1. Verificar Prerrequisitos
```bash
# Verificar Java 17+
java --version

# Verificar Maven
mvn --version

# Verificar PostgreSQL
psql --version
```

### 2. Configurar Base de Datos

**Opción A: Script automático (Windows)**
```bash
configurar_base_datos.bat
```

**Opción B: Manual**
```sql
-- Conectar a PostgreSQL
psql -U postgres

-- Crear base de datos
CREATE DATABASE p5_form_dev;

-- Ejecutar script completo
\i database/INSTALACION_COMPLETA.sql
```

### 3. Configurar Variables de Entorno
```bash
# Windows (PowerShell)
$env:DB_PASSWORD="tu_password_postgres"

# Linux/Mac
export DB_PASSWORD=tu_password_postgres
```

### 4. Ejecutar la Aplicación
```bash
# Opción A: Usar script automático (Windows)
ejecutar.bat

# Opción B: Usar Maven
mvn spring-boot:run

# Opción C: Usar Maven Wrapper
./mvnw spring-boot:run
```

### 5. Acceder al Sistema
- **URL**: http://localhost:8083
- **Admin**: http://localhost:8083/login
  - Usuario: `admin`
  - Contraseña: `admin123`

## 🚨 Solución de Problemas Rápidos

| Problema | Solución |
|----------|----------|
| Puerto ocupado | Cambiar `server.port=8084` en `application.properties` |
| Error de DB | Verificar PostgreSQL ejecutándose |
| Error de Java | Instalar Java 17+ |
| Error de Maven | Instalar Maven 3.6+ |

## 📞 Soporte Inmediato

Si tienes problemas durante la instalación:
1. Revisar logs: `mvn spring-boot:run -Dlogging.level.com.formulario=DEBUG`
2. Verificar conexión DB: `psql -h localhost -U postgres -d p5_form_dev`
3. Contactar soporte: soporte@piso5.com

---

**Tiempo estimado de instalación**: 10-15 minutos 