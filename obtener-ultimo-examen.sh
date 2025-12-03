#!/bin/bash

# Script para obtener el último examen creado y generar el link
# Uso: ./obtener-ultimo-examen.sh

echo "=== Obteniendo último examen creado ==="
echo ""

# Obtener el último examen ID desde la base de datos
EXAMEN_ID=$(docker compose exec -T postgres psql -U postgres -d p5_form_prod -t -c "SELECT id FROM examen ORDER BY id DESC LIMIT 1;" | tr -d ' ')

if [ -z "$EXAMEN_ID" ] || [ "$EXAMEN_ID" = "" ]; then
    echo "❌ No se encontraron exámenes en la base de datos"
    exit 1
fi

# Obtener la IP pública del servidor (si está disponible)
SERVER_IP=$(curl -s ifconfig.me 2>/dev/null || echo "34.238.57.131")

echo "✅ Último examen encontrado: ID $EXAMEN_ID"
echo ""
echo "🔗 Link para probar el examen:"
echo ""
echo "   http://${SERVER_IP}:8083/examen/${EXAMEN_ID}"
echo ""
echo "   O desde el servidor mismo:"
echo "   http://localhost:8083/examen/${EXAMEN_ID}"
echo ""

# Obtener información adicional del examen
PERSONA_ID=$(docker compose exec -T postgres psql -U postgres -d p5_form_prod -t -c "SELECT persona_id FROM examen WHERE id = $EXAMEN_ID;" | tr -d ' ')

if [ -n "$PERSONA_ID" ] && [ "$PERSONA_ID" != "" ]; then
    PERSONA_INFO=$(docker compose exec -T postgres psql -U postgres -d p5_form_prod -t -c "SELECT nombre || ' ' || apellido || ' (' || email || ')' FROM persona WHERE id = $PERSONA_ID;" | tr -d ' ')
    echo "📋 Información del examen:"
    echo "   Persona ID: $PERSONA_ID"
    echo "   Persona: $PERSONA_INFO"
    echo ""
fi





