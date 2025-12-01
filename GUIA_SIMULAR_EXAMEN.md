# 🧪 Guía para Simular el Examen en Ambiente Local

Esta guía te ayudará a simular el examen completo desde el inicio hasta ver los resultados y recomendaciones.

## 📋 Flujo Completo del Examen

1. **Paso 1**: Completar datos personales
2. **Paso 2**: Realizar el examen (32 preguntas múltiple choice)
3. **Resultado**: Ver puntuaciones y recomendaciones

---

## 🚀 Paso 1: Iniciar la Aplicación

```bash
# Ejecutar la aplicación
.\ejecutar.bat

# O usando Maven
.\mvnw.cmd spring-boot:run
```

La aplicación estará disponible en: **http://localhost:8083**

---

## 📝 Paso 2: Completar Datos Personales (Paso 1)

### 2.1. Acceder al Formulario

1. Abre tu navegador y ve a: **http://localhost:8083/**
2. Serás redirigido automáticamente al formulario de datos personales
3. O accede directamente a: **http://localhost:8083/paso1**

### 2.2. Completar el Formulario

Completa todos los campos requeridos:

**Datos Personales:**
- **Nombre**: Juan
- **Apellido**: Pérez
- **Email**: juan.perez@ejemplo.com (debe ser único)
- **Teléfono**: 1123456789
- **Fecha de Nacimiento**: 1990-01-15
- **Género**: Masculino
- **CUIL**: 20123456789 (debe ser único)
- **DNI**: 12345678

**Ubicación:**
- **Provincia**: Selecciona una (ej: Buenos Aires)
- **Localidad**: Selecciona una localidad de la provincia
- **Dirección**: Calle Falsa 123

**Información Adicional:**
- **Conocimientos de Programación**: Selecciona un nivel
- **Internet en Hogar**: Sí/No
- **Trabaja Actualmente**: Sí/No
- **Trabaja en Sector IT**: Sí/No (si trabaja actualmente)

### 2.3. Enviar el Formulario

1. Haz clic en **"Continuar al Examen"**
2. Se creará automáticamente un examen para esta persona
3. Serás redirigido al examen: `/examen/{examenId}`

---

## ✍️ Paso 3: Realizar el Examen

### 3.1. Estructura del Examen

El examen consta de:
- **32 preguntas** en total
- **4 áreas de conocimiento**:
  - **Lógica** (8 preguntas)
  - **Matemática** (8 preguntas)
  - **Creatividad** (8 preguntas)
  - **Programación** (8 preguntas)
- **Tiempo límite**: 60 minutos
- **Tipo**: Múltiple choice (4 opciones por pregunta)

### 3.2. Navegación del Examen

- **Ver una pregunta a la vez**
- **Navegación**: Botones "Anterior" y "Siguiente"
- **Barra de progreso**: Muestra el avance
- **Temporizador**: Cuenta regresiva de 60 minutos
- **Preguntas respondidas**: Se marcan automáticamente

### 3.3. Responder las Preguntas

1. Lee cada pregunta cuidadosamente
2. Selecciona la respuesta que consideres correcta
3. Usa los botones de navegación para avanzar/retroceder
4. Puedes cambiar tus respuestas en cualquier momento antes de finalizar

### 3.4. Finalizar el Examen

1. Una vez que hayas respondido todas las preguntas (o cuando quieras terminar)
2. Haz clic en **"Finalizar Examen"**
3. Se calcularán automáticamente las puntuaciones
4. Serás redirigido a la página de resultados

---

## 📊 Paso 4: Ver Resultados

### 4.1. Página de Resultados

Después de finalizar el examen, verás:

**Puntuaciones por Área:**
- Lógica: X%
- Matemática: X%
- Creatividad: X%
- Programación: X%
- **Promedio General**: X%

**Estado:**
- ✅ **Aprobado** (si promedio >= 70)
- ❌ **Reprobado** (si promedio < 70)

### 4.2. Recomendaciones

En la página de resultados también verás:
- **Recomendaciones de Puestos Laborales** (si existen)
- **Recomendaciones de Estudios** (si están vinculadas a los puestos recomendados)

---

## 🔄 Simular Múltiples Exámenes

Para simular múltiples exámenes con diferentes resultados:

### Opción 1: Crear Nuevas Personas

1. Ve a: **http://localhost:8083/paso1**
2. Completa el formulario con **datos diferentes** (email y CUIL únicos)
3. Realiza el examen
4. Repite el proceso

### Opción 2: Usar la API (Avanzado)

Puedes crear personas directamente desde la API:

```bash
POST http://localhost:8083/api/persona/crear
Content-Type: application/json

{
  "nombre": "María",
  "apellido": "González",
  "email": "maria.gonzalez@ejemplo.com",
  "telefono": "1198765432",
  "fechaNacimiento": "1995-05-20",
  "genero": "Femenino",
  "cuil": "27123456789",
  "dni": "87654321",
  "provincia": "Buenos Aires",
  "localidad": "La Plata",
  "direccion": "Av. 7 1234",
  "conocimientosProgramacion": "Intermedio",
  "internetHogar": "Sí",
  "trabajaActualmente": "No"
}
```

Esto creará automáticamente un examen y te devolverá el `examenId`.

---

## 🎯 URLs Importantes para Simular

### Flujo Normal
1. **Inicio**: http://localhost:8083/
2. **Paso 1 (Datos)**: http://localhost:8083/paso1
3. **Examen**: http://localhost:8083/examen/{examenId}
4. **Resultado**: http://localhost:8083/resultado/{personaId}

### Acceso Directo al Examen (si ya tienes un examenId)
- **Examen**: http://localhost:8083/examen/1
  - Reemplaza `1` con el ID del examen que quieras ver

### API para Obtener Preguntas
- **Preguntas del Examen**: http://localhost:8083/api/examen/{examenId}/preguntas
  - Ejemplo: http://localhost:8083/api/examen/1/preguntas

---

## 💡 Tips para Simular Diferentes Escenarios

### Escenario 1: Examen Aprobado (Alto Promedio)
- Responde correctamente la mayoría de las preguntas
- Objetivo: Promedio >= 70%

### Escenario 2: Examen Reprobado (Bajo Promedio)
- Responde incorrectamente la mayoría de las preguntas
- Objetivo: Promedio < 70%

### Escenario 3: Examen con Fortalezas en Área Específica
- Responde bien en un área (ej: Programación)
- Responde mal en otras áreas
- Útil para probar recomendaciones personalizadas

### Escenario 4: Examen Balanceado
- Responde bien en todas las áreas de manera equilibrada
- Útil para ver recomendaciones generales

---

## 🔍 Verificar Datos en la Base de Datos

Puedes verificar los datos directamente en PostgreSQL:

```sql
-- Ver todas las personas
SELECT id, nombre, apellido, email, cuil FROM personas;

-- Ver todos los exámenes
SELECT id, persona_id, logica, matematica, creatividad, programacion, 
       (logica + matematica + creatividad + programacion) / 4.0 as promedio
FROM examenes;

-- Ver respuestas de un examen específico
SELECT re.*, p.enunciado, p.area_conocimiento
FROM respuestas_examen re
JOIN preguntas p ON re.pregunta_id = p.id
WHERE re.examen_id = 1;

-- Ver recomendaciones de estudios para una persona
SELECT re.*, pl.titulo as posicion_titulo
FROM recomendaciones_estudios re
JOIN recomendaciones_estudios_posiciones rep ON re.id = rep.recomendacion_estudios_id
JOIN posiciones_laborales pl ON rep.posicion_laboral_id = pl.id
WHERE re.activa = true;
```

---

## 🐛 Solución de Problemas

### Error: "Examen no encontrado"
- Verifica que el examenId exista en la base de datos
- Asegúrate de que el examen no haya sido completado previamente

### Error: "Este examen ya fue completado"
- El examen ya tiene `fecha_fin` establecida
- Crea un nuevo examen para la misma persona o usa otra persona

### Error: "Email ya existe"
- Usa un email diferente
- O verifica si esa persona ya tiene un examen y accede directamente al resultado

### Error: "No hay preguntas disponibles"
- Verifica que existan preguntas activas en la base de datos
- El sistema debería crear preguntas automáticamente al iniciar

### El examen no carga las preguntas
- Abre la consola del navegador (F12) para ver errores
- Verifica que el endpoint `/api/examen/{examenId}/preguntas` funcione
- Revisa los logs de la aplicación

---

## 📱 Probar desde Diferentes Dispositivos

Para probar la experiencia móvil:

1. Abre las herramientas de desarrollador (F12)
2. Activa el modo de dispositivo móvil
3. Selecciona un dispositivo (iPhone, Android, etc.)
4. Navega al examen y prueba la experiencia

---

## ✅ Checklist de Simulación

- [ ] La aplicación está corriendo en http://localhost:8083
- [ ] Puedo acceder al formulario de datos personales
- [ ] Puedo completar y enviar el formulario
- [ ] Se crea un examen automáticamente
- [ ] Puedo ver las preguntas del examen
- [ ] Puedo navegar entre preguntas
- [ ] Puedo responder las preguntas
- [ ] El temporizador funciona correctamente
- [ ] Puedo finalizar el examen
- [ ] Veo los resultados correctamente
- [ ] Las puntuaciones se calculan bien
- [ ] Veo las recomendaciones de puestos (si aplica)
- [ ] Veo las recomendaciones de estudios (si están vinculadas)

---

## 🎓 Datos de Prueba Sugeridos

### Persona 1: Estudiante con Buen Rendimiento
- Email: estudiante1@ejemplo.com
- CUIL: 20111111111
- Responde bien la mayoría de preguntas
- Resultado esperado: Aprobado con buen promedio

### Persona 2: Profesional con Experiencia
- Email: profesional@ejemplo.com
- CUIL: 20222222222
- Responde muy bien en Programación y Lógica
- Resultado esperado: Aprobado con fortalezas específicas

### Persona 3: Principiante
- Email: principiante@ejemplo.com
- CUIL: 20333333333
- Responde mal la mayoría de preguntas
- Resultado esperado: Reprobado

---

¡Listo para simular! 🚀

Si tienes algún problema, revisa los logs de la aplicación o la consola del navegador para más detalles.

