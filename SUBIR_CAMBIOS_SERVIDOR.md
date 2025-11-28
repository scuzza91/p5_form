# 📤 Cómo Subir Cambios al Servidor

## 🎯 Opción 1: Usando Git (Recomendado)

Si tu servidor tiene acceso al repositorio Git:

### Paso 1: Hacer Push al Repositorio

```powershell
# En tu máquina local
git push origin main
# O si tu rama se llama diferente:
git push origin master
```

### Paso 2: En el Servidor

```bash
# Conectarse al servidor
ssh usuario@tu-servidor

# Ir al directorio del proyecto
cd ~/p5_form

# Hacer pull de los cambios
git pull origin main
# O:
git pull origin master

# Reconstruir y reiniciar los contenedores
docker compose down
docker compose up -d --build

# Ver los logs
docker compose logs -f app
```

---

## 🎯 Opción 2: Usando SCP (Si no usas Git en el servidor)

### Paso 1: Hacer Push al Repositorio (Opcional)

```powershell
git push origin main
```

### Paso 2: Subir Archivos con SCP

**Usando el script:**
```powershell
.\subir-servidor.ps1 -Servidor "tu-ip-servidor" -Usuario "ubuntu" -ClavePem "ruta\a\tu-clave.pem"
```

**O manualmente:**
```powershell
scp -i tu-clave.pem -r . usuario@tu-servidor:~/p5_form
```

### Paso 3: En el Servidor

```bash
# Conectarse al servidor
ssh usuario@tu-servidor

# Ir al directorio
cd ~/p5_form

# Reconstruir y reiniciar
docker compose down
docker compose up -d --build

# Ver logs
docker compose logs -f app
```

---

## 🎯 Opción 3: Solo Archivos Modificados (Más Rápido)

Si solo cambiaste algunos archivos, puedes subir solo esos:

```powershell
# Ver qué archivos cambiaron
git status

# Subir solo archivos específicos
scp -i tu-clave.pem src/main/java/com/formulario/model/PersonaApiDTO.java usuario@servidor:~/p5_form/src/main/java/com/formulario/model/
scp -i tu-clave.pem src/main/java/com/formulario/controller/FormularioController.java usuario@servidor:~/p5_form/src/main/java/com/formulario/controller/
```

Luego en el servidor:
```bash
cd ~/p5_form
docker compose restart app
# O si necesitas recompilar:
docker compose up -d --build app
```

---

## ⚡ Método Rápido Recomendado

### 1. Push a Git
```powershell
git push origin main
```

### 2. En el Servidor (SSH)
```bash
cd ~/p5_form
git pull
docker compose up -d --build
docker compose logs -f app
```

---

## 🔄 Actualización Automática (Opcional)

Puedes crear un script en el servidor para actualizar automáticamente:

```bash
# En el servidor: ~/p5_form/actualizar.sh
#!/bin/bash
cd ~/p5_form
git pull
docker compose down
docker compose up -d --build
docker compose logs -f app
```

Hacer ejecutable:
```bash
chmod +x actualizar.sh
```

Usar:
```bash
./actualizar.sh
```

---

## ✅ Verificar que Funcionó

1. **Ver logs:**
   ```bash
   docker compose logs -f app
   ```
   Deberías ver: `Started FormularioApplication`

2. **Probar el endpoint:**
   ```powershell
   # Desde tu máquina local
   $url = "http://tu-ip-servidor:8083/api/persona/crear"
   # ... (usar el script de prueba)
   ```

3. **Acceder a la aplicación:**
   ```
   http://tu-ip-servidor:8083
   ```

---

## 📝 Resumen de Comandos

**Local (Windows):**
```powershell
git add .
git commit -m "Mensaje del commit"
git push origin main
```

**Servidor (Linux):**
```bash
cd ~/p5_form
git pull
docker compose up -d --build
docker compose logs -f app
```

¡Listo! Tus cambios están en producción. 🚀

