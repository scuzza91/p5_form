# 🚀 Guía Completa de Despliegue con Docker

## 📋 Resumen

Esta guía te ayudará a desplegar tu aplicación en un servidor usando Docker, para que Bondarea pueda conectarse desde internet.

---

## 🎯 Opciones de Servidor

### Opción 1: AWS EC2 (Recomendado)
- **Ventaja:** Escalable, confiable, fácil de configurar
- **Costo:** ~$5-10/mes para instancia pequeña
- **Guía:** Ver `README_DOCKER.md`

### Opción 2: VPS (DigitalOcean, Linode, etc.)
- **Ventaja:** Similar a EC2, precios competitivos
- **Costo:** ~$5-10/mes
- **Guía:** Similar a EC2

### Opción 3: Servidor Propio
- **Ventaja:** Control total
- **Costo:** Depende de tu infraestructura
- **Guía:** Similar a EC2

---

## 📦 Paso 1: Preparar el Código

### 1.1. Verificar que todo esté listo

```powershell
# En tu máquina local
cd C:\Users\Francisco\Documents\p5_form

# Verificar que existe Dockerfile y docker-compose.yml
ls Dockerfile, docker-compose.yml
```

### 1.2. Crear archivo .env.example (si no existe)

Ya está creado. Verifica que tenga:
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

---

## 🖥️ Paso 2: Preparar el Servidor

### 2.1. Conectarse al Servidor

```bash
ssh usuario@tu-servidor
# O con clave PEM:
ssh -i tu-clave.pem usuario@tu-servidor
```

### 2.2. Instalar Docker y Docker Compose

**En Ubuntu/Debian:**
```bash
# Actualizar sistema
sudo apt update && sudo apt upgrade -y

# Instalar Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Agregar usuario al grupo docker
sudo usermod -aG docker $USER
newgrp docker

# Verificar instalación
docker --version
docker compose version
```

**En CentOS/RHEL:**
```bash
sudo yum install -y docker
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -aG docker $USER
newgrp docker
```

### 2.3. Configurar Firewall

```bash
# Permitir SSH
sudo ufw allow 22/tcp

# Permitir puerto de la aplicación
sudo ufw allow 8083/tcp

# Activar firewall
sudo ufw enable

# Verificar
sudo ufw status
```

---

## 📤 Paso 3: Subir el Código al Servidor

### Opción A: Usando Git (Recomendado)

```bash
# En el servidor
cd ~
git clone <url-de-tu-repositorio>
cd p5_form
```

### Opción B: Usando SCP desde Windows

```powershell
# Desde tu máquina local
.\subir-servidor.ps1 -Servidor "tu-ip-servidor" -Usuario "ubuntu" -ClavePem "ruta\a\tu-clave.pem"
```

O manualmente:
```powershell
scp -i tu-clave.pem -r . usuario@tu-servidor:~/p5_form
```

### Opción C: Usando WinSCP o FileZilla

1. Conecta al servidor con SFTP
2. Sube todos los archivos a `~/p5_form`

---

## ⚙️ Paso 4: Configurar en el Servidor

### 4.1. Crear archivo .env

```bash
# En el servidor
cd ~/p5_form
cp .env.example .env
nano .env
```

**Edita con tus valores:**
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

⚠️ **IMPORTANTE:** 
- Cambia `TU_PASSWORD_SEGURO_AQUI` por una contraseña fuerte
- Guarda el archivo (Ctrl+X, luego Y, luego Enter en nano)

### 4.2. Construir y Levantar

```bash
# Construir las imágenes
docker compose build

# Levantar los servicios
docker compose up -d

# Ver los logs
docker compose logs -f
```

Espera a ver:
```
p5_form_app | Started FormularioApplication in X.XXX seconds
```

Presiona `Ctrl+C` para salir de los logs.

### 4.3. Verificar Estado

```bash
docker compose ps
```

Deberías ver:
```
NAME                STATUS          PORTS
p5_form_app         Up X minutes    0.0.0.0:8083->8083/tcp
p5_form_postgres    Up X minutes    0.0.0.0:5432->5432/tcp
```

---

## 🌐 Paso 5: Acceder a la Aplicación

### 5.1. Obtener la IP del Servidor

```bash
# En el servidor
curl ifconfig.me
# O
hostname -I
```

### 5.2. Acceder desde el Navegador

```
http://tu-ip-servidor:8083
```

O si tienes dominio:
```
http://tu-dominio.com:8083
```

### 5.3. Iniciar Sesión

- Usuario: `admin`
- Contraseña: `admin123`

⚠️ **Cambia estas credenciales inmediatamente después del primer acceso.**

---

## 🔐 Paso 6: Configurar Token de Bondarea

### 6.1. Desde la UI

1. Accede a: `http://tu-servidor:8083/configuracion`
2. Inicia sesión como administrador
3. Busca "Token de API - Bondarea"
4. Pega el token: `41855ad220d5c0f4fb39ea6b2ed8d56e`
5. Guarda

### 6.2. Verificar

Deberías ver: ✅ "Token configurado (oculto por seguridad)"

---

## 🔗 Paso 7: Configurar en Bondarea

### 7.1. Obtener URL Pública

Tu URL del endpoint será:
```
http://tu-ip-servidor:8083/api/persona/crear
```

O si tienes dominio:
```
https://tu-dominio.com/api/persona/crear
```

### 7.2. Configurar Webhook en Bondarea

1. Accede a Bondarea: `https://argentinatech.bondarea.com`
2. Ve a Webhooks/Integraciones
3. Configura:
   - **URL:** `http://tu-ip-servidor:8083/api/persona/crear`
   - **Método:** `POST`
   - **Headers:**
     ```
     Content-Type: application/json
     X-API-Token: 41855ad220d5c0f4fb39ea6b2ed8d56e
     ```
   - **Body:** JSON con `idStage` y campos `custom_*`

### 7.3. Probar

Crea un caso de prueba en Bondarea y verifica que se reciba en tu aplicación.

---

## 🔧 Comandos Útiles

### Ver logs
```bash
# Todos los servicios
docker compose logs -f

# Solo la aplicación
docker compose logs -f app

# Solo PostgreSQL
docker compose logs -f postgres
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
# Si usas Git
git pull
docker compose up -d --build

# Si subes manualmente
docker compose down
# Sube los nuevos archivos
docker compose up -d --build
```

### Backup de base de datos
```bash
docker compose exec postgres pg_dump -U postgres p5_form_prod > backup_$(date +%Y%m%d).sql
```

### Restaurar backup
```bash
docker compose exec -T postgres psql -U postgres p5_form_prod < backup.sql
```

---

## 🔒 Seguridad Adicional

### 1. Cambiar credenciales por defecto
- Accede a `/configuracion`
- Cambia la contraseña del usuario `admin`

### 2. Configurar HTTPS (Recomendado)

**Opción A: Nginx + Let's Encrypt**
```bash
# Instalar Nginx
sudo apt install nginx certbot python3-certbot-nginx

# Configurar certificado SSL
sudo certbot --nginx -d tu-dominio.com
```

**Opción B: Cloudflare**
- Configura tu dominio en Cloudflare
- Activa SSL/TLS automático

### 3. Restringir acceso a PostgreSQL
- No expongas el puerto 5432 públicamente
- Solo accesible desde la red interna de Docker

---

## ❌ Solución de Problemas

### La aplicación no inicia
```bash
# Ver logs detallados
docker compose logs app

# Verificar que PostgreSQL está corriendo
docker compose ps postgres

# Verificar variables de entorno
docker compose exec app env | grep SPRING
```

### Error de conexión a BD
```bash
# Verificar que PostgreSQL acepta conexiones
docker compose exec postgres pg_isready -U postgres

# Verificar variables de entorno
docker compose exec app env | grep SPRING_DATASOURCE
```

### Puerto ocupado
```bash
# Ver qué está usando el puerto
sudo lsof -i :8083

# O cambiar el puerto en .env
SERVER_PORT=8084
```

### Limpiar todo y empezar de nuevo
```bash
# ⚠️ ADVERTENCIA: Esto elimina TODOS los datos
docker compose down -v
docker system prune -a
```

---

## ✅ Checklist Final

- [ ] Docker y Docker Compose instalados
- [ ] Código subido al servidor
- [ ] Archivo `.env` configurado
- [ ] Contenedores corriendo (`docker compose ps`)
- [ ] Aplicación accesible en `http://tu-servidor:8083`
- [ ] Token de Bondarea configurado en `/configuracion`
- [ ] Webhook configurado en Bondarea
- [ ] Prueba realizada desde Bondarea
- [ ] Datos recibidos correctamente

---

## 📝 Resumen de URLs

| Tipo | URL |
|------|-----|
| **Aplicación** | `http://tu-ip-servidor:8083` |
| **Endpoint API** | `http://tu-ip-servidor:8083/api/persona/crear` |
| **Configuración** | `http://tu-ip-servidor:8083/configuracion` |
| **Inscripciones** | `http://tu-ip-servidor:8083/inscripciones` |

---

¡Listo! Tu aplicación está en producción y lista para recibir datos de Bondarea. 🎉

