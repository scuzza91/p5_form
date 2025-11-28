# ✅ Solución Aplicada al Error de PostgreSQL

## 🔍 Problema Identificado

1. **Puerto incorrecto**: La aplicación estaba configurada para el puerto `5432`, pero PostgreSQL en Docker está en el puerto `5433`
2. **Contraseña incorrecta**: La contraseña configurada era `Francisco.91`, pero la contraseña por defecto en Docker es `changeme`
3. **Base de datos no existía**: La base de datos `p5_form_dev` no existía en PostgreSQL

---

## ✅ Cambios Realizados

### 1. Actualizado `application.properties`

**Antes:**
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/p5_form_dev
spring.datasource.password=${DB_PASSWORD:Francisco.91}
```

**Después:**
```properties
spring.datasource.url=jdbc:postgresql://localhost:5433/p5_form_dev
spring.datasource.password=${DB_PASSWORD:changeme}
```

### 2. Creada la base de datos

```sql
CREATE DATABASE p5_form_dev;
```

---

## 🚀 Próximos Pasos

1. **Ejecuta la aplicación:**
   ```powershell
   ./mvnw spring-boot:run
   ```

2. **La aplicación debería:**
   - Conectarse correctamente a PostgreSQL
   - Crear las tablas automáticamente (porque `spring.jpa.hibernate.ddl-auto=update`)
   - Iniciar sin errores

---

## 📝 Notas Importantes

### Si quieres usar una contraseña diferente:

1. **Cambia la contraseña en Docker:**
   ```powershell
   docker exec postgres psql -U postgres -c "ALTER USER postgres WITH PASSWORD 'tu-nueva-contraseña';"
   ```

2. **Actualiza `application.properties`:**
   ```properties
   spring.datasource.password=${DB_PASSWORD:tu-nueva-contraseña}
   ```

3. **O usa variable de entorno:**
   ```powershell
   $env:DB_PASSWORD="tu-nueva-contraseña"
   ./mvnw spring-boot:run
   ```

### Si quieres usar el puerto 5432:

1. **Detén el contenedor actual:**
   ```powershell
   docker stop postgres
   docker rm postgres
   ```

2. **Inicia con el puerto correcto:**
   ```powershell
   docker run -d --name postgres -e POSTGRES_PASSWORD=changeme -p 5432:5432 postgres:15-alpine
   ```

3. **Actualiza `application.properties`** para usar el puerto 5432

---

## ✅ Verificación

Después de ejecutar la aplicación, deberías ver en los logs:

```
HikariPool-1 - Start completed.
Started FormularioApplication in X.XXX seconds
```

En lugar del error de autenticación.

