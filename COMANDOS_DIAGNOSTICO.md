# 🔍 Comandos para Diagnosticar Error 405 en el Servidor

## 1. Ver logs completos con toda la información del request

```bash
docker compose logs -f app | grep -A 10 "REQUEST RECIBIDO"
```

Este comando mostrará las 10 líneas después de "REQUEST RECIBIDO", incluyendo:
- Método HTTP
- URI
- Content-Type
- Headers

---

## 2. Ver todos los logs recientes del error 405

```bash
docker compose logs app --tail=100 | grep -i "405\|Method Not Allowed\|REQUEST RECIBIDO"
```

---

## 3. Ver logs en tiempo real (sin filtro)

```bash
docker compose logs -f app
```

Presiona `Ctrl+C` para salir.

---

## 4. Ver logs solo de errores HTTP

```bash
docker compose logs app --tail=200 | grep -E "405|Method|ERROR|WARN"
```

---

## 5. Probar el endpoint directamente desde el servidor

```bash
curl -X POST http://localhost:8083/api/persona/crear \
  -H "Content-Type: application/json" \
  -H "X-API-Token: 41855ad220d5c0f4fb39ea6b2ed8d56e" \
  -d '{"idCaso":"TEST123","idStage":"B26F5HRR"}'
```

---

## 6. Probar con método OPTIONS (preflight CORS)

```bash
curl -X OPTIONS http://localhost:8083/api/persona/crear \
  -H "Origin: https://argentinatech.bondarea.com" \
  -H "Access-Control-Request-Method: POST" \
  -H "Access-Control-Request-Headers: Content-Type,X-API-Token" \
  -v
```

El flag `-v` muestra información detallada de la respuesta.

---

## 7. Ver qué método HTTP está llegando realmente

```bash
docker compose logs app --tail=50 | grep -E "Método HTTP|URI:|Content-Type"
```

---

## 8. Monitorear logs en tiempo real y guardar en archivo

```bash
docker compose logs -f app 2>&1 | tee /tmp/app_logs.txt
```

Luego puedes revisar el archivo:
```bash
cat /tmp/app_logs.txt | grep -A 10 "REQUEST RECIBIDO"
```

---

## 9. Verificar que el contenedor está corriendo

```bash
docker compose ps
```

Deberías ver algo como:
```
NAME                STATUS          PORTS
p5_form_app         Up X minutes    0.0.0.0:8083->8083/tcp
```

---

## 10. Reiniciar la aplicación después de los cambios

```bash
docker compose restart app
```

O si necesitas reconstruir:
```bash
docker compose up -d --build app
```

---

## 11. Ver logs de Spring Boot específicos del endpoint

```bash
docker compose logs app | grep -E "FormularioController|persona/crear|405"
```

---

## 12. Ver logs del error 405 con toda la información (NUEVO)

Ahora cuando ocurra un error 405, verás información detallada:

```bash
docker compose logs app | grep -A 15 "ERROR 405 - METHOD NOT ALLOWED"
```

Esto mostrará:
- Método HTTP recibido
- Métodos permitidos
- URI completa
- Headers (User-Agent, Origin, Referer)
- IP del cliente

---

## 📋 Comando Recomendado (Todo en uno)

Ejecuta este comando para ver toda la información relevante:

```bash
docker compose logs app --tail=200 | grep -E "REQUEST RECIBIDO|Método HTTP|URI:|Content-Type|405|Method Not Allowed" -A 5
```

---

## 🔧 Si el problema persiste

1. **Verificar la configuración de Bondarea:**
   - Asegúrate de que el método sea **POST** (no GET)
   - Verifica que la URL sea correcta: `http://tu-servidor:8083/api/persona/crear`

2. **Verificar si hay un proxy reverso (nginx/Apache):**
   ```bash
   # Ver si nginx está corriendo
   sudo systemctl status nginx
   
   # Ver configuración de nginx
   sudo cat /etc/nginx/sites-enabled/default | grep -A 10 "location"
   ```

3. **Probar desde fuera del servidor:**
   ```bash
   # Desde tu máquina local, reemplaza IP_SERVIDOR con la IP real
   curl -X POST http://IP_SERVIDOR:8083/api/persona/crear \
     -H "Content-Type: application/json" \
     -H "X-API-Token: 41855ad220d5c0f4fb39ea6b2ed8d56e" \
     -d '{"idCaso":"TEST123"}' \
     -v
   ```

