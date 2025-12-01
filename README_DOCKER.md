# 🐳 Guía de Despliegue con Docker - EC2 Ubuntu 24.04

Esta guía te ayudará a desplegar el sistema de evaluación argentina<strong>tech</strong> en una instancia EC2 de Amazon usando Docker y Docker Compose.

## 📋 Prerrequisitos

- Instancia EC2 con Ubuntu 24.04.3 LTS
- Acceso SSH a la instancia
- Permisos de administrador (sudo)

## 🚀 Instalación en EC2

### Paso 1: Conectarse a la Instancia EC2

```bash
ssh -i tu-clave.pem ubuntu@tu-ip-ec2
```

### Paso 2: Instalar Docker y Docker Compose

```bash
# Actualizar el sistema
sudo apt update && sudo apt upgrade -y

# Instalar dependencias
sudo apt install -y apt-transport-https ca-certificates curl software-properties-common

# Agregar la clave GPG oficial de Docker
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg

# Agregar el repositorio de Docker
echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# Instalar Docker
sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# Verificar instalación
docker --version
docker compose version

# Agregar el usuario actual al grupo docker (para no usar sudo)
sudo usermod -aG docker $USER
newgrp docker

# Verificar que Docker funciona
docker run hello-world
```

### Paso 3: Configurar Security Groups en AWS

En la consola de AWS EC2, asegúrate de que tu Security Group permita:

- **Puerto 22 (SSH)**: Para acceso remoto
- **Puerto 8083 (HTTP)**: Para la aplicación web
- **Puerto 5432 (PostgreSQL)**: Solo si necesitas acceso externo a la BD (recomendado: NO exponerlo públicamente)

**Configuración recomendada del Security Group:**
```
Tipo: SSH
Puerto: 22
Origen: Tu IP / 0.0.0.0/0 (solo para desarrollo)

Tipo: Custom TCP
Puerto: 8083
Origen: 0.0.0.0/0 (o tu IP específica)
```

### Paso 4: Subir el Proyecto a EC2

**Opción A: Usando Git (Recomendado)**

```bash
# En tu EC2
cd ~
git clone <url-de-tu-repositorio>
cd p5_form
```

**Opción B: Usando SCP desde tu máquina local**

```bash
# Desde tu máquina local (Windows PowerShell o Linux/Mac)
scp -i tu-clave.pem -r . ubuntu@tu-ip-ec2:~/p5_form
```

### Paso 5: Configurar Variables de Entorno

```bash
# En la instancia EC2
cd ~/p5_form

# Copiar el archivo de ejemplo
cp .env.example .env

# Editar el archivo .env con tus valores
nano .env
```

**Configuración mínima del archivo `.env`:**

```env
POSTGRES_DB=p5_form_prod
POSTGRES_USER=postgres
POSTGRES_PASSWORD=TU_PASSWORD_SEGURO_AQUI
POSTGRES_PORT=5432

SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8083

SPRING_JPA_DDL_AUTO=update
SPRING_JPA_SHOW_SQL=false

LOGGING_LEVEL=INFO
```

⚠️ **IMPORTANTE**: Cambia `TU_PASSWORD_SEGURO_AQUI` por una contraseña fuerte.

### Paso 6: Construir y Levantar los Contenedores

```bash
# Construir las imágenes y levantar los servicios
docker compose up -d --build

# Ver los logs para verificar que todo funciona
docker compose logs -f
```

Espera a que veas mensajes como:
```
p5_form_app | Started FormularioApplication in X.XXX seconds
```

Presiona `Ctrl+C` para salir de los logs.

### Paso 7: Verificar que Todo Funciona

```bash
# Ver el estado de los contenedores
docker compose ps

# Deberías ver algo como:
# NAME                STATUS          PORTS
# p5_form_app         Up X minutes    0.0.0.0:8083->8083/tcp
# p5_form_postgres    Up X minutes    0.0.0.0:5432->5432/tcp
```

### Paso 8: Acceder a la Aplicación

Abre tu navegador y visita:
```
http://tu-ip-ec2:8083
```

O si configuraste un dominio:
```
http://tu-dominio.com:8083
```

**Credenciales por defecto del administrador:**
- Usuario: `admin`
- Contraseña: `admin123`

⚠️ **IMPORTANTE**: Cambia estas credenciales después del primer acceso.

## 🔧 Comandos Útiles de Docker

### Ver logs
```bash
# Logs de todos los servicios
docker compose logs -f

# Logs solo de la aplicación
docker compose logs -f app

# Logs solo de PostgreSQL
docker compose logs -f postgres
```

### Detener los servicios
```bash
docker compose down
```

### Detener y eliminar volúmenes (⚠️ elimina los datos de la BD)
```bash
docker compose down -v
```

### Reiniciar los servicios
```bash
docker compose restart
```

### Reconstruir después de cambios en el código
```bash
docker compose up -d --build
```

### Ver el uso de recursos
```bash
docker stats
```

### Acceder al contenedor de la aplicación
```bash
docker compose exec app sh
```

### Acceder a PostgreSQL
```bash
docker compose exec postgres psql -U postgres -d p5_form_prod
```

## 🔄 Actualizar la Aplicación

Cuando necesites actualizar el código:

```bash
# 1. Detener los servicios
docker compose down

# 2. Si usas Git, hacer pull
git pull

# 3. Reconstruir y levantar
docker compose up -d --build

# 4. Verificar logs
docker compose logs -f app
```

## 🗄️ Backup de la Base de Datos

### Crear un backup
```bash
docker compose exec postgres pg_dump -U postgres p5_form_prod > backup_$(date +%Y%m%d_%H%M%S).sql
```

### Restaurar un backup
```bash
# Primero, copiar el archivo SQL al contenedor
docker cp backup.sql p5_form_postgres:/tmp/backup.sql

# Luego restaurar
docker compose exec postgres psql -U postgres -d p5_form_prod < /tmp/backup.sql
```

## 🔒 Seguridad Adicional

### 1. Cambiar credenciales por defecto
- Accede al panel de administración
- Cambia la contraseña del usuario `admin`
- Crea usuarios adicionales si es necesario

### 2. Configurar Firewall (UFW)
```bash
# Permitir SSH
sudo ufw allow 22/tcp

# Permitir puerto de la aplicación
sudo ufw allow 8083/tcp

# Activar firewall
sudo ufw enable

# Verificar estado
sudo ufw status
```

### 3. Configurar SSL/HTTPS (Opcional pero Recomendado)

Para producción, considera usar:
- **Nginx como reverse proxy** con Let's Encrypt
- **Cloudflare** para SSL gratuito
- **AWS Application Load Balancer** con certificado SSL

## 🐛 Solución de Problemas

### La aplicación no inicia
```bash
# Ver logs detallados
docker compose logs app

# Verificar que PostgreSQL está corriendo
docker compose ps postgres

# Verificar conectividad entre contenedores
docker compose exec app ping postgres
```

### Error de conexión a la base de datos
```bash
# Verificar variables de entorno
docker compose exec app env | grep SPRING_DATASOURCE

# Verificar que PostgreSQL acepta conexiones
docker compose exec postgres pg_isready -U postgres
```

### Puerto ya en uso
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

## 📊 Monitoreo

### Ver uso de recursos
```bash
docker stats
```

### Ver espacio en disco
```bash
df -h
docker system df
```

## 📞 Soporte

Si encuentras problemas:
1. Revisa los logs: `docker compose logs -f`
2. Verifica las variables de entorno en `.env`
3. Asegúrate de que los Security Groups en AWS están configurados correctamente
4. Verifica que Docker y Docker Compose están instalados correctamente

---

**Versión**: 1.0.0  
**Última actualización**: Diciembre 2024

