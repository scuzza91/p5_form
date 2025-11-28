# 🔧 Solución: Error de Autenticación PostgreSQL

## ❌ Error
```
FATAL: password authentication failed for user "postgres"
```

## 🔍 Causa
La contraseña configurada en `application.properties` no coincide con la contraseña real de PostgreSQL.

---

## ✅ Soluciones

### Opción 1: Verificar y Corregir la Contraseña en application.properties

1. **Abre el archivo:**
   ```
   src/main/resources/application.properties
   ```

2. **Verifica la línea 5:**
   ```properties
   spring.datasource.password=${DB_PASSWORD:Francisco.91}
   ```

3. **Cambia la contraseña** por la correcta de tu PostgreSQL:
   ```properties
   spring.datasource.password=TU_CONTRASEÑA_REAL
   ```

4. **O configura una variable de entorno:**
   ```powershell
   $env:DB_PASSWORD="tu-contraseña-real"
   ```

---

### Opción 2: Verificar que PostgreSQL Esté Corriendo

**Verificar el servicio:**
```powershell
# Ver si PostgreSQL está corriendo
Get-Service -Name postgresql*
```

**Si no está corriendo, inícialo:**
```powershell
# Iniciar PostgreSQL (ajusta el nombre del servicio según tu instalación)
Start-Service postgresql-x64-15
# O el nombre que tengas instalado
```

---

### Opción 3: Cambiar la Contraseña de PostgreSQL

Si no recuerdas la contraseña, puedes cambiarla:

1. **Abre pgAdmin o psql**

2. **Conecta como administrador**

3. **Cambia la contraseña:**
   ```sql
   ALTER USER postgres WITH PASSWORD 'nueva-contraseña';
   ```

4. **Actualiza `application.properties`** con la nueva contraseña

---

### Opción 4: Usar Docker (Más Fácil)

Si tienes Docker instalado, puedes usar la base de datos de Docker:

1. **Inicia Docker Compose:**
   ```powershell
   docker-compose up -d postgres
   ```

2. **Verifica que esté corriendo:**
   ```powershell
   docker-compose ps
   ```

3. **La contraseña por defecto en Docker es:** `changeme`

4. **Actualiza `application.properties`:**
   ```properties
   spring.datasource.password=changeme
   ```

---

### Opción 5: Usar H2 (Base de Datos en Memoria) para Pruebas

Si solo quieres probar la aplicación sin PostgreSQL:

1. **Comenta las líneas de PostgreSQL** en `application.properties`

2. **Agrega configuración de H2:**
   ```properties
   # Base de datos H2 (solo para pruebas)
   spring.datasource.url=jdbc:h2:mem:testdb
   spring.datasource.driverClassName=org.h2.Driver
   spring.datasource.username=sa
   spring.datasource.password=
   spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
   ```

**Nota:** H2 es solo para desarrollo. Los datos se pierden al reiniciar.

---

## 🎯 Solución Rápida Recomendada

### Paso 1: Verificar PostgreSQL

```powershell
# Ver si está corriendo
Get-Service postgresql*
```

### Paso 2: Probar la Conexión

```powershell
# Instalar psql si no lo tienes, o usar pgAdmin
# Probar conexión con:
psql -U postgres -h localhost
```

### Paso 3: Actualizar application.properties

Abre `src/main/resources/application.properties` y cambia:

```properties
spring.datasource.password=TU_CONTRASEÑA_CORRECTA
```

### Paso 4: Reiniciar la Aplicación

```powershell
./mvnw spring-boot:run
```

---

## 📝 Verificar la Configuración

Después de corregir, deberías ver en los logs:

```
HikariPool-1 - Start completed.
Started FormularioApplication in X.XXX seconds
```

En lugar del error de autenticación.

---

## 💡 Consejo

Para evitar este problema en el futuro, puedes usar variables de entorno:

```powershell
# Configurar variable de entorno
$env:DB_PASSWORD="tu-contraseña-segura"

# Luego ejecutar
./mvnw spring-boot:run
```

Así no tendrás la contraseña en el código.

