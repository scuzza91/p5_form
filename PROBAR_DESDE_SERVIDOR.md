# 🧪 Probar el Endpoint desde el Servidor (Bash)

## ⚠️ Importante

Los comandos que te di antes son de **PowerShell (Windows)**. En el servidor Linux debes usar **bash/cURL**.

---

## ✅ Opción 1: Usar el Script Bash (Recomendado)

### En el servidor:

```bash
# Dar permisos de ejecución
chmod +x probar-servidor.sh

# Ejecutar el script
./probar-servidor.sh
```

---

## ✅ Opción 2: Comando Directo con cURL

### Comando completo:

```bash
curl -X POST http://localhost:8083/api/persona/crear \
  -H "Content-Type: application/json" \
  -H "X-API-Token: 41855ad220d5c0f4fb39ea6b2ed8d56e" \
  -d '{
    "idStage": "B26F5HRR",
    "custom_B26FNN8U": "Juan",
    "custom_B26FNN83": "Perez",
    "custom_B26FNHKS": "12345678",
    "custom_B26FNN87": "juan.perez.test@example.com",
    "custom_B26FNN8P": "valor_adicional"
  }'
```

### O en una sola línea (más fácil de copiar):

```bash
curl -X POST http://localhost:8083/api/persona/crear -H "Content-Type: application/json" -H "X-API-Token: 41855ad220d5c0f4fb39ea6b2ed8d56e" -d '{"idStage":"B26F5HRR","custom_B26FNN8U":"Juan","custom_B26FNN83":"Perez","custom_B26FNHKS":"12345678","custom_B26FNN87":"juan.perez.test@example.com","custom_B26FNN8P":"valor_adicional"}'
```

---

## 📋 Respuesta Esperada

Si todo funciona, deberías recibir:

```json
{
  "examenId": 123,
  "personaId": 456,
  "mensaje": "Persona y examen creados exitosamente"
}
```

---

## ❌ Errores Comunes

### Error 401: Token no configurado

**Solución:**
```bash
# Accede desde tu navegador a:
http://TU-IP-SERVIDOR:8083/configuracion

# O desde el servidor mismo:
http://localhost:8083/configuracion
```

Ingresa el token: `41855ad220d5c0f4fb39ea6b2ed8d56e`

### Error: jq no está instalado

Si el script dice que `jq` no está instalado:

```bash
# Instalar jq (opcional, solo para formatear JSON)
sudo apt-get update
sudo apt-get install -y jq
```

O simplemente ignora el error, el script funcionará igual.

---

## 🔍 Ver Logs Mientras Pruebas

En otra terminal del servidor:

```bash
docker compose logs -f app
```

Esto te mostrará en tiempo real qué está pasando cuando haces la petición.

---

## 📝 Resumen

1. **En el servidor** usa **bash/cURL**, NO PowerShell
2. **Script bash:** `./probar-servidor.sh`
3. **O comando directo:** `curl -X POST ...`
4. **Ver logs:** `docker compose logs -f app`

---

¡Ahora deberías poder probar correctamente desde el servidor! 🚀



