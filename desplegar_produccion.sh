#!/bin/bash

# Script de Despliegue a Producción
# Este script actualiza la aplicación en el servidor de producción

echo "========================================="
echo "  Despliegue a Producción"
echo "  Sistema argentinatech"
echo "========================================="
echo ""

# Colores para output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Verificar que estamos en el directorio correcto
if [ ! -f "docker-compose.yml" ]; then
    echo -e "${RED}Error: No se encontró docker-compose.yml${NC}"
    echo "Asegúrate de estar en el directorio del proyecto"
    exit 1
fi

# Paso 1: Detener los servicios actuales
echo -e "${YELLOW}[1/5] Deteniendo servicios actuales...${NC}"
docker compose down

# Paso 2: Si se usa Git, actualizar código
if [ -d ".git" ]; then
    echo -e "${YELLOW}[2/5] Actualizando código desde Git...${NC}"
    git pull origin main || git pull origin master
else
    echo -e "${YELLOW}[2/5] No se detectó repositorio Git, continuando...${NC}"
fi

# Paso 3: Reconstruir las imágenes
echo -e "${YELLOW}[3/5] Reconstruyendo imágenes Docker...${NC}"
docker compose build --no-cache

# Paso 4: Levantar los servicios
echo -e "${YELLOW}[4/5] Iniciando servicios...${NC}"
docker compose up -d

# Paso 5: Verificar que todo funciona
echo -e "${YELLOW}[5/5] Verificando estado de los servicios...${NC}"
sleep 5

# Verificar estado de contenedores
if docker compose ps | grep -q "Up"; then
    echo -e "${GREEN}✓ Servicios iniciados correctamente${NC}"
else
    echo -e "${RED}✗ Error: Algunos servicios no se iniciaron correctamente${NC}"
    echo "Revisa los logs con: docker compose logs"
    exit 1
fi

# Mostrar estado final
echo ""
echo -e "${GREEN}=========================================${NC}"
echo -e "${GREEN}  Despliegue completado exitosamente${NC}"
echo -e "${GREEN}=========================================${NC}"
echo ""
echo "Estado de los contenedores:"
docker compose ps
echo ""
echo "Para ver los logs en tiempo real:"
echo "  docker compose logs -f app"
echo ""
echo "Para verificar que la aplicación funciona:"
echo "  curl http://localhost:8083"
echo ""

