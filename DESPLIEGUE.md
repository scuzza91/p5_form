# 🚀 Guía de Despliegue a Producción

## 📋 Opción 1: Usando Git (Recomendado)

Si tu código está en un repositorio Git:

### En tu máquina local:

```bash
# 1. Asegúrate de que todos los cambios estén guardados
git add .
git commit -m "Actualización: Cambio de marca a argentinatech y mejoras de UI"
git push origin main  # o master, según tu rama principal
```

### En el servidor de producción:

```bash
# 1. Conectarse al servidor
ssh -i tu-clave.pem ubuntu@tu-ip-servidor

# 2. Ir al directorio del proyecto
cd ~/p5_form  # o la ruta donde está tu proyecto

# 3. Actualizar código desde Git
git pull origin main  # o master

# 4. Ejecutar el script de despliegue
chmod +x desplegar_produccion.sh
./desplegar_produccion.sh

# O manualmente:
docker compose down
docker compose up -d --build
```

---

## 📋 Opción 2: Usando SCP (Sin Git)

Si no usas Git, puedes copiar los archivos directamente:

### En tu máquina local (Windows PowerShell):

```powershell
# Copiar todos los archivos modificados al servidor
scp -i tu-clave.pem -r src/main/resources/templates/* ubuntu@tu-ip-servidor:~/p5_form/src/main/resources/templates/
scp -i tu-clave.pem -r src/main/resources/static/* ubuntu@tu-ip-servidor:~/p5_form/src/main/resources/static/
```

### En el servidor:

```bash
# 1. Ir al directorio del proyecto
cd ~/p5_form

# 2. Ejecutar despliegue
chmod +x desplegar_produccion.sh
./desplegar_produccion.sh

# O manualmente:
docker compose down
docker compose up -d --build
```

---

## 📋 Opción 3: Despliegue Manual (Paso a Paso)

### En el servidor de producción:

```bash
# 1. Conectarse al servidor
ssh -i tu-clave.pem ubuntu@tu-ip-servidor

# 2. Ir al directorio del proyecto
cd ~/p5_form

# 3. Hacer backup de la base de datos (opcional pero recomendado)
docker compose exec postgres pg_dump -U postgres p5_form_prod > backup_$(date +%Y%m%d_%H%M%S).sql

# 4. Detener los servicios
docker compose down

# 5. Si usas Git, actualizar código
git pull origin main  # o master

# 6. Reconstruir las imágenes (esto incluye los nuevos archivos)
docker compose build --no-cache

# 7. Levantar los servicios
docker compose up -d

# 8. Verificar que todo funciona
docker compose ps
docker compose logs -f app
```

---

## ✅ Verificación Post-Despliegue

Después del despliegue, verifica que todo funciona:

```bash
# 1. Ver estado de contenedores
docker compose ps

# 2. Ver logs de la aplicación
docker compose logs -f app

# 3. Probar que la aplicación responde
curl http://localhost:8083

# 4. Verificar desde el navegador
# Abre: http://tu-ip-servidor:8083
```

---

## 🔧 Solución de Problemas

### Si los cambios no se reflejan:

```bash
# Forzar reconstrucción completa
docker compose down
docker compose build --no-cache
docker compose up -d
```

### Si hay errores de compilación:

```bash
# Ver logs detallados
docker compose logs app

# Verificar que los archivos se copiaron correctamente
ls -la src/main/resources/templates/
```

### Si la aplicación no inicia:

```bash
# Verificar variables de entorno
docker compose exec app env | grep SPRING

# Verificar conexión a base de datos
docker compose exec postgres pg_isready -U postgres
```

---

## 📝 Resumen Rápido

**Para despliegue rápido:**

```bash
cd ~/p5_form
git pull  # si usas Git
docker compose down
docker compose up -d --build
docker compose logs -f app
```

---

## ⚠️ Importante

- **Backup**: Siempre haz backup de la base de datos antes de desplegar
- **Horario**: Considera desplegar en horarios de bajo tráfico
- **Testing**: Prueba en un entorno de staging antes de producción
- **Rollback**: Si algo sale mal, puedes restaurar el backup

---

**Última actualización**: Diciembre 2024

