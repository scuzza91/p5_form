# 🔄 Actualizar Aplicación en el Servidor

## ❌ Problema: Ves la Versión Vieja

Esto significa que los contenedores Docker están usando la imagen antigua. Necesitas **reconstruir** las imágenes.

---

## ✅ Solución Rápida

### En el Servidor (SSH):

```bash
# Conectarse al servidor
ssh usuario@tu-servidor

# Ir al directorio
cd ~/p5_form

# Si usas Git, actualizar código
git pull origin main

# IMPORTANTE: Reconstruir las imágenes
docker compose down
docker compose build --no-cache
docker compose up -d

# Ver logs para verificar
docker compose logs -f app
```

---

## 🔍 Verificar que Funcionó

### 1. Ver los Logs

```bash
docker compose logs -f app
```

Deberías ver:
```
Started FormularioApplication in X.XXX seconds
```

### 2. Verificar Versión

Puedes agregar un endpoint de versión o simplemente verificar que los cambios nuevos funcionan.

### 3. Probar el Endpoint

```powershell
# Desde tu máquina local
.\scripts\test-simple.ps1
```

Pero cambiando la URL a:
```powershell
$url = "http://tu-ip-servidor:8083/api/persona/crear"
```

---

## 🎯 Comandos Completos

### Opción 1: Manual

```bash
cd ~/p5_form
git pull
docker compose down
docker compose build --no-cache
docker compose up -d
docker compose logs -f app
```

### Opción 2: Usando el Script

```bash
# Subir el script al servidor primero
# Luego en el servidor:
chmod +x actualizar-servidor.sh
./actualizar-servidor.sh
```

---

## ⚠️ Importante

**Siempre usa `--no-cache` al reconstruir** para asegurarte de que se usan los nuevos cambios:

```bash
docker compose build --no-cache
```

O simplemente:
```bash
docker compose up -d --build --force-recreate
```

---

## 🔄 Flujo Completo de Actualización

```bash
# 1. En tu máquina local
git add .
git commit -m "Descripción de cambios"
git push origin main

# 2. En el servidor
cd ~/p5_form
git pull
docker compose down
docker compose build --no-cache
docker compose up -d
docker compose logs -f app
```

---

## ❓ ¿Por Qué Veo la Versión Vieja?

**Causa:** Docker usa imágenes en caché. Cuando cambias el código, necesitas reconstruir la imagen.

**Solución:** Siempre usa `--build` o `--no-cache` al actualizar.

---

## ✅ Verificar que Está Actualizado

1. **Ver fecha de compilación en logs:**
   ```bash
   docker compose logs app | grep "Started"
   ```

2. **Probar funcionalidad nueva:**
   - Si agregaste un endpoint nuevo, pruébalo
   - Si cambiaste un comportamiento, verifica que funcione

3. **Ver versión del código:**
   ```bash
   docker compose exec app ls -la /app/app.jar
   ```

---

¡Después de reconstruir, deberías ver la versión nueva! 🚀


