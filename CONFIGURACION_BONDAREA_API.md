# 🔧 Configuración de la API de Bondarea para GET

## 📋 Situación Actual

He implementado la integración con la API de Bondarea para obtener datos de solicitudes de financiamiento cuando no se encuentran localmente. El servicio intenta múltiples patrones de URL comunes hasta encontrar uno que funcione.

## 🔍 Cómo Encontrar la URL Correcta

### Paso 1: Acceder a la Documentación

1. Accede a: `https://www.bondarea.com/?c=comunidad&v=arm_rep&idreport=api_doc`
2. Inicia sesión con tus credenciales de Bondarea
3. Busca la sección de **"Solicitud de Financiamiento"** o **"GET" endpoints**

### Paso 2: Identificar el Endpoint Correcto

Busca en la documentación:
- El endpoint para obtener datos de una solicitud por ID
- La URL base de la API (ej: `https://www.bondarea.com/api` o `https://argentinatech.bondarea.com/api`)
- El formato del endpoint (ej: `/solicitud-financiamiento/{id}` o `/stage/{id}`)

### Paso 3: Verificar los Headers Requeridos

La documentación debería indicar:
- Qué headers son necesarios (probablemente `X-API-Token` o `Authorization: Bearer`)
- El formato del token
- Si requiere otros headers adicionales

## 🔧 Configuración Actual

El servicio `BondareaService` intenta automáticamente estos patrones de URL:

```java
- https://www.bondarea.com/api/solicitud-financiamiento/{idStage}
- https://www.bondarea.com/api/stage/{idStage}
- https://www.bondarea.com/api/stages/{idStage}
- https://argentinatech.bondarea.com/api/solicitud-financiamiento/{idStage}
- https://argentinatech.bondarea.com/api/stage/{idStage}
- https://api.bondarea.com/solicitud-financiamiento/{idStage}
- https://api.bondarea.com/stage/{idStage}
```

## ✅ Cómo Actualizar la URL Correcta

Una vez que identifiques la URL correcta de la documentación:

### Opción 1: Editar el Código (Recomendado para producción)

1. Abre: `src/main/java/com/formulario/service/BondareaService.java`
2. Busca el array `BONDAREA_API_URL_PATTERNS`
3. Coloca la URL correcta **al principio** del array para que se intente primero:

```java
private static final String[] BONDAREA_API_URL_PATTERNS = {
    "https://URL-CORRECTA-DE-LA-DOCUMENTACION/{idStage}",  // ← Agregar aquí primero
    "https://www.bondarea.com/api/solicitud-financiamiento/{idStage}",
    // ... resto de patrones
};
```

### Opción 2: Verificar en los Logs

Después de desplegar, revisa los logs para ver qué URL está funcionando:

```bash
docker compose logs -f app | grep -i bondarea
```

Busca líneas como:
```
Datos obtenidos exitosamente de Bondarea desde URL: https://...
```

## 🧪 Probar la Integración

### Desde el Navegador

1. Accede a: `http://34.238.57.131:8083/prueba.html?id=128379`
2. Haz clic en "Obtener Datos (GET)"
3. Revisa la respuesta

### Desde la Terminal

```bash
curl http://34.238.57.131:8083/debug/examen/128379
```

### Respuesta Esperada

Si funciona correctamente, deberías ver algo como:

```json
{
  "source": "bondarea",
  "idStage": "128379",
  "status": "OK",
  "message": "Datos obtenidos desde Bondarea",
  "datos": {
    "custom_B26FNN8U": "Nombre",
    "custom_B26FNN83": "Apellido",
    ...
  }
}
```

## 🔍 Debugging

Si no funciona, revisa los logs:

```bash
# Ver todos los logs
docker compose logs app

# Filtrar solo logs de Bondarea
docker compose logs app | grep -i bondarea

# Ver logs en tiempo real
docker compose logs -f app | grep -i bondarea
```

### Errores Comunes

1. **Token no configurado**
   - Solución: Configurar el token en `/configuracion`

2. **URL incorrecta (404)**
   - Solución: Actualizar `BONDAREA_API_URL_PATTERNS` con la URL correcta

3. **Error de autenticación (401/403)**
   - Solución: Verificar que el token sea correcto y esté bien configurado

4. **Timeout o conexión rechazada**
   - Solución: Verificar que la URL sea accesible desde el servidor

## 📝 Notas Importantes

- El servicio intenta automáticamente múltiples URLs hasta encontrar una que funcione
- Los logs mostrarán qué URL está siendo intentada
- Si ninguna URL funciona, retornará `null` y el endpoint mostrará "NOT_FOUND"
- El token debe estar configurado en `/configuracion` antes de usar esta funcionalidad

## 🚀 Próximos Pasos

1. ✅ Revisar la documentación de Bondarea (requiere login)
2. ✅ Identificar la URL exacta del endpoint GET
3. ✅ Actualizar `BONDAREA_API_URL_PATTERNS` con la URL correcta
4. ✅ Probar con el ID `128379` en producción
5. ✅ Verificar los logs para confirmar que funciona

---

**Última actualización**: Diciembre 2024

