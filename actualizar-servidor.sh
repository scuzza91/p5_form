#!/bin/bash

# Script para actualizar la aplicación en el servidor
# Uso: ./actualizar-servidor.sh

set -e

echo "🔄 Actualizando aplicación en el servidor..."
echo ""

# Ir al directorio del proyecto
cd ~/p5_form || { echo "❌ Error: No se encontró el directorio ~/p5_form"; exit 1; }

# Si usa Git, hacer pull
if [ -d .git ]; then
    echo "📥 Actualizando código desde Git..."
    git pull origin main || git pull origin master
    echo ""
fi

# Detener contenedores
echo "🛑 Deteniendo contenedores..."
docker compose down

# Reconstruir imágenes (importante: --build)
echo "🔨 Reconstruyendo imágenes con los nuevos cambios..."
docker compose build --no-cache

# Levantar contenedores
echo "🚀 Levantando contenedores..."
docker compose up -d

# Esperar un momento
echo "⏳ Esperando a que los servicios estén listos..."
sleep 10

# Verificar estado
echo ""
echo "📊 Estado de los contenedores:"
docker compose ps

# Mostrar logs
echo ""
echo "📋 Últimos logs (presiona Ctrl+C para salir):"
echo ""
docker compose logs -f app


