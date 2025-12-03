# 🧪 Guía para Probar el Módulo de Recomendaciones de Estudios en Ambiente Local

Esta guía te ayudará a probar el nuevo módulo de recomendaciones de estudios en tu ambiente local.

## 📋 Requisitos Previos

1. **Java 17** instalado y configurado
2. **PostgreSQL** ejecutándose (puerto 5433 según tu configuración)
3. **Maven** o Maven Wrapper (`mvnw.cmd`)
4. Base de datos `p5_form_dev` creada

## 🚀 Paso 1: Verificar la Base de Datos

Asegúrate de que PostgreSQL esté corriendo y la base de datos exista:

```bash
# Verificar que PostgreSQL esté corriendo
# En Windows PowerShell:
Get-Service -Name postgresql*

# O verificar conexión:
psql -h localhost -p 5433 -U postgres -d p5_form_dev
```

Si la base de datos no existe, créala:

```sql
CREATE DATABASE p5_form_dev;
```

## 🔧 Paso 2: Compilar el Proyecto

Abre una terminal en la raíz del proyecto y ejecuta:

### Opción A: Usando Maven Wrapper (Recomendado)
```bash
# Windows
.\mvnw.cmd clean install

# Linux/Mac
./mvnw clean install
```

### Opción B: Usando Maven instalado
```bash
mvn clean install
```

Esto compilará el proyecto y creará las nuevas tablas automáticamente gracias a `spring.jpa.hibernate.ddl-auto=update`.

## ▶️ Paso 3: Ejecutar la Aplicación

### Opción A: Usando el script ejecutar.bat (Windows)
```bash
.\ejecutar.bat
```

### Opción B: Usando Maven directamente
```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

### Opción C: Desde tu IDE
1. Abre el proyecto en tu IDE (IntelliJ, Eclipse, VS Code)
2. Busca la clase `FormularioApplication.java`
3. Ejecuta la clase como aplicación Java

La aplicación debería iniciar en: **http://localhost:8083**

## 🔐 Paso 4: Iniciar Sesión como Administrador

1. Abre tu navegador y ve a: **http://localhost:8083/login**

2. **Credenciales por defecto:**
   - Si no tienes un usuario admin, necesitas crear uno primero
   - Ve a: **http://localhost:8083/registro**
   - Crea un usuario con rol **ADMIN**

3. O verifica en la base de datos si ya existe un usuario admin:
```sql
SELECT * FROM usuarios WHERE rol = 'ADMIN';
```

## 📝 Paso 5: Probar el Módulo de Recomendaciones

### 5.1. Acceder al Dashboard

1. Inicia sesión como administrador
2. Serás redirigido al **Dashboard** (`/dashboard`)
3. Verás el botón destacado **"Administrar Recomendaciones de Estudios"**

### 5.2. Crear una Recomendación de Estudios

1. Haz clic en el botón **"Administrar Recomendaciones de Estudios"** o ve a:
   - **http://localhost:8083/admin/recomendaciones-estudios**

2. Haz clic en **"Nueva Recomendación"**

3. Completa el formulario:
   - **Nombre Institución**: Ej: "Universidad Tecnológica Nacional"
   - **Nombre Oferta**: Ej: "Tecnicatura en Programación"
   - **Duración**: Ej: "2 años"
   - **URL Imagen**: Ej: "https://ejemplo.com/imagen.jpg" (opcional)
   - **Descripción**: Ej: "Programa completo de programación..."
   - **Costo**: Ej: 50000.00
   - **Posiciones Laborales**: Selecciona una o más posiciones (si existen)
   - **Activa**: Marca el checkbox

4. Haz clic en **"Crear Recomendación"**

### 5.3. Ver Lista de Recomendaciones

1. Después de crear, serás redirigido a la lista
2. Verás todas las recomendaciones en una tabla
3. Podrás ver:
   - Nombre de institución
   - Nombre de oferta
   - Duración
   - Costo
   - Posiciones vinculadas
   - Estado (Activa/Inactiva)

### 5.4. Editar una Recomendación

1. En la lista, haz clic en **"Editar"** en cualquier recomendación
2. Modifica los campos que desees
3. Haz clic en **"Guardar Cambios"**

### 5.5. Eliminar una Recomendación

1. En la lista, haz clic en **"Eliminar"** en cualquier recomendación
2. Confirma la eliminación
3. La recomendación se desactivará (soft delete)

## 🧪 Paso 6: Probar la API REST (Opcional)

También puedes probar los endpoints REST directamente:

### Obtener todas las recomendaciones
```bash
GET http://localhost:8083/api/recomendaciones-estudios
```

### Obtener una recomendación por ID
```bash
GET http://localhost:8083/api/recomendaciones-estudios/1
```

### Crear una recomendación (POST)
```bash
POST http://localhost:8083/api/recomendaciones-estudios
Content-Type: application/json

{
  "nombreInstitucion": "Instituto Tecnológico",
  "nombreOferta": "Curso de Desarrollo Web",
  "duracion": "6 meses",
  "imagenInstitucion": "https://ejemplo.com/imagen.jpg",
  "descripcion": "Curso completo de desarrollo web",
  "costo": 30000.00,
  "activa": true,
  "posicionesLaboralesIds": [1, 2]
}
```

### Obtener recomendaciones para un candidato
```bash
GET http://localhost:8083/api/recomendaciones-estudios/para-candidato/1
```

## 🔍 Verificar que las Tablas se Crearon

Puedes verificar en PostgreSQL que las tablas se crearon correctamente:

```sql
-- Ver todas las tablas relacionadas
SELECT table_name 
FROM information_schema.tables 
WHERE table_schema = 'public' 
AND table_name LIKE '%recomendacion%';

-- Ver estructura de la tabla principal
\d recomendaciones_estudios

-- Ver tabla de relación
\d recomendaciones_estudios_posiciones

-- Ver datos de ejemplo
SELECT * FROM recomendaciones_estudios;
SELECT * FROM recomendaciones_estudios_posiciones;
```

## 🐛 Solución de Problemas

### Error: "No se puede conectar a la base de datos"
- Verifica que PostgreSQL esté corriendo en el puerto 5433
- Verifica las credenciales en `application.properties`
- Verifica que la base de datos `p5_form_dev` exista

### Error: "Tabla no existe"
- La aplicación debería crear las tablas automáticamente
- Verifica que `spring.jpa.hibernate.ddl-auto=update` esté en `application.properties`
- Reinicia la aplicación

### Error: "Acceso denegado" al intentar acceder a `/admin/recomendaciones-estudios`
- Asegúrate de estar logueado como usuario con rol **ADMIN**
- Verifica en la base de datos: `SELECT * FROM usuarios WHERE rol = 'ADMIN';`

### Error: "No hay posiciones laborales disponibles"
- Necesitas crear posiciones laborales primero
- Las recomendaciones se vinculan con posiciones laborales existentes

## 📊 Datos de Prueba Sugeridos

Para hacer pruebas completas, te sugiero crear:

1. **Al menos 2-3 posiciones laborales** (si no existen):
   - Desarrollador Java Junior
   - Desarrollador Full Stack Senior
   - Analista de Sistemas

2. **Al menos 3-5 recomendaciones de estudios**:
   - Vinculadas con diferentes posiciones
   - Con diferentes costos
   - Algunas activas y otras inactivas

## ✅ Checklist de Pruebas

- [ ] La aplicación inicia correctamente
- [ ] Puedo iniciar sesión como administrador
- [ ] Veo el botón de recomendaciones en el dashboard
- [ ] Puedo crear una nueva recomendación
- [ ] Puedo ver la lista de recomendaciones
- [ ] Puedo editar una recomendación
- [ ] Puedo eliminar (desactivar) una recomendación
- [ ] Puedo vincular recomendaciones con posiciones laborales
- [ ] Las recomendaciones se muestran correctamente en la tabla
- [ ] La API REST funciona correctamente

## 🎯 URLs Importantes

- **Dashboard**: http://localhost:8083/dashboard
- **Login**: http://localhost:8083/login
- **Gestión Recomendaciones**: http://localhost:8083/admin/recomendaciones-estudios
- **Nueva Recomendación**: http://localhost:8083/admin/recomendaciones-estudios/nueva
- **API Base**: http://localhost:8083/api/recomendaciones-estudios

## 💡 Tips

1. **Logs**: Revisa la consola para ver los logs de la aplicación
2. **Base de Datos**: Usa pgAdmin o DBeaver para ver los datos directamente
3. **Navegador**: Abre las herramientas de desarrollador (F12) para ver errores de JavaScript
4. **Postman/Insomnia**: Úsalos para probar la API REST fácilmente

¡Listo para probar! 🚀



