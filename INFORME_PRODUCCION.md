# Informe: ¿Está el proyecto listo para producción?

**Fecha de análisis:** Febrero 2025  
**Proyecto:** p5-form – Sistema de Evaluación y Administración de Candidatos

---

## Resumen ejecutivo

**Conclusión: NO está listo para producción** en su estado actual. Hay **varios problemas críticos de seguridad y configuración** que deben corregirse antes de desplegar en un entorno real.

---

## Crítico – Bloqueantes para producción

### 1. Seguridad: todas las rutas están abiertas (SecurityConfig)

**Archivo:** `src/main/java/com/formulario/config/SecurityConfig.java`

```java
.anyRequest().permitAll()  // ← TODAS las rutas sin autenticación
```

**Problema:** Cualquier persona puede acceder sin login a:

- `/dashboard` – panel administrativo
- `/inscripciones` – datos de candidatos
- `/configuracion` – configuración del sistema (incl. token de Bondarea)
- Rutas sensibles que deberían estar protegidas

**Acción requerida:** Definir reglas por rol (por ejemplo: `/api/persona/crear` y `/examen/**` públicos; `/dashboard`, `/inscripciones`, `/configuracion`, `/admin/**` solo para usuarios autenticados con rol ADMIN) y quitar `.anyRequest().permitAll()`.

---

### 2. Credenciales y secretos expuestos

| Ubicación | Problema |
|-----------|----------|
| `application.properties` | `spring.datasource.password=${DB_PASSWORD:Francisco.91}` – contraseña por defecto en el repositorio. |
| `application.properties` | `examen.token.secret=default-secret-key-change-in-production-2024` – secreto débil y fijo. |
| `application-prod.properties` | `SPRING_DATASOURCE_PASSWORD:password` – valor por defecto inseguro. |
| `GUIA_DESPLIEGUE_COMPLETA.md` | Token de Bondarea en texto plano: `41855ad220d5c0f4fb39ea6b2ed8d56e`. |

**Acción requerida:**

- Eliminar contraseñas y secretos por defecto del código; usar solo variables de entorno en producción.
- No incluir el token real de Bondarea en la documentación; referenciar “configurar en `/configuracion`” o en `.env`.
- Añadir `.env` al `.gitignore` si no está, y usar `.env.example` sin valores reales.

---

### 3. Usuario administrador recreado en cada arranque (DataInitializer)

**Archivo:** `src/main/java/com/formulario/config/DataInitializer.java`

- Elimina el usuario `admin` si existe y lo vuelve a crear con contraseña `admin123`.
- Imprime en logs: `"Contraseña: admin123"` y `"Contraseña encriptada: ..."`.

**Problemas:**

- En producción, no debe resetearse la contraseña del admin en cada despliegue.
- Las contraseñas (aunque sea la por defecto) no deben aparecer en logs.

**Acción requerida:** Crear el usuario admin **solo si no existe** (por ejemplo `usuarioRepository.findByUsername("admin").isEmpty()`). No eliminar ni recrear en cada arranque. No loguear contraseñas ni hashes.

---

### 4. Puerto incorrecto de PostgreSQL en Docker (docker-compose.yml)

**Línea 19:**

```yaml
ports:
  - "${POSTGRES_PORT:-5434}:50000"
```

La imagen oficial de PostgreSQL escucha en el puerto **5432**, no en 50000. El mapeo correcto sería, por ejemplo:

```yaml
- "${POSTGRES_PORT:-5434}:5432"
```

De lo contrario, el contenedor de la app no podrá conectarse a Postgres usando `postgres:5432` si se espera que el host exponga 5434.

**Acción requerida:** Cambiar `50000` por `5432` en el mapeo de puertos del servicio `postgres`.

---

## Importante – Recomendaciones antes de producción

### 5. Archivo `.env.example` ausente

La documentación (`GUIA_DESPLIEGUE_COMPLETA.md`, `deploy.sh`, `README_DOCKER.md`) indica copiar `.env.example` a `.env`, pero **no existe `.env.example`** en el repositorio.

**Acción requerida:** Crear `.env.example` con las variables necesarias (sin valores sensibles), por ejemplo:

- `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`, `POSTGRES_PORT`
- `SPRING_PROFILES_ACTIVE`, `SERVER_PORT`
- `SPRING_JPA_HIBERNATE_DDL_AUTO`, `SPRING_JPA_SHOW_SQL`
- `LOGGING_LEVEL_COM_FORMULARIO`
- `EXAMEN_TOKEN_SECRET` (opcional)
- `API_TOKEN_BONDAREA` (opcional, indicando que se configura en UI)

Añadir `.env` al `.gitignore` si no está.

---

### 6. API sin token cuando no está configurado (ConfiguracionService)

**Archivo:** `src/main/java/com/formulario/service/ConfiguracionService.java`

```java
// Si no hay token configurado, permitir acceso (para desarrollo)
if (tokenConfigurado == null || tokenConfigurado.isEmpty()) {
    return true;  // Permite todas las peticiones
}
```

**Problema:** En producción, si alguien olvida configurar el token, el endpoint `/api/persona/crear` acepta cualquier petición.

**Acción requerida:** En perfil `prod`, si el token no está configurado, **no** permitir acceso (devolver `false` o rechazar la petición con 401/403).

---

### 7. CORS muy abierto (ApiController)

**Archivo:** `src/main/java/com/formulario/controller/ApiController.java`

```java
@CrossOrigin(origins = "*")
```

**Problema:** Cualquier origen puede llamar a la API. Aumenta el riesgo de uso indebido si la API es pública.

**Acción requerida:** En producción, restringir a orígenes conocidos (p. ej. dominios de Bondarea y del front propio) o gestionar CORS desde `CorsConfig` con una lista de orígenes permitidos.

---

### 8. Sin tests automatizados

No existe el directorio `src/test`. No hay pruebas unitarias ni de integración.

**Recomendación:** Añadir al menos:

- Tests para los servicios críticos (FormularioService, AuthService, ConfiguracionService).
- Test de integración para `POST /api/persona/crear` (con y sin token, validaciones básicas).

---

## Aspectos positivos (ya adecuados para producción)

- **Docker:** Dockerfile multi-stage, usuario no-root, healthcheck.
- **docker-compose:** Perfil `prod`, variables de entorno, healthchecks, volúmenes para datos y uploads.
- **Documentación:** README, guías de despliegue y de Docker bien detalladas.
- **Manejo de errores:** `GlobalExceptionHandler` para 405 y 400.
- **JPA en producción:** `application-prod.properties` con `ddl-auto=validate` por defecto (buena práctica si se usan migraciones).
- **Thymeleaf:** Cache activado en prod.
- **Logging:** Niveles diferenciados (INFO en prod, menos SQL).
- **Contraseñas:** Uso de BCrypt en `AuthService` para usuarios.
- **Token de examen:** `ExamenTokenUtil` con HMAC y secret configurable.
- **API Bondarea:** Validación de token en `/api/persona/crear` cuando el token está configurado.

---

## Checklist pre-producción

| # | Tarea | Prioridad |
|---|--------|------------|
| 1 | Proteger rutas en `SecurityConfig` (quitar `permitAll()`, definir roles) | Crítica |
| 2 | Eliminar credenciales/secretos de `application*.properties` y docs | Crítica |
| 3 | DataInitializer: crear admin solo si no existe; no loguear contraseñas | Crítica |
| 4 | Corregir puerto de Postgres en docker-compose: `5432` en lugar de `50000` | Crítica |
| 5 | Crear `.env.example` y documentar variables; asegurar `.env` en `.gitignore` | Alta |
| 6 | En prod, no permitir acceso a la API si el token Bondarea no está configurado | Alta |
| 7 | Restringir CORS en producción a orígenes conocidos | Media |
| 8 | Añadir tests básicos (servicios clave + API) | Media |
| 9 | Revisar que no queden tokens ni contraseñas en GUIA_DESPLIEGUE_COMPLETA.md | Alta |

---

## Conclusión

El proyecto tiene una buena base (Stack, Docker, documentación, manejo de errores, uso de BCrypt y token de examen), pero **no debe desplegarse a producción** hasta resolver los puntos críticos:

1. **Seguridad de rutas** (SecurityConfig).  
2. **Credenciales y secretos** (properties, documentación).  
3. **Comportamiento del DataInitializer** (admin y logs).  
4. **Configuración de Docker** (puerto de PostgreSQL).

Una vez aplicados estos cambios (y recomendablemente los del apartado “Importante”), el sistema estará en condiciones mucho más seguras para uso en producción.
