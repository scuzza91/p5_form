#!/bin/bash

# Script de despliegue para EC2 Ubuntu 24.04
# Uso: ./deploy.sh

set -e  # Salir si hay algún error

echo "🚀 Iniciando despliegue de P5 Form..."

# Verificar que Docker está instalado
if ! command -v docker &> /dev/null; then
    echo "❌ Docker no está instalado. Por favor, instálalo primero."
    echo "   Ver instrucciones en README_DOCKER.md"
    exit 1
fi

# Verificar que Docker Compose está instalado
if ! command -v docker compose &> /dev/null; then
    echo "❌ Docker Compose no está instalado. Por favor, instálalo primero."
    echo "   Ver instrucciones en README_DOCKER.md"
    exit 1
fi

# Verificar que existe el archivo .env
if [ ! -f .env ]; then
    echo "⚠️  Archivo .env no encontrado."
    if [ -f .env.example ]; then
        echo "📋 Copiando .env.example a .env..."
        cp .env.example .env
        echo "✅ Archivo .env creado. Por favor, edítalo con tus valores antes de continuar."
        echo "   Ejecuta: nano .env"
        exit 1
    else
        echo "❌ No se encontró .env.example. Por favor, crea un archivo .env manualmente."
        exit 1
    fi
fi

# Detener contenedores existentes si están corriendo
echo "🛑 Deteniendo contenedores existentes..."
docker compose down 2>/dev/null || true

# Construir y levantar los contenedores
echo "🔨 Construyendo imágenes..."
docker compose build

echo "🚀 Levantando contenedores..."
docker compose up -d

# Esperar a que los servicios estén listos
echo "⏳ Esperando a que los servicios estén listos..."
sleep 10

# Verificar el estado de los contenedores
echo "📊 Estado de los contenedores:"
docker compose ps

# Mostrar logs
echo ""
echo "📋 Últimos logs de la aplicación:"
echo "   (Presiona Ctrl+C para salir de los logs)"
echo ""
docker compose logs -f app

