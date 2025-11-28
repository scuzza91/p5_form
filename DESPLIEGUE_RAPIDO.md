# 🚀 Guía Rápida de Despliegue con Docker

## 📋 Checklist Pre-Despliegue

- [ ] Tienes acceso a un servidor (EC2, VPS, etc.)
- [ ] Docker y Docker Compose instalados en el servidor
- [ ] Puerto 8083 disponible
- [ ] Acceso SSH al servidor

---

## 🎯 Pasos Rápidos

### 1. Subir el Código al Servidor

**Opción A: Git (Recomendado)**
```bash
# En el servidor
git clone <url-de-tu-repositorio>
cd p5_form
```

**Opción B: SCP desde Windows**
```powershell
# Desde tu máquina local
scp -i tu-clave.pem -r . usuario@tu-servidor:~/p5_form
```

### 2. Configurar Variables de Entorno

```bash
# En el servidor
cd ~/p5_form
cp .env.example .env
nano .env
```

**Edita el archivo `.env` con:**
```env
POSTGRES_DB=p5_form_prod
POSTGRES_USER=postgres
POSTGRES_PASSWORD=TU_PASSWORD_SEGURO_AQUI
POSTGRES_PORT=5432

SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8083

SPRING_JPA_HIBERNATE_DDL_AUTO=update
SPRING_JPA_SHOW_SQL=false

LOGGING_LEVEL=INFO
```

⚠️ **IMPORTANTE:** Cambia `TU_PASSWORD_SEGURO_AQUI` por una contraseña fuerte.

### 3. Construir y Levantar

```bash
# Construir y levantar los contenedores
docker compose up -d --build

# Ver los logs
docker compose logs -f
```

Espera a ver: `Started FormularioApplication in X.XXX seconds`

### 4. Verificar

```bash
# Ver estado de contenedores
docker compose ps

# Deberías ver:
# p5_form_app         Up    0.0.0.0:8083->8083/tcp
# p5_form_postgres   Up    0.0.0.0:5432->5432/tcp
```

### 5. Acceder a la Aplicación

```
http://tu-ip-servidor:8083
```

O si tienes dominio:
```
http://tu-dominio.com:8083
```

**Credenciales por defecto:**
- Usuario: `admin`
- Contraseña: `admin123`

⚠️ **Cambia estas credenciales después del primer acceso.**

---

## 🔐 Configurar Token de Bondarea en Producción

### Opción 1: Desde la UI (Recomendado)

1. Accede a: `http://tu-servidor:8083/configuracion`
2. Inicia sesión como administrador
3. Busca "Token de API - Bondarea"
4. Pega el token: `41855ad220d5c0f4fb39ea6b2ed8d56e`
5. Guarda

### Opción 2: Variable de Entorno

Agrega al archivo `.env`:
```env
API_TOKEN_BONDAREA=41855ad220d5c0f4fb39ea6b2ed8d56e
```

Y actualiza `docker-compose.yml` para pasarlo como variable de entorno.

---

## 🌐 Configurar en Bondarea

Una vez que tu aplicación esté corriendo en el servidor:

1. **Obtén la URL pública:**
   ```
   http://tu-ip-servidor:8083/api/persona/crear
   ```
   O si tienes dominio:
   ```
   https://tu-dominio.com/api/persona/crear
   ```

2. **Configura en Bondarea:**
   - URL: `http://tu-ip-servidor:8083/api/persona/crear`
   - Método: `POST`
   - Headers:
     ```
     Content-Type: application/json
     X-API-Token: 41855ad220d5c0f4fb39ea6b2ed8d56e
     ```
   - Body: JSON con los campos `idStage` y `custom_*`

---

## 🔧 Comandos Útiles

### Ver logs
```bash
docker compose logs -f app
```

### Reiniciar
```bash
docker compose restart
```

### Detener
```bash
docker compose down
```

### Actualizar código
```bash
git pull
docker compose up -d --build
```

### Backup de base de datos
```bash
docker compose exec postgres pg_dump -U postgres p5_form_prod > backup.sql
```

---

## 🔒 Seguridad

### 1. Cambiar credenciales por defecto
- Accede a `/configuracion`
- Cambia la contraseña del usuario `admin`

### 2. Configurar Firewall
```bash
sudo ufw allow 22/tcp    # SSH
sudo ufw allow 8083/tcp  # Aplicación
sudo ufw enable
```

### 3. HTTPS (Recomendado para Producción)

Considera usar:
- **Nginx** como reverse proxy con Let's Encrypt
- **Cloudflare** para SSL gratuito
- **AWS Application Load Balancer** con certificado SSL

---

## ❌ Solución de Problemas

### La aplicación no inicia
```bash
docker compose logs app
```

### Error de conexión a BD
```bash
docker compose logs postgres
docker compose exec app env | grep SPRING_DATASOURCE
```

### Puerto ocupado
```bash
sudo lsof -i :8083
# O cambia SERVER_PORT en .env
```

---

## ✅ Resumen

1. ✅ Sube el código al servidor
2. ✅ Configura `.env`
3. ✅ Ejecuta `docker compose up -d --build`
4. ✅ Accede a `http://tu-servidor:8083`
5. ✅ Configura el token de Bondarea en `/configuracion`
6. ✅ Configura el webhook en Bondarea con la URL pública

¡Listo! Tu aplicación está en producción. 🎉

